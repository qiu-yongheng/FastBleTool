package com.qyh.fastble.ble.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;

import com.qyh.fastble.ble.conn.BleConnector;
import com.qyh.fastble.ble.conn.BleGattCallback;
import com.qyh.fastble.ble.data.BleDevice;
import com.qyh.fastble.ble.exception.BleException;
import com.qyh.fastble.ble.exception.ConnectException;
import com.qyh.fastble.ble.exception.NotFoundDeviceException;
import com.qyh.fastble.ble.exception.ScanFailedException;
import com.qyh.fastble.ble.scan.MacScanCallback;
import com.qyh.fastble.ble.scan.NameScanCallback;
import com.qyh.fastble.ble.scan.PeriodScanCallback;
import com.qyh.fastble.ble.utils.BleLog;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author 邱永恒
 * @time 2017/8/13  09:20
 * @desc ${TODD}
 */
public class BleBluetooth {

    private static final String CONNECT_CALLBACK_KEY = "connect_key";
    public static final String READ_RSSI_KEY = "rssi_key";

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_SCANNING = 1;
    private static final int STATE_CONNECTING = 2;
    private static final int STATE_CONNECTED = 3;
    private static final int STATE_SERVICES_DISCOVERED = 4;

    private int connectionState = STATE_DISCONNECTED;
    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private Handler handler = new Handler(Looper.getMainLooper());
    private HashMap<String, BluetoothGattCallback> callbackHashMap = new HashMap<>();
    private PeriodScanCallback periodScanCallback;


