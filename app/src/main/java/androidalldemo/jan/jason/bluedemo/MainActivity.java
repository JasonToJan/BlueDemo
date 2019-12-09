package androidalldemo.jan.jason.bluedemo;


import android.os.Bundle;
import android.view.View;

import com.cxz.swipelibrary.SwipeBackActivity;

import androidalldemo.jan.jason.bluedemo.databinding.LayoutMainActivityBinding;
import androidalldemo.jan.jason.bluedemo.ui.BleBlueActivity;
import androidalldemo.jan.jason.bluedemo.ui.NormalBlueActivity;
import androidalldemo.jan.jason.bluedemo.ui.TestAPIActivity;
import androidalldemo.jan.jason.bluedemo.utils.Utils;
import androidx.databinding.DataBindingUtil;

public class MainActivity extends SwipeBackActivity implements View.OnClickListener{

    LayoutMainActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.layout_main_activity);

        initData();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lma_test_api_tv:
                Utils.jumpToActivity(this, TestAPIActivity.class,null);
                break;

            case R.id.lma_normal_blue_tv:
                Utils.jumpToActivity(this, NormalBlueActivity.class,null);
                break;

            case R.id.lma_ble_blue_tv:
                Utils.jumpToActivity(this, BleBlueActivity.class,null);
                break;

        }
    }

    private void initData(){
        binding.lmaTestApiTv.setOnClickListener(this);
        binding.lmaNormalBlueTv.setOnClickListener(this);
        binding.lmaBleBlueTv.setOnClickListener(this);
    }

}
