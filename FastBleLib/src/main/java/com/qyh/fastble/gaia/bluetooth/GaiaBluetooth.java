package com.qyh.fastble.gaia.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;

import com.qyh.fastble.ble.utils.BleLog;
import com.qyh.fastble.gaia.conn.GaiaCallback;
import com.qyh.fastble.gaia.conn.GaiaConnect;
import com.qyh.fastble.gaia.receiver.BREDRDiscoveryReceiver;
import com.qyh.fastble.gaia.scan.PeriodScanListener;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

import static android.content.ContentValues.TAG;

/**
 * @author 邱永恒
 * @time 2017/8/13  22:27
 * @desc ${TODD}
 */


public class GaiaBluetooth {

    private Context context;
    private BluetoothAdapter bluetoothAdapter;

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_SCANNING = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_TRANSFER = 4;

    private final int SCANNING_TIME = 10000;

    private int connectionState = STATE_DISCONNECTED;

    private final Handler handler = new Handler(Looper.getMainLooper());


    private BREDRDiscoveryReceiver discoveryReceiver = null;
    private PeriodScanListener periodScanListener;
    private GaiaConnect gaiaConnect;

    public GaiaBluetooth(Context context) {
        this.context = context.getApplicationContext();

        BluetoothManager bluetoothManager = (BluetoothManager) context
                .getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    /**
     * 发送数据给设备
     *
     * @param data
     * @return
     */
    public boolean sendData(byte[] data) {
        if (gaiaConnect == null) {
            return false;
        }

        return gaiaConnect.sendData(data);
    }

    /**
     * <p>All UUIDs over RFCOMM this application can use.</p>
     */
    public static class UUIDs {
        /**
         * The SPP UUID as defined by Bluetooth specifications.
         */
        public static final UUID SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        /**
         * The specific GAIA UUID.
         */
        public static final UUID GAIA = UUID.fromString("00001107-D102-11E1-9B23-00025B00A5A5");
    }

    /**
     * 是否开启蓝牙
     *
     * @return
     */
    public boolean isBlueEnable() {
        return bluetoothAdapter.isEnabled();
    }

    /**
     * 扫描设备
     *
     * @param scan
     * @param listener
     * @return
     */
    public boolean scanDevices(boolean scan, PeriodScanListener listener) {
        this.periodScanListener = listener;
        boolean isDiscovering = false;

        if (scan && !isInScanning()) {
            listener.setGaiaBluetooth(this).notifyScanStarted();

            discoveryReceiver = new BREDRDiscoveryReceiver(listener);
            // 开启广播
            registerReceiver();

            // 开始搜索
            isDiscovering = bluetoothAdapter.startDiscovery();

            if (isDiscovering) {
                connectionState = STATE_SCANNING;
            }
            BleLog.i("Start scan of LE devices: " + isInScanning() + " - start discovery of BR/EDR devices: " + isDiscovering);

        } else if (isInScanning()) {
            listener.removeHandlerMsg();

            // 取消广播
            unregisterReceiver();

            // 停止搜索
            isDiscovering = bluetoothAdapter.cancelDiscovery();

            if (isDiscovering) {
                connectionState = STATE_DISCONNECTED;
            }
            BleLog.i("Stop scan of LE devices - stop discovery of BR/EDR devices: " + isDiscovering);
        }

        return isDiscovering;
    }

    public void stopScan(PeriodScanListener listener) {
        scanDevices(false, listener);
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

    public boolean isDisConnected() {
        return connectionState == STATE_DISCONNECTED;
    }

    public boolean isTransfer() {
        return connectionState == STATE_TRANSFER;
    }

    public void setState(int state) {
        connectionState = state;
    }

    /**
     * 注册广播
     */
    private void registerReceiver() {
        if (discoveryReceiver != null) {
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            context.registerReceiver(discoveryReceiver, filter);
        }
    }

    /**
     * 取消广播
     */
    private void unregisterReceiver() {
        if (discoveryReceiver != null) {
            context.unregisterReceiver(discoveryReceiver);
        }
    }

    /**
     * 检查是否包含指定地址的蓝牙设备
     *
     * @param address
     * @return
     */
    public boolean checkBluetoothAddress(String address) {
        return BluetoothAdapter.checkBluetoothAddress(address);
    }

    /**
     * 根据地址获取蓝牙设备
     *
     * @param address
     * @return
     */
    public BluetoothDevice getRemoteDevice(String address) {
        return bluetoothAdapter.getRemoteDevice(address);
    }

    /**
     * 获取设备的SPP OR GAIA UUID
     *
     * @param uuids
     * @return
     */
    public UUID getUUIDTransport(ParcelUuid[] uuids) {
        if (uuids == null) {
            return null;
        }
        for (ParcelUuid parcel : uuids) {
            UUID uuid = parcel.getUuid();
            if (uuid.equals(UUIDs.SPP) || uuid.equals(UUIDs.GAIA)) {
                return uuid;
            }
        }
        return null;
    }

    /**
     * 连接设备
     *
     * @param device
     * @param callback
     * @return
     */
    public boolean connect(BluetoothDevice device, GaiaCallback callback) {
        if (device == null) {
            BleLog.i("error: device is null");
            return false;
        }

        if (isConnected()) {
            BleLog.i("error: 蓝牙设备已连接");
            return false;
        }

        // DEVICE_TYPE_CLASSIC: 传统蓝牙
        // DEVICE_TYPE_DUAL: 双模蓝牙, 支持GAIA AND BLE
        if (device.getType() != BluetoothDevice.DEVICE_TYPE_CLASSIC
                && device.getType() != BluetoothDevice.DEVICE_TYPE_DUAL) {
            Log.w(TAG, "connection failed: the device is not BR/EDR compatible.");
            // 设备不支持gaia连接
            return false;
        }

        if (!checkBluetoothAddress(device.getAddress())) {
            BleLog.i("error: address is no device");
            return false;
        }

        if (!isBlueEnable()) {
            BleLog.i("error: bluetooth must be open");
            return false;
        }

        // 找到设备的SPP GAIA UUID
        UUID transport = getUUIDTransport(device.getUuids());

        // connection can be processed only if a compatible transport has been found.
        // if the device has not yet been bonded, the UUIDs has not been fetched yet by the system.
        // 如果设备已经配对了, 但是找不到匹配的UUID, 默认使用SPP的UUID进行连接
        if (transport == null && device.getBondState() != BluetoothDevice.BOND_BONDED) {
            Log.i(TAG, "connection: device not bonded, no UUID available, attempt to connect using SPP.");
            transport = GaiaBluetooth.UUIDs.SPP;
        } else if (transport == null) {
            Log.w(TAG, "connection failed: device bonded and no compatible UUID available.");
            return false;
        }

        return connect(device, transport, callback);
    }

    /**
     * 连接设备
     *
     * @param device
     * @param transport
     * @param callback
     * @return
     */
    public boolean connect(BluetoothDevice device, UUID transport, GaiaCallback callback) {
        // Check there is no running connection
        if (isConnected() || isTransfer()) {
            Log.w(TAG, "connection failed: Provider is already connected to a device with an active communication.");
            return false;
        }

        // 创建连接对象
        gaiaConnect = GaiaConnect.getInstance(this, callback);

        // 初始化线程
        gaiaConnect.cancelConnectionThread();
        gaiaConnect.cancelCommunicationThread();

        setState(GaiaBluetooth.STATE_CONNECTING);

        // 创建socket对象
        BluetoothSocket socket = createSocket(device, transport);

        if (socket == null) {
            Log.w(TAG, "connection failed: creation of a Bluetooth socket failed.");
            return false; // socket creation failed
        }

        // 启动连接线程
        gaiaConnect.startConnectionThread(socket);

        return true;
    }

    /**
     * 根据SDK版本, 获取socket对象
     *
     * @param device
     * @param transport
     * @return
     */
    public BluetoothSocket createSocket(BluetoothDevice device, UUID transport) {
        try {
            if (btIsSecure()) {
                return device.createInsecureRfcommSocketToServiceRecord(transport);
            } else {
                return device.createRfcommSocketToServiceRecord(transport);
            }
        } catch (IOException e) {
            try {
                // This is a workaround that reportedly helps on some older devices like HTC Desire, where using
                // the standard createRfcommSocketToServiceRecord() method always causes connect() to fail.
                //noinspection RedundantArrayCreation
                Method method = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                // noinspection UnnecessaryBoxing
                return (BluetoothSocket) method.invoke(device, Integer.valueOf(1));
            } catch (Exception e1) {
                // NoSuchMethodException from method getMethod: impossible to retrieve the method.
                // IllegalArgumentException from method invoke: problem with arguments which don't match with
                // expectations.
                // IllegalAccessException from method invoke: if invoked object is not accessible.
                // InvocationTargetException from method invoke: Exception thrown by the invoked method.
            }
        }

        return null;
    }

    /**
     * Check for RFCOMM security.
     *
     * @return True if RFCOMM security is implemented.
     */
    public boolean btIsSecure() {
        // Establish if RFCOMM security is implemented, in which case we'll
        // use the insecure variants of the RFCOMM functions
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1;
    }

    /**
     * 连接设备
     *
     * @param address
     * @param callback
     * @return
     */
    public boolean connect(String address, GaiaCallback callback) {
        if (TextUtils.isEmpty(address)) {
            BleLog.i("error: address is null");
            return false;
        }

        if (!isBlueEnable()) {
            BleLog.i("error: bluetooth must be open");
            return false;
        }

        if (!checkBluetoothAddress(address)) {
            BleLog.i("error: address is no device");
            return false;
        }

        BluetoothDevice device = getRemoteDevice(address);

        if (device == null) {
            BleLog.i("error: device is null");
            return false;
        }

        return connect(device, callback);
    }
}
