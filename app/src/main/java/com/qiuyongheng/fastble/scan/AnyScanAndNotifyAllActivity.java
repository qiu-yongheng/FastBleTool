package com.qiuyongheng.fastble.scan;


import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qiuyongheng.fastble.R;
import com.qyh.fastble.ble.BleManager;
import com.qyh.fastble.ble.conn.BleCharacterCallback;
import com.qyh.fastble.ble.constant.UUIDConstant;
import com.qyh.fastble.ble.data.BleDevice;
import com.qyh.fastble.ble.exception.BleException;
import com.qyh.fastble.ble.service.BluetoothService;
import com.qyh.fastble.ble.utils.BleLog;
import com.qyh.fastble.ble.utils.HexUtil;

import java.util.ArrayList;
import java.util.List;

public class AnyScanAndNotifyAllActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_start, btn_stop;
    private ImageView img_loading;
    private Animation operatingAnim;
    private ResultAdapter mResultAdapter;
    private ProgressDialog progressDialog;

    private BluetoothService mBluetoothService;
    private BleManager bleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_any_scan);
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothService != null)
            unbindService();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("搜索设备");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        bleManager = new BleManager(this);

        btn_start = (Button) findViewById(R.id.btn_start);
        btn_start.setOnClickListener(this);
        btn_stop = (Button) findViewById(R.id.btn_stop);
        btn_stop.setOnClickListener(this);
        img_loading = (ImageView) findViewById(R.id.img_loading);
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        operatingAnim.setInterpolator(new LinearInterpolator());
        progressDialog = new ProgressDialog(this);

        mResultAdapter = new ResultAdapter(this);
        ListView listView_device = (ListView) findViewById(R.id.list_device);
        listView_device.setAdapter(mResultAdapter);
        /** 点击事件 */
        listView_device.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mBluetoothService != null) {
                    mBluetoothService.cancelScan();
                    mBluetoothService.connectDevice(mResultAdapter.getItem(position));
                    mResultAdapter.clear();
                    mResultAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                checkPermissions();
                break;

            case R.id.btn_stop:
                if (mBluetoothService != null) {
                    mBluetoothService.cancelScan();
                }
                break;
        }
    }

    private class ResultAdapter extends BaseAdapter {

        private Context context;
        private List<BleDevice> scanResultList;

        ResultAdapter(Context context) {
            this.context = context;
            scanResultList = new ArrayList<>();
        }

        void addResult(BleDevice result) {
            scanResultList.add(result);
        }

        void clear() {
            scanResultList.clear();
        }

        @Override
        public int getCount() {
            return scanResultList.size();
        }

        @Override
        public BleDevice getItem(int position) {
            if (position > scanResultList.size())
                return null;
            return scanResultList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ResultAdapter.ViewHolder holder;
            if (convertView != null) {
                holder = (ResultAdapter.ViewHolder) convertView.getTag();
            } else {
                convertView = View.inflate(context, R.layout.adapter_scan_result, null);
                holder = new ResultAdapter.ViewHolder();
                convertView.setTag(holder);
                holder.txt_name = (TextView) convertView.findViewById(R.id.txt_name);
                holder.txt_mac = (TextView) convertView.findViewById(R.id.txt_mac);
                holder.txt_rssi = (TextView) convertView.findViewById(R.id.txt_rssi);
            }

            BleDevice result = scanResultList.get(position);
            BluetoothDevice device = result.getDevice();
            String name = device.getName();
            String mac = device.getAddress();
            int rssi = result.getRssi();
            holder.txt_name.setText(name);
            holder.txt_mac.setText(mac);
            holder.txt_rssi.setText(String.valueOf(rssi));
            return convertView;
        }

        class ViewHolder {
            TextView txt_name;
            TextView txt_mac;
            TextView txt_rssi;
        }
    }

    private void bindService() {
        Intent bindIntent = new Intent(this, BluetoothService.class);
        this.bindService(bindIntent, mFhrSCon, Context.BIND_AUTO_CREATE);
    }

    private void unbindService() {
        this.unbindService(mFhrSCon);
    }

    /**
     * service绑定回调
     */
    private ServiceConnection mFhrSCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothService = ((BluetoothService.BluetoothBinder) service).getService();
            mBluetoothService.setScanCallback(callback);
            mBluetoothService.scanDevice();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService = null;
        }
    };

    private BluetoothService.Callback callback = new BluetoothService.Callback() {
        @Override
        public void onStartScan() {
            mResultAdapter.clear();
            mResultAdapter.notifyDataSetChanged();
            img_loading.startAnimation(operatingAnim);
            btn_start.setEnabled(false);
            btn_stop.setVisibility(View.VISIBLE);
        }

        @Override
        public void onScanning(BleDevice result) {
            mResultAdapter.addResult(result);
            mResultAdapter.notifyDataSetChanged();
        }

        @Override
        public void onScanComplete() {
            img_loading.clearAnimation();
            btn_start.setEnabled(true);
            btn_stop.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onConnecting() {
            progressDialog.show();
        }

        @Override
        public void onConnectFail(BleException exception) {
            img_loading.clearAnimation();
            btn_start.setEnabled(true);
            btn_stop.setVisibility(View.INVISIBLE);
            progressDialog.dismiss();
            Toast.makeText(AnyScanAndNotifyAllActivity.this, "连接失败", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onDisConnected() {
            progressDialog.dismiss();
            mResultAdapter.clear();
            mResultAdapter.notifyDataSetChanged();
            img_loading.clearAnimation();
            btn_start.setEnabled(true);
            btn_stop.setVisibility(View.INVISIBLE);
            Toast.makeText(AnyScanAndNotifyAllActivity.this, "连接断开", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            progressDialog.dismiss();

            /** 订阅心率 */
            mBluetoothService.notify(UUIDConstant.HRM_SERVICE.toString(), UUIDConstant.HRM_CHAR.toString(), new BleCharacterCallback() {
                @Override
                public void onSuccess(BluetoothGattCharacteristic characteristic) {
                    byte[] value = characteristic.getValue();
                    BleLog.e("==", HexUtil.bytesToHexString(value));
                }

                @Override
                public void onFailure(BleException exception) {

                }

                @Override
                public void onInitiatedResult(boolean result) {

                }
            });

        }
    };

    @Override
    public final void onRequestPermissionsResult(int requestCode,
                                                 @NonNull String[] permissions,
                                                 @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 12:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            onPermissionGranted(permissions[i]);
                        }
                    }
                }
                break;
        }
    }

    private void checkPermissions() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(this, deniedPermissions, 12);
        }
    }

    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (mBluetoothService == null) {
                    bindService();
                } else {
                    mBluetoothService.scanDevice();
                }
                break;
        }
    }

}
