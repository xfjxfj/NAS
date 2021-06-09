package com.viegre.nas.pad.activity.im;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.djangoogle.framework.activity.BaseActivity;
import com.google.gson.Gson;
import com.kongzue.dialog.interfaces.OnDialogButtonClickListener;
import com.kongzue.dialog.interfaces.OnDismissListener;
import com.kongzue.dialog.util.BaseDialog;
import com.kongzue.dialog.v3.MessageDialog;
import com.kongzue.dialog.v3.TipDialog;
import com.kongzue.dialog.v3.WaitDialog;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.adapter.ContactsRvDevicesAdapter;
import com.viegre.nas.pad.adapter.ContactsRvFriendsAdapter;
import com.viegre.nas.pad.adapter.ContactsRvRecordAdapter;
import com.viegre.nas.pad.config.PathConfig;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.config.UrlConfig;
import com.viegre.nas.pad.databinding.ActivityContactsBinding;
import com.viegre.nas.pad.entity.ContactsBean;
import com.viegre.nas.pad.entity.DevicesFollowEntity;
import com.viegre.nas.pad.util.CommonUtils;
import com.viegre.nas.pad.util.ExpandableViewHoldersUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

public class ContactsActivity extends BaseActivity<ActivityContactsBinding> implements View.OnClickListener {

    private RecyclerView contactsRv1;
    private RecyclerView contactsRv2;
    private RecyclerView contactsRv3;
    private ImageView homeImg;
    private final List<ContactsBean> mFriendData = new ArrayList<>();
    private final List<String> mDevicesData = new ArrayList<>();
    private List<String> mRecordData = new ArrayList<>();
    public static Boolean Token_valid = true;
    private TextView textView2;
    private TextView textRecord;
    private ContactsRvRecordAdapter contactsRvRecordAdapter;

    @Override
    protected void initialize() {
        initView();
        getContactsDatas();
    }

    private void initView() {
        contactsRv1 = findViewById(R.id.contactsRv1);
        contactsRv2 = findViewById(R.id.contactsRv2);
        contactsRv3 = findViewById(R.id.contactsRv3);
        textRecord = findViewById(R.id.textRecord);
        homeImg = findViewById(R.id.homeImg);
        textView2 = findViewById(R.id.textView2);
        mViewBinding.homeImg.setOnClickListener(view -> finish());
        textView2.setOnClickListener(this);
        ExpandableViewHoldersUtil.getInstance().init().setNeedExplanedOnlyOne(false);
//      初始化RecycleViewAdapter
        ExpandableViewHoldersUtil.getInstance().resetExpanedList();
//        initFriendData(mContactsData);
        ifRecordList();
        initDevicesData(mDevicesData);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        contactsRvRecordAdapter.notifyDataSetChanged();
    }

    private void ifRecordList() {
        mRecordData = getRecordData();
        initRecordData(mRecordData);
        if (mRecordData.size() == 0) {
//            contactsRv1.setVisibility(View.GONE);//无数据隐藏列表
            ResetRecord();
        }
    }

