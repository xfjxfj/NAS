package org.primftpd.filepicker.nononsenseapps;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

/**
 * A simple adapter which also inserts a header item ".." to handle going up to the parent folder.
 *
 * @param <T> the type which is used, for example a normal java File object.
 */
public class FileItemAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	protected final LogicHandler<T> mLogic;
	protected SortedList<T> mList = null;

	public FileItemAdapter(@NonNull LogicHandler<T> logic) {
		this.mLogic = logic;
	}

	public void setList(@Nullable SortedList<T> list) {
		mList = list;
		notifyDataSetChanged();
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return mLogic.onCreateViewHolder(parent, viewType);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int headerPosition) {
		if (headerPosition == 0) {
			mLogic.onBindHeaderViewHolder((AbstractFilePickerFragment<T>.HeaderViewHolder) viewHolder);
		} else {
			int pos = headerPosition - 1;
			mLogic.onBindViewHolder((AbstractFilePickerFragment<T>.DirViewHolder) viewHolder, pos, mList.get(pos));
		}
	}

	@Override
	public int getItemViewType(int headerPosition) {
		if (0 == headerPosition) {
			return LogicHandler.VIEWTYPE_HEADER;
		} else {
			int pos = headerPosition - 1;
			return mLogic.getItemViewType(pos, mList.get(pos));
		}
	}

	@Override
	public int getItemCount() {
		if (mList == null) {
			return 0;
		}

		// header + count
		return 1 + mList.size();
	}

	/**
	 * Get the item at the designated position in the adapter.
	 *
	 * @param position of item in adapter
	 * @return null if position is zero (that means it's the ".." header), the item otherwise.
	 */
	protected @Nullable
	T getItem(int position) {
		if (position == 0) {
			return null;
		}
		return mList.get(position - 1);
	}
}