    public BleBluetooth(Context context) {
        this.context = context = context.getApplicationContext();
        BluetoothManager bluetoothManager = (BluetoothManager) context
                .getSystemService(Context.BLUETOOTH_SERVICE);

        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    public BleConnector newBleConnector() {
        return new BleConnector(this);
    }


    public boolean isInScanning() {
        return connectionState == STATE_SCANNING;
    }

    public boolean isConnectingOrConnected() {
        return connectionState >= STATE_CONNECTING;
    }

    public boolean isConnected() {
        return connectionState >= STATE_CONNECTED;
    }

    public boolean isServiceDiscovered() {
        return connectionState == STATE_SERVICES_DISCOVERED;
    }


    private void addConnectGattCallback(BleGattCallback callback) {
        callbackHashMap.put(CONNECT_CALLBACK_KEY, callback);
    }

    public void addGattCallback(String uuid, BluetoothGattCallback callback) {
        callbackHashMap.put(uuid, callback);
    }

    public void removeConnectGattCallback() {
        callbackHashMap.remove(CONNECT_CALLBACK_KEY);
    }

    public void removeGattCallback(String key) {
        callbackHashMap.remove(key);
    }

    public void clearCallback() {
        callbackHashMap.clear();
    }

    public BluetoothGattCallback getGattCallback(String uuid) {
        if (TextUtils.isEmpty(uuid))
            return null;
        return callbackHashMap.get(uuid);
    }

    public boolean startLeScan(PeriodScanCallback callback) {
        this.periodScanCallback = callback;
        callback.setBleBluetooth(this).notifyScanStarted();
        boolean success = bluetoothAdapter.startLeScan(callback);
        if (success) {
            connectionState = STATE_SCANNING;
        } else {
            callback.removeHandlerMsg();
        }
        return success;
    }

    /**
     * 暴露给外界使用
     */
    public void cancelScan() {
        if (periodScanCallback != null && connectionState == STATE_SCANNING)
            periodScanCallback.notifyScanCancel();
    }

    /**
     * 在扫描回调中使用
     *
     * @param callback
     */
    public void stopScan(BluetoothAdapter.LeScanCallback callback) {
        if (callback instanceof PeriodScanCallback) {
            ((PeriodScanCallback) callback).removeHandlerMsg();
        }
        bluetoothAdapter.stopLeScan(callback);
        if (connectionState == STATE_SCANNING) {
            connectionState = STATE_DISCONNECTED;
        }
    }

    /**
     * @param bleDevice
     * @param autoConnect
     * @param callback
     * @return
     */
    public synchronized BluetoothGatt connect(BleDevice bleDevice,
                                              boolean autoConnect,
                                              BleGattCallback callback) {
        addConnectGattCallback(callback);
        BleLog.i("connect name: " + bleDevice.getDevice().getName()
                + "\nmac: " + bleDevice.getDevice().getAddress()
                + "\ndevice is remote: " + BluetoothAdapter.checkBluetoothAddress(bleDevice.getDevice().getAddress())
                + "\nautoConnect: " + autoConnect
                + "\ncallback num: " + callbackHashMap.size());
        return bleDevice.getDevice().connectGatt(context, autoConnect, coreGattCallback, BluetoothDevice.TRANSPORT_LE);
    }

    public void scanNameAndConnect(String name, long time_out, final boolean autoConnect, final BleGattCallback callback) {
        scanNameAndConnect(name, time_out, autoConnect, false, callback);
    }

    public void scanNameAndConnect(String name, long time_out, final boolean autoConnect, boolean fuzzy, final BleGattCallback callback) {
        if (TextUtils.isEmpty(name)) {
            if (callback != null) {
                callback.onConnectError(new NotFoundDeviceException());
            }
            return;
        }
        boolean success = startLeScan(new NameScanCallback(name, time_out, fuzzy) {

            @Override
            public void onDeviceFound(final BleDevice scanResult) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onFoundDevice(scanResult);
                        }
                        connect(scanResult, autoConnect, callback);
                    }
                });
            }

            @Override
            public void onDeviceNotFound() {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onConnectError(new NotFoundDeviceException());
                        }
                    }
                });
            }
        });
        if (!success && callback != null) {
            callback.onConnectError(new ScanFailedException());
        }
    }

    public void scanNameAndConnect(String[] names, long time_out, final boolean autoConnect, final BleGattCallback callback) {
        scanNameAndConnect(names, time_out, autoConnect, false, callback);
    }

    public void scanNameAndConnect(String[] names, long time_out, final boolean autoConnect, boolean fuzzy, final BleGattCallback callback) {
        if (names == null || names.length < 1) {
            if (callback != null) {
                callback.onConnectError(new NotFoundDeviceException());
            }
            return;
        }
        boolean success = startLeScan(new NameScanCallback(names, time_out, fuzzy) {

            @Override
            public void onDeviceFound(final BleDevice scanResult) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onFoundDevice(scanResult);
                        }
                        connect(scanResult, autoConnect, callback);
                    }
                });
            }

            @Override
            public void onDeviceNotFound() {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onConnectError(new NotFoundDeviceException());
                        }
                    }
                });
            }
        });
        if (!success && callback != null) {
            callback.onConnectError(new ScanFailedException());
        }
    }

    public void scanMacAndConnect(String mac, long time_out, final boolean autoConnect, final BleGattCallback callback) {
        if (TextUtils.isEmpty(mac)) {
            if (callback != null) {
                callback.onConnectError(new NotFoundDeviceException());
            }
            return;
        }
        boolean success = startLeScan(new MacScanCallback(mac, time_out) {

            @Override
            public void onDeviceFound(final BleDevice scanResult) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onFoundDevice(scanResult);
                        }
                        connect(scanResult, autoConnect, callback);
                    }
                });
            }

            @Override
            public void onDeviceNotFound() {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onConnectError(new NotFoundDeviceException());
                        }
                    }
                });
            }
        });
        if (!success && callback != null) {
            callback.onConnectError(new ScanFailedException());
        }
    }

    public boolean refreshDeviceCache() {
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (refresh != null) {
                boolean success = (Boolean) refresh.invoke(getBluetoothGatt());
                BleLog.i("refreshDeviceCache, is success:  " + success);
                return success;
            }
        } catch (Exception e) {
            BleLog.i("exception occur while refreshing device: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 退出界面必须调用此方法, 断开GATT连接, 不然会阻塞, 下次连接不上
     */
    public void closeBluetoothGatt() {
        /** 断开gatt连接 */
        if (bluetoothGatt != null) {
            BleLog.i("Request disconnect from BluetoothDevice " + bluetoothGatt.getDevice().getAddress() + " starts.");
            BleLog.i("gatt disconnect");
            bluetoothGatt.disconnect();
        }

        /** 刷新缓存 */
        if (bluetoothGatt != null) {
            refreshDeviceCache();
        }

        /** 关闭GATT连接 */
        if (bluetoothGatt != null) {
            BleLog.i("gatt close");
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

    public void enableBluetoothIfDisabled() {
        if (!isBlueEnable()) {
            enableBluetooth();
        }
    }

    /**
     * 获取特征码的权限特性
     *
     * @param characteristic
     * @return
     */
    public String getProperty(BluetoothGattCharacteristic characteristic) {
        int properties = characteristic.getProperties();
        StringBuffer sb = new StringBuffer();
        /** &运算, 一般做查询处理 */
        /** 获取当前权限, 与指定权限 & 运算, 如果 = 1, 有权限, = 0, 没权限 */
        if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            sb.append("Read");
            sb.append(" , ");
        }
        if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
            sb.append("Write");
            sb.append(" , ");
        }
        if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
            sb.append("Write No Response");
            sb.append(" , ");
        }
        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            sb.append("Notify");
            sb.append(" , ");
        }
        if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
            sb.append("Indicate");
            sb.append(" , ");
        }

        return sb.toString();
    }

    public boolean isBlueEnable() {
        return bluetoothAdapter.isEnabled();
    }

    public void enableBluetooth() {
        bluetoothAdapter.enable();
    }

    public void disableBluetooth() {
        bluetoothAdapter.disable();
    }

    private void runOnMainThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }

    public Context getContext() {
        return context;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }

    public int getConnectionState() {
        return connectionState;
    }

    private BleGattCallback coreGattCallback = new BleGattCallback() {

        @Override
        public void onFoundDevice(BleDevice bleDevice) {
            BleLog.i("BleGattCallback：onFoundDevice ");
        }

        @Override
        public void onConnecting(BluetoothGatt gatt, int status) {
            BleLog.i("BleGattCallback：onConnectSuccess ");

            bluetoothGatt = gatt;
            Iterator iterator = callbackHashMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object call = entry.getValue();
                if (call instanceof BleGattCallback) {
                    ((BleGattCallback) call).onConnecting(gatt, status);
                }
            }
        }

        @Override
        public void onConnectSuccess(BluetoothGatt gatt, int status) {
            BleLog.i("BleGattCallback：onConnectSuccess ");

            bluetoothGatt = gatt;
            Iterator iterator = callbackHashMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object call = entry.getValue();


                if (call instanceof BleGattCallback) {
                    ((BleGattCallback) call).onConnectSuccess(gatt, status);

                    /** 断开连接后, 进行重连, 在进行discoverServices操作前, 必须睡眠一段时间, 不然在连接成功后, 获取不到service */
                    SystemClock.sleep(100);
                    gatt.discoverServices();
                }
            }
        }

        @Override
        public void onDisConnected(BluetoothGatt gatt, int status, BleException exception) {
            BleLog.i("BleGattCallback：onConnectFailure ");

            gatt.close();
            bluetoothGatt = null;

            //closeBluetoothGatt();
            //bluetoothGatt = null;
            Iterator iterator = callbackHashMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object call = entry.getValue();
                if (call instanceof BleGattCallback) {
                    ((BleGattCallback) call).onDisConnected(gatt, status, exception);
                }
            }
        }

        @Override
        public void onConnectError(BleException exception) {
            BleLog.i("BleGattCallback：onConnectError ");
        }

        /**
         *
         * @param gatt GATT
         * @param status 连接状态, 成功=0 BluetoothGatt.STATE_SUCCESS
         * @param newState 详细状态
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            BleLog.i("BleGattCallback：onConnectionStateChange "
                    + '\n' + "status: " + status
                    + '\n' + "newState: " + newState
                    + '\n' + "currentThread: " + Thread.currentThread().getId());

            //            bluetoothGatt = gatt;

            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                connectionState = STATE_CONNECTED;
                onConnectSuccess(gatt, newState);

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                BleLog.e("BleBluetooth", "connectError: " + status);
                connectionState = STATE_DISCONNECTED;
                onDisConnected(gatt, newState, new ConnectException(gatt, newState));

            } else if (newState == BluetoothProfile.STATE_CONNECTING) {
                connectionState = STATE_CONNECTING;
                onConnecting(gatt, newState);
            }

            Iterator iterator = callbackHashMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object call = entry.getValue();
                if (call instanceof BluetoothGattCallback) {
                    ((BluetoothGattCallback) call).onConnectionStateChange(gatt, status, newState);
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BleLog.i("BleGattCallback：onServicesDiscovered :" + gatt.getServices().size());
            BleLog.i("BleGattCallback：onServicesDiscovered status:" + status);
            BleLog.i("BluetoothGatt.GATT_SUCCESS:" + BluetoothGatt.GATT_SUCCESS);

//            if (status == BluetoothGatt.GATT_SUCCESS) {
                connectionState = STATE_SERVICES_DISCOVERED;
                Iterator iterator = callbackHashMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    Object call = entry.getValue();
                    if (call instanceof BluetoothGattCallback) {
                        ((BluetoothGattCallback) call).onServicesDiscovered(gatt, status);
                    }
                }
//            } else if (status == 129) { // GATT_INTERNAL_ERROR
//                BleLog.i("GATT_INTERNAL_ERROR: 129, 重启蓝牙");
//            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            BleLog.i("BleGattCallback：onCharacteristicRead ");

            Iterator iterator = callbackHashMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object call = entry.getValue();
                if (call instanceof BluetoothGattCallback) {
                    ((BluetoothGattCallback) call).onCharacteristicRead(gatt, characteristic, status);
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            BleLog.i("BleGattCallback：onCharacteristicWrite ");

            Iterator iterator = callbackHashMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object call = entry.getValue();
                if (call instanceof BluetoothGattCallback) {
                    ((BluetoothGattCallback) call).onCharacteristicWrite(gatt, characteristic, status);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            BleLog.i("BleGattCallback：onCharacteristicChanged ");

            Iterator iterator = callbackHashMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object call = entry.getValue();
                if (call instanceof BluetoothGattCallback) {
                    ((BluetoothGattCallback) call).onCharacteristicChanged(gatt, characteristic);
                }
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            BleLog.i("BleGattCallback：onDescriptorRead ");

            Iterator iterator = callbackHashMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object call = entry.getValue();
                if (call instanceof BluetoothGattCallback) {
                    ((BluetoothGattCallback) call).onDescriptorRead(gatt, descriptor, status);
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            BleLog.i("BleGattCallback：onDescriptorWrite ");

            Iterator iterator = callbackHashMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object call = entry.getValue();
                if (call instanceof BluetoothGattCallback) {
                    ((BluetoothGattCallback) call).onDescriptorWrite(gatt, descriptor, status);
                }
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            BleLog.i("BleGattCallback：onReliableWriteCompleted ");

            Iterator iterator = callbackHashMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object call = entry.getValue();
                if (call instanceof BluetoothGattCallback) {
                    ((BluetoothGattCallback) call).onReliableWriteCompleted(gatt, status);
                }
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            BleLog.i("BleGattCallback：onReadRemoteRssi ");

            Iterator iterator = callbackHashMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object call = entry.getValue();
                if (call instanceof BluetoothGattCallback) {
                    ((BluetoothGattCallback) call).onReadRemoteRssi(gatt, rssi, status);
                }
            }
        }
    };
}
