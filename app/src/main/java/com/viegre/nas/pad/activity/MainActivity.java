package com.viegre.nas.pad.activity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.view.View;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BusUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.djangoogle.framework.activity.BaseActivity;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.activity.audio.AudioActivity;
import com.viegre.nas.pad.activity.image.ImageActivity;
import com.viegre.nas.pad.activity.video.VideoActivity;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.databinding.ActivityMainBinding;
import com.viegre.nas.pad.entity.LoginInfoEntity;
import com.viegre.nas.pad.entity.WeatherEntity;
import com.viegre.nas.pad.manager.AMapLocationManager;
import com.viegre.nas.pad.util.CommonUtils;
import com.youth.banner.Banner;
import com.youth.banner.adapter.BannerImageAdapter;
import com.youth.banner.holder.BannerImageHolder;
import com.youth.banner.indicator.CircleIndicator;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hdp.http.APIConstant;

/**
 * Created by レインマン on 2020/12/15 09:29 with Android Studio.
 */
public class MainActivity extends BaseActivity<ActivityMainBinding> {

	private final Map<String, Integer> mWeatherMap = new HashMap<>();

	@Override
	protected void initialize() {
		initIcon();
		initBanner();
		initWeather();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		initUser();
		AMapLocationManager.INSTANCE.getLocation();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * 初始化用户区域
	 */
	private void initUser() {
		ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<LoginInfoEntity>() {
			@Override
			public LoginInfoEntity doInBackground() {
				return LitePal.findFirst(LoginInfoEntity.class);
			}

			@Override
			public void onSuccess(LoginInfoEntity result) {
				if (null == result) {
					Glide.with(mActivity)
					     .load(R.mipmap.main_unlogin)
					     .apply(RequestOptions.bitmapTransform(new CircleCrop()))
					     .into(mViewBinding.acivMainUserIcon);
					mViewBinding.actvMainUserInfo.setText(R.string.main_click_to_login);
					mViewBinding.llcMainUser.setOnClickListener(view -> ActivityUtils.startActivity(LoginActivity.class));
				} else {
					Glide.with(mActivity)
					     .load(R.mipmap.main_unlogin)
					     .apply(RequestOptions.bitmapTransform(new CircleCrop()))
					     .into(mViewBinding.acivMainUserIcon);
					mViewBinding.actvMainUserInfo.setText(CommonUtils.getMarkedPhoneNumber(result.getPhoneNumber()));
					mViewBinding.llcMainUser.setOnClickListener(null);
				}
				mViewBinding.llcMainUser.setVisibility(View.VISIBLE);
			}
		});
	}

	private void initIcon() {
		//图片
		Glide.with(this)
		     .load(R.mipmap.main_icon_image)
		     .apply(RequestOptions.bitmapTransform(new RoundedCorners(24)))
		     .into(mViewBinding.acivMainIconImage);
		mViewBinding.acivMainIconImage.setOnClickListener(view -> ActivityUtils.startActivity(ImageActivity.class));

		//音频
		Glide.with(this)
		     .load(R.mipmap.main_icon_audio)
		     .apply(RequestOptions.bitmapTransform(new RoundedCorners(24)))
		     .into(mViewBinding.acivMainIconAudio);
		mViewBinding.acivMainIconAudio.setOnClickListener(view -> ActivityUtils.startActivity(AudioActivity.class));

		//视频
		Glide.with(this)
		     .load(R.mipmap.main_icon_video)
		     .apply(RequestOptions.bitmapTransform(new RoundedCorners(24)))
		     .into(mViewBinding.acivMainIconVideo);
		mViewBinding.acivMainIconVideo.setOnClickListener(view -> ActivityUtils.startActivity(VideoActivity.class));

		Glide.with(this).load(R.mipmap.test_icon_3).apply(RequestOptions.bitmapTransform(new RoundedCorners(24))).into(mViewBinding.acivMainIcon3);
		Glide.with(this).load(R.mipmap.test_icon_4).apply(RequestOptions.bitmapTransform(new RoundedCorners(24))).into(mViewBinding.acivMainIcon4);
		Glide.with(this).load(R.mipmap.test_icon_5).apply(RequestOptions.bitmapTransform(new RoundedCorners(24))).into(mViewBinding.acivMainIcon5);
		Glide.with(this).load(R.mipmap.test_icon_6).apply(RequestOptions.bitmapTransform(new RoundedCorners(24))).into(mViewBinding.acivMainIcon6);
		Glide.with(this).load(R.mipmap.test_icon_7).apply(RequestOptions.bitmapTransform(new RoundedCorners(24))).into(mViewBinding.acivMainIcon7);
		Glide.with(this).load(R.mipmap.test_icon_8).apply(RequestOptions.bitmapTransform(new RoundedCorners(24))).into(mViewBinding.acivMainIcon8);
		mViewBinding.acivMainIcon5.setOnClickListener(view -> {
			Intent liveIntent = new Intent();
			liveIntent.putExtra(APIConstant.HIDE_LOADING_DEFAULT, true);
			liveIntent.putExtra(APIConstant.HIDE_EXIT_DIAG, true);
			liveIntent.setAction("com.hdpfans.live.start");
			liveIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			liveIntent.putExtra("ChannelNum", 1);
			ActivityUtils.startActivity(liveIntent);
		});
		mViewBinding.acivMainIcon8.setOnClickListener(view -> ActivityUtils.startActivity(SettingsActivity.class));
	}

