package com.qyh.fastble.ble.conn;

import android.bluetooth.BluetoothGattCharacteristic;


public abstract class BleCharacterCallback extends BleCallback {
    public abstract void onSuccess(BluetoothGattCharacteristic characteristic);
}