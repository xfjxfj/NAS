package com.viegre.nas.pad.manager;

import android.content.Context;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.config.UrlConfig;
import com.viegre.nas.pad.entity.WeatherEntity;
import com.viegre.nas.pad.entity.WeatherRootEntity;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import rxhttp.RxHttp;

/**
 * Created by レインマン on 2021/01/12 10:20 with Android Studio.
 */
public enum AMapLocationManager {

	INSTANCE;

	private AMapLocationClient mAMapLocationClient = null;

	public void initialize(Context applicationContext) {
		if (null == mAMapLocationClient) {
			mAMapLocationClient = new AMapLocationClient(applicationContext);
			mAMapLocationClient.setLocationListener(aMapLocation -> {
				if (null == aMapLocation) {
					EventBus.getDefault().post(new WeatherEntity());
				} else {
					if (0 != aMapLocation.getErrorCode()) {
						//定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
						LogUtils.e("location Error, ErrCode:" + aMapLocation.getErrorCode() + ", errInfo:" + aMapLocation.getErrorInfo());
						EventBus.getDefault().post(new WeatherEntity());
					} else {
						RxHttp.postForm(UrlConfig.Device.GET_WEATHER)
						      .setAssemblyEnabled(false)
						      .add("lat", aMapLocation.getLatitude())
						      .add("lng", aMapLocation.getLongitude())
						      .add("sn", SPUtils.getInstance().getString(SPConfig.ANDROID_ID))
						      .asResponse(WeatherRootEntity.class)
						      .subscribe(new Observer<WeatherRootEntity>() {
							      @Override
							      public void onSubscribe(@NonNull Disposable d) {}

							      @Override
							      public void onNext(@NonNull WeatherRootEntity weatherRootEntity) {
								      List<WeatherEntity> weatherList = weatherRootEntity.getWeather();
								      if (!weatherList.isEmpty()) {
									      for (WeatherEntity weather : weatherList) {
										      if (TimeUtils.isToday(weather.getDate(),
										                            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()))) {
											      EventBus.getDefault().post(weather);
											      return;
										      }
									      }
								      }
								      EventBus.getDefault().post(new WeatherEntity());
							      }

							      @Override
							      public void onError(@NonNull Throwable e) {
								      e.printStackTrace();
								      EventBus.getDefault().post(new WeatherEntity());
							      }

							      @Override
							      public void onComplete() {}
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
