/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.voip.conference;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.user.UserViewModel;
import cn.wildfire.chat.kit.widget.FixedTextInputEditText;
import cn.wildfirechat.avenginekit.AVEngineKit;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

public class CreateConferenceActivity extends WfcBaseActivity {
    @BindView(R2.id.conferenceTitleTextInputEditText)
    FixedTextInputEditText titleEditText;
    @BindView(R2.id.conferenceDescTextInputEditText)
    FixedTextInputEditText descEditText;
    @BindView((R2.id.videoSwitch))
    SwitchMaterial videoSwitch;
    @BindView((R2.id.audienceSwitch))
    SwitchMaterial audienceSwitch;
    @BindView((R2.id.advancedSwitch))
    SwitchMaterial advancedSwitch;

    @BindView(R2.id.createConferenceBtn)
    Button createButton;

    private String title;
    private String desc;

    @Override
    protected int contentLayout() {
        return R.layout.conference_create_activity;
    }

    @Override
    protected void afterViews() {
        super.afterViews();
        UserViewModel userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        UserInfo userInfo = userViewModel.getUserInfo(ChatManager.Instance().getUserId(), false);
        if(userInfo != null) {
            titleEditText.setText(userInfo.displayName + "的会议");
        } else {
            titleEditText.setText("会议");
        }
        descEditText.setText("欢迎参加");
        advancedSwitch.setChecked(false);
    }

    @OnCheckedChanged(R2.id.advancedSwitch)
    void advancedChecked(CompoundButton button, boolean checked) {
        if(checked) {
            audienceSwitch.setEnabled(false);
            audienceSwitch.setChecked(false);
        } else {
            audienceSwitch.setEnabled(true);
        }
    }
    @OnTextChanged(value = R2.id.conferenceTitleTextInputEditText, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void conferenceTitleChannelName(Editable editable) {
        this.title = editable.toString();
	    createButton.setEnabled(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(desc));
    }

    @OnTextChanged(value = R2.id.conferenceDescTextInputEditText, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void conferenceDescChannelName(Editable editable) {
        this.desc = editable.toString();
	    createButton.setEnabled(!TextUtils.isEmpty(desc) && !TextUtils.isEmpty(title));
    }

    @OnClick(R2.id.createConferenceBtn)
    public void onClickCreateBtn() {
        boolean audioOnly = !videoSwitch.isChecked();
        boolean audience = !audienceSwitch.isChecked();
        boolean advanced = advancedSwitch.isChecked();
        String title = titleEditText.getText().toString();
        String desc = descEditText.getText().toString();
        AVEngineKit.CallSession session = AVEngineKit.Instance().startConference(null, audioOnly, null, ChatManager.Instance().getUserId(), title, desc, audience, advanced, false, null);
        if (session != null) {
            Intent intent = new Intent(this, ConferenceActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "创建会议失败", Toast.LENGTH_SHORT).show();
        }
    }


}
