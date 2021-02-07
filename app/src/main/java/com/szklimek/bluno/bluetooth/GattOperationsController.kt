package com.szklimek.bluno.bluetooth

import android.bluetooth.*
import android.bluetooth.BluetoothGatt.*
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.szklimek.bluno.core.Log
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class GattOperationsController {
    private val _connectionStateLiveData = MutableLiveData<ConnectionState>()
    val connectionStateLive
        get() = _connectionStateLiveData as LiveData<ConnectionState>

    private val characteristicObservers = mutableListOf<CharacteristicObserver>()
    private var deviceConnectionGATT: BluetoothGatt? = null
    private var currentOperation: Operation<*>? = null
    private val operationsQueue = ConcurrentLinkedQueue<Operation<*>>()

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.d(
                "onConnectionStateChange(); Device address: ${gatt.device.address}. " +
                        "${ConnectionState.fromCode(newState)}; ${
                            OperationStatus.fromCode(status).run { "$this:${this.message}" }
                        }"
            )
            if (status == GATT_SUCCESS && newState == STATE_CONNECTED) {
                Log.d("Connected with device. Discovering services")
                deviceConnectionGATT = gatt
                deviceConnectionGATT!!.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            gatt.services.forEach { service ->
                val characteristicsTable = service.characteristics.joinToString(
                    separator = "\n|--",
                    prefix = "|--"
                ) { "${it.uuid} (${it.printProperties()})" }
                Log.d(
                    "\nService ${service.uuid}\nCharacteristics:\n$characteristicsTable"
                )
            }
            if (currentOperation is Operation.Connect) {
                _connectionStateLiveData.postValue(ConnectionState.STATE_CONNECTED)
                (currentOperation as Operation.Connect).callback.invoke(Result.success(Unit))
                signalOperationFinished()
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            Log.d("onCharacteristicChanged() ${characteristic.value.contentToString()}")
            val serviceUUID = characteristic.service.uuid.toString()
            val characteristicUUID = characteristic.uuid.toString()
            characteristicObservers
                .filter { it.serviceUUID == serviceUUID && it.characteristicUUID == characteristicUUID }
                .forEach { it.action.invoke(characteristic.value) }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (currentOperation is Operation.CharacteristicWrite) {
                (currentOperation as Operation.CharacteristicWrite).callback.invoke(
                    Result.success(Unit)
                )
                signalOperationFinished()
            }
        }
    }

    fun enqueue(operation: Operation<*>) {
        Log.d("Operation: $operation")
        enqueueOperation(operation)
    }

    fun addCharacteristicObserver(characteristicObserver: CharacteristicObserver) {
        if (deviceConnectionGATT == null) {
            Log.e("There is no active connection. Please connect before subscribing")
            return
        }
        val service = deviceConnectionGATT!!.getService(UUID.fromString(characteristicObserver.serviceUUID))
        if (service == null) {
            Log.e("There is no service with UUID: ${characteristicObserver.serviceUUID}. Ensure that services are discovered")
            return
        }
        val characteristic = service.getCharacteristic(UUID.fromString(characteristicObserver.characteristicUUID))
        if (characteristic == null) {
            Log.e("There is no characteristic with UUID: ${characteristicObserver.characteristicUUID}")
            return
        }
        deviceConnectionGATT!!.setCharacteristicNotification(characteristic, true)
        characteristicObservers.add(characteristicObserver)
    }

    fun removeCharacteristicObserver(characteristicObserver: CharacteristicObserver) {
        characteristicObservers.remove(characteristicObserver)
    }

    @Synchronized
    private fun enqueueOperation(operation: Operation<*>) {
        Log.d("Operation: $operation")
        operationsQueue.add(operation)
        if (currentOperation == null) {
            doNextOperation()
        }
    }

    @Synchronized
    private fun doNextOperation() {
        Log.d("")
        if (currentOperation != null) {
            Log.e("doNextOperation() called when $currentOperation is in progress! Aborting.")
            return
        }
        val operation = operationsQueue.poll() ?: run {
            Log.e("Operation queue empty, returning")
            return
        }
        currentOperation = operation
        if (operation is Operation.Connect) {
            with(operation) {
                device.connectGatt(context, false, gattCallback)
                return
            }
        }

        if (deviceConnectionGATT == null) {
            Log.e("There is no connection with device ${operation.device.address}. Aborting $operation")
            operation.callback.invoke(Result.failure(Exception("Device not connected")))
            signalOperationFinished()
            return
        }
        with(operation) {
            when (this) {
                is Operation.Disconnect -> {
                    Log.d("Disconnecting from ${device.address}")
                    deviceConnectionGATT?.close()
                    deviceConnectionGATT = null
                    signalOperationFinished()
                }
                is Operation.CharacteristicWrite -> {
                    Log.d("Writing characteristic ")
                    val service = deviceConnectionGATT!!.getService(UUID.fromString(serviceUUID))
                    if (service == null) {
                        Log.e("There is no service with UUID: $serviceUUID. Ensure that services are discovered")
                        return
                    }
                    val characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID))
                    if (characteristic == null) {
                        Log.e("There is no characteristic with UUID: $serviceUUID")
                        return
                    }
                    characteristic.value = (operation as Operation.CharacteristicWrite).payload
                    deviceConnectionGATT!!.writeCharacteristic(characteristic)
                }
                else -> Unit
            }
        }
    }

    private fun signalOperationFinished() {
        Log.d("")
        currentOperation = null
        if (operationsQueue.isNotEmpty()) {
            doNextOperation()
        }
    }
}

