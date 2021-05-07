package com.viegre.nas.pad.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.entity.ContactsBean;
import com.viegre.nas.pad.entity.ImageEntity;

import java.util.List;

import cn.wildfire.chat.kit.WfcUIKit;

public class PhotoAlbumAdapter extends RecyclerView.Adapter<PhotoAlbumAdapter.MyHolder> {

    private List<ImageEntity> mImageList;
    private Context context1;

    public PhotoAlbumAdapter(Context context, List<ImageEntity> imageList) {
        this.mImageList = imageList;
        context1 = context;
    }

    public PhotoAlbumAdapter() {
    }

    @NonNull
    @Override
    public PhotoAlbumAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //将我们自定义的item布局R.layout.item_one转换为View
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_friends_rv_item, parent, false);
        //将view传递给我们自定义的ViewHolder
        PhotoAlbumAdapter.MyHolder holder = new PhotoAlbumAdapter.MyHolder(view);
        //返回这个MyHolder实体
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoAlbumAdapter.MyHolder holder, int position) {
        Glide.with(context1).load(mImageList.get(position).getPath()).into(holder.mImage);

    }

    @Override
    public int getItemCount() {
        return mImageList.size();
    }

    /**
     * 自定义的ViewHolder
     */
    class MyHolder extends RecyclerView.ViewHolder {

        private final ImageView mImage;
        private final ConstraintLayout contactsFr;
        private final ImageView img_im1;
        private final ImageView img_im2;
        private final ImageView img_im3;

        public MyHolder(View itemView) {
            super(itemView);
            mImage = itemView.findViewById(R.id.acivItemImageRoot);
            contactsFr = itemView.findViewById(R.id.contactsFr);
            img_im1 = itemView.findViewById(R.id.img_im1);
            img_im2 = itemView.findViewById(R.id.img_im2);
            img_im3 = itemView.findViewById(R.id.img_im3);

        }
    }
}


















