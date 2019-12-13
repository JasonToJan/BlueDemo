package androidalldemo.jan.jason.bluedemo.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import androidalldemo.jan.jason.bluedemo.R;
import androidalldemo.jan.jason.bluedemo.bean.BlueBleBean;

/**
 * Description: BLE 蓝牙适配器
 * *
 * Creator: Wang
 * Date: 2019/12/8 21:23
 */
public class BlueBleConnectAdapter extends BaseQuickAdapter<BlueBleBean,BaseViewHolder> {

    public BlueBleConnectAdapter(){
        super(R.layout.item_blue_adapter);
    }

    @Override
    protected void convert(BaseViewHolder helper, BlueBleBean item) {

        helper.setText(R.id.iba_tv_name, "name = "+item.getDevice().getName()+"\n address = "+item.getDevice().getAddress())
                .setText(R.id.iba_tv_address, "ScanResult = \n"+item.getScanResult().getScanRecord());

    }
}
