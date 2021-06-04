package com.viegre.nas.pad.activity.image;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.djangoogle.framework.activity.BaseActivity;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.adapter.ImageListAdapter;
import com.viegre.nas.pad.config.PathConfig;
import com.viegre.nas.pad.databinding.ActivityImageBinding;
import com.viegre.nas.pad.entity.ImageEntity;
import com.viegre.nas.pad.manager.TextStyleManager;
import com.viegre.nas.pad.util.MediaScanner;
import com.viegre.nas.pad.widget.GridSpaceItemDecoration;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 2021年5月18日
 */
public class ImageActivity extends BaseActivity<ActivityImageBinding> implements View.OnClickListener {

    private volatile boolean mIsPublic = true;
    private List<ImageEntity> imageList;
    private ImageListAdapter mImageListAdapter;
    private RightPopupWindows rightpopuwindows;

    @Override
    protected void initialize() {
//        mainlayout = findViewById(R.id.mainlayout);
        mViewBinding.iImageTitle.actvFileManagerTitle.setText(R.string.image);
        mViewBinding.iImageTitle.llcFileManagerTitleBack.setOnClickListener(view -> finish());
        mViewBinding.iImageTitle.acivFileManagerFilter.setOnClickListener(this);
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
        MediaScanner mediaScanner = new MediaScanner(this, this::getImageList);
        mediaScanner.scanFile(new File(mIsPublic ? PathConfig.PUBLIC : PathConfig.PRIVATE));
    }

    private void getImageList() {
        ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<List<ImageEntity>>() {
            @Override
            public List<ImageEntity> doInBackground() {
                imageList = new ArrayList<>();
                ContentResolver contentResolver = mActivity.getContentResolver();
                Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
                while (cursor.moveToNext()) {
                    String dataPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    int width = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.WIDTH));
                    int height = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT));
                    imageList.add(new ImageEntity(dataPath));
                }
                cursor.close();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImageListAdapter.setList(imageList);
                        mViewBinding.srlImageRefresh.setRefreshing(true);
                    }
                });
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
        }
    }

    private void setPupwind() {
        rightpopuwindows = new RightPopupWindows(this,rightonclick );
        rightpopuwindows.showAtLocation(mViewBinding.mainlayout, Gravity.RIGHT,0,0);
        rightpopuwindows.setWindowAlpa(true);

        rightpopuwindows.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                rightpopuwindows.setWindowAlpa(false);
            }
        });
    }

    private View.OnClickListener rightonclick = new View.OnClickListener() {
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
}
