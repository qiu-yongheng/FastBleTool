package com.qyh.fastble.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.util.Log;

import com.qyh.fastble.ble.bluetooth.BleBluetooth;
import com.qyh.fastble.ble.conn.BleCharacterCallback;
import com.qyh.fastble.ble.conn.BleGattCallback;
import com.qyh.fastble.ble.conn.BleRssiCallback;
import com.qyh.fastble.ble.data.BleDevice;
import com.qyh.fastble.ble.exception.BleException;
import com.qyh.fastble.ble.exception.BlueToothNotEnableException;
import com.qyh.fastble.ble.exception.NotFoundDeviceException;
import com.qyh.fastble.ble.exception.hanlder.DefaultBleExceptionHandler;
import com.qyh.fastble.ble.scan.ListScanCallback;
import com.qyh.fastble.ble.service.BluetoothLeService;
import com.qyh.fastble.ble.utils.BleLog;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * @author 邱永恒
 * @time 2017/8/13  09:20
 * @desc 蓝牙BLE操作管理类
 */
public class BleManager {

    private Context context;
    private BleBluetooth bleBluetooth;
    private DefaultBleExceptionHandler bleExceptionHandler;
    private Intent intent;

    /**
     * 创建BLE管理类
     * @param context
     */
    public BleManager(Context context) {
        this.context = context.getApplicationContext();

        if (isSupportBle()) {
            if (bleBluetooth == null) {
                bleBluetooth = new BleBluetooth(context);
            }
        } else {
            handleException(new BlueToothNotEnableException());
        }

        // 异常处理
        bleExceptionHandler = new DefaultBleExceptionHandler(context);
    }



    /**
     * handle Exception Information
     */
    public void handleException(BleException exception) {
        bleExceptionHandler.handleException(exception);
    }

    /**
     * scan device around
     * @param callback
     * @return
     */
    public boolean scanDevice(ListScanCallback callback) {
        if (!isBlueEnable()) {
            handleException(new BlueToothNotEnableException());
            return false;
        }

        return bleBluetooth.startLeScan(callback);
    }

    /**
     * connect a searched device
     *
     * @param bleDevice 封装了蓝牙设备信息的类
     * @param autoConnect 是否自动连接, 默认false
     * @param callback 连接回调
     */
    public void connectDevice(BleDevice bleDevice,
                              boolean autoConnect,
                              BleGattCallback callback) {
        if (bleDevice == null || bleDevice.getDevice() == null) {
            if (callback != null) {
                callback.onConnectError(new NotFoundDeviceException());
            }
        } else {
            if (callback != null) {
                callback.onFoundDevice(bleDevice);
            }
            bleBluetooth.connect(bleDevice, autoConnect, callback);
        }
    }

    /**
     * scan a known name device, then connect
     *
     * @param deviceName
     * @param time_out
     * @param autoConnect
     * @param callback
     */
    public void scanNameAndConnect(String deviceName,
                                   long time_out,
                                   boolean autoConnect,
                                   BleGattCallback callback) {
        if (!isBlueEnable() && callback != null) {
            callback.onConnectError(new BlueToothNotEnableException());
        } else {
            bleBluetooth.scanNameAndConnect(deviceName, time_out, autoConnect, callback);
        }
    }

    /**
     * scan known names device, then connect
     *
     * @param deviceNames
     * @param time_out
     * @param autoConnect
     * @param callback
     */
    public void scanNamesAndConnect(String[] deviceNames,
                                    long time_out,
                                    boolean autoConnect,
                                    BleGattCallback callback) {
        if (!isBlueEnable() && callback != null) {
            callback.onConnectError(new BlueToothNotEnableException());
        } else {
            bleBluetooth.scanNameAndConnect(deviceNames, time_out, autoConnect, callback);
        }
    }

