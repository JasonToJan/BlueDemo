package androidalldemo.jan.jason.bluedemo;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Handler;
import android.widget.Toast;

/**
 * Description: 全局入口
 * *
 * Creator: Wang
 * Date: 2019/12/9 20:32
 */
public class App extends Application {

    /**
     * 入口 实例，给别的地方用
     */
    private static App instance;

    /**
     * 全局句柄
     */
    private static final Handler sHandler = new Handler();


    @SuppressLint("ShowToast")
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static App getInstance() {
        return instance;
    }

    /**
     * handler线程切换 到主线程，可以在子线程中调用该方法，进而切换到主线程，方便更新UI
     * @param runnable
     */
    public static void runUi(Runnable runnable) {
        sHandler.post(runnable);
    }
}
