package com.keyboardunity

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.keyboardunity.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var hidService: BluetoothHidService? = null
    private var serviceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            hidService = (binder as BluetoothHidService.LocalBinder).getService()
            serviceBound = true
            hidService?.addListener(connectionListener)
            updateStatusBar()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            hidService?.removeListener(connectionListener)
            hidService = null
            serviceBound = false
        }
    }

    private val connectionListener = object : BluetoothHidService.ConnectionListener {
        override fun onConnectionStateChanged(connected: Boolean, deviceName: String) {
            runOnUiThread { updateStatusBar() }
        }

        override fun onRegistered(registered: Boolean) {
            runOnUiThread { updateStatusBar() }
        }
    }

    // ── Permission handling ────────────────────────────────────────────────

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.all { it.value }) {
            startAndBindService()
        } else {
            Toast.makeText(this, "Bluetooth permissions are required", Toast.LENGTH_LONG).show()
        }
    }

    private val discoverableLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* result handled via connection callback */ }

    // ── Lifecycle ──────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        setupViewPager()

        if (hasBluetoothPermissions()) {
            startAndBindService()
        } else {
            permissionLauncher.launch(requiredPermissions())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serviceBound) {
            hidService?.removeListener(connectionListener)
            unbindService(serviceConnection)
        }
    }

    // ── Menu ───────────────────────────────────────────────────────────────

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_discoverable -> { makeDiscoverable(); true }
            R.id.action_connect      -> { showDeviceList(); true }
            R.id.action_disconnect   -> { hidService?.disconnect(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // ── UI helpers ─────────────────────────────────────────────────────────

    private fun setupViewPager() {
        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 2
            override fun createFragment(position: Int): Fragment = when (position) {
                0    -> KeyboardFragment()
                else -> TrackpadFragment()
            }
        }
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, pos ->
            tab.text = if (pos == 0) "⌨ Keyboard" else "🖱 Trackpad"
        }.attach()
    }

    private fun updateStatusBar() {
        val svc = hidService
        val text = when {
            svc == null                -> "Service not running"
            svc.isConnected()          -> "● Connected – ${svc.getConnectedDeviceName()}"
            svc.isAppRegistered()      -> "○ Ready – pair from your PC's Bluetooth settings"
            else                       -> "○ Initialising…"
        }
        binding.tvStatus.text = text
    }

    private fun makeDiscoverable() {
        @Suppress("DEPRECATION")
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        }
        discoverableLauncher.launch(intent)
    }

    private fun showDeviceList() {
        val paired = hidService?.getPairedDevices()?.toList() ?: emptyList()
        if (paired.isEmpty()) {
            Toast.makeText(this, "No paired devices found. Pair from PC first.", Toast.LENGTH_LONG).show()
            return
        }
        val names = paired.map { it.name ?: it.address }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Connect to device")
            .setItems(names) { _, idx ->
                hidService?.connectToDevice(paired[idx])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ── Service ────────────────────────────────────────────────────────────

    private fun startAndBindService() {
        val intent = Intent(this, BluetoothHidService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    // ── Permissions ────────────────────────────────────────────────────────

    private fun requiredPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }

    private fun hasBluetoothPermissions(): Boolean =
        requiredPermissions().all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
}
