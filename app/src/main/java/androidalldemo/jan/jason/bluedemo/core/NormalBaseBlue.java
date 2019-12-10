package androidalldemo.jan.jason.bluedemo.core;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Environment;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.UUID;

import androidalldemo.jan.jason.bluedemo.App;
import androidalldemo.jan.jason.bluedemo.utils.LogUtils;
import androidalldemo.jan.jason.bluedemo.utils.ToastUtils;
import androidalldemo.jan.jason.bluedemo.utils.Utils;

/**
 * desc: 控制普通蓝牙的 核心基础类
 * *
 * user: JasonJan 1211241203@qq.com
 * time: 2019/12/10 13:45
 **/
public class NormalBaseBlue {

    /**
     * 普通蓝牙 串口通信 需要的UUID 其它的不行哦
     */
    public static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    /**
     * 文件路径 API 29 注意更换一下
     */
    private static final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/bluetooth/";
    /**
     * 通信时 发送的是消息字符串类型
     */
    private static final int FLAG_MSG = 0;  //消息标记
    /**
     * 通信时 发送的是文件类型
     */
    private static final int FLAG_FILE = 1; //文件标记
    /**
     * 蓝牙通信的Socket
     */
    private BluetoothSocket mSocket;
    /**
     * 输出流
     */
    private DataOutputStream mOut;
    /**
     * 内部监听，主要用于回调相关东西
     */
    private Listener mListener;
    /**
     * 是否正在读取
     */
    private boolean isRead;
    /**
     * 是否正在发送
     */
    private boolean isSending;

    /**
     * 内部监听器，回调通知状态变更
     */
    public interface Listener {
        int DISCONNECTED = 0;
        int CONNECTED = 1;
        int MSG = 2;

        void socketNotify(int state, Object obj);
    }

    /**
     * 构造器传入一个监听器
     * @param listener
     */
    NormalBaseBlue(Listener listener) {
        mListener = listener;
    }

     /**
     * 死循环读取socket 里面的数据
     * @param socket 目标socket 如果当前应用为客户端，这里就应该是服务端的socket
     *               如果当前应用为服务端，这里就应该是客户端的socket
     */
    public void loopRead(BluetoothSocket socket){

        mSocket = socket;
        if (socket == null) return ;

        try{

            if (!mSocket.isConnected()) {
                mSocket.connect(); //确保当前socket处于连接状态，这里很有可能抛出异常
            }

            //如果没有发生异常，说明连接成功，继续往下走了,这里通知UI ，连接成功后，这里可以通过socket获取到远程设备的信息
            notifyUI(Listener.CONNECTED, mSocket.getRemoteDevice());

            mOut = new DataOutputStream(mSocket.getOutputStream());//将远程socket的输出流包装进本类的out流中
            DataInputStream remoteInputStream = new DataInputStream(mSocket.getInputStream());//包装远程socket的输入流

            isRead = true;//开始读取信息,可能是文件，也可能是字符串
            while (isRead) {

                LogUtils.d("TEST##"," while 内部...");
                switch (remoteInputStream.readInt()) {

                    case FLAG_MSG: //读取短消息
                        String msg = remoteInputStream.readUTF();
                        notifyUI(Listener.MSG, "接收短消息：" + msg);
                        break;

                    case FLAG_FILE: //读取文件
                        Utils.mkdirs(FILE_PATH);//先创建一个目录，准备写入东西了
                        String fileName = remoteInputStream.readUTF(); //文件名
                        long fileLen = remoteInputStream.readLong(); //文件长度

                        // 读取文件内容
                        long len = 0;
                        int r;
                        byte[] b = new byte[4 * 1024];//缓冲区

                        //状态变更，通知UI
                        notifyUI(Listener.MSG, "正在接收文件(" + fileName + "),请稍后...");

                        //文件流
                        FileOutputStream out = new FileOutputStream(FILE_PATH + fileName);
                        while ((r = remoteInputStream.read(b)) != -1) {//循环读取文件
                            out.write(b, 0, r);
                            len += r;
                            if (len >= fileLen) {
                                break;
                            }
                        }

                        //文件接收完成，通知UI
                        notifyUI(Listener.MSG, "文件接收完成(存放在:" + FILE_PATH + ")");

                        break;
                }
            }


        }catch (Throwable e){
            LogUtils.d("","Error##"+e.getMessage());
            close();
        }
    }

