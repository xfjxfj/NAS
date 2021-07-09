package com.viegre.nas.pad.fragment.settings;

import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.SPUtils;
import com.djangoogle.framework.fragment.BaseFragment;
import com.google.gson.Gson;
import com.kongzue.dialog.v3.TipDialog;
import com.kongzue.dialog.v3.WaitDialog;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.activity.im.ContactsActivity;
import com.viegre.nas.pad.adapter.AutoAnswerRvAdapter;
import com.viegre.nas.pad.adapter.ContactsRvDevicesAdapter;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.config.UrlConfig;
import com.viegre.nas.pad.databinding.FragmentAutoAnswerBinding;
import com.viegre.nas.pad.entity.ContactsBean;
import com.viegre.nas.pad.entity.DataBeanXX;
import com.viegre.nas.pad.entity.DevicesFollowEntity;
import com.viegre.nas.pad.entity.DevicesFriendList;
import com.viegre.nas.pad.entity.DevicesFriendsListBean;
import com.viegre.nas.pad.util.CommonUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import rxhttp.RxHttp;

/**
 * Created by レインマン on 2020/12/17 17:36 with Android Studio.
 */
public class AutoAnswerFragment extends BaseFragment<FragmentAutoAnswerBinding> {
    private final List<ContactsBean> mFriendData = new ArrayList<>();
    private AutoAnswerRvAdapter mAdapter;

    @Override
    protected void initialize() {
//        mViewBinding.fragmentAutoRv
        getFriendList();
    }


    private void getFriendList() {
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
                            List<DataBeanXX> data = devicesFollowEntity.getData();
                            if (null != data) {
                                for (DataBeanXX datum : data) {
                                    String nickName = String.valueOf(datum.getNickName());
                                    String phone = datum.getPhone();
                                    String picdata = String.valueOf(datum.getPicData());
                                    String userid = datum.getCallId();
                                    mFriendData.add(new ContactsBean(userid, picdata, nickName, phone));
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
//                            TipDialog.show((AppCompatActivity) mActivity, "成功", TipDialog.TYPE.SUCCESS).doDismiss();
                            getDevicesfriend(mFriendData);
                        } else {
//                            Token_valid = false;
//                            TipDialog.show((AppCompatActivity) mActivity, devicesFollowEntity.getMsg(), TipDialog.TYPE.ERROR).doDismiss();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        TipDialog.show((AppCompatActivity) getContext(), e.getMessage(), TipDialog.TYPE.SUCCESS).doDismiss();
                        CommonUtils.showErrorToast(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d("", "");
                    }
                });
    }

    private void getDevicesfriend(List<ContactsBean> mFriendData) {
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
                            for (int i = 0; i < friends.size(); i++) {
                                mFriendData.add(new ContactsBean(friends.get(i).getCallId(), "", friends.get(i).getName(), ""));
                            }
                            initDevicesData(mFriendData);
                        } else {

                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
//                        TipDialog.show(ContactsActivity.this, e.getMessage(), TipDialog.TYPE.SUCCESS).doDismiss();
                        CommonUtils.showErrorToast(e.getMessage());
                    }


                    @Override
                    public void onComplete() {
                        Log.d("onSubscribe", "1231456");
                    }
                });
    }

    private void initDevicesData(List<ContactsBean> mDevicesData) {
        if (mDevicesData.size() == 0) {
            mViewBinding.fragmentAutoRv.setVisibility(View.GONE);
            mViewBinding.fragmentAutotips.setVisibility(View.VISIBLE);
            return;
        }
        //初始化数据
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        //设置布局管理器
        mViewBinding.fragmentAutoRv.setLayoutManager(linearLayoutManager2);
        //创建适配器，将数据传递给适配器
        //设置适配器adapter
        mAdapter = new AutoAnswerRvAdapter(mDevicesData);
        mViewBinding.fragmentAutoRv.setAdapter(mAdapter);
    }

    public static AutoAnswerFragment newInstance() {
        return new AutoAnswerFragment();
    }
}
