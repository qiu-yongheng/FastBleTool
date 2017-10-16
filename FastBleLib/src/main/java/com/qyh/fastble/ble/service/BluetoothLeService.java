package com.qyh.fastble.ble.service;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import com.qyh.fastble.ble.BleManager;
import com.qyh.fastble.ble.callback.BleServiceCallBack;
import com.qyh.fastble.ble.conn.BleCharacterCallback;
import com.qyh.fastble.ble.conn.BleGattCallback;
import com.qyh.fastble.ble.conn.BleRssiCallback;
import com.qyh.fastble.ble.constant.UUIDConstant;
import com.qyh.fastble.ble.data.BleDevice;
import com.qyh.fastble.ble.exception.BleException;
import com.qyh.fastble.ble.scan.ListScanCallback;
import com.qyh.fastble.ble.utils.BleLog;
import com.qyh.fastble.ble.utils.HexUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author 邱永恒
 * @time 2017/10/16  11:14
 * @desc ${TODD}
 */

public class BluetoothLeService extends Service {
    /**
     * 蓝牙管理类
     */
    private BleManager bleManager;
    /**
     * 回调接口集合
     */
    private List<BleServiceCallBack> list;
    /**
     * 主线程handler
     */
    private Handler threadHandler = new Handler(Looper.getMainLooper());
    /**
     * 已连接设备集合
     */
    private List<BluetoothDevice> connectList;
    /**
     * 重连设备集合
     */
    private List<BluetoothDevice> reConnectList;
    /**
     * binder
     */
    private final BluetoothBinder mBinder = new BluetoothBinder();
    private BluetoothGatt gatt;

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
        connectList = new ArrayList<>();
    }

    /**
     * 添加连接回调
     *
     * @param callback
     */
    public void addCallBack(BleServiceCallBack callback) {
        if (callback == null) {
            return;
        }
        if (!list.contains(callback)) {
            list.add(callback);
        }
    }

    /**
     * 删除连接回调
     *
     * @param callBack
     */
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
                        for (BleServiceCallBack callBack : list) {
                            callBack.onLeScan(result);
                        }
                    }
                });
            }

            @Override
            public void onScanComplete(final BleDevice[] results) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        for (BleServiceCallBack callBack : list) {
                            callBack.onScanComplete(results);
                        }
                    }

                });
            }
        });
        if (!b) {
            for (BleServiceCallBack callBack : list) {
                callBack.onScanError();
            }
        }
    }

    /**
     * 取消扫描
     */
    public void cancelScan() {
        bleManager.cancelScan();
    }

    /**
     * 根据MAC地址连接设备
     * @param macAddress
     */
    public void connectDevice(String macAddress) {
        BluetoothDevice remoteDevice = bleManager.getRemoteDevice(macAddress);
        connectDevice(new BleDevice(remoteDevice, 0, null, System.currentTimeMillis()));
    }

    /**
     * 连接设备
     *
     * @param scanResult
     */
    public void connectDevice(final BleDevice scanResult) {

        for (BleServiceCallBack callBack : list) {
            callBack.onConnecting();
        }

        bleManager.connectDevice(scanResult, false, new BleGattCallback() {

            @Override
            public void onFoundDevice(BleDevice scanResult) {

            }

            @Override
            public void onConnecting(BluetoothGatt gatt, int status) {

            }

            @Override
            public void onConnectError(final BleException exception) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        for (BleServiceCallBack callBack : list) {
                            callBack.onConnectFail(exception);
                        }
                    }
                });
            }

            @Override
            public void onConnectSuccess(BluetoothGatt gatt, int status) {
                BleLog.i("连接状态: " + status);
                BluetoothDevice device = gatt.getDevice();
                if (!connectList.contains(device)) {
                    connectList.add(device);
                }

                if (reConnectList.contains(device)) {
                    reConnectList.remove(device);
                }

                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        for (BleServiceCallBack callBack : list) {
                            callBack.onConnectSuccess();
                        }
                    }
                });
            }

            @Override
            public void onDisConnected(final BluetoothGatt gatt, int status, BleException exception) {
                BleLog.i("连接断开: " + gatt.getDevice().getName() + " >>> " + gatt.getDevice().getAddress());
                final BluetoothDevice device = gatt.getDevice();
                if (connectList.contains(device)) {
                    connectList.remove(device);
                }

                if (!reConnectList.contains(device)) {
                    reConnectList.add(device);
                }

                // 睡眠100毫秒后再执行后续操作, 如重连
                SystemClock.sleep(100);
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        for (BleServiceCallBack callBack : list) {
                            callBack.onDisConnected(device);
                        }
                    }
                });
            }

            @Override
            public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
                BleLog.i("服务被发现: " + gatt.getServices().size() + "个");

                if (gatt != null) {
                    for (BluetoothGattService service : gatt.getServices()) {
                        BleLog.i("service: " + service.getUuid());
                        for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                            BleLog.i("  characteristic: " + characteristic.getUuid() + " value: " + Arrays.toString(characteristic.getValue()));
                            for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                                BleLog.i("        descriptor: " + descriptor.getUuid() + " value: " + Arrays.toString(descriptor.getValue()));
                            }
                        }
                    }
                }

                BluetoothLeService.this.gatt = gatt;
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        for (BleServiceCallBack callBack : list) {
                            callBack.onServicesDiscovered(gatt, status);
                        }
                    }
                });
            }
        });
    }

    /** ----------------------------------------读写数据------------------------------------------------ **/
    public boolean read(String uuid_service, String uuid_read, BleCharacterCallback callback) {
        return bleManager.readDevice(uuid_service, uuid_read, callback);
    }

    public boolean write(String uuid_service, String uuid_write, String hex, BleCharacterCallback callback) {
        return bleManager.writeDevice(uuid_service, uuid_write, HexUtil.hexStringToBytes(hex), callback);
    }

    public boolean notify(String uuid_service, String uuid_notify) {
        return bleManager.notify(uuid_service, uuid_notify, new BleCharacterCallback() {
            @Override
            public void onSuccess(BluetoothGattCharacteristic characteristic) {
                if (characteristic.getUuid().toString().equals(UUIDConstant.HRM_CHAR)) {
                    for (BleServiceCallBack callBack : list) {
                        callBack.onHRMNotify(characteristic);
                    }
                } else {
                    for (BleServiceCallBack callBack : list) {
                        callBack.onChanged(characteristic);
                    }
                }
            }

            @Override
            public void onFailure(BleException exception) {

            }

            @Override
            public void onInitiatedResult(boolean result) {

            }
        });
    }

    public boolean indicate(String uuid_service, String uuid_indicate, BleCharacterCallback callback) {
        return bleManager.indicate(uuid_service, uuid_indicate, callback);
    }

    public boolean stopNotify(String uuid_service, String uuid_notify) {
        return bleManager.stopNotify(uuid_service, uuid_notify);
    }

    public boolean stopIndicate(String uuid_service, String uuid_indicate) {
        return bleManager.stopIndicate(uuid_service, uuid_indicate);
    }

    public boolean readRssi(BleRssiCallback callback) {
        return bleManager.readRssi(callback);
    }

    public void closeConnect() {
        bleManager.closeBluetoothGatt();
    }
    /** ----------------------------------------读写数据------------------------------------------------ **/


    public List<BluetoothDevice> getConnectList() {
        return connectList;
    }

    public List<BluetoothDevice> getReConnectList() {
        return reConnectList;
    }

    /**
     * 在主线程执行
     *
     * @param runnable
     */
    private void runOnMainThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            threadHandler.post(runnable);
        }
    }

    /**
     * 取消绑定:
     * 1. 断开设备连接
     * 2. 释放资源
     *
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
