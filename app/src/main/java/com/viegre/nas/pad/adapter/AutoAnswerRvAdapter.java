package com.viegre.nas.pad.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SPUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.entity.MyfriendDataFriend;
import com.viegre.nas.pad.fragment.settings.AutoAnswerFragment;
import com.viegre.nas.pad.util.CommonUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * xfj 2021年6月25日
 */
public class AutoAnswerRvAdapter extends RecyclerView.Adapter<AutoAnswerRvAdapter.MyHolder> {
    private final List<MyfriendDataFriend> mData;
    private final Activity mThis;

    public AutoAnswerRvAdapter(Activity isThis, List<MyfriendDataFriend> mDevicesData) {
        this.mData = mDevicesData;
        this.mThis = isThis;
    }

    @NonNull
    @NotNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        //将我们自定义的item布局R.layout.item_one转换为View
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_devices_rv_add_item, parent, false);
        //将view传递给我们自定义的ViewHolder
        AutoAnswerRvAdapter.MyHolder holder = new AutoAnswerRvAdapter.MyHolder(view);
        //返回这个MyHolder实体
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MyHolder holder, int position) {
        holder.automaic_name.setText(mData.get(position).getFriendName());
        Glide.with(mThis)
                .load(CommonUtils.stringToBitmap(mData.get(position).getIco()))
                .placeholder(R.mipmap.main_unlogin)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(holder.automaic_ico);
        holder.automaic_sw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setImag(holder, mData.get(position));
            }
        });
    }

    private void setImag(MyHolder holder, MyfriendDataFriend myfriendDataFriend) {
        String autoStatus = SPUtils.getInstance().getString(myfriendDataFriend.getCallId(), "1");
        if (autoStatus.equals("1")) {
            holder.automaic_sw.setImageResource(R.mipmap.friend_sw_off);
            SPUtils.getInstance().put(myfriendDataFriend.getCallId(), "0");
        } else {
            holder.automaic_sw.setImageResource(R.mipmap.friend_sw_on);
            SPUtils.getInstance().put(myfriendDataFriend.getCallId(), "1");
        }
    }

    private String getuserjs(String callId, String s) {
        JSONObject jsonArray = new JSONObject();
        try {
            jsonArray.put("call", s);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray.toString();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        private final ImageView automaic_ico;
        private final TextView automaic_name;
        private final ImageView automaic_sw;

        public MyHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            automaic_ico = itemView.findViewById(R.id.automaic_ico);
            automaic_name = itemView.findViewById(R.id.automaic_name);
            automaic_sw = itemView.findViewById(R.id.automaic_sw);
        }
    }
}
