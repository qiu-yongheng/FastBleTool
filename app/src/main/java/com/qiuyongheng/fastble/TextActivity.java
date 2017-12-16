package com.qiuyongheng.fastble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.qyh.fastble.ble.BleManager;
import com.qyh.fastble.ble.callback.BleServiceCallBack;
import com.qyh.fastble.ble.data.BleDevice;
import com.qyh.fastble.ble.service.BluetoothLeService;
import com.qyh.fastble.ble.utils.BleLog;
import com.qyh.fastble.ble.utils.HexUtil;

/**
 * @author 邱永恒
 * @time 2017/10/17  11:09
 * @desc ${TODD}
 */

public class TextActivity extends AppCompatActivity {

    private TextView tvHeart;
    private BleManager bleManager;
    private BluetoothLeService mBluetoothService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        tvHeart = (TextView) findViewById(R.id.tv_heart);


        bleManager = BleManager.getInstance();
        bleManager.bindService(this, mFhrSCon);
    }

    /**
     * service绑定回调
     */
    private ServiceConnection mFhrSCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothService = ((BluetoothLeService.BluetoothBinder) service).getService();
            mBluetoothService.addCallBack(serviceCallBack);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService = null;
        }
    };

    /**
     * 连接回调
     */
    private BleServiceCallBack serviceCallBack = new BleServiceCallBack() {

        @Override
        public void onLeScan(BleDevice device) {

        }

        @Override
        public void onScanComplete(BleDevice[] results) {
        }

        @Override
        public void onChanged(BluetoothGattCharacteristic characteristic) {

        }

        @Override
        public void onHRMNotify(BluetoothGattCharacteristic characteristic) {
            byte[] value = characteristic.getValue();
            BleLog.e("==", HexUtil.bytesToHexString(value));
            tvHeart.setText("心率: " + (value[1] & 0xff));
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothService != null) {
            bleManager.unBindService(this, mFhrSCon);
            mBluetoothService.removeCallBack(serviceCallBack);
        }
    }
}
