package com.greenskinmonster.a51nb.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;

import com.greenskinmonster.a51nb.R;
import com.greenskinmonster.a51nb.async.PostSmsAsyncTask;
import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.bean.SimpleListBean;
import com.greenskinmonster.a51nb.bean.SimpleListItemBean;
import com.greenskinmonster.a51nb.job.EventCallback;
import com.greenskinmonster.a51nb.job.JobMgr;
import com.greenskinmonster.a51nb.job.SimpleListEvent;
import com.greenskinmonster.a51nb.job.SimpleListJob;
import com.greenskinmonster.a51nb.job.SmsRefreshEvent;
import com.greenskinmonster.a51nb.okhttp.OkHttpHelper;
import com.greenskinmonster.a51nb.okhttp.ParamsMap;
import com.greenskinmonster.a51nb.parser.HiParser;
import com.greenskinmonster.a51nb.ui.adapter.RecyclerItemClickListener;
import com.greenskinmonster.a51nb.ui.adapter.SmsAdapter;
import com.greenskinmonster.a51nb.ui.widget.ContentLoadingView;
import com.greenskinmonster.a51nb.ui.widget.CountdownButton;
import com.greenskinmonster.a51nb.ui.widget.HiProgressDialog;
import com.greenskinmonster.a51nb.ui.widget.OnSingleClickListener;
import com.greenskinmonster.a51nb.ui.widget.SimplePopupMenu;
import com.greenskinmonster.a51nb.ui.widget.XRecyclerView;
import com.greenskinmonster.a51nb.utils.Constants;
import com.greenskinmonster.a51nb.utils.HiUtils;
import com.greenskinmonster.a51nb.utils.HtmlCompat;
import com.greenskinmonster.a51nb.utils.Logger;
import com.greenskinmonster.a51nb.utils.UIUtils;
import com.greenskinmonster.a51nb.utils.Utils;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.vanniktech.emoji.EmojiEditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Request;

public class SmsFragment extends BaseFragment implements PostSmsAsyncTask.SmsPostListener {

    public static final String ARG_AUTHOR = "AUTHOR";
    public static final String ARG_UID = "UID";

    private String mAuthor;
    private String mUid;
    private String mPmid;
    private String mFormhash;
    private SmsAdapter mSmsAdapter;
    private List<SimpleListItemBean> mSmsBeans = new ArrayList<>();
    private XRecyclerView mRecyclerView;

    private EmojiEditText mEtSms;
    private ImageButton mIbEmojiSwitch;
    private CountdownButton mCountdownButton;

    private ContentLoadingView mLoadingView;
    private SmsEventCallback mEventCallback = new SmsEventCallback();
    private boolean mSending = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (getArguments().containsKey(ARG_AUTHOR)) {
            mAuthor = getArguments().getString(ARG_AUTHOR);
        }
        if (getArguments().containsKey(ARG_UID)) {
            mUid = getArguments().getString(ARG_UID);
        }

