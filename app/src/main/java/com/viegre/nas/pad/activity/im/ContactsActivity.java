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
import com.viegre.nas.pad.entity.DataBeanXX;
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

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import cn.wildfire.chat.kit.common.OperateResult;
import cn.wildfire.chat.kit.contact.ContactViewModel;
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
    private ContactsRvRecordAdapter contactsRvRecordAdapter;
    private MQTTService myService;
    private String newFriendName = "";
    private String TAG = CommonUtils.getFileName();
    //处理mqtt那边传递过来的消息
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myService = ((MQTTService.DownLoadBinder) service).getService();
//             回调接口
            myService.setTipsDevicesFriend(new MQTTService.TipsDevicesFriend() {
                @Override
                public void onTipsdevicesFriend(String requestID, String callId) {
                    DialogSettings.isUseBlur = true;
                    CustomDialog.build(ContactsActivity.this, R.layout.contacts_add_devices_invitation_dialog, new CustomDialog.OnBindView() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onBind(final CustomDialog dialog, View v) {
                            TextView invitation_tips = v.findViewById(R.id.invitation_tips);
                            Button cancle_bt = v.findViewById(R.id.cancle_bt);
                            Button button_ok = v.findViewById(R.id.button_ok);

                            invitation_tips.setText(getResources().getString(R.string.contacts_add_devices11) + "\"" + requestID + "\"" + getResources()
                                    .getString(R.string.contacts_add_devices12));
                            button_ok.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
//                                    接受好友邀请
                                    AccectRequest(1, requestID, dialog, callId);
                                }
                            });
                            cancle_bt.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AccectRequest(2, requestID, dialog, callId);
//                                    dialog.doDismiss();
                                }
                            });
                        }
                    }).setFullScreen(true).show();
                }

                @Override
                public void onTipsdevicesFriendStatus(String requestID, String requestedSn, String callid,String name) {
                    String status = "";
                    switch (requestID) {
                        case "1":
                            status = "接受";
                            break;
                        case "2":
                            status = "拒绝";
                            break;
                    }
                    String finalStatus = status;
                    CustomDialog.build(ContactsActivity.this, R.layout.contacts_add_devices_invitation_dialog, new CustomDialog.OnBindView() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onBind(final CustomDialog dialog, View v) {
                            TextView dialogTips = v.findViewById(R.id.dialogtips);
                            TextView invitation_tips = v.findViewById(R.id.invitation_tips);
                            Button cancle_bt = v.findViewById(R.id.cancle_bt);
                            Button button_ok = v.findViewById(R.id.button_ok);

                            dialogTips.setText("好友请求提示");
                            invitation_tips.setText("对方已经" + finalStatus + "了你的好友请求");
                            cancle_bt.setVisibility(View.GONE);
                            button_ok.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.doDismiss();
                                    if (requestID.equals("1")) {//接受好友
//										getDevicesfriend();
                                        posNetWork(newFriendName, requestedSn, null, callid,name);//添加好友后，修改名称
                                    } else {//拒绝好友
                                        getDevicesfriend();
                                    }
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
    private ContactViewModel contactViewModel;

    //***接受或者拒绝好友申请
    @SuppressLint("UseValueOf")
    private void AccectRequest(int status, String friendId, CustomDialog dialog, String callId) {
        RxHttp.postForm(UrlConfig.Device.GET_ADDFRIENDRESULT)
                .addHeader(SPConfig.TOKEN, SPUtils.getInstance().getString(SPConfig.DEVICES_TOKEN))
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
                        Log.d("GET_ADDFRIENDRESULT:", addDevicesFriend.toString());
                        if (addDevicesFriend.getMsg().equals("OK")) {
                            if (status == 1) {
                                Toast.makeText(ContactsActivity.this, "添加成功", Toast.LENGTH_LONG).show();
                                accept(callId);
                            } else {
                                Toast.makeText(ContactsActivity.this, "已拒绝该好友添加请求", Toast.LENGTH_LONG).show();
                            }
                            getDevicesfriend();
                        } else {
                            CommonUtils.showErrorToast(addDevicesFriend.getMsg());
                        }
                        dismiss(dialog);
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

    private void dismiss(CustomDialog dialog) {
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
    }

    @Override
    protected void initialize() {
        initView();

    }

    private void initView() {
//        EventBus.getDefault().postSticky(BusConfig.STOP_MSC);
//        ChatManager.Instance().getUserInfo(fileRecord.userId, false)
        getContactsDatas();//获取联系人好友
        getDevicesfriend();//获取设备好友
        contactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);//野火修改备注需要
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
//      initFriendData(mContactsData);

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

    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
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
        ContactsRvDevicesAdapter adapter = new ContactsRvDevicesAdapter(mActivity, mDevicesData, inflate);
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

    @SuppressLint("NewApi")
    private void initRecordData(List<String> mRecordData) {
        //初始化数据
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        //设置布局管理器
        contactsRv1.setLayoutManager(linearLayoutManager);
        //创建适配器，将数据传递给适配器
        //设置适配器adapter
        contactsRvRecordAdapter = new ContactsRvRecordAdapter(this, mFriendData, textRecord);
        contactsRv1.setAdapter(contactsRvRecordAdapter);
    }

    //获取设备好友
    private void getDevicesfriend() {
        if (dialog == null) {
            dialog = WaitDialog.show(this, "请稍候...");
        }
        RxHttp.get(UrlConfig.Device.GET_GETFRIENDS)
                .addHeader(SPConfig.TOKEN, SPUtils.getInstance().getString(SPConfig.DEVICES_TOKEN))
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
                            CommonUtils.showErrorDialog(ContactsActivity.this, DevicesFriendList.getMsg());
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
                .addHeader(SPConfig.TOKEN, SPUtils.getInstance().getString(SPConfig.DEVICES_TOKEN))
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
                        if (devicesFollowEntity.getMsg().equals("OK")) {//返回数据正确
                            List<DataBeanXX> data = devicesFollowEntity.getData();
                            if (null != data) {
                                for (DataBeanXX datum : data) {
                                    String nickName = String.valueOf(datum.getNickName());
                                    String phone = datum.getPhone();
                                    String picdata = String.valueOf(datum.getPicData());
                                    String userid = datum.getCallId();
                                    mFriendData.add(new ContactsBean(userid, picdata, nickName, phone));
                                }
                            }
                            TipDialog.show(ContactsActivity.this, "成功", TipDialog.TYPE.SUCCESS).doDismiss();
                            initFriendData(mFriendData);
                            ifRecordList();
                            Token_valid = true;
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
                MessageDialog.show(this, "提示", "确定删除通话记录吗", "是", "取消").setOnOkButtonClickListener(new OnDialogButtonClickListener() {
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
                                        TipDialog.show(ContactsActivity.this, "成功！", TipDialog.TYPE.SUCCESS)
                                                .setOnDismissListener(new OnDismissListener() {
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
                }).setButtonOrientation(LinearLayout.VERTICAL);
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

    //添加设备好友
    @Override
    public void onAddDevicesFriendClick(Button bt, String friendId, String friendName) {
        if (friendId.equals("")) {
            Toast.makeText(this, "请输入序列号", Toast.LENGTH_LONG).show();
        } else {

            newFriendName = friendName;//本地复制名字，为添加成功后 修改好友名称赋值
            bt.setText("请稍等....");
            RxHttp.postForm(UrlConfig.Device.GET_ADDFRIENDREQUEST)
                    .addHeader(SPConfig.TOKEN, SPUtils.getInstance().getString(SPConfig.DEVICES_TOKEN))
                    .add("requestedSn", friendId)
                    .add("sn", SPUtils.getInstance().getString(SPConfig.ANDROID_ID))
                    .asString()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            Log.d("onSubscribe", d.toString());
                        }

                        @SuppressLint("LongLogTag")
                        @Override
                        public void onNext(@NonNull String s) {
                            Gson gson = new Gson();
                            AddDevicesFriend addDevicesFriend = gson.fromJson(s, AddDevicesFriend.class);
                            Log.d("onAddDevicesFriendClick ：", addDevicesFriend.toString());
                            if (addDevicesFriend.getMsg().equals("OK")) {
                                Toast.makeText(ContactsActivity.this, "添加请求发送成功，等待对方接受。", Toast.LENGTH_LONG).show();
                                invite(addDevicesFriend.getData().getCallId(), "");
                            } else {
                                Toast.makeText(ContactsActivity.this, addDevicesFriend.getMsg(), Toast.LENGTH_LONG).show();
                            }
                            tips(bt);
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
                EditText fName = v.findViewById(R.id.add_device_dialog_edittext);
                Button cancle_bt = v.findViewById(R.id.cancle_bt);
                Button button_ok = v.findViewById(R.id.button_ok);
                button_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        newFriendName = fName.getText().toString().trim();
                        if (newFriendName.equals("")) {
                            Toast.makeText(mActivity, "请输入新的名称", Toast.LENGTH_LONG).show();
                        } else {
                            posNetWork(newFriendName, devicesSn, dialog, callId,newFriendName);
                        }
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

    //修改设备名称
    private void posNetWork(String newFriendName, String devicesSn, CustomDialog dialog, String callId,String name) {
        String isName = "";
        if (newFriendName.equals(name)) {
            isName = newFriendName;
        } else {
            isName = name;
        }
        String finalIsName = isName;
        RxHttp.postForm(UrlConfig.Device.GET_SETFRIENDNAME)
                .addHeader(SPConfig.TOKEN, SPUtils.getInstance().getString(SPConfig.DEVICES_TOKEN))
                .add("name", finalIsName)
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
                        Gson gson = new Gson();
                        AddDevicesFriend addDevicesFriend = gson.fromJson(s, AddDevicesFriend.class);
                        if (addDevicesFriend.getMsg().equals("OK")) {
                            Toast.makeText(ContactsActivity.this, "修改成功", Toast.LENGTH_LONG).show();
                            changeAlias(callId, finalIsName);
                            if (dialog != null) {
                                dialog.doDismiss();
                            }
                        } else {
                            Toast.makeText(ContactsActivity.this, addDevicesFriend.getMsg(), Toast.LENGTH_LONG).show();
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

    //删除设备好友弹框提示
    @Override
    public void onDeleteDevicesFriend(String friendSn, String friendName, String callId) {
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
                        dialog.doDismiss();
                        postDeleteNetWork(friendSn, callId);
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
    private void postDeleteNetWork(String friendSn, String callid) {
        RxHttp.postForm(UrlConfig.Device.GET_DELFRIEND)
                .addHeader(SPConfig.TOKEN, SPUtils.getInstance().getString(SPConfig.DEVICES_TOKEN))
                .add("requestedSn", friendSn)
                .add("sn", SPUtils.getInstance().getString(SPConfig.ANDROID_ID))
                .asString()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        Log.d("onSubscribe", d.toString());
                    }

                    //1794d52b003521f4
//                    {"msg":"token verify fail","code":"4111"}   2021年5月21日
                    @Override
                    public void onNext(@NonNull String s) {
//                        {"code":0,"msg":"OK","data":null}
                        Gson gson = new Gson();
                        AddDevicesFriend addDevicesFriend = gson.fromJson(s, AddDevicesFriend.class);
                        if (addDevicesFriend.getMsg().equals("OK")) {
                            Toast.makeText(ContactsActivity.this, "删除成功", Toast.LENGTH_LONG).show();
                            delefriened(callid);
                            if (dialog != null) {
                                dialog.doDismiss();
                            }

                        } else {
                            Toast.makeText(ContactsActivity.this, addDevicesFriend.getMsg(), Toast.LENGTH_LONG).show();
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

    /**
     * 野火添加好友請求
     */
    public void invite(String userId, String userTips) {
        ContactViewModel contactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
        contactViewModel.invite(userId, userTips)
                .observe(this, new androidx.lifecycle.Observer<Boolean>() {
                    @Override
                    public void onChanged(@Nullable Boolean aBoolean) {
                        if (aBoolean) {
                            Toast.makeText(mActivity, "好友邀请已发送", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mActivity, "添加好友失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * 修改对好友的备注
     */
    private void changeAlias(String userId, String displayName) {
        contactViewModel.setFriendAlias(userId, displayName).observe(this, new androidx.lifecycle.Observer<OperateResult<Integer>>() {
            @Override
            public void onChanged(OperateResult<Integer> integerOperateResult) {
                if (integerOperateResult.isSuccess()) {
//                    Toast.makeText(SetAliasActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                    Log.d(CommonUtils.getFileName() + CommonUtils.getLineNumber(), "修改别名成功");
                } else {
//                    Toast.makeText(SetAliasActivity.this, "修改别名错误：" + integerOperateResult.getErrorCode(), Toast.LENGTH_SHORT).show();
                    Log.d(CommonUtils.getFileName() + CommonUtils.getLineNumber(), "修改别名失败" + integerOperateResult.getErrorCode());
                }
                getDevicesfriend();
            }
        });
    }

    /**
     * 野火好友列表接受好友邀请
     */
    void accept(String callId) {
        contactViewModel.acceptFriendRequest(callId).observe((LifecycleOwner) mActivity, aBoolean -> {
            if (aBoolean) {
                Log.d("", "accept: " + "已经接受好友请求");
            } else {
                Log.d("", "accept: " + "好友请求失败");
            }
        });
    }

    /**
     * delefriend
     */
    private void delefriened(String userid) {
        contactViewModel.deleteFriend(userid).observe(
                (LifecycleOwner) mActivity, booleanOperateResult -> {
                    if (booleanOperateResult.isSuccess()) {
//                        Toast.makeText(mActivity, "delete friend OK " + booleanOperateResult.getErrorCode(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "delefriened: 删除好友成功");
                    } else {
//                        Toast.makeText(mActivity, "delete friend error " + booleanOperateResult.getErrorCode(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "delefriened: 删除好友失败");
                    }
                    getDevicesfriend();
                }
        );
    }
}












