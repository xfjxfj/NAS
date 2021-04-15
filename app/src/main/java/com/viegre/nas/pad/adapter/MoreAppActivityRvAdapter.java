package com.viegre.nas.pad.adapter;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.viegre.nas.pad.R;
import com.viegre.nas.pad.activity.MoreAppActivity;
import com.viegre.nas.pad.util.CommonUtils;

import java.util.List;

public class MoreAppActivityRvAdapter extends RecyclerView.Adapter<MoreAppActivityRvAdapter.MyHolder> {

    private MoreAppActivity mThis;//数据源
    private List<PackageInfo> mList;//数据源
    private List<PackageInfo> mList1;//数据源


    public MoreAppActivityRvAdapter(MoreAppActivity mContent, List<PackageInfo> list) {
        mThis = mContent;
        mList = list;
        mList1 = list;
    }

    //创建ViewHolder并返回，后续item布局里控件都是从ViewHolder中取出
    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //将我们自定义的item布局R.layout.item_one转换为View
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.more_app_activity_rv_item, parent, false);
        //将view传递给我们自定义的ViewHolder
        MyHolder holder = new MyHolder(view);
        //返回这个MyHolder实体
        return holder;
    }


    //通过方法提供的ViewHolder，将数据绑定到ViewHolderini
    public void onBindViewHolder(MyHolder holder, int position) {
        PackageManager packageManager = mThis.getPackageManager();
        position = position * 6;
        if (position <= mList.size()) {
            holder.more_app_item_img1.setImageDrawable(packageManager.getApplicationIcon(mList.get(position).applicationInfo));
            int finalPosition1 = position;
            holder.more_app_item_img1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CommonUtils.getOpenApp(mThis, mList.get(finalPosition1));
                }
            });
        }
        position = position + 1;
        if (position <= mList.size()) {
            holder.more_app_item_img2.setImageDrawable(packageManager.getApplicationIcon(mList.get(position).applicationInfo));
            int finalPosition = position;
            holder.more_app_item_img2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CommonUtils.getOpenApp(mThis, mList.get(finalPosition));
                }
            });
        }
        position = position + 1;
        if (position <= mList.size()) {
            holder.more_app_item_img3.setImageDrawable(packageManager.getApplicationIcon(mList.get(position).applicationInfo));
            int finalPosition2 = position;
            holder.more_app_item_img3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CommonUtils.getOpenApp(mThis, mList.get(finalPosition2));
                }
            });
        }
        position = position + 1;
        if (position <= mList.size()) {
            holder.more_app_item_img4.setImageDrawable(packageManager.getApplicationIcon(mList.get(position).applicationInfo));
            int finalPosition3 = position;
            holder.more_app_item_img4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CommonUtils.getOpenApp(mThis, mList.get(finalPosition3));
                }
            });
        }
        position = position + 1;
        if (position <= mList.size()) {
            holder.more_app_item_img5.setImageDrawable(packageManager.getApplicationIcon(mList.get(position).applicationInfo));
            int finalPosition4 = position;
            holder.more_app_item_img5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CommonUtils.getOpenApp(mThis, mList.get(finalPosition4));
                }
            });
        }
        position = position + 1;
        if (position <= mList.size() - 1) {
            try {
                holder.more_app_item_img6.setImageDrawable(packageManager.getApplicationIcon(mList.get(position).applicationInfo));
                int finalPosition5 = position;
                holder.more_app_item_img6.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CommonUtils.getOpenApp(mThis, mList.get(finalPosition5));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    //获取数据源总的条数
    @Override
    public int getItemCount() {
        return (int) Math.ceil((double) mList1.size() / 6);
    }

    /**
     * 自定义的ViewHolder
     */
    class MyHolder extends RecyclerView.ViewHolder {

        TextView textView;
        public final ImageView more_app_item_img1;
        public final ImageView more_app_item_img2;
        public final ImageView more_app_item_img3;
        public final ImageView more_app_item_img4;
        public final ImageView more_app_item_img5;
        public final ImageView more_app_item_img6;

        public MyHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv_content);
            more_app_item_img1 = itemView.findViewById(R.id.more_app_item_img1);
            more_app_item_img2 = itemView.findViewById(R.id.more_app_item_img2);
            more_app_item_img3 = itemView.findViewById(R.id.more_app_item_img3);
            more_app_item_img4 = itemView.findViewById(R.id.more_app_item_img4);
            more_app_item_img5 = itemView.findViewById(R.id.more_app_item_img5);
            more_app_item_img6 = itemView.findViewById(R.id.more_app_item_img6);
        }
    }
}