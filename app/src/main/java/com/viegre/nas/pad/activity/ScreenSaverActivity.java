package com.viegre.nas.pad.activity;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.blankj.utilcode.util.SPUtils;
import com.bumptech.glide.Glide;
import com.djangoogle.framework.activity.BaseActivity;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.databinding.ActivityScreenSaverBinding;
import com.youth.banner.Banner;
import com.youth.banner.adapter.BannerImageAdapter;
import com.youth.banner.holder.BannerImageHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by レインマン on 2021/03/09 17:35 with Android Studio.
 */
public class ScreenSaverActivity extends BaseActivity<ActivityScreenSaverBinding> {

	private PowerManager.WakeLock mWakeLock;
	private KeyguardManager.KeyguardLock mKeyguardLock;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	protected void initialize() {
		initWakeLock();
		mViewBinding.getRoot().setOnTouchListener((view, motionEvent) -> {
			mKeyguardLock.disableKeyguard();
			finish();
			return false;
		});
		initBanner();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		initBanner();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (null != mWakeLock) {
			mWakeLock.acquire();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (null != mWakeLock && mWakeLock.isHeld()) {
			mWakeLock.release();
		}
	}

	@SuppressLint("WakelockTimeout")
	private void initWakeLock() {
		PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
		mWakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,
		                                     "nas:screensaver");
		KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
		mKeyguardLock = keyguardManager.newKeyguardLock("nas:keyguardLocker");
	}

	private void initBanner() {
		Set<String> images = SPUtils.getInstance().getStringSet(SPConfig.SCREEN_SAVER_CUSTOM_IMAGES, new TreeSet<>());
		if (images.isEmpty()) {
			List<Integer> bannerList = new ArrayList<>();
			bannerList.add(R.mipmap.screen_standby_image_default_1);
			bannerList.add(R.mipmap.screen_standby_image_default_2);
			bannerList.add(R.mipmap.screen_standby_image_default_3);
			bannerList.add(R.mipmap.screen_standby_image_default_4);
			Banner<Integer, BannerImageAdapter<Integer>> bMainBanner = findViewById(R.id.bScreenSaverBanner);
			bMainBanner.setAdapter(new BannerImageAdapter<Integer>(bannerList) {
				@Override
				public void onBindView(BannerImageHolder holder, Integer data, int position, int size) {
					Glide.with(holder.itemView).load(data).into(holder.imageView);
				}
			}).addBannerLifecycleObserver(this).setLoopTime(5 * 1000L).setUserInputEnabled(false);
		} else {
			List<String> bannerList = new ArrayList<>(images);
			Banner<String, BannerImageAdapter<String>> bMainBanner = findViewById(R.id.bScreenSaverBanner);
			bMainBanner.setAdapter(new BannerImageAdapter<String>(bannerList) {
				@Override
				public void onBindView(BannerImageHolder holder, String data, int position, int size) {
					Glide.with(holder.itemView).load(data).into(holder.imageView);
				}
			}).addBannerLifecycleObserver(this).setLoopTime(5 * 1000L).setUserInputEnabled(false);
		}
	}

	@Override
	public void onBackPressed() {
		finish();
	}
}
