package androidalldemo.jan.jason.bluedemo.ui;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;

import com.cxz.swipelibrary.SwipeBackActivity;

import androidalldemo.jan.jason.bluedemo.R;
import androidalldemo.jan.jason.bluedemo.databinding.ActivityBleServerBinding;

/**
 * Ble 蓝牙 服务端
 */
public class BleServerActivity extends SwipeBackActivity {

    private ActivityBleServerBinding binding;

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
    }
}
