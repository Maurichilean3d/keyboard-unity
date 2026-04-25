package com.keyboardunity

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppQosSettings
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.concurrent.Executors

class BluetoothHidService : Service() {

    companion object {
        private const val TAG = "BtHidService"
        private const val CHANNEL_ID = "keyboard_unity_channel"
        private const val NOTIFICATION_ID = 1
    }

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothHidService = this@BluetoothHidService
    }

    private val binder = LocalBinder()
    private var hidDevice: BluetoothHidDevice? = null
    private var connectedDevice: BluetoothDevice? = null
    private var isRegistered = false

    // Listeners notified on state changes
    private val listeners = mutableListOf<ConnectionListener>()

    interface ConnectionListener {
        fun onConnectionStateChanged(connected: Boolean, deviceName: String)
        fun onRegistered(registered: Boolean)
    }

    fun addListener(l: ConnectionListener) { listeners.add(l) }
    fun removeListener(l: ConnectionListener) { listeners.remove(l) }

    fun isConnected() = connectedDevice != null
    fun getConnectedDeviceName() = connectedDevice?.name ?: ""
    fun isAppRegistered() = isRegistered

    // ── HID callbacks ──────────────────────────────────────────────────────

    private val hidCallback = object : BluetoothHidDevice.Callback() {

        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            Log.d(TAG, "onAppStatusChanged registered=$registered")
            isRegistered = registered
            listeners.forEach { it.onRegistered(registered) }
            updateNotification()
        }

        override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
            Log.d(TAG, "onConnectionStateChanged state=$state device=${device.name}")
            when (state) {
                BluetoothProfile.STATE_CONNECTED -> {
                    connectedDevice = device
                    listeners.forEach { it.onConnectionStateChanged(true, device.name ?: "Unknown") }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    connectedDevice = null
                    listeners.forEach { it.onConnectionStateChanged(false, "") }
                }
            }
            updateNotification()
        }

        override fun onGetReport(device: BluetoothDevice, type: Byte, id: Byte, bufferSize: Int) {
            val hid = hidDevice ?: return
            when (id.toInt()) {
                HidDescriptor.REPORT_ID_KEYBOARD ->
                    hid.replyReport(device, type, id, ByteArray(8))
                HidDescriptor.REPORT_ID_MOUSE ->
                    hid.replyReport(device, type, id, ByteArray(4))
                else ->
                    hid.reportError(device, BluetoothHidDevice.ERROR_RSP_INVALID_RPT_ID)
            }
        }

        override fun onSetReport(device: BluetoothDevice, type: Byte, id: Byte, data: ByteArray) {
            hidDevice?.replyReport(device, type, id, ByteArray(0))
        }

        override fun onSetProtocol(device: BluetoothDevice, protocol: Byte) {
            Log.d(TAG, "onSetProtocol protocol=$protocol")
        }

        override fun onVirtualCableUnplug(device: BluetoothDevice) {
            Log.d(TAG, "onVirtualCableUnplug")
            connectedDevice = null
            listeners.forEach { it.onConnectionStateChanged(false, "") }
        }
    }

    // ── Profile proxy listener ─────────────────────────────────────────────

    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile != BluetoothProfile.HID_DEVICE) return
            Log.d(TAG, "HID_DEVICE profile connected")
            hidDevice = proxy as BluetoothHidDevice
            registerHidApp()
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile != BluetoothProfile.HID_DEVICE) return
            Log.d(TAG, "HID_DEVICE profile disconnected")
            hidDevice = null
            isRegistered = false
        }
    }

    // ── Service lifecycle ──────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Initialising…"))
        setupBluetooth()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        hidDevice?.let { hid ->
            if (isRegistered) hid.unregisterApp()
            getBluetoothAdapter()?.closeProfileProxy(BluetoothProfile.HID_DEVICE, hid)
        }
    }

    // ── Bluetooth setup ────────────────────────────────────────────────────

    private fun setupBluetooth() {
        val adapter = getBluetoothAdapter() ?: run {
            Log.e(TAG, "Bluetooth not available")
            return
        }
        adapter.getProfileProxy(this, profileListener, BluetoothProfile.HID_DEVICE)
    }

    private fun registerHidApp() {
        val hid = hidDevice ?: return

        val sdp = BluetoothHidDeviceAppSdpSettings(
            "Keyboard Unity",
            "BT Keyboard + Trackpad",
            "KeyboardUnity",
            BluetoothHidDevice.SUBCLASS1_COMBO,
            HidDescriptor.COMBINED_DESCRIPTOR
        )

        val qos = BluetoothHidDeviceAppQosSettings(
            BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT,
            800,
            9,
            0,
            11250,
            BluetoothHidDeviceAppQosSettings.MAX
        )

        hid.registerApp(sdp, null, qos, Executors.newSingleThreadExecutor(), hidCallback)
    }

    // ── Public API for UI ──────────────────────────────────────────────────

    fun connectToDevice(device: BluetoothDevice): Boolean {
        return hidDevice?.connect(device) ?: false
    }

    fun disconnect() {
        connectedDevice?.let { hidDevice?.disconnect(it) }
    }

    fun getPairedDevices(): Set<BluetoothDevice> {
        return try {
            getBluetoothAdapter()?.bondedDevices ?: emptySet()
        } catch (e: SecurityException) {
            emptySet()
        }
    }

    fun sendKeyReport(modifier: Int, keyCodes: List<Int>) {
        val device = connectedDevice ?: return
        val hid = hidDevice ?: return
        val report = ByteArray(8)
        report[0] = modifier.toByte()
        report[1] = 0
        keyCodes.take(6).forEachIndexed { i, code -> report[2 + i] = code.toByte() }
        try {
            hid.sendReport(device, HidDescriptor.REPORT_ID_KEYBOARD, report)
        } catch (e: SecurityException) {
            Log.e(TAG, "sendKeyReport: $e")
        }
    }

    fun sendMouseReport(buttons: Int, dx: Int, dy: Int, scroll: Int) {
        val device = connectedDevice ?: return
        val hid = hidDevice ?: return
        val report = ByteArray(4)
        report[0] = buttons.toByte()
        report[1] = dx.coerceIn(-127, 127).toByte()
        report[2] = dy.coerceIn(-127, 127).toByte()
        report[3] = scroll.coerceIn(-127, 127).toByte()
        try {
            hid.sendReport(device, HidDescriptor.REPORT_ID_MOUSE, report)
        } catch (e: SecurityException) {
            Log.e(TAG, "sendMouseReport: $e")
        }
    }

    // ── Notification helpers ───────────────────────────────────────────────

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Keyboard Unity",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Bluetooth HID connection status" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(statusText: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Keyboard Unity")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification() {
        val text = when {
            connectedDevice != null -> "Connected to ${connectedDevice?.name}"
            isRegistered            -> "Ready – waiting for connection"
            else                    -> "Initialising Bluetooth HID…"
        }
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, buildNotification(text))
    }

    private fun getBluetoothAdapter(): BluetoothAdapter? =
        (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
}
