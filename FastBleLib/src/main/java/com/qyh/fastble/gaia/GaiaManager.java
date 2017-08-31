package com.qyh.fastble.gaia;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.qyh.fastble.ble.exception.BleException;
import com.qyh.fastble.ble.exception.BlueToothNotEnableException;
import com.qyh.fastble.ble.exception.hanlder.DefaultBleExceptionHandler;
import com.qyh.fastble.gaia.bluetooth.GaiaBluetooth;
import com.qyh.fastble.gaia.conn.GaiaCallback;
import com.qyh.fastble.gaia.scan.PeriodScanListener;

import java.util.UUID;

/**
 * @author 邱永恒
 * @time 2017/8/13  22:12
 * @desc ${TODD}
 */


public class GaiaManager {
    private DefaultBleExceptionHandler bleExceptionHandler;
    private Context context;
    private GaiaBluetooth gaiaBluetooth;

    public GaiaManager(Context context) {
        this.context = context.getApplicationContext();

        if (gaiaBluetooth == null) {
            gaiaBluetooth = new GaiaBluetooth(context);
        }

        // 异常处理
        bleExceptionHandler = new DefaultBleExceptionHandler(context);
    }

    /**
     * 处理异常
     *
     * @param exception
     */
    public void handleException(BleException exception) {
        bleExceptionHandler.handleException(exception);
    }

    /**
     * 扫描设备
     *
     * @param scan
     * @param listener
     * @return
     */
    public boolean scanDevice(boolean scan, PeriodScanListener listener) {
        if (!isBlueEnable()) {
            handleException(new BlueToothNotEnableException());
            return false;
        }

        return gaiaBluetooth.scanDevices(scan, listener);
    }

    /**
     * 停止扫描
     * @param listener
     */
    public void stopScan(PeriodScanListener listener) {
        gaiaBluetooth.stopScan(listener);
    }

    /**
     * 连接设备
     * @param address
     * @param callback
     * @return
     */
    public boolean connect(String address, GaiaCallback callback) {
        return gaiaBluetooth.connect(address, callback);
    }

    /**
     * 连接设备
     * @param device
     * @param callback
     * @return
     */
    public boolean connect(BluetoothDevice device, GaiaCallback callback) {
        return gaiaBluetooth.connect(device, callback);
    }

    /**
     * 连接设备
     * @param device
     * @param transport
     * @param callback
     * @return
     */
    public boolean connect(BluetoothDevice device, UUID transport, GaiaCallback callback) {
        return gaiaBluetooth.connect(device, transport, callback);
    }

    /**
     * 连接设备
     * @param data
     * @return
     */
    public boolean sendData(String data) {
        return sendData(data.getBytes());
    }

    /**
     * 向设备发送数据
     * @param data
     * @return
     */
    public boolean sendData(byte[] data) {
        return gaiaBluetooth.sendData(data);
    }


    public boolean isBlueEnable() {
        return gaiaBluetooth != null && gaiaBluetooth.isBlueEnable();
    }
}
