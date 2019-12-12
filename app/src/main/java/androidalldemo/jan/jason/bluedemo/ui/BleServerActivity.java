package androidalldemo.jan.jason.bluedemo.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;

import androidalldemo.jan.jason.bluedemo.App;
import androidalldemo.jan.jason.bluedemo.utils.ToastUtils;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;

import com.cxz.swipelibrary.SwipeBackActivity;

import java.util.UUID;

import androidalldemo.jan.jason.bluedemo.R;
import androidalldemo.jan.jason.bluedemo.databinding.ActivityBleServerBinding;

/**
 * Ble 蓝牙 服务端
 */
public class BleServerActivity extends SwipeBackActivity {

    public static final UUID UUID_SERVICE = UUID.fromString("10000000-0000-0000-0000-000000000000"); //自定义UUID
    public static final UUID UUID_CHAR_READ_NOTIFY = UUID.fromString("11000000-0000-0000-0000-000000000000");
    public static final UUID UUID_DESC_NOTITY = UUID.fromString("11100000-0000-0000-0000-000000000000");
    public static final UUID UUID_CHAR_WRITE = UUID.fromString("12000000-0000-0000-0000-000000000000");

    private ActivityBleServerBinding binding;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser; // BLE广播
    private BluetoothGattServer mBluetoothGattServer; // BLE服务端

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ble_server);

        initData();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        setTitle("BLE 蓝牙 服务端");

        startAbvertise();//启动广播雷达死亡射线
        startGattService();//启动BLE服务端
    }

    /**
     * 启动死亡射线，这样别的手机能够发现我们
     * 这个主要是方便别人，来找到我们
     */
    private void startAbvertise(){

        //广播设置（必须）
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                 //广播模式：低功耗，平衡，低延迟，这样类似于发送一个不耗电的广播
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                //发射功率，一般都要高一点
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                //是否能够连接，广播分为连接广播和不可连接广播
                .setConnectable(true)
                .build();

        //广播数据（必须，广播启动就会发送这个数据）
        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true) //包含蓝牙名称
                .setIncludeTxPowerLevel(true) //包含发射功率级别
                .addManufacturerData(1,new byte[]{23,33}) //设备厂商数据
                .build();

        //广播数据（可选，当客户端扫描时才发送的数据）
        AdvertiseData scanResponse = new AdvertiseData.Builder()
                .addManufacturerData(2,new byte[]{66,66}) //设备厂商数据，自定义
                .addServiceUuid(new ParcelUuid(UUID_SERVICE)) //服务的UUID
                .build();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();//广播发布者
        mBluetoothLeAdvertiser.startAdvertising(settings,advertiseData,scanResponse,mAdvertiseCallback);

    }

    /**
     * 启动服务，这样才能代表我们是服务端
     * 这个主要是方便我们自己，处理一些通信逻辑的
     *
     */
    private void startGattService(){

    }

    private void logTv(final String msg) {
        if (isDestroyed())
            return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.show(msg);
                binding.absServerLogTv.append(msg + "\n\n");
            }
        });
    }

    // BLE广播Callback
    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            logTv("BLE广播开启成功");
        }

        @Override
        public void onStartFailure(int errorCode) {
            logTv("BLE广播开启失败,错误码:" + errorCode);
        }
    };
}
