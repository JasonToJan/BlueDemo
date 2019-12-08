package androidalldemo.jan.jason.bluedemo.bean;

/**
 * Description: 连接蓝牙需要用的实体
 * *
 * Creator: Wang
 * Date: 2019/12/8 21:21
 */
public class BlueBean {

    private String name;

    private String address;

    public BlueBean() {
    }

    public BlueBean(String name, String address) {
        this.name = name;
        this.address = address;
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
