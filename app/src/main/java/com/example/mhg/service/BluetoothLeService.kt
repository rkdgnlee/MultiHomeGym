package com.example.mhg.service

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.util.UUID


class BluetoothLeService : Service() {
    private var mBluetoothManager: BluetoothManager? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothDeviceAddress: String? = null
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mConnectionState = STATE_DISCONNECTED
    private val mGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            //super.onConnectionStateChange(gatt, status, newState);
            val intentAction: String
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED
                mConnectionState = STATE_CONNECTED
                broadcastUpdate(intentAction)
                Log.i(TAG, "Connected to GATT server")
                Log.i(
                    TAG,
                    "Attempting to start service discorvery:" + mBluetoothGatt!!.discoverServices()
                )
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED
                mConnectionState = STATE_DISCONNECTED
                Log.i(TAG, "Disconnected from GATT server")
                broadcastUpdate(intentAction)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "mBluetoothGatt = $mBluetoothGatt")
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
            } else {
                Log.w(
                    TAG,
                    "onServicesDiscovered received: $status"
                )
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onCharacteristicRead")
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            Log.e(TAG, "onCharacteristicChanged")
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
        }
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic) {
        val intent = Intent(action)
        if (TX_CHAR_UUID == characteristic.uuid) {
            intent.putExtra(EXTRA_DATA, characteristic.value)
        } else {
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    inner class LocalBinder : Binder() {
        fun getService() : BluetoothLeService {
            return this@BluetoothLeService
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onUnbind(intent: Intent): Boolean {
        close()
        return super.onUnbind(intent)
    }

    private val mBinder: IBinder = LocalBinder()
    fun initialize(): Boolean {
        if (mBluetoothManager == null) {
            mBluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager")
                return false
            }
        }
        mBluetoothAdapter = mBluetoothManager!!.adapter
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter")
            return false
        }
        return true
    }

    @SuppressLint("MissingPermission")
    fun connect(address: String?): Boolean {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address")
            return false
        }
        Log.d(TAG, "connect to $address")
        if (mBluetoothDeviceAddress != null && address == mBluetoothDeviceAddress && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection")
            return if (mBluetoothGatt!!.connect()) {
                mConnectionState = STATE_CONNECTING
                true
            } else {
                false
            }
        }
        val device = mBluetoothAdapter!!.getRemoteDevice(address)
        if (device == null) {
            Log.w(TAG, "Device not found. Unable to connect")
            return false
        }
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback)
        Log.d(TAG, "Trying to create a new connection")
        mBluetoothDeviceAddress = address
        mConnectionState = STATE_CONNECTING
        return true
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.disconnect()
    }

    @SuppressLint("MissingPermission")
    fun close() {
        if (mBluetoothGatt == null) return
        Log.w(TAG, "mBluetoothGatt closed")
        mBluetoothDeviceAddress = null
        mBluetoothGatt!!.close()
        mBluetoothGatt = null
    }

    @SuppressLint("MissingPermission")
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic?) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.readCharacteristic(characteristic)
    }

    @SuppressLint("MissingPermission")
    fun enableTxNotification() {
        Log.d(TAG, "enableTxNotification() - 1")
        if (mBluetoothGatt == null) {
            showMessage("mBluetoothGatt null$mBluetoothGatt")
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART)
            return
        }
        Log.d(TAG, "enableTxNotification() - 2")
        val RxService = mBluetoothGatt!!.getService(RX_SERVICE_UUID)
        if (RxService == null) {
            showMessage("Rx service not found!")
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART)
            return
        }
        Log.d(TAG, "enableTxNotification() - 3")
        val TxChar = RxService.getCharacteristic(TX_CHAR_UUID)
        if (TxChar == null) {
            showMessage("Tx characteristic not found!")
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART)
            return
        }
        mBluetoothGatt!!.setCharacteristicNotification(TxChar, true)
        Log.d(TAG, "enableTxNotification() - 4")
        val descriptor = TxChar.getDescriptor(CCCD)
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        mBluetoothGatt!!.writeDescriptor(descriptor)
        Log.d(TAG, "enableTxNotification() - 5")
    }

    @SuppressLint("MissingPermission")
    fun writeRxCharacteristic(value: ByteArray?) {
        val RxService = mBluetoothGatt!!.getService(RX_SERVICE_UUID)
        showMessage("mBluetoothGatt null$mBluetoothGatt")
        if (RxService == null) {
            showMessage("Rx service not found")
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART)
            return
        }
        val RxChar = RxService.getCharacteristic(RX_CHAR_UUID)
        if (RxChar == null) {
            showMessage("Rx characteristic not found")
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART)
            return
        }
        RxChar.setValue(value)
        val status = mBluetoothGatt!!.writeCharacteristic(RxChar)
        Log.d(TAG, "write TXChar - status=$status")
    }

    private fun showMessage(msg: String) {
        Log.e(TAG, msg)
    }

    companion object {
        private val TAG = BluetoothLeService::class.java.getSimpleName()
        private const val STATE_DISCONNECTED = 0
        private const val STATE_CONNECTING = 1
        private const val STATE_CONNECTED = 2
        const val ACTION_GATT_CONNECTED = "com.tangoplus.blecomtest.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED = "com.tangoplus.blecomtest.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED =
            "com.tangoplus.blecomtest.ACTION_SERVICES_DISCOVERED"
        const val ACTION_DATA_AVAILABLE = "com.tangoplus.blecomtest.ACTION_DATA_AVAILABLE"
        const val EXTRA_DATA = "com.tangoplus.blecomtest.EXTRA_DATA"
        const val DEVICE_DOES_NOT_SUPPORT_UART =
            "com.tangoplus.blecomtest.DEVICE_DOES_NOT_SUPPORT_UART"
        val TX_POWER_UUID = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb")
        val TX_POWER_LEVEL_UUID = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb")
        val CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        val FIRMWARE_REVISON_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb")
        val DIS_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")
        val RX_SERVICE_UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")

        //0000fff1 = Android --> BLE UUID Write
        val RX_CHAR_UUID = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb")

        //0000fff4 = BLE --> Android Notification
        val TX_CHAR_UUID = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb")
    }
}


