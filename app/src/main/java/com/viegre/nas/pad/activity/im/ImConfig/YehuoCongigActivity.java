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
//        //???????????????
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
//        //?????????????????????
        yh_rv.setLayoutManager(linearLayoutManager2);
//        //?????????????????????????????????????????????
//        //???????????????adapter
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
                    Toast.makeText(YehuoCongigActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void showDialogView() {
        InputDialog.build(this)
                //.setButtonTextInfo(new TextInfo().setFontColor(Color.GREEN))
                .setTitle("??????").setMessage("???????????????")
                .setInputText("??????")
                .setOkButton("??????", new OnInputDialogButtonClickListener() {
                    @Override
                    public boolean onClick(BaseDialog baseDialog, View v, String inputStr) {
                        addFriend(inputStr, "");
                        return false;
                    }
                })
                .setCancelButton("??????")
                .setHintText("???????????????")
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
     * ????????????????????????
     */
    public void addFriend(String userId, String userTips) {
        ContactViewModel contactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
        contactViewModel.invite(userId, userTips)
                .observe(this, new androidx.lifecycle.Observer<Boolean>() {
                    @Override
                    public void onChanged(@Nullable Boolean aBoolean) {
                        if (aBoolean) {
                            Toast.makeText(YehuoCongigActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
                            Log.d("", "onChanged: " + "?????????????????????");
                        } else {
                            Toast.makeText(YehuoCongigActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
                            Log.d("", "onChanged: " + "??????????????????");
                        }
                    }
                });
    }

    /**
     * ??????????????????
     *
     * @param friendId
     */
    void accept(String friendId) {
        contactViewModel.acceptFriendRequest(friendId).observe((LifecycleOwner) YehuoCongigActivity.this, aBoolean -> {
            if (aBoolean) {
                Toast.makeText(YehuoCongigActivity.this, "?????????" + friendId + "?????????", Toast.LENGTH_SHORT).show();
                Log.d("", "onChanged: " + "?????????" + friendId + "?????????");
            } else {
                Toast.makeText(YehuoCongigActivity.this, "??????" + friendId + "??????", Toast.LENGTH_SHORT).show();
                Log.d("", "onChanged: " + "??????" + friendId + "??????");
            }
        });
    }

}