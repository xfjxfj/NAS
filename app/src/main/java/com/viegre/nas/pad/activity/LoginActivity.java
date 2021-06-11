package com.viegre.nas.pad.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.bumptech.glide.Glide;
import com.djangoogle.framework.activity.BaseActivity;
import com.google.gson.Gson;
import com.kongzue.dialog.v3.TipDialog;
import com.kongzue.dialog.v3.WaitDialog;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.config.UrlConfig;
import com.viegre.nas.pad.databinding.ActivityLoginBinding;
import com.viegre.nas.pad.entity.LoginEntity;
import com.viegre.nas.pad.entity.LoglinCodeEntity;
import com.viegre.nas.pad.manager.PopupManager;
import com.viegre.nas.pad.popup.LoginTimePopup;
import com.viegre.nas.pad.util.CommonUtils;
import com.viegre.nas.pad.util.ImageStreamUtils;
import com.viegre.nas.pad.util.ZxingUtils;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import rxhttp.RxHttp;
import rxhttp.RxHttpPlugins;

/**
 * Created by レインマン on 2021/01/06 10:39 with Android Studio.
 */
public class LoginActivity extends BaseActivity<ActivityLoginBinding> implements View.OnClickListener {

    private CountDownTimer timer;

    @Override
    protected void initialize() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("login", SPUtils.getInstance().getString(SPConfig.ANDROID_ID));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("login json:", jsonObject.toString());
        mViewBinding.acivLoginQRCode.setImageBitmap(ZxingUtils.createQRCodewhite(jsonObject.toString(), 500, 500, true));
        mViewBinding.acivLoginAccountCode.setTag(false);

        mViewBinding.acetLoginAccountPhone.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);//控制键盘不全屏
        mViewBinding.acetLoginAccountPassword.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);//控制键盘不全屏
        mViewBinding.acetLoginAccountCode.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);//控制键盘不全屏

        mViewBinding.acetLoginPhone.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);//控制键盘不全屏
        mViewBinding.acetLoginPhoneCode.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);//控制键盘不全屏
