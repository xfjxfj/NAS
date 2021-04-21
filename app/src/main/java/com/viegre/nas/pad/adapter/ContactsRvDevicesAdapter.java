package com.viegre.nas.pad.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.viegre.nas.pad.R;

import java.util.List;

public class ContactsRvDevicesAdapter extends RecyclerView.Adapter<ContactsRvDevicesAdapter.MyHolder> {

    private final List<String> languages;

    public ContactsRvDevicesAdapter(Context context, List<String> languages) {
        this.languages = languages;

    }

    @NonNull
    @Override
    public ContactsRvDevicesAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //将我们自定义的item布局R.layout.item_one转换为View
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.more_app_activity_rv_item, parent, false);
        //将view传递给我们自定义的ViewHolder
        ContactsRvDevicesAdapter.MyHolder holder = new ContactsRvDevicesAdapter.MyHolder(view);
        //返回这个MyHolder实体
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsRvDevicesAdapter.MyHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    /**
     * 自定义的ViewHolder
     */
    class MyHolder extends RecyclerView.ViewHolder {

        public MyHolder(View itemView) {
            super(itemView);
        }
    }
}
