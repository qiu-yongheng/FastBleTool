package com.qyh.fastble.ble.scan;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import com.qyh.fastble.ble.data.BleDevice;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * scan a known name device, then connect
 */
public abstract class NameScanCallback extends PeriodScanCallback {

    private String mName = null;
    private String[] mNames = null;
    private boolean mFuzzy = false;
    private AtomicBoolean hasFound = new AtomicBoolean(false);

    public NameScanCallback(String name, long timeoutMillis, boolean fuzzy) {
        super(timeoutMillis);
        this.mName = name;
        this.mFuzzy = fuzzy;
        if (TextUtils.isEmpty(name)) {
            onDeviceNotFound();
        }
    }

    public NameScanCallback(String[] names, long timeoutMillis, boolean fuzzy) {
        super(timeoutMillis);
        this.mNames = names;
        this.mFuzzy = fuzzy;
        if (names == null || names.length < 1) {
            onDeviceNotFound();
        }
    }

    /**
     * 根据设备名搜索指定设备
     *
     * 模糊搜索区分大小写
     * @param device
     * @param rssi
     * @param scanRecord
     */
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (device == null)
            return;

        if (TextUtils.isEmpty(device.getName())) {
            return;
        }

        if (!hasFound.get()) {

            BleDevice bleDevice = new BleDevice(device, rssi, scanRecord,
                    System.currentTimeMillis());

            if (mName != null) {
                if (mFuzzy ? device.getName().contains(mName) : mName.equalsIgnoreCase(device.getName())) {
                    hasFound.set(true);
                    bleBluetooth.stopScan(NameScanCallback.this);
                    onDeviceFound(bleDevice);
                }

            } else if (mNames != null) {
                for (String name : mNames) {
                    if (mFuzzy ? device.getName().contains(name) : name.equalsIgnoreCase(device.getName())) {
                        hasFound.set(true);
                        bleBluetooth.stopScan(NameScanCallback.this);
                        onDeviceFound(bleDevice);
                        return;
                    }
                }
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

    public abstract void onDeviceFound(BleDevice sanResult);

    public abstract void onDeviceNotFound();
}
