package com.szklimek.bluno

import android.Manifest
import android.bluetooth.*
import android.bluetooth.BluetoothGatt.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.szklimek.bluno.bluetooth.*
import com.szklimek.bluno.core.Log
import kotlinx.coroutines.*
import java.util.*


const val BLUNO_SERVICE_UUID = "0000dfb0-0000-1000-8000-00805f9b34fb"
const val BLUNO_CHARACTERISTIC_UUID = "0000dfb1-0000-1000-8000-00805f9b34fb"

class BlunoDeviceController(private val context: Context) : DeviceController {
    override val stateLiveData: LiveData<DeviceController.State>
        get() = _stateLiveData

    private val coroutineScope = MainScope()
    private val _stateLiveData =
        MutableLiveData<DeviceController.State>().apply {
            value = DeviceController.State.NotInitialized
        }
    private val gattOperationsController = GattOperationsController()
    private var device: BluetoothDevice? = null
    private var messagesObserver: ((String) -> Unit)? = null

    override fun connect() {
        Log.d("connect()")
        if (!hasLocationPermission()) {
            _stateLiveData.postValue(DeviceController.State.MissingLocationPermission)
            return
        }
        if (!hasEnabledBluetooth()) {
            _stateLiveData.postValue(DeviceController.State.BluetoothDisabled)
            return
        }
        if (device != null && gattOperationsController.connectionStateLive.value == ConnectionState.STATE_CONNECTED) {
            Log.d("Device already connected")
            return
        }
        Log.d("Bluetooth ready")
        val bluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
        _stateLiveData.postValue(DeviceController.State.FindingDevice)
        coroutineScope.launch {
            Log.d("Finding device")
            val findDeviceResult = findDevice(bluetoothLeScanner).invoke(Unit)
            Log.d("Find device result: $findDeviceResult")
            if (findDeviceResult.isFailure) {
                _stateLiveData.postValue(DeviceController.State.DeviceNotFound)
                return@launch
            }

            device = findDeviceResult.getOrThrow()
            Log.d("Device found: $device")

            _stateLiveData.postValue(DeviceController.State.Connecting)
            gattOperationsController.enqueue(Operation.Connect(device!!, context) {
                if (it.isFailure) {
                    _stateLiveData.postValue(DeviceController.State.ConnectionError)
                    return@Connect
                }
                _stateLiveData.postValue(DeviceController.State.Connected)
                gattOperationsController.addCharacteristicObserver(
                    CharacteristicObserver(
                        BLUNO_SERVICE_UUID,
                        BLUNO_CHARACTERISTIC_UUID
                    ) { data ->
                        messagesObserver?.invoke(data.toString())
                    })
            })
        }
    }

    override fun disconnect() {
        gattOperationsController.enqueue(Operation.Disconnect(device!!, context) {
            Log.d("Result received: $it")
        })
    }

    override fun addMessagesObserver(messagesObserver: (String) -> Unit) {
        this.messagesObserver = messagesObserver

    }

    override fun removeMessagesObserver() {
        messagesObserver = null
    }

    override fun write(message: String, callback: (Result<Unit>) -> Unit) {
        Log.d("Writing message: $message")
        gattOperationsController.enqueue(
            Operation.CharacteristicWrite(
                device = device!!,
                serviceUUID = BLUNO_SERVICE_UUID,
                characteristicUUID = BLUNO_CHARACTERISTIC_UUID,
                payload = message.toByteArray(),
                callback = callback
            )
        )
    }

    private suspend fun findDevice(bluetoothLeScanner: BluetoothLeScanner): (Unit) -> Result<BluetoothDevice> =
        withContext(Dispatchers.IO) {
            val scanResults = mutableListOf<ScanResult>()
            val scanCallback = object :
                ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    Log.d("onScanResult, Device { name: ${result.device?.name}, address: ${result.device?.address}, type: ${result.device?.type}}; ScanResult: $result")
                    scanResults.add(result)
                }

                override fun onScanFailed(errorCode: Int) {
                    Log.e("Scan failed. Reason: ${getScanFailureReasonMessage(errorCode)}")
                }
            }
            bluetoothLeScanner.startScan(scanCallback)
            Log.d("Scanning started")
            repeat(100) {
                Log.d("ScanResults: ${scanResults.size}")
                val expectedDevice = scanResults.find {
                    it.scanRecord?.serviceUuids?.contains(
                        ParcelUuid(UUID.fromString(BLUNO_SERVICE_UUID))
                    ) == true
                }
                if (expectedDevice != null) {
                    Log.d("Expected device found")
                    bluetoothLeScanner.stopScan(scanCallback)
                    return@withContext { Result.success(expectedDevice.device) }
                }
                delay(100)
            }
            bluetoothLeScanner.stopScan(scanCallback)
            return@withContext { Result.failure<BluetoothDevice>(Exception("Unable to find device with service UUID: $BLUNO_SERVICE_UUID")) }
        }

    private fun hasLocationPermission() =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun hasEnabledBluetooth() =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.isEnabled
}
