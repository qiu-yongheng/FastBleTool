package com.qiuyongheng.fastble.app;

import android.app.Application;

import com.qyh.fastble.ble.BleManager;

/**
 * @author 邱永恒
 * @time 2017/8/29  15:50
 * @desc ${TODD}
 */

public class App extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        BleManager.init(this);
    }
}
