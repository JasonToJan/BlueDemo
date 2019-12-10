package androidalldemo.jan.jason.bluedemo.ui;

import android.os.Bundle;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.cxz.swipelibrary.SwipeBackActivity;

import androidalldemo.jan.jason.bluedemo.R;
import androidalldemo.jan.jason.bluedemo.databinding.ActivityNormalBlueBinding;
import androidalldemo.jan.jason.bluedemo.utils.Utils;

/**
 * 普通的蓝牙 Activity
 */
public class NormalBlueActivity extends SwipeBackActivity implements View.OnClickListener{

    ActivityNormalBlueBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_normal_blue);

        initData();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.anb_client_btn:
                Utils.jumpToActivity(this,NormalClientActivity.class,null);
                break;

            case R.id.anb_server_btn:
                Utils.jumpToActivity(this,NormalServerActivity.class,null);
                break;
        }
    }

    /**
     * 初始化数据以及监听相关
     */
    private void initData() {

        binding.anbClientBtn.setOnClickListener(this);
        binding.anbServerBtn.setOnClickListener(this);

        setTitle("普通蓝牙");

    }


}
