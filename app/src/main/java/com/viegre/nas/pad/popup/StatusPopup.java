package com.viegre.nas.pad.popup;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.WindowManager;
import android.widget.SeekBar;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.Utils;
import com.blankj.utilcode.util.VolumeUtils;
import com.lxj.xpopup.impl.FullScreenPopupView;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.activity.MainActivity;
import com.viegre.nas.pad.activity.SettingsActivity;
import com.viegre.nas.pad.databinding.PopupStatusBinding;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * Created by レインマン on 2021/07/29 14:13 with Android Studio.
 */
public class StatusPopup extends FullScreenPopupView {

	public StatusPopup(@NonNull Context context) {
		super(context);
	}

	@Override
	protected int getImplLayoutId() {
		return R.layout.popup_status;
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	protected void onCreate() {
		super.onCreate();
		PopupStatusBinding popupStatusBinding = PopupStatusBinding.bind(getPopupImplView());

		popupStatusBinding.vStatusDismiss.setOnClickListener(view -> dismiss());
		popupStatusBinding.rlStatusHome.setOnClickListener(view -> ActivityUtils.startActivity(MainActivity.class));
		popupStatusBinding.rlStatusSettings.setOnClickListener(view -> ActivityUtils.startActivity(SettingsActivity.class));
		Bundle bundle = new Bundle();
		bundle.putString("WiFi", "WiFi");
		popupStatusBinding.rlStatusWiFi.setOnClickListener(view -> ActivityUtils.startActivity(MainActivity.class, bundle));

		try {
			int brightness = Settings.System.getInt(Utils.getApp().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
			if (brightness >= 0) {
				popupStatusBinding.acsbStatusBrightness.setProgress(brightness);
			}
		} catch (Settings.SettingNotFoundException e) {
			e.printStackTrace();
		}
		popupStatusBinding.acsbStatusBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
				setScreenBrightness(seekBar.getProgress());
				setImageRes(i, 255, popupStatusBinding.acivStatusBrightness);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});

		int maxVolume = VolumeUtils.getMaxVolume(AudioManager.STREAM_MUSIC);
		popupStatusBinding.acsbStatusVolume.setMax(maxVolume);
		int volume = VolumeUtils.getVolume(AudioManager.STREAM_MUSIC);
		popupStatusBinding.acsbStatusVolume.setProgress(volume);
		popupStatusBinding.acsbStatusVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
				VolumeUtils.setVolume(AudioManager.STREAM_MUSIC, i, AudioManager.FLAG_PLAY_SOUND);
				VolumeUtils.setVolume(AudioManager.STREAM_ALARM, i, AudioManager.FLAG_PLAY_SOUND);
				setImageRes(i, maxVolume, popupStatusBinding.acivStatusVolume);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
	}

	/**
	 * 设置屏幕的亮度
	 */
	private void setScreenBrightness(int process) {
		//设置当前窗口的亮度值
		WindowManager.LayoutParams localLayoutParams = ActivityUtils.getTopActivity().getWindow().getAttributes();
		localLayoutParams.screenBrightness = process / 255.0F;
		ActivityUtils.getTopActivity().getWindow().setAttributes(localLayoutParams);
		//修改系统的亮度值,以至于退出应用程序亮度保持
		saveBrightness(Utils.getApp().getContentResolver(), process);
	}

	private void saveBrightness(ContentResolver resolver, int brightness) {
		//改变系统的亮度值
		//设置为手动调节模式
		Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
		//保存到系统中
		Uri uri = android.provider.Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
		android.provider.Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
		resolver.notifyChange(uri, null);
	}

	private void setImageRes(int progress, int max, AppCompatImageView appCompatImageView) {
		boolean isBrightness = R.id.acivStatusBrightness == appCompatImageView.getId();
		if (progress <= max / 3) {
			appCompatImageView.setImageResource(isBrightness ? R.mipmap.status_brightness_0 : R.mipmap.status_volume_0);
		} else if (progress > max / 3 && progress < max * 2 / 3) {
			appCompatImageView.setImageResource(isBrightness ? R.mipmap.status_brightness_1 : R.mipmap.status_volume_1);
		} else {
			appCompatImageView.setImageResource(isBrightness ? R.mipmap.status_brightness_2 : R.mipmap.status_volume_2);
		}
	}
}
