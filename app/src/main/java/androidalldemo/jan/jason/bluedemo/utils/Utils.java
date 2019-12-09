package androidalldemo.jan.jason.bluedemo.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Description: 通用工具，杂乱无比
 * *
 * Creator: Wang
 * Date: 2019/12/9 21:03
 */
public class Utils {

    /**
     * 跳转到一个Activity
     * @param sourceActivity
     * @param targetActivityClass
     * @param extrasBundle 携带的包数据
     */
    public static void jumpToActivity(Activity sourceActivity, Class<?> targetActivityClass, Bundle extrasBundle){
        Intent intent = new Intent(sourceActivity, targetActivityClass);
        if (extrasBundle != null) {
            intent.putExtras(extrasBundle);
        }
        sourceActivity.startActivity(intent);
    }


}
