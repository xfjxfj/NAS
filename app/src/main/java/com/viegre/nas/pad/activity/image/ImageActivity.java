package com.viegre.nas.pad.activity.image;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.Utils;
import com.djangoogle.framework.activity.BaseActivity;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.activity.LoginActivity;
import com.viegre.nas.pad.adapter.ImageListAdapter;
import com.viegre.nas.pad.config.PathConfig;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.databinding.ActivityImageBinding;
import com.viegre.nas.pad.entity.ImageEntity;
import com.viegre.nas.pad.manager.TextStyleManager;
import com.viegre.nas.pad.util.CommonUtils;
import com.viegre.nas.pad.widget.GridSpaceItemDecoration;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.StringDef;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * 2021年5月18日
 */
public class ImageActivity extends BaseActivity<ActivityImageBinding> implements View.OnClickListener {

	private ImageListAdapter mImageListAdapter;
	private RightPopupWindows rightpopuwindows;
	private String mKeywords = "", mType = Type.PUBLIC, mTime = TIME.ALL;

	@Override
	protected void initialize() {
		mViewBinding.iImageTitle.actvFileManagerTitle.setText(R.string.image);
		mViewBinding.iImageTitle.llcFileManagerTitleBack.setOnClickListener(view -> finish());
		mViewBinding.iImageTitle.acivFileManagerFilter.setOnClickListener(this);
		mViewBinding.imageActivityButtonBt.setOnClickListener(this);
		initRadioGroup();
		initList();
	}

//    private void initRadioGroup() {
//        mViewBinding.rgImageTag.setOnCheckedChangeListener((radioGroup, i) -> {
//            if (R.id.acrbImageTagPrivate == i) {
//				if (!SPUtils.getInstance().contains(SPConfig.PHONE)) {
////                    mIsPublic = false;
//					mViewBinding.rvImageList.setVisibility(View.GONE);
//					mViewBinding.srlImageRefresh.setVisibility(View.GONE);
//					mViewBinding.imageActivityButtonBt.setVisibility(View.VISIBLE);
//				} else {
//
//				}
//            } else if (R.id.acrbImageTagPublic == i) {
//                mViewBinding.rvImageList.setVisibility(View.VISIBLE);
//                mViewBinding.srlImageRefresh.setVisibility(View.VISIBLE);
//                mViewBinding.imageActivityButtonBt.setVisibility(View.GONE);
////                mIsPublic = true;
//            }
//            scanMedia();
//        });
//        TextStyleManager.INSTANCE.setFileManagerTagOnCheckedChange(mViewBinding.acrbImageTagPrivate, mViewBinding.acrbImageTagPublic);
//    }
	private void initRadioGroup() {
		mViewBinding.rgImageTag.setOnCheckedChangeListener((radioGroup, i) -> {
			if (R.id.acrbImageTagPrivate == i) {
				if (SPUtils.getInstance().contains(SPConfig.PHONE)) {
					queryImage("", Type.PRIVATE, TIME.ALL);
				} else {
					mViewBinding.rvImageList.setVisibility(View.GONE);
				}
			} else if (R.id.acrbImageTagPublic == i) {
				mViewBinding.rvImageList.setVisibility(View.VISIBLE);
				queryImage("", Type.PUBLIC, TIME.ALL);
			}
		});
		TextStyleManager.INSTANCE.setFileManagerTagOnCheckedChange(mViewBinding.acrbImageTagPrivate, mViewBinding.acrbImageTagPublic);
	}

	private void initList() {
		mImageListAdapter = new ImageListAdapter();
		mViewBinding.rvImageList.setLayoutManager(new GridLayoutManager(this, 4));
		mViewBinding.rvImageList.addItemDecoration(new GridSpaceItemDecoration(4, 12, 12));
		mViewBinding.rvImageList.setAdapter(mImageListAdapter);
		//禁用动画以防止更新列表时产生闪烁
		SimpleItemAnimator simpleItemAnimator = (SimpleItemAnimator) mViewBinding.rvImageList.getItemAnimator();
		if (null != simpleItemAnimator) {
			simpleItemAnimator.setSupportsChangeAnimations(false);
		}
		mViewBinding.srlImageRefresh.setColorSchemeResources(R.color.settings_menu_selected_bg);
		mViewBinding.srlImageRefresh.setProgressBackgroundColorSchemeResource(R.color.file_manager_tag_unpressed);
		mViewBinding.srlImageRefresh.setOnRefreshListener(this::queryImage);
		mViewBinding.srlImageRefresh.setRefreshing(true);
		queryImage("", Type.PUBLIC, TIME.MONTH_3);
	}

	private void queryImage(String keywords, @Type String type, @TIME String time) {
		mKeywords = keywords;
		mType = type;
		mTime = time;
		queryImage();
	}

