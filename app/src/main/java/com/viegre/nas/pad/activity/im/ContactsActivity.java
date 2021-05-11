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
        //初始化RecycleViewAdapter
//        getContactsDatas();
//        initAdapter();
    }


    private void initAdapter(List<ContactsBean> mContactsData) {
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
        contactsRv2.setAdapter(new ContactsRvFriendsAdapter(this, mContactsData));

        //初始化数据
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        //设置布局管理器
        contactsRv3.setLayoutManager(linearLayoutManager2);
        //创建适配器，将数据传递给适配器
        //设置适配器adapter
        contactsRv3.setAdapter(new ContactsRvDevicesAdapter(this, languages));
    }

    private void callLogin(String phone) {
        try {
            JSONObject json153 = new JSONObject();
            json153.put("userid", "sws8s888");
            json153.put("userimg", "http://pics6.baidu.com/feed/32fa828ba61ea8d35f0fc102b0795946251f5806.jpeg?token=7e8e4aa8ff38f2f87cb19d100b003e82");
            json153.put("username", "张三");
            json153.put("userphone", "15357906428");

            JSONObject json131 = new JSONObject();
            json131.put("userid", "VaVvVvii");
            json131.put("userimg", "http://t12.baidu.com/it/u=531657465,141298675&fm=30&app=106&f=JPEG?w=312&h=208&s=DEA208C41A5079DE5D89553A0300E010");
            json131.put("username", "李四");
            json131.put("userphone", "13168306428");

            JSONObject jsonF = new JSONObject();
            jsonF.put("userid", "y7y7y733");
            jsonF.put("userimg", "http://t12.baidu.com/it/u=531657465,141298675&fm=30&app=106&f=JPEG?w=312&h=208&s=DEA208C41A5079DE5D89553A0300E010");
            jsonF.put("username", "风顺");
            jsonF.put("userphone", "15156021911");

            mContactsData.add(new Gson().fromJson(json153.toString(), ContactsBean.class));
            mContactsData.add(new Gson().fromJson(json131.toString(), ContactsBean.class));
            mContactsData.add(new Gson().fromJson(jsonF.toString(), ContactsBean.class));

        } catch (JSONException e) {
            e.printStackTrace();
        }
//        音视频登录
//        ContactsActivity.phone = "15357906428";
//        String phoneNumber = "13168306428";
//        String phoneNumber = "7dd40314e43596cf";
        String authCode = "66666";
        AppService.Instance().smsLogin(phone, authCode, new AppService.LoginCallback() {
            @Override
            public void onUiSuccess(LoginResult loginResult) {
                if (isFinishing()) {
                    return;
                }
                //需要注意token跟clientId是强依赖的，一定要调用getClientId获取到clientId，然后用这个clientId获取token，这样connect才能成功，如果随便使用一个clientId获取到的token将无法链接成功。
                ChatManagerHolder.gChatManager.connect(loginResult.getUserId(), loginResult.getToken());
                SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);

                sp.edit()
                        .putString("id", loginResult.getUserId())
                        .putString("token", loginResult.getUserId())
                        .apply();
//                ConstactBean constactBean = new ConstactBean(ContactsActivity.phone, loginResult.getUserId());
                initAdapter(mContactsData);
            }

            @Override
            public void onUiFailure(int code, String msg) {
                if (isFinishing()) {
                    return;
                }
                Toast.makeText(ContactsActivity.this, "登录失败：" + code + " " + msg, Toast.LENGTH_SHORT).show();
//                loginButton.setEnabled(true);
            }
        });
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
                        Gson gson = new Gson();
                        DevicesFollowEntity devicesFollowEntity = gson.fromJson(s, DevicesFollowEntity.class);
                        List<DevicesFollowEntity.DataDTO> data = devicesFollowEntity.getData();
                        if (!data.isEmpty()) {
                            for (DevicesFollowEntity.DataDTO datum : data) {
                                String userid = datum.getCallId();
                                String phone = datum.getPhone();
                                    String nickName = "";
                                if (datum.getNickName() == null) {
                                    nickName = "";
                                } else {
                                    nickName = (String) datum.getNickName();
                                }
                                mContactsData.add(new ContactsBean(userid,"",nickName,phone));
                            }
                                mContactsData.add(new ContactsBean("ceciciJJ", "", "郑飞", "138"));
                            mContactsData.add(new ContactsBean("ISIFIF99", "", "OPPO Pad", "138"));
                        }
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












