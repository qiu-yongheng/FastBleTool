
package com.qyh.fastble.ble.conn;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;

import com.qyh.fastble.ble.data.BleDevice;
import com.qyh.fastble.ble.exception.BleException;

/**
 * 连接回调
 */
public abstract class BleGattCallback extends BluetoothGattCallback {

    /**
     * 返回连接的设备
     * @param bleDevice
     */
    public void onFoundDevice(BleDevice bleDevice) {}

    /**
     * 连接中 (onConnectionStateChange 连接中状态)
     * @param gatt
     * @param status
     */
    public void onConnecting(BluetoothGatt gatt, int status) {}

    /**
     * 连接异常 (在连接前回调)
     * @param exception
     */
    public abstract void onConnectError(BleException exception);

    /**
     * 连接成功 (onConnectionStateChange 连接成功状态)
     * @param gatt
     * @param status
     */
    public abstract void onConnectSuccess(BluetoothGatt gatt, int status);

    /**
     * 服务被发现
     * @param gatt
     * @param status
     */
    @Override
    public abstract void onServicesDiscovered(BluetoothGatt gatt, int status);

    /**
     * 断开连接 (onConnectionStateChange 连接断开状态)
     * @param gatt
     * @param status
     * @param exception
     */
    public abstract void onDisConnected(BluetoothGatt gatt, int status, BleException exception);

}