    /**
     * 通过远程socket的 输出流，给远程设备发送相关数据
     */
    public void sendMessage(String msg){
        if (checkSend()) return; //如果当前正在发送，则这次就不发送了
        if (mOut == null) {
            LogUtils.d("","##"+"sorry 远程socket的 输出流为空，无法传递数据了~");
            close();
            return ;
        }

        isSending = true;
        try {
            mOut.writeInt(FLAG_MSG); //消息标记
            mOut.writeUTF(msg);
            mOut.flush();
            notifyUI(Listener.MSG, "发送短消息：" + msg);
        } catch (Throwable e) {
            LogUtils.d("","##"+"sorry 远程socket的 输出流出现异常，无法传递数据了~");
            close();
        }

        isSending = false;
    }

    /**
     * 通过 远程socket的输出流 ，给远程设备发送 文件数据
     * filePath 是本地文件路径 当然要包装到一个文件流 再发送才行的
     */
    public void sendFileData(final String filePath){
        if (checkSend()) return;
        isSending = true;

        //文件比较耗时，最好异步发送
        Utils.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    //通过一个路径来 解析文件
                    FileInputStream in = new FileInputStream(filePath);
                    File file = new File(filePath);
                    mOut.writeInt(FLAG_FILE); //先发送文件标记
                    mOut.writeUTF(file.getName()); //再发送文件名
                    mOut.writeLong(file.length()); //再发送文件长度

                    notifyUI(Listener.MSG, "正在发送文件(" + filePath + "),请稍后...");
                    //最后发送文件
                    int r;
                    byte[] b = new byte[4 * 1024];
                    while ((r = in.read(b)) != -1) {
                        mOut.write(b, 0, r);
                    }
                    mOut.flush();
                    notifyUI(Listener.MSG, "文件发送完成.");

                } catch (Throwable e){
                    LogUtils.d("","Error##"+e.getMessage());
                    close();//发送过程中，出现连接断开，或者其他原因，这里就关闭socket
                } finally {
                    isSending = false;
                }
            }
        });

    }

    /**
     * 当前设备与指定设备是否连接
     * @param dev 指定设备
     * @return
     */
    public boolean isConnected(BluetoothDevice dev) {
        boolean connected = (mSocket != null && mSocket.isConnected());
        if (dev == null) {
            return connected;  //没有指定设备的话，就判断当前socket是否连接吧
        }
        return connected && mSocket.getRemoteDevice().equals(dev);
    }

    /**
     * 释放监听引用(例如释放对Activity引用，避免内存泄漏)
     */
    public void unListener() {
        mListener = null;
    }

    /**
     * 关闭Socket连接
     */
    public void close() {
        try {
            isRead = false;
            mSocket.close();
            notifyUI(Listener.DISCONNECTED, null);//通知UI , 连接异常，关闭socket连接
        } catch (Throwable e) {
            LogUtils.d("","Error##"+e.getMessage());
        }
    }

    /**
     * 通知UI 进行一些事物变更
     * 线程切换到主线程
     * 进行一些ui操作
     * @param state
     * @param obj
     */
    private void notifyUI(final int state, final Object obj) {
        App.runUi(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mListener != null) {
                        mListener.socketNotify(state, obj); //直接回调到目标类中
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 务必要在主线程中执行
     * @return
     */
    private boolean checkSend() {
        if (isSending) {
            ToastUtils.show("正在发送其它数据,请稍后再发...");
            return true;
        }
        return false;
    }
}
