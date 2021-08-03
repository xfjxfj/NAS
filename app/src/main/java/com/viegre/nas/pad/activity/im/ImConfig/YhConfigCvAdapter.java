package com.viegre.nas.pad.activity.im.ImConfig;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.viegre.nas.pad.R;
import com.viegre.nas.pad.activity.im.popupwindow.HorizontalPosition;
import com.viegre.nas.pad.activity.im.popupwindow.SmartPopupWindow;
import com.viegre.nas.pad.activity.im.popupwindow.TestPopupWindow;
import com.viegre.nas.pad.activity.im.popupwindow.VerticalPosition;
import com.viegre.nas.pad.adapter.ContactsRvDevicesAdapter;
import com.viegre.nas.pad.util.CommonUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import cn.wildfire.chat.kit.common.OperateResult;
import cn.wildfire.chat.kit.contact.ContactViewModel;
import cn.wildfirechat.model.UserInfo;

public class YhConfigCvAdapter extends RecyclerView.Adapter<YhConfigCvAdapter.MyHolder> {
    private List<UserInfo> myFriendListInfoData = new ArrayList();
    private final View mPopupContentView;
    private final YehuoCongigActivity mThis;
    private ContactViewModel mContactViewModel;
    private int mGravity = Gravity.START;
    private int mOffsetX = 0;
    private int mOffsetY = 0;

    public YhConfigCvAdapter(YehuoCongigActivity yehuoCongigActivity, List<UserInfo> myFriendListInfo, View inflate, ContactViewModel contactViewModel) {
        mThis = yehuoCongigActivity;
        myFriendListInfoData = myFriendListInfo;
        this.mPopupContentView = inflate;
        mContactViewModel = contactViewModel;
    }

    @NonNull
    @NotNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        //将我们自定义的item布局R.layout.item_one转换为View
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.yehuo_config_rv_item, parent, false);
        //将view传递给我们自定义的ViewHolder
        YhConfigCvAdapter.MyHolder holder = new YhConfigCvAdapter.MyHolder(view);
        //返回这个MyHolder实体
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MyHolder holder, int position) {
        if (myFriendListInfoData.get(position).friendAlias.equals("")) {
            holder.friendName.setText(myFriendListInfoData.get(position).displayName + myFriendListInfoData.get(position).uid);
        } else {
            holder.friendName.setText(myFriendListInfoData.get(position).friendAlias + myFriendListInfoData.get(position).uid);
        }
        holder.friendview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                CommonUtils.setBackgroundAlpha(mThis, 1f);
                mypopupmenu(v, myFriendListInfoData, position);
                return false;
            }
        });
    }


    @Override
    public int getItemCount() {
        return myFriendListInfoData == null ? 0 : myFriendListInfoData.size();
    }

    private void mypopupmenu(View v, List<UserInfo> myFriendListInfoData, int position) {
        TestPopupWindow mWindow = new TestPopupWindow(mThis);
        mGravity = Gravity.START;
        mOffsetX = Math.abs(mWindow.getContentView().getMeasuredWidth() - v.getWidth()) / 2;
        mOffsetY = -(mWindow.getContentView().getMeasuredHeight() + v.getHeight());
        SmartPopupWindow popupWindow = SmartPopupWindow.Builder
                .build((Activity) mThis, mPopupContentView)
                .createPopupWindow();
        popupWindow.showAtAnchorView(v, VerticalPosition.ABOVE, HorizontalPosition.CENTER);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                CommonUtils.setBackgroundAlpha((Activity) mThis, 1f);
            }
        });
        mPopupContentView.findViewById(R.id.edit_devices_view).setOnClickListener(new View.OnClickListener() {//修改备注
            @Override
            public void onClick(View v) {
                //修改名称
                changeAlias(myFriendListInfoData.get(position).uid, "我叫张三");
            }
        });
        mPopupContentView.findViewById(R.id.delete_devices_view).setOnClickListener(new View.OnClickListener() {//删除好友
            @Override
            public void onClick(View v) {
                //修改名称
                delefriened(myFriendListInfoData.get(position).uid);
            }
        });
    }

    /**
     * 自定义的ViewHolder
     */
    class MyHolder extends RecyclerView.ViewHolder {
        private final ImageView friendImage;
        private final TextView friendName;
        private final LinearLayout friendview;
        public MyHolder(View itemView) {
            super(itemView);
            friendImage = itemView.findViewById(R.id.yh_friendicon);
            friendName = itemView.findViewById(R.id.yh_friendname);
            friendview = itemView.findViewById(R.id.friendview);
        }
    }

    /**
     * 修改对好友的备注
     */
    private void changeAlias(String userId, String displayName) {
        mContactViewModel.setFriendAlias(userId, displayName).observe(mThis, new androidx.lifecycle.Observer<OperateResult<Integer>>() {
            @Override
            public void onChanged(OperateResult<Integer> integerOperateResult) {
                if (integerOperateResult.isSuccess()) {
//                    Toast.makeText(SetAliasActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                    Log.d(CommonUtils.getFileName() + CommonUtils.getLineNumber(), "修改别名成功");
                } else {
//                    Toast.makeText(SetAliasActivity.this, "修改别名错误：" + integerOperateResult.getErrorCode(), Toast.LENGTH_SHORT).show();
                    Log.d(CommonUtils.getFileName() + CommonUtils.getLineNumber(), "修改别名失败" + integerOperateResult.getErrorCode());
                }
            }
        });
    }

    /**
     * delefriend
     */
    private void delefriened(String userid) {
        mContactViewModel.deleteFriend(userid).observe(
                mThis, booleanOperateResult -> {
                    if (booleanOperateResult.isSuccess()) {
                        Toast.makeText(mThis, "delete friend OK " + booleanOperateResult.getErrorCode(), Toast.LENGTH_SHORT).show();
//                        Intent intent = new Intent(getPackageName() + ".main");
//                        startActivity(intent);
                    } else {
                        Toast.makeText(mThis, "delete friend error " + booleanOperateResult.getErrorCode(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}
