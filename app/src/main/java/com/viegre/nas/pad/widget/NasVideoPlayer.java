package com.viegre.nas.pad.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.viegre.nas.pad.R;

/**
 * Created by Djangoogle on 2021/01/14 18:55 with Android Studio.
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
		return R.layout.video_player_nas;
	}

	@Override
	protected void onClickUiToggle() {
		super.onClickUiToggle();
	}

	@Override
	protected void touchDoubleUp() {}

	@Override
	protected void touchSurfaceMove(float deltaX, float deltaY, float y) {}
}
