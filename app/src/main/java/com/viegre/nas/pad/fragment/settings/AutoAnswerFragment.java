package com.viegre.nas.pad.fragment.settings;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.SPUtils;
import com.djangoogle.framework.fragment.BaseFragment;
import com.google.gson.Gson;
import com.kongzue.dialog.v3.TipDialog;
import com.kongzue.dialog.v3.WaitDialog;
import com.viegre.nas.pad.activity.im.ContactsActivity;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.config.UrlConfig;
import com.viegre.nas.pad.databinding.FragmentAutoAnswerBinding;
import com.viegre.nas.pad.entity.ContactsBean;
import com.viegre.nas.pad.entity.DevicesFollowEntity;
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

    @Override
    protected void initialize() {
//        getFriendList();
    }

    private void getFriendList() {
        WaitDialog.show((AppCompatActivity) getContext(), "请稍候...");
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
                            TipDialog.show((AppCompatActivity) getContext(), "成功", TipDialog.TYPE.SUCCESS).doDismiss();
//                            initFriendData(mFriendData)
                        } else {
//                            Token_valid = false;
                            TipDialog.show((AppCompatActivity) getContext(), devicesFollowEntity.getMsg(), TipDialog.TYPE.ERROR).doDismiss();
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

    public static AutoAnswerFragment newInstance() {
        return new AutoAnswerFragment();
    }
}
