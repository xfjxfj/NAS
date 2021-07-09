package com.viegre.nas.pad.adapter;

import android.content.Context;
import android.os.Build;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.activity.im.ContactsActivity;
import com.viegre.nas.pad.config.PathConfig;
import com.viegre.nas.pad.entity.RecordListBean2;
import com.viegre.nas.pad.util.ExpandableViewHoldersUtil;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通话记录adapter
 */
public class ContactsRvRecordAdapter extends RecyclerView.Adapter<ContactsRvRecordAdapter.ViewHolder> {
    private ExpandableViewHoldersUtil.KeepOneHolder<ViewHolder> keepOne;
    private final Context mcontext;
    boolean isClick = false;
    private List<String> data;
    private Gson gs = new Gson();

    public ContactsRvRecordAdapter(Context context) {
        mcontext = context;
        data = getRecordData();
    }

    @Override
    public int getItemCount() {
        List<String> recordData = getRecordData();
        data = recordData;
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
        holder.item_user_time.setText(setTimeText(mdata, holder));
        holder.delete_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
        String url = "https://t7.baidu.com/it/u=4036010509,3445021118&fm=193&f=GIF";
        Glide.with(mcontext)
                .load(url)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    public List<String> getRecordData() {
//        mRecordData.clear();
        ArrayList<String> data = new ArrayList<>();
        File file = new File(mcontext.getFilesDir().toString() + PathConfig.CONTACTS_RECOMDING);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String textOnLineString;
            while ((textOnLineString = reader.readLine()) != null) {
                data.add(textOnLineString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        data.stream().distinct().collect(Collectors.toList());//去除重复数据
        Collections.reverse(data);//倒序list
        return data;
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
        if (split1.length > 1) {
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
        } else {
            holder.delete_text.setText("未接");
        }
        return year + time;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements ExpandableViewHoldersUtil.Expandable {
        TextView tvTitle;
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