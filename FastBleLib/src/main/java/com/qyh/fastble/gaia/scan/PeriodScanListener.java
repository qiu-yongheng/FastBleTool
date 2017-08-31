package com.qyh.fastble.gaia.scan;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;

import com.qyh.fastble.gaia.bluetooth.GaiaBluetooth;

/**
 * @author 邱永恒
 * @time 2017/8/13  23:12
 * @desc 扫描设备监听, 有超时时间
 */

public abstract class PeriodScanListener {
    private Handler handler = new Handler(Looper.getMainLooper());
    private long timeoutMillis = 10000;
    private GaiaBluetooth gaiaBluetooth;

    public PeriodScanListener (int timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public abstract void onScanTimeout();

    public abstract void onScanCancel();

    public abstract void onDeviceFound(BluetoothDevice device);

    /**
     * handler定时停止扫描
     */
    public void notifyScanStarted() {
        if (timeoutMillis > 0) {
            removeHandlerMsg();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    gaiaBluetooth.stopScan(PeriodScanListener.this);
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
        gaiaBluetooth.stopScan(PeriodScanListener.this);
        onScanCancel();
    }

    public void removeHandlerMsg() {
        handler.removeCallbacksAndMessages(null);
    }

    public PeriodScanListener setGaiaBluetooth (GaiaBluetooth gaiaBluetooth) {
        this.gaiaBluetooth = gaiaBluetooth;
        return this;
    }
}
