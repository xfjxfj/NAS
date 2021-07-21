package com.viegre.nas.pad.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Handler;
import android.util.Base64;
import android.view.Gravity;
import android.view.WindowManager;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.kongzue.dialog.interfaces.OnDismissListener;
import com.kongzue.dialog.v3.TipDialog;
import com.viegre.nas.pad.R;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.Temporal;
import java.util.TimeZone;

import static com.blankj.utilcode.util.ViewUtils.runOnUiThread;

/**
 * Created by レインマン on 2021/01/08 10:54 with Android Studio.
 */
public class CommonUtils {

    public static final long DEFAULT_SPLASH_GUIDE_DURATION = 5 * 1000L;

    /**
     * 获取毫秒转换成时分秒
     * @param seconds
     * @return
     */
    public static String getDateFormatFromMilliSecond(long seconds) {
        //初始化format格式
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        //设置时区，跳过此步骤会默认设置为"GMT+08:00" 得到的结果会多出来8个小时
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        String time = dateFormat.format(seconds);
        return time;
    }
    /**
     * 计算时间间隔
     *
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static long DurationData(Temporal startTime, Temporal endTime) {
        Duration duration = Duration.between(startTime, endTime);
        return duration.toHours();
    }

    public static String getFileName() {
        StackTraceElement ste = new Throwable().getStackTrace()[1];
        return ste.getFileName();
    }

    public static String getLineNumber() {
        StackTraceElement ste = new Throwable().getStackTrace()[1];
        return String.valueOf(ste.getLineNumber());
    }

    /**
     * Base64字符串转换成图片
     *
     * @param string
     * @return
     */
    public static Bitmap stringToBitmap(String string) {
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray = Base64.decode(string, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 标记手机号中间4位为*
     *
     * @param phoneNumber
     * @return
     */
    public static String getMarkedPhoneNumber(String phoneNumber) {
        String start = phoneNumber.substring(0, 3);
        String end = phoneNumber.substring(phoneNumber.length() - 4);
        return start + "****" + end;
    }

    /**
     * 调节activity透明度
     *
     * @param activity
     * @param bgAlpha  0.3f
     */
    public static void setBackgroundAlpha(Activity activity, float bgAlpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        activity.getWindow().setAttributes(lp);
    }

    /**
     * 弹出普通Toast
     *
     * @param msg
     */
    public static void showToast(String msg) {
        ToastUtils.make().setTextColor(Color.BLACK).show(msg);
    }

    public static void showSuccessDialog(AppCompatActivity context, String msg) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TipDialog.show(context, msg, TipDialog.TYPE.SUCCESS).setOnDismissListener(new OnDismissListener() {
                            @Override
                            public void onDismiss() {

                            }
                        });
                    }
                });
            }
        }, 1000);
    }

    public static void showErrorDialog(AppCompatActivity context, String msg) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TipDialog.show(context, msg, TipDialog.TYPE.ERROR).setOnDismissListener(new OnDismissListener() {
                            @Override
                            public void onDismiss() {

                            }
                        });
                    }
                });
            }
        }, 1000);
    }

    /**
     * 弹出失败Toast
     *
     * @param msg
     */
    public static void showErrorToast(String msg) {
        ToastUtils.make()
                .setGravity(Gravity.CENTER, 0, 0)
                .setBgResource(R.drawable.login_error_toast_bg)
                .setTextColor(Color.WHITE)
                .setTextSize(30)
                .show(msg);
    }

    /**
     * 弹出失败Toast
     *
     * @param id
     */
    public static void showErrorToast(int id) {
        showErrorToast(StringUtils.getString(id));
    }

    /**
     * 获取本地视频总时长
     *
     * @param path
     * @return
     */
    public static long getLocalVideoDuration(String path) {
        long duration = DEFAULT_SPLASH_GUIDE_DURATION;
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(path);
            duration = Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return duration;
    }

    public static String sqliteEscape(String keyWord) {
        keyWord = keyWord.replace("/", "//")
                .replace("'", "''")
                .replace("[", "/[")
                .replace("]", "/]")
                .replace("%", "/%")
                .replace("&", "/&")
                .replace("_", "/_")
                .replace("(", "/(")
                .replace(")", "/)");
        return keyWord;
    }
}
