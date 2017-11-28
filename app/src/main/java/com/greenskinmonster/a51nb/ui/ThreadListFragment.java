package com.greenskinmonster.a51nb.ui;


import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.greenskinmonster.a51nb.R;
import com.greenskinmonster.a51nb.async.LoginHelper;
import com.greenskinmonster.a51nb.async.NetworkReadyEvent;
import com.greenskinmonster.a51nb.async.PostHelper;
import com.greenskinmonster.a51nb.async.QianDaoHelper;
import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.bean.NotificationBean;
import com.greenskinmonster.a51nb.bean.PostBean;
import com.greenskinmonster.a51nb.bean.ThreadBean;
import com.greenskinmonster.a51nb.bean.ThreadListBean;
import com.greenskinmonster.a51nb.db.HistoryDao;
import com.greenskinmonster.a51nb.job.EventCallback;
import com.greenskinmonster.a51nb.job.JobMgr;
import com.greenskinmonster.a51nb.job.PostEvent;
import com.greenskinmonster.a51nb.job.SimpleListJob;
import com.greenskinmonster.a51nb.job.ThreadListEvent;
import com.greenskinmonster.a51nb.job.ThreadListJob;
import com.greenskinmonster.a51nb.service.NotiHelper;
import com.greenskinmonster.a51nb.ui.adapter.RecyclerItemClickListener;
import com.greenskinmonster.a51nb.ui.adapter.ThreadListAdapter;
import com.greenskinmonster.a51nb.ui.widget.BottomDialog;
import com.greenskinmonster.a51nb.ui.widget.ContentLoadingView;
import com.greenskinmonster.a51nb.ui.widget.FABHideOnScrollBehavior;
import com.greenskinmonster.a51nb.ui.widget.OnViewItemSingleClickListener;
import com.greenskinmonster.a51nb.ui.widget.SimpleDivider;
import com.greenskinmonster.a51nb.ui.widget.ValueChagerView;
import com.greenskinmonster.a51nb.ui.widget.XFooterView;
import com.greenskinmonster.a51nb.ui.widget.XRecyclerView;
import com.greenskinmonster.a51nb.utils.ColorHelper;
import com.greenskinmonster.a51nb.utils.Constants;
import com.greenskinmonster.a51nb.utils.HiUtils;
import com.greenskinmonster.a51nb.utils.UIUtils;
import com.greenskinmonster.a51nb.utils.Utils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ThreadListFragment extends BaseFragment
        implements SwipeRefreshLayout.OnRefreshListener {

    public static final String ARG_FID_KEY = "fid";

    private Context mCtx;
    private int mForumId = 0;
    private int mPage = 1;
    private String mTypeId;
    private String mOrderBy = ThreadListJob.ORDER_BY_REPLY;
    private Map<String, String> mTypes;

    private ThreadListAdapter mThreadListAdapter;
    private List<ThreadBean> mThreadBeans = new ArrayList<>();
    private XRecyclerView mRecyclerView;

    private boolean mInloading = false;
    private SwipeRefreshLayout swipeLayout;
    private ContentLoadingView mLoadingView;

    private int mFirstVisibleItem = 0;
    private boolean mDataReceived = false;
    private String mFormhash;

    private MenuItem mForumTypeMenuItem;
    private MenuItem mOrderByMenuItem;

    private ThreadListEventCallback mEventCallback = new ThreadListEventCallback();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCtx = getActivity();

        if (getArguments() != null && getArguments().containsKey(ARG_FID_KEY)) {
            mForumId = getArguments().getInt(ARG_FID_KEY);
        }
        if (!HiUtils.isForumValid(mForumId)) {
            if (HiSettingsHelper.getInstance().getFreqForums().size() > 0) {
                mForumId = HiSettingsHelper.getInstance().getFreqForums().get(0).getId();
            } else {
                mForumId = HiUtils.FID_THINPAD;
            }
        }

        HiSettingsHelper.getInstance().setLastForumId(mForumId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_thread_list, parent, false);
        mRecyclerView = (XRecyclerView) view.findViewById(R.id.rv_threads);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mCtx));
        mRecyclerView.addItemDecoration(new SimpleDivider(getActivity()));

        RecyclerItemClickListener itemClickListener = new RecyclerItemClickListener(mCtx, new OnItemClickListener());

        mThreadListAdapter = new ThreadListAdapter(Glide.with(this), itemClickListener);
        mThreadListAdapter.setDatas(mThreadBeans);

        mRecyclerView.setAdapter(mThreadListAdapter);
        mRecyclerView.addOnScrollListener(new OnScrollListener());

        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(ColorHelper.getSwipeColor(getActivity()));
        swipeLayout.setProgressBackgroundColorSchemeColor(ColorHelper.getSwipeBackgroundColor(getActivity()));

        mLoadingView = (ContentLoadingView) view.findViewById(R.id.content_loading);
        mLoadingView.setErrorStateListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mInloading) {
                    mInloading = true;
                    mLoadingView.setState(ContentLoadingView.LOAD_NOW);
                    refresh();
                }
            }
        });

        mLoadingView.setNotLoginStateListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainFrameActivity) getActivity()).showLoginDialog();
            }
        });

        mRecyclerView.scrollToPosition(mFirstVisibleItem);

        setActionBarTitle(HiUtils.getForumNameByFid(mForumId));
        if (getActivity() instanceof MainFrameActivity) {
            ((MainFrameActivity) getActivity()).setActionBarDisplayHomeAsUpEnabled(false);
            ((MainFrameActivity) getActivity()).syncActionBarState();
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mCtx = getActivity();
        }
        startLoading();
    }

    @Override
    public void onResume() {
        super.onResume();
        startLoading();
    }

    private void startLoading() {
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        if (!mInloading) {
            if (mThreadBeans.size() == 0) {
                mLoadingView.setState(ContentLoadingView.LOAD_NOW);
                mInloading = true;
                startJob();
            } else {
                swipeLayout.setRefreshing(false);
                mLoadingView.setState(ContentLoadingView.CONTENT);
                hideFooter();
            }
        }
        if (getActivity() != null && getActivity() instanceof MainFrameActivity) {
            ((MainFrameActivity) getActivity()).setDrawerSelection(mForumId);
        }
        if (LoginHelper.isLoggedIn()) {
            showNotification();
        }
    }


    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_thread_list, menu);
        mForumTypeMenuItem = menu.findItem(R.id.action_filter_by_type);
        mOrderByMenuItem = menu.findItem(R.id.action_order_by);
        MenuItem showStickItem = menu.findItem(R.id.action_show_stick_threads);
        showStickItem.setChecked(HiSettingsHelper.getInstance().isShowStickThreads());
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        mOrderByMenuItem.setChecked(ThreadListJob.ORDER_BY_THREAD.equals(mOrderBy));
        menu.findItem(R.id.action_new_thread).setEnabled(LoginHelper.isLoggedIn());
        menu.findItem(R.id.action_new_poll_thread).setEnabled(LoginHelper.isLoggedIn());
        if (LoginHelper.isLoggedIn() && (mForumId == HiUtils.FID_TRADE || mForumId == HiUtils.FID_TEST)) {
            menu.findItem(R.id.action_new_trade_thread).setVisible(true);
        }
        menu.findItem(R.id.action_new_poll_thread).setVisible(mForumId != HiUtils.FID_TRADE);
        menu.findItem(R.id.action_new_thread).setVisible(mForumId != HiUtils.FID_TRADE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_thread_list_settings:
                showThreadListSettingsDialog();
                return true;
            case R.id.action_new_thread:
                FragmentUtils.showNewPostActivity(getActivity(), mForumId, PostHelper.SPECIAL_NORM, mSessionId);
                return true;
            case R.id.action_new_poll_thread:
                FragmentUtils.showNewPostActivity(getActivity(), mForumId, PostHelper.SPECIAL_POLL, mSessionId);
                return true;
            case R.id.action_new_trade_thread:
                FragmentUtils.showNewPostActivity(getActivity(), mForumId, PostHelper.SPECIAL_TRADE, mSessionId);
                return true;
            case R.id.action_filter_by_type:
                showForumTypesDialog();
                return true;
            case R.id.action_order_by:
                if (!mInloading) {
                    item.setChecked(!item.isChecked());
                    mOrderBy = item.isChecked() ? ThreadListJob.ORDER_BY_THREAD : ThreadListJob.ORDER_BY_REPLY;
                    mLoadingView.setState(ContentLoadingView.LOAD_NOW);
                    startJob();
                    setActionBarSubtitle();
                }
                return true;
            case R.id.action_show_stick_threads:
                item.setChecked(!item.isChecked());
                HiSettingsHelper.getInstance().setShowStickThreads(item.isChecked());
                mLoadingView.setState(ContentLoadingView.LOAD_NOW);
                refresh();
                return true;
//            case R.id.action_open_by_url:
//                showOpenUrlDialog();
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    void setupFab() {
        if (mMainFab != null) {
            mMainFab.setImageResource(R.drawable.ic_refresh_white_24dp);
            mMainFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mLoadingView.setState(ContentLoadingView.LOAD_NOW);
                    if (swipeLayout.isShown())
                        swipeLayout.setRefreshing(false);
                    refresh();
                }
            });
            if (mThreadBeans.size() > 0)
                mMainFab.show();
        }

        if (mNotificationFab != null) {
            mNotificationFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NotificationBean bean = NotiHelper.getCurrentNotification();
                    if (bean.getSmsCount() == 1
                            && bean.getThreadCount() == 0
                            && HiUtils.isValidId(bean.getUid())
                            && !TextUtils.isEmpty(bean.getUsername())) {
                        FragmentUtils.showSmsActivity(getActivity(), false, bean.getUid(), bean.getUsername());
                        mNotificationFab.hide();
                    } else if (bean.getSmsCount() > 0 || bean.isHasSms()) {
                        FragmentUtils.showSimpleListActivity(getActivity(), false, SimpleListJob.TYPE_SMS);
                    } else if (bean.getTotalNotiCount() > 0) {
                        FragmentUtils.showNotifyListActivity(getActivity(), false, SimpleListJob.TYPE_THREAD_NOTIFY, SimpleListJob.NOTIFY_UNREAD);
                        mNotificationFab.hide();
                    } else if (bean.isQiandao()) {
                        QianDaoHelper.qiandao(mFormhash);
                        bean.setQiandao(false);
                        showNotification();
                    } else {
                        UIUtils.toast("没有未处理的通知");
                        mNotificationFab.hide();
                    }
                }
            });
        }
    }

    private void refresh() {
        mPage = 1;
        mRecyclerView.scrollToTop();
        hideFooter();
        mInloading = true;
        if (HiSettingsHelper.getInstance().isFabAutoHide() && mMainFab != null) {
            FABHideOnScrollBehavior.hideFab(mMainFab);
        }
        startJob();
    }

    private void startJob() {
        ThreadListJob job = new ThreadListJob(getActivity(), mSessionId, mForumId, mPage, mTypeId, mOrderBy);
        JobMgr.addJob(job);
    }

    @Override
    public void onRefresh() {
        refresh();
        if (mThreadBeans.size() > 0)
            mLoadingView.setState(ContentLoadingView.CONTENT);
    }

    public void notifyDataSetChanged() {
        if (mThreadListAdapter != null)
            mThreadListAdapter.notifyDataSetChanged();
    }

    private class OnScrollListener extends RecyclerView.OnScrollListener {
        int visibleItemCount, totalItemCount;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (dy > 0) {
                LinearLayoutManager mLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                visibleItemCount = mLayoutManager.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                mFirstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                if ((visibleItemCount + mFirstVisibleItem) >= totalItemCount - 5) {
                    if (!mInloading) {
                        mPage++;
                        mInloading = true;
                        mRecyclerView.setFooterState(XFooterView.STATE_LOADING);
                        startJob();
                    }
                }
            }
        }
    }

    private class OnItemClickListener implements RecyclerItemClickListener.OnItemClickListener {

        @Override
        public void onItemClick(View view, int position) {
            ThreadBean thread = mThreadListAdapter.getItem(position);
            if (thread != null) {
                String tid = thread.getTid();
                String title = thread.getTitle();
                FragmentUtils.showThreadActivity(getActivity(), false, tid, title, -1, -1, null, thread.getMaxPage());
                HistoryDao.saveHistoryInBackground(tid, mForumId + "",
                        title, thread.getAuthorId(), thread.getAuthor(), thread.getCreateTime());
            }
        }

        @Override
        public void onLongItemClick(View view, int position) {
            ThreadBean thread = mThreadListAdapter.getItem(position);
            if (thread != null) {
                String tid = thread.getTid();
                String title = thread.getTitle();
                FragmentUtils.showThreadActivity(getActivity(), false, tid, title, thread.getMaxPage(), ThreadDetailFragment.LAST_FLOOR, null, thread.getMaxPage());
                HistoryDao.saveHistoryInBackground(tid, "", title, thread.getAuthorId(), thread.getAuthor(), thread.getCreateTime());
            }
        }

        @Override
        public void onDoubleTap(View view, int position) {
        }
    }

    private void hideFooter() {
        mRecyclerView.setFooterState(XFooterView.STATE_HIDDEN);
    }

    private void showThreadListSettingsDialog() {
        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.dialog_thread_list_settings, null);

        final ValueChagerView valueChagerView = (ValueChagerView) view.findViewById(R.id.value_changer);

        valueChagerView.setCurrentValue(HiSettingsHelper.getInstance().getTitleTextSizeAdj());

        final BottomSheetDialog dialog = new BottomDialog(getActivity());

        valueChagerView.setOnChangeListener(new ValueChagerView.OnChangeListener() {
            @Override
            public void onChange(int currentValue) {
                HiSettingsHelper.getInstance().setTitleTextSizeAdj(currentValue);
                if (mThreadListAdapter != null)
                    mThreadListAdapter.notifyDataSetChanged();
            }
        });

        dialog.setContentView(view);
        BottomSheetBehavior mBehavior = BottomSheetBehavior.from((View) view.getParent());
        mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        dialog.show();
    }

    private void showForumTypesDialog() {
        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View viewlayout = inflater.inflate(R.layout.dialog_forum_types, null);

        final ListView listView = (ListView) viewlayout.findViewById(R.id.lv_forum_types);

        listView.setAdapter(new ForumTypesAdapter(getActivity()));

        final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
        popDialog.setView(viewlayout);
        final AlertDialog dialog = popDialog.create();
        dialog.show();

        listView.setOnItemClickListener(new OnViewItemSingleClickListener() {
            @Override
            public void onItemSingleClick(AdapterView<?> adapterView, View view, int position, long row) {
                dialog.dismiss();
                String key = mTypes.keySet().toArray(new String[mTypes.size()])[position];
                if (mTypes != null && mTypes.get(key) != null && !mTypes.get(key).equals(mTypeId)) {
                    mTypeId = key;
                    mLoadingView.setState(ContentLoadingView.LOAD_NOW);
                    refresh();
                    setActionBarSubtitle();
                }
            }
        });
    }

    private void setActionBarSubtitle() {
        if (!TextUtils.isEmpty(mOrderBy) && !TextUtils.isEmpty(mTypeId)) {
            setActionBarSubtitle(mTypes.get(mTypeId) + " · " + getString(R.string.action_order_by_thread));
        } else if (!TextUtils.isEmpty(mOrderBy)) {
            setActionBarSubtitle(getString(R.string.action_order_by_thread));
        } else if (!TextUtils.isEmpty(mTypeId)) {
            setActionBarSubtitle(mTypes.get(mTypeId));
        } else {
            setActionBarSubtitle("");
        }
    }

    private void showOpenUrlDialog() {
        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View viewlayout = inflater.inflate(R.layout.dialog_open_by_url, null);

        String urlFromClip = HiUtils.ThreadListUrl;
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard.hasPrimaryClip()) {
            String text = Utils.nullToText(clipboard.getPrimaryClip().getItemAt(0).getText()).replace("\n", "").trim();
            if (FragmentUtils.parseUrl(text) != null)
                urlFromClip = text;
        }

        final EditText etUrl = (EditText) viewlayout.findViewById(R.id.et_url);
        etUrl.setText(urlFromClip);
        etUrl.selectAll();
        etUrl.requestFocus();

        final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
        popDialog.setTitle(mCtx.getResources().getString(R.string.action_open_by_url));
        popDialog.setView(viewlayout);
        popDialog.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FragmentUtils.show(getActivity(), FragmentUtils.parseUrl(Utils.nullToText(etUrl.getText()).replace("\n", "").trim()));
            }
        });

        final AlertDialog dialog = popDialog.create();

        etUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String url = Utils.nullToText(etUrl.getText()).replace("\n", "").trim();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(FragmentUtils.parseUrl(url) != null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        dialog.show();

        String url = Utils.nullToText(etUrl.getText()).replace("\n", "").trim();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(FragmentUtils.parseUrl(url) != null);
    }

    public void showNotification() {
        if (mNotificationFab != null) {
            NotificationBean bean = NotiHelper.getCurrentNotification();
            if (bean.isHasSms() || bean.getSmsCount() > 0) {
                mNotificationFab.setImageResource(R.drawable.ic_mail_white_24dp);
                mNotificationFab.show();
            } else if (bean.getTotalNotiCount() > 0) {
                mNotificationFab.setImageResource(R.drawable.ic_notifications_white_24dp);
                mNotificationFab.show();
            } else if (bean.isQiandao()) {
                mNotificationFab.setImageResource(R.drawable.ic_add_location_white_24dp);
                mNotificationFab.show();
            } else {
                mNotificationFab.hide();
            }
            ((MainFrameActivity) getActivity()).updateDrawerBadge();
        }
    }

    public void scrollToTop() {
        mRecyclerView.scrollToTop();
    }

    public void stopScroll() {
        mRecyclerView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0));
    }

    private class ForumTypesAdapter extends ArrayAdapter {

        public ForumTypesAdapter(Context context) {
            super(context, 0, mTypes.keySet().toArray(new String[mTypes.size()]));
        }

        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View row;
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.item_forum_type, parent, false);
            } else {
                row = convertView;
            }
            TextView text = (TextView) row.findViewById(R.id.forum_type_text);

            String key = getItem(position).toString();
            row.setTag(key);
            text.setText(mTypes.get(key));
            return row;
        }
    }

    private class ThreadListEventCallback extends EventCallback<ThreadListEvent> {
        @Override
        public void onSuccess(ThreadListEvent event) {
            mInloading = false;
            swipeLayout.setRefreshing(false);
            hideFooter();

            ThreadListBean threads = event.mData;
            mFormhash = threads.getFormhash();

            if (mPage == 1) {
                mThreadBeans.clear();
                mThreadBeans.addAll(threads.getThreads());
                mThreadListAdapter.setDatas(mThreadBeans);
                mRecyclerView.scrollToTop();
            } else {
                for (ThreadBean newthread : threads.getThreads()) {
                    boolean duplicate = false;
                    for (int i = 0; i < mThreadBeans.size(); i++) {
                        ThreadBean oldthread = mThreadBeans.get(i);
                        if (newthread != null && newthread.getTid().equals(oldthread.getTid())) {
                            duplicate = true;
                            break;
                        }
                    }
                    if (!duplicate) {
                        mThreadBeans.add(newthread);
                    }
                }
                mThreadListAdapter.setDatas(mThreadBeans);
            }

            if (!mDataReceived) {
                mDataReceived = true;
                mMainFab.show();
                setActionBarSubtitle();
                if (TextUtils.isEmpty(mTypeId) && mTypes == null) {
                    if (threads.getTypes() != null && threads.getTypes().size() > 0) {
                        mTypes = threads.getTypes();
                        mForumTypeMenuItem.setVisible(true);
                        mForumTypeMenuItem.setIcon(new IconicsDrawable(mCtx, GoogleMaterial.Icon.gmd_sort)
                                .color(HiSettingsHelper.getInstance().getToolbarTextColor()).actionBar());
                    }
                }
                if (HiSettingsHelper.getInstance().isAppBarCollapsible())
                    ((MainFrameActivity) getActivity()).mAppBarLayout.setExpanded(true, true);
            }
            showNotification();
            if (mThreadListAdapter.getDatas().size() > 0) {
                mLoadingView.setState(ContentLoadingView.CONTENT);
            } else {
                mLoadingView.setState(ContentLoadingView.NO_DATA);
            }
        }

        @Override
        public void onFail(ThreadListEvent event) {
            mInloading = false;
            swipeLayout.setRefreshing(false);
            hideFooter();

            if (mPage > 1)
                mPage--;

            if (mThreadBeans.size() > 0) {
                mLoadingView.setState(ContentLoadingView.CONTENT);
            } else {
                mLoadingView.setState(ContentLoadingView.ERROR);
            }
            UIUtils.errorSnack(getView(), event.mMessage, event.mDetail);
            if (event.mStatus == Constants.STATUS_FAIL_SEC_QUESTION) {
                FragmentUtils.showPasswordActivity(getActivity(), false, true);
            }
        }

        @Override
        public void onFailRelogin(ThreadListEvent event) {
            enterNotLoginState();
            ((MainFrameActivity) getActivity()).showLoginDialog();
        }
    }

    protected void enterNotLoginState() {
        mInloading = false;
        swipeLayout.setRefreshing(false);
        hideFooter();
        mThreadBeans.clear();
        mThreadListAdapter.notifyDataSetChanged();
        mLoadingView.setState(ContentLoadingView.NOT_LOGIN);
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(PostEvent event) {
        PostBean postResult = event.mPostResult;

        String activitySessionId = "";
        if (getActivity() != null && getActivity() instanceof BaseActivity) {
            activitySessionId = ((BaseActivity) getActivity()).mSessionId;
        }
        if (!mSessionId.equals(event.mSessionId)
                && !activitySessionId.equals(event.mSessionId))
            return;

        EventBus.getDefault().removeStickyEvent(event);

        String message = event.mMessage;

        if (event.mStatus == Constants.STATUS_SUCCESS) {
            if (HiUtils.isValidId(postResult.getTid()))
                FragmentUtils.showThreadActivity(getActivity(), true, postResult.getTid(), postResult.getSubject(), -1, -1, null, -1);
            refresh();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NetworkReadyEvent event) {
        if (!mInloading && mThreadBeans.size() == 0)
            refresh();
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(ThreadListEvent event) {
        if (!mSessionId.equals(event.mSessionId))
            return;
        EventBus.getDefault().removeStickyEvent(event);
        mEventCallback.process(event);
    }

}