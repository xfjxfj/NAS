package com.viegre.nas.pad.activity.im;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.SPUtils;
import com.djangoogle.framework.activity.BaseActivity;
import com.google.gson.Gson;
import com.kongzue.dialog.interfaces.OnDialogButtonClickListener;
import com.kongzue.dialog.interfaces.OnDismissListener;
import com.kongzue.dialog.util.BaseDialog;
import com.kongzue.dialog.util.DialogSettings;
import com.kongzue.dialog.v3.CustomDialog;
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
import com.viegre.nas.pad.entity.AddDevicesFriend;
import com.viegre.nas.pad.entity.ContactsBean;
import com.viegre.nas.pad.entity.DevicesFollowEntity;
import com.viegre.nas.pad.entity.DevicesFriendList;
import com.viegre.nas.pad.entity.DevicesFriendsListBean;
import com.viegre.nas.pad.service.MQTTService;
import com.viegre.nas.pad.util.CommonUtils;
import com.viegre.nas.pad.util.ExpandableViewHoldersUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import rxhttp.RxHttp;

import static android.app.PendingIntent.getActivity;

/**
 * 联系人相关类
 */

public class ContactsActivity extends BaseActivity<ActivityContactsBinding> implements View.OnClickListener, ContactsRvDevicesAdapter.addDevicesFriend, ContactsRvDevicesAdapter.editDevicesName, ContactsRvDevicesAdapter.deleteDevicesFriend {

