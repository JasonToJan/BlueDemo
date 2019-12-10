package androidalldemo.jan.jason.bluedemo.ui;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.cxz.swipelibrary.SwipeBackActivity;

import androidalldemo.jan.jason.bluedemo.R;
import androidalldemo.jan.jason.bluedemo.adapter.BlueConnectAdapter;
import androidalldemo.jan.jason.bluedemo.bean.BlueBean;
import androidalldemo.jan.jason.bluedemo.core.NormalBaseBlue;
import androidalldemo.jan.jason.bluedemo.core.NormalBlueClient;
import androidalldemo.jan.jason.bluedemo.databinding.ActivityNormalClientBinding;
import androidalldemo.jan.jason.bluedemo.utils.LogUtils;
import androidalldemo.jan.jason.bluedemo.utils.ToastUtils;

/**
 * 普通 蓝牙 客户端
 */
public class NormalClientActivity extends SwipeBackActivity implements
        View.OnClickListener,
        NormalBaseBlue.Listener
{

    private ActivityNormalClientBinding binding;
    private BluetoothAdapter mBluetoothAdapter;
    private BlueConnectAdapter mAdapter;
    private NormalBlueClient mClient;//自己创建的一个普通蓝牙客户端对象，处理数据连接和传递

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_normal_client);

        initView();
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
             unregisterReceiver(receiver);
        }catch (Throwable e){
           LogUtils.d("","Error##"+e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10) { //得到权限后
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mBluetoothAdapter != null) {
                    LogUtils.d("TEST##","开始查找");
                    mBluetoothAdapter.startDiscovery();
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.anc_client_scan :
                scanBluetooth();
                break;
        }
    }

    /**
     * socket 内部细节变更 通知UI 提示
     * @param state
     * @param obj
     */
    @Override
    public void socketNotify(int state, Object obj) {
        if (isDestroyed())
            return;
        String msg = null;
        switch (state) {
            case NormalBaseBlue.Listener.CONNECTED:
                BluetoothDevice dev = (BluetoothDevice) obj;
                msg = String.format("与%s(%s)连接成功", dev.getName(), dev.getAddress());
                binding.ancConnectStateTv.setText(msg);
                break;
            case NormalBaseBlue.Listener.DISCONNECTED:
                msg = "连接断开";
                binding.ancConnectStateTv.setText(msg);
                break;
            case NormalBaseBlue.Listener.MSG:
                msg = String.format("\n%s", obj);
                binding.ancInclude.ismLogTv.append(msg);
                break;
        }
    }

    /**
     * 初始化视图相关
     * 设置监听等
     */
    private void initView(){
        binding.ancClientScan.setOnClickListener(this);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        setTitle("普通蓝牙 客户端");

        //第一次扫描，可以开或者注释
        scanBluetooth();

        initRecyclerView();

        initReceiver();

        mClient = new NormalBlueClient(this);
    }

    /**
     * 注册蓝牙相关的广播
     */
    private void initReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);// 找到设备的广播
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//开始扫描
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);// 搜索完成的广播
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);//已经连接
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);//蓝牙断开
        registerReceiver(receiver, filter);
    }

    /**
     * 初始化列表
     */
    private void initRecyclerView() {
        mAdapter = new BlueConnectAdapter();

        binding.ancClientBluelistRv.setLayoutManager(new LinearLayoutManager(this));
        binding.ancClientBluelistRv.setAdapter(mAdapter);

        mAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        mAdapter.isFirstOnly(false);

        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (mAdapter == null || position < 0 || position >= mAdapter.getData().size()) return ;

                if (mClient.isConnected(mAdapter.getData().get(position).getDevice())) {
                    ToastUtils.show("亲，已经连接了，没有必要再次连接哦~");
                    return;
                }
                mClient.connectToServer(mAdapter.getData().get(position).getDevice());
                ToastUtils.show("正在连接...到...服务端");
                binding.ancInclude.ismLogTv.setText("正在连接...");
            }
        });
    }

    /**
     * 扫描周围蓝牙
     */
    private void scanBluetooth(){
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        if (mBluetoothAdapter == null) {
            ToastUtils.show("本地蓝牙无法使用哦~");
            finish();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();//务必先打开一下
        }

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        getPermissionThenStartDiacovery();
    }

    /**
     * 获取权限，然后执行搜索
     */
    private void getPermissionThenStartDiacovery() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(NormalClientActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
            } else {
                LogUtils.d("开始扫描");
                mBluetoothAdapter.startDiscovery();
            }
        } else {
            mBluetoothAdapter.startDiscovery();
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 收到的广播类型
            String action = intent.getAction();

            if (action == null) return;

            LogUtils.d("TEST##","receive action = "+ action);

            switch (action) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    LogUtils.d("TEST##","begin to discovery");
                    if (mAdapter != null && mAdapter.getData() != null) {
                        mAdapter.getData().clear();
                    }
                    break;

                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    BlueBean bean = new BlueBean(device.getName(), device.getAddress(), device.getBondState(), device);
                    if (mAdapter != null) {
                        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                            mAdapter.addData(bean);//适配器
                        } else {
                            mAdapter.addData(0,bean);//适配器
                        }
                    }
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    ToastUtils.show("已完成这次蓝牙扫描任务！");
                    break;

                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    ToastUtils.show("蓝牙已经连接了 ACTION_ACL_CONNECTED");
                    break;

                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    ToastUtils.show("蓝牙断开连接了 ACTION_ACL_DISCONNECTED");
                    break;
            }
        }
    };

}
