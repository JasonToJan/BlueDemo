package androidalldemo.jan.jason.bluedemo.ui;

import androidalldemo.jan.jason.bluedemo.App;
import androidalldemo.jan.jason.bluedemo.R;
import androidalldemo.jan.jason.bluedemo.databinding.ActivityNormalBlueBinding;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.view.View;

import com.cxz.swipelibrary.SwipeBackActivity;

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
                App.toast("click client btn", true);
                break;

            case R.id.anb_server_btn:
                App.toast("click server btn", true);
                break;
        }
    }

    /**
     * 初始化数据以及监听相关
     */
    private void initData() {

        binding.anbClientBtn.setOnClickListener(this);
        binding.anbServerBtn.setOnClickListener(this);

    }


}
