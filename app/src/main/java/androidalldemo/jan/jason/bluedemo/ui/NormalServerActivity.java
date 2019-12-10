package androidalldemo.jan.jason.bluedemo.ui;


import android.os.Bundle;

import androidx.databinding.DataBindingUtil;

import com.cxz.swipelibrary.SwipeBackActivity;

import androidalldemo.jan.jason.bluedemo.R;
import androidalldemo.jan.jason.bluedemo.databinding.ActivityNormalServerBinding;

/**
 * 普通蓝牙服务端
 */
public class NormalServerActivity extends SwipeBackActivity {

    private ActivityNormalServerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_normal_server);

        initData();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        setTitle("普通蓝牙 服务端");
    }
}
