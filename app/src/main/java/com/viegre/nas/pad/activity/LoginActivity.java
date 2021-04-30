package com.viegre.nas.pad.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.bumptech.glide.Glide;
import com.djangoogle.framework.activity.BaseActivity;
import com.kongzue.dialog.v3.TipDialog;
import com.rxjava.rxlife.RxLife;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.config.UrlConfig;
import com.viegre.nas.pad.databinding.ActivityLoginBinding;
import com.viegre.nas.pad.entity.LoginEntity;
import com.viegre.nas.pad.entity.LoginResult;
import com.viegre.nas.pad.manager.PopupManager;
import com.viegre.nas.pad.popup.LoginTimePopup;
import com.viegre.nas.pad.service.AppService;
import com.viegre.nas.pad.util.CommonUtils;
import com.viegre.nas.pad.util.ImageStreamUtils;
import com.viegre.nas.pad.util.ZxingUtils;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import rxhttp.RxHttp;

import cn.wildfire.chat.kit.ChatManagerHolder;

/**
 * Created by レインマン on 2021/01/06 10:39 with Android Studio.
 */
public class LoginActivity extends BaseActivity<ActivityLoginBinding> implements View.OnClickListener {


    @Override
    protected void initialize() {
        mViewBinding.acivLoginQRCode.setImageBitmap(ZxingUtils.createQRCodewhite(SPUtils.getInstance().getString(SPConfig.ANDROID_ID), 500, 500, true));
        mViewBinding.acivLoginAccountCode.setTag(false);
//        mViewBinding.acetLoginAccountPhone.setText("18715008554");
//        mViewBinding.acetLoginAccountPassword.setText("abcd1234");

//        mViewBinding.acetLoginAccountPhone.setText("15357906428");
//        mViewBinding.acetLoginAccountPassword.setText("abcd123456");

        mViewBinding.acetLoginAccountPhone.setText("13168306428");
        mViewBinding.acetLoginAccountPassword.setText("abcd123456");
        initListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消获取图片验证码任务
        ThreadUtils.cancel(ThreadUtils.getSinglePool());
        //清除验证码sessionId缓存
        SPUtils.getInstance().remove(SPConfig.LOGIN_CODE_SESSION_ID);
    }

