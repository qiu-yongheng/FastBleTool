package com.qiuyongheng.fastble.app;

import android.app.Application;

import com.elvishew.xlog.LogConfiguration;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;

/**
 * @author 邱永恒
 * @time 2017/8/29  15:50
 * @desc ${TODD}
 */

public class App extends Application{
    @Override
    public void onCreate() {
        super.onCreate();

        // XLog
        LogConfiguration config = new LogConfiguration.Builder()
                .tag("BLE_TAG") // TAG
                .t() // 运行打印线程信息
                .b() // 允许答应日志边框
                .st(3) // 允许打印深度为3的调用栈信息
                .build();
        XLog.init(LogLevel.ALL, config);
    }
}
