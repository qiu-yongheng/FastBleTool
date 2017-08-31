package com.example.liteble.exception.hanlder;


import com.example.liteble.exception.BleException;
import com.example.liteble.exception.ConnectException;
import com.example.liteble.exception.GattException;
import com.example.liteble.exception.InitiatedException;
import com.example.liteble.exception.OtherException;
import com.example.liteble.exception.TimeoutException;

/**
 * 处理异常
 *
 * @author MaTianyu @http://litesuits.com
 * @date 2015-11-21
 */
public abstract class BleExceptionHandler {

    public BleExceptionHandler handleException(BleException exception) {
        if (exception != null) {
            if (exception instanceof ConnectException) {
                onConnectException((ConnectException) exception);
            } else if (exception instanceof GattException) {
                onGattException((GattException) exception);
            } else if (exception instanceof TimeoutException) {
                onTimeoutException((TimeoutException) exception);
            } else if (exception instanceof InitiatedException) {
                onInitiatedException((InitiatedException) exception);
            } else {
                onOtherException((OtherException) exception);
            }
        }
        return this;
    }

    /**
     * connect failed
     */
    protected abstract void onConnectException(ConnectException e);

    /**
     * gatt error status
     */
    protected abstract void onGattException(GattException e);

    /**
     * operation timeout
     */
    protected abstract void onTimeoutException(TimeoutException e);

    /**
     * operation inititiated error
     */
    protected abstract void onInitiatedException(InitiatedException e);

    /**
     * other exceptions
     */
    protected abstract void onOtherException(OtherException e);
}
