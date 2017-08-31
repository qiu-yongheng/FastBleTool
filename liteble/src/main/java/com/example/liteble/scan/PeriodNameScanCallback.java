package com.example.liteble.scan;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author 邱永恒
 * @time 2017/7/31  13:17
 * @desc 根据设备名进行扫描的回调, 返回指定设备名的device (有超时时间, 可以模糊搜索)
 *
 * 回调:
 * 1. 扫描超时
 * 2. onDeviceFound()
 */

public abstract class PeriodNameScanCallback extends PeriodScanCallback {
    private String name = null;
    private String[] names = null;
    private boolean isFuzzy = false;
    private AtomicBoolean hasFound = new AtomicBoolean(false);

    /**
     * 搜索制定设备名的device
     * @param name 设备名
     * @param timeoutMillis 连接超时时间
     * @param isFuzzy 是否模糊搜索
     */
    public PeriodNameScanCallback(String name, long timeoutMillis, boolean isFuzzy) {
        super(timeoutMillis);
        this.name = name;
        this.isFuzzy = isFuzzy;

        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("start scan, name can not be null!");
        }
    }

    /**
     * 在一个设备数组中, 搜索设备
     * @param names 设备名集合
     * @param timeoutMillis 连接超时时间
     * @param isFuzzy 是否模糊搜索
     */
    public PeriodNameScanCallback(String[] names, long timeoutMillis, boolean isFuzzy) {
        super(timeoutMillis);
        this.names = names;
        this.isFuzzy = isFuzzy;

        if (names == null || names.length < 1) {
            throw new IllegalArgumentException("start scan, name can not be null!");
        }
    }

    @Override
    public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {
        if (!hasFound.get()) {
            if (name != null) {
                if (isFuzzy ? bluetoothDevice.getName().contains(name) : name.equalsIgnoreCase(bluetoothDevice.getName())) {
                    hasFound.set(true);
                    liteBluetooth.stopScan(this);
                    onDeviceFound(bluetoothDevice, rssi, bytes);
                }

            } else if (names != null) {
                for (String name : names) {
                    if (isFuzzy ? bluetoothDevice.getName().contains(name) : name.equalsIgnoreCase(bluetoothDevice.getName())) {
                        hasFound.set(true);
                        liteBluetooth.stopScan(this);
                        onDeviceFound(bluetoothDevice, rssi, bytes);
                        return;
                    }
                }
            }
        }
    }

    /**
     * 回调给子类实现
     * @param device
     * @param rssi
     * @param scanRecord
     */
    public abstract void onDeviceFound(BluetoothDevice device, int rssi, byte[] scanRecord);
}
