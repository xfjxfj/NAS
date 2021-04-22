package com.viegre.nas.pad.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.viegre.nas.pad.R;

import java.util.List;

public class ContactsRvRecordAdapter extends RecyclerView.Adapter<ContactsRvRecordAdapter.MyHolder> {

    private final List<String> languages;

    public ContactsRvRecordAdapter(Context context, List<String> languages) {
        this.languages = languages;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //将我们自定义的item布局R.layout.item_one转换为View
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_record_rv_item, parent, false);
        //将view传递给我们自定义的ViewHolder
        ContactsRvRecordAdapter.MyHolder holder = new ContactsRvRecordAdapter.MyHolder(view);
        //返回这个MyHolder实体
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        holder.textView123.setText(languages.get(position));
    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    /**
     * 自定义的ViewHolder
     */
    class MyHolder extends RecyclerView.ViewHolder {

        private final TextView textView123;

        public MyHolder(View itemView) {
            super(itemView);
            textView123 = itemView.findViewById(R.id.text_re);
        }
    }
}
