package com.qyh.fastble.ble.service;


import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.qyh.fastble.ble.BleManager;
import com.qyh.fastble.ble.conn.BleCharacterCallback;
import com.qyh.fastble.ble.conn.BleGattCallback;
import com.qyh.fastble.ble.conn.BleRssiCallback;
import com.qyh.fastble.ble.data.BleDevice;
import com.qyh.fastble.ble.exception.BleException;
import com.qyh.fastble.ble.scan.ListScanCallback;
import com.qyh.fastble.ble.utils.BleLog;
import com.qyh.fastble.ble.utils.HexUtil;

import java.util.Arrays;

/**
 * 蓝牙连接service
 */
public class BluetoothService extends Service {
    /** binder */
    public BluetoothBinder mBinder = new BluetoothBinder();
    /** 蓝牙管理类 */
    private BleManager bleManager;
    /** 主线程handler */
    private Handler threadHandler = new Handler(Looper.getMainLooper());
    /** 扫描并连接回调 */
    private Callback mCallback = null;
    /** 连接回调 */
    private Callback2 mCallback2 = null;
    /** 设备名 */
    private String name;
    /** 设备Mac地址 */
    private String mac;
    /** Gatt */
    private BluetoothGatt gatt;
    /** 服务码 */
    private BluetoothGattService service;
    /** 特征码 */
    private BluetoothGattCharacteristic characteristic;
    private int charaProp;

    /**
     * 初始化BleManager
     * 打开蓝牙
     */
    @Override
    public void onCreate() {
        BleLog.i("service onCreate");
        bleManager = BleManager.getInstance();
        bleManager.enableBluetooth();
    }

