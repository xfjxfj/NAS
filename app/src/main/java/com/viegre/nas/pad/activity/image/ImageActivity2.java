package com.viegre.nas.pad.activity.image;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.blankj.utilcode.util.ThreadUtils;
import com.djangoogle.framework.activity.BaseActivity;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.adapter.ImageListAdapter;
import com.viegre.nas.pad.databinding.ActivityImageBinding;
import com.viegre.nas.pad.entity.ImageEntity;
import com.viegre.nas.pad.manager.TextStyleManager;
import com.viegre.nas.pad.widget.GridSpaceItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by レインマン on 2021/01/18 16:36 with Android Studio.
 */
public class ImageActivity2 extends BaseActivity<ActivityImageBinding> {

    private volatile boolean mIsPublic = true;

    private ImageListAdapter mImageListAdapter;

    @Override
    protected void initialize() {
        mViewBinding.iImageTitle.actvFileManagerTitle.setText(R.string.image);
        mViewBinding.iImageTitle.llcFileManagerTitleBack.setOnClickListener(view -> finish());
        initRadioGroup();
        initList();
    }

    private void initRadioGroup() {
        mViewBinding.rgImageTag.setOnCheckedChangeListener((radioGroup, i) -> {
            if (R.id.acrbImageTagPrivate == i) {
                mIsPublic = false;
            } else if (R.id.acrbImageTagPublic == i) {
                mIsPublic = true;
            }
            scanMedia();
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
        mViewBinding.srlImageRefresh.setOnRefreshListener(this::scanMedia);
        mViewBinding.srlImageRefresh.setRefreshing(true);
        scanMedia();
    }

    private void scanMedia() {
        getImageList();
//		MediaScanner mediaScanner = new MediaScanner(this, this::getImageList);
//		mediaScanner.scanFile(new File(mIsPublic ? PathConfig.PUBLIC : PathConfig.PRIVATE));
    }

    private void getImageList() {

        ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<List<ImageEntity>>() {
            @Override
            public List<ImageEntity> doInBackground() {
                List<ImageEntity> imageList = new ArrayList<>();
                ContentResolver contentResolver = mActivity.getContentResolver();
                Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
                while (cursor.moveToNext()) {
                    String dataPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    int width = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.WIDTH));
                    int height = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT));
                    imageList.add(new ImageEntity(dataPath));
                }
                cursor.close();
                mImageListAdapter.setList(imageList);
                return imageList;
            }

            @Override
            public void onSuccess(List<ImageEntity> result) {
                mImageListAdapter.setList(result);
                mViewBinding.srlImageRefresh.setRefreshing(false);
            }
        });
    }
}