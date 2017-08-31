package com.qyh.fastble.ble.scan;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.os.Looper;

import com.qyh.fastble.ble.bluetooth.BleBluetooth;

/**
 * 有扫描超时的扫描回调
 */
public abstract class PeriodScanCallback implements BluetoothAdapter.LeScanCallback {

    private Handler handler = new Handler(Looper.getMainLooper());
    private long timeoutMillis = 10000;
    BleBluetooth bleBluetooth;

    PeriodScanCallback(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public abstract void onScanTimeout();

    public abstract void onScanCancel();

    /**
     * handler定时停止扫描
     */
    public void notifyScanStarted() {
        if (timeoutMillis > 0) {
            removeHandlerMsg();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bleBluetooth.stopScan(PeriodScanCallback.this);
                    onScanTimeout();
                }
            }, timeoutMillis);
        }
    }

    /**
     * 手动停止扫描
     */
    public void notifyScanCancel() {
        removeHandlerMsg();
        bleBluetooth.stopScan(PeriodScanCallback.this);
        onScanCancel();
    }

    /**
     * 移除定时任务(超时)
     */
    public void removeHandlerMsg() {
        handler.removeCallbacksAndMessages(null);
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public PeriodScanCallback setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        return this;
    }

    public BleBluetooth getBleBluetooth() {
        return bleBluetooth;
    }

    public PeriodScanCallback setBleBluetooth(BleBluetooth bluetooth) {
        this.bleBluetooth = bluetooth;
        return this;
    }
}
