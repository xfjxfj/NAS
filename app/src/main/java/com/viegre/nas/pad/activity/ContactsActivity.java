package com.viegre.nas.pad.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;


import com.blankj.utilcode.util.SPUtils;

import com.kongzue.dialog.v3.TipDialog;
import com.kongzue.dialog.v3.WaitDialog;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.adapter.ContactsRvDevicesAdapter;
import com.viegre.nas.pad.adapter.ContactsRvFriendsAdapter;
import com.viegre.nas.pad.adapter.ContactsRvRecordAdapter;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.config.UrlConfig;
import com.djangoogle.framework.activity.BaseActivity;
import com.viegre.nas.pad.databinding.ActivityContactsBinding;
import com.viegre.nas.pad.util.CommonUtils;
import com.yanzhenjie.kalle.Kalle;
import com.yanzhenjie.kalle.simple.SimpleCallback;
import com.yanzhenjie.kalle.simple.SimpleResponse;
import java.util.ArrayList;
import java.util.List;


/**
 * 联系人相关类
 */

public class ContactsActivity extends BaseActivity<ActivityContactsBinding> implements View.OnClickListener {

    private RecyclerView contactsRv1;
    private RecyclerView contactsRv2;
    private RecyclerView contactsRv3;
    private ImageView homeImg;

    @Override
    protected void initialize() {
        CommonUtils.hideBottomUIMenu(this);
        CommonUtils.hideStatusBar(this);
        initView();
        getContactsDatas();
    }

    private void initView() {
        contactsRv1 = findViewById(R.id.contactsRv1);
        contactsRv2 = findViewById(R.id.contactsRv2);
        contactsRv3 = findViewById(R.id.contactsRv3);
        homeImg = findViewById(R.id.homeImg);
        //初始化点击事件
        homeImg.setOnClickListener(this);
        //初始化RecycleViewAdapter
//        initAdapter();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.homeImg:
                ContactsActivity.this.finish();
                break;
        }
    }

    @SuppressLint("WrongConstant")
    private void getContactsDatas() {
        TipDialog show = WaitDialog.show(ContactsActivity.this, "请稍候...");
        Kalle.post(UrlConfig.Device.GET_GETALLFOLLOWS)
                .param("sn", SPUtils.getInstance().getString(SPConfig.ANDROID_ID))
                .perform(new SimpleCallback<String>() {
                    @SuppressLint({"WrongConstant", "ApplySharedPref"})
                    @Override
                    public void onResponse(SimpleResponse<String, String> response) {
                        TipDialog.show(ContactsActivity.this, "成功", TipDialog.TYPE.SUCCESS).doDismiss();
                        Log.d("", "");
//                        List<ContactsBean> contactsBeans = new List<>();
                        initAdapter();
                    }

                    @Override
                    public void onEnd() {
                        super.onEnd();
                        Log.d("", "");
                    }
                });
    }
}
























