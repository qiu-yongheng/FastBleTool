package com.qyh.fastble.gaia.conn;

/**
 * @author 邱永恒
 * @time 2017/8/14  22:13
 * @desc ${TODD}
 */


public interface GaiaCallback {
    // 正在传输数据
    void onCommunicationRunning();

    // 读取数据
    void onDataFound(byte[] data);

    // 发送数据
    void onDataSand(byte[] data);
}
