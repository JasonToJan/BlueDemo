package androidalldemo.jan.jason.bluedemo.adapter;

import android.bluetooth.BluetoothDevice;

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

    public BlueConnectAdapter(){
        super(R.layout.item_blue_adapter);
    }

    @Override
    protected void convert(BaseViewHolder helper, BlueBean item) {

        helper.setText(R.id.iba_tv_name, "name = "+item.getName())
                .setText(R.id.iba_tv_address, "address = \n"+item.getAddress() +
                        (item.getBondState() == BluetoothDevice.BOND_BONDED ? "\n(已配对)" : "\n未配对"));

    }
}
