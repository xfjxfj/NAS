package com.viegre.nas.pad.widget;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by レインマン on 2021/01/25 12:53 AM with Android Studio.
 */
public class GridSpaceItemDecoration extends RecyclerView.ItemDecoration {

	private final int mSpanCount;//横条目数量
	private final int mRowSpacing;//行间距
	private final int mColumnSpacing;// 列间距

	public GridSpaceItemDecoration(int spanCount, int rowSpacing, int columnSpacing) {
		mSpanCount = spanCount;
		mRowSpacing = rowSpacing;
		mColumnSpacing = columnSpacing;
	}

	@Override
	public void getItemOffsets(
			@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
		int position = parent.getChildAdapterPosition(view); // 获取view 在adapter中的位置。
		int column = position % mSpanCount; // view 所在的列

		outRect.left = column * mColumnSpacing / mSpanCount; // column * (列间距 * (1f / 列数))
		outRect.right = mColumnSpacing - (column + 1) * mColumnSpacing / mSpanCount; // 列间距 - (column + 1) * (列间距 * (1f /列数))

		// 如果position > 行数，说明不是在第一行，则不指定行高，其他行的上间距为 top=mRowSpacing
		if (position >= mSpanCount) {
			outRect.top = mRowSpacing; // item top
		}
	}
}
