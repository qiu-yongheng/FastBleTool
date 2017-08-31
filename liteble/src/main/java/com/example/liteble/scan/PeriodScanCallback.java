package com.example.liteble.scan;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.os.Looper;

import com.example.liteble.LiteBluetooth;

/**
 * @author MaTianyu
 * @date 2015-01-22
 * 周期的扫描BLE设备的回调(有超时时间)
 * 回调:
 * 1. 扫描超时
 * 2. onLesScan()
 */
public abstract class PeriodScanCallback implements BluetoothAdapter.LeScanCallback {
    protected Handler handler = new Handler(Looper.getMainLooper());
    protected long timeoutMillis;
    protected LiteBluetooth liteBluetooth;

    public PeriodScanCallback(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    /**
     * 子类设置扫描超时时间
     */
    public abstract void onScanTimeout();

    /**
     * 设置扫描超时
     */
    public void notifyScanStarted() {
        if (timeoutMillis > 0) {
            // 移除定时任务
            removeHandlerMsg();

            // 设置定时任务
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    liteBluetooth.stopScan(PeriodScanCallback.this);
                    onScanTimeout();
                }
            }, timeoutMillis);
        }
    }

    /**
     *
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

    public LiteBluetooth getLiteBluetooth() {
        return liteBluetooth;
    }

    /**
     * 传入BLE管理类
     * @param liteBluetooth
     * @return
     */
    public PeriodScanCallback setLiteBluetooth(LiteBluetooth liteBluetooth) {
        this.liteBluetooth = liteBluetooth;
        return this;
    }
}
