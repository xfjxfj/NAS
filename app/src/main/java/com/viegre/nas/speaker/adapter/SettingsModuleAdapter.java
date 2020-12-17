package com.viegre.nas.speaker.adapter;

import android.content.res.TypedArray;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.viegre.nas.speaker.R;
import com.viegre.nas.speaker.entity.SettingsModuleEntity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by Djangoogle on 2020/12/16 16:41 with Android Studio.
 */
public class SettingsModuleAdapter extends BaseQuickAdapter<SettingsModuleEntity, BaseViewHolder> {

	public SettingsModuleAdapter(@Nullable List<SettingsModuleEntity> data) {
		super(R.layout.item_settings_module, data);
	}

	@Override
	protected void convert(@NotNull BaseViewHolder baseViewHolder, SettingsModuleEntity settingsModuleEntity) {
		baseViewHolder.setBackgroundResource(R.id.llcItemSettingsModuleRoot,
		                                     settingsModuleEntity.isSelected() ? R.color.item_settings_module_selected_bg : R.color.item_settings_module_unselected_bg)
		              .setGone(R.id.vItemSettingsModuleHalfLine, baseViewHolder.getAdapterPosition() == getData().size() - 1)
		              .setGone(R.id.vItemSettingsModuleLine, baseViewHolder.getAdapterPosition() < getData().size() - 1);
		AppCompatImageView acivItemSettingsModuleIcon = baseViewHolder.getView(R.id.acivItemSettingsModuleIcon);
		TypedArray typedArray = getContext().getResources().obtainTypedArray(R.array.settings_module_icon);
		acivItemSettingsModuleIcon.setImageResource(typedArray.getResourceId(baseViewHolder.getAdapterPosition(), 0));
		typedArray.recycle();
		AppCompatTextView actvItemSettingsModuleName = baseViewHolder.getView(R.id.actvItemSettingsModuleName);
		actvItemSettingsModuleName.setText(settingsModuleEntity.getName());
		actvItemSettingsModuleName.getPaint().setFakeBoldText(settingsModuleEntity.isSelected());
		baseViewHolder.getView(R.id.llcItemSettingsModuleRoot).setOnClickListener(view -> {
			if (!getData().get(baseViewHolder.getAdapterPosition()).isSelected()) {
				getData().get(baseViewHolder.getAdapterPosition()).setSelected(true);
				notifyItemChanged(baseViewHolder.getAdapterPosition());
			}
			for (int i = 0; i < getData().size(); i++) {
				if (i != baseViewHolder.getAdapterPosition() && getData().get(i).isSelected()) {
					getData().get(i).setSelected(false);
					notifyItemChanged(i);
					break;
				}
			}
		});
	}
}
