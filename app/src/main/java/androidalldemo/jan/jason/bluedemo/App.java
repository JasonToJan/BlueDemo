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
     * 全局句柄
     */
    private static final Handler sHandler = new Handler();

    /**
     * 全局Toast
     */
    private static Toast sToast; // 单例Toast,避免重复创建，显示时间过长

    @SuppressLint("ShowToast")
    @Override
    public void onCreate() {
        super.onCreate();
        sToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
    }

    /**
     * 展示一个吐司
     * @param txt
     * @param isShort 是否是一个短的吐司
     */
    public static void toast(String txt, boolean isShort) {
        sToast.setText(txt);
        sToast.setDuration(isShort ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG);
        sToast.show();
    }

    /**
     * handler线程切换 到主线程，可以在子线程中调用该方法，进而切换到主线程，方便更新UI
     * @param runnable
     */
    public static void runUi(Runnable runnable) {
        sHandler.post(runnable);
    }
}