data class CharacteristicObserver(
    val serviceUUID: String,
    val characteristicUUID: String,
    val action: (ByteArray) -> Unit
)

sealed class Operation<T> {
    abstract val device: BluetoothDevice
    abstract val callback: (Result<T>) -> Unit

    data class Connect(
        override val device: BluetoothDevice,
        val context: Context,
        override val callback: (Result<Unit>) -> Unit,
    ) : Operation<Unit>()

    data class Disconnect(
        override val device: BluetoothDevice,
        val context: Context,
        override val callback: (Result<Unit>) -> Unit,
    ) : Operation<Unit>()

    data class CharacteristicWrite(
        override val device: BluetoothDevice,
        val serviceUUID: String,
        val characteristicUUID: String,
        val payload: ByteArray,
        override val callback: (Result<Unit>) -> Unit,
    ) : Operation<Unit>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CharacteristicWrite

            if (device != other.device) return false
            if (serviceUUID != other.serviceUUID) return false
            if (characteristicUUID != other.characteristicUUID) return false
            if (!payload.contentEquals(other.payload)) return false
            if (callback != other.callback) return false

            return true
        }

        override fun hashCode(): Int {
            var result = device.hashCode()
            result = 31 * result + serviceUUID.hashCode()
            result = 31 * result + characteristicUUID.hashCode()
            result = 31 * result + payload.contentHashCode()
            result = 31 * result + callback.hashCode()
            return result
        }
    }
}

enum class OperationStatus(val message: String) {
    SUCCESS("A GATT operation completed successfully"),
    READ_NOT_PERMITTED("GATT read operation is not permitted"),
    WRITE_NOT_PERMITTED("GATT write operation is not permitted"),
    INSUFFICIENT_AUTHENTICATION("Insufficient authentication for a given operation"),
    REQUEST_NOT_SUPPORTED("The given request is not supported"),
    INSUFFICIENT_ENCRYPTION("Insufficient encryption for a given operation"),
    INVALID_OFFSET("A read or write operation was requested with an invalid offset"),
    INVALID_ATTRIBUTE_LENGTH("A write operation exceeds the maximum length of the attribute"),
    CONNECTION_CONGESTED("A remote device connection is congested"),
    FAILURE("A GATT operation failed, errors other than the above"),
    UNKNOWN("Unknown GATT operation status");

    companion object {
        @JvmStatic
        fun fromCode(operationStatusCode: Int): OperationStatus =
            when (operationStatusCode) {
                GATT_SUCCESS -> SUCCESS
                GATT_READ_NOT_PERMITTED -> READ_NOT_PERMITTED
                GATT_WRITE_NOT_PERMITTED -> WRITE_NOT_PERMITTED
                GATT_INSUFFICIENT_AUTHENTICATION -> INSUFFICIENT_AUTHENTICATION
                GATT_REQUEST_NOT_SUPPORTED -> REQUEST_NOT_SUPPORTED
                GATT_INSUFFICIENT_ENCRYPTION -> INSUFFICIENT_ENCRYPTION
                GATT_INVALID_OFFSET -> INVALID_OFFSET
                GATT_INVALID_ATTRIBUTE_LENGTH -> INVALID_ATTRIBUTE_LENGTH
                GATT_CONNECTION_CONGESTED -> CONNECTION_CONGESTED
                GATT_FAILURE -> FAILURE
                else -> UNKNOWN
            }
    }
}

enum class ConnectionState {
    STATE_DISCONNECTED,
    STATE_CONNECTING,
    STATE_CONNECTED,
    STATE_DISCONNECTING,
    STATE_UNKNOWN;

    companion object {
        @JvmStatic
        fun fromCode(stateCode: Int) = when (stateCode) {
            BluetoothProfile.STATE_DISCONNECTED -> STATE_DISCONNECTED
            BluetoothProfile.STATE_CONNECTING -> STATE_CONNECTING
            BluetoothProfile.STATE_CONNECTED -> STATE_CONNECTED
            BluetoothProfile.STATE_DISCONNECTING -> STATE_DISCONNECTING
            else -> STATE_UNKNOWN
        }
    }
}

fun BluetoothGattCharacteristic.isReadable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

fun BluetoothGattCharacteristic.isWritable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

fun BluetoothGattCharacteristic.isIndicatable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)

fun BluetoothGattCharacteristic.isNotifiable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)

fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean {
    return properties and property != 0
}

fun BluetoothGattCharacteristic.printProperties(): String = mutableListOf<String>().apply {
    if (isReadable()) add("READABLE")
    if (isWritable()) add("WRITABLE")
    if (isWritableWithoutResponse()) add("WRITABLE WITHOUT RESPONSE")
    if (isIndicatable()) add("INDICATABLE")
    if (isNotifiable()) add("NOTIFIABLE")
    if (isEmpty()) add("EMPTY")
}.joinToString()
