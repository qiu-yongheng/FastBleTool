package com.qyh.fastble.ble.scan;

import android.bluetooth.BluetoothDevice;

import com.qyh.fastble.ble.data.BleDevice;
import com.qyh.fastble.ble.utils.BleLog;
import com.qyh.fastble.ble.utils.HexUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author 邱永恒
 * @time 2017/8/13  09:20
 * @desc
 *
 * scan for a period of time
 */
public abstract class ListScanCallback extends PeriodScanCallback {

    private List<BleDevice> resultList = new ArrayList<>();
    private AtomicBoolean hasFound = new AtomicBoolean(false);

    public ListScanCallback(long timeoutMillis) {
        super(timeoutMillis);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (device == null) {
            return;
        }
        BleLog.i(HexUtil.bytesToHexString(scanRecord));

        BleDevice bleDevice = new BleDevice(device, rssi, scanRecord,
                System.currentTimeMillis());

        synchronized (this) {
            hasFound.set(false);
            for (BleDevice result : resultList) {
                if (result.getDevice().equals(device)) {
                    hasFound.set(true);
                }
            }
            if (!hasFound.get()) {
                resultList.add(bleDevice);
                onScanning(bleDevice);
            }
        }
    }

    @Override
    public void onScanTimeout() {
        BleDevice[] results = new BleDevice[resultList.size()];
        for (int i = 0; i < results.length; i++) {
            results[i] = resultList.get(i);
        }
        onScanComplete(results);
    }

    @Override
    public void onScanCancel() {
        BleDevice[] resultArr = resultList.toArray(new BleDevice[resultList.size()]);
        onScanComplete(resultArr);
    }

    public abstract void onScanning(BleDevice result);

    public abstract void onScanComplete(BleDevice[] results);

}