    public List<String> getRecordData() {
        ArrayList<String> data = new ArrayList<>();
        File file = new File(getFilesDir().toString() + PathConfig.CONTACTS_RECOMDING);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String textOnLineString;
            while ((textOnLineString = reader.readLine()) != null) {
                data.add(textOnLineString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return data;
    }


    private void initDevicesData(List<String> mDevicesData) {
        List<String> wwwww = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            wwwww.add(i + "");
        }
        //初始化数据
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        //设置布局管理器
        contactsRv3.setLayoutManager(linearLayoutManager2);
        //创建适配器，将数据传递给适配器
        //设置适配器adapter
        contactsRv3.setAdapter(new ContactsRvDevicesAdapter(this, wwwww));
    }

    private void initFriendData(List<ContactsBean> mContactsData) {
        //初始化数据
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        //设置布局管理器
        contactsRv2.setLayoutManager(linearLayoutManager1);
        //创建适配器，将数据传递给适配器
        //设置适配器adapter
        contactsRv2.setAdapter(new ContactsRvFriendsAdapter(this, mContactsData));
    }

    private void initRecordData(List<String> mRecordData) {
        //初始化数据
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        //设置布局管理器
        contactsRv1.setLayoutManager(linearLayoutManager);
        //创建适配器，将数据传递给适配器
        //设置适配器adapter
        contactsRvRecordAdapter = new ContactsRvRecordAdapter(this);
        contactsRv1.setAdapter(contactsRvRecordAdapter);
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

                    //                    {"msg":"token verify fail","code":"4111"}   2021年5月21日
                    @Override
                    public void onNext(@NonNull String s) {
//                        {"msg":"token verify fail","code":"4111"}
//                        {"code":0,"msg":"OK","data":[{"phone":"15357906428","nickName":null,"avater":null,"callId":"sws8s888","status":{"desc":"管理员","code":3},"boundTime":"2021-05-20 10:45:19.447.44.4"}]}
                        Gson gson = new Gson();
                        DevicesFollowEntity devicesFollowEntity = gson.fromJson(s, DevicesFollowEntity.class);
                        if (devicesFollowEntity.getMsg().equals("OK")) {//返回数据正确
                            List<DevicesFollowEntity.DataDTO> data = devicesFollowEntity.getData();
                            if (null != data) {
                                for (DevicesFollowEntity.DataDTO datum : data) {
                                    String userid = datum.getCallId();
                                    String phone = datum.getPhone();
                                    String nickName = "";
                                    if (datum.getNickName() == null) {
                                        nickName = "";
                                    } else {
                                        nickName = (String) datum.getNickName();
                                    }
                                    mFriendData.add(new ContactsBean(userid, "", nickName, phone));
                                }
                                mFriendData.add(new ContactsBean("ceciciJJ", "", "郑飞", "138"));
                                mFriendData.add(new ContactsBean("anaOaOjj", "", "设备pad", "191"));
                                mFriendData.add(new ContactsBean("ISIFIF99", "", "oppo-pad", "191"));
                                mFriendData.add(new ContactsBean("agahahss", "", "华为AL00-pad", "456"));
                                mFriendData.add(new ContactsBean("ZoZcZcKK", "", "夜神模拟器-pad", "666"));
                                mFriendData.add(new ContactsBean("RlRbRbGG", "", "设备2-pad", "666"));
                                mFriendData.add(new ContactsBean("OkORORNN", "", "设备3-oppo", "1313"));
                                mFriendData.add(new ContactsBean("-a-X-Xoo", "", "设备4-小米re", "1313"));
                            }
                            TipDialog.show(ContactsActivity.this, "成功", TipDialog.TYPE.SUCCESS).doDismiss();
                            initFriendData(mFriendData);
                        } else {
                            Token_valid = false;
                            TipDialog.show(ContactsActivity.this, devicesFollowEntity.getMsg(), TipDialog.TYPE.ERROR).doDismiss();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        TipDialog.show(ContactsActivity.this, e.getMessage(), TipDialog.TYPE.SUCCESS).doDismiss();

                        CommonUtils.showErrorToast(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d("", "");
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            });
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.textView2:
                MessageDialog.show(this, "提示", "确定删除通话记录吗", "是", "取消")
                        .setOnOkButtonClickListener(new OnDialogButtonClickListener() {
                            @Override
                            public boolean onClick(BaseDialog baseDialog, View v) {
                                WaitDialog.show(ContactsActivity.this, "请稍候...");
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ResetRecord();
                                                TipDialog.show(ContactsActivity.this, "成功！", TipDialog.TYPE.SUCCESS).setOnDismissListener(new OnDismissListener() {
                                                    @Override
                                                    public void onDismiss() {

                                                    }
                                                });
                                            }
                                        });
                                    }
                                }, 1000);
                                return false;
                            }
                        })
                        .setButtonOrientation(LinearLayout.VERTICAL);

                break;
        }
    }

    private void ResetRecord() {
        mRecordData.clear();
        try {
            File file = new File(getFilesDir().toString() + PathConfig.CONTACTS_RECOMDING);
            if (file.exists()) {
                if (file.isFile()) {  // 为文件时调用删除文件方法
                    file.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        initRecordData(mRecordData);
        textRecord.setVisibility(View.VISIBLE);//显示提示信息
    }
}