	private void queryImage() {
		ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<List<ImageEntity>>() {
			@Override
			public List<ImageEntity> doInBackground() {
				List<ImageEntity> imageList = new ArrayList<>();
				StringBuilder selection = new StringBuilder();
				List<String> selectionArgList = new ArrayList<>();
				switch (mType) {
					case Type.PUBLIC:
						selection.append(MediaStore.Images.ImageColumns.DATA + " like ? escape '/'");
						selectionArgList.add(CommonUtils.sqliteEscape(PathConfig.PUBLIC) + "%");
						break;

					case Type.PRIVATE:
						selection.append(MediaStore.Images.ImageColumns.DATA + " like ? escape '/'");
						selectionArgList.add(CommonUtils.sqliteEscape(PathConfig.PRIVATE + SPUtils.getInstance()
						                                                                          .getString(SPConfig.PHONE) + File.separator) + "%");
						break;

					default:
						selection.append(MediaStore.Images.ImageColumns.DATA + " like ? escape '/' and " + MediaStore.Images.ImageColumns.DATA + " like ? escape '/'");
						selectionArgList.add(CommonUtils.sqliteEscape(PathConfig.PUBLIC) + "%");
						selectionArgList.add(CommonUtils.sqliteEscape(PathConfig.PRIVATE + SPUtils.getInstance()
						                                                                          .getString(SPConfig.PHONE) + File.separator) + "%");
						break;
				}
				if (!mKeywords.isEmpty()) {
					selection.append(" and " + MediaStore.Images.ImageColumns.DISPLAY_NAME + " like ? escape '/'");
					selectionArgList.add(CommonUtils.sqliteEscape(mKeywords) + "%");
				}
				selection.append(" and " + MediaStore.Images.ImageColumns.DATE_MODIFIED + " > ?");
				long nowTime = System.currentTimeMillis() / 1000 / 60 / 60 / 24;
				switch (mTime) {
					case TIME.DAY_1:
						selectionArgList.add(String.valueOf(nowTime - 1));
						break;

					case TIME.DAY_3:
						selectionArgList.add(String.valueOf(nowTime - 3));
						break;

					case TIME.DAY_7:
						selectionArgList.add(String.valueOf(nowTime - 7));
						break;

					case TIME.MONTH_1:
						selectionArgList.add(String.valueOf(nowTime - 30));
						break;

					case TIME.MONTH_3:
						selectionArgList.add(String.valueOf(nowTime - 90));
						break;

					default:
						selectionArgList.add(String.valueOf(0));
						break;
				}
				Cursor cursor = Utils.getApp()
				                     .getContentResolver()
				                     .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				                            new String[]{MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.DISPLAY_NAME, MediaStore.Images.ImageColumns.DATE_MODIFIED},
				                            selection.toString(),
				                            selectionArgList.toArray(new String[0]),
				                            null);
				if (null != cursor) {
					while (cursor.moveToNext()) {
						String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA));
						imageList.add(new ImageEntity(path));
					}
					cursor.close();
				}
				return imageList;
			}

			@Override
			public void onSuccess(List<ImageEntity> result) {
				mImageListAdapter.setList(result);
				mViewBinding.srlImageRefresh.setRefreshing(false);
			}
		});
	}

	@SuppressLint("NonConstantResourceId")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.acivFileManagerFilter:
				setPupwind();
				break;
			case R.id.image_activity_button_bt:
				startActivity(new Intent(ImageActivity.this, LoginActivity.class));
				break;
		}
	}

	private void setPupwind() {
		rightpopuwindows = new RightPopupWindows(this, rightonclick);
		rightpopuwindows.showAtLocation(mViewBinding.mainlayout, Gravity.RIGHT, 0, 0);
		rightpopuwindows.setWindowAlpa(true);

		rightpopuwindows.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				rightpopuwindows.setWindowAlpa(false);
			}
		});
	}

	private final View.OnClickListener rightonclick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			rightpopuwindows.dismiss();
			switch (v.getId()) {
				case R.id.shenqing:
					Toast.makeText(ImageActivity.this, "菜单1", Toast.LENGTH_SHORT).show();
					break;

				case R.id.exit:
					Toast.makeText(ImageActivity.this, "退出", Toast.LENGTH_SHORT).show();
					break;
			}
		}
	};

	@Retention(SOURCE)
	@Target({PARAMETER})
	@StringDef(value = {Type.ALL, Type.PUBLIC, Type.PRIVATE})
	public @interface Type {
		String ALL = "all";
		String PUBLIC = "public";
		String PRIVATE = "private";
	}

	@Retention(SOURCE)
	@Target({PARAMETER})
	@StringDef(value = {TIME.ALL, TIME.DAY_1, TIME.DAY_3, TIME.DAY_7, TIME.MONTH_1, TIME.MONTH_3})
	public @interface TIME {
		String ALL = "all";
		String DAY_1 = "day1";
		String DAY_3 = "day3";
		String DAY_7 = "day7";
		String MONTH_1 = "month1";
		String MONTH_3 = "month3";
	}
}
