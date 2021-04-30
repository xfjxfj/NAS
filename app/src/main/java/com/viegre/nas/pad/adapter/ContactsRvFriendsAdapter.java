package com.viegre.nas.pad.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.PointerIcon;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.topqizhi.ai.manager.AIUIManager;
import com.topqizhi.ai.manager.MscManager;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.activity.ContactsActivity;
import com.viegre.nas.pad.entity.ConstactBean;

import java.util.List;

import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.conversation.ext.VoipExt;

public class ContactsRvFriendsAdapter extends RecyclerView.Adapter<ContactsRvFriendsAdapter.MyHolder> {

    private final List<ConstactBean> languages;
    private final Context context1;

    public ContactsRvFriendsAdapter(Context context, List<ConstactBean> languages) {
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
//        holder.textfr.setText(languages.get(position).getPhone());
        if (ContactsActivity.phone.equals("15357906428")) {
            holder.textfr.setText("13168306428");
        } else {
            holder.textfr.setText("15357906428");
        }
        holder.contactsFr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context1, position + "", Toast.LENGTH_LONG).show();
                RunCall(languages.get(position).getUserid(), holder.textfr);
            }
        });
    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    private void RunCall(String userid, TextView textfr) {
        if (ContactsActivity.phone.equals("15357906428")) {
            WfcUIKit.singleCall(context1, "VaVvVvii", false);//13168306428
        } else {
            WfcUIKit.singleCall(context1, "sws8s888", false);//15357906428
        }
//        VoipExt.video
//        WfcUIKit.singleCall(context1, userid, false);//13168306428
//        WfcUIKit.singleCall(context1, "TUT9T9LL", false);
//        WfcUIKit.singleCall(context1, "agahahss", false);
//        WfcUIKit.singleCall(context1, "dzxGhGjj", false);
    }

    /**
     * 自定义的ViewHolder
     */
    class MyHolder extends RecyclerView.ViewHolder {

        private final TextView textfr;
        private final ConstraintLayout contactsFr;

        public MyHolder(View itemView) {
            super(itemView);
            textfr = itemView.findViewById(R.id.textfr);
            contactsFr = itemView.findViewById(R.id.contactsFr);
        }
    }
}























