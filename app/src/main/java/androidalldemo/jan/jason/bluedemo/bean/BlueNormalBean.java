package androidalldemo.jan.jason.bluedemo.bean;

import android.bluetooth.BluetoothDevice;

/**
 * Description: 连接蓝牙需要用的实体
 * *
 * Creator: Wang
 * Date: 2019/12/8 21:21
 */
public class BlueNormalBean {

    private String name;

    private String address;
    /**
     * 配对状态
     */
    private int bondState;
    /**
     * 当前蓝牙设备
     */
    private BluetoothDevice device;

    public BlueNormalBean() {
    }

    public BlueNormalBean(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public BlueNormalBean(String name, String address, int bondState) {
        this.name = name;
        this.address = address;
        this.bondState = bondState;
    }

    public BlueNormalBean(String name, String address, int bondState, BluetoothDevice device) {
        this.name = name;
        this.address = address;
        this.bondState = bondState;
        this.device = device;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public int getBondState() {
        return bondState;
    }

    public void setBondState(int bondState) {
        this.bondState = bondState;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
