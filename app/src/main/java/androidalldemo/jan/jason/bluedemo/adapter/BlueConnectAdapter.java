package androidalldemo.jan.jason.bluedemo.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import androidalldemo.jan.jason.bluedemo.R;
import androidalldemo.jan.jason.bluedemo.bean.BlueBean;

/**
 * Description:
 * *
 * Creator: Wang
 * Date: 2019/12/8 21:23
 */
public class BlueConnectAdapter extends BaseQuickAdapter<BlueBean,BaseViewHolder> {

    public BlueConnectAdapter(List<BlueBean> datas){
        super(R.layout.item_blue_adapter, datas);
    }

    @Override
    protected void convert(BaseViewHolder helper, BlueBean item) {

        helper.setText(R.id.iba_tv_name, "name = "+item.getName())
                .setText(R.id.iba_tv_address, "address = "+item.getAddress());

    }
}
