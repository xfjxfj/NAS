package com.viegre.nas.speaker.manager;

import android.content.Context;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.blankj.utilcode.util.BusUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PhoneUtils;
import com.viegre.nas.speaker.config.BusConfig;
import com.viegre.nas.speaker.config.UrlConfig;
import com.viegre.nas.speaker.entity.WeatherRootEntity;
import com.yanzhenjie.kalle.Kalle;
import com.yanzhenjie.kalle.simple.SimpleCallback;
import com.yanzhenjie.kalle.simple.SimpleResponse;

/**
 * Created by Djangoogle on 2021/01/12 10:20 with Android Studio.
 */
public enum AMapLocationManager {

	INSTANCE;

	private AMapLocationClient mAMapLocationClient = null;

	public void initialize(Context applicationContext) {
		if (null == mAMapLocationClient) {
			mAMapLocationClient = new AMapLocationClient(applicationContext);
			mAMapLocationClient.setLocationListener(aMapLocation -> {
				if (null == aMapLocation) {
					BusUtils.post(BusConfig.WEATHER, null);
				} else {
					if (0 != aMapLocation.getErrorCode()) {
						//定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
						LogUtils.e("location Error, ErrCode:" + aMapLocation.getErrorCode() + ", errInfo:" + aMapLocation.getErrorInfo());
						BusUtils.post(BusConfig.WEATHER, null);
					} else {
						LogUtils.iTag("AmapTest", aMapLocation.getLatitude(), aMapLocation.getLongitude());
						Kalle.post(UrlConfig.DeviceConfig.GET_WEATHER)
						     .param("lat", aMapLocation.getLatitude())
						     .param("lng", aMapLocation.getLongitude())
						     .param("sn", PhoneUtils.getSerial())
						     .perform(new SimpleCallback<WeatherRootEntity>() {
							     @Override
							     public void onResponse(SimpleResponse<WeatherRootEntity, String> response) {
								     if (!response.isSucceed()) {
									     BusUtils.post(BusConfig.WEATHER, null);
								     } else {
									     BusUtils.post(BusConfig.WEATHER, response.succeed().getWeather().get(0));
								     }
							     }
						     });
					}
				}
			});
		}
	}

	/**
	 * 获取当前定位
	 */
	public void getLocation() {
		AMapLocationClientOption option = new AMapLocationClientOption();
		//设置定位场景，目前支持三种场景（签到、出行、运动，默认无场景）
		option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
		//设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
		option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
		//获取一次定位结果：
		//该方法默认为false。
		option.setOnceLocation(true);
		//设置是否允许模拟位置,默认为true，允许模拟位置
		option.setMockEnable(false);
		//单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
		option.setHttpTimeOut(10 * 1000L);
		if (null != mAMapLocationClient) {
			mAMapLocationClient.setLocationOption(option);
			//设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
			mAMapLocationClient.stopLocation();
			mAMapLocationClient.startLocation();
		}
	}
}