    /**
     * fuzzy search name
     *
     * @param fuzzyName
     * @param time_out
     * @param autoConnect
     * @param callback
     */
    public void scanfuzzyNameAndConnect(String fuzzyName,
                                        long time_out,
                                        boolean autoConnect,
                                        BleGattCallback callback) {
        if (!isBlueEnable() && callback != null) {
            callback.onConnectError(new BlueToothNotEnableException());
        } else {
            bleBluetooth.scanNameAndConnect(fuzzyName, time_out, autoConnect, true, callback);
        }
    }

    /**
     * fuzzy search name
     *
     * @param fuzzyNames
     * @param time_out
     * @param autoConnect
     * @param callback
     */
    public void scanfuzzyNamesAndConnect(String[] fuzzyNames,
                                         long time_out,
                                         boolean autoConnect,
                                         BleGattCallback callback) {
        if (!isBlueEnable() && callback != null) {
            callback.onConnectError(new BlueToothNotEnableException());
        } else {
            bleBluetooth.scanNameAndConnect(fuzzyNames, time_out, autoConnect, true, callback);
        }
    }

    /**
     * scan a known mca device, then connect
     *
     * @param deviceMac
     * @param time_out
     * @param autoConnect
     * @param callback
     */
    public void scanMacAndConnect(String deviceMac,
                                  long time_out,
                                  boolean autoConnect,
                                  BleGattCallback callback) {
        if (!isBlueEnable() && callback != null) {
            callback.onConnectError(new BlueToothNotEnableException());
        } else {
            bleBluetooth.scanMacAndConnect(deviceMac, time_out, autoConnect, callback);
        }
    }

    /**
     * 取消扫描
     */
    public void cancelScan() {
        bleBluetooth.cancelScan();
    }

    /**
     * 订阅通知notify
     *
     * @param uuid_service 服务码
     * @param uuid_notify 特征码
     * @param callback 获取特征码返回数据的回调
     * @return
     */
    public boolean notify(String uuid_service,
                          String uuid_notify,
                          BleCharacterCallback callback) {
        BleLog.i("==", "nority service : " + uuid_service + ", \ncharacter : " + uuid_notify);

        return bleBluetooth.newBleConnector()
                .withUUIDString(uuid_service, uuid_notify, null)
                .enableCharacteristicNotify(callback, uuid_notify);
    }

    /**
     * 订阅通知indicate
     *
     * @param uuid_service 服务码
     * @param uuid_indicate 特征码
     * @param callback 获取特征码返回数据的回调
     * @return
     */
    public boolean indicate(String uuid_service,
                            String uuid_indicate,
                            BleCharacterCallback callback) {
        return bleBluetooth.newBleConnector()
                .withUUIDString(uuid_service, uuid_indicate, null)
                .enableCharacteristicIndicate(callback, uuid_indicate);
    }

    /**
     * stop notify, remove callback
     *
     * @param uuid_service
     * @param uuid_notify
     * @return
     */
    public boolean stopNotify(String uuid_service, String uuid_notify) {
        boolean success = bleBluetooth.newBleConnector()
                .withUUIDString(uuid_service, uuid_notify, null)
                .disableCharacteristicNotify();
        if (success) {
            bleBluetooth.removeGattCallback(uuid_notify);
        }
        return success;
    }

    /**
     * stop indicate, remove callback
     *
     * @param uuid_service
     * @param uuid_indicate
     * @return
     */
    public boolean stopIndicate(String uuid_service, String uuid_indicate) {
        boolean success = bleBluetooth.newBleConnector()
                .withUUIDString(uuid_service, uuid_indicate, null)
                .disableCharacteristicIndicate();
        if (success) {
            bleBluetooth.removeGattCallback(uuid_indicate);
        }
        return success;
    }

    /**
     * write
     *
     * 向特征码写入数据, 发送给远程设备
     *
     * @param uuid_service 服务码
     * @param uuid_write 特征码
     * @param data 写入的数据
     * @param callback
     * @return
     */
    public boolean writeDevice(String uuid_service,
                               String uuid_write,
                               byte[] data,
                               BleCharacterCallback callback) {
        return bleBluetooth.newBleConnector()
                .withUUIDString(uuid_service, uuid_write, null)
                .writeCharacteristic(data, callback, uuid_write);
    }

