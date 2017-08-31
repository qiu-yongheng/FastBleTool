package com.example.liteble.scan;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author 邱永恒
 * @date 2015-01-22
 * 根据MAC地址进行扫描的回调, 返回指定mac的device (有超时时间)
 * 回调:
 * 1. 扫描超时
 * 2. onDeviceFound()
 */
public abstract class PeriodMacScanCallback extends PeriodScanCallback {
    private String mac;
    /**
     * 原子特性的boolean变量
     */
    private AtomicBoolean hasFound = new AtomicBoolean(false);

    /**
     * 创建对象
     * @param mac 设备Mac地址
     * @param timeoutMillis 超时时间
     */
    public PeriodMacScanCallback(String mac, long timeoutMillis) {
        super(timeoutMillis);
        this.mac = mac;
        if (TextUtils.isEmpty(mac)) {
            throw new IllegalArgumentException("start scan, mac can not be null!");
        }
    }

    /**
     * 在callback中实现, 返回device给子类
     * @param device
     * @param rssi
     * @param scanRecord
     */
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (!hasFound.get()) {
            // equalsIgnoreCase: 忽略大小写
            if (mac.equalsIgnoreCase(device.getAddress())) {
                // 当找到Mac对应的device后, 设置为true, 下次不再进入此方法
                hasFound.set(true);
                liteBluetooth.stopScan(PeriodMacScanCallback.this);
                onDeviceFound(device, rssi, scanRecord);
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
