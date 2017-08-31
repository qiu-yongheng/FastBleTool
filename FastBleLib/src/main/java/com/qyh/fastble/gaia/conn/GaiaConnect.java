package com.qyh.fastble.gaia.conn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;
import android.util.Log;

import com.qyh.fastble.gaia.bluetooth.GaiaBluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.content.ContentValues.TAG;

/**
 * @author 邱永恒
 * @time 2017/8/14  21:37
 * @desc socket 连接设备, 进行数据传输
 */


public class GaiaConnect {

    private final BluetoothAdapter bluetoothAdapter;
    private static GaiaConnect instance;
    private final GaiaBluetooth gaiaBluetooth;
    private final GaiaCallback callback;

    private ConnectionThread connectionThread = null;
    private CommunicationThread communicationThread = null;

    private GaiaConnect(GaiaBluetooth gaiaBluetooth, GaiaCallback callback) {
        this.callback = callback;
        this.gaiaBluetooth = gaiaBluetooth;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static GaiaConnect getInstance(GaiaBluetooth gaiaBluetooth, GaiaCallback callback) {
        if (instance == null) {
            synchronized (GaiaConnect.class) {
                if (instance == null) {
                    instance = new GaiaConnect(gaiaBluetooth, callback);
                }
            }

        }

        return instance;
    }

    public void startConnectionThread(BluetoothSocket socket) {
        if (connectionThread == null) {
            synchronized (GaiaConnect.class) {
                if (connectionThread == null) {
                    connectionThread = new ConnectionThread(socket);
                }
            }
        }
        connectionThread.start();
    }

    /**
     * <p>Thread to use in order to connect a BluetoothDevice using a BluetoothSocket.</p>
     * <p>The connection to a BluetoothDevice using a BluetoothSocket is synchronous but can take time. To avoid to
     * block the current Thread of the application - in general the UI Thread - the connection runs in its own
     * Thread.</p>
     * <p>
     * 连接线程
     */
    private class ConnectionThread extends Thread {

        /**
         * The Bluetooth socket to use to connect to the remote device.
         */
        private final BluetoothSocket bluetoothSocket;
        /**
         * <p>The tag to display for logs of this Thread.</p>
         */
        private final String THREAD_TAG = "ConnectionThread";

        /**
         * <p>To create a new instance of this class.</p>
         *
         * @param socket The necessary Bluetooth socket for this Thread to connect with a device.
         */
        private ConnectionThread(@NonNull BluetoothSocket socket) {
            setName(THREAD_TAG + getId());
            bluetoothSocket = socket;
        }

        @Override
        public void run() {
            try {
                // 取消搜索
                bluetoothAdapter.cancelDiscovery();

                // 连接设备
                // This call blocks until it succeeds or throws an exception.
                bluetoothSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    bluetoothSocket.close();
                } catch (IOException closeException) {
                    Log.w(THREAD_TAG, "Could not close the client socket", closeException);
                }
                // 改变状态, 处理异常, 关闭线程
                onConnectionFailed();
                connectionThread = null;
                return;
            }

            // connection succeeds
            onSocketConnected(bluetoothSocket);
        }

        /**
         * To cancel this thread if it was running.
         */
        private void cancel() {
            // stop the thread if still running
            // because the BluetoothSocket.connect() method is synchronous the only way to stop this Thread is to use
            // the interrupt() method even if it is not recommended.
            interrupt();
        }
    }

    /**
     * 连接成功, 开始数据传输
     *
     * @param socket
     */
    private void onSocketConnected(BluetoothSocket socket) {
        // Cancel the thread that completed the connection
        cancelConnectionThread();
        // Cancel any thread currently running a connection
        cancelCommunicationThread();

        // 设备连接成功, 可以进行数据传输
        gaiaBluetooth.setState(GaiaBluetooth.STATE_TRANSFER);

        communicationThread = new CommunicationThread(socket);
        communicationThread.start();
    }

    public void cancelCommunicationThread() {
        if (communicationThread != null) {
            communicationThread.cancel();
            communicationThread = null;
        }
    }

    public void cancelConnectionThread() {
        if (connectionThread != null) {
            connectionThread.cancel();
            connectionThread = null;
        }
    }

    private void onConnectionFailed() {
        gaiaBluetooth.setState(GaiaBluetooth.STATE_DISCONNECTED);
    }

    /**
     * <p>Thread to use in order to listen for incoming message from a connected BluetoothDevice.</p>
     * <p>To get messages from a remote device connected using a BluetoothSocket, an application has to constantly
     * read bytes over the InputStream of the BluetoothSocket. In order to avoid to block the current Thread of an
     * application - usually the UI Thread - it is recommended to do it in its own thread.</p>
     */
    private class CommunicationThread extends Thread {
        /**
         * The InputStream object to read bytes from in order to get messages from a connected remote device.
         */
        private final InputStream inputStream;
        /**
         * The OutputStream object to write bytes on in  order to send messages to a connected remote device.
         */
        private final OutputStream outputStream;
        /**
         * The BluetoothSocket which has successfully been connected to a BluetoothDevice.
         */
        private final BluetoothSocket socket;
        /**
         * To constantly read messages coming from the remote device.
         */
        private boolean mmIsRunning = false;
        /**
         * The tag to display for logs of this Thread.
         */
        private final String THREAD_TAG = "CommunicationThread";

