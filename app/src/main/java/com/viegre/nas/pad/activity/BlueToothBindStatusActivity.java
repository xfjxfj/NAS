package com.viegre.nas.pad.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.djangoogle.framework.activity.BaseActivity;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.databinding.ActivityBlueToothBindStatusBinding;
import com.viegre.nas.pad.service.MQTTService;
import com.viegre.nas.pad.util.CommonUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class BlueToothBindStatusActivity extends BaseActivity<ActivityBlueToothBindStatusBinding> {
    private MQTTService myService;
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myService = ((MQTTService.DownLoadBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void initialize() {
        bindService(new Intent(this, MQTTService.class), conn, Context.BIND_AUTO_CREATE);
        mViewBinding.bindNextButton.setOnClickListener(view -> {
            myService.getWelcomeBindStr().onWelcomeBind("绑定成功");
            CommonUtils.showToast("绑定成功，请稍等");
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void deviceBoundSuccess(String event) {
        if (!BusConfig.DEVICE_BOUND_SUCCESS.equals(event)) {
            return;
        }
        mViewBinding.blueBindingContainer.setVisibility(View.GONE);
        mViewBinding.blueBindContainer.setVisibility(View.VISIBLE);
    }
}