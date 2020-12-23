package com.viegre.nas.speaker.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.Gravity;

import com.viegre.nas.speaker.R;

import androidx.core.content.ContextCompat;
import q.rorbin.verticaltablayout.adapter.TabAdapter;
import q.rorbin.verticaltablayout.widget.ITabView;

/**
 * Created by Djangoogle on 2020/12/22 15:21 with Android Studio.
 */
public class SettingsMenuAdapter implements TabAdapter {

	private final Context mContext;
	private final String[] mMenuNameArr;

	public SettingsMenuAdapter(Context context) {
		mContext = context;
		mMenuNameArr = context.getResources().getStringArray(R.array.settings_menu_name);
	}

	@Override
	public int getCount() {
		return mMenuNameArr.length;
	}

	@Override
	public ITabView.TabBadge getBadge(int position) {
		return null;
	}

	@Override
	public ITabView.TabIcon getIcon(int position) {
		TypedArray typedArray = mContext.getResources().obtainTypedArray(R.array.settings_menu_icon);
		int resId = typedArray.getResourceId(position, 0);
		typedArray.recycle();
		return new ITabView.TabIcon.Builder().setIcon(resId, resId).setIconSize(36, 36).setIconGravity(Gravity.START).setIconMargin(15).build();
	}

	@Override
	public ITabView.TabTitle getTitle(int position) {
		return new ITabView.TabTitle.Builder().setTextColor(ContextCompat.getColor(mContext, R.color.settings_title),
		                                                    ContextCompat.getColor(mContext, R.color.settings_title))
		                                      .setTextSize(20)
		                                      .setContent(mMenuNameArr[position])
		                                      .build();
	}

	@Override
	public int getBackground(int position) {
		return 0;
	}
}
