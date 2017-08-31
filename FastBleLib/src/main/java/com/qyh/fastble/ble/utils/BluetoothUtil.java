package com.qyh.fastble.ble.utils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

/**
 * @author MaTianyu
 * @date 2015-03-12
 * 蓝牙操作工具类
 */
public class BluetoothUtil {

    private static final String TAG = "BluetoothUtil";

    /**
     * 开启蓝牙
     * @param activity
     * @param requestCode
     */
    public static void enableBluetooth(Activity activity, int requestCode) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 打印所有的service, 特征吗, 描述符
     * 在service被发现后, 可以调用
     * @param gatt
     */
    public static void printServices(BluetoothGatt gatt) {
        if (gatt != null) {
            for (BluetoothGattService service : gatt.getServices()) {
                BleLog.i(TAG, "service: " + service.getUuid());
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    BleLog.d(TAG, "  characteristic: " + characteristic.getUuid() + " value: " + Arrays.toString(characteristic.getValue()));
                    for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                        BleLog.v(TAG, "        descriptor: " + descriptor.getUuid() + " value: " + Arrays.toString(descriptor.getValue()));
                    }
                }
            }
        }
    }

    /*------------  BluetoothGatt  ------------ */
    /**
     * Clears the device cache. After uploading new hello4 the DFU target will have other services than before.
     * 刷新设备缓存
     */
    public static boolean refreshDeviceCache(BluetoothGatt gatt) {
        /*
         * There is a refresh() method in BluetoothGatt class but for now it's hidden. We will call it using reflections.
		 */
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (refresh != null) {
                final boolean success = (Boolean) refresh.invoke(gatt);
                Log.i(TAG, "Refreshing result: " + success);
                return success;
            }
        } catch (Exception e) {
            Log.e(TAG, "An exception occured while refreshing device", e);
        }
        return false;
    }

    /**
     * 关闭GATT连接
     * @param gatt
     */
    public static void closeBluetoothGatt(BluetoothGatt gatt) {
        if (gatt != null) {
            gatt.disconnect();
            refreshDeviceCache(gatt);
            gatt.close();
        }
    }

    /*------------  Service  ------------ */

    /**
     * 根据UUID, 获取service
     * @param gatt
     * @param serviceUUID
     * @return
     */
    public static BluetoothGattService getService(BluetoothGatt gatt, String serviceUUID) {
        return gatt.getService(UUID.fromString(serviceUUID));
    }

    /*------------  Characteristic服务  ------------ */

    /**
     * 根据UUDI, 获取character
     * @param service
     * @param charactUUID
     * @return
     */
    public static BluetoothGattCharacteristic getCharacteristic(BluetoothGattService service, String charactUUID) {
        if (service != null) {
            return service.getCharacteristic(UUID.fromString(charactUUID));
        }
        return null;
    }

    /**
     * 根据service, character , 获取character
     * @param gatt
     * @param serviceUUID
     * @param charactUUID
     * @return
     */
    public static BluetoothGattCharacteristic getCharacteristic(BluetoothGatt gatt, String serviceUUID, String charactUUID) {
        BluetoothGattService service = gatt.getService(UUID.fromString(serviceUUID));
        if (service != null) {
            return service.getCharacteristic(UUID.fromString(charactUUID));
        }
        return null;
    }

}
