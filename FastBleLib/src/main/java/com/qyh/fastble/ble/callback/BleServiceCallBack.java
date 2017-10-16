package com.qyh.fastble.ble.callback;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.qyh.fastble.ble.data.BleDevice;
import com.qyh.fastble.ble.exception.BleException;

public abstract class BleServiceCallBack {

    /**
     * 开始扫描
     */
    public void onStartScan(){};

    /**
     * 停止扫描
     */
    public void onStop(){};

    /**
     * 扫描失败
     */
    public void onScanError() {};

    /**
     * 扫描到设备
     * @param device
     */
    public abstract void onLeScan(BleDevice device);

    /**
     * 扫描完成
     * @param results
     */
    public abstract void onScanComplete(BleDevice[] results);

    /**
     * 正在连接
     */
    public void onConnecting() {};

    /**
     * 连接异常
     * @param exception
     */
    public void onConnectFail(BleException exception) {};

    /**
     * 连接成功
     */
    public abstract void onConnectSuccess();

    /**
     * 连接断开
     * @param device
     */
    public abstract void onDisConnected(BluetoothDevice device);

    /**
     *  When the write succeeds
     * @param gatt Bluetooth central device
     */
    public void onWrite(BluetoothGatt gatt){};

    /**
     *  Has been connected
     */
//    public void onConnected(BluetoothDevice device){};

//    public void onDisConnected(BluetoothDevice device){};

    /**
     *  When the MCU returns the data read
     * @param device ble device object
     */
    public void onRead(BluetoothDevice device){};

    /**
     *  MCU data sent to the app when the data callback call is setNotify
     * @param characteristic  characteristic
     */
    public abstract void onChanged(BluetoothGattCharacteristic characteristic);

    /**
     * 心率数据刷新
     * @param characteristic
     */
    public abstract void onHRMNotify(BluetoothGattCharacteristic characteristic);

    /**
     *  Set the notification feature to be successful and can send data
     * @param device ble device object
     */
    public void onReady(BluetoothDevice device){};

    /**
     *  Set the notification here when the service finds a callback       setNotify
     * @param gatt gatt
     */
    public abstract void onServicesDiscovered(BluetoothGatt gatt, int status);

    /**
     *  The callback is disconnected or connected when the connection is changed
     * @param device ble device object
     */
    public abstract void onConnectionChanged(BleDevice device);

    /**
     *  The notification describes when the write succeeded
     * @param gatt gatt
     */
    public void onDescriptorWriter(BluetoothGatt gatt){};

    /**
     *  Reads when the notification description is successful
     * @param gatt gatt
     */
    public void onDescriptorRead(BluetoothGatt gatt){};

    /**
     *  When the callback when the error, such as app can only connect four devices
     *  at the same time forcing the user to connect more than four devices will call back the method
     * @param errorCode errorCode
     */
    public void onError(int errorCode){};

    /**
     *  device connect timeout
     */
    public void onConnectTimeOut(){}

    /**
     *  Unable to initialize Bluetooth
     */
    public void onInitFailed(){}
}
