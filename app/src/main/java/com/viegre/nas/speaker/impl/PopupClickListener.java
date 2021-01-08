package com.viegre.nas.speaker.impl;

/**
 * Created by Djangoogle on 2021/01/08 16:46 with Android Studio.
 */
public abstract class PopupClickListener implements PopupBaseClickListener {

	public void onCancel() {}

	public abstract void onConfirm();
}