    private void initListener() {
        mViewBinding.actvLoginTabScan.setOnClickListener(this);
        mViewBinding.actvLoginTabAccount.setOnClickListener(this);
        mViewBinding.actvLoginTabPhoneCode.setOnClickListener(this);
        mViewBinding.acivLoginAccountCode.setOnClickListener(this);
        mViewBinding.actvLoginAccountBtn.setOnClickListener(this);
        mViewBinding.acivLoginExit.setOnClickListener(this);
        mViewBinding.actvLoginPhoneGetCode.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (R.id.actvLoginTabScan == view.getId()) {//点击扫码登录标签
            mViewBinding.actvLoginTabScan.setTextColor(Color.WHITE);
            mViewBinding.actvLoginTabScan.setBackgroundResource(R.drawable.login_tab_bg);
            mViewBinding.clLoginScan.setVisibility(View.VISIBLE);
            mViewBinding.actvLoginTabAccount.setTextColor(ColorUtils.getColor(R.color.network_password_popup_hint));
            mViewBinding.actvLoginTabAccount.setBackgroundResource(0);
            mViewBinding.clLoginAccount.setVisibility(View.GONE);
            mViewBinding.actvLoginTabPhoneCode.setTextColor(ColorUtils.getColor(R.color.network_password_popup_hint));
            mViewBinding.actvLoginTabPhoneCode.setBackgroundResource(0);
            mViewBinding.clLoginPhoneCode.setVisibility(View.GONE);
        } else if (R.id.actvLoginTabAccount == view.getId()) {//点击账号密码登录标签
            //如果没有加载验证码则请求获取
            if (!(Boolean) mViewBinding.acivLoginAccountCode.getTag()) {
                getCodeImage();
            }
            mViewBinding.actvLoginTabScan.setTextColor(ColorUtils.getColor(R.color.network_password_popup_hint));
            mViewBinding.actvLoginTabScan.setBackgroundResource(0);
            mViewBinding.clLoginScan.setVisibility(View.GONE);
            mViewBinding.actvLoginTabAccount.setTextColor(Color.WHITE);
            mViewBinding.actvLoginTabAccount.setBackgroundResource(R.drawable.login_tab_bg);
            mViewBinding.clLoginAccount.setVisibility(View.VISIBLE);
            mViewBinding.actvLoginTabPhoneCode.setTextColor(ColorUtils.getColor(R.color.network_password_popup_hint));
            mViewBinding.actvLoginTabPhoneCode.setBackgroundResource(0);
            mViewBinding.clLoginPhoneCode.setVisibility(View.GONE);
        } else if (R.id.actvLoginTabPhoneCode == view.getId()) {//点击手机验证码登录标签
            mViewBinding.actvLoginTabScan.setTextColor(ColorUtils.getColor(R.color.network_password_popup_hint));
            mViewBinding.actvLoginTabScan.setBackgroundResource(0);
            mViewBinding.clLoginScan.setVisibility(View.GONE);
            mViewBinding.actvLoginTabAccount.setTextColor(ColorUtils.getColor(R.color.network_password_popup_hint));
            mViewBinding.actvLoginTabAccount.setBackgroundResource(0);
            mViewBinding.clLoginAccount.setVisibility(View.GONE);
            mViewBinding.actvLoginTabPhoneCode.setTextColor(Color.WHITE);
            mViewBinding.actvLoginTabPhoneCode.setBackgroundResource(R.drawable.login_tab_bg);
            mViewBinding.clLoginPhoneCode.setVisibility(View.VISIBLE);

        } else if (R.id.acivLoginAccountCode == view.getId()) {//点击验证码
            getCodeImage();
        } else if (R.id.actvLoginAccountBtn == view.getId()) {//账号密码登录
            loginbyAccount();
        } else if (R.id.acivLoginExit == view.getId()) {//点击退出
            finish();
        } else if (R.id.actvLoginPhoneGetCode == view.getId()) {
            getPhoneNumber(mViewBinding.acetLoginPhone.getText().toString());
        }
    }

    private void getPhoneNumber(String phoneString) {
        phoneString = "15357906428";
        if (judgePhone(phoneString)) return;
        RxHttp.postForm(UrlConfig.User.GET_PHONENUMBER)
                .addHeader("Cookie", SPUtils.getInstance().getString(SPConfig.LOGIN_CODE_SESSION_ID))
                .add("phoneNumber", phoneString)
                .asString()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        Log.d("onSubscribe", d.toString());
                    }

                    @Override
                    public void onNext(@NonNull String s) {
                        TipDialog.show(LoginActivity.this, "成功", TipDialog.TYPE.SUCCESS).doDismiss();
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
     * 获取图片验证码
     */
    private void getCodeImage() {
        mViewBinding.acivLoginAccountCode.setClickable(false);
        ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<byte[]>() {
            @Override
            public byte[] doInBackground() throws Throwable {
                return ImageStreamUtils.getImageFromStream(UrlConfig.User.GET_IMAGE_CODE);
            }

            @Override
            public void onSuccess(byte[] result) {
                if (null == result) {
                    mViewBinding.acivLoginAccountCode.setImageResource(0);
                    mViewBinding.acivLoginAccountCode.setTag(false);
                } else {
                    Glide.with(mActivity).load(result).into(mViewBinding.acivLoginAccountCode);
                    mViewBinding.acivLoginAccountCode.setTag(true);
                }
                mViewBinding.acivLoginAccountCode.setClickable(true);
                mViewBinding.acetLoginAccountCode.setText("");
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                t.printStackTrace();
                mViewBinding.acivLoginAccountCode.setImageResource(0);
                mViewBinding.acivLoginAccountCode.setTag(false);
                mViewBinding.acivLoginAccountCode.setClickable(true);
                mViewBinding.acetLoginAccountCode.setText("");
            }
        });
    }

