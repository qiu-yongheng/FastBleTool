/*
 * Copyright (C) 2013 litesuits.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.example.liteble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;

import com.example.liteble.exception.BleException;

/**
 * LiteBleGattCallback is an abstract extension of BluetoothGattCallback.
 * 蓝牙连接的回调
 */
public abstract class LiteBleGattCallback extends BluetoothGattCallback {

    /**
     * 连接成功, 可以进行服务搜索操作
     * @param gatt
     * @param status
     */
    public abstract void onConnectSuccess(BluetoothGatt gatt, int status);

    /**
     * 服务被发现
     * @param gatt
     * @param status
     */
    @Override
    public abstract void onServicesDiscovered(BluetoothGatt gatt, int status);

    /**
     * 连接失败
     * @param exception
     */
    public abstract void onConnectFailure(BleException exception);
}