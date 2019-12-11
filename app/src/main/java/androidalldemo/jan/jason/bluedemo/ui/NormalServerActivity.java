package androidalldemo.jan.jason.bluedemo.ui;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidalldemo.jan.jason.bluedemo.core.NormalBaseBlue;
import androidalldemo.jan.jason.bluedemo.core.NormalBlueServer;
import androidalldemo.jan.jason.bluedemo.utils.ToastUtils;
import androidx.databinding.DataBindingUtil;

import com.cxz.swipelibrary.SwipeBackActivity;

import java.io.File;

import androidalldemo.jan.jason.bluedemo.R;
import androidalldemo.jan.jason.bluedemo.databinding.ActivityNormalServerBinding;

/**
 * 普通蓝牙服务端
 */
public class NormalServerActivity extends SwipeBackActivity implements NormalBaseBlue.Listener
    , View.OnClickListener{

    private ActivityNormalServerBinding binding;
    private BluetoothAdapter mBluetoothAdapter;
    private NormalBlueServer mServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_normal_server);

        initView();
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //服务端，设定，这个Activity存活的时候，在初始化的时候开始监听，如果这个Activity死亡了，务必要关闭监听
        mServer.unListener();//先取消监听，这样不会回调到一个不存在的Activity了
        mServer.close();//先关闭正在通讯的socket，再关闭服务端监听的socket
        mServer = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ism_send_msg_btn:
                sendMessage();
                break;

            case R.id.ism_input_file_btn:
                sendFileData();
                break;

            case R.id.ans_open_blue_btn:
                if (!mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.enable();//务必先打开一下
                } else {
                    mBluetoothAdapter.disable();//反之关一下
                }
                break;

            case R.id.ans_enable_discovery_btn:
                enableDdiscovery(300);
                break;
        }
    }

    /**
     * socket连接状态变更，通知一下UI
     * @param state
     * @param obj
     */
    @Override
    public void socketNotify(int state, Object obj) {

        if (isDestroyed()) return ;

        String msg = null;
        switch (state) {
            case NormalBaseBlue.Listener.CONNECTED:
                BluetoothDevice dev = (BluetoothDevice) obj;
                msg = String.format("与%s\n(%s)\n连接成功", dev.getName(), dev.getAddress());
                binding.ansConnectStateTv.setText(msg);
                break;
            case NormalBaseBlue.Listener.DISCONNECTED:
                msg = "连接断开了，原因：\n 1.客户端蓝牙断开 \n2.服务端蓝牙断开 \n如果是客户端蓝牙断开需要重新监听";
                binding.ansConnectStateTv.setText(msg);
                if (mBluetoothAdapter.isEnabled()) {
                    mServer.listenClient();
                }
                break;
            case NormalBaseBlue.Listener.MSG:
                msg = String.format("\n%s", obj);
                binding.ansIncludeSend.ismLogTv.append(msg);
                break;
        }
    }

    /**
     * 初始化视图相关，包括监听设置
     */
    private void initView() {
        binding.ansIncludeSend.ismSendMsgBtn.setOnClickListener(this);
        binding.ansIncludeSend.ismInputFileBtn.setOnClickListener(this);

        binding.ansOpenBlueBtn.setOnClickListener(this);
        binding.ansEnableDiscoveryBtn.setOnClickListener(this);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        setTitle("普通蓝牙 服务端");
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        mServer = new NormalBlueServer(this);
        //这里立马会开启一个监听，类似于淘宝的客服一样，随时在线，随时解决用户需求
        //这个随时在线的实现就是利用一个ServerSocket来完成的，但是这个随时在线并不能实现互相发送消息
        //真正发送消息的是，另外一个互相通讯的socket，此时双方已经平等，人人平等，都可以互相发送消息和文件（是因为平等我们才共用一个NormalBaseBlue类）
        //如果要实现互相通讯，就应该找的目标对象，然后我们在手机上输入一些文字或图片，点击发送，前提一定是先找的目标对象
        //socket也一样，都是先getRemoteDevices，获取到远程设备，然后再互相发送消息

    }

    /**
     * 服务端使自己的蓝牙能够被检测
     * @param duration_second
     */
    private void enableDdiscovery(int duration_second) {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);

        // 第二个参数可设置的范围是0~3600秒，在此时间区间（窗口期）内可被发现
        // 任何不在此区间的值都将被自动设置成120秒。
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration_second);
        startActivity(discoverableIntent);
    }

    /**
     * 服务端需要发送的字符串信息
     * 然后调用服务端的发送消息逻辑，进行消息发送
     */
    private void sendMessage(){
        //服务端根本不关心客户端是不是原来那个，因为我们采用的是1对1，如果服务端已经监听到用客户端请求，只需要判断服务端是否处于连接就能发送数据了
        if (mServer.isConnected(null)) {
            String msg = binding.ansIncludeSend.ismInputEt.getText().toString();
            if (TextUtils.isEmpty(msg)) {
                ToastUtils.show("不能给客户发空消息哦~");
            } else {
                mServer.sendMessage(msg);
                //清空EditText中的消息，再键盘隐藏掉
                clearEditText(binding.ansIncludeSend.ismInputEt);
            }
        } else {
            ToastUtils.show("服务端(客服小姐姐)：根本没有人联系我呀，或者之前联系的用户断线了~ 但是我还是会保持监听状态的哦~");
            binding.ansConnectStateTv.setText("没有人联系我(此时依旧在监听)");
        }
    }

    /**
     * 服务端需要发送给客户端 文件信息
     * 然后调用服务端对象的发送文件逻辑，进行文件发送
     */
    private void sendFileData(){
        if (mServer.isConnected(null)) {
            String filePath = binding.ansIncludeSend.ismInputFileEt.getText().toString();
            if (TextUtils.isEmpty(filePath) || !new File(filePath).isFile()) {
                ToastUtils.show("抱歉~ 选择文件无效哦~");
            } else {
                mServer.sendFileData(filePath);
                //清空EditText中的消息，再键盘隐藏掉
                clearEditText(binding.ansIncludeSend.ismInputFileEt);
            }
        } else {
            ToastUtils.show("服务端(客服小姐姐)：根本没有人联系我呀，或者之前联系的用户断线了~ 但是我还是会保持监听状态的哦~");
            binding.ansConnectStateTv.setText("没有人联系我(此时依旧在监听)");
        }
    }

    /**
     * 清空Edit中的文字
     */
    private void clearEditText(EditText editText){
        if (editText != null) {
            editText.setText("");
        }

        //隐藏键盘
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager)
                    getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}
