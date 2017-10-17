# FastBle
Android Bluetooth Low Energy 蓝牙快速开发框架。

使用简单的方式进行搜索、连接、读写、通知的订阅与取消等一系列蓝牙操作，并实时地得到操作反馈。

## 一. 申请权限
**扫描BLE设备需要动态获取以下权限**
```
Manifest.permission.ACCESS_FINE_LOCATION
```

## 二. 初始化操作
#### 1. 初始化BleManager
```Java
BleManager bleManager = new BleManager(this);
```

#### 2. 启动并绑定service
**第一次执行需要启动并绑定service**
```Java
bleManager.startAndBindService(this, mFhrSCon);
```
**如果service已经启动过了, 且界面需要获取数据, 只需绑定service**
```Java
bleManager.bindService(this, mFhrSCon);
```

#### 3. 绑定service的回调操作
* 获取service对象
* 添加回调, 与BLE的交互结果都会通过回调返回
```Java
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
```

#### 4. 回调数据
```Java
/**
 * 连接回调
 */
private BleServiceCallBack serviceCallBack = new BleServiceCallBack() {

    @Override
    public void onStartScan() {
        // 开始扫描
    }

    @Override
    public void onScanError() {
        // 扫描报错
    }

    @Override
    public void onLeScan(BleDevice device) {
        // 扫描到设备时回调
    }

    @Override
    public void onScanComplete(BleDevice[] results) {
        // 扫描结束
    }

    @Override
    public void onConnecting() {
        // 正在链接
    }

    @Override
    public void onConnectFail(BleException exception) {
        Toast.makeText(TextActivity.this, "连接失败", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectSuccess() {
        // 连接成功
    }

    @Override
    public void onDisConnected(final BluetoothDevice device) {
        Toast.makeText(TextActivity.this, "连接断开", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onChanged(BluetoothGattCharacteristic characteristic) {
        // 接收设备刷新返回的数据
    }

    @Override
    public void onHRMNotify(BluetoothGattCharacteristic characteristic) {
        // 接收心率设备返回数据
        byte[] value = characteristic.getValue();
        tvHeart.setText(HexUtil.bytesToHexString(value));
        //BleLog.e("==", HexUtil.bytesToHexString(value));
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        // 服务被发现, 可以与设备进行交互
        // 获取心率数据
        mBluetoothService.notify(UUIDConstant.HRM_SERVICE.toString(), UUIDConstant.HRM_CHAR.toString());
    }
};
```

## 三. BLE操作
#### 1. 扫描BLE设备
```Java
mBluetoothService.scanDevice();
```
#### 2. 取消扫描
```Java
mBluetoothService.cancelScan();
```

#### 3. 连接设备
**根据设备进行连接, 可选断开后是否自动连接**
```Java
/**
 * 连接设备
 *
 * @param scanResult    设备
 * @param isAutoConnect 连接断开后, 是否自动连接
 * @param delay         重连间隔
 */
public void connectDevice(BleDevice scanResult, booleanisAutoConnect, long delay)
```
**根据设备MAC地址进行连接, 可选断开后是否自动连接**
```Java
/**
 * 连接设备
 *
 * @param address       设备MAC地址
 * @param isAutoConnect 连接断开后, 是否自动连接
 * @param delay         重连间隔
 */
public void connectDevice(String address, booleanisAutoConnect, long delay)
```
#### 4. 断开设备连接
```Java
mBluetoothService.closeConnect();
```
## 四. 释放资源
#### 1. 退出Activity
```Java
@Override
protected void onDestroy() {
    super.onDestroy();
    if (mBluetoothService != null) {
        // 取消绑定
        bleManager.unBindService(this, mFhrSCon);
        // 移除回调
        mBluetoothService.removeCallBack(serviceCallBack);
    }
}
```
#### 2. 退出程序
```Java
@Override
protected void onDestroy() {
    super.onDestroy();
    if (mBluetoothService != null) {
        // 取消绑定
        bleManager.unBindService(this, mFhrSCon);
        // 停止service
        bleManager.stopService(this);
        // 断开设备连接
        mBluetoothService.closeConnect();
        // 移除回调
        mBluetoothService.removeCallBack(serviceCallBack);
    }
}
```

## 五. 使用示例
```Java
public class TextActivity extends AppCompatActivity {

    private TextView tvHeart;
    private BleManager bleManager;
    private BluetoothLeService mBluetoothService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        tvHeart = (TextView) findViewById(R.id.tv_heart);


        bleManager = new BleManager(this);
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
            tvHeart.setText(HexUtil.bytesToHexString(value));
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            mBluetoothService.notify(UUIDConstant.HRM_SERVICE.toString(), UUIDConstant.HRM_CHAR.toString());
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

```



## License

	   Copyright 2016 chenlijian

	   Licensed under the Apache License, Version 2.0 (the "License");
	   you may not use this file except in compliance with the License.
	   You may obtain a copy of the License at

   		   http://www.apache.org/licenses/LICENSE-2.0

	   Unless required by applicable law or agreed to in writing, software
	   distributed under the License is distributed on an "AS IS" BASIS,
	   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	   See the License for the specific language governing permissions and
	   limitations under the License.




