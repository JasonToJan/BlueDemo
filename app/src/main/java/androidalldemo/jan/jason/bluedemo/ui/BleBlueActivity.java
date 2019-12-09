package androidalldemo.jan.jason.bluedemo.ui;

import androidalldemo.jan.jason.bluedemo.App;
import androidalldemo.jan.jason.bluedemo.R;
import androidalldemo.jan.jason.bluedemo.databinding.ActivityBleBlueBinding;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.view.View;

import com.cxz.swipelibrary.SwipeBackActivity;

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
                App.toast("click client btn", true);
                break;

            case R.id.abb_server_btn:
                App.toast("click server btn", true);
                break;
        }
    }

    /**
     * 初始化数据以及监听相关
     */
    private void initData() {

        binding.abbClientBtn.setOnClickListener(this);
        binding.abbServerBtn.setOnClickListener(this);

    }


}