package com.qyh.fastble.ble.exception.hanlder;


import android.content.Context;
import android.widget.Toast;

import com.qyh.fastble.ble.exception.BlueToothNotEnableException;
import com.qyh.fastble.ble.exception.ConnectException;
import com.qyh.fastble.ble.exception.GattException;
import com.qyh.fastble.ble.exception.NotFoundDeviceException;
import com.qyh.fastble.ble.exception.OtherException;
import com.qyh.fastble.ble.exception.ScanFailedException;
import com.qyh.fastble.ble.exception.TimeoutException;
import com.qyh.fastble.ble.utils.BleLog;

public class DefaultBleExceptionHandler extends BleExceptionHandler {

    private Context context;

    public DefaultBleExceptionHandler(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    protected void onConnectException(ConnectException e) {
        Toast.makeText(context, e.getDescription(), Toast.LENGTH_LONG).show();
        BleLog.i(e.getDescription());
    }

    @Override
    protected void onGattException(GattException e) {
        Toast.makeText(context, e.getDescription(), Toast.LENGTH_LONG).show();
        BleLog.i(e.getDescription());
    }

    @Override
    protected void onTimeoutException(TimeoutException e) {
        Toast.makeText(context, e.getDescription(), Toast.LENGTH_LONG).show();
        BleLog.i(e.getDescription());
    }

    @Override
    protected void onNotFoundDeviceException(NotFoundDeviceException e) {
        Toast.makeText(context, e.getDescription(), Toast.LENGTH_LONG).show();
        BleLog.i(e.getDescription());
    }

    @Override
    protected void onBlueToothNotEnableException(BlueToothNotEnableException e) {
        Toast.makeText(context, e.getDescription(), Toast.LENGTH_LONG).show();
        BleLog.i(e.getDescription());
    }

    @Override
    protected void onScanFailedException(ScanFailedException e) {
        Toast.makeText(context, e.getDescription(), Toast.LENGTH_LONG).show();
        BleLog.i(e.getDescription());
    }

    @Override
    protected void onOtherException(OtherException e) {
        Toast.makeText(context, e.getDescription(), Toast.LENGTH_LONG).show();
        BleLog.i(e.getDescription());
    }
}