    @Override
    public void onDestroy() {
        BleLog.i("service onDestroy");
        super.onDestroy();
        bleManager = null;
        mCallback = null;
        mCallback2 = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * 取消绑定时, 关闭gatt连接
     * @param intent
     * @return
     */
    @Override
    public boolean onUnbind(Intent intent) {
        BleLog.i("service onUnbind closeBluetoothGatt");
        bleManager.closeBluetoothGatt();
        return super.onUnbind(intent);
    }

    /**
     * binder
     * getService(): 获取BluetoothService对象
     */
    public class BluetoothBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    /**
     * 设置扫描连接回调
     * @param callback
     */
    public void setScanCallback(Callback callback) {
        mCallback = callback;
    }

    /**
     * 设置连接回调
     * @param callback
     */
    public void setConnectCallback(Callback2 callback) {
        mCallback2 = callback;
    }

    /**
     * 扫描连接回调
     */
    public interface Callback {

        void onStartScan();

        void onScanning(BleDevice scanResult);

        void onScanComplete();

        void onConnecting();

        void onConnectFail(BleException exception);

        void onDisConnected();

        void onServicesDiscovered(BluetoothGatt gatt, int status);
    }

    /**
     *
     */
    public interface Callback2 {
        void onDisConnected();
    }

    /**
     * 扫描设备
     */
    public void scanDevice() {
        resetInfo();

        if (mCallback != null) {
            mCallback.onStartScan();
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
     * 取消扫描
     */
    public void cancelScan() {
        bleManager.cancelScan();
    }

    /**
     * 连接设备
     * @param scanResult
     */
    public void connectDevice(final BleDevice scanResult) {
        if (mCallback != null) {
            mCallback.onConnecting();
        }


        bleManager.connectDevice(scanResult, false, new BleGattCallback() {

            @Override
            public void onFoundDevice(BleDevice scanResult) {
                BluetoothService.this.name = scanResult.getDevice().getName(); // 设备名
                BluetoothService.this.mac = scanResult.getDevice().getAddress(); // 设备地址
            }

            @Override
            public void onConnecting(BluetoothGatt gatt, int status) {

            }

            @Override
            public void onConnectError(final BleException exception) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onConnectFail(exception);
                        }
                    }
                });
            }

            @Override
            public void onConnectSuccess(BluetoothGatt gatt, int status) {
                BleLog.i("连接状态: " + status);
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

                BluetoothService.this.gatt = gatt;
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onServicesDiscovered(gatt, status);
                        }
                    }
                });
            }

            @Override
            public void onDisConnected(final BluetoothGatt gatt, int status, BleException exception) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onDisConnected();
                        }
                        if (mCallback2 != null) {
                            mCallback2.onDisConnected();
                        }

                    }
                });
            }

        });
    }

    /**
     * 扫描并连接设备
     * @param name
     */
    public void scanNameAndConnect(String name) {
        resetInfo();

        if (mCallback != null) {
            mCallback.onStartScan();
        }

        bleManager.scanNameAndConnect(name, 5000, false, new BleGattCallback() {

            @Override
            public void onFoundDevice(BleDevice scanResult) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onScanComplete();
                        }
                    }
                });
                BluetoothService.this.name = scanResult.getDevice().getName();
                BluetoothService.this.mac = scanResult.getDevice().getAddress();
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onConnecting();
                        }
                    }
                });
            }

            @Override
            public void onConnecting(BluetoothGatt gatt, int status) {

            }

            @Override
            public void onConnectError(final BleException exception) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onConnectFail(exception);
                        }
                    }
                });
            }

            @Override
            public void onConnectSuccess(BluetoothGatt gatt, int status) {

            }

            @Override
            public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
                BluetoothService.this.gatt = gatt;
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onServicesDiscovered(gatt, status);
                        }
                    }
                });
            }

            @Override
            public void onDisConnected(BluetoothGatt gatt, int status, BleException exception) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onDisConnected();
                        }
                        if (mCallback2 != null) {
                            mCallback2.onDisConnected();
                        }
                    }
                });
            }
        });
    }

    /**
     * 模糊搜索, 并连接
     * @param name
     */
    public void scanFuzzyNameAndConnect(String name) {
        resetInfo();

        if (mCallback != null) {
            mCallback.onStartScan();
        }

        bleManager.scanfuzzyNameAndConnect(name, 5000, false, new BleGattCallback() {

            @Override
            public void onFoundDevice(BleDevice scanResult) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onScanComplete();
                        }
                    }
                });
                BluetoothService.this.name = scanResult.getDevice().getName();
                BluetoothService.this.mac = scanResult.getDevice().getAddress();
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onConnecting();
                        }
                    }
                });
            }

            @Override
            public void onConnecting(BluetoothGatt gatt, int status) {

            }

            @Override
            public void onConnectError(final BleException exception) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onConnectFail(exception);
                        }
                    }
                });
            }

            @Override
            public void onConnectSuccess(BluetoothGatt gatt, int status) {

            }

            @Override
            public void onDisConnected(BluetoothGatt gatt, int status, BleException exception) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onDisConnected();
                        }
                        if (mCallback2 != null) {
                            mCallback2.onDisConnected();
                        }
                    }
                });
            }

            @Override
            public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
                BluetoothService.this.gatt = gatt;
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onServicesDiscovered(gatt, status);
                        }
                    }
                });
            }
        });
    }

    public void scanNamesAndConnect(String[] names) {
        resetInfo();

        if (mCallback != null) {
            mCallback.onStartScan();
        }

        bleManager.scanNamesAndConnect(names, 5000, false, new BleGattCallback() {

            @Override
            public void onFoundDevice(BleDevice scanResult) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onScanComplete();
                        }
                    }
                });
                BluetoothService.this.name = scanResult.getDevice().getName();
                BluetoothService.this.mac = scanResult.getDevice().getAddress();
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onConnecting();
                        }
                    }
                });
            }

            @Override
            public void onConnecting(BluetoothGatt gatt, int status) {

            }

            @Override
            public void onConnectError(final BleException exception) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onConnectFail(exception);
                        }
                    }
                });
            }

            @Override
            public void onConnectSuccess(BluetoothGatt gatt, int status) {

            }

            @Override
            public void onDisConnected(BluetoothGatt gatt, int status, BleException exception) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onDisConnected();
                        }
                        if (mCallback2 != null) {
                            mCallback2.onDisConnected();
                        }
                    }
                });
            }

            @Override
            public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
                BluetoothService.this.gatt = gatt;
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onServicesDiscovered(gatt, status);
                        }
                    }
                });
            }
        });

    }

    public void scanFuzzyNamesAndConnect(String[] names) {
        resetInfo();

        if (mCallback != null) {
            mCallback.onStartScan();
        }

        bleManager.scanfuzzyNamesAndConnect(names, 5000, false, new BleGattCallback() {

            @Override
            public void onFoundDevice(BleDevice scanResult) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onScanComplete();
                        }
                    }
                });
                BluetoothService.this.name = scanResult.getDevice().getName();
                BluetoothService.this.mac = scanResult.getDevice().getAddress();
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onConnecting();
                        }
                    }
                });
            }

            @Override
            public void onConnecting(BluetoothGatt gatt, int status) {

            }

            @Override
            public void onConnectError(final BleException exception) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onConnectFail(exception);
                        }
                    }
                });
            }

            @Override
            public void onConnectSuccess(BluetoothGatt gatt, int status) {

            }

            @Override
            public void onDisConnected(BluetoothGatt gatt, int status, BleException exception) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onDisConnected();
                        }
                        if (mCallback2 != null) {
                            mCallback2.onDisConnected();
                        }
                    }
                });
            }

            @Override
            public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
                BluetoothService.this.gatt = gatt;
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onServicesDiscovered(gatt, status);
                        }
                    }
                });
            }
        });
    }

    /**
     * 根据MAC地址扫描设备并连接
     * @param mac
     */
    public void scanMacAndConnect(String mac) {
        resetInfo();

        if (mCallback != null) {
            mCallback.onStartScan();
        }

        bleManager.scanMacAndConnect(mac, 5000, false, new BleGattCallback() {

            @Override
            public void onFoundDevice(BleDevice scanResult) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onScanComplete();
                        }
                    }
                });
                BluetoothService.this.name = scanResult.getDevice().getName();
                BluetoothService.this.mac = scanResult.getDevice().getAddress();
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onConnecting();
                        }
                    }
                });
            }

            @Override
            public void onConnecting(BluetoothGatt gatt, int status) {

            }

            @Override
            public void onConnectError(final BleException exception) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onConnectFail(exception);
                        }
                    }
                });
            }

            @Override
            public void onConnectSuccess(BluetoothGatt gatt, int status) {

            }

            @Override
            public void onDisConnected(BluetoothGatt gatt, int status, BleException exception) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onDisConnected();
                        }
                        if (mCallback2 != null) {
                            mCallback2.onDisConnected();
                        }
                    }
                });
            }

            @Override
            public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
                BluetoothService.this.gatt = gatt;
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onServicesDiscovered(gatt, status);
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

    public boolean notify(String uuid_service, String uuid_notify, BleCharacterCallback callback) {
        return bleManager.notify(uuid_service, uuid_notify, callback);
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


    /**
     * 初始化信息
     */
    private void resetInfo() {
        name = null;
        mac = null;
        gatt = null;
        service = null;
        characteristic = null;
        charaProp = 0;
    }

    public String getName() {
        return name;
    }

    public String getMac() {
        return mac;
    }

    public BluetoothGatt getGatt() {
        return gatt;
    }

    public void setService(BluetoothGattService service) {
        this.service = service;
    }

    public BluetoothGattService getService() {
        return service;
    }

    public void setCharacteristic(BluetoothGattCharacteristic characteristic) {
        this.characteristic = characteristic;
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return characteristic;
    }

    /**
     * 权限
     * @param charaProp
     */
    public void setCharaProp(int charaProp) {
        this.charaProp = charaProp;
    }

    public int getCharaProp() {
        return charaProp;
    }

    /**
     * 在主线程执行
     * @param runnable
     */
    private void runOnMainThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            threadHandler.post(runnable);
        }
    }


}