    /**
     * read
     *
     * 读取特征码数据
     *
     * @param uuid_service
     * @param uuid_read
     * @param callback
     * @return
     */
    public boolean readDevice(String uuid_service,
                              String uuid_read,
                              BleCharacterCallback callback) {
        return bleBluetooth.newBleConnector()
                .withUUIDString(uuid_service, uuid_read, null)
                .readCharacteristic(callback, uuid_read);
    }

    /**
     * read Rssi
     *
     * @param callback
     * @return
     */
    public boolean readRssi(BleRssiCallback callback) {
        return bleBluetooth.newBleConnector()
                .readRemoteRssi(callback);
    }


    /**
     * refresh Device Cache
     */
    public void refreshDeviceCache() {
        bleBluetooth.refreshDeviceCache();
    }

    /**
     * close gatt
     *
     * 退出界面必须调用此方法, 断开GATT连接, 不然会阻塞, 下次连接不上
     */
    public void closeBluetoothGatt() {
        if (bleBluetooth != null) {
            bleBluetooth.clearCallback();
            try {
                Log.d("==", "gatt close");
                bleBluetooth.closeBluetoothGatt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * is support ble?
     *
     * @return
     */
    public boolean isSupportBle() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * open bluetooth 没有弹窗提示
     */
    public void enableBluetooth() {
        if (bleBluetooth != null) {
            bleBluetooth.enableBluetoothIfDisabled();
        }
    }

    /**
     * close bluetooth
     */
    public void disableBluetooth() {
        if (bleBluetooth != null) {
            bleBluetooth.disableBluetooth();
        }
    }

    /**
     * 是否已开启蓝牙
     * @return
     */
    public boolean isBlueEnable() {
        return bleBluetooth != null && bleBluetooth.isBlueEnable();
    }

    /**
     * 是否正在扫描
     * @return
     */
    public boolean isInScanning() {
        return bleBluetooth.isInScanning();
    }

    /**
     * 正在连接或已连接
     * @return
     */
    public boolean isConnectingOrConnected() {
        return bleBluetooth.isConnectingOrConnected();
    }

    /**
     * 已连接
     * @return
     */
    public boolean isConnected() {
        return bleBluetooth.isConnected();
    }

    /**
     * 服务被发现
     * @return
     */
    public boolean isServiceDiscovered() {
        return bleBluetooth.isServiceDiscovered();
    }

    /**
     * remove callback form a character
     * 移除特征码对应的回调
     */
    public void stopListenCharacterCallback(String uuid) {
        bleBluetooth.removeGattCallback(uuid);
    }

    /**
     * remove callback for gatt connect
     */
    public void stopListenConnectCallback() {
        bleBluetooth.removeConnectGattCallback();
    }

    public BleBluetooth getBleBluetooth() {
        return bleBluetooth;
    }

    /**
     * 根据MAC地址获取远程设备
     * @param address
     */
    public BluetoothDevice getRemoteDevice(String address) {
        return bleBluetooth.getTemoteDevice(address);
    }

    /**
     * 启动service
     */
    public void startAndBindService(Context context, ServiceConnection connection) {
        intent = new Intent(context, BluetoothLeService.class);
        context.startService(intent);
        context.bindService(intent, connection, BIND_AUTO_CREATE);
    }

    /**
     * 绑定service
     * @param context
     * @param connection
     */
    public void bindService(Context context, ServiceConnection connection) {
        intent = new Intent(context, BluetoothLeService.class);
        context.bindService(intent, connection, BIND_AUTO_CREATE);
    }

    /**
     * 取消绑定service
     * @param context
     * @param connection
     */
    public void unBindService(Context context, ServiceConnection connection) {
        if (connection == null) {
            throw new RuntimeException("ServiceConnection is null");
        }
        context.unbindService(connection);
    }

    /**
     * 停止service
     * @param context
     */
    public void stopService(Context context) {
        if (intent == null) {
            throw new RuntimeException("intent is null");
        }
        context.stopService(intent);
    }
}
