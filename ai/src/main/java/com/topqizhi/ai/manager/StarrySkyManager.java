package com.topqizhi.ai.manager;

import com.blankj.utilcode.util.ThreadUtils;
import com.lzx.starrysky.StarrySky;

/**
 * Created by レインマン on 2021/05/10 17:43 with Android Studio.
 */
public enum StarrySkyManager {

	INSTANCE;

	public void stop() {
		ThreadUtils.runOnUiThread(() -> StarrySky.with().stopMusic());
	}

	public void restore() {
		ThreadUtils.runOnUiThread(() -> StarrySky.with().restoreMusic());
	}
}
