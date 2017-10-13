package com.qyh.fastble.ble.constant;

import java.util.UUID;

/**
 * @author 邱永恒
 * @time 2017/10/13  14:01
 * @desc ${TODD}
 */

public class UUIDConstant {
    /** 通用特征码 */
    public static final UUID DES = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    /** 标准HRM心率传输协议 */
    public static final UUID HRM_SERVICE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
    public static final UUID HRM_CHAR = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
}