//        mViewBinding.acetLoginAccountPassword.setText("abcd123456");
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
        mViewBinding.actvLoginPhoneBtn.setOnClickListener(this);
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        mViewBinding.acivLoginExit.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipData mClipData = ClipData.newPlainText("Label", SPUtils.getInstance().getString(SPConfig.ANDROID_ID));
                cm.setPrimaryClip(mClipData);
                Toast.makeText(LoginActivity.this, SPUtils.getInstance().getString(SPConfig.ANDROID_ID) + "-----已复制到剪切板", Toast.LENGTH_LONG).show();
                return false;
            }
        });
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
            mViewBinding.actvLoginPhoneBtn.setText("验证码登录");

            mViewBinding.actvLoginTabScan.setTextColor(ColorUtils.getColor(R.color.network_password_popup_hint));
            mViewBinding.actvLoginTabScan.setBackgroundResource(0);
            mViewBinding.clLoginScan.setVisibility(View.GONE);
            mViewBinding.actvLoginTabAccount.setTextColor(ColorUtils.getColor(R.color.network_password_popup_hint));
            mViewBinding.actvLoginTabAccount.setBackgroundResource(0);
            mViewBinding.clLoginAccount.setVisibility(View.GONE);
            mViewBinding.actvLoginTabPhoneCode.setTextColor(Color.WHITE);
            mViewBinding.actvLoginTabPhoneCode.setBackgroundResource(R.drawable.login_tab_bg);
            mViewBinding.clLoginPhoneCode.setVisibility(View.VISIBLE);
            mViewBinding.actvLoginPhoneBtn.setClickable(true);
        } else if (R.id.acivLoginAccountCode == view.getId()) {//点击验证码
            getCodeImage();
        } else if (R.id.actvLoginAccountBtn == view.getId()) {
            //账号密码登录
            loginbyAccount();
        } else if (R.id.acivLoginExit == view.getId()) {//点击退出
            finish();
        } else if (R.id.actvLoginPhoneGetCode == view.getId()) {
            getPhoneNumberCode(mViewBinding.acetLoginPhone.getText().toString());
            timeStart(60000);
        } else if (R.id.actvLoginPhoneBtn == view.getId()) {
            phoneCodeLogin(mViewBinding.acetLoginPhone.getText().toString(), mViewBinding.acetLoginPhoneCode.getText().toString());
        }
    }

    private void timeStart(long time) {
        timer = new CountDownTimer(time, 1000) {
            public void onTick(long millisUntilFinished) {
                mViewBinding.actvLoginPhoneGetCode.setText("倒计时" + millisUntilFinished / 1000 + "秒");
                mViewBinding.actvLoginPhoneGetCode.setEnabled(false);
                mViewBinding.actvLoginPhoneGetCode.setTextColor(Color.WHITE);
            }

            public void onFinish() {
                mViewBinding.actvLoginPhoneGetCode.setText("获取验证码");
                mViewBinding.actvLoginPhoneGetCode.setEnabled(true);
                mViewBinding.actvLoginPhoneGetCode.setTextColor(getResources().getColor(R.color.login_get_code));
            }
        };
    }

    //验证码登录
    private void phoneCodeLogin(String phone, String phoneCode) {
        if (judgePhone(phone)) {
            return;
        }
        if (judgeCode(phoneCode)) {
            return;
        }
        RxHttp.postForm(UrlConfig.User.GET_loginWithSms)
                .add("phoneNumber", phone)
                .add("code", phoneCode)
                .asResponse(LoginEntity.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<LoginEntity>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        Log.d("onSubscribe", d.toString());
                    }

                    @Override
                    public void onNext(@NonNull LoginEntity loginEntity) {
//                        {"code":0,"msg":"OK","data":{"bindList":[{"id":1010,"sn":"f6fe25922fa2028a","wlanMac":null,"bluetoothMac":null,"barCode":null,"qrCode":null,"channelName":null,"fwVersion":null,"hwVersion":null,"status":0,"activeTime":null,"createTime":null}],"token":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpdGVtIjp7Iml0ZW1JZCI6IjE1MzU3OTA2NDI4IiwiaXRlbVR5cGUiOiIxIn0sImlzcyI6ImF1dGgwIiwiZXhwIjoxNjIwNDE4MDAzfQ.b53lDMgUKJ7El60Jy5eR1hiTOgGDPxs1us9tdvpY0fc"}}
                        RxHttpPlugins.init(RxHttpPlugins.getOkHttpClient())
                                .setOnParamAssembly(param -> param.addHeader("token", loginEntity.getToken()));
                        SPUtils.getInstance().put(SPConfig.PHONE, phone);
//                        SPUtils.getInstance().put("token", loginEntity.getToken());
                        setLoginTime();
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

    private void getPhoneNumberCode(String phoneString) {
        if (judgePhone(phoneString)) {
            return;
        }
        RxHttp.postForm(UrlConfig.User.GET_PHONENUMBER).add("phoneNumber", phoneString).asString()
//                .asResponse(LoglinCodeEntity.class)
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                Log.d("onSubscribe", d.toString());
            }

            @Override
            public void onNext(@NonNull String s) {
//              {"code":0,"msg":"OK","data":null}
                CommonUtils.showSuccessDialog(LoginActivity.this, "短信发送成功");
                Gson gson = new Gson();
                LoglinCodeEntity loglinCodeEntity = gson.fromJson(s, LoglinCodeEntity.class);
                String msg = loglinCodeEntity.getMsg();
                timer.start();
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
//        mViewBinding.acivLoginAccountCode.setClickable(false);
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
//                mViewBinding.acivLoginAccountCode.setClickable(true);
                mViewBinding.acetLoginAccountCode.setText("");
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                t.printStackTrace();
                mViewBinding.acivLoginAccountCode.setImageResource(0);
                mViewBinding.acivLoginAccountCode.setTag(false);
//                mViewBinding.acivLoginAccountCode.setClickable(true);
                mViewBinding.acetLoginAccountCode.setText("");
            }
        });
    }

    /**
     * 账号密码登录
     */
    private void loginbyAccount() {
//        mViewBinding.actvLoginAccountBtn.setClickable(false);
        String phone = String.valueOf(mViewBinding.acetLoginAccountPhone.getText()), password = String.valueOf(mViewBinding.acetLoginAccountPassword.getText()), code = String
                .valueOf(mViewBinding.acetLoginAccountCode.getText());
        if (judgePhone(phone)) {
            return;
        }
        if (password.length() < 8 || !RegexUtils.isMatch(".*(?:[a-zA-z]+.*\\d+)|(?:\\d+.*[a-zA-z]+).*", password)) {
            CommonUtils.showErrorToast(R.string.login_please_input_password);
//            mViewBinding.actvLoginAccountBtn.setClickable(true);
            return;
        }
        if (StringUtils.isEmpty(code)) {
            CommonUtils.showErrorToast(R.string.login_please_input_code);//请输入验证码
//            mViewBinding.actvLoginAccountBtn.setClickable(true);
            return;
        }
        if (code.length() < 4) {
            CommonUtils.showErrorToast(R.string.login_code_error);//验证码错误
//            mViewBinding.actvLoginAccountBtn.setClickable(true);
            return;
        }
        TipDialog show = WaitDialog.show(this, "请稍候...");
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
                        RxHttpPlugins.init(RxHttpPlugins.getOkHttpClient())
                                .setOnParamAssembly(param -> param.addHeader("token", loginEntity.getToken()));
                        SPUtils.getInstance().put(SPConfig.PHONE, phone);
                        SPUtils.getInstance().put("token", loginEntity.getToken());
                        TipDialog.show(LoginActivity.this, "成功", TipDialog.TYPE.SUCCESS).doDismiss();
                        setLoginTime();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        CommonUtils.showErrorDialog(LoginActivity.this, e.getMessage());
                        if (e.getMessage().equals("图片验证码错误")) {
                            getCodeImage();
                        }
                    }

                    @Override
                    public void onComplete() {
                        SPUtils.getInstance().remove(SPConfig.LOGIN_CODE_SESSION_ID);
//                        mViewBinding.actvLoginAccountBtn.setClickable(true);
                    }
                });
    }

    private boolean judgePhone(String phone) {
        if (StringUtils.isEmpty(phone)) {
            CommonUtils.showErrorToast(R.string.login_please_input_phone_number);
//            mViewBinding.actvLoginAccountBtn.setClickable(true);
            return true;
        }
        if (!RegexUtils.isMobileExact(phone)) {
            CommonUtils.showErrorToast(R.string.login_phone_error);
//            mViewBinding.actvLoginAccountBtn.setClickable(true);
            return true;
        }
        return false;
    }

    private boolean judgeCode(String code) {
        if ("".equals(code)) {
            CommonUtils.showErrorToast("请输入验证码");
            return true;
        }
        return false;
    }

    /**
     * 设置登录有效时间
     */
    private void setLoginTime() {
        PopupManager.INSTANCE.showCustomXPopup(this, new LoginTimePopup(this));
    }
}
