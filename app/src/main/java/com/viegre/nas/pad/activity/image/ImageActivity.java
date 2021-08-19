package com.viegre.nas.pad.activity.image;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;

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
import java.sql.Time;
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
    private Button all_button;
    private Button private_button;
    private Button public_bt;
    private Button all_time_bt;
    private Button oneDayTime_bt;
    private Button threeDaysTime_bt;
    private Button sevenDaysTime_day;
    private Button oneMonthTime_bt;
    private Button threeMonthsTime_bt;
    private Button reset_bt;
    private Button window_ok;
    private ArrayList<Button> mButtonList;
    private ArrayList<Button> mButtonList1;
    private String rangeStringType = Type.ALL;
    private String timeStringType = TIME.ALL;
    private List<ImageEntity> allImageList = new ArrayList<>();

    @Override
    protected void initialize() {
        mViewBinding.iImageTitle.actvFileManagerTitle.setText(R.string.image);
        mViewBinding.iImageTitle.acivFileManagerTitleBack.setOnClickListener(view -> finish());
        mViewBinding.iImageTitle.acivFileManagerFilter.setOnClickListener(this);
        mViewBinding.imageActivityButtonBt.setOnClickListener(this);
        initRadioGroup();
        initList();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (SPUtils.getInstance().contains(SPConfig.PHONE)) {
            privateUI();
        } else {
            ifLogin();
            Log.d(CommonUtils.getFileName() + CommonUtils.getLineNumber(), "未登录");
        }
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
//                mIsPublic = true;
//            }
//            scanMedia();
//        });
//        TextStyleManager.INSTANCE.setFileManagerTagOnCheckedChange(mViewBinding.acrbImageTagPrivate, mViewBinding.acrbImageTagPublic);
//    }
    private void initRadioGroup() {
        mViewBinding.rgImageTag.setOnCheckedChangeListener((radioGroup, i) -> {
            if (R.id.acrbImageTagPrivate == i) {
                if (SPUtils.getInstance().contains(SPConfig.PHONE)) {
                    privateUI();
                } else {
                    ifLogin();
                    Log.d(CommonUtils.getFileName() + CommonUtils.getLineNumber(), "未登录");
                }
            } else if (R.id.acrbImageTagPublic == i) {
                publicPic();
            }
        });
        TextStyleManager.INSTANCE.setFileManagerTagOnCheckedChange(mViewBinding.acrbImageTagPrivate, mViewBinding.acrbImageTagPublic);
    }

    private void privateUI() {
        mViewBinding.rvImageList.setVisibility(View.VISIBLE);
        mViewBinding.srlImageRefresh.setVisibility(View.VISIBLE);
        mViewBinding.imageConst.setVisibility(View.GONE);
        queryImage("", Type.PRIVATE, TIME.ALL);
    }

    private void publicPic() {
        mViewBinding.rvImageList.setVisibility(View.VISIBLE);
        mViewBinding.srlImageRefresh.setVisibility(View.VISIBLE);
        mViewBinding.imageConst.setVisibility(View.GONE);
        queryImage("", Type.PUBLIC, TIME.ALL);
    }

    private void ifLogin() {
        mViewBinding.rvImageList.setVisibility(View.GONE);
        mViewBinding.srlImageRefresh.setVisibility(View.GONE);
        mViewBinding.imageConst.setVisibility(View.VISIBLE);
        mViewBinding.imageConst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mActivity, LoginActivity.class));
            }
        });
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
//                long nowTime = System.currentTimeMillis() / 1000;
                switch (mTime) {
                    case TIME.DAY_1:
                        selectionArgList.add(String.valueOf(nowTime - dayToSecond(1)));
                        break;

                    case TIME.DAY_3:
                        selectionArgList.add(String.valueOf(nowTime - dayToSecond(3)));
                        break;

                    case TIME.DAY_7:
                        selectionArgList.add(String.valueOf(nowTime - dayToSecond(7)));
                        break;

                    case TIME.MONTH_1:
                        selectionArgList.add(String.valueOf(nowTime - dayToSecond(30)));
                        break;

                    case TIME.MONTH_3:
                        selectionArgList.add(String.valueOf(nowTime - dayToSecond(90)));
                        break;

                    default:
                        selectionArgList.add(String.valueOf(0));
                        break;
                }
                getdataList(imageList, selection, selectionArgList);
                return imageList;
            }

            @Override
            public void onSuccess(List<ImageEntity> result) {
                if (ty) {
                    mType = Type.PRIVATE;
                    ty = false;
                    allImageList = result;
                    queryImage2();
                } else {
                    mImageListAdapter.setList(result);
                    mViewBinding.srlImageRefresh.setRefreshing(false);
                }
            }
        });
    }

    private void queryImage2() {
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
//                long nowTime = System.currentTimeMillis() / 1000;
                switch (mTime) {
                    case TIME.DAY_1:
                        selectionArgList.add(String.valueOf(nowTime - dayToSecond(1)));
                        break;

                    case TIME.DAY_3:
                        selectionArgList.add(String.valueOf(nowTime - dayToSecond(3)));
                        break;

                    case TIME.DAY_7:
                        selectionArgList.add(String.valueOf(nowTime - dayToSecond(7)));
                        break;

                    case TIME.MONTH_1:
                        selectionArgList.add(String.valueOf(nowTime - dayToSecond(30)));
                        break;

                    case TIME.MONTH_3:
                        selectionArgList.add(String.valueOf(nowTime - dayToSecond(90)));
                        break;

                    default:
                        selectionArgList.add(String.valueOf(0));
                        break;
                }
                getdataList(imageList, selection, selectionArgList);
                for (int i = 0; i < imageList.size(); i++) {
                    allImageList.add(imageList.get(i));
                }
                return allImageList;
            }

            @Override
            public void onSuccess(List<ImageEntity> result) {
                mImageListAdapter.setList(result);
                mViewBinding.srlImageRefresh.setRefreshing(false);
            }
        });
    }

    private void getdataList(List<ImageEntity> imageList, StringBuilder selection, List<String> selectionArgList) {
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
    }

    private void setPupwind() {
        rightpopuwindows = new RightPopupWindows(this, rightonclick);
        rightpopuwindows.showAtLocation(mViewBinding.mainlayout, Gravity.RIGHT, 0, 0);
        rightpopuwindows.setWindowAlpa(true);
        View view = rightpopuwindows.getmMenuView();

        all_button = view.findViewById(R.id.all_button);
        private_button = view.findViewById(R.id.private_button);
        public_bt = view.findViewById(R.id.public_bt);
        all_time_bt = view.findViewById(R.id.all_time_bt);
        oneDayTime_bt = view.findViewById(R.id.oneDayTime_bt);
        threeDaysTime_bt = view.findViewById(R.id.ThreeDaysTime_bt);
        sevenDaysTime_day = view.findViewById(R.id.sevenDaysTime_day);
        oneMonthTime_bt = view.findViewById(R.id.oneMonthTime_bt);
        threeMonthsTime_bt = view.findViewById(R.id.threeMonthsTime_bt);
        reset_bt = view.findViewById(R.id.reset_bt);
        window_ok = view.findViewById(R.id.window_ok);

        mButtonList = new ArrayList<>();
        mButtonList1 = new ArrayList<>();

        mButtonList.add(all_button);
        mButtonList.add(private_button);
        mButtonList.add(public_bt);

        mButtonList1.add(all_time_bt);
        mButtonList1.add(oneDayTime_bt);
        mButtonList1.add(threeDaysTime_bt);
        mButtonList1.add(sevenDaysTime_day);
        mButtonList1.add(oneMonthTime_bt);
        mButtonList1.add(threeMonthsTime_bt);

//        mButtonList.add(reset_bt);
//        mButtonList.add(window_ok);
        restButton();
        for (int i = 0; i < mButtonList.size(); i++) {
            mButtonList.get(i).setOnClickListener(this);
        }
        for (int i = 0; i < mButtonList1.size(); i++) {
            mButtonList1.get(i).setOnClickListener(this);
        }
        reset_bt.setOnClickListener(this);
        window_ok.setOnClickListener(this);


        rightpopuwindows.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                rightpopuwindows.setWindowAlpa(false);
            }
        });
    }

    @SuppressLint({"NonConstantResourceId", "ResourceAsColor", "NewApi"})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.acivFileManagerFilter:
                setPupwind();
                break;
            case R.id.image_activity_button_bt:
                startActivity(new Intent(ImageActivity.this, LoginActivity.class));
                break;
            case R.id.all_button:
                setButton(all_button, mButtonList);
                rangeStringType = Type.ALL;
                break;
            case R.id.private_button:
                setButton(private_button, mButtonList);
                rangeStringType = Type.PRIVATE;
                break;
            case R.id.public_bt:
                rangeStringType = Type.PUBLIC;
                setButton(public_bt, mButtonList);
                break;
            case R.id.all_time_bt:
                timeStringType = TIME.ALL;
                setButton(all_time_bt, mButtonList1);
                break;
            case R.id.oneDayTime_bt://1天内
                timeStringType = TIME.DAY_1;
                setButton(oneDayTime_bt, mButtonList1);
                break;
            case R.id.ThreeDaysTime_bt:
                timeStringType = TIME.DAY_3;
                setButton(threeDaysTime_bt, mButtonList1);
                break;
            case R.id.sevenDaysTime_day:
                timeStringType = TIME.DAY_7;
                setButton(sevenDaysTime_day, mButtonList1);
                break;
            case R.id.oneMonthTime_bt:
                timeStringType = TIME.MONTH_1;
                setButton(oneMonthTime_bt, mButtonList1);
                break;
            case R.id.threeMonthsTime_bt:
                timeStringType = TIME.MONTH_3;
                setButton(threeMonthsTime_bt, mButtonList1);
                break;
            case R.id.reset_bt:
                //重置
                restButton();
                break;
            case R.id.window_ok:
                //确定
                dataOk(rangeStringType, timeStringType);
                break;
        }
    }

    private void restButton() {
        setButton(all_button, mButtonList);
        setButton(all_time_bt, mButtonList1);
        rangeStringType = Type.ALL;
        timeStringType = TIME.ALL;
    }

    Boolean ty = false;

    private void dataOk(String rangeStringType, String timeStringType) {
        if ("all".equals(rangeStringType)) {
            rangeStringType = Type.PUBLIC;
            ty = true;
            queryImage("", rangeStringType, timeStringType);
        } else {
            queryImage("", rangeStringType, timeStringType);
        }
    }

    @SuppressLint("NewApi")
    private void resetData() {
        all_button.setBackgroundResource(R.color.blue4);
        all_button.setTextColor(getColor(R.color.image_puwind_button_text_2));
        rangeStringType = Type.ALL;
        timeStringType = TIME.ALL;
    }

    @SuppressLint("NewApi")
    private void setButton(Button mView, ArrayList<Button> mButtonList) {
        for (int i = 0; i < mButtonList.size(); i++) {
            if (mView.getId() == mButtonList.get(i).getId()) {
                mButtonList.get(i).setBackgroundResource(R.color.blue4);
                mButtonList.get(i).setTextColor(getColor(R.color.image_puwind_button_text_2));
            } else {
                mButtonList.get(i).setBackgroundResource(R.color.black13);
                mButtonList.get(i).setTextColor(getColor(R.color.image_puwind_button_text_1));
            }
        }
    }


    private final View.OnClickListener rightonclick = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            rightpopuwindows.dismiss();
        }
    };

    private int dayToSecond(int day) {
        return day * 24 * 60;
    }

    @Retention(SOURCE)
    @Target({PARAMETER})
    @StringDef(value = {Type.ALL, Type.PUBLIC, Type.PRIVATE})
    private @interface Type {
        String ALL = "all";
        String PUBLIC = "public";
        String PRIVATE = "private";
    }

    @Retention(SOURCE)
    @Target({PARAMETER})
    @StringDef(value = {TIME.ALL, TIME.DAY_1, TIME.DAY_3, TIME.DAY_7, TIME.MONTH_1, TIME.MONTH_3})
    private @interface TIME {
        String ALL = "all";
        String DAY_1 = "day1";
        String DAY_3 = "day3";
        String DAY_7 = "day7";
        String MONTH_1 = "month1";
        String MONTH_3 = "month3";
    }
}
