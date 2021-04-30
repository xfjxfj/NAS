package com.viegre.nas.pad.activity;

import android.widget.ImageView;

import com.blankj.utilcode.util.SPUtils;
import com.djangoogle.framework.activity.BaseActivity;
import com.kongzue.dialog.v3.TipDialog;
import com.kongzue.dialog.v3.WaitDialog;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.adapter.ContactsRvDevicesAdapter;
import com.viegre.nas.pad.adapter.ContactsRvFriendsAdapter;
import com.viegre.nas.pad.adapter.ContactsRvRecordAdapter;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.config.UrlConfig;
import com.viegre.nas.pad.databinding.ActivityContactsBinding;
import com.viegre.nas.pad.util.CommonUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import rxhttp.RxHttp;

/**
 * 联系人相关类
 */

public class ContactsActivity extends BaseActivity<ActivityContactsBinding> {

	private RecyclerView contactsRv1;
	private RecyclerView contactsRv2;
	private RecyclerView contactsRv3;
	private ImageView homeImg;

	@Override
	protected void initialize() {
		initView();
		getContactsDatas();
	}

	private void initView() {
		contactsRv1 = findViewById(R.id.contactsRv1);
		contactsRv2 = findViewById(R.id.contactsRv2);
		contactsRv3 = findViewById(R.id.contactsRv3);
		mViewBinding.homeImg.setOnClickListener(view -> finish());
		//初始化RecycleViewAdapter
		initAdapter();
	}

	private void initAdapter() {
		List<String> languages = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			languages.add(i + "");
		}

		//初始化数据
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		//设置布局管理器
		contactsRv1.setLayoutManager(linearLayoutManager);
		//创建适配器，将数据传递给适配器
		//设置适配器adapter
		contactsRv1.setAdapter(new ContactsRvRecordAdapter(this, languages));

		//初始化数据
		LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		//设置布局管理器
		contactsRv2.setLayoutManager(linearLayoutManager1);
		//创建适配器，将数据传递给适配器
		//设置适配器adapter
		contactsRv2.setAdapter(new ContactsRvFriendsAdapter(this, languages));

		//初始化数据
		LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
		//设置布局管理器
		contactsRv3.setLayoutManager(linearLayoutManager2);
		//创建适配器，将数据传递给适配器
		//设置适配器adapter
		contactsRv3.setAdapter(new ContactsRvDevicesAdapter(this, languages));
	}

	private void getContactsDatas() {
		TipDialog show = WaitDialog.show(this, "请稍候...");
		RxHttp.postForm(UrlConfig.Device.GET_GETALLFOLLOWS)
		      .add("sn", SPUtils.getInstance().getString(SPConfig.ANDROID_ID))
		      .asString()
		      .observeOn(AndroidSchedulers.mainThread())
		      .subscribe(new Observer<String>() {
			      @Override
			      public void onSubscribe(@NonNull Disposable d) {}

			      @Override
			      public void onNext(@NonNull String s) {
				      TipDialog.show(ContactsActivity.this, "成功", TipDialog.TYPE.SUCCESS).doDismiss();
			      }

			      @Override
			      public void onError(@NonNull Throwable e) {
				      CommonUtils.showErrorToast(e.getMessage());
			      }

			      @Override
			      public void onComplete() {}
		      });
	}
}
























