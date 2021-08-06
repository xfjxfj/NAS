package com.viegre.nas.pad.fragment.settings;

import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.SPUtils;
import com.djangoogle.framework.fragment.BaseFragment;
import com.google.gson.Gson;
import com.kongzue.dialog.v3.TipDialog;
import com.viegre.nas.pad.activity.MainActivity;
import com.viegre.nas.pad.adapter.AutoAnswerRvAdapter;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.config.UrlConfig;
import com.viegre.nas.pad.databinding.FragmentAutoAnswerBinding;
import com.viegre.nas.pad.entity.ContactsBean;
import com.viegre.nas.pad.entity.DataBeanXX;
import com.viegre.nas.pad.entity.DevicesFollowEntity;
import com.viegre.nas.pad.entity.DevicesFriendList;
import com.viegre.nas.pad.entity.MyfriendDataFriend;
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
//      mViewBinding.fragmentAutoRv
//		getFriendList();
        initDevicesData(MainActivity.mDevicesFriend);
    }

    private void initDevicesData(List<MyfriendDataFriend> mDevicesData) {
        if (mDevicesData.size() == 0) {
            mViewBinding.fragmentAutoRv.setVisibility(View.GONE);
            mViewBinding.fragmentAutotips.setVisibility(View.VISIBLE);
            return;
        } else {
            mViewBinding.fragmentAutoRv.setVisibility(View.VISIBLE);
            mViewBinding.fragmentAutotips.setVisibility(View.GONE);
        }
        //初始化数据
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        //设置布局管理器
        mViewBinding.fragmentAutoRv.setLayoutManager(linearLayoutManager2);
        //创建适配器，将数据传递给适配器
        //设置适配器adapter
        mAdapter = new AutoAnswerRvAdapter(mActivity,mDevicesData);
        mViewBinding.fragmentAutoRv.setAdapter(mAdapter);
    }

    public static AutoAnswerFragment newInstance() {
        return new AutoAnswerFragment();
    }
}