    private RecyclerView contactsRv1;
    private RecyclerView contactsRv2;
    private RecyclerView contactsRv3;
    private ImageView homeImg;
    private final List<ContactsBean> mFriendData = new ArrayList<>();
    private List<DevicesFriendsListBean> mDevicesData = new ArrayList<>();
    private List<String> mRecordData = new ArrayList<>();
    public static Boolean Token_valid = true;
    private TextView textView2;
    private TextView textRecord;
    boolean isLoading = false;
    private ContactsRvRecordAdapter contactsRvRecordAdapter;
    private ContactsRvDevicesAdapter adapter;
    private MQTTService myService;
    //处理mqtt那边传递过来的消息
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myService = ((MQTTService.DownLoadBinder) service).getService();
//             回调接口
            myService.setTipsDevicesFriend(new MQTTService.TipsDevicesFriend() {
                @Override
                public void onTipsdevicesFriend(String requestID) {
                    DialogSettings.isUseBlur = true;
                    CustomDialog.build(ContactsActivity.this, R.layout.contacts_add_devices_invitation_dialog, new CustomDialog.OnBindView() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onBind(final CustomDialog dialog, View v) {
                            TextView invitation_tips = v.findViewById(R.id.invitation_tips);
                            Button cancle_bt = v.findViewById(R.id.cancle_bt);
                            Button button_ok = v.findViewById(R.id.button_ok);

                            invitation_tips.setText(getResources().getString(R.string.contacts_add_devices11) + "\"" + requestID + "\"" + getResources().getString(R.string.contacts_add_devices12));
                            button_ok.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AccectRequest(1, requestID, dialog);
                                }
                            });
                            cancle_bt.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.doDismiss();
                                }
                            });
                        }
                    }).setFullScreen(true).show();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private TipDialog dialog;

    //***接受或者拒绝好友申请
    @SuppressLint("UseValueOf")
    private void AccectRequest(int status, String friendId, CustomDialog dialog) {
        RxHttp.postForm(UrlConfig.Device.GET_ADDFRIENDRESULT)
                .addHeader("token", SPUtils.getInstance().getString("token"))
                .add("requesterSn", friendId)
                .add("sn", SPUtils.getInstance().getString(SPConfig.ANDROID_ID))
                .add("status", new Integer(status))
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
//                        {"code":0,"msg":"OK","data":null}
                        Gson gson = new Gson();
//                        {"code":4000,"msg":"参数不正确","data":null}
                        AddDevicesFriend addDevicesFriend = gson.fromJson(s, AddDevicesFriend.class);
                        if (addDevicesFriend.msg.equals("OK")) {
                            Toast.makeText(ContactsActivity.this, "添加成功", Toast.LENGTH_LONG).show();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
//                                            bt.setText("发送邀请");
                                            dialog.doDismiss();
                                        }
                                    });
                                }
                            }, 1500);
                        } else {
                            CommonUtils.showErrorToast(addDevicesFriend.msg);
                        }
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

    @Override
    protected void initialize() {
        initView();
        getContactsDatas();
    }

    private void initView() {
        getDevicesfriend();
        contactsRv1 = findViewById(R.id.contactsRv1);
        contactsRv2 = findViewById(R.id.contactsRv2);
        contactsRv3 = findViewById(R.id.contactsRv3);
        textRecord = findViewById(R.id.textRecord);
        homeImg = findViewById(R.id.homeImg);
        textView2 = findViewById(R.id.textView2);
        mViewBinding.homeImg.setOnClickListener(view -> finish());
        textView2.setOnClickListener(this);
        Intent intent = new Intent(this, MQTTService.class);
        // 标志位BIND_AUTO_CREATE是的服务中onCreate得到执行,onStartCommand不会执行
        bindService(intent, conn, Context.BIND_AUTO_CREATE);

        ExpandableViewHoldersUtil.getInstance().init().setNeedExplanedOnlyOne(false);
//      初始化RecycleViewAdapter
        ExpandableViewHoldersUtil.getInstance().resetExpanedList();
//        initFriendData(mContactsData);
        ifRecordList();
        initDevicesData(mDevicesData);

        contactsRv3.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@androidx.annotation.NonNull @NotNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (recyclerView != null && recyclerView.getChildCount() > 0) {
                    try {
                        int currentPosition = ((RecyclerView.LayoutParams) recyclerView.getChildAt(0).getLayoutParams()).getViewAdapterPosition();
                        Log.e("=====currentPosition", "" + currentPosition);
                    } catch (Exception e) {
                    }
                }
            }

            @Override
            public void onScrolled(@androidx.annotation.NonNull @NotNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                RecyclerView.LayoutManager linearLayoutManager = contactsRv3.getLayoutManager();
                int itemCount = linearLayoutManager.getItemCount();
                int baseline = linearLayoutManager.getBaseline();
                Log.d("=====itemCount=", itemCount + "");
                Log.d("=====dx=", dx + "");
                Log.d("=====dy=", dy + "");

                if (!recyclerView.canScrollVertically(1)) {

                }
            }
        });
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


    private void initDevicesData(List<DevicesFriendsListBean> mDevicesData) {
        //初始化数据
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        //设置布局管理器
        contactsRv3.setLayoutManager(linearLayoutManager2);
        //创建适配器，将数据传递给适配器
        //设置适配器adapter
        View inflate = getLayoutInflater().inflate(R.layout.contacts_devices_popup, null);
        mDevicesData.add(new DevicesFriendsListBean());
        adapter = new ContactsRvDevicesAdapter(this, mDevicesData, inflate);
        contactsRv3.setAdapter(adapter);
        adapter.setaddDevicesFriend(this);
        adapter.setEditDevicesName(this);
        adapter.setDeleteFriend(this);
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

    private void getDevicesfriend() {
        if (dialog == null) {
            dialog = WaitDialog.show(this, "请稍候...");
        }
        RxHttp.get(UrlConfig.Device.GET_GETFRIENDS)
                .addHeader("token", SPUtils.getInstance().getString("token"))
                .add("pageNum", new Integer(0))
                .add("pageSize", new Integer(100))
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
//                        {"code":0,"msg":"OK","data":{"total":1,"friends":[{"friendId":1169,"createTime":"2021-06-18T17:05:17.138","boundTime":"2021-06-21T15:22:01.584","name":"杭州","updateTime":"2021-06-21T15:22:01.584","model":"杭州","id":8,"sn":"6fa8295f4764b429","deviceId":1279,"status":{"desc":"同意(普通角色)","code":1}}]}}
//                        {"code":0,"msg":"OK","data":{"total":1,"friends":[{"callId":"BiBkBkmm","friendId":1169,"createTime":"2021-06-18T17:05:17.138","boundTime":"2021-06-21T15:22:01.584","name":"杭州","updateTime":"2021-06-21T15:22:01.584","model":"杭州","id":8,"sn":"6fa8295f4764b429","deviceId":1279,"status":{"desc":"同意(普通角色)","code":1}}]}}
                        Gson gson = new Gson();
                        DevicesFriendList DevicesFriendList = gson.fromJson(s, DevicesFriendList.class);
                        if (DevicesFriendList.getMsg().equals("OK")) {//返回数据正确
                            List<com.viegre.nas.pad.entity.DevicesFriendList.FriendsBean> friends = DevicesFriendList.getData().getFriends();
                            mDevicesData = new ArrayList<>();
                            for (int i = 0; i < friends.size(); i++) {
                                DevicesFriendsListBean devicesFriendsListBean = new DevicesFriendsListBean();
                                devicesFriendsListBean.setCallId(friends.get(i).getCallId());
                                devicesFriendsListBean.setName(friends.get(i).getName());
                                devicesFriendsListBean.setSn(friends.get(i).getSn());
                                mDevicesData.add(devicesFriendsListBean);
                            }
                            initDevicesData(mDevicesData);
                        } else {

                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        TipDialog.show(ContactsActivity.this, e.getMessage(), TipDialog.TYPE.SUCCESS).doDismiss();
                        CommonUtils.showErrorToast(e.getMessage());
                    }


                    @Override
                    public void onComplete() {
                        Log.d("onSubscribe", "1231456");
                    }
                });
    }

    private void getContactsDatas() {
        dialog = WaitDialog.show(this, "请稍候...");
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

    @Override
    public void onAddDevicesFriendClick(Button bt, String friendId, String friendName) {
        bt.setText("请稍等....");
        RxHttp.postForm(UrlConfig.Device.GET_ADDFRIENDREQUEST)
                .addHeader("token", SPUtils.getInstance().getString("token"))
                .add("requestedSn", friendId)
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
//                        {"code":0,"msg":"OK","data":null}
                        Gson gson = new Gson();
                        AddDevicesFriend addDevicesFriend = gson.fromJson(s, AddDevicesFriend.class);
                        if (addDevicesFriend.msg.equals("OK")) {
                            Toast.makeText(ContactsActivity.this, "添加请求发送成功，等待对方接受。", Toast.LENGTH_LONG).show();
                            tips(bt);
                        } else {
                            Toast.makeText(ContactsActivity.this, addDevicesFriend.msg, Toast.LENGTH_LONG).show();
                            tips(bt);
                        }
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

    private void tips(Button bt) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bt.setText("发送邀请");
                    }
                });
            }
        }, 1500);
    }

    //修改好友名称
    @Override
    public void onEditDevicesNameClick(String callId, String devicesSn) {
        CustomDialog.build(ContactsActivity.this, R.layout.contacts_edit_devices__name_dialog, new CustomDialog.OnBindView() {
            @Override
            public void onBind(final CustomDialog dialog, View v) {
                EditText newFriendName = v.findViewById(R.id.add_device_dialog_edittext);
                Button cancle_bt = v.findViewById(R.id.cancle_bt);
                Button button_ok = v.findViewById(R.id.button_ok);

                button_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        posNetWork(newFriendName.getText().toString(), devicesSn, dialog);
                    }
                });
                cancle_bt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.doDismiss();
                    }
                });
            }
        }).setFullScreen(true).show();
    }

    private void posNetWork(String newFriendName, String devicesSn, CustomDialog dialog) {
        RxHttp.postForm(UrlConfig.Device.GET_SETFRIENDNAME)
                .addHeader("token", SPUtils.getInstance().getString("token"))
                .add("name", newFriendName)
                .add("requestedSn", devicesSn)
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
//                        {"code":0,"msg":"OK","data":null}
                        Gson gson = new Gson();
                        AddDevicesFriend addDevicesFriend = gson.fromJson(s, AddDevicesFriend.class);
                        if (addDevicesFriend.msg.equals("OK")) {
                            Toast.makeText(ContactsActivity.this, "修改成功", Toast.LENGTH_LONG).show();
                            dialog.doDismiss();
                            getDevicesfriend();
                        } else {
                            Toast.makeText(ContactsActivity.this, addDevicesFriend.msg, Toast.LENGTH_LONG).show();
                        }
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

    @Override
    public void onDeleteDevicesFriend(String friendSn, String friendName) {
        CustomDialog.build(ContactsActivity.this, R.layout.contacts_delete_devices_friend_dialog, new CustomDialog.OnBindView() {
            @Override
            public void onBind(final CustomDialog dialog, View v) {
                TextView tipsTextView = v.findViewById(R.id.invitation_tips);
                Button cancle_bt = v.findViewById(R.id.cancle_bt);
                Button button_ok = v.findViewById(R.id.button_ok);

                tipsTextView.setText(getResources().getString(R.string.contacts_add_devices19) + "\"" + friendName + "\"？");

                button_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        postDeleteNetWork(friendSn);
                    }
                });
                cancle_bt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.doDismiss();
                    }
                });
            }
        }).setFullScreen(true).show();
    }

    //6fa8295f4764b429 删除设备
    private void postDeleteNetWork(String friendSn) {
        RxHttp.postForm(UrlConfig.Device.GET_DELFRIEND)
                .addHeader("token", SPUtils.getInstance().getString("token"))
                .add("requestedSn", friendSn)
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
//                        {"code":0,"msg":"OK","data":null}
                        Gson gson = new Gson();
                        AddDevicesFriend addDevicesFriend = gson.fromJson(s, AddDevicesFriend.class);
                        if (addDevicesFriend.msg.equals("OK")) {
                            Toast.makeText(ContactsActivity.this, "删除成功", Toast.LENGTH_LONG).show();
                            dialog.doDismiss();
                            getDevicesfriend();
                        } else {
                            Toast.makeText(ContactsActivity.this, addDevicesFriend.msg, Toast.LENGTH_LONG).show();
                        }
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












