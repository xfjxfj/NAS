package com.viegre.nas.pad.activity.audio;

import android.content.Intent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.UriUtils;
import com.bumptech.glide.Glide;
import com.djangoogle.framework.activity.BaseActivity;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.databinding.ActivityAudioPlayerBinding;
import com.viegre.nas.pad.manager.AudioPlayListManager;

import nl.changer.audiowife.AudioWife;

/**
 * Created by レインマン on 2021/01/22 17:17 with Android Studio.
 */
public class AudioPlayerActivity extends BaseActivity<ActivityAudioPlayerBinding> {

	private Animation mAlbumAnimation;

	@Override
	protected void initialize() {
		mViewBinding.iAudioPlayerTitle.llcFileManagerTitleBack.setOnClickListener(view -> finish());
		mViewBinding.iAudioPlayerTitle.acivFileManagerFilter.setVisibility(View.GONE);
		AudioWife.getInstance().addOnPlayClickListener(view -> mViewBinding.acivAudioPlayerAlbum.startAnimation(mAlbumAnimation));
		AudioWife.getInstance().addOnPauseClickListener(view -> mViewBinding.acivAudioPlayerAlbum.clearAnimation());
		AudioWife.getInstance().addOnCompletionListener(mediaPlayer -> playNext());
		mViewBinding.acivAudioPlayerPrevious.setOnClickListener(view -> playPrevious());
		mViewBinding.acivAudioPlayerNext.setOnClickListener(view -> playNext());
		initAlbumAnim();
		init(getIntent().getIntExtra("position", 0));
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		int position = intent.getIntExtra("position", 0);
		if (AudioPlayListManager.INSTANCE.getPosition() == position) {
			return;
		}
		init(intent.getIntExtra("position", 0));
	}

	/**
	 * 初始化专辑动画
	 */
	private void initAlbumAnim() {
		mAlbumAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.audio_player_album_anim);
		LinearInterpolator li = new LinearInterpolator();
		mAlbumAnimation.setInterpolator(li);
	}

	private void init(int position) {
		AudioPlayListManager.INSTANCE.setPosition(position);
		mViewBinding.iAudioPlayerTitle.actvFileManagerTitle.setText(AudioPlayListManager.INSTANCE.getList()
		                                                                                         .get(AudioPlayListManager.INSTANCE
				                                                                                              .getPosition())
		                                                                                         .getName());
		AudioWife.getInstance().release();
		AudioWife.getInstance()
		         .init(this,
		               UriUtils.file2Uri(FileUtils.getFileByPath(AudioPlayListManager.INSTANCE.getList()
		                                                                                      .get(AudioPlayListManager.INSTANCE.getPosition())
		                                                                                      .getPath())))
		         .setPlayView(mViewBinding.acivAudioPlayerPlay)
		         .setPauseView(mViewBinding.acivAudioPlayerPause)
		         .setSeekBar(mViewBinding.acsbAudioPlayer)
		         .setRuntimeView(mViewBinding.actvAudioPlayerRunTime)
		         .setTotalTimeView(mViewBinding.actvAudioPlayerTotalTime)
		         .play();
		Glide.with(this)
		     .load(AudioPlayListManager.INSTANCE.getList().get(AudioPlayListManager.INSTANCE.getPosition()).getAlbumImage())
		     .circleCrop()
		     .placeholder(R.mipmap.audio_player_album_default)
		     .fallback(R.mipmap.audio_player_album_default)
		     .error(R.mipmap.audio_player_album_default)
		     .into(mViewBinding.acivAudioPlayerAlbum);
		mViewBinding.acivAudioPlayerAlbum.startAnimation(mAlbumAnimation);
	}

	/**
	 * 播放上一首
	 */
	private void playPrevious() {
		if (AudioPlayListManager.INSTANCE.getPosition() <= 0) {
			AudioPlayListManager.INSTANCE.setPosition(AudioPlayListManager.INSTANCE.getList().size() - 1);
		} else {
			AudioPlayListManager.INSTANCE.cutPostion();
		}
		mViewBinding.iAudioPlayerTitle.actvFileManagerTitle.setText(AudioPlayListManager.INSTANCE.getList()
		                                                                                         .get(AudioPlayListManager.INSTANCE
				                                                                                              .getPosition())
		                                                                                         .getName());
		AudioWife.getInstance().release();
		AudioWife.getInstance()
		         .init(this,
		               UriUtils.file2Uri(FileUtils.getFileByPath(AudioPlayListManager.INSTANCE.getList()
		                                                                                      .get(AudioPlayListManager.INSTANCE.getPosition())
		                                                                                      .getPath())))
		         .setSeekBar(mViewBinding.acsbAudioPlayer)
		         .setRuntimeView(mViewBinding.actvAudioPlayerRunTime)
		         .setTotalTimeView(mViewBinding.actvAudioPlayerTotalTime)
		         .play();
		Glide.with(this)
		     .load(AudioPlayListManager.INSTANCE.getList().get(AudioPlayListManager.INSTANCE.getPosition()).getAlbumImage())
		     .circleCrop()
		     .placeholder(R.mipmap.audio_player_album_default)
		     .fallback(R.mipmap.audio_player_album_default)
		     .error(R.mipmap.audio_player_album_default)
		     .into(mViewBinding.acivAudioPlayerAlbum);
		mViewBinding.acivAudioPlayerAlbum.startAnimation(mAlbumAnimation);
	}

	/**
	 * 播放下一首
	 */
	private void playNext() {
		if (AudioPlayListManager.INSTANCE.getPosition() >= AudioPlayListManager.INSTANCE.getList().size() - 1) {
			AudioPlayListManager.INSTANCE.setPosition(0);
		} else {
			AudioPlayListManager.INSTANCE.addPostion();
		}
		mViewBinding.iAudioPlayerTitle.actvFileManagerTitle.setText(AudioPlayListManager.INSTANCE.getList()
		                                                                                         .get(AudioPlayListManager.INSTANCE
				                                                                                              .getPosition())
		                                                                                         .getName());
		AudioWife.getInstance().release();
		AudioWife.getInstance()
		         .init(this,
		               UriUtils.file2Uri(FileUtils.getFileByPath(AudioPlayListManager.INSTANCE.getList()
		                                                                                      .get(AudioPlayListManager.INSTANCE.getPosition())
		                                                                                      .getPath())))
		         .setSeekBar(mViewBinding.acsbAudioPlayer)
		         .setRuntimeView(mViewBinding.actvAudioPlayerRunTime)
		         .setTotalTimeView(mViewBinding.actvAudioPlayerTotalTime)
		         .play();
		Glide.with(this)
		     .load(AudioPlayListManager.INSTANCE.getList().get(AudioPlayListManager.INSTANCE.getPosition()).getAlbumImage())
		     .circleCrop()
		     .placeholder(R.mipmap.audio_player_album_default)
		     .fallback(R.mipmap.audio_player_album_default)
		     .error(R.mipmap.audio_player_album_default)
		     .into(mViewBinding.acivAudioPlayerAlbum);
		mViewBinding.acivAudioPlayerAlbum.startAnimation(mAlbumAnimation);
	}

	@Override
	public void finish() {
		//退到后台
		moveTaskToBack(true);
	}
}
