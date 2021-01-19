package com.viegre.nas.pad.task;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;

/**
 * Created by レインマン on 2021/01/19 09:40 with Android Studio.
 */
public abstract class VoidTask<T> extends ThreadUtils.SimpleTask<T> {

	@Override
	public void onSuccess(T result) {
		LogUtils.iTag(VoidTask.class.getSimpleName(), "VoidTask.onSuccess()");
	}
}
