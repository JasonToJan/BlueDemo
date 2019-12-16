package androidalldemo.jan.jason.bluedemo.utils;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static androidalldemo.jan.jason.bluedemo.utils.LogUtils.d;


/**
 * 蓝牙配对
 */
public class BlueUtils {

     /**
     * 与设备配对 参考源码：platform/packages/apps/Settings.git 
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java 
     */  
    public static boolean createBond(Class btClass, BluetoothDevice btDevice){

        try {
            Method createBondMethod = btClass.getMethod("createBond");
            Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
            return returnValue.booleanValue();
        } catch (Throwable e) {
            d("Error", "##" + e.getMessage());
        }

        return false;
    }  
   
    /** 
     * 与设备解除配对 参考源码：platform/packages/apps/Settings.git 
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java 
     */  
     public static boolean removeBond(Class<?> btClass, BluetoothDevice btDevice) {

        try {
            Method removeBondMethod = btClass.getMethod("removeBond");
            Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
            return returnValue.booleanValue();
        } catch (Throwable e) {
            d("Error", "##" + e.getMessage());
        }

        return false;
    }  
   
     public static boolean setPin(Class<? extends BluetoothDevice> btClass, BluetoothDevice btDevice, String str) {
        try {
            Method removeBondMethod = btClass.getDeclaredMethod("setPin",  
                    new Class[]  
                    {byte[].class});  
            Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice,  
                    new Object[]  
                    {str.getBytes()});  
            Log.e("returnValue", "" + returnValue);

        } catch (Throwable e) {
            d("Error##", e.getMessage());
        }
        return true;  
   
    }

    /**
     * 取消用户输入
     * @param btClass
     * @param device
     * @return
     */
     public static boolean cancelPairingUserInput(Class<?> btClass, BluetoothDevice device) {

        try {
            Method createBondMethod = btClass.getMethod("cancelPairingUserInput");
//        cancelBondProcess(btClass, device);
            Boolean returnValue = (Boolean) createBondMethod.invoke(device);
            return returnValue.booleanValue();
        } catch (Throwable e) {
            d("Error", "##" + e.getMessage());
        }

        return false;
    }


    /**
     * 取消配对
     * @param btClass
     * @param device
     * @return
     */
     public static boolean cancelBondProcess(Class<?> btClass, BluetoothDevice device) {

         try {
             Method createBondMethod = btClass.getMethod("cancelBondProcess");
             Boolean returnValue = (Boolean) createBondMethod.invoke(device);
             return returnValue.booleanValue();
         } catch (Throwable e) {
             d("Error", "##" + e.getMessage());
         }

         return false;
    }

    /**
     * 确认配对
     * @param btClass
     * @param device
     * @param isConfirm
     * @throws Exception
     */
     public static void setPairingConfirmation(Class<?> btClass,BluetoothDevice device,boolean isConfirm) {
         try {
             Method setPairingConfirmation = btClass.getDeclaredMethod("setPairingConfirmation",boolean.class);
             setPairingConfirmation.invoke(device,isConfirm);
         } catch (Throwable e) {
             d("Error", "##" + e.getMessage());
         }
    }
    
   
    /** 
     * 
     * @param clsShow 
     */  
     public static void printAllInform(Class clsShow) {
        try {
            // 取得所有方法  
            Method[] hideMethod = clsShow.getMethods();  
            int i = 0;  
            for (; i < hideMethod.length; i++)  
            {  
                Log.e("method name", hideMethod[i].getName() + ";and the i is:"  
                        + i);  
            }
            // 取得所有常量  
            Field[] allFields = clsShow.getFields();
            for (i = 0; i < allFields.length; i++)  
            {  
                Log.e("Field name", allFields[i].getName());  
            }
        } catch (Throwable e) {
            d("Error", "##" + e.getMessage());
        }
    }

    /**
     * BLE 客户端 连接服务端 反射实现
     * @param device
     * @param gattCallback
     * @return
     */
    public static BluetoothGatt connectToBleService(Context context,BluetoothDevice device,BluetoothGattCallback gattCallback) {
        try {
            Method m = device.getClass().getDeclaredMethod("connectGatt", Context.class, boolean.class,
                    BluetoothGattCallback.class, int.class);

            int transport = device.getClass().getDeclaredField("TRANSPORT_LE").getInt(null);
            return (BluetoothGatt) m.invoke(device, context, false, gattCallback, transport);

        } catch (Throwable e) {
            LogUtils.d("TEST##",e.getMessage());
            return null;
        }
    }
} 