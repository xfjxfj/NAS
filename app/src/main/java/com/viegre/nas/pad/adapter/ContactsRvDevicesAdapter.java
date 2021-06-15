package com.viegre.nas.pad.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.kongzue.dialog.util.DialogSettings;
import com.kongzue.dialog.v3.CustomDialog;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.activity.im.popupwindow.HorizontalPosition;
import com.viegre.nas.pad.activity.im.popupwindow.SmartPopupWindow;
import com.viegre.nas.pad.activity.im.popupwindow.TestPopupWindow;
import com.viegre.nas.pad.activity.im.popupwindow.VerticalPosition;
import com.viegre.nas.pad.util.CommonUtils;

import java.util.List;

public class ContactsRvDevicesAdapter extends RecyclerView.Adapter<ContactsRvDevicesAdapter.MyHolder> {

    private final List<String> languages;
    private final Context mContext;
    private final View mPopupContentView;
    private int mGravity = Gravity.START;
    private int mOffsetX = 0;
    private int mOffsetY = 0;
    private boolean useSmartPopup = true;

    public ContactsRvDevicesAdapter(Context context, List<String> languages, View inflate) {
        this.languages = languages;
        this.mContext = context;
        this.mPopupContentView = inflate;
    }

    @NonNull
    @Override
    public ContactsRvDevicesAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //将我们自定义的item布局R.layout.item_one转换为View
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_devices_rv_add_item, parent, false);
        //将view传递给我们自定义的ViewHolder
        ContactsRvDevicesAdapter.MyHolder holder = new ContactsRvDevicesAdapter.MyHolder(view);
        //返回这个MyHolder实体
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsRvDevicesAdapter.MyHolder holder, int position) {
        holder.textdv.setText(languages.get(position));
        if (languages.size() - 1 == position) {
            holder.de_laytou.setVisibility(View.GONE);
            holder.de_laytou1.setVisibility(View.VISIBLE);
        }

        holder.de_laytou.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                CommonUtils.setBackgroundAlpha((Activity) mContext, 0.2f);
                mypopupmenu(v);
                return false;
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
                        Button btnOk = v.findViewById(R.id.cancle_bt);
                        btnOk.setOnClickListener(new View.OnClickListener() {
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


    @Override
    public int getItemCount() {
        return languages.size();
    }

    private void mypopupmenu(View v) {

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
        }
//        //定义popupmenu对象
//        PopupMenu popupmenu = new PopupMenu(mContext, v);
//        //设置popupmenu对象的布局
//        popupmenu.getMenuInflater().inflate(R.menu.menu, popupmenu.getMenu());
//        //设置popupmenu的点击事件
//        popupmenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                Toast.makeText(mContext, "点击了----" + item.getTitle(), Toast.LENGTH_SHORT).show();
//                return false;
//            }
//        });
//
//        popupmenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
//            @Override
//            public void onDismiss(PopupMenu menu) {
//                CommonUtils.setBackgroundAlpha((Activity) mContext, 1f);
//            }
//        });
//        //显示菜单
//        popupmenu.show();
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
