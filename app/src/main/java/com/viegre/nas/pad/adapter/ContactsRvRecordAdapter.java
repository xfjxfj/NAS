package com.viegre.nas.pad.adapter;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.entity.RecordListBean2;
import com.viegre.nas.pad.util.ExpandableViewHoldersUtil;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class ContactsRvRecordAdapter extends RecyclerView.Adapter<ContactsRvRecordAdapter.ViewHolder> {
    private ExpandableViewHoldersUtil.KeepOneHolder<ViewHolder> keepOne;
    private final Context mcontext;
    boolean isClick = false;
    private final List<String> data;
    private Gson gs = new Gson();

    public ContactsRvRecordAdapter(Context context, List<String> languages) {
        mcontext = context;
        data = languages;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mcontext).inflate(R.layout.contacts_record_rv_item, viewGroup, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {

        RecordListBean2 mdata = gs.fromJson(data.get(position), RecordListBean2.class);
        if (null == mdata) {
            return;
        }


        if (mdata.getDirection().equals("Receive")) {
            holder.delete_text.setText("呼入");

            holder.tvTitle.setText(mdata.getTargetId());
            holder.tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);

        } else {
            holder.delete_text.setText("呼出");

            holder.tvTitle.setText(mdata.getCallId());
            holder.tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
        }
        holder.item_user_time.setText(setTimeText(mdata,holder));
        holder.delete_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);

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
                data.remove(position);
                notifyItemRemoved(position);
                notifyDataSetChanged();
            }
        });
        holder.delete_text_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data.remove(position);
                notifyItemRemoved(position);
                notifyDataSetChanged();
            }
        });
    }

    private String setTimeText(RecordListBean2 mdata, ViewHolder holder) {
        String year = "";
        if (new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime()).split(" ")[0].equals(mdata.getCallTime().split(" ")[0])) {
            String[] s = mdata.getCallTime().split(" ");
            year = s[1];
        } else {
            String[] split = mdata.getCallTime().split("-");
            year = split[0] + "/" + split[1] + "/" + split[2];
        }

        String[] split1 = mdata.getTurnOnTime().split(":");
        String time = " 通话";
        if (!split1[0].equals("0")) {
            time = time + split1[0] + "小时";
        }
        if (!split1[1].equals("00")) {
            time = time + split1[1] + "分";
        }
        if (!split1[2].equals("0")) {
            time = time + split1[2] + "秒";
        } else {
            holder.delete_text.setText("未接");
        }
        return year + time;
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
        private final TextView item_user_time;
        private final TextView delete_text;

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
            item_user_time = itemView.findViewById(R.id.item_user_time);
            delete_text = itemView.findViewById(R.id.delete_text);

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
    }
}