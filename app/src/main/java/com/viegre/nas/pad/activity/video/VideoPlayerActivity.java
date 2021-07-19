package com.viegre.nas.pad.activity.video;

import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.djangoogle.framework.activity.BaseActivity;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.databinding.ActivityVideoPlayerBinding;
import com.viegre.nas.pad.entity.VideoEntity;

/**
 * Created by レインマン on 2021/01/27 10:50 with Android Studio.
 */
public class VideoPlayerActivity extends BaseActivity<ActivityVideoPlayerBinding> {

	@Override
	protected void initialize() {
		VideoEntity videoEntity = (VideoEntity) getIntent().getSerializableExtra("video");
		mViewBinding.sgvpVideoPlayer.setUp(videoEntity.getPath(), true, videoEntity.getName());
		mViewBinding.sgvpVideoPlayer.getBackButton().setImageResource(R.mipmap.file_manager_back);
		mViewBinding.sgvpVideoPlayer.getTitleTextView().setTextSize(TypedValue.COMPLEX_UNIT_PX, 32F);
		mViewBinding.sgvpVideoPlayer.getTitleTextView().getPaint().setFakeBoldText(true);
		mViewBinding.sgvpVideoPlayer.getTitleTextView().setSingleLine(true);
		mViewBinding.sgvpVideoPlayer.getTitleTextView().setEllipsize(TextUtils.TruncateAt.END);
		mViewBinding.sgvpVideoPlayer.getTitleTextView().setPadding(25, 0, 0, 0);
		mViewBinding.sgvpVideoPlayer.getTitleTextView().setIncludeFontPadding(false);
		LinearLayout topContainer = (LinearLayout) mViewBinding.sgvpVideoPlayer.getBackButton().getParent();
		RelativeLayout.LayoutParams topContainerParams = (RelativeLayout.LayoutParams) topContainer.getLayoutParams();
		topContainerParams.topMargin = 16;
		topContainer.setLayoutParams(topContainerParams);
		mViewBinding.sgvpVideoPlayer.getBackButton().setOnClickListener(view -> finish());
		mViewBinding.sgvpVideoPlayer.getFullscreenButton().setVisibility(View.GONE);
		mViewBinding.sgvpVideoPlayer.setAutoFullWithSize(true);
		mViewBinding.sgvpVideoPlayer.setShowFullAnimation(true);
		mViewBinding.sgvpVideoPlayer.setVideoAllCallBack(new GSYSampleCallBack() {
			@Override
			public void onComplete(String url, Object... objects) {
				super.onComplete(url, objects);
				finish();
			}
		});
		mViewBinding.sgvpVideoPlayer.startPlayLogic();
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
}
