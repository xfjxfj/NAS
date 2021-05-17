package com.viegre.nas.pad.adapter;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.activity.MoreAppActivity;
import com.viegre.nas.pad.activity.SettingsActivity;

import java.util.List;

public class MoreAppActivityRvAdapter extends RecyclerView.Adapter<MoreAppActivityRvAdapter.MyHolder> {

    private final MoreAppActivity mThis;//数据源
    private final List<PackageInfo> mList;//数据源

    public MoreAppActivityRvAdapter(MoreAppActivity mContent, List<PackageInfo> list) {
        mThis = mContent;
        mList = list;
    }

    //创建ViewHolder并返回，后续item布局里控件都是从ViewHolder中取出
    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //将我们自定义的item布局R.layout.item_one转换为View
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.more_app_activity_rv_item2, parent, false);
        //将view传递给我们自定义的ViewHolder
        MyHolder holder = new MyHolder(view);
        //返回这个MyHolder实体
        return holder;
    }

    //通过方法提供的ViewHolder，将数据绑定到ViewHolderini
    public void onBindViewHolder(MyHolder holder, int position) {
        if (position == 0) {
            Glide.with(mThis)
                    .load(mThis.getDrawable(R.mipmap.set_app))
//                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(20)))
                    .placeholder(R.mipmap.ic_launcher)
                    .into(holder.more_app_ico_img);
        } else {
            PackageManager packageManager = mThis.getPackageManager();
            String ur = "https://t7.baidu.com/it/u=2963767354,870442698&fm=193&f=GIF";
            Glide.with(mThis)
                    .load(packageManager.getApplicationIcon(mList.get(position).applicationInfo))
//                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(20)))
                    .placeholder(R.mipmap.ic_launcher)
                    .into(holder.more_app_ico_img);
            holder.more_app_ico_img.setOnClickListener(v -> AppUtils.launchApp(mList.get(position).packageName));
        }
//        position = position * 6;
//        if (position < mList.size()) {
//            if (position == 0) {
//                holder.more_app_item_img1.setImageDrawable(mThis.getDrawable(R.mipmap.set_app));
//                holder.more_app_item_img1.setOnClickListener(view -> ActivityUtils.startActivity(SettingsActivity.class));
//            } else {
//                holder.more_app_item_img1.setImageDrawable(packageManager.getApplicationIcon(mList.get(position).applicationInfo));
//                int finalPosition1 = position;
//                holder.more_app_item_img1.setOnClickListener(v -> AppUtils.launchApp(mList.get(finalPosition1).packageName));
//            }
//        }
//     
    }

    //获取数据源总的条数
    @Override
    public int getItemCount() {
        return mList.size();
    }

    /**
     * 自定义的ViewHolder
     */
    class MyHolder extends RecyclerView.ViewHolder {

        ImageView more_app_ico_img;

        public MyHolder(View itemView) {
            super(itemView);
            more_app_ico_img = itemView.findViewById(R.id.more_app_ico_img);
        }
    }
}