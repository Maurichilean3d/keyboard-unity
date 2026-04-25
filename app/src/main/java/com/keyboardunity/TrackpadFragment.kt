package com.keyboardunity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.keyboardunity.databinding.FragmentTrackpadBinding

class TrackpadFragment : Fragment() {

    private var _binding: FragmentTrackpadBinding? = null
    private val binding get() = _binding!!

    // Button press state: bit 0 = left, bit 1 = right
    private var buttonState = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrackpadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.trackpadView.onMouseMove = { dx, dy ->
            getService()?.sendMouseReport(buttonState, dx, dy, 0)
        }

        binding.trackpadView.onScroll = { delta ->
            getService()?.sendMouseReport(0, 0, 0, delta)
        }

        // Left click
        binding.btnLeftClick.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    buttonState = buttonState or 0x01
                    getService()?.sendMouseReport(buttonState, 0, 0, 0)
                }
                android.view.MotionEvent.ACTION_UP,
                android.view.MotionEvent.ACTION_CANCEL -> {
                    buttonState = buttonState and 0x01.inv()
                    getService()?.sendMouseReport(buttonState, 0, 0, 0)
                }
            }
            true
        }

        // Right click
        binding.btnRightClick.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    buttonState = buttonState or 0x02
                    getService()?.sendMouseReport(buttonState, 0, 0, 0)
                }
                android.view.MotionEvent.ACTION_UP,
                android.view.MotionEvent.ACTION_CANCEL -> {
                    buttonState = buttonState and 0x02.inv()
                    getService()?.sendMouseReport(buttonState, 0, 0, 0)
                }
            }
            true
        }

        // Middle click (optional)
        binding.btnMiddleClick.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    buttonState = buttonState or 0x04
                    getService()?.sendMouseReport(buttonState, 0, 0, 0)
                }
                android.view.MotionEvent.ACTION_UP,
                android.view.MotionEvent.ACTION_CANCEL -> {
                    buttonState = buttonState and 0x04.inv()
                    getService()?.sendMouseReport(buttonState, 0, 0, 0)
                }
            }
            true
        }
    }

    private fun getService(): BluetoothHidService? =
        (activity as? MainActivity)?.hidService

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
