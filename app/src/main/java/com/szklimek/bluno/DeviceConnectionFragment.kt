package com.szklimek.bluno

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.szklimek.bluno.bluetooth.EnableBluetoothContract
import com.szklimek.bluno.core.BaseFragment
import com.szklimek.bluno.core.Log
import kotlinx.android.synthetic.main.fragment_device_connection.*

class DeviceConnectionFragment : BaseFragment() {
    private val deviceConnectionController by lazy { BlunoDeviceController(requireContext()) }

    private val enableBluetoothLauncher =
        registerForActivityResult(EnableBluetoothContract()) { isEnabled ->
            Log.d("Result received: bluetooth enabled $isEnabled")
            if (isEnabled == true) {
                deviceConnectionController.connect()
            }
        }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isEnabled ->
            Log.d("Result received: location permission granted $isEnabled")
            if (isEnabled) {
                deviceConnectionController.connect()
            } else {
                // TODO Show explanation why location is needed
            }
        }

    private val messageObserver: (String) -> Unit = { message ->
        Log.d("Message received: $message")
    }

    private val connectionStateObserver = Observer<DeviceController.State> {
        Log.d("Connection state updated: $it")
        device_connection_status.text = it.javaClass.simpleName
        if (it is DeviceController.State.Connected)
        when (it) {
            DeviceController.State.NotInitialized -> {
                // TODO Explain user to tap button to start
            }

            DeviceController.State.MissingLocationPermission -> {
                Log.d("Location permission missing. Requesting")
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            DeviceController.State.BluetoothDisabled -> {
                Log.d("Bluetooth not enabled. Enabling")
                enableBluetoothLauncher.launch(Unit)
            }
            DeviceController.State.FindingDevice -> {
                // TODO Show progress bar when finding in progress
            }
            DeviceController.State.DeviceNotFound -> {
                // TODO Handle finding device error gracefully
            }
            DeviceController.State.Connecting -> Unit
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_device_connection, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        deviceConnectionController.stateLiveData.observe(
            viewLifecycleOwner,
            connectionStateObserver
        )
        initViews()
    }

    override fun onPause() {
        super.onPause()
        deviceConnectionController.disconnect()
    }

    private fun initViews() {
        button_first.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        button_start.setOnClickListener {
            deviceConnectionController.connect()
        }
        button_send.setOnClickListener {
            val message = message_edit_text.text.toString()
            deviceConnectionController.write(message) {
                Log.d("Writing message result: $it")
            }
            message_edit_text.text = null
        }
    }
}
