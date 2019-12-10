package androidalldemo.jan.jason.bluedemo.ui;

import android.os.Bundle;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.cxz.swipelibrary.SwipeBackActivity;

import androidalldemo.jan.jason.bluedemo.R;
import androidalldemo.jan.jason.bluedemo.databinding.ActivityBleBlueBinding;
import androidalldemo.jan.jason.bluedemo.utils.ToastUtils;
import androidalldemo.jan.jason.bluedemo.utils.Utils;

public class BleBlueActivity extends SwipeBackActivity implements View.OnClickListener{

    private ActivityBleBlueBinding binding ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ble_blue);

        initData();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.abb_client_btn:
                Utils.jumpToActivity(this,BleClientActivity.class,null);
                break;

            case R.id.abb_server_btn:
                Utils.jumpToActivity(this,BleServerActivity.class,null);
                break;
        }
    }

    /**
     * 初始化数据以及监听相关
     */
    private void initData() {

        binding.abbClientBtn.setOnClickListener(this);
        binding.abbServerBtn.setOnClickListener(this);

        setTitle("BLE 蓝牙");

    }


}