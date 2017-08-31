package com.example.liteble.exception;


public class NotFoundDeviceException extends BleException {
    public NotFoundDeviceException() {
        super(ERROR_CODE_NOT_FOUND_DEVICE, "Not Found Device Exception Occurred!");
    }
}