        /**
         * <p>To create a new instance of this class.</p>
         * <p>This constructor will initialized all its private field depending on the given BluetoothSocket.</p>
         *
         * @param socket A BluetoothSocket which has successfully been connected to a BluetoothDevice.
         */
        CommunicationThread(@NonNull BluetoothSocket socket) {
            setName("CommunicationThread" + getId());
            this.socket = socket;

            // temporary object to get the Bluetooth socket input and output streams as they are final
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = this.socket.getInputStream();
                tmpOut = this.socket.getOutputStream();
            } catch (IOException e) {
                Log.e(THREAD_TAG, "Error occurred when getting input and output streams", e);
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
        }


        @Override // Thread
        public void run() {
            if (inputStream == null) {
                Log.w(THREAD_TAG, "Run thread failed: InputStream is null.");
                disconnect();
                return;
            }

            if (outputStream == null) {
                Log.w(THREAD_TAG, "Run thread failed: OutputStream is null.");
                disconnect();
                return;
            }

            if (socket == null) {
                Log.w(THREAD_TAG, "Run thread failed: BluetoothSocket is null.");
                disconnect();
                return;
            }

            if (!socket.isConnected()) {
                Log.w(THREAD_TAG, "Run thread failed: BluetoothSocket is not connected.");
                disconnect();
                return;
            }

            // all check passed successfully, the listening can start
            listenStream();
        }

        /**
         * <p>This method runs the constant read of the InputStream in order to get messages from the connected
         * remote device.</p>
         * <p>
         * 读取数据
         */
        private void listenStream() {
            final int MAX_BUFFER = 1024;
            byte[] buffer = new byte[MAX_BUFFER];

            mmIsRunning = true;

            // 通知子类可以与设备进行连接
            callback.onCommunicationRunning();

            while (gaiaBluetooth.isConnected() && mmIsRunning) {
                int length;
                try {
                    length = inputStream.read(buffer);
                } catch (IOException e) {
                    Log.e(THREAD_TAG, "Reception of data failed: exception occurred while reading: " + e.toString());
                    mmIsRunning = false;
                    if (gaiaBluetooth.isConnected()) {
                        onConnectionLost();
                    }
                    communicationThread = null;
                    break;
                }

                // if buffer contains some bytes, they are sent to the listener
                if (length > 0) {
                    byte[] data = new byte[length];
                    System.arraycopy(buffer, 0, data, 0, length);

                    // 回调从设备读取的数据
                    callback.onDataFound(data);
                }
            }
        }

        /**
         * <p>To write some data on the OutputStream in order to send it to a connected remote device.</p>
         * <p>
         * 发送数据
         *
         * @param data the data to send.
         * @return true, if the data had successfully been writing on the OutputStream.
         */
        /*package*/ boolean sendStream(byte[] data) {
            if (socket == null) {
                Log.w(THREAD_TAG, "Sending of data failed: BluetoothSocket is null.");
                return false;
            }

            if (!socket.isConnected()) {
                Log.w(THREAD_TAG, "Sending of data failed: BluetoothSocket is not connected.");
                return false;
            }

            if (!gaiaBluetooth.isConnected()) {
                Log.w(THREAD_TAG, "Sending of data failed: Provider is not connected.");
                return false;
            }

            if (outputStream == null) {
                Log.w(THREAD_TAG, "Sending of data failed: OutputStream is null.");
                return false;
            }

            try {
                outputStream.write(data);
                // flush the data to make sure the packet is sent immediately.
                // this is less efficient for the Android application, but helpful for ADK applications.
                // The sendStream() method can be called more than once before the write() method sends the packet
                // from the previous call. The sending is processed asynchronously for system efficiency. If the
                // write method is called more than once before the sending is done, all packets are buffered and
                // are sent using only one message.
                // ADK handles a packet per message sent faster than a message which contains more than one packet.
                outputStream.flush();

                callback.onDataSand(data);
            } catch (IOException e) {
                Log.w(THREAD_TAG, "Sending of data failed: Exception occurred while writing data: " + e.toString());
                return false;
            }

            return true;
        }

        /**
         * To cancel this thread if it was running.
         */
        /*package*/ void cancel() {
            mmIsRunning = false;

            try {
                socket.close();
            } catch (IOException e) {
                Log.w(THREAD_TAG, "Cancellation of the Thread: Close of BluetoothSocket failed: " + e.toString());
            }
        }

    }

    /**
     * 连接丢失
     */
    private void onConnectionLost() {
        gaiaBluetooth.setState(GaiaBluetooth.STATE_DISCONNECTED);
    }

    /**
     * 断开连接
     */
    private boolean disconnect() {
        if (gaiaBluetooth.isDisConnected()) {
            Log.w(TAG, "disconnection failed: no device connected.");
            return false;
        }

        // cancel any running thread
        cancelConnectionThread();
        cancelCommunicationThread();

        gaiaBluetooth.setState(GaiaBluetooth.STATE_DISCONNECTED);
        return true;
    }

    /**
     * 向设备发送数据
     *
     * @param data
     * @return
     */
    public boolean sendData(byte[] data) {
        CommunicationThread t;
        synchronized (GaiaConnect.class) {
            if (!gaiaBluetooth.isTransfer() || communicationThread == null) {
                return false;
            }

            t = communicationThread;
        }

        return t.sendStream(data);
    }
}
