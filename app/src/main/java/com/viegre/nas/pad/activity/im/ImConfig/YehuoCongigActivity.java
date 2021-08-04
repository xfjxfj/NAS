package com.viegre.nas.pad.activity.im.ImConfig;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.kongzue.dialog.interfaces.OnInputDialogButtonClickListener;
import com.kongzue.dialog.util.BaseDialog;
import com.kongzue.dialog.util.InputInfo;
import com.kongzue.dialog.v3.InputDialog;
import com.kongzue.dialog.v3.TipDialog;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.activity.im.ContactsActivity;
import com.viegre.nas.pad.adapter.ContactsRvDevicesAdapter;
import com.viegre.nas.pad.entity.DevicesFriendsListBean;

import java.util.List;

import cn.wildfire.chat.kit.contact.ContactViewModel;
import cn.wildfirechat.model.Friend;
import cn.wildfirechat.model.FriendRequest;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

import static cn.wildfire.chat.kit.R2.id.addFriend;

public class YehuoCongigActivity extends AppCompatActivity implements View.OnClickListener {

    private List<FriendRequest> friendRequest;
    private ContactViewModel contactViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yehuo_congig);
        initView();
    }

    private void initView() {
        RecyclerView yh_rv = findViewById(R.id.yh_rv);
        Button addFriend = findViewById(R.id.addFriend);
        Button addFriendOk = findViewById(R.id.addFriendOk);
        addFriend.setOnClickListener(this);
        addFriendOk.setOnClickListener(this);

        contactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);

        List<UserInfo> myFriendListInfo = ChatManager.Instance().getMyFriendListInfo(true);
        friendRequest = ChatManager.Instance().getFriendRequest(true);
        Log.d("", "initView: " + myFriendListInfo.toString());

        View inflate = getLayoutInflater().inflate(R.layout.yehuo_friend_popup, null);
//        //初始化数据
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
//        //设置布局管理器
        yh_rv.setLayoutManager(linearLayoutManager2);
//        //创建适配器，将数据传递给适配器
//        //设置适配器adapter
        YhConfigCvAdapter adapter = new YhConfigCvAdapter(YehuoCongigActivity.this,myFriendListInfo,inflate,contactViewModel);
        yh_rv.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addFriend:
                showDialogView();
                break;
            case R.id.addFriendOk:
//                accept();
                if (friendRequest.size() != 0) {
                    for (int i = 0; i < friendRequest.size(); i++) {
                        accept(friendRequest.get(i).target);
                    }
                } else {
                    Toast.makeText(YehuoCongigActivity.this, "无新的好友请求", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void showDialogView() {
        InputDialog.build(this)
                //.setButtonTextInfo(new TextInfo().setFontColor(Color.GREEN))
                .setTitle("提示").setMessage("请输入账号")
                .setInputText("账号")
                .setOkButton("确定", new OnInputDialogButtonClickListener() {
                    @Override
                    public boolean onClick(BaseDialog baseDialog, View v, String inputStr) {
                        addFriend(inputStr, "");
                        return false;
                    }
                })
                .setCancelButton("取消")
                .setHintText("请输入密码")
                .setInputInfo(new InputInfo()
                                .setMAX_LENGTH(10)
                                .setInputType(InputType.TYPE_TEXT_VARIATION_URI)
                        //.setTextInfo(new TextInfo()
                        //        .setFontColor(Color.RED)
                        //)
                )
                .setCancelable(true)
                .show();
    }

    /**
     * 野火添加好友請求
     */
    public void addFriend(String userId, String userTips) {
        ContactViewModel contactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
        contactViewModel.invite(userId, userTips)
                .observe(this, new androidx.lifecycle.Observer<Boolean>() {
                    @Override
                    public void onChanged(@Nullable Boolean aBoolean) {
                        if (aBoolean) {
                            Toast.makeText(YehuoCongigActivity.this, "好友邀请已发送", Toast.LENGTH_SHORT).show();
                            Log.d("", "onChanged: " + "好友邀请已发送");
                        } else {
                            Toast.makeText(YehuoCongigActivity.this, "添加好友失败", Toast.LENGTH_SHORT).show();
                            Log.d("", "onChanged: " + "添加好友失败");
                        }
                    }
                });
    }

    /**
     * 好友邀请请求
     *
     * @param friendId
     */
    void accept(String friendId) {
        contactViewModel.acceptFriendRequest(friendId).observe((LifecycleOwner) YehuoCongigActivity.this, aBoolean -> {
            if (aBoolean) {
                Toast.makeText(YehuoCongigActivity.this, "已接受" + friendId + "的邀请", Toast.LENGTH_SHORT).show();
                Log.d("", "onChanged: " + "已接受" + friendId + "的邀请");
            } else {
                Toast.makeText(YehuoCongigActivity.this, "添加" + friendId + "失败", Toast.LENGTH_SHORT).show();
                Log.d("", "onChanged: " + "添加" + friendId + "失败");
            }
        });
    }

}