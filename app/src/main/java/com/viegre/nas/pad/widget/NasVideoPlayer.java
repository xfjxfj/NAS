package com.viegre.nas.pad.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.config.BusConfig;

import org.greenrobot.eventbus.EventBus;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * Created by レインマン on 2021/01/14 18:55 with Android Studio.
 */
public class NasVideoPlayer extends StandardGSYVideoPlayer {

	private boolean isShowControl = false;
	private AppCompatImageView previous, next;

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
	protected void init(Context context) {
		super.init(context);
		previous = findViewById(R.id.previous);
		next = findViewById(R.id.next);
		if (null != previous) {
			previous.setOnClickListener(this);
		}
		if (null != next) {
			next.setOnClickListener(this);
		}
	}

	@Override
	public int getLayoutId() {
		return R.layout.nas_video_player;
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		if (isShowControl) {
			int id = v.getId();
			if (R.id.previous == id) {
				EventBus.getDefault().post(BusConfig.VIDEO_PLAY_PREVIOUS);
			} else if (R.id.next == id) {
				EventBus.getDefault().post(BusConfig.VIDEO_PLAY_NEXT);
			}
		}
	}

	@Override
	protected void hideAllWidget() {
		super.hideAllWidget();
		setViewShowState(previous, INVISIBLE);
		setViewShowState(next, INVISIBLE);
	}

	@Override
	protected void changeUiToNormal() {
		super.changeUiToNormal();
		if (isShowControl) {
			setViewShowState(previous, VISIBLE);
			setViewShowState(next, VISIBLE);
		}
	}

	@Override
	protected void changeUiToPreparingShow() {
		super.changeUiToPreparingShow();
		setViewShowState(previous, INVISIBLE);
		setViewShowState(next, INVISIBLE);
	}

	@Override
	protected void changeUiToPlayingShow() {
		super.changeUiToPlayingShow();
		if (isShowControl) {
			setViewShowState(previous, VISIBLE);
			setViewShowState(next, VISIBLE);
		}
	}

	@Override
	protected void changeUiToPauseShow() {
		super.changeUiToPauseShow();
		if (isShowControl) {
			setViewShowState(previous, VISIBLE);
			setViewShowState(next, VISIBLE);
		}
	}

	@Override
	protected void changeUiToPlayingBufferingShow() {
		super.changeUiToPlayingBufferingShow();
		setViewShowState(previous, INVISIBLE);
		setViewShowState(next, INVISIBLE);
	}

	@Override
	protected void changeUiToCompleteShow() {
		super.changeUiToCompleteShow();
		if (isShowControl) {
			setViewShowState(previous, VISIBLE);
			setViewShowState(next, VISIBLE);
		}
	}

	@Override
	protected void changeUiToError() {
		super.changeUiToError();
		if (isShowControl) {
			setViewShowState(previous, VISIBLE);
			setViewShowState(next, VISIBLE);
		}
	}

	@Override
	protected void changeUiToPrepareingClear() {
		super.changeUiToPrepareingClear();
		setViewShowState(previous, INVISIBLE);
		setViewShowState(next, INVISIBLE);
	}

	@Override
	protected void changeUiToPlayingBufferingClear() {
		super.changeUiToPlayingBufferingClear();
		setViewShowState(previous, INVISIBLE);
		setViewShowState(next, INVISIBLE);
	}

	@Override
	protected void changeUiToClear() {
		super.changeUiToClear();
		setViewShowState(previous, INVISIBLE);
		setViewShowState(next, INVISIBLE);
	}

	@Override
	protected void changeUiToCompleteClear() {
		super.changeUiToCompleteClear();
		if (isShowControl) {
			setViewShowState(previous, VISIBLE);
			setViewShowState(next, VISIBLE);
		}
	}

	public void setShowControl(boolean showControl) {
		isShowControl = showControl;
	}
}
