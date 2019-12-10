package androidalldemo.jan.jason.bluedemo.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import androidalldemo.jan.jason.bluedemo.App;


/**
 * Description: 弹出提示
 * *
 * Creator: Wang
 * Date: 2019/5/26 19:22
 */
public class ToastUtils {

    private static Context context = App.getInstance();

    private static Toast toast;

    public static void show(int resId) {
        show(context.getResources().getText(resId), Toast.LENGTH_SHORT);
    }

    public static void show(int resId, int duration) {
        show(context.getResources().getText(resId), duration);
    }

    public static void show(CharSequence text) {
        show(text, Toast.LENGTH_SHORT);
    }

    public static void show(CharSequence text, int duration) {
        try{
            if (context == null) {
                throw new RuntimeException("sorry context == null");
            }

            text = TextUtils.isEmpty(text == null ? "" : text.toString()) ? "nothing" : text;
            if (toast == null) {
                toast = Toast.makeText(context, text.toString(), duration);
            } else {
                toast.setText(text);
            }
            toast.show();
        }catch (Throwable e){
           LogUtils.d("","Error##"+e.getMessage());
        }
    }

    public static void show(int resId, Object... args) {
        show(String.format(context.getResources().getString(resId), args),
                Toast.LENGTH_SHORT);
    }

    public static void show(String format, Object... args) {
        show(String.format(format, args), Toast.LENGTH_SHORT);
    }

    public static void show(int resId, int duration, Object... args) {
        show(String.format(context.getResources().getString(resId), args),
                duration);
    }

    public static void show(String format, int duration, Object... args) {
        show(String.format(format, args), duration);
    }
}
