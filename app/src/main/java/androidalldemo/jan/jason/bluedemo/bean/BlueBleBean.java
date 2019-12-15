package androidalldemo.jan.jason.bluedemo.bean;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;

import java.util.Objects;
import androidx.annotation.Nullable;

/**
 * Description: BLE 实体类
 * *
 * Creator: Wang
 * Date: 2019/12/13 22:13
 */
public class BlueBleBean {

    /**
     * 蓝牙设备
     */
    private BluetoothDevice device;

    /**
     * 官方的一个扫描结果类
     */
    ScanResult scanResult;

    public BlueBleBean(BluetoothDevice device, ScanResult scanResult) {
        this.device = device;
        this.scanResult = scanResult;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public ScanResult getScanResult() {
        return scanResult;
    }

    public void setScanResult(ScanResult scanResult) {
        this.scanResult = scanResult;
    }

    @Override
    public boolean equals(@Nullable Object obj) {

        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        BlueBleBean bleBean = (BlueBleBean) obj;
        //比较里面的设备
        return Objects.equals(device,bleBean.device);



    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
