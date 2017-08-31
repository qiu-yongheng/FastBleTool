package com.example.liteble.exception.hanlder;

import android.content.Context;
import android.widget.Toast;

import com.example.liteble.exception.ConnectException;
import com.example.liteble.exception.GattException;
import com.example.liteble.exception.InitiatedException;
import com.example.liteble.exception.OtherException;
import com.example.liteble.exception.TimeoutException;

/**
 * Toast exception.
 *
 * @author MaTianyu @http://litesuits.com
 * @date 2015-11-21
 */
public class DefaultBleExceptionHandler extends BleExceptionHandler {
    private Context context;

    public DefaultBleExceptionHandler(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    protected void onConnectException(ConnectException e) {
        Toast.makeText(context, e.getDescription(), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onGattException(GattException e) {
        Toast.makeText(context, e.getDescription(), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onTimeoutException(TimeoutException e) {
        Toast.makeText(context, e.getDescription(), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onInitiatedException(InitiatedException e) {
        Toast.makeText(context, e.getDescription(), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onOtherException(OtherException e) {
        Toast.makeText(context, e.getDescription(), Toast.LENGTH_LONG).show();
    }
}
