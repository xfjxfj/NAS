package com.viegre.nas.pad.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.viegre.nas.pad.R;

/**
 * Created by レインマン on 2021/01/14 18:55 with Android Studio.
 */
public class NasVideoPlayer extends StandardGSYVideoPlayer {

	public NasVideoPlayer(Context context, Boolean fullFlag) {
		super(context, fullFlag);
	}

	public NasVideoPlayer(Context context) {
		super(context);
	}

	public NasVideoPlayer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public int getLayoutId() {
		return R.layout.splash_video_player;
	}

	@Override
	protected void onClickUiToggle(MotionEvent e) {
		super.onClickUiToggle(e);
	}

	@Override
	protected void touchDoubleUp(MotionEvent e) {}

	@Override
	protected void touchSurfaceMove(float deltaX, float deltaY, float y) {}
}
