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
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import cn.wildfirechat.remote.ChatManager;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import rxhttp.RxHttp;

import static android.app.PendingIntent.getActivity;

/**
 * 联系人相关类
 */

public class
ContactsActivity extends BaseActivity<ActivityContactsBinding> implements View.OnClickListener, ContactsRvDevicesAdapter.addDevicesFriend, ContactsRvDevicesAdapter.editDevicesName, ContactsRvDevicesAdapter.deleteDevicesFriend {

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
                                    AccectRequest(2, requestID, dialog);
//                                    dialog.doDismiss();
                                }
                            });
                        }
                    }).setFullScreen(true).show();
                }

                @Override
                public void onTipsdevicesFriendStatus(String requestID) {
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
                                    if (requestID.equals("1")) {
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

    //***接受或者拒绝好友申请
    @SuppressLint("UseValueOf")
    private void AccectRequest(int status, String friendId, CustomDialog dialog) {
        RxHttp.postForm(UrlConfig.Device.GET_ADDFRIENDRESULT)
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
                        if (addDevicesFriend.msg.equals("OK")) {
                            if (status == 1) {
                                Toast.makeText(ContactsActivity.this, "添加成功", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ContactsActivity.this, "已拒绝该好友添加请求", Toast.LENGTH_LONG).show();
                            }
                            getDevicesfriend();
                        } else {
                            CommonUtils.showErrorToast(addDevicesFriend.msg);
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
        contactsRvRecordAdapter = new ContactsRvRecordAdapter(this);
        contactsRv1.setAdapter(contactsRvRecordAdapter);
    }

    //获取设备好友
    private void getDevicesfriend() {
        if (dialog == null) {
            dialog = WaitDialog.show(this, "请稍候...");
        }
        RxHttp.get(UrlConfig.Device.GET_GETFRIENDS)
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
//                        {"code":0,"msg":"OK","data":[{"phone":"13168306428","nickName":null,"avatar":null,"picData":null,"callId":"VaVvVvii","status":{"desc":"管理员","code":3},"boundTime":"2021-06-30 17:32:36.725.72.7"},{"phone":"15357906428","nickName":"CCTV","avatar":"defe1b2f-0f4a-43ba-9285-39dc05e4f62e.jpg","picData":"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AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMDIAAAAAAAAADAAGQwGWAAAADLAAAAAAAAAAOQAAAyYAywAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA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","callId":"sws8s888","status":{"desc":"管理员","code":3},"boundTime":"2021-07-06 10:40:32.766.76.7"}]}
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

    //添加设备好友
    @Override
    public void onAddDevicesFriendClick(Button bt, String friendId, String friendName) {
        if (friendId.equals("")) {
            Toast.makeText(this, "请输入序列号或联系人名称", Toast.LENGTH_LONG).show();
        } else if (friendName.equals("")) {
            Toast.makeText(this, "请输入序列号或联系人名称", Toast.LENGTH_LONG).show();
        } else {
            bt.setText("请稍等....");
            RxHttp.postForm(UrlConfig.Device.GET_ADDFRIENDREQUEST)
                    .add("requestedSn", friendId)
                    .add("sn", SPUtils.getInstance().getString(SPConfig.ANDROID_ID))
                    .asString()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            Log.d("onSubscribe", d.toString());
                        }

                        // {"msg":"token verify fail","code":"4111"}   2021年5月21日
                        @Override
                        public void onNext(@NonNull String s) {
//                        {"code":0,"msg":"OK","data":null}
                            Gson gson = new Gson();
                            AddDevicesFriend addDevicesFriend = gson.fromJson(s, AddDevicesFriend.class);
                            Log.d("GET_ADDFRIENDREQUEST：", addDevicesFriend.toString());
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
                        if (newFriendName.getText().toString().trim().equals("")) {
                            Toast.makeText(mActivity, "请输入新的名称", Toast.LENGTH_LONG).show();
                        } else {
                            posNetWork(newFriendName.getText().toString(), devicesSn, dialog);
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
    private void posNetWork(String newFriendName, String devicesSn, CustomDialog dialog) {
        RxHttp.postForm(UrlConfig.Device.GET_SETFRIENDNAME)
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

    //删除设备好友弹框提示
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
                        dialog.doDismiss();
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












