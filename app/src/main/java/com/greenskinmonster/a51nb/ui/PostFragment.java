package com.greenskinmonster.a51nb.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.greenskinmonster.a51nb.BuildConfig;
import com.greenskinmonster.a51nb.R;
import com.greenskinmonster.a51nb.async.PostHelper;
import com.greenskinmonster.a51nb.async.PrePostAsyncTask;
import com.greenskinmonster.a51nb.bean.Forum;
import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.bean.PostBean;
import com.greenskinmonster.a51nb.bean.PrePostInfoBean;
import com.greenskinmonster.a51nb.db.Content;
import com.greenskinmonster.a51nb.db.ContentDao;
import com.greenskinmonster.a51nb.job.ImageUploadEvent;
import com.greenskinmonster.a51nb.job.ImageUploadJob;
import com.greenskinmonster.a51nb.job.JobMgr;
import com.greenskinmonster.a51nb.job.PostEvent;
import com.greenskinmonster.a51nb.job.PostJob;
import com.greenskinmonster.a51nb.job.UploadImage;
import com.greenskinmonster.a51nb.ui.adapter.GridImageAdapter;
import com.greenskinmonster.a51nb.ui.adapter.SimpleTypeAdapter;
import com.greenskinmonster.a51nb.ui.widget.ContentLoadingProgressBar;
import com.greenskinmonster.a51nb.ui.widget.CountdownButton;
import com.greenskinmonster.a51nb.ui.widget.HiProgressDialog;
import com.greenskinmonster.a51nb.ui.widget.OnSingleClickListener;
import com.greenskinmonster.a51nb.ui.widget.OnViewItemSingleClickListener;
import com.greenskinmonster.a51nb.utils.Constants;
import com.greenskinmonster.a51nb.utils.HiUtils;
import com.greenskinmonster.a51nb.utils.HtmlCompat;
import com.greenskinmonster.a51nb.utils.UIUtils;
import com.greenskinmonster.a51nb.utils.Utils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.vanniktech.emoji.EmojiEditText;
import com.vdurmont.emoji.EmojiParser;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PostFragment extends BaseFragment {
    private static final int SELECT_PICTURE = 1;

    public static final String ARG_FID_KEY = "fid";
    public static final String ARG_TID_KEY = "tid";
    public static final String ARG_PID_KEY = "pid";
    public static final String ARG_FLOOR_KEY = "floor";
    public static final String ARG_FLOOR_AUTHOR_KEY = "floor_author";
    public static final String ARG_TEXT_KEY = "text";
    public static final String ARG_QUOTE_TEXT_KEY = "quote_text";
    public static final String ARG_MODE_KEY = "mode";
    public static final String ARG_PARENT_ID = "parent_id";
    public static final String ARG_SPECIAL_ID = "special";

    public static final String BUNDLE_POSITION_KEY = "content_position";
    private LayoutInflater mInflater;

    private int mFid;
    private String mSpecial = "";
    private String mTid;
    private String mPid;
    private int mFloor;
    private String mFloorAuthor;
    private String mText;
    private String mQuoteText;
    private int mMode;
    private TextView mTvQuoteText;
    private TextView mTvType;
    private TextView mTvTopic;
    private TextView mTvReadPerm;
    private TextView mTvImagesInfo;
    private TextView mTvCredit;
    private EditText mEtSubject;
    private EmojiEditText mEtContent;
    private ImageButton mIbEmojiSwitch;
    private int mContentPosition = -1;

    private PrePostAsyncTask.PrePostListener mPrePostListener = new PrePostListener();
    private PrePostInfoBean mPrePostInfo;
    private PrePostAsyncTask mPrePostAsyncTask;
    private Snackbar mSnackbar;
    private int mFetchInfoCount = 0;
    private boolean mFetchingInfo = false;
    private ContentLoadingProgressBar mProgressBar;
    private Drawable mIbDrawable;

    private String mForumName;
    private String mParentSessionId;
    private String mTypeId = "0";
    private String mTopic = "";
    private Map<String, String> mTypeValues;
    private Map<String, String> mTopicValues;

    private GridImageAdapter mImageAdapter;
    private HiProgressDialog mProgressDialog;
    private boolean mImageUploading = false;
    private Map<Uri, UploadImage> mUploadImages = new LinkedHashMap<>();
    private Collection<Uri> mHoldedImages = new ArrayList<>();
    private long mLastSavedTime = -1;

    private EditText mEtItemName;
    private EditText mEtItemLocus;
    private EditText mEtItemPrice;

    private EditText mEtPollMaxChoices;
    private EditText mEtPollDays;
    private CheckBox mCbPollVisibility;
    private CheckBox mCbPollOvert;
    private LinearLayout mPollLayout;
    private LinearLayout mPollChoicesLayout;
    private Button mBtnPollAddChoice;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(false);

        if (getArguments().containsKey(ARG_FID_KEY)) {
            mFid = getArguments().getInt(ARG_FID_KEY);
        }
        if (getArguments().containsKey(ARG_SPECIAL_ID)) {
            mSpecial = getArguments().getString(ARG_SPECIAL_ID);
        }
        if (getArguments().containsKey(ARG_TID_KEY)) {
            mTid = getArguments().getString(ARG_TID_KEY);
        }
        if (getArguments().containsKey(ARG_PID_KEY)) {
            mPid = getArguments().getString(ARG_PID_KEY);
        }
        if (getArguments().containsKey(ARG_FLOOR_KEY)) {
            mFloor = getArguments().getInt(ARG_FLOOR_KEY);
        }
        if (getArguments().containsKey(ARG_FLOOR_AUTHOR_KEY)) {
            mFloorAuthor = getArguments().getString(ARG_FLOOR_AUTHOR_KEY);
        }
        if (getArguments().containsKey(ARG_MODE_KEY)) {
            mMode = getArguments().getInt(ARG_MODE_KEY);
        }
        if (getArguments().containsKey(ARG_TEXT_KEY)) {
            mText = getArguments().getString(ARG_TEXT_KEY);
        }
        if (getArguments().containsKey(ARG_QUOTE_TEXT_KEY)) {
            mQuoteText = getArguments().getString(ARG_QUOTE_TEXT_KEY);
        }
        if (getArguments().containsKey(ARG_PARENT_ID)) {
            mParentSessionId = getArguments().getString(ARG_PARENT_ID);
        }

        mIbDrawable = new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_close).sizeDp(16).color(Color.GRAY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mInflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        mEtContent = (EmojiEditText) view.findViewById(R.id.et_reply);
        mTvQuoteText = (TextView) view.findViewById(R.id.tv_quote_text);
        mTvType = (TextView) view.findViewById(R.id.tv_type);
        mTvTopic = (TextView) view.findViewById(R.id.tv_topic);
        mTvReadPerm = (TextView) view.findViewById(R.id.tv_readperm);
        mTvCredit = (TextView) view.findViewById(R.id.tv_credit);
        mTvImagesInfo = (TextView) view.findViewById(R.id.tv_image_info);
        mProgressBar = (ContentLoadingProgressBar) view.findViewById(R.id.preinfo_loading);

        if (PostHelper.SPECIAL_TRADE.equals(mSpecial)
                && (mMode == PostHelper.MODE_NEW_THREAD
                || (mMode == PostHelper.MODE_EDIT_POST && mFloor == 1))) {
            LinearLayout tradeLayout = (LinearLayout) view.findViewById(R.id.trade_layout);
            tradeLayout.setVisibility(View.VISIBLE);
            mEtItemName = (EditText) view.findViewById(R.id.et_item_name);
            mEtItemLocus = (EditText) view.findViewById(R.id.et_item_locus);
            mEtItemPrice = (EditText) view.findViewById(R.id.et_item_price);
        }

        mPollLayout = (LinearLayout) view.findViewById(R.id.poll_layout);
        mEtPollMaxChoices = (EditText) view.findViewById(R.id.et_poll_max_choices);
        mEtPollDays = (EditText) view.findViewById(R.id.et_poll_days);
        mCbPollVisibility = (CheckBox) view.findViewById(R.id.cb_poll_visibility);
        mCbPollOvert = (CheckBox) view.findViewById(R.id.cb_poll_overt);
        mPollChoicesLayout = (LinearLayout) view.findViewById(R.id.poll_choices_layout);
        mBtnPollAddChoice = (Button) view.findViewById(R.id.btn_add_choices);

        mImageAdapter = new GridImageAdapter(getActivity());

        mTvImagesInfo.setText("图片信息");
        mTvImagesInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContentPosition = mEtContent.getSelectionStart();
                showImagesDialog();
            }
        });
        updateImageInfo();

        Forum forum = HiUtils.getForumByFid(mFid);
        if (forum != null)
            mForumName = forum.getName();

        mEtSubject = (EditText) view.findViewById(R.id.et_subject);
        mEtContent.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
        UIUtils.setLineSpacing(mEtContent);

        mEtContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                savePostConent(false);
            }
        });

        mEtContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    savePostConent(true);
            }
        });

        mEtSubject.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && mEmojiPopup != null && mEmojiPopup.isShowing())
                    mEmojiPopup.dismiss();
            }
        });

        if (!TextUtils.isEmpty(mText)) {
            mEtContent.setText(mText);
        }

        final CountdownButton countdownButton = (CountdownButton) view.findViewById(R.id.countdown_button);
        countdownButton.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_send).sizeDp(28).color(Color.GRAY));
        countdownButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                postReply();
            }
        });

        if (mMode != PostHelper.MODE_EDIT_POST)
            countdownButton.setCountdown(PostHelper.getWaitTimeToPost());

        mIbEmojiSwitch = (ImageButton) view.findViewById(R.id.ib_emoji_switch);
        setUpEmojiPopup(mEtContent, mIbEmojiSwitch);

        setActionBarTitle(R.string.action_reply);

        switch (mMode) {
            case PostHelper.MODE_REPLY_THREAD:
                setActionBarTitle("回复帖子");
                break;
            case PostHelper.MODE_REPLY_POST:
                setActionBarTitle("回复 " + mFloor + "# " + mFloorAuthor);
                break;
            case PostHelper.MODE_NEW_THREAD:
                setActionBarTitle("发表 · " + mForumName);
                mEtSubject.setVisibility(View.VISIBLE);
                break;
            case PostHelper.MODE_EDIT_POST:
                setActionBarTitle(getActivity().getResources().getString(R.string.action_edit));
                break;
        }
        return view;
    }

    private void savePostConent(boolean force) {
        if (force || SystemClock.uptimeMillis() - mLastSavedTime > 3000) {
            mLastSavedTime = SystemClock.uptimeMillis();
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    ContentDao.saveContent(mSessionId, mEtContent.getText().toString());
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        if (mPrePostInfo == null) {
            fetchPrePostInfo(false);
        } else {
            setupPrePostInfo();
        }

        if (mMode == PostHelper.MODE_NEW_THREAD) {
            (new Handler()).postDelayed(new Runnable() {
                public void run() {
                    mEtSubject.requestFocus();
                    long t = SystemClock.uptimeMillis();
                    mEtSubject.dispatchTouchEvent(MotionEvent.obtain(t, t, MotionEvent.ACTION_DOWN, 0, 0, 0));
                    mEtSubject.dispatchTouchEvent(MotionEvent.obtain(t, t, MotionEvent.ACTION_UP, 0, 0, 0));
                }
            }, 100);
        } else {
            (new Handler()).postDelayed(new Runnable() {
                public void run() {
                    mEtContent.requestFocus();
                    long t = SystemClock.uptimeMillis();
                    mEtContent.dispatchTouchEvent(MotionEvent.obtain(t, t, MotionEvent.ACTION_DOWN, 0, 0, 0));
                    mEtContent.dispatchTouchEvent(MotionEvent.obtain(t, t, MotionEvent.ACTION_UP, 0, 0, 0));
                    if (mContentPosition < 0)
                        mEtContent.setSelection(mEtContent.getText().length());
                }
            }, 100);
        }
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        savePostConent(true);
        if (mPrePostInfo != null) {
            if (mEtSubject.getVisibility() == View.VISIBLE)
                mPrePostInfo.setSubject(mEtSubject.getText().toString());
            mPrePostInfo.setText(mEtContent.getText().toString());
            mPrePostInfo.setTypeId(mTypeId);

            if (PostHelper.SPECIAL_TRADE.equals(mSpecial)) {
                mPrePostInfo.setItemName(mEtItemName.getText().toString());
                mPrePostInfo.setItemPrice(mEtItemPrice.getText().toString());
                mPrePostInfo.setItemLocus(mEtItemLocus.getText().toString());
            }

            updatePostingDatas();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mSnackbar != null)
            mSnackbar.dismiss();
        if (mPrePostAsyncTask != null)
            mPrePostAsyncTask.cancel(true);
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BUNDLE_POSITION_KEY, mContentPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mContentPosition = savedInstanceState.getInt(BUNDLE_POSITION_KEY, -1);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_reply, menu);

        menu.findItem(R.id.action_upload_img).setIcon(new IconicsDrawable(getActivity(),
                GoogleMaterial.Icon.gmd_add_a_photo).actionBar()
                .color(HiSettingsHelper.getInstance().getToolbarTextColor()));

        if (HiUtils.CLIENT_TID == Utils.parseInt(mTid)) {
            MenuItem menuItem = menu.findItem(R.id.action_device_info);
            menuItem.setIcon(new IconicsDrawable(getActivity(),
                    GoogleMaterial.Icon.gmd_bug_report).actionBar()
                    .color(HiSettingsHelper.getInstance().getToolbarTextColor()));
            menuItem.setVisible(true);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Implemented in activity
                return false;
            case R.id.action_upload_img:
                if (mPrePostInfo == null) {
                    fetchPrePostInfo(false);
                    UIUtils.toast("请等待信息收集结束再选择图片");
                } else {
                    if (UIUtils.askForBothPermissions(getActivity()))
                        return true;

                    showImageSelector();
                }
                return true;
            case R.id.action_device_info:
                showAppendDeviceInfoDialog();
                return true;
            case R.id.action_restore_content:
                mEtContent.requestFocus();
                showRestoreContentDialog();
                return true;
            default:
                return false;
        }
    }

    protected void showImageSelector() {
        mContentPosition = mEtContent.getSelectionStart();

        Matisse.from(PostFragment.this)
                .choose(MimeType.ofImage())
                .countable(true)
                .maxSelectable(9)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .theme(HiSettingsHelper.getInstance().getImageActivityTheme(getActivity()))
                .capture(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                .captureStrategy(new CaptureStrategy(false, BuildConfig.APPLICATION_ID + ".provider"))
                .forResult(SELECT_PICTURE);
    }

    private void postReply() {
        if (mPrePostInfo == null) {
            fetchPrePostInfo(false);
            UIUtils.toast("请等待信息收集结束再发送");
            return;
        }

        if (mMode == PostHelper.MODE_NEW_THREAD &&
                mPrePostInfo.isTypeRequired() && "0".equals(mTypeId)) {
            UIUtils.toast("请选择主题分类");
            return;
        }

        final String subjectText = mEtSubject.getText().toString();
        if (mEtSubject.getVisibility() == View.VISIBLE) {
            if (Utils.getWordCount(subjectText) < 4) {
                UIUtils.toast("主题字数必须大于 4");
                return;
            }
            if (Utils.getWordCount(subjectText) > 80) {
                UIUtils.toast("主题字数必须少于 80");
                return;
            }
        }

        final String replyText = mEtContent.getText().toString();
        if (Utils.getWordCount(replyText) < 4) {
            UIUtils.toast("帖子内容字数必须大于 4");
            return;
        }

        if (PostHelper.SPECIAL_TRADE.equals(mSpecial)) {
            if (mEtItemName.getText().length() == 0) {
                UIUtils.toast("请输入商品名称");
                mEtItemName.requestFocus();
                return;
            }
            if (mEtItemLocus.getText().length() == 0) {
                UIUtils.toast("请输入所在地点");
                mEtItemLocus.requestFocus();
                return;
            }
            if (mEtItemPrice.getText().length() == 0) {
                UIUtils.toast("请输入商品价格");
                mEtItemPrice.requestFocus();
                return;
            }
            mPrePostInfo.setItemLocus(mEtItemLocus.getText().toString());
            mPrePostInfo.setItemName(mEtItemName.getText().toString());
            mPrePostInfo.setItemPrice(mEtItemPrice.getText().toString());
        }
        if (PostHelper.SPECIAL_POLL.equals(mSpecial)) {
            if (Utils.parseInt(mEtPollMaxChoices.getText().toString()) < 1) {
                UIUtils.toast("请输入最多可选项数目, 至少 1 个");
                mEtPollMaxChoices.requestFocus();
                return;
            }
            if (mPollChoicesLayout.getChildCount() < 2) {
                UIUtils.toast("请至少填写 2 个选项");
                return;
            }
            List<String> choices = new ArrayList<>();
            for (int i = 0; i < mPollChoicesLayout.getChildCount(); i++) {
                View view = mPollChoicesLayout.getChildAt(i);
                EditText etChoice = (EditText) view.findViewById(R.id.et_poll_text);
                if (TextUtils.isEmpty(etChoice.getText().toString().trim())) {
                    UIUtils.toast("选项内容不能为空");
                    etChoice.requestFocus();
                    return;
                }
            }
            mPrePostInfo.setPollChoices(choices);
        }

        UIUtils.hideSoftKeyboard(getActivity());

        final List<String> extraImgs = new ArrayList<>();
        if (mPrePostInfo.getAllImages().size() > 0) {
            for (String imgId : mPrePostInfo.getAllImages()) {
                String attachStr = "[attachimg]" + imgId + "[/attachimg]";
                if (!replyText.contains(attachStr)) {
                    extraImgs.add(imgId);
                }
            }
            if (extraImgs.size() > 0) {
                Dialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle("未使用的图片")
                        .setMessage(HtmlCompat.fromHtml("有 " + extraImgs.size() + " 张图片未以标签[attachimg]形式显示在帖子中<br>"
                                + "<br>如果您希望显示这些图片，请选择 <b>保留图片</b>"
                                + "<br>否则请选择 <b>丢弃图片</b>"))
                        .setPositiveButton("保留图片",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        StringBuilder sb = new StringBuilder();
                                        sb.append(replyText).append("\n");
                                        for (String imgId : extraImgs) {
                                            sb.append("[attachimg]").append(imgId).append("[/attachimg]").append("\n");
                                            mPrePostInfo.addNewAttach(imgId);
                                        }
                                        startPostJob(subjectText, sb.toString());
                                    }
                                })
                        .setNeutralButton("丢弃图片",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        for (String imgId : extraImgs) {
                                            mPrePostInfo.addDeleteAttach(imgId);
                                        }
                                        startPostJob(subjectText, replyText);
                                    }
                                }).create();
                dialog.show();
                return;
            }
        }
        startPostJob(subjectText, replyText);
    }

    private void startPostJob(String subjectText, String replyText) {
        PostBean postBean = new PostBean();
        postBean.setContent(replyText);
        postBean.setTid(mTid);
        postBean.setPid(mPid);
        postBean.setFid(mFid);
        postBean.setTypeid(mTypeId);
        postBean.setSubject(subjectText);
        postBean.setFloor(mFloor);

        updatePostingDatas();

        JobMgr.addJob(new PostJob(mSessionId, mMode, mPrePostInfo, postBean, false));
    }

    private void updatePostingDatas() {
        if (PostHelper.SPECIAL_POLL.equals(mSpecial)) {
            mPrePostInfo.setPollVisibility(mCbPollVisibility.isChecked());
            mPrePostInfo.setPollOvert(mCbPollOvert.isChecked());
            mPrePostInfo.setPollDays(mEtPollDays.getText().toString());
            mPrePostInfo.setPollMaxChoices(mEtPollMaxChoices.getText().toString());
            List<String> choices = new ArrayList<>();
            for (int i = 0; i < mPollChoicesLayout.getChildCount(); i++) {
                View view = mPollChoicesLayout.getChildAt(i);
                EditText etChoice = (EditText) view.findViewById(R.id.et_poll_text);
                choices.add(etChoice.getText().toString());
            }
            mPrePostInfo.setPollChoices(choices);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //avoid double click select button
        if (mImageUploading) {
            return;
        }
        mImageUploading = true;
        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                mImageUploading = false;
            }
        }, 2000);

        if (resultCode == Activity.RESULT_OK && requestCode == SELECT_PICTURE) {
            boolean duplicate = false;
            Collection<Uri> uris = new ArrayList<>();

            List<Uri> selects = Matisse.obtainResult(intent);
            for (Uri uri : selects) {
                if (!mUploadImages.containsKey(uri)) {
                    uris.add(uri);
                } else {
                    duplicate = true;
                }
            }

            if (uris.size() == 0) {
                if (duplicate) {
                    UIUtils.toast("选择的图片重复");
                } else {
                    UIUtils.toast("无法获取图片信息");
                }
                return;
            }

            mProgressDialog = HiProgressDialog.show(getActivity(), "正在上传...");
            if (mPrePostInfo != null) {
                JobMgr.addJob(new ImageUploadJob(mSessionId, mPrePostInfo.getHash(), uris.toArray(new Uri[uris.size()])));
            } else {
                //hold selected images, upload them after fetch pre post info success
                mHoldedImages.addAll(uris);
            }
        }
    }

    private void showRestoreContentDialog() {
        final Content[] contents = ContentDao.getSavedContents(mSessionId);

        if (contents == null || contents.length == 0) {
            UIUtils.toast("没有之前输入的内容");
            return;
        }

        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View viewlayout = inflater.inflate(R.layout.dialog_restore_content, null);
        final ListView listView = (ListView) viewlayout.findViewById(R.id.lv_contents);

        listView.setAdapter(new SavedContentsAdapter(getActivity(), 0, contents));

        final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
        popDialog.setView(viewlayout);
        final AlertDialog dialog = popDialog.create();
        dialog.show();

        listView.setOnItemClickListener(new OnViewItemSingleClickListener() {
            @Override
            public void onItemSingleClick(AdapterView<?> adapterView, View view, int position, long row) {
                if (!TextUtils.isEmpty(mEtContent.getText()) && !mEtContent.getText().toString().endsWith("\n"))
                    mEtContent.append("\n");
                mEtContent.append(contents[position].getContent());
                mEtContent.requestFocus();
                mEtContent.setSelection(mEtContent.getText().length());
                dialog.dismiss();
            }
        });

    }

    private void updateImageInfo() {
        if (mUploadImages.size() > 0) {
            mTvImagesInfo.setVisibility(View.VISIBLE);
            mTvImagesInfo.setText("图片(" + mUploadImages.size() + ")");
            if (mImageAdapter != null)
                mImageAdapter.setImages(mUploadImages.values());
        } else {
            mTvImagesInfo.setText("没有图片");
            mTvImagesInfo.setVisibility(View.GONE);
        }
    }

    private void appendImage(String imgId) {
        if (HiUtils.isValidId(imgId)) {
            String imgTxt = "[attachimg]" + imgId + "[/attachimg]\n";
            int selectionStart = mContentPosition;
            if (mContentPosition < 0 || mContentPosition > mEtContent.length())
                selectionStart = mEtContent.getSelectionStart();
            if (selectionStart > 0 && mEtContent.getText().charAt(selectionStart - 1) != '\n')
                imgTxt = "\n" + imgTxt;
            mEtContent.getText().insert(selectionStart, imgTxt);
            mEtContent.setSelection(selectionStart + imgTxt.length());
            mContentPosition = selectionStart + imgTxt.length();
            mEtContent.requestFocus();
            mPrePostInfo.addNewAttach(imgId);
            mPrePostInfo.addImage(imgId);
        }
    }

    private class PrePostListener implements PrePostAsyncTask.PrePostListener {
        @Override
        public void PrePostComplete(int mode, boolean result, String message, PrePostInfoBean info) {
            mFetchingInfo = false;
            mProgressBar.hide();
            if (result) {
                mPrePostInfo = info;
                setupPrePostInfo();
                if (mFetchInfoCount > 1)
                    UIUtils.toast("收集信息成功");
            } else {
                if (getView() != null) {
                    mSnackbar = Snackbar.make(getView(), "收集信息失败 : " + message, Snackbar.LENGTH_LONG);
                    UIUtils.setSnackbarMessageTextColor(mSnackbar, ContextCompat.getColor(getActivity(), R.color.md_yellow_500));
                    mSnackbar.setAction("重试", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fetchPrePostInfo(true);
                            mSnackbar.dismiss();
                        }
                    });
                    mSnackbar.show();
                }
            }
        }
    }

    private void fetchPrePostInfo(boolean showProgressNow) {
        if (!mFetchingInfo) {
            mFetchingInfo = true;
            mFetchInfoCount++;
            if (showProgressNow) {
                mProgressBar.showNow();
            } else {
                mProgressBar.show();
            }
            mPrePostAsyncTask = new PrePostAsyncTask(mPrePostListener, mMode, mSpecial);
            PostBean postBean = new PostBean();
            postBean.setTid(mTid);
            postBean.setPid(mPid);
            postBean.setFid(mFid);
            mPrePostAsyncTask.execute(postBean);
        }
    }

    private void setupPrePostInfo() {
        if (mPrePostInfo == null)
            return;

        //mSpecial = Utils.nullToText(mPrePostInfo.getSpecial());

        setHasOptionsMenu(true);
        getActivity().invalidateOptionsMenu();

        mTypeValues = mPrePostInfo.getTypeValues();
        mTypeId = mPrePostInfo.getTypeId();

        if (mTypeValues.size() > 0) {
            mTvType.setText(mTypeValues.get(mTypeId));
            mTvType.setTag(mTypeId);
            mTvType.setVisibility(View.VISIBLE);
            mTvType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showThreadTypesDialog();
                }
            });
        }

        mTopicValues = mPrePostInfo.getTopicValues();
        mTopic = mPrePostInfo.getTopic();
        if (mTopicValues.size() > 0) {
            mTvTopic.setText(mTopicValues.get(mTopic));
            mTvTopic.setTag(mTopic);
            mTvTopic.setVisibility(View.VISIBLE);
            mTvTopic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTopicDialog();
                }
            });
        }

        if ((mMode == PostHelper.MODE_EDIT_POST && mFloor == 1)
                || !TextUtils.isEmpty(mPrePostInfo.getSubject())) {
            mEtSubject.setText(EmojiParser.parseToUnicode(mPrePostInfo.getSubject()));
            mEtSubject.setVisibility(View.VISIBLE);
        }
        if (mMode == PostHelper.MODE_EDIT_POST) {
            mEtContent.setText(EmojiParser.parseToUnicode(mPrePostInfo.getText()));
        } else if (!TextUtils.isEmpty(mPrePostInfo.getQuoteText())) {
            mTvQuoteText.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
            UIUtils.setLineSpacing(mTvQuoteText);
            mTvQuoteText.setText(mPrePostInfo.getQuoteText());
            mTvQuoteText.setVisibility(View.VISIBLE);
            mTvQuoteText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UIUtils.showMessageDialog(getActivity(), mFloor + "# " + mFloorAuthor, mPrePostInfo.getQuoteText(), true);
                }
            });
        }

        String mReadPerm = mPrePostInfo.getReadPerm();
        Map<String, String> mReadPerms = mPrePostInfo.getReadPerms();
        if (mReadPerms != null && mReadPerms.size() > 0) {
            if (TextUtils.isEmpty(mReadPerm)) {
                mTvReadPerm.setText("权限");
            } else {
                mTvReadPerm.setText("权限(" + mReadPerm + ")");
            }
            mTvReadPerm.setTag(mReadPerm);
            mTvReadPerm.setVisibility(View.VISIBLE);
            mTvReadPerm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showReadPermDialog();
                }
            });
        }

        if (mPrePostInfo.getExtCredit() >= 0) {
            mTvCredit.setText("悬赏(" + mPrePostInfo.getExtCredit() + ")");
            mTvCredit.setVisibility(View.VISIBLE);
            mTvCredit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCreditDialog();
                }
            });
        }

        if (mEtItemName != null) {
            mEtItemName.setText(mPrePostInfo.getItemName());
            mEtItemLocus.setText(mPrePostInfo.getItemLocus());
            mEtItemPrice.setText(mPrePostInfo.getItemPrice());
        }

        if (PostHelper.SPECIAL_POLL.equals(mSpecial)) {
            mPollLayout.setVisibility(View.VISIBLE);
            mEtPollMaxChoices.setText(Utils.nullToText(mPrePostInfo.getPollMaxChoices()));
            mEtPollDays.setText(Utils.nullToText(mPrePostInfo.getPollDays()));
            mCbPollVisibility.setChecked(mPrePostInfo.isPollVisibility());
            mCbPollOvert.setChecked(mPrePostInfo.isPollOvert());
            mBtnPollAddChoice.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    addPollChoice("");
                }
            });
            if (mPollChoicesLayout.getChildCount() == 0) {
                if (mPrePostInfo.getPollChoices().size() > 0) {
                    for (String choice : mPrePostInfo.getPollChoices()) {
                        addPollChoice(choice);
                    }
                } else {
                    addPollChoice("");
                    addPollChoice("");
                    addPollChoice("");
                }
            }
        }

        //try to upload holded images when pre post info is ready
        if (mHoldedImages != null && mHoldedImages.size() > 0) {
            JobMgr.addJob(new ImageUploadJob(mSessionId, mPrePostInfo.getHash(), mHoldedImages.toArray(new Uri[mHoldedImages.size()])));
            mHoldedImages.clear();
        }
    }

    private void addPollChoice(String choice) {
        if (mPollChoicesLayout != null) {
            final ViewGroup choiceLayout = (ViewGroup) mInflater.inflate(R.layout.vw_post_poll_choice, null, false);
            choiceLayout.setPadding(16, 4, 16, 4);
            EditText etChoice = (EditText) choiceLayout.findViewById(R.id.et_poll_text);
            etChoice.setText(choice);
            ImageButton ibRemove = (ImageButton) choiceLayout.findViewById(R.id.ib_remove);
            ibRemove.setImageDrawable(mIbDrawable);
            ibRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPollChoicesLayout.removeView(choiceLayout);
                }
            });
            mPollChoicesLayout.addView(choiceLayout);
        }
    }

    public boolean isUserInputted() {
        return !TextUtils.isEmpty(mEtContent.getText())
                || !TextUtils.isEmpty(mEtSubject.getText())
                || mUploadImages.size() > 0;
    }

    @Override
    public boolean onBackPressed() {
        if (isUserInputted()) {
            Dialog dialog = new AlertDialog.Builder(getActivity())
                    .setTitle("放弃发表？")
                    .setMessage("\n确认放弃已输入的内容吗？\n")
                    .setPositiveButton(getResources().getString(android.R.string.ok),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    PostFragment.this.getActivity().finish();
                                }
                            })
                    .setNegativeButton(getResources().getString(android.R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).create();
            dialog.show();
            return true;
        }
        return false;
    }

    private void showAppendDeviceInfoDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("追加系统信息？");
        builder.setMessage("反馈问题时，提供系统信息可以帮助开发者更好的定位问题。\n\n" + Utils.getDeviceInfo());
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectionStart = 0;
                String deviceInfo = Utils.getDeviceInfo();
                if (mContentPosition < 0 || mContentPosition > mEtContent.length())
                    selectionStart = mEtContent.getSelectionStart();
                if (selectionStart > 0 && mEtContent.getText().charAt(selectionStart - 1) != '\n')
                    deviceInfo = "\n" + deviceInfo;
                mEtContent.getText().insert(selectionStart, deviceInfo);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showThreadTypesDialog() {
        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View viewlayout = inflater.inflate(R.layout.dialog_forum_types, null);

        final ListView listView = (ListView) viewlayout.findViewById(R.id.lv_forum_types);

        listView.setAdapter(new SimpleTypeAdapter(getActivity(), mTypeValues));

        final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
        popDialog.setView(viewlayout);
        final AlertDialog dialog = popDialog.create();
        dialog.show();

        listView.setOnItemClickListener(new OnViewItemSingleClickListener() {
            @Override
            public void onItemSingleClick(AdapterView<?> adapterView, View view, int position, long row) {
                mTypeId = mTypeValues.keySet().toArray()[position].toString();
                mTvType.setText(mTypeValues.get(mTypeId));
                dialog.dismiss();
            }
        });

    }

    private void showTopicDialog() {
        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View viewlayout = inflater.inflate(R.layout.dialog_forum_types, null);

        final ListView listView = (ListView) viewlayout.findViewById(R.id.lv_forum_types);

        listView.setAdapter(new SimpleTypeAdapter(getActivity(), mTopicValues));

        final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
        popDialog.setView(viewlayout);
        final AlertDialog dialog = popDialog.create();
        dialog.show();

        listView.setOnItemClickListener(new OnViewItemSingleClickListener() {
            @Override
            public void onItemSingleClick(AdapterView<?> adapterView, View view, int position, long row) {
                mTopic = mTopicValues.keySet().toArray()[position].toString();
                mTvTopic.setText(mTopicValues.get(mTopic));
                mEtSubject.setText(mTopic + mEtSubject.getText().toString());
                mEtSubject.setSelection(mEtSubject.getText().length());
                dialog.dismiss();
            }
        });

    }

    private void showReadPermDialog() {
        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View viewlayout = inflater.inflate(R.layout.dialog_forum_types, null);

        final ListView listView = (ListView) viewlayout.findViewById(R.id.lv_forum_types);

        listView.setAdapter(new SimpleTypeAdapter(getActivity(), mPrePostInfo.getReadPerms(), true));

        final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
        popDialog.setView(viewlayout);
        final AlertDialog dialog = popDialog.create();
        dialog.show();

        listView.setOnItemClickListener(new OnViewItemSingleClickListener() {
            @Override
            public void onItemSingleClick(AdapterView<?> adapterView, View view, int position, long row) {
                mPrePostInfo.setReadPerm(mPrePostInfo.getReadPerms().keySet().toArray()[position].toString());
                mTvReadPerm.setText("权限(" + mPrePostInfo.getReadPerm() + ")");
                dialog.dismiss();
            }
        });

    }

    private MaterialDialog mCreditDialog;

    private void showCreditDialog() {
        mCreditDialog = new MaterialDialog.Builder(getActivity())
                .title("自动悬赏")
                .customView(R.layout.dialog_credit, false)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        setCreditValues();
                    }
                })
                .show();

        initCreditValues();
    }

    private void initCreditValues() {
        if (mCreditDialog != null && mCreditDialog.isShowing()) {
            View view = mCreditDialog.getCustomView();
            TextView tvInfo = (TextView) view.findViewById(R.id.tv_info);
            EditText etExtCredit = (EditText) view.findViewById(R.id.et_ext_credit);
            EditText etCreditTimes = (EditText) view.findViewById(R.id.et_credit_times);
            EditText etCreditMemberTimes = (EditText) view.findViewById(R.id.et_credit_member_times);
            final Spinner spinner = (Spinner) view.findViewById(R.id.sp_credit_rate);

            etExtCredit.setText(String.valueOf(mPrePostInfo.getExtCredit()));
            etCreditTimes.setText(String.valueOf(mPrePostInfo.getCreditTimes()));
            etCreditMemberTimes.setText(String.valueOf(mPrePostInfo.getCreditMemberTimes()));

            int total = mPrePostInfo.getExtCredit() * mPrePostInfo.getCreditTimes();
            int totalTax = total == 0 ? 0 : total + (total / 100) + 1;

            tvInfo.setText("自动悬赏总额: " + total + " nb资产值, 税后支付 资产值 " + totalTax + " nb, 您有 资产值 " + mPrePostInfo.getCreditLeft() + " nb");

            etExtCredit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updateCreditValues();
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            etCreditTimes.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updateCreditValues();
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });


            String[] values = {"100", "90", "80", "70", "60", "50", "40", "30", "20", "10"};
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter(getActivity(), R.layout.spinner_row, values);
            spinner.setAdapter(spinnerAdapter);
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(String.valueOf(mPrePostInfo.getCreditRandom()))) {
                    spinner.setSelection(i);
                }
            }
        }
    }

    private void updateCreditValues() {
        if (mCreditDialog != null && mCreditDialog.isShowing()) {
            View view = mCreditDialog.getCustomView();
            TextView tvInfo = (TextView) view.findViewById(R.id.tv_info);
            EditText etExtCredit = (EditText) view.findViewById(R.id.et_ext_credit);
            EditText etCreditTimes = (EditText) view.findViewById(R.id.et_credit_times);

            int total = Utils.parseInt(etExtCredit.getText().toString()) * Utils.parseInt(etCreditTimes.getText().toString());
            int totalTax = total == 0 ? 0 : total + (total / 100) + 1;

            String s = "自动悬赏总额: " + total + " nb资产值, 税后支付 资产值 " + totalTax + " nb, 您有 资产值 " + mPrePostInfo.getCreditLeft() + " nb";
            if (totalTax <= mPrePostInfo.getCreditLeft()) {
                tvInfo.setText(s);
            } else {
                tvInfo.setText(HtmlCompat.fromHtml("<font color=red>" + s + "</font>"));
            }

        }
    }

    private void setCreditValues() {
        if (mCreditDialog != null && mCreditDialog.isShowing()) {
            View view = mCreditDialog.getCustomView();
            EditText etExtCredit = (EditText) view.findViewById(R.id.et_ext_credit);
            EditText etCreditTimes = (EditText) view.findViewById(R.id.et_credit_times);
            EditText etCreditMemberTimes = (EditText) view.findViewById(R.id.et_credit_member_times);
            Spinner spinner = (Spinner) view.findViewById(R.id.sp_credit_rate);

            int extCredit = Utils.parseInt(etExtCredit.getText().toString());
            int creditTimes = Utils.parseInt(etCreditTimes.getText().toString());
            int creditMemberTimes = Utils.parseInt(etCreditMemberTimes.getText().toString());
            int creditRandom = Utils.parseInt(spinner.getSelectedItem().toString());

            int total = extCredit * creditTimes;
            int totalTax = total == 0 ? 0 : total + (total / 100) + 1;

            if (totalTax > mPrePostInfo.getCreditLeft()) {
                UIUtils.toast("回帖奖励积分总额过大 (" + totalTax + ") nb");
            } else {
                mPrePostInfo.setExtCredit(extCredit);
                mPrePostInfo.setCreditTimes(creditTimes);
                mPrePostInfo.setCreditMemberTimes(creditMemberTimes);
                mPrePostInfo.setCreditRandom(creditRandom);
                mTvCredit.setText("悬赏(" + mPrePostInfo.getExtCredit() + ")");
            }
        }
    }

    private void showImagesDialog() {
        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View viewlayout = inflater.inflate(R.layout.dialog_images, null);

        final GridView gridView = (GridView) viewlayout.findViewById(R.id.gv_images);

        gridView.setAdapter(mImageAdapter);

        final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
        popDialog.setView(viewlayout);
        final AlertDialog dialog = popDialog.create();
        dialog.show();

        gridView.setOnItemClickListener(new OnViewItemSingleClickListener() {
            @Override
            public void onItemSingleClick(AdapterView<?> adapterView, View view, int position, long row) {
                if (view.getTag() != null)
                    appendImage(view.getTag().toString());
                dialog.dismiss();
            }
        });

    }

    private void imageProcess(int total, int current, int percentage) {
        mProgressDialog.setMessage("正在上传... (" + (current + 1) + "/" + total + ")");
    }

    private void imageDone(ImageUploadEvent event) {
        UploadImage image = event.mImage;
        if (HiUtils.isValidId(image.getImgId())) {
            mUploadImages.put(image.getUri(), image);
            mEmojiPopup.addImage(image.getImgId(), image.getThumb());
            appendImage(image.getImgId());
        } else {
            mUploadImages.remove(image.getUri());
            UIUtils.errorSnack(getView(), "图片上传失败：" + Utils.nullToText(event.message), event.mDetail);
        }
        updateImageInfo();
    }

    private void imageAllDone() {
        mImageUploading = false;
        mProgressDialog.dismiss();
        updateImageInfo();
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(ImageUploadEvent event) {
        if (!mSessionId.equals(event.mSessionId))
            return;

        Collection<ImageUploadEvent> events = new ArrayList<>();
        if (event.holdEvents != null && event.holdEvents.size() > 0) {
            events.addAll(event.holdEvents);
        }
        events.add(event);

        for (ImageUploadEvent evt : events) {
            if (evt.type == ImageUploadEvent.UPLOADING) {
                imageProcess(evt.total, evt.current, evt.percentage);
            } else if (evt.type == ImageUploadEvent.ITEM_DONE) {
                imageDone(evt);
            } else if (evt.type == ImageUploadEvent.ALL_DONE) {
                imageAllDone();
            }
        }
        EventBus.getDefault().removeStickyEvent(event);
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(PostEvent event) {
        PostBean postResult = event.mPostResult;

        if (!mSessionId.equals(event.mSessionId))
            return;

        EventBus.getDefault().removeStickyEvent(event);
        String message = event.mMessage;

        if (event.mStatus == Constants.STATUS_IN_PROGRESS) {
            mProgressDialog = HiProgressDialog.show(getActivity(), "正在发表...");
        } else if (event.mStatus == Constants.STATUS_SUCCESS) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
            if (message.contains("审核"))
                UIUtils.toast(message);

            //re-post to parent
            event.mSessionId = mParentSessionId;
            EventBus.getDefault().postSticky(event);

            ((BaseActivity) getActivity()).finishWithNoSlide();
        } else {
            if (mProgressDialog != null) {
                mProgressDialog.dismissError(message);
            } else {
                UIUtils.toast(message);
            }
        }
    }

    private class SavedContentsAdapter extends ArrayAdapter {
        Content[] contents;

        public SavedContentsAdapter(Context context, int resource, Content[] objects) {
            super(context, resource, objects);
            contents = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row;
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.item_saved_content, parent, false);
            } else {
                row = convertView;
            }
            Content content = contents[position];

            TextView tvContent = (TextView) row.findViewById(R.id.tv_content);
            TextView tvDesc = (TextView) row.findViewById(R.id.tv_desc);

            tvContent.setText(content.getContent().replace("\n", " "));
            tvDesc.setText(content.getDesc());

            return row;
        }
    }

}
