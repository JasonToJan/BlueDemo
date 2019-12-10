package androidalldemo.jan.jason.bluedemo.ui;


import android.os.Bundle;

import androidx.databinding.DataBindingUtil;

import com.cxz.swipelibrary.SwipeBackActivity;

import androidalldemo.jan.jason.bluedemo.R;
import androidalldemo.jan.jason.bluedemo.databinding.ActivityBleClientBinding;

/**
 * BLE 蓝牙 客户端
 */
public class BleClientActivity extends SwipeBackActivity {

    private ActivityBleClientBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ble_client);

        initData();
    }

    /**
     * 初始化数据相关
     */
    private void initData() {
        setTitle("BLE 蓝牙 客户端");
    }
}
