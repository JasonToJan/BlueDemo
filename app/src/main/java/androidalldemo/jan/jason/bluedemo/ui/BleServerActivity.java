package androidalldemo.jan.jason.bluedemo.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.os.SystemClock;

import com.cxz.swipelibrary.SwipeBackActivity;

import java.util.Arrays;
import java.util.UUID;

import androidalldemo.jan.jason.bluedemo.R;
import androidalldemo.jan.jason.bluedemo.databinding.ActivityBleServerBinding;
import androidalldemo.jan.jason.bluedemo.utils.LogUtils;
import androidx.databinding.DataBindingUtil;

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

        //默认的蓝牙适配器通过Advertiser开广播
    }

    /**
     * 启动服务，这样才能代表我们是服务端
     * 这个主要是方便我们自己，处理一些通信逻辑的
     * 最终boss 服务开启 控制死亡射线，以及处理很多逻辑
     * 蓝牙管理员 开服务
     *
     */
    private void startGattService(){

        BluetoothGattService service = new BluetoothGattService(UUID_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        //配置服务属性
        BluetoothGattCharacteristic characteristicRead = new BluetoothGattCharacteristic(
                UUID_CHAR_READ_NOTIFY,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ
        );
        characteristicRead.addDescriptor(new BluetoothGattDescriptor(UUID_DESC_NOTITY,BluetoothGattCharacteristic.PERMISSION_WRITE));

        service.addCharacteristic(characteristicRead);

        //继续配置服务属性
        BluetoothGattCharacteristic characteristicWrite = new BluetoothGattCharacteristic(
                UUID_CHAR_WRITE,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);

        service.addCharacteristic(characteristicWrite);

        //通过蓝牙管理员 开启服务
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            mBluetoothGattServer = bluetoothManager.openGattServer(this,mBluetoothGattServerCallback);
            mBluetoothGattServer.addService(service);//addService方式来增加服务
        }
    }

    private void logTv(final String msg) {
        if (isDestroyed())
            return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //ToastUtils.show(msg);
                binding.absServerLogTv.append(msg + "\n\n");
            }
        });
    }

    // BLE广播Callback
    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            logTv("\nBLE广播开启成功");
        }

        @Override
        public void onStartFailure(int errorCode) {
            logTv("\nBLE广播开启失败,错误码:" + errorCode);
        }
    };

    //服务端Gatter回调
    private BluetoothGattServerCallback mBluetoothGattServerCallback = new BluetoothGattServerCallback() {

        //这个不管连不连接，只有boss服务开启成功，这个函数就会回调
        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            LogUtils.i(String.format("onServiceAdded:%s,%s", status, service.getUuid()));
            logTv(String.format(status == 0 ? "添加服务[%s]成功" : "添加服务[%s]失败,错误码:" + status, service.getUuid()));
        }

        //连接状态变更，这个的话，只要有客户端连接成功，会触发这个回调，当客户端退出，也会触发断开回调
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            LogUtils.i(String.format("onConnectionStateChange:%s,%s,%s,%s", device.getName(), device.getAddress(), status, newState));
            logTv(String.format(status == 0 ? (newState == 2 ? "与[%s]连接成功" : "与[%s]连接断开") : ("与[%s]连接出错,错误码:" + status), device));
        }

        //客户端请求读取，会触发这个函数，然后这个函数给客户端发了一个字符串，主要是为了响应客户端，而不是客户端想要读取这个字符串，先后顺序很重要
        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            LogUtils.i(String.format("onCharacteristicReadRequest:%s,%s,%s,%s,%s", device.getName(), device.getAddress(), requestId, offset, characteristic.getUuid()));
            String response = "CHAR_" + (int) (Math.random() * 100); //模拟数据
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, response.getBytes());// 响应客户端
            logTv("客户端读取Characteristic[" + characteristic.getUuid() + "]:\n" + response);
        }

        //客户端请求写入，会触发这个函数，然后这个函数回调参数中有客户端传过来的byte流。
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] requestBytes) {
            // 获取客户端发过来的数据
            String requestStr = new String(requestBytes);
            LogUtils.i(String.format("onCharacteristicWriteRequest:%s,%s,%s,%s,%s,%s,%s,%s", device.getName(), device.getAddress(), requestId, characteristic.getUuid(),
                    preparedWrite, responseNeeded, offset, requestStr));
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, requestBytes);// 响应客户端
            logTv("客户端写入Characteristic[" + characteristic.getUuid() + "]:\n" + requestStr);
        }

        //客户端请求读取 通知 ，然后，这里给客户端发送随机通知
        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            LogUtils.i(String.format("onDescriptorReadRequest:%s,%s,%s,%s,%s", device.getName(), device.getAddress(), requestId, offset, descriptor.getUuid()));
            String response = "DESC_" + (int) (Math.random() * 100); //模拟数据
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, response.getBytes()); // 响应客户端
            logTv("客户端读取Descriptor[" + descriptor.getUuid() + "]:\n" + response);
        }

        //客户端请求写入通知，会触发这个回调，这里先获取客户端写的value，写 的话 说明回调里面是有数据哒
        @Override
        public void onDescriptorWriteRequest(final BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            // 获取客户端发过来的数据
            String valueStr = Arrays.toString(value);
            LogUtils.i(String.format("onDescriptorWriteRequest:%s,%s,%s,%s,%s,%s,%s,%s", device.getName(), device.getAddress(), requestId, descriptor.getUuid(),
                    preparedWrite, responseNeeded, offset, valueStr));
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);// 响应客户端
            logTv("客户端写入Descriptor[" + descriptor.getUuid() + "]:\n" + valueStr);

            // 简单模拟通知客户端Characteristic变化
            if (Arrays.toString(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE).equals(valueStr)) { //是否开启通知
                final BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < 2; i++) {
                            SystemClock.sleep(3000);
                            String response = "CHAR_" + (int) (Math.random() * 100); //模拟数据
                            characteristic.setValue(response);
                            mBluetoothGattServer.notifyCharacteristicChanged(device, characteristic, false);
                            logTv("通知客户端改变Characteristic[" + characteristic.getUuid() + "]:\n" + response);
                        }
                    }
                }).start();
            }
        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            LogUtils.i(String.format("onExecuteWrite:%s,%s,%s,%s", device.getName(), device.getAddress(), requestId, execute));
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            LogUtils.i(String.format("onNotificationSent:%s,%s,%s", device.getName(), device.getAddress(), status));
        }

        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu) {
            LogUtils.i(String.format("onMtuChanged:%s,%s,%s", device.getName(), device.getAddress(), mtu));
        }
    };
}
