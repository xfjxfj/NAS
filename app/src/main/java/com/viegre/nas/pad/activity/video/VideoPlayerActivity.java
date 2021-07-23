package com.viegre.nas.pad.activity.video;

import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.djangoogle.framework.activity.BaseActivity;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.databinding.ActivityVideoPlayerBinding;
import com.viegre.nas.pad.entity.VideoEntity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by レインマン on 2021/01/27 10:50 with Android Studio.
 */
public class VideoPlayerActivity extends BaseActivity<ActivityVideoPlayerBinding> {

	private final List<VideoEntity> mVideoList = new ArrayList<>();
	private int mIndex;

	@Override
	protected void initialize() {
		mVideoList.addAll(JSON.parseArray(getIntent().getStringExtra("videoListJson"), VideoEntity.class));
		mIndex = getIntent().getIntExtra("index", 0);
		mViewBinding.nvpVideoPlayer.setShowControl(mVideoList.size() > 1);
		mViewBinding.nvpVideoPlayer.setUp(mVideoList.get(mIndex).getPath(), true, mVideoList.get(mIndex).getName());
		mViewBinding.nvpVideoPlayer.getBackButton().setImageResource(R.mipmap.file_manager_back);
		mViewBinding.nvpVideoPlayer.getTitleTextView().setTextSize(TypedValue.COMPLEX_UNIT_PX, 32F);
		mViewBinding.nvpVideoPlayer.getTitleTextView().getPaint().setFakeBoldText(true);
		mViewBinding.nvpVideoPlayer.getTitleTextView().setSingleLine(true);
		mViewBinding.nvpVideoPlayer.getTitleTextView().setEllipsize(TextUtils.TruncateAt.END);
		mViewBinding.nvpVideoPlayer.getTitleTextView().setPadding(25, 0, 0, 0);
		mViewBinding.nvpVideoPlayer.getTitleTextView().setIncludeFontPadding(false);
		LinearLayout topContainer = (LinearLayout) mViewBinding.nvpVideoPlayer.getBackButton().getParent();
		RelativeLayout.LayoutParams topContainerParams = (RelativeLayout.LayoutParams) topContainer.getLayoutParams();
		topContainerParams.topMargin = 16;
		topContainer.setLayoutParams(topContainerParams);
		mViewBinding.nvpVideoPlayer.getBackButton().setOnClickListener(view -> finish());
		mViewBinding.nvpVideoPlayer.getFullscreenButton().setVisibility(View.GONE);
		mViewBinding.nvpVideoPlayer.setAutoFullWithSize(true);
		mViewBinding.nvpVideoPlayer.setShowFullAnimation(true);
		mViewBinding.nvpVideoPlayer.setVideoAllCallBack(new GSYSampleCallBack() {
			@Override
			public void onComplete(String url, Object... objects) {
				super.onComplete(url, objects);
				if (mVideoList.size() == 1) {
					finish();
				} else {
					playNext();
				}
			}
		});
		mViewBinding.nvpVideoPlayer.startPlayLogic();
	}

	@Override
	protected void onResume() {
		super.onResume();
		GSYVideoManager.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		GSYVideoManager.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		GSYVideoManager.releaseAllVideos();
	}

	private void playPrevious() {
		mIndex--;
		if (mIndex < 0) {
			mIndex = mVideoList.size() - 1;
			mViewBinding.nvpVideoPlayer.setUp(mVideoList.get(mIndex).getPath(), true, mVideoList.get(mIndex).getName());
			mViewBinding.nvpVideoPlayer.startPlayLogic();
		}
	}

	private void playNext() {
		mIndex++;
		if (mIndex == mVideoList.size() - 1) {
			mIndex = 0;
			mViewBinding.nvpVideoPlayer.setUp(mVideoList.get(mIndex).getPath(), true, mVideoList.get(mIndex).getName());
			mViewBinding.nvpVideoPlayer.startPlayLogic();
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onVideoPlayControl(String event) {
		if (BusConfig.VIDEO_PLAY_PREVIOUS.equals(event)) {
			playPrevious();
		} else if (BusConfig.VIDEO_PLAY_NEXT.equals(event)) {
			playNext();
		}
	}
}
