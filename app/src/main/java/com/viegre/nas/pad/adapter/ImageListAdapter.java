package com.viegre.nas.pad.adapter;

import android.media.Image;
import android.view.View;
import android.widget.Toast;

import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.draggable.library.extension.ImageViewerHelper;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.entity.ImageEntity;
import com.viegre.nas.pad.util.CommonUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import androidx.appcompat.widget.AppCompatImageView;

import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.*;

/**
 * Created by レインマン on 2021/01/24 11:34 PM with Android Studio.
 */
public class ImageListAdapter extends BaseQuickAdapter<ImageEntity, BaseViewHolder> {

    private List<ImageEntity> imageList = new ArrayList<>();
    private List<String> iList = new ArrayList<>();

    public ImageListAdapter() {
        super(R.layout.item_image_list);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, ImageEntity imageEntity) {
        AppCompatImageView acivItemImageRoot = baseViewHolder.getView(R.id.acivItemImageRoot);
        Glide.with(getContext()).load(imageEntity.getPath()).centerCrop().into(acivItemImageRoot);
        imageList = getData();
        if (iList.isEmpty()) {
            for (ImageEntity entity : imageList) {
                iList.add(entity.getPath().toString());
            }
        }
        baseViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageViewerHelper.INSTANCE.showImages(getContext(), iList, getItemPosition(imageEntity), true);
            }
        });
    }
}