        RecyclerItemClickListener itemClickListener = new RecyclerItemClickListener(getActivity(), new OnItemClickListener());
        mSmsAdapter = new SmsAdapter(this, new AvatarOnClickListener(), itemClickListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sms, container, false);
        mRecyclerView = (XRecyclerView) view.findViewById(R.id.rv_sms);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mLoadingView = (ContentLoadingView) view.findViewById(R.id.content_loading);
        mLoadingView.setErrorStateListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLoadingView.setState(ContentLoadingView.LOAD_NOW);
                Bundle bundle = new Bundle();
                bundle.putString("extra", mUid);
                SimpleListJob job = new SimpleListJob(getActivity(), mSessionId, SimpleListJob.TYPE_SMS_DETAIL, 1, bundle);
                JobMgr.addJob(job);
            }
        });

        mCountdownButton = (CountdownButton) view.findViewById(R.id.countdown_button);
        mCountdownButton.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_send).sizeDp(28).color(Color.GRAY));

        mEtSms = (EmojiEditText) view.findViewById(R.id.et_sms);
        mEtSms.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
        mCountdownButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                String replyText = mEtSms.getText().toString();
                if (replyText.length() > 0) {
                    sendSms(replyText);
                }
            }
        });

        mIbEmojiSwitch = (ImageButton) view.findViewById(R.id.ib_emoji_switch);
        setUpEmojiPopup(mEtSms, mIbEmojiSwitch);

        showSendButton();

        return view;
    }

    private void sendSms(String replyText) {
        mSending = true;
        new PostSmsAsyncTask(getActivity(), mFormhash, mPmid, mUid, null, SmsFragment.this, null).execute(replyText);
        mEtSms.setText("");
        mCountdownButton.setEnabled(false);
        SimpleListItemBean bean = new SimpleListItemBean();
        bean.setAuthor(HiSettingsHelper.getInstance().getUsername());
        bean.setAuthorId(HiSettingsHelper.getInstance().getUid());
        bean.setNew(false);
        bean.setCreateTime(Utils.formatDate(new Date(), "yyyy-M-d HH:mm"));
        bean.setInfo(replyText);
        bean.setStatus(Constants.STATUS_IN_PROGRESS);
        mSmsAdapter.getDatas().add(bean);
        mSmsAdapter.notifyItemInserted(mSmsAdapter.getItemCount() - 1);
    }

    private void removeFailedSms(int position) {
        if (position >= 0 && position < mSmsAdapter.getItemCount()) {
            mSmsAdapter.getDatas().remove(position);
            mSmsAdapter.notifyItemRemoved(position);
            mSmsAdapter.notifyItemRangeChanged(position, mSmsAdapter.getItemCount());
        }
    }

    private void markNewSmsFailed() {
        SimpleListItemBean bean = mSmsAdapter.getDatas().get(mSmsAdapter.getItemCount() - 1);
        if (bean.getStatus() == Constants.STATUS_IN_PROGRESS) {
            bean.setStatus(Constants.STATUS_FAIL);
            bean.setCreateTime("发送失败");
            mSmsAdapter.notifyItemChanged(mSmsAdapter.getItemCount() - 1);
        }
    }

    private void deleteSingleSms(SimpleListItemBean bean, final int position) {
        String url = HiUtils.DeleteSingleSMS
                .replace("{pmid}", bean.getPmid())
                .replace("{uid}", mUid)
                .replace("{formhash}", mFormhash);
        ParamsMap params = new ParamsMap();
        params.put("op", "delete");
        params.put("deletepm_deluid[]", mUid);
        params.put("uid", mUid);
        params.put("infloat", "yes");
        params.put("inajax", "1");
        params.put("ajaxtarget", "fwin_content_a_pmdelete_0");

        OkHttpHelper.getInstance().asyncPost(url, params, new OkHttpHelper.ResultCallback() {
            @Override
            public void onError(Request request, Exception e) {
                UIUtils.toast(OkHttpHelper.getErrorMessage(e).getMessage());
            }

            @Override
            public void onResponse(String response) {
                if (response.contains("succeedhandle")) {
                    removeFailedSms(position);
                    if (mSmsAdapter.getDatas().size() == 0) {
                        EventBus.getDefault().postSticky(new SmsRefreshEvent());
                        ((BaseActivity) getActivity()).finishWithNoSlide();
                    }
                } else {
                    UIUtils.toast("发生错误");
                }
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRecyclerView.setAdapter(mSmsAdapter);

        if (mSmsBeans.size() == 0) {
            mLoadingView.setState(ContentLoadingView.LOADING);
            Bundle bundle = new Bundle();
            bundle.putString("extra", mUid);
            SimpleListJob job = new SimpleListJob(getActivity(), mSessionId, SimpleListJob.TYPE_SMS_DETAIL, 1, bundle);
            JobMgr.addJob(job);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        inflater.inflate(R.menu.menu_sms_detail, menu);
        menu.findItem(R.id.action_clear_sms)
                .setIcon(new IconicsDrawable(getActivity(), FontAwesome.Icon.faw_trash).actionBar()
                        .color(HiSettingsHelper.getInstance().getToolbarTextColor()));

        setActionBarTitle(mAuthor);
        mEtSms.setHint(getString(R.string.txt_quick_hint) + " · " + mAuthor);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Implemented in activity
                return false;
            case R.id.action_clear_sms:
                showClearSmsDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showClearSmsDialog() {
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
        popDialog.setTitle("清空悄悄话？");
        popDialog.setMessage(HtmlCompat.fromHtml("确认清空所有与用户 <b>" + mAuthor + "</b> 的悄悄话？<br><br><font color=red>注意：此操作不可恢复。</font>"));
        popDialog.setPositiveButton("清空",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final HiProgressDialog progress = HiProgressDialog.show(getActivity(), "正在处理...");

                        String url = HiUtils.DeleteAllSMS;
                        ParamsMap params = new ParamsMap();
                        params.put("deletepm_deluid[]", mUid);
                        params.put("deletepmsubmit_btn", "true");
                        params.put("deletesubmit", "true");
                        params.put("formhash", mFormhash);

                        OkHttpHelper.getInstance().asyncPost(url, params, new OkHttpHelper.ResultCallback() {
                            @Override
                            public void onError(Request request, Exception e) {
                                progress.dismissError("操作时发生错误 : " + OkHttpHelper.getErrorMessage(e));
                                EventBus.getDefault().postSticky(new SmsRefreshEvent());
                                ((BaseActivity) getActivity()).finishWithNoSlide();
                            }

                            @Override
                            public void onResponse(String response) {
                                String errorMessage;
                                try {
                                    errorMessage = HiParser.parseErrorMessage(Jsoup.parse(response));
                                } catch (Exception e) {
                                    Logger.e(e);
                                    errorMessage = OkHttpHelper.getErrorMessage(e).getMessage();
                                }
                                if (TextUtils.isEmpty(errorMessage)) {
                                    progress.dismiss("操作完成");
                                } else {
                                    progress.dismissError(errorMessage);
                                }
                                EventBus.getDefault().postSticky(new SmsRefreshEvent());
                                ((BaseActivity) getActivity()).finishWithNoSlide();
                            }
                        });

                    }
                });
        popDialog.setIcon(new IconicsDrawable(getActivity(), FontAwesome.Icon.faw_exclamation_circle).sizeDp(24).color(Color.RED));
        popDialog.setNegativeButton("取消", null);
        popDialog.create().show();
    }

    @Override
    public void onSmsPrePost() {
        mRecyclerView.scrollToBottom();
    }

    @Override
    public void onSmsPostDone(int status, final String message, AlertDialog dialog) {
        if (status == Constants.STATUS_SUCCESS) {
            mSending = false;

            showSendButton();

            mCountdownButton.setEnabled(true);
            //new sms has some delay, so this is a dirty hack
            new CountDownTimer(1000, 1000) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    try {
                        Bundle bundle = new Bundle();
                        bundle.putString("extra", mUid);
                        SimpleListJob job = new SimpleListJob(getActivity(), mSessionId, SimpleListJob.TYPE_SMS_DETAIL, 1, bundle);
                        JobMgr.addJob(job);
                    } catch (Exception ignored) {
                    }
                }
            }.start();
        } else {
            mSending = false;
            mCountdownButton.setEnabled(true);
            markNewSmsFailed();
            UIUtils.toast(message);
        }
    }

    private void showSendButton() {
        mCountdownButton.setCountdown(PostSmsAsyncTask.getWaitTimeToSendSms());
    }

    private class AvatarOnClickListener extends OnSingleClickListener {
        @Override
        public void onSingleClick(View view) {
            String uid = (String) view.getTag(R.id.avatar_tag_uid);
            String username = (String) view.getTag(R.id.avatar_tag_username);

            FragmentUtils.showUserInfoActivity(getActivity(), false, uid, username);
        }
    }

    private class OnItemClickListener implements RecyclerItemClickListener.OnItemClickListener {

        @Override
        public void onItemClick(View view, int position) {
        }

        @Override
        public void onLongItemClick(View view, int position) {
            final SimpleListItemBean bean = mSmsAdapter.getItem(position);
            final int rvPosition = position;
            if (bean != null) {
                SimplePopupMenu popupMenu = new SimplePopupMenu(getActivity());
                popupMenu.add("copy", "复制内容", new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        CharSequence content = Utils.fromHtmlAndStrip(bean.getInfo());
                        if (content.length() > 0) {
                            ClipData clip = ClipData.newPlainText("SMS CONTENT", content);
                            clipboard.setPrimaryClip(clip);
                            UIUtils.toast("内容已复制");
                        } else {
                            UIUtils.toast("内容为空");
                        }
                    }
                });
                popupMenu.add("delete", "删除此条", new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        deleteSingleSms(bean, rvPosition);
                    }
                });
                if (!mSending && bean.getStatus() == Constants.STATUS_FAIL && PostSmsAsyncTask.getWaitTimeToSendSms() <= 0) {
                    popupMenu.add("resend", "重新发送", new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if (!mSending && PostSmsAsyncTask.getWaitTimeToSendSms() <= 0) {
                                String content = bean.getInfo();
                                removeFailedSms(rvPosition);
                                sendSms(content);
                            } else {
                                UIUtils.toast("悄悄话发送时间限制");
                            }
                        }
                    });
                }
                popupMenu.show();
            }
        }

        @Override
        public void onDoubleTap(View view, int position) {
        }
    }

    @Override
    public void scrollToTop() {
        if (mSmsAdapter != null && mSmsAdapter.getItemCount() > 0)
            mRecyclerView.scrollToPosition(0);
    }

    private class SmsEventCallback extends EventCallback<SimpleListEvent> {
        @Override
        public void onSuccess(SimpleListEvent event) {

            SimpleListBean list = event.mData;
            if (list == null || list.getCount() == 0) {
                mLoadingView.setState(ContentLoadingView.NO_DATA);
            } else {
                mPmid = list.getPmid();
                mFormhash = list.getFormhash();
                mLoadingView.setState(ContentLoadingView.CONTENT);
                mSmsBeans.clear();
                mSmsBeans.addAll(list.getAll());
                mSmsAdapter.setDatas(mSmsBeans);
                mRecyclerView.scrollToBottom();
            }
        }

        @Override
        public void onFail(SimpleListEvent event) {
            mLoadingView.setState(ContentLoadingView.ERROR);
            UIUtils.toast(event.mMessage);
        }

        @Override
        public void onFailRelogin(SimpleListEvent event) {
            mSmsBeans.clear();
            mSmsAdapter.notifyDataSetChanged();
            mLoadingView.setState(ContentLoadingView.NOT_LOGIN);
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(SimpleListEvent event) {
        if (!mSessionId.equals(event.mSessionId))
            return;
        EventBus.getDefault().removeStickyEvent(event);
        mEventCallback.process(event);
    }
}
