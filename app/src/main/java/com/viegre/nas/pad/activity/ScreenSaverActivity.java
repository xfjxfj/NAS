package com.viegre.nas.pad.activity;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;

import com.bumptech.glide.Glide;
import com.djangoogle.framework.activity.BaseActivity;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.databinding.ActivityScreenSaverBinding;
import com.youth.banner.Banner;
import com.youth.banner.adapter.BannerImageAdapter;
import com.youth.banner.holder.BannerImageHolder;

import java.util.ArrayList;
import java.util.List;

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
		List<String> bannerList = new ArrayList<>();
		bannerList.add("https://pic1.zhimg.com/c7ad985268e7144b588d7bf94eedb487_r.jpg?source=1940ef5c");
		bannerList.add("https://pic1.zhimg.com/v2-3ff3d6a85edb2f19d343668d24ed9269_r.jpg?source=1940ef5c");
		bannerList.add("https://pic3.zhimg.com/v2-3fcdfeacc10696e3f71d66a9ba6e9cc4_r.jpg?source=1940ef5c");
		bannerList.add("https://pic2.zhimg.com/v2-73b8307b2db44c617f4e8515ce67dd39_r.jpg?source=1940ef5c");
		bannerList.add("https://pic2.zhimg.com/v2-f85f658e4f785d48cf04dd8f47acc6fa_r.jpg?source=1940ef5c");
		bannerList.add("https://pic4.zhimg.com/v2-e5427c1e9ad8aaad99d643e7bd7e927b_r.jpg?source=1940ef5c");
		bannerList.add("https://pic2.zhimg.com/v2-d024c6ad6851b266e8509d1aa0948ceb_r.jpg?source=1940ef5c");
		Banner<String, BannerImageAdapter<String>> bMainBanner = findViewById(R.id.bScreenSaverBanner);
		bMainBanner.setAdapter(new BannerImageAdapter<String>(bannerList) {
			@Override
			public void onBindView(BannerImageHolder holder, String data, int position, int size) {
				Glide.with(holder.itemView).load(data).into(holder.imageView);
			}
		}).addBannerLifecycleObserver(this).setLoopTime(5 * 1000L).setUserInputEnabled(false);
	}

	@Override
	public void onBackPressed() {
		finish();
	}
}
