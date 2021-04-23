package com.viegre.nas.pad.activity;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.ImageView;

import com.djangoogle.framework.activity.BaseActivity;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.adapter.MoreAppActivityRvAdapter;
import com.viegre.nas.pad.databinding.MoreAppActivityRvBinding;
import com.viegre.nas.pad.util.CommonUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;

public class MoreAppActivity extends BaseActivity<MoreAppActivityRvBinding> {

	private androidx.recyclerview.widget.RecyclerView mRecycleView;
	private MoreAppActivityRvAdapter mAdapter;//适配器
	private LinearLayoutManager mLinearLayoutManager;//布局管理器
	private ImageView more_ac_img;

	@Override
	protected void initialize() {
		CommonUtils.hideBottomUIMenu(this);
		initView();
		initAdapter();
	}

	private void initAdapter() {

		//初始化数据
		mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		//创建适配器，将数据传递给适配器
		mAdapter = new MoreAppActivityRvAdapter(MoreAppActivity.this, getAllApps(this));
		//设置布局管理器
		mRecycleView.setLayoutManager(mLinearLayoutManager);
		//设置适配器adapter
		mRecycleView.setAdapter(mAdapter);
	}

	private void initView() {
		mRecycleView = findViewById(R.id.rv_list);
		more_ac_img = findViewById(R.id.more_ac_img);
		more_ac_img.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MoreAppActivity.this.finish();
			}
		});
	}

	/**
	 * 查询手机内非系统应用
	 *
	 * @param context
	 * @return
	 */
	public static List<PackageInfo> getAllApps(Context context) {
		List<PackageInfo> apps = new ArrayList<PackageInfo>();
		PackageManager pManager = context.getPackageManager();
		//获取手机内所有应用
		List<PackageInfo> paklist = pManager.getInstalledPackages(0);
		for (int i = 0; i < paklist.size(); i++) {
			PackageInfo pak = (PackageInfo) paklist.get(i);
			//判断是否为非系统预装的应用程序
			if ((pak.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
				// customs applications
				apps.add(pak);
			}
		}
		return apps;
	}
}
