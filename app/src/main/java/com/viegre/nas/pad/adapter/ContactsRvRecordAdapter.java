package com.viegre.nas.pad.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.util.ExpandableViewHoldersUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ContactsRvRecordAdapter extends RecyclerView.Adapter<ContactsRvRecordAdapter.ViewHolder> {
    private ExpandableViewHoldersUtil.KeepOneHolder<ViewHolder> keepOne;
    private final Context mcontext;
    boolean isClick = false;
    private final List<String> mData;

    public ContactsRvRecordAdapter(Context context, List<String> languages) {
        mcontext = context;
        mData = languages;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mcontext).inflate(R.layout.contacts_record_rv_item, viewGroup, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        holder.tvTitle.setText("张三" + mData.get(position));
        Glide.with(mcontext)
                .load("https://image.baidu.com/search/albumsdetail?tn=albumsdetail&word=%E6%B8%90%E5%8F%98%E9%A3%8E%E6%A0%BC%E6%8F%92%E7%94%BB&fr=albumslist&album_tab=%E8%AE%BE%E8%AE%A1%E7%B4%A0%E6%9D%90&album_id=409&rn=30")
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.userImage);

        keepOne.bind(holder, position);
        holder.tvTitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                keepOne.toggle(holder);
                isClick = true;
                return true;
            }
        });
        holder.tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isClick) {
                    keepOne.toggle(holder);
                    isClick = false;
                }
            }
        });
        holder.delete_text_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mData.remove(position);
                notifyItemRemoved(position);
//                notifyItemRangeChanged(position,getItemCount()-position);
                notifyDataSetChanged();
            }
        });
        holder.delete_text_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mData.remove(position);
                notifyItemRemoved(position);
//                notifyItemRangeChanged(position,getItemCount()-position);
                notifyDataSetChanged();
            }
        });
//        holder.tvTitle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                    if(ExpandableViewHoldersUtil.isExpaned(position)){
////                        holder.contentTv.setMaxLines(3);
////                    }else {
////                        holder.contentTv.setMaxLines(100);
////                    }
//                keepOne.toggle((ViewHolder) holder);
//            }
//        });

//        holder.lvArrorwBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                keepOne.toggle(holder);
//            }
//        });
    }

    class ViewHolder extends RecyclerView.ViewHolder implements ExpandableViewHoldersUtil.Expandable {
        TextView tvTitle;
        //        ImageView arrowImage;
//        LinearLayout lvArrorwBtn;
        LinearLayout lvLinearlayout;
        ImageView contentTv;
        private final TextView delete_text_1;
        private final TextView delete_text_2;
        private final ImageView userImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.item_user_concern_title);
            lvLinearlayout = itemView.findViewById(R.id.item_user_concern_link_layout);
//            lvArrorwBtn = itemView.findViewById(R.id.item_user_concern_arrow);
//            arrowImage = itemView.findViewById(R.id.item_user_concern_arrow_image);
            delete_text_1 = itemView.findViewById(R.id.delete_text_1);
            delete_text_2 = itemView.findViewById(R.id.delete_text_2);
            contentTv = itemView.findViewById(R.id.delete_image_3);
            userImage = itemView.findViewById(R.id.userImage);

            keepOne = ExpandableViewHoldersUtil.getInstance().getKeepOneHolder();

            lvLinearlayout.setVisibility(View.GONE);
            lvLinearlayout.setAlpha(0);
        }

        @Override
        public View getExpandView() {
            return lvLinearlayout;
        }

        @Override
        public void doCustomAnim(boolean isOpen) {
            if (isOpen) {
//                ExpandableViewHoldersUtil.getInstance().rotateExpandIcon(arrowImage, 180, 0);
            } else {
//                ExpandableViewHoldersUtil.getInstance().rotateExpandIcon(arrowImage, 0, 180);
            }
        }

//    public static void showActivity(Context context) {
//        Intent intent = new Intent(context, ExPandableViewActivity.class);
//        context.startActivity(intent);
//    }
    }
}