package com.viegre.nas.pad.fragment.settings;

import android.media.AudioManager;
import android.widget.SeekBar;

import com.blankj.utilcode.util.VolumeUtils;
import com.djangoogle.framework.fragment.BaseFragment;
import com.viegre.nas.pad.databinding.FragmentSoundBinding;

/**
 * Created by レインマン on 2020/12/17 17:38 with Android Studio.
 */
public class SoundFragment extends BaseFragment<FragmentSoundBinding> {

	@Override
	protected void initialize() {
		int maxVolume = VolumeUtils.getMaxVolume(AudioManager.STREAM_MUSIC);
		mViewBinding.acsbSoundVolumeControl.setMax(maxVolume);
		int volume = VolumeUtils.getVolume(AudioManager.STREAM_MUSIC);
		mViewBinding.acsbSoundVolumeControl.setProgress(volume);
		mViewBinding.acsbSoundVolumeControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
				VolumeUtils.setVolume(AudioManager.STREAM_MUSIC, i, AudioManager.FLAG_PLAY_SOUND);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
	}

	public static SoundFragment newInstance() {
		return new SoundFragment();
	}
}
