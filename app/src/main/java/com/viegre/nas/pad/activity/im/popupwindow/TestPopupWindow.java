package com.viegre.nas.pad.activity.im.popupwindow;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.viegre.nas.pad.R;

/**
 * Created by gavin
 * Created date 17/9/13
 */

public class TestPopupWindow extends PopupWindow {

    public TestPopupWindow(Context context) {
        super(context);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setOutsideTouchable(true);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View contentView = LayoutInflater.from(context).inflate(R.layout.contacts_devices_popup, null, false);
        setContentView(contentView);
    }
}
