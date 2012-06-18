
package com.intel.crashreport.logconfig;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.SystemProperties;
import android.util.Log;

import com.intel.crashreport.logconfig.bean.FSLogSetting;
import com.intel.crashreport.logconfig.bean.PropertyLogSetting;

public class LogConfigClient {

    private static final int MAX_RETRY = 10;
    private static final int MAX_CONN_TIMEOUT = 100;
    private static final String SOCKET_ADDRESS_NAME = "logconfig";
    private static final String TAG = "LogConfig";

    private static LogConfigClient M_INSTANCE = null;
    private static LocalSocket mSocket = null;
    private static BufferedReader mInputStream = null;
    private static DataOutputStream mOutputStream = null;

    private LogConfigClient() {
    }

    public static synchronized LogConfigClient getInstance() throws Exception {
        if (M_INSTANCE == null) {
            M_INSTANCE = new LogConfigClient();
            if (!init()) {
                M_INSTANCE = null;
                throw new Exception("Init logconfig service failed");
            }
        }
        return M_INSTANCE;
    }

    private static boolean init() {
        // Start service which will open the server socket
        SystemProperties.set("ctl.start", "logconfig");
        mSocket = new LocalSocket();
        LocalSocketAddress mSocketAddress = new LocalSocketAddress(SOCKET_ADDRESS_NAME,
                LocalSocketAddress.Namespace.RESERVED);
        long finishTimeMs;
        int remainingTimeoutMs;
        int i;
        retry: for (i = 0; i < MAX_RETRY; i++) {
            finishTimeMs = System.currentTimeMillis() + MAX_CONN_TIMEOUT;
            try {
                mSocket.connect(mSocketAddress);
                break;
            } catch (IOException e) {
                do {
                    remainingTimeoutMs = (int) (finishTimeMs - System.currentTimeMillis());
                    if (remainingTimeoutMs <= 0) {
                        Log.d(TAG, "Connection failed, retry ...");
                        continue retry;
                    }
                } while (!mSocket.isConnected());
                break;
            }
        }
        if (i == MAX_RETRY) {
            Log.e(TAG, "Service connection almost failed !!!");
            return false;
        }
        try {
            mInputStream = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            mOutputStream = new DataOutputStream(mSocket.getOutputStream());
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Service connection almost failed !!!", e);
        }
        return false;
    }

    public void close() {
        try {
            if (mOutputStream != null) {
                mOutputStream.flush();
                mOutputStream.close();
            }
            if (mInputStream != null)
                mInputStream.close();
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            M_INSTANCE = null;
        }
    }

    public void writeCommand(byte cmd, Object obj) {
        switch (cmd) {
            case CommandLogConfigAdapter.CMD_WRITE_FILE:
                try {
                    CommandLogConfigAdapter.writeFile(mOutputStream, (FSLogSetting) obj);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case CommandLogConfigAdapter.CMD_SET_PROP:
                try {
                    CommandLogConfigAdapter.setProperty(mOutputStream, (PropertyLogSetting) obj);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public boolean writeData(byte[] b) {
        if (mOutputStream != null) {
            try {
                mOutputStream.write(b, 0, b.length);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean finishData() {
        if (mOutputStream != null) {
            try {
                mOutputStream.write('\0');
                mOutputStream.flush();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static class CommandLogConfigAdapter {

        static final byte CMD_WRITE_FILE = 0;
        static final byte CMD_SET_PROP = 1;

        /**
         * Write file command's method <br>
         * <code>
         * Data schema :
         * # of bytes : description
         * 1 : command (CMD_WRITE_FILE)
         * 1 : append (boolean)
         * 4 : path length (int)
         * n : path
         * 4 : value length (int)
         * n : value
         * 1 : end of command char (\0)
         * </code>
         *
         * @param stream the stream where cmd is written
         * @param s the config to apply
         * @throws IOException when an error occurs during write
         */
        public static void writeFile(DataOutputStream stream, FSLogSetting s) throws IOException {
            byte bPath[] = s.path.getBytes();
            byte bValue[] = s.valueToApply.getBytes();
            stream.writeByte(CMD_WRITE_FILE);
            stream.writeBoolean(s.append);
            stream.writeInt(bPath.length);
            stream.write(bPath);
            stream.writeInt(bValue.length);
            stream.write(bValue);
            stream.write('\0');
        }

        /**
         * set property command's method <br>
         * <code>
         * Data schema :
         * # of bytes : description
         * 1 : command (CMD_SET_PROP)
         * 4 : prop length (int)
         * n : prop
         * 4 : value length (int)
         * n : value
         * 1 : end of command char (\0)
         * </code>
         *
         * @param stream the stream where cmd is written
         * @param s the config to apply
         * @throws IOException when an error occurs during write
         */
        public static void setProperty(DataOutputStream stream, PropertyLogSetting s)
                throws IOException {
            byte bProp[] = s.name.getBytes();
            byte bValue[] = s.valueToApply.getBytes();
            stream.writeByte(CMD_SET_PROP);
            stream.writeInt(bProp.length);
            stream.write(bProp);
            stream.writeInt(bValue.length);
            stream.write(bValue);
            stream.write('\0');
        }

    }
}
