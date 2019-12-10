package androidalldemo.jan.jason.bluedemo.core;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import androidalldemo.jan.jason.bluedemo.utils.LogUtils;
import androidalldemo.jan.jason.bluedemo.utils.Utils;

import static androidalldemo.jan.jason.bluedemo.utils.LogUtils.d;

/**
 * Description: 普通蓝牙 服务端
 * （就是某一端通过搜索的蓝牙列表中选择的连接对象，可以说服务端是被动的连接者，反之客户端是主动连接者）
 * *
 * Creator: Wang
 * Date: 2019/12/10 20:38
 */
public class NormalBlueServer extends NormalBaseBlue{

    private static final String TAG = "NormalBlueServer";

    /**
     * 服务端用到的socket 是单独的 服务端一般都是一直监听客户端的消息
     * 这个socket存在服务端中
     * 这个socket可以用来拦截一切客户端发送过来的消息
     * 这个socket可以用来发送给某些客户端一些消息
     */
    private BluetoothServerSocket mServerSocket;

    /**
     * 构造器传入一个监听器
     *
     * @param listener
     */
    public NormalBlueServer(Listener listener) {
        super(listener);

        //构造函数里面直接开始监听
        listenClient();
    }

    @Override
    public void close() {
        super.close();//关闭服务端拿到的客户端socket
        try {
            if (mServerSocket != null) {
                mServerSocket.close();//服务端监听的socket也关闭
            }
        } catch (Throwable e) {
            d("Error", "##" + e.getMessage());
        }
    }

    /**
     * 监听客户端发起的连接请求
     * 应该是要一直监听的
     * 服务端的socket有一个accept，就类似于一个阻塞作用，当监听到有客户端发来的连接请求后
     * 这里是关闭了监听，相当于这个服务端就只监听一个客户端，这个客户端一旦出现，服务端就关闭监听
     * 其他地方不见得是这样用的，这个就仅仅是1对1 型的服务端而已。
     */
    public void listenClient(){

        try {

            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

            //法1：安全加密传输
            //mServerSocket = adapter.listenUsingRfcommWithServiceRecord(TAG, SPP_UUID); //加密传输，Android强制执行配对，弹窗显示配对码

            //法2：明文传输
            mServerSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(TAG,SPP_UUID);//用专门的UUID

            Utils.EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        //拿到服务端接收到的客户端socket，这样可以根据这个socket拿到客户端想要发送过来的数据
                        BluetoothSocket socket = mServerSocket.accept();

                        //一旦accept走完，说明服务端已经监听到了客户端的请求连接
                        d("TEST##","服务端socket 发现有客户端请求，不再阻塞，开始向下执行逻辑...");
                        mServerSocket.close();//此时立马关闭服务端socket,不再监听其他客户端的请求了，这里自由处理，这里为了简单，就1对1，所以关闭了监听

                        loopRead(socket);//

                    } catch (Throwable e) {
                        d("Error", "##" + e.getMessage());
                        close();//accept方法以及loopRead方法中 有可能有异常，比如正在连接的双方，客户端突然关闭蓝牙，就应该走这里了
                    }
                }
            });

        } catch (Throwable e) {
            d("Error", "##" + e.getMessage());
        }
    }

}