    /**
     * 账号密码登录
     */
    private void loginbyAccount() {
        mViewBinding.actvLoginAccountBtn.setClickable(false);
        String phone = String.valueOf(mViewBinding.acetLoginAccountPhone.getText()), password = String.valueOf(mViewBinding.acetLoginAccountPassword
                .getText()), code = String
                .valueOf(mViewBinding.acetLoginAccountCode.getText());
        if (judgePhone(phone)) return;
        if (password.length() < 8 || !RegexUtils.isMatch(".*(?:[a-zA-z]+.*\\d+)|(?:\\d+.*[a-zA-z]+).*", password)) {
            CommonUtils.showErrorToast(R.string.login_please_input_password);
            mViewBinding.actvLoginAccountBtn.setClickable(true);
            return;
        }
        if (StringUtils.isEmpty(code)) {
            CommonUtils.showErrorToast(R.string.login_please_input_code);
            mViewBinding.actvLoginAccountBtn.setClickable(true);
            return;
        }
        if (code.length() < 4) {
            CommonUtils.showErrorToast(R.string.login_code_error);
            mViewBinding.actvLoginAccountBtn.setClickable(true);
            return;
        }

        RxHttp.postForm(UrlConfig.User.LOGIN)
                .addHeader("Cookie", SPUtils.getInstance().getString(SPConfig.LOGIN_CODE_SESSION_ID))
                .add("code", code)
                .add("password", password)
                .add("phoneNumber", phone)
                .add("sn", SPUtils.getInstance().getString(SPConfig.ANDROID_ID))
                .asResponse(LoginEntity.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<LoginEntity>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onNext(@NonNull LoginEntity loginEntity) {
                        //添加公共请求头
                        RxHttp.setOnParamAssembly(param -> param.addHeader("token", loginEntity.getToken()));
                        SPUtils.getInstance().put(SPConfig.PHONE, phone);
                        SPUtils.getInstance().put("token", loginEntity.getToken());
                        callLogin(phone);//音视频登录
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        CommonUtils.showErrorToast(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        SPUtils.getInstance().remove(SPConfig.LOGIN_CODE_SESSION_ID);
                        mViewBinding.actvLoginAccountBtn.setClickable(true);
                    }
                });
    }

    private boolean judgePhone(String phone) {
        if (StringUtils.isEmpty(phone)) {
            CommonUtils.showErrorToast(R.string.login_please_input_phone_number);
            mViewBinding.actvLoginAccountBtn.setClickable(true);
            return true;
        }
        if (!RegexUtils.isMobileExact(phone)) {
            CommonUtils.showErrorToast(R.string.login_phone_error);
            mViewBinding.actvLoginAccountBtn.setClickable(true);
            return true;
        }
        return false;
    }

    private void callLogin(String phone) {
        //音视频登录
//        String phoneNumber = "15357906428";
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
                        .putString("mToken", loginResult.getToken())
                        .apply();
                setLoginTime();
//                postUserId(loginResult.getToken());
//                Intent intent = new Intent(ContactsActivity.this, MainActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(intent);
//                finish();
            }

            @Override
            public void onUiFailure(int code, String msg) {
                if (isFinishing()) {
                    return;
                }
                Toast.makeText(LoginActivity.this, "登录失败：" + code + " " + msg, Toast.LENGTH_SHORT).show();
//                loginButton.setEnabled(true);
            }
        });
    }

    private void postUserId(String userid) {
        RxHttp.postForm(UrlConfig.Call.GET_REPORTINFO)
                .add("callId", userid)
                .asString()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull String s) {
                        //添加公共请求头
                        Log.d("", "");
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
//                        CommonUtils.showErrorToast(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        SPUtils.getInstance().remove(SPConfig.LOGIN_CODE_SESSION_ID);
//                        mViewBinding.actvLoginAccountBtn.setClickable(true);
                    }
                });
    }

    /**
     * 设置登录有效时间
     */
    private void setLoginTime() {
        PopupManager.INSTANCE.showCustomXPopup(this, new LoginTimePopup(this));
    }
}
