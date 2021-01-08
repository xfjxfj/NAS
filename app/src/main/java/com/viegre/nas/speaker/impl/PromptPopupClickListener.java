package com.viegre.nas.speaker.impl;

/**
 * Created by Djangoogle on 2021/01/08 16:46 with Android Studio.
 */
public abstract class PromptPopupClickListener implements PromptPopupBaseClickListener {

	public void onCancel() {}

	public abstract void onConfirm();
}
