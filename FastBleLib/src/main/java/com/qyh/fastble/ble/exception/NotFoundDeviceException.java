package com.qyh.fastble.ble.exception;


public class NotFoundDeviceException extends BleException {
    public NotFoundDeviceException() {
        super(ERROR_CODE_NOT_FOUND_DEVICE, "Not Found Device Exception Occurred!");
    }
}
