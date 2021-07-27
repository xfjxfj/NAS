package com.viegre.nas.pad.interceptor;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.kongzue.dialog.interfaces.OnDialogButtonClickListener;
import com.kongzue.dialog.interfaces.OnDismissListener;
import com.kongzue.dialog.util.BaseDialog;
import com.kongzue.dialog.v3.MessageDialog;
import com.kongzue.dialog.v3.WaitDialog;
import com.viegre.nas.pad.activity.LoginActivity;
import com.viegre.nas.pad.activity.MainActivity;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.config.NasConfig;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.config.UrlConfig;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import rxhttp.RxHttpPlugins;

import static com.blankj.utilcode.util.ThreadUtils.runOnUiThread;

// token拦截器
public class TokenInterceptor implements Interceptor {

	private static final String TAG = "TokenInterceptor";

	@Override
	public Response intercept(Chain chain) throws IOException {
		Request request = chain.request();
		Response originalResponse = chain.proceed(request);
		MediaType mediaType = originalResponse.body().contentType();
		String content = originalResponse.body().string();
		try {
			JSONObject jsonObject = JSONObject.parseObject(content);
			String code = String.valueOf(jsonObject.get("code"));
			if (code.equals(NasConfig.TOKEN_FAILED)) {
				Log.e(TAG, "intercept: " + "token失效");
				Thread.sleep(500);
				if (request.url().toString().contains(UrlConfig.APP_SERVER + UrlConfig.User.USER)) {/// 用户接口失效判断
					handleTokenInvalid();
				} else if (request.url().toString().contains(UrlConfig.APP_SERVER + UrlConfig.Device.DEVICE)) {/// 设备接口失效判断
					EventBus.getDefault().post(BusConfig.DEVICE_TOKEN_UPDATE);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return originalResponse.newBuilder().body(ResponseBody.Companion.create(content, mediaType)).build();
	}

	/**
	 * token失效处理
	 */
	private void handleTokenInvalid() {
		ActivityUtils.finishToActivity(MainActivity.class, false);
		if (SPUtils.getInstance().getBoolean(SPConfig.LOGIN_ERROR_SHOW, false)) {
			showTips();
		}
	}

	/**
	 * 展示重新登录逻辑
	 */
	public static void showTips() {
		if (SPUtils.getInstance().getBoolean(SPConfig.LOGIN_ERROR_SHOW, false)) {
			SPUtils.getInstance().put(SPConfig.LOGIN_ERROR_SHOW, false);
			clearTokenInfo();
			MessageDialog.show((AppCompatActivity) ActivityUtils.getTopActivity(), "提示", "登录已经过期,请重新登录！", "确定")
			             .setOnOkButtonClickListener(new OnDialogButtonClickListener() {
				             @Override
				             public boolean onClick(BaseDialog baseDialog, View v) {
					             new Handler().postDelayed(new Runnable() {
						             @Override
						             public void run() {
							             runOnUiThread(new Runnable() {
								             @Override
								             public void run() {
									             ActivityUtils.startActivity(LoginActivity.class);
								             }
							             });
						             }
					             }, 300);
					             SPUtils.getInstance().put(SPConfig.LOGIN_ERROR_SHOW, true);
					             return false;
				             }
			             })
			             .setOnDismissListener(new OnDismissListener() {
				             @Override
				             public void onDismiss() {
					             SPUtils.getInstance().put(SPConfig.LOGIN_ERROR_SHOW, true);
				             }
			             })
			             .setButtonOrientation(LinearLayout.VERTICAL);
			EventBus.getDefault().post(BusConfig.USER_INFO_UPDATE);
		}
	}

	/**
	 * 登录信息持久化
	 *
	 * @param phone 登录手机号
	 * @param token token
	 */
	static public void saveTokenInfo(String phone, String token) {
		SPUtils.getInstance().put(SPConfig.PHONE, phone);
		SPUtils.getInstance().put(SPConfig.TOKEN, token);
	}

	/**
	 * 清空用户信息
	 */
	static public void clearTokenInfo() {
		//清空手机号
		SPUtils.getInstance().remove(SPConfig.PHONE);
		//清空token
		SPUtils.getInstance().remove(SPConfig.TOKEN);
		SPUtils.getInstance().put(SPConfig.TOKEN_TIME, "");
	}
}
