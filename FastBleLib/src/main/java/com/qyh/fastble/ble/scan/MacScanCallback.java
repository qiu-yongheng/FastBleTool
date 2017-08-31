package com.qyh.fastble.ble.scan;


import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import com.qyh.fastble.ble.data.BleDevice;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * scan a known mac device, then connect
 */
public abstract class MacScanCallback extends PeriodScanCallback {

    private String mMac;
    private AtomicBoolean hasFound = new AtomicBoolean(false);

    public MacScanCallback(String mac, long timeoutMillis) {
        super(timeoutMillis);
        this.mMac = mac;
        if (TextUtils.isEmpty(mac)) {
            onDeviceNotFound();
        }
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (device == null)
            return;
        if (TextUtils.isEmpty(device.getAddress())) {
            return;
        }

        if (!hasFound.get()) {

            BleDevice bleDevice = new BleDevice(device, rssi, scanRecord,
                    System.currentTimeMillis());

            if (mMac.equalsIgnoreCase(device.getAddress())) {
                hasFound.set(true);
                bleBluetooth.stopScan(MacScanCallback.this);
                onDeviceFound(bleDevice);
            }
        }
    }

    @Override
    public void onScanTimeout() {
        onDeviceNotFound();
    }

    @Override
    public void onScanCancel() {

    }

    public abstract void onDeviceFound(BleDevice bleDevice);

    public abstract void onDeviceNotFound();
}