	private void initBanner() {
		List<String> bannerList = new ArrayList<>();
		bannerList.add("https://pic1.zhimg.com/c7ad985268e7144b588d7bf94eedb487_r.jpg?source=1940ef5c");
		bannerList.add("https://pic1.zhimg.com/v2-3ff3d6a85edb2f19d343668d24ed9269_r.jpg?source=1940ef5c");
		bannerList.add("https://pic3.zhimg.com/v2-3fcdfeacc10696e3f71d66a9ba6e9cc4_r.jpg?source=1940ef5c");
		bannerList.add("https://pic2.zhimg.com/v2-73b8307b2db44c617f4e8515ce67dd39_r.jpg?source=1940ef5c");
		bannerList.add("https://pic2.zhimg.com/v2-f85f658e4f785d48cf04dd8f47acc6fa_r.jpg?source=1940ef5c");
		bannerList.add("https://pic4.zhimg.com/v2-e5427c1e9ad8aaad99d643e7bd7e927b_r.jpg?source=1940ef5c");
		bannerList.add("https://pic2.zhimg.com/v2-d024c6ad6851b266e8509d1aa0948ceb_r.jpg?source=1940ef5c");
		Banner<String, BannerImageAdapter<String>> bMainBanner = findViewById(R.id.bMainBanner);
		bMainBanner.setAdapter(new BannerImageAdapter<String>(bannerList) {
			@Override
			public void onBindView(BannerImageHolder holder, String data, int position, int size) {
				Glide.with(holder.itemView).load(data).into(holder.imageView);
			}
		}).addBannerLifecycleObserver(this).setBannerRound2(16F).setIndicator(new CircleIndicator(this));
	}

	/**
	 * 初始化天气
	 */
	private void initWeather() {
		mViewBinding.llcMainWeather.setOnClickListener(view -> AMapLocationManager.INSTANCE.getLocation());
		ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<HashMap<String, Integer>>() {
			@Override
			public HashMap<String, Integer> doInBackground() {
				String[] weatherNameArr = getResources().getStringArray(R.array.main_weather_name);
				TypedArray typedArray = getResources().obtainTypedArray(R.array.main_weather_icon);
				HashMap<String, Integer> weatherMap = new HashMap<>();
				for (int i = 0; i < weatherNameArr.length; i++) {
					weatherMap.put(weatherNameArr[i], typedArray.getResourceId(i, 0));
				}
				typedArray.recycle();
				return weatherMap;
			}

			@Override
			public void onSuccess(HashMap<String, Integer> result) {
				mWeatherMap.putAll(result);
				AMapLocationManager.INSTANCE.getLocation();
			}
		});
	}

	@BusUtils.Bus(tag = BusConfig.WEATHER, threadMode = BusUtils.ThreadMode.MAIN)
	public void getWeather(WeatherEntity weatherEntity) {
		if (null != weatherEntity && !mWeatherMap.isEmpty()) {
			for (Map.Entry<String, Integer> entry : mWeatherMap.entrySet()) {
				String name = entry.getKey();
				if (name.contains(weatherEntity.getWeather())) {
					mViewBinding.acivMainWeather.setImageResource(entry.getValue());
					mViewBinding.actvMainTemperature.setText(weatherEntity.getCurtemperature() + StringUtils.getString(R.string.weather_temperature));
					return;
				}
			}
		}
		mViewBinding.acivMainWeather.setImageResource(R.mipmap.weather_unknown);
		mViewBinding.actvMainTemperature.setText(R.string.weather_unknown_temperature);
	}
}
