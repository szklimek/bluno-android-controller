package com.szklimek.bluno

import androidx.lifecycle.LiveData

interface DeviceController {
    sealed class State {
        object NotInitialized : State()
        object MissingLocationPermission : State()
        object BluetoothDisabled : State()
        object FindingDevice : State()
        object DeviceNotFound : State()
        object Connecting : State()
        object ConnectionError : State()
        object Connected : State()
    }
    val stateLiveData: LiveData<State>
    fun connect()
    fun disconnect()
    fun addMessagesObserver(messagesObserver: (String) -> Unit)
    fun removeMessagesObserver()
    fun write(message: String, callback: (Result<Unit>) -> Unit)
}
