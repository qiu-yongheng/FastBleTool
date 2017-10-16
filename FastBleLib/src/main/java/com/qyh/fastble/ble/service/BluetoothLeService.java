package com.qyh.fastble.ble.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.qyh.fastble.ble.BleManager;
import com.qyh.fastble.ble.callback.BleServiceCallBack;
import com.qyh.fastble.ble.data.BleDevice;
import com.qyh.fastble.ble.scan.ListScanCallback;
import com.qyh.fastble.ble.utils.BleLog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 邱永恒
 * @time 2017/10/16  11:14
 * @desc ${TODD}
 */

public class BluetoothLeService extends Service{
    /** 蓝牙管理类 */
    private BleManager bleManager;
    /** 回调接口集合 */
    private List<BleServiceCallBack> list;
    /**
     * binder
     */
    private final BluetoothBinder mBinder = new BluetoothBinder();
    public class BluetoothBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * 初始化BleManager
     * 打开蓝牙
     */
    @Override
    public void onCreate() {
        BleLog.i("service onCreate");
        bleManager = new BleManager(this);
        bleManager.enableBluetooth();
        list = new ArrayList<>();
    }

    public void addCallBack(BleServiceCallBack callback) {
        if (callback == null) {
            return;
        }
        if (!list.contains(callback)) {
            list.add(callback);
        }
    }

    public void removeCallBack(BleServiceCallBack callBack) {
        if (callBack == null) {
            return;
        }
        if (list.contains(callBack)) {
            list.remove(callBack);
        }
    }

    /**
     * 扫描设备
     */
    public void scanDevice() {

        for (BleServiceCallBack callBack : list) {
            callBack.onStartScan();
        }

        boolean b = bleManager.scanDevice(new ListScanCallback(5000) {

            @Override
            public void onScanning(final BleDevice result) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onScanning(result);
                        }
                    }
                });
            }

            @Override
            public void onScanComplete(final BleDevice[] results) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onScanComplete();
                        }
                    }
                });
            }
        });
        if (!b) {
            if (mCallback != null) {
                mCallback.onScanComplete();
            }
        }
    }

    /**
     * 取消绑定:
     * 1. 断开设备连接
     * 2. 释放资源
     * @param intent
     * @return
     */
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        BleLog.i("service onDestroy closeBluetoothGatt");
        super.onDestroy();
        bleManager.closeBluetoothGatt();
        bleManager = null;
    }
}
