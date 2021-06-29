package com.viegre.nas.pad.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SPUtils;
import com.kongzue.dialog.util.DialogSettings;
import com.kongzue.dialog.v3.CustomDialog;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.activity.im.popupwindow.HorizontalPosition;
import com.viegre.nas.pad.activity.im.popupwindow.SmartPopupWindow;
import com.viegre.nas.pad.activity.im.popupwindow.TestPopupWindow;
import com.viegre.nas.pad.activity.im.popupwindow.VerticalPosition;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.entity.DevicesFriendsListBean;
import com.viegre.nas.pad.util.CommonUtils;

import java.util.List;
import java.util.Random;

import cn.wildfire.chat.kit.WfcUIKit;

public class ContactsRvDevicesAdapter extends RecyclerView.Adapter<ContactsRvDevicesAdapter.MyHolder> {

    private final List<DevicesFriendsListBean> languages;
    private final Context mContext;
    private final View mPopupContentView;
    private int mGravity = Gravity.START;
    private int mOffsetX = 0;
    private int mOffsetY = 0;
    private boolean useSmartPopup = true;
    private ContactsRvDevicesAdapter.addDevicesFriend addDevicesFriend;
    private ContactsRvDevicesAdapter.editDevicesName editDevicesName;
    private ContactsRvDevicesAdapter.deleteDevicesFriend deleteDevicesFriend;

    public ContactsRvDevicesAdapter(Context context, List<DevicesFriendsListBean> languages, View inflate) {
        this.languages = languages;
        this.mContext = context;
        this.mPopupContentView = inflate;
    }

    @NonNull
    @Override
    public ContactsRvDevicesAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //将我们自定义的item布局R.layout.item_one转换为View
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_devices_rv_item, parent, false);
        //将view传递给我们自定义的ViewHolder
        ContactsRvDevicesAdapter.MyHolder holder = new ContactsRvDevicesAdapter.MyHolder(view);
        //返回这个MyHolder实体
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsRvDevicesAdapter.MyHolder holder, int position) {
        if (languages.size() - 1 == position) {
            holder.de_laytou.setVisibility(View.GONE);
            holder.de_laytou1.setVisibility(View.VISIBLE);
        } else {
            holder.textdv.setText(languages.get(position).getName() + languages.get(position).getCallId());
            holder.de_laytou.setVisibility(View.VISIBLE);
            holder.de_laytou1.setVisibility(View.GONE);
        }

        holder.de_laytou.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                CommonUtils.setBackgroundAlpha((Activity) mContext, 0.2f);
                mypopupmenu(v, languages.get(position).getCallId(), languages.get(position).getSn(), languages.get(position).getName());
                return false;
            }
        });
        holder.de_laytou.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RunCall(languages.get(position).getCallId(), false);
            }
        });

        holder.de_laytou1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                DialogSettings.use_blur = true;
                DialogSettings.isUseBlur = true;
                CustomDialog.build((AppCompatActivity) mContext, R.layout.contacts_add_devices_dialog, new CustomDialog.OnBindView() {
                    @Override
                    public void onBind(final CustomDialog dialog, View v) {
                        /**
                         * 测试数据
                         */
//                        5830e3fbe8c57dd5
                        EditText viewById = v.findViewById(R.id.add_device_dialog_edittext);
                        EditText viewById1 = v.findViewById(R.id.add_device_username_dialog_edittext);
                        Button cancle_bt = v.findViewById(R.id.cancle_bt);
                        Button button_ok = v.findViewById(R.id.button_ok);

//                        viewById.setText(SPUtils.getInstance().getString(SPConfig.ANDROID_ID));
                        viewById.setText("6fa8295f4764b429");
                        viewById1.setText(new Random().nextInt(1000) + "---魏格设备");

                        button_ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (addDevicesFriend != null) {
                                    addDevicesFriend.onAddDevicesFriendClick(button_ok, viewById.getText().toString(), viewById1.getText().toString());
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
        });
    }

    //    RunCall(languages.get(position).getUserid(), false);
    private void RunCall(String userid, Boolean isAudioOnly) {
        WfcUIKit.singleCall(mContext, userid, isAudioOnly);
    }

    //回调接口  添加设备好友
    public interface addDevicesFriend {
        void onAddDevicesFriendClick(Button bt, String friendId, String friendName);
    }

    //回调接口 修改名称
    public interface editDevicesName {
        void onEditDevicesNameClick(String callId, String devicesSn);
    }

    //    回调接口删除设备
    public interface deleteDevicesFriend {
        void onDeleteDevicesFriend(String friendSn, String friendName);
    }

    //定义回调方法 修改名称
    public void setEditDevicesName(editDevicesName editDevicesName) {
        this.editDevicesName = editDevicesName;
    }

    //定义回调方法 添加设备好友
    public void setaddDevicesFriend(addDevicesFriend addDevicesFriend) {
        this.addDevicesFriend = addDevicesFriend;
    }

    //定义回调方法，删除设备好友
    public void setDeleteFriend(deleteDevicesFriend deleteFriend) {
        this.deleteDevicesFriend = deleteFriend;
    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    private void mypopupmenu(View v, String callId, String friendSn, String friendName) {
        TestPopupWindow mWindow = new TestPopupWindow(mContext);
        mGravity = Gravity.START;
        mOffsetX = Math.abs(mWindow.getContentView().getMeasuredWidth() - v.getWidth()) / 2;
        mOffsetY = -(mWindow.getContentView().getMeasuredHeight() + v.getHeight());
        //使用SmartPopup
        if (useSmartPopup) {
            SmartPopupWindow popupWindow = SmartPopupWindow.Builder
                    .build((Activity) mContext, mPopupContentView)
                    .createPopupWindow();
            popupWindow.showAtAnchorView(v, VerticalPosition.ABOVE, HorizontalPosition.CENTER);
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    CommonUtils.setBackgroundAlpha((Activity) mContext, 1f);
                }
            });
            mPopupContentView.findViewById(R.id.call_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    语音呼叫
                    RunCall(callId, true);
                }
            });
            mPopupContentView.findViewById(R.id.video_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    视频呼叫
                    RunCall(callId, false);
                }
            });
            mPopupContentView.findViewById(R.id.edit_devices_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    修改设备名称
                    popupWindow.dismiss();
                    editDevicesName.onEditDevicesNameClick(callId, friendSn);
                }
            });
            mPopupContentView.findViewById(R.id.delete_devices_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    删除设备呼叫
                    popupWindow.dismiss();
                    deleteDevicesFriend.onDeleteDevicesFriend(friendSn, friendName);
                }
            });
        }
    }

    /**
     * 自定义的ViewHolder
     */
    class MyHolder extends RecyclerView.ViewHolder {
        private final TextView textdv;
        private final ConstraintLayout de_laytou;
        private final ConstraintLayout de_laytou1;

        public MyHolder(View itemView) {
            super(itemView);
            textdv = itemView.findViewById(R.id.text_dv);
            de_laytou = itemView.findViewById(R.id.de_laytou);
            de_laytou1 = itemView.findViewById(R.id.de_laytou1);
        }
    }
}
