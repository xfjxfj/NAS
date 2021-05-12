package com.viegre.nas.pad.activity.im;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.blankj.utilcode.util.SPUtils;
import com.djangoogle.framework.activity.BaseActivity;
import com.google.gson.Gson;
import com.kongzue.dialog.v3.TipDialog;
import com.kongzue.dialog.v3.WaitDialog;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.adapter.ContactsRvDevicesAdapter;
import com.viegre.nas.pad.adapter.ContactsRvFriendsAdapter;
import com.viegre.nas.pad.adapter.ContactsRvRecordAdapter;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.config.UrlConfig;
import com.viegre.nas.pad.databinding.ActivityContactsBinding;
import com.viegre.nas.pad.entity.ContactsBean;
import com.viegre.nas.pad.entity.DevicesFollowEntity;
import com.viegre.nas.pad.entity.LoginResult;
import com.viegre.nas.pad.service.AppService;
import com.viegre.nas.pad.util.CommonUtils;
import com.viegre.nas.pad.util.ExpandableViewHoldersUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cn.wildfire.chat.kit.ChatManagerHolder;
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
    private final List<ContactsBean> mContactsData = new ArrayList<>();
    public static String phone = "";

    @Override
    protected void initialize() {
        initView();
        getContactsDatas();
    }

    private void initView() {
        contactsRv1 = findViewById(R.id.contactsRv1);
        contactsRv2 = findViewById(R.id.contactsRv2);
        contactsRv3 = findViewById(R.id.contactsRv3);
        homeImg = findViewById(R.id.homeImg);
        mViewBinding.homeImg.setOnClickListener(view -> finish());
        ExpandableViewHoldersUtil.getInstance().init().setNeedExplanedOnlyOne(false);
        //初始化RecycleViewAdapter
//        getContactsDatas();
//        initAdapter();
    }


    private void initAdapter(List<ContactsBean> mContactsData) {
        ExpandableViewHoldersUtil.getInstance().resetExpanedList();
        List<String> qqqqq = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            qqqqq.add(i + "");
        }
        List<String> wwwww = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            wwwww.add(i + "");
        }
        //初始化数据
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        //设置布局管理器
        contactsRv1.setLayoutManager(linearLayoutManager);
        //创建适配器，将数据传递给适配器
        //设置适配器adapter
        contactsRv1.setAdapter(new ContactsRvRecordAdapter(this, qqqqq));
//        contactsRv1.setAdapter(new ContactsRvRecordAdapter(this));

        //初始化数据
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        //设置布局管理器
        contactsRv2.setLayoutManager(linearLayoutManager1);
        //创建适配器，将数据传递给适配器
        //设置适配器adapter
        contactsRv2.setAdapter(new ContactsRvFriendsAdapter(this, mContactsData));

        //初始化数据
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        //设置布局管理器
        contactsRv3.setLayoutManager(linearLayoutManager2);
        //创建适配器，将数据传递给适配器
        //设置适配器adapter
        contactsRv3.setAdapter(new ContactsRvDevicesAdapter(this, wwwww));
    }

    private void getContactsDatas() {
        TipDialog show = WaitDialog.show(this, "请稍候...");
        RxHttp.postForm(UrlConfig.Device.GET_GETALLFOLLOWS)
                .addHeader("token", SPUtils.getInstance().getString("token"))
                .add("sn", SPUtils.getInstance().getString(SPConfig.ANDROID_ID))
                .asString()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        Log.d("onSubscribe", d.toString());
                    }

                    @Override
                    public void onNext(@NonNull String s) {
//                        Gson gson = new Gson();
//                        DevicesFollowEntity devicesFollowEntity = gson.fromJson(s, DevicesFollowEntity.class);
//                        List<DevicesFollowEntity.DataDTO> data = devicesFollowEntity.getData();
//                        if (!data.isEmpty()) {
//                            for (DevicesFollowEntity.DataDTO datum : data) {
//                                String userid = datum.getCallId();
//                                String phone = datum.getPhone();
//                                    String nickName = "";
//                                if (datum.getNickName() == null) {
//                                    nickName = "";
//                                } else {
//                                    nickName = (String) datum.getNickName();
//                                }
//                                mContactsData.add(new ContactsBean(userid,"",nickName,phone));
//                            }
//                                mContactsData.add(new ContactsBean("ceciciJJ","","郑飞","138"));
//                                mContactsData.add(new ContactsBean("anaOaOjj","","设备pad","191"));
//                                mContactsData.add(new ContactsBean("ISIFIF99","","oppo-pad","191"));
//                                mContactsData.add(new ContactsBean("agahahss","","华为AL00-pad","456"));
//                        }
                        TipDialog.show(ContactsActivity.this, "成功", TipDialog.TYPE.SUCCESS).doDismiss();
                        initAdapter(mContactsData);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        CommonUtils.showErrorToast(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d("", "");
                    }
                });
    }
}












