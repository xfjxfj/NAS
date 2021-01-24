package com.viegre.nas.pad.activity.audio;

import android.net.Uri;
import android.view.View;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.UriUtils;
import com.djangoogle.framework.activity.BaseActivity;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.databinding.ActivityAudioPlayerBinding;

import nl.changer.audiowife.AudioWife;

/**
 * Created by レインマン on 2021/01/22 17:17 with Android Studio.
 */
public class AudioPlayerActivity extends BaseActivity<ActivityAudioPlayerBinding> {

	@Override
	protected void initialize() {
		mViewBinding.iAudioPlayerTitle.actvFileManagerTitle.setText(R.string.audio);
		mViewBinding.iAudioPlayerTitle.llcFileManagerTitleBack.setOnClickListener(view -> finish());
		mViewBinding.iAudioPlayerTitle.acivFileManagerFilter.setVisibility(View.GONE);
		Uri uri = UriUtils.file2Uri(FileUtils.getFileByPath(getIntent().getStringExtra("audioPath")));
		AudioWife.getInstance()
		         .init(this, uri)
		         .setPlayView(mViewBinding.acivAudioPlayerPlay)
		         .setPauseView(mViewBinding.acivAudioPlayerPause)
		         .setSeekBar(mViewBinding.acsbAudioPlayer)
		         .setRuntimeView(mViewBinding.actvAudioPlayerRunTime)
		         .setTotalTimeView(mViewBinding.actvAudioPlayerTotalTime);
	}
}
