package androidalldemo.jan.jason.bluedemo.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Description: 通用工具，杂乱无比
 * *
 * Creator: Wang
 * Date: 2019/12/9 21:03
 */
public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    /**
     * 开启一个Cache线程池
     */
    public static final Executor EXECUTOR = Executors.newCachedThreadPool();

    /**
     * 创建一个文件夹
     * @param filePath
     */
    public static void mkdirs(String filePath) {
        boolean mk = new File(filePath).mkdirs();
        LogUtils.d(TAG, "mkdirs: " + mk);
    }

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
