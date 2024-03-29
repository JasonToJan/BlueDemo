package androidalldemo.jan.jason.bluedemo.core;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import androidalldemo.jan.jason.bluedemo.utils.LogUtils;
import androidalldemo.jan.jason.bluedemo.utils.Utils;

/**
 * desc: 普通蓝牙 的客户端
 * *
 * user: JasonJan 1211241203@qq.com
 * time: 2019/12/10 14:33
 **/
public class NormalBlueClient extends NormalBaseBlue{

    Listener listener;

    /**
     * 构造器传入一个监听器
     *
     * @param listener
     */
    public NormalBlueClient(Listener listener) {
        super(listener);
        this.listener = listener;
    }

    /**
     * 连接到 服务端
     * 目标服务端为： dev
     */
    public void connectToServer(BluetoothDevice dev){

        close();//确保之前的连接先断开

        try{
            //加密传输，Android系统强制配对，弹窗显示配对码   注意：dev是客户端自己查找，然后系统广播发过来的一个序列号的一个类对象为dev
            //然后，我们就可以根据广播发过来的这个类来创建 一个蓝牙socket，类似本地 对应 一个水管，这个水管直接通向了目标服务端 的一个水管头。
            final BluetoothSocket socket = dev.createRfcommSocketToServiceRecord(SPP_UUID);
            //明文传输(不安全)，无需配对
            //final BluetoothSocket socket = dev.createInsecureRfcommSocketToServiceRecord(SPP_UUID);

            Utils.EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    loopRead(socket); //循环读取 服务端设备的socket
                }
            });

        }catch (Throwable e){
           LogUtils.d("","Error##"+e.getMessage());
           close();//有可能获取socket出现异常了
        }
    }
}
