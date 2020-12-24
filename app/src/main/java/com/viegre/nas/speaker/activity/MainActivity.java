package com.viegre.nas.speaker.activity;

import com.blankj.utilcode.util.ActivityUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.viegre.nas.speaker.R;
import com.viegre.nas.speaker.activity.base.BaseActivity;
import com.viegre.nas.speaker.databinding.ActivityMainBinding;
import com.youth.banner.adapter.BannerImageAdapter;
import com.youth.banner.holder.BannerImageHolder;
import com.youth.banner.indicator.CircleIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Djangoogle on 2020/12/15 09:29 with Android Studio.
 */
public class MainActivity extends BaseActivity<ActivityMainBinding> {

	@Override
	protected void initView() {
		initIcon();
		initBanner();
	}

	@Override
	protected void initData() {}

	private void initIcon() {
		Glide.with(this)
		     .load(R.mipmap.main_unlogin)
		     .apply(RequestOptions.bitmapTransform(new CircleCrop()))
		     .into(mViewBinding.acivMainUserIcon);
		Glide.with(this)
		     .load(R.mipmap.main_icon_image)
		     .apply(RequestOptions.bitmapTransform(new RoundedCorners(24)))
		     .into(mViewBinding.acivMainIconImage);
		Glide.with(this)
		     .load(R.mipmap.main_icon_voice)
		     .apply(RequestOptions.bitmapTransform(new RoundedCorners(24)))
		     .into(mViewBinding.acivMainIconVoice);
		Glide.with(this)
		     .load(R.mipmap.main_icon_video)
		     .apply(RequestOptions.bitmapTransform(new RoundedCorners(24)))
		     .into(mViewBinding.acivMainIconVideo);
		Glide.with(this)
		     .load(R.mipmap.test_icon_3)
		     .apply(RequestOptions.bitmapTransform(new RoundedCorners(24)))
		     .into(mViewBinding.acivMainIcon3);
		Glide.with(this)
		     .load(R.mipmap.test_icon_4)
		     .apply(RequestOptions.bitmapTransform(new RoundedCorners(24)))
		     .into(mViewBinding.acivMainIcon4);
		Glide.with(this)
		     .load(R.mipmap.test_icon_5)
		     .apply(RequestOptions.bitmapTransform(new RoundedCorners(24)))
		     .into(mViewBinding.acivMainIcon5);
		Glide.with(this)
		     .load(R.mipmap.test_icon_6)
		     .apply(RequestOptions.bitmapTransform(new RoundedCorners(24)))
		     .into(mViewBinding.acivMainIcon6);
		Glide.with(this)
		     .load(R.mipmap.test_icon_7)
		     .apply(RequestOptions.bitmapTransform(new RoundedCorners(24)))
		     .into(mViewBinding.acivMainIcon7);
		Glide.with(this)
		     .load(R.mipmap.test_icon_8)
		     .apply(RequestOptions.bitmapTransform(new RoundedCorners(24)))
		     .into(mViewBinding.acivMainIcon8);
		mViewBinding.acivMainIcon8.setOnClickListener(view -> ActivityUtils.startActivity(SettingsActivity.class));
	}

	private void initBanner() {
		List<String> bannerList = new ArrayList<>();
		bannerList.add("https://pic1.zhimg.com/c7ad985268e7144b588d7bf94eedb487_r.jpg?source=1940ef5c");
		bannerList.add("https://pic1.zhimg.com/v2-3ff3d6a85edb2f19d343668d24ed9269_r.jpg?source=1940ef5c");
		bannerList.add("https://pic3.zhimg.com/v2-3fcdfeacc10696e3f71d66a9ba6e9cc4_r.jpg?source=1940ef5c");
		bannerList.add("https://pic2.zhimg.com/v2-73b8307b2db44c617f4e8515ce67dd39_r.jpg?source=1940ef5c");
		bannerList.add("https://pic2.zhimg.com/v2-f85f658e4f785d48cf04dd8f47acc6fa_r.jpg?source=1940ef5c");
		bannerList.add("https://pic4.zhimg.com/v2-e5427c1e9ad8aaad99d643e7bd7e927b_r.jpg?source=1940ef5c");
		bannerList.add("https://pic2.zhimg.com/v2-d024c6ad6851b266e8509d1aa0948ceb_r.jpg?source=1940ef5c");
		mViewBinding.bMainBanner.addBannerLifecycleObserver(this).setAdapter(new BannerImageAdapter<String>(bannerList) {
			@Override
			public void onBindView(BannerImageHolder holder, String data, int position, int size) {
				Glide.with(holder.itemView).load(data).into(holder.imageView);
			}
		}).setBannerRound2(16F).setIndicator(new CircleIndicator(this));
	}
}
