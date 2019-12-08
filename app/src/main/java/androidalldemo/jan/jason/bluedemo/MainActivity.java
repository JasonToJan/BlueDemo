package androidalldemo.jan.jason.bluedemo;

import androidalldemo.jan.jason.bluedemo.adapter.BlueConnectAdapter;
import androidalldemo.jan.jason.bluedemo.bean.BlueBean;
import androidalldemo.jan.jason.bluedemo.databinding.ActivityMainBinding;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "TEST##MainActivity";

    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE = 1;
    private ActivityMainBinding binding;

    private BlueConnectAdapter adapter;
    private ArrayList<BlueBean> blueDatas = new ArrayList<>();

    //连接时需要用的类
    private BluetoothDevice connectDevice;//连接的设备
    private BluetoothSocket connectClientSocket;//连接时，客户端的Socket
    private OutputStream connectOs;
    private final UUID MY_UUID_TEST1 = UUID.fromString("db764ac8-4b08-7f25-aafe-59d03c27bae3");
    private final UUID MY_UUID_SSP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//Android SSP串口的默认UUID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);

        initView();
        initData();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            Log.d(TAG, "##" + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10) { //得到权限后
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mBluetoothAdapter != null) {
                    Log.d("TEST##","开始查找");
                    mBluetoothAdapter.startDiscovery();
                }
            }
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btnSearch:
                if (mBluetoothAdapter == null) return;
                // 判断是否在搜索,如果在搜索，就取消搜索
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                // 开始搜索
                getPermissionThenDo();
                break;
        }
    }

    /**
     * 获取权限，然后执行搜索
     */
    private void getPermissionThenDo() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
            } else {
                mBluetoothAdapter.startDiscovery();
            }
        } else {
            mBluetoothAdapter.startDiscovery();
        }
    }

    /**
     * 初始化视图相关，设置监听相关
     */
    private void initView(){
        binding.btnSearch.setOnClickListener(this);
    }

    /**
     * 初始化数据相关
     */
    private void initData(){
        initBluetooth();
        getMyDevicesBluetoothInfo();
        getBondedDevices();
        registerMyReceiver();

        initRecyclerView();
    }

    /**
     * 初始化配置列表
     */
    private void initRecyclerView() {
        adapter = new BlueConnectAdapter(blueDatas);

        binding.amRvBluelist.setLayoutManager(new LinearLayoutManager(this));
        binding.amRvBluelist.setAdapter(adapter);

        adapter.openLoadAnimation(BaseQuickAdapter.SCALEIN);
        adapter.isFirstOnly(false);

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                //Toast.makeText(MainActivity.this, "click item", Toast.LENGTH_SHORT).show();
                try {
                    doConnectLogic((BlueBean) adapter.getData().get(position));
                } catch (Exception e) {
                    Log.d(TAG, "##" + e.getMessage());
                }
            }
        });

    }

    /**
     * 处理连接的逻辑
     */
    private void doConnectLogic(BlueBean bean) {
        String address = bean.getAddress();
        if (mBluetoothAdapter == null) return ;

        try {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }

            if (connectDevice == null) {
                connectDevice = mBluetoothAdapter.getRemoteDevice(address);//地址拿到的设备,远程设备
            }

            if (connectClientSocket == null) {
                connectClientSocket = connectDevice.createRfcommSocketToServiceRecord(MY_UUID_TEST1);//设备创建一个Socket
                connectClientSocket.connect();

                connectOs = connectClientSocket.getOutputStream();//得到远程设备创建的Socket中的输出流
            }

            if (connectOs != null) {
                connectOs.write("Hello Bluetooth! ".getBytes("utf-8"));//再添加点东西给输出流
            }

        } catch (Exception e) {
            Log.d(TAG, "##" + e.getMessage());
        }
    }

    /**
     * 注册广播
     */
    private void registerMyReceiver() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);// 找到设备的广播
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//开始扫描
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);// 搜索完成的广播
        registerReceiver(receiver, filter);

    }

    /**
     * 获取已经配对过的设备
     */
    private void getBondedDevices() {
        // 获取已经配对的设备
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                .getBondedDevices();

        // 判断是否有配对过的设备
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                // 遍历到列表中
                binding.tvDevices.append(device.getName() + ":" + device.getAddress());
                Log.i(TAG+"已配对设备", binding.tvDevices.getText().toString());
            }
        }
    }

    /**
     * 初始化蓝牙，判断是否打开，如果没打开，弹出系统对话框，用户决定是否打开
     */
    private void initBluetooth(){
        //获取本地蓝牙适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //判断是否硬件支持蓝牙
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "本地蓝牙不可用", Toast.LENGTH_SHORT).show();
            //退出应用
            finish();
        }

        //判断是否打开蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            //弹出对话框提示用户是否打开
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE);
            //不做提示，强行打开
            // mBluetoothAdapter.enable();
        }
    }

    /**
     * 获取我自己的设备的蓝牙信息，显示一下
     */
    private void getMyDevicesBluetoothInfo(){
        //获取本地蓝牙名称
        String name = mBluetoothAdapter.getName();
        //获取本地蓝牙地址
        String address = mBluetoothAdapter.getAddress();
        //打印相关信息
        Log.i(TAG, name +" \n BLE Address = "+ address);

        binding.amTv1.setText(name+"\n"+address);
    }

    // 可选方法，非必需
    // 此方法使自身的蓝牙设备可以被其他蓝牙设备扫描到，
    // 注意时间阈值。0 - 3600 秒。
    // 通常设置时间为120秒。
    private void enable_discovery() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);

        // 第二个参数可设置的范围是0~3600秒，在此时间区间（窗口期）内可被发现
        // 任何不在此区间的值都将被自动设置成120秒。
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);

        startActivity(discoverableIntent);
    }

    // 广播接收器
    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 收到的广播类型
            String action = intent.getAction();

            if (action == null) return;

            Log.d("TEST##","receive action = "+ action);

            switch (action) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    Log.d("TEST##","begin to discovery");
                    blueDatas.clear();
                    break;

                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // 判断是否配对过
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        // 添加到列表
                        blueDatas.add(new BlueBean(device.getName(), device.getAddress()));
                        binding.tvDevices.append(device.getName() + ":"
                                + device.getAddress() + "\n");
                    }
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:

                    //replaceData
                    if (adapter != null) {
                        adapter.replaceData(blueDatas);
                    }

                    break;
            }
        }
    };

    /**
     * 定义一个传菜员
     */
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Toast.makeText(MainActivity.this, String.valueOf(msg.obj), Toast.LENGTH_SHORT).show();
            super.handleMessage(msg);
        }
    };

    /**
     * 线程
     */
    private class AcceptThread extends Thread {

        private BluetoothServerSocket serverSocket;
        private BluetoothSocket socket;

        //输入 输出流
        private OutputStream os;
        private InputStream is;

        public AcceptThread() {

        }
    }


}
