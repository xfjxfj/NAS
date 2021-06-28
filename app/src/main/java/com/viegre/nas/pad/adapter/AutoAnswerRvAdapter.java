package com.viegre.nas.pad.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.TimeUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.entity.AudioEntity;
import com.viegre.nas.pad.entity.ContactsBean;
import com.viegre.nas.pad.manager.TextStyleManager;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * xfj 2021年6月25日
 */
public class AutoAnswerRvAdapter extends RecyclerView.Adapter {
    private final List<ContactsBean> mData;

    public AutoAnswerRvAdapter(List<ContactsBean> mDevicesData) {
        this.mData = mDevicesData;
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_devices_rv_add_item,parent,false);
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
