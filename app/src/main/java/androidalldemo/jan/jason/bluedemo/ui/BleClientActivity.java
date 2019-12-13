package androidalldemo.jan.jason.bluedemo.ui;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.cxz.swipelibrary.SwipeBackActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidalldemo.jan.jason.bluedemo.R;
import androidalldemo.jan.jason.bluedemo.adapter.BlueBleConnectAdapter;
import androidalldemo.jan.jason.bluedemo.bean.BlueBleBean;
import androidalldemo.jan.jason.bluedemo.databinding.ActivityBleClientBinding;
import androidalldemo.jan.jason.bluedemo.utils.LogUtils;
import androidalldemo.jan.jason.bluedemo.utils.ToastUtils;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * BLE 蓝牙 客户端
 */
public class BleClientActivity extends SwipeBackActivity {

    private ActivityBleClientBinding binding;
    private MyHandler myHandler;
    private boolean isScanning;//是否正在扫描
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private List<BlueBleBean> bleBeans;
    private BlueBleConnectAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ble_client);

        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myHandler != null) {
            myHandler.removeCallbacksAndMessages(null);
            myHandler = null;
        }

    }

    /**
     * 初始化数据相关
     */
    private void initData() {
        setTitle("BLE 蓝牙 客户端");

        myHandler = new MyHandler(this);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        myAdapter = new BlueBleConnectAdapter();
        binding.abcBleListRv.setLayoutManager(new LinearLayoutManager(this));
        binding.abcBleListRv.setAdapter(myAdapter);

        doSomethingForAdapter();

        //第一次打开扫描
        scanSurroundBle();
    }

    /**
     * adapter的监听相关
     */
    private void doSomethingForAdapter() {
        bleBeans = myAdapter.getData();
        myAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        myAdapter.isFirstOnly(false);

        myAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                ToastUtils.show("click the item"+position);
            }
        });
    }

    /**
     * 搜索周围的BLE蓝牙
     */
    private void scanSurroundBle(){
        //搜索前 一定要清空
        if (myAdapter != null) {
            myAdapter.getData().clear();
            myAdapter.notifyDataSetChanged();
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
                            LogUtils.d("延迟停止扫描BLE蓝牙~完成。");
                        }
                    }
                },3000);
            }
        }
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
