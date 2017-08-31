package com.example.liteble.exception;

/**
 * @author MaTianyu @http://litesuits.com
 * @date 2015-11-21
 */
public class OtherException extends BleException {
    public OtherException(String description) {
        super(ERROR_CODE_OTHER, description);
    }
}
