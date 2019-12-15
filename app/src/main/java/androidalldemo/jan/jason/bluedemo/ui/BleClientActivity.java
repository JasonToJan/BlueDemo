package androidalldemo.jan.jason.bluedemo.ui;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.cxz.swipelibrary.SwipeBackActivity;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import androidalldemo.jan.jason.bluedemo.R;
import androidalldemo.jan.jason.bluedemo.adapter.BlueBleConnectAdapter;
import androidalldemo.jan.jason.bluedemo.bean.BlueBleBean;
import androidalldemo.jan.jason.bluedemo.databinding.ActivityBleClientBinding;
import androidalldemo.jan.jason.bluedemo.utils.LogUtils;
import androidalldemo.jan.jason.bluedemo.utils.ToastUtils;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import static androidalldemo.jan.jason.bluedemo.utils.LogUtils.d;

/**
 * BLE 蓝牙 客户端
 */
public class BleClientActivity extends SwipeBackActivity implements View.OnClickListener{

    private ActivityBleClientBinding binding;
    private MyHandler myHandler;
    private boolean isScanning;//是否正在扫描
    private boolean isConnected = false;//判断是否已经成功连接了，可以处理其他逻辑
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private List<BlueBleBean> bleBeans;
    private BlueBleConnectAdapter myAdapter;
    /**
     * 服务端叫做BluetoothGattServer，客户端就是BluetoothGatt了
     */
    private BluetoothGatt mBluetoothGatt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ble_client);

        initView();
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeConnection();
        if (myHandler != null) {
            myHandler.removeCallbacksAndMessages(null);
            myHandler = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10) { //得到权限后
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanSurroundBle();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.abc_scan_btn:
                scanSurroundBle();
                break;

            case R.id.abc_request_read_btn:
                requestRead();
                break;

            case R.id.abc_request_write_btn:
                requestWrite();
                break;

            case R.id.abc_request_read_notify_btn:
                requestReadNotify();
                break;

            case R.id.abc_request_write_notify_btn:
                requestWriteNotify();
                break;
        }
    }

    /**
     * 初始化视图
     */
    private void initView(){
        binding.abcRequestReadBtn.setOnClickListener(this);
        binding.abcRequestWriteBtn.setOnClickListener(this);
        binding.abcRequestReadNotifyBtn.setOnClickListener(this);
        binding.abcRequestWriteNotifyBtn.setOnClickListener(this);
        binding.abcScanBtn.setOnClickListener(this);
    }

    /**
     * 初始化数据相关
     */
    private void initData() {
        setTitle("BLE 蓝牙 客户端");

        myHandler = new MyHandler(this);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();//务必先打开一下
        }
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        myAdapter = new BlueBleConnectAdapter();
        binding.abcBleListRv.setLayoutManager(new LinearLayoutManager(this));
        binding.abcBleListRv.setAdapter(myAdapter);

        doSomethingForAdapter();

        getPermissionThenStartDiacovery();
    }

    /**
     *
     *
     * adapter的监听相关
     */
    private void doSomethingForAdapter() {
        bleBeans = myAdapter.getData();
        myAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        myAdapter.isFirstOnly(false);

        myAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                //处理客户端连接服务端Ble蓝牙的逻辑
                closeConnection();//断开之前的
                if (myAdapter.getData().size() <= position || position < 0)  return;
                
                BluetoothDevice device = myAdapter.getData().get(position).getDevice();//拿到服务端设备信息
                //通过这个设备信息 进行 连接
                mBluetoothGatt = device.connectGatt(BleClientActivity.this,false,mBluetoothGattCallback);
                logTv("开始连接服务端设备："+"\nname = "+device.getName()+" \naddress = "+device.getAddress());
                
            }
        });
    }

    /**
     * 获取权限，然后执行搜索
     */
    private void getPermissionThenStartDiacovery() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(BleClientActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
            } else {
                d("开始扫描");
                scanSurroundBle();
            }
        } else {
            scanSurroundBle();
        }
    }

    /**
     * 搜索周围的BLE蓝牙
     */
    private void scanSurroundBle(){

        //搜索前 一定要清空之前的记录
        if (myAdapter != null) {
            myAdapter.getData().clear();
            myAdapter.notifyDataSetChanged();
        }
        if (isScanning) {
            d("sorry，正在扫描，请稍后再试!");
            return ;
        }

        isScanning = true;

        if(bluetoothLeScanner != null) {
            bluetoothLeScanner.startScan(mScanCallback);

            //三秒后，延迟停止
            if (myHandler != null) {
                myHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (bluetoothLeScanner != null) {
                            bluetoothLeScanner.stopScan(mScanCallback);
                            isScanning = false;
                            d("延迟停止扫描BLE蓝牙~完成。");
                        }
                    }
                },3000);
            }
        }
    }

    /**
     * 关闭之前的连接，因为对于BLE蓝牙来说，可以同时连接2~7个，如果其他app也在连接，说不定这个连接就会错误了
     * 所以在本次连接前，务必断开之前可能存在的旧连接
     */
    private void closeConnection(){
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();//断开
            mBluetoothGatt.close();//彻底关闭
            mBluetoothGatt = null;
            d("已经关闭之前的连接");
        }
    }

    /**
     * 日志输出
     * @param msg
     */
    private void logTv(final String msg) {
        if (isDestroyed() || isFinishing()) return;
        
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.show(msg);
                binding.abcClientLogTv.append(msg + "\n\n");
            }
        });
        
    }

    /**
     * 请求读
     */
    private void requestRead(){
        BluetoothGattService service = getGattService(BleServerActivity.UUID_SERVICE);
        if (service != null) {
            //通过UUID获取可读的Characteristic，读到的东西，在BluetoothGattCallback回调里面
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(BleServerActivity.UUID_CHAR_READ_NOTIFY);
            mBluetoothGatt.readCharacteristic(characteristic);//通过调用Gatt的read方法来读取
        }
    }

    /**
     * 请求写，写入数据成功后，会回调到BluetoothGattCallback里面
     */
    private void requestWrite(){
        BluetoothGattService service = getGattService(BleServerActivity.UUID_SERVICE);
        if (service != null) {
            String text = binding.abcRequestWriteEt.getText().toString();
            if (!TextUtils.isEmpty(text)) {
                //通过UUID获取可写的Characteristic
                try {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(BleServerActivity.UUID_CHAR_WRITE);
                    characteristic.setValue(text.getBytes()); //单次最多20个字节
                    mBluetoothGatt.writeCharacteristic(characteristic);//调用Gatt的write方法来写入
                } catch (Throwable e) {
                    d("Error", "##" + e.getMessage());
                }
            } else {
                ToastUtils.show("请输入您要写给服务端的数据~");
            }
        }
    }

    /**
     * 请求读通知
     */
    private void requestReadNotify(){
        BluetoothGattService service = getGattService(BleServerActivity.UUID_SERVICE);
        if (service != null) {
            // 设置Characteristic通知
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(BleServerActivity.UUID_CHAR_READ_NOTIFY);//通过UUID获取可通知的Characteristic
            mBluetoothGatt.setCharacteristicNotification(characteristic, true);

            // 向Characteristic的Descriptor属性写入通知开关，使蓝牙设备主动向手机发送数据
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(BleServerActivity.UUID_DESC_NOTITY);
            // descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);//和通知类似,但服务端不主动发数据,只指示客户端读取数据
            // descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.readDescriptor(descriptor);
        }
    }

    /**
     * 请求写通知
     */
    private void requestWriteNotify(){
        BluetoothGattService service = getGattService(BleServerActivity.UUID_SERVICE);
        if (service != null) {
            // 设置Characteristic通知
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(BleServerActivity.UUID_CHAR_READ_NOTIFY);//通过UUID获取可通知的Characteristic
            mBluetoothGatt.setCharacteristicNotification(characteristic, true);

            // 向Characteristic的Descriptor属性写入通知开关，使蓝牙设备主动向手机发送数据
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(BleServerActivity.UUID_DESC_NOTITY);
            // descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);//和通知类似,但服务端不主动发数据,只指示客户端读取数据
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * 找到已经连接到的服务
     * @param uuid
     * @return
     */
    private BluetoothGattService getGattService(UUID uuid) {
        if (!isConnected) {
            ToastUtils.show("暂时没有连接哦");
            return null;
        }
        BluetoothGattService service = mBluetoothGatt.getService(uuid);
        if (service == null) {
            ToastUtils.show("sorry,没有找到UUID为："+uuid+" 的服务哦");
        }
        return service;
    }

    /**
     * 扫描回调，类似普通蓝牙的广播接收
     */
    private final ScanCallback mScanCallback = new ScanCallback() {// 扫描Callback
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            BlueBleBean dev = new BlueBleBean(result.getDevice(), result);
            if (!bleBeans.contains(dev)) {
                if (myAdapter != null) {
                    myAdapter.addData(dev);
                    LogUtils.i("onScanResult: " + result); // result.getScanRecord() 获取BLE广播数据
                }
            }
        }
    };

    /**
     * 客户端 通讯服务端的回调，几乎和服务端一致，这时候，双方平等
     * 之前普通蓝牙是通过传输水管socket进行通讯，这里就是通过 Gatt回调 通讯
     * 客户端和服务端的回调 不是同一类型，客户端是：BluetoothGattCallback 服务端是：BluetoothGattServerCallback
     * 仅仅都多了一个Server而已。
     */
    public BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {

        //这里最先执行，判断连接状态的变更
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            BluetoothDevice dev = gatt.getDevice();//目标服务端设备
            LogUtils.i(String.format("onConnectionStateChange:%s,%s,%s,%s", dev.getName(), dev.getAddress(), status, newState));
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                isConnected = true;
                gatt.discoverServices(); //启动服务发现
            } else {
                isConnected = false;
                closeConnection();
            }
            // 旧status 为 0, 新 newState为 2 说明连接成功
            // 旧status 为 0, 新 newState为 其他 说明连接断开
            // 旧status 为 不为 0, 说明连接出现异常了
            logTv(String.format(status == 0 ? (newState == 2 ? "与[%s]连接成功" : "与[%s]连接断开") : ("与[%s]连接出错,错误码:" + status), dev));
        }

        //服务发现后触发，一个Gatt 里面有可能会有多个服务，通过gatt.getServices函数名就知道
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            LogUtils.i( String.format("onServicesDiscovered:%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), status));
            if (status == BluetoothGatt.GATT_SUCCESS) { //BLE服务发现成功
                // 遍历获取BLE服务Services/Characteristics/Descriptors的全部UUID
                for (BluetoothGattService service : gatt.getServices()) {
                    StringBuilder allUUIDs = new StringBuilder("UUIDs={\nS=" + service.getUuid().toString());

                    //遍历所有Character
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        allUUIDs.append(",\nC=").append(characteristic.getUuid());

                        //遍历所有Descriptor
                        for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors())
                            allUUIDs.append(",\nD=").append(descriptor.getUuid());
                    }

                    allUUIDs.append("}");

                    LogUtils.i( "onServicesDiscovered:" + allUUIDs.toString());
                    logTv("发现服务" + allUUIDs);
                }
            }
        }

        //客户端读Character 的时候 会触发这个函数 ,读的东西包装在参数characteristic里面
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            UUID uuid = characteristic.getUuid();
            String valueStr = new String(characteristic.getValue());
            LogUtils.i(String.format("onCharacteristicRead:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr, status));
            logTv("读取Characteristic[" + uuid + "]:\n" + valueStr);
        }

        //客户端写入Character 的时候 会触发这个函数 ,首先客户端写的话，会先走 blueGatt.writeCharacteristic(...)函数
        //然后才触发这里
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            UUID uuid = characteristic.getUuid();
            String valueStr = new String(characteristic.getValue());
            LogUtils.i(String.format("onCharacteristicWrite:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr, status));
            logTv("写入Characteristic[" + uuid + "]:\n" + valueStr);
        }

        //客户端的Character发生改变后，会触发这里，不过还不知道触发点在什么地方
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            UUID uuid = characteristic.getUuid();
            String valueStr = new String(characteristic.getValue());
            LogUtils.i(String.format("onCharacteristicChanged:%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr));
            logTv("通知Characteristic[" + uuid + "]:\n" + valueStr);
        }

        //客户端尝试读取Descripor的时候，会触发这里读取的东西包含在参数中
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            UUID uuid = descriptor.getUuid();
            String valueStr = Arrays.toString(descriptor.getValue());
            LogUtils.i(String.format("onDescriptorRead:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr, status));
            logTv("读取Descriptor[" + uuid + "]:\n" + valueStr);
        }

        //客户端尝试写入Descriptor的时候，会触发这里，写入的东西是之前通过Gatt.writeDescripor中传过去的，
        //先写了，然后才触发这里
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            UUID uuid = descriptor.getUuid();
            String valueStr = Arrays.toString(descriptor.getValue());
            LogUtils.i(String.format("onDescriptorWrite:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr, status));
            logTv("写入Descriptor[" + uuid + "]:\n" + valueStr);
        }
    };

    private static class MyHandler extends Handler {

        WeakReference<BleClientActivity> parent;

        public MyHandler(BleClientActivity activity) {
            parent = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if(null!=parent) {
                BleClientActivity activity = (BleClientActivity) parent.get();
            }
        }
    }
}
