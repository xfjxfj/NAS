package com.viegre.nas.pad.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SPUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.entity.ContactsBean;
import com.viegre.nas.pad.util.CommonUtils;

import java.util.List;

import cn.wildfire.chat.kit.WfcUIKit;

/**
 * 联系人好友adapter
 */
public class ContactsRvFriendsAdapter extends RecyclerView.Adapter<ContactsRvFriendsAdapter.MyHolder> {

    private final List<ContactsBean> languages;
    private final Context context1;

    public ContactsRvFriendsAdapter(Context context, List<ContactsBean> languages) {
        this.languages = languages;
        context1 = context;
    }

    @NonNull
    @Override
    public ContactsRvFriendsAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //将我们自定义的item布局R.layout.item_one转换为View
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_friends_rv_item, parent, false);
        //将view传递给我们自定义的ViewHolder
        ContactsRvFriendsAdapter.MyHolder holder = new ContactsRvFriendsAdapter.MyHolder(view);
        //返回这个MyHolder实体
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsRvFriendsAdapter.MyHolder holder, int position) {
        holder.textfr.setText(languages.get(position).getUsername().equals("null")?languages.get(position).getUserphone():languages.get(position).getUsername());
            Glide.with(context1)
                    .load(CommonUtils.stringToBitmap(languages.get(position).getUserimg()))
                    .placeholder(R.mipmap.main_unlogin)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(holder.img_im1);

        holder.img_im2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RunCall(languages.get(position).getUserid(), true);
            }
        });
        holder.img_im3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RunCall(languages.get(position).getUserid(), false);
            }
        });
    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    private void RunCall(String userid, Boolean isAudioOnly) {
        WfcUIKit.singleCall(context1, userid, isAudioOnly);
    }

    /**
     * 自定义的ViewHolder
     */
    class MyHolder extends RecyclerView.ViewHolder {

        private final TextView textfr;
        private final ConstraintLayout contactsFr;
        private final ImageView img_im1;
        private final ImageView img_im2;
        private final ImageView img_im3;

        public MyHolder(View itemView) {
            super(itemView);
            textfr = itemView.findViewById(R.id.textfr);
            contactsFr = itemView.findViewById(R.id.contactsFr);
            img_im1 = itemView.findViewById(R.id.img_im1);
            img_im2 = itemView.findViewById(R.id.img_im2);
            img_im3 = itemView.findViewById(R.id.img_im3);
        }
    }
}























