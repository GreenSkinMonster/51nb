package com.greenskinmonster.a51nb.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.greenskinmonster.a51nb.R;
import com.greenskinmonster.a51nb.async.BlacklistHelper;
import com.greenskinmonster.a51nb.async.PostSmsAsyncTask;
import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.bean.UserInfoBean;
import com.greenskinmonster.a51nb.glide.GlideHelper;
import com.greenskinmonster.a51nb.job.SimpleListJob;
import com.greenskinmonster.a51nb.okhttp.OkHttpHelper;
import com.greenskinmonster.a51nb.parser.HiParser;
import com.greenskinmonster.a51nb.ui.widget.HiProgressDialog;
import com.greenskinmonster.a51nb.ui.widget.OnSingleClickListener;
import com.greenskinmonster.a51nb.ui.widget.SimpleDivider;
import com.greenskinmonster.a51nb.ui.widget.TextViewWithEmoticon;
import com.greenskinmonster.a51nb.utils.Constants;
import com.greenskinmonster.a51nb.utils.HiUtils;
import com.greenskinmonster.a51nb.utils.Logger;
import com.greenskinmonster.a51nb.utils.UIUtils;
import com.greenskinmonster.a51nb.utils.Utils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Map;

import okhttp3.Request;

public class UserinfoFragment extends BaseFragment implements PostSmsAsyncTask.SmsPostListener {

    public static final String ARG_USERNAME = "USERNAME";
    public static final String ARG_UID = "UID";

    private String mUid;
    private String mUsername;
    private String mAvatarUrl;
    private String mFormhash;

    private ImageView mAvatarView;
    private TextView mUsernameView;
    private TextView mOnlineView;

    private RecyclerView mRecyclerView;
    private UserInfoAdapter mUserInfoAdapter;
    private Map<String, String> mUserInfos;

    private HiProgressDialog smsPostProgressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (getArguments().containsKey(ARG_USERNAME)) {
            mUsername = getArguments().getString(ARG_USERNAME);
        }

        if (getArguments().containsKey(ARG_UID)) {
            mUid = getArguments().getString(ARG_UID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_info, container, false);
        view.setClickable(false);

        mAvatarView = (ImageView) view.findViewById(R.id.userinfo_avatar);
        if (HiSettingsHelper.getInstance().isLoadAvatar()) {
            mAvatarView.setVisibility(View.VISIBLE);
            mAvatarView.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    if (!TextUtils.isEmpty(mAvatarUrl)) {
                        GlideHelper.clearAvatarCache(mAvatarUrl);
                        GlideHelper.loadAvatar(UserinfoFragment.this, mAvatarView, mAvatarUrl);
                        UIUtils.toast("头像已经刷新");
                    } else {
                        UIUtils.toast("用户未设置头像");
                    }
                }
            });
        } else {
            mAvatarView.setVisibility(View.GONE);
        }

        mUsernameView = (TextView) view.findViewById(R.id.userinfo_username);
        mUsernameView.setText(mUsername);
        mUsernameView.setTextSize(HiSettingsHelper.getInstance().getPostTextSize() + 2);

        mOnlineView = (TextView) view.findViewById(R.id.user_online);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new SimpleDivider(getActivity()));

        mUserInfoAdapter = new UserInfoAdapter(getActivity());
        mRecyclerView.setAdapter(mUserInfoAdapter);

        Button btnShowMyPosts = (Button) view.findViewById(R.id.btn_search_threads);
        btnShowMyPosts.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(SimpleListFragment.ARG_UID, mUid);
                bundle.putString(SimpleListFragment.ARG_USERNAME, mUsername);
                FragmentUtils.showSimpleListActivity(UserinfoFragment.this.getActivity(), false, SimpleListJob.TYPE_MYPOST, bundle);

            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        OkHttpHelper.getInstance().asyncGet(HiUtils.UserInfoUrl + mUid, new UserInfoCallback());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_userinfo, menu);
        menu.findItem(R.id.action_send_sms).setIcon(new IconicsDrawable(getActivity(),
                GoogleMaterial.Icon.gmd_insert_comment).actionBar()
                .color(HiSettingsHelper.getInstance().getToolbarTextColor()));

        setActionBarTitle("用户信息");

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Implemented in activity
                return false;
            case R.id.action_send_sms:
                UIUtils.showSendSmsDialog(getActivity(), mFormhash, mUid, mUsername, this);
                return true;
            case R.id.action_blacklist:
                BlacklistHelper.addBlacklist(mFormhash, mUsername, new OkHttpHelper.ResultCallback() {
                    @Override
                    public void onError(Request request, Exception e) {
                        UIUtils.toast(OkHttpHelper.getErrorMessage(e).getMessage());
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            Document doc = Jsoup.parse(response);
                            String message = Utils.nullToText(HiParser.parseErrorMessage(doc));
                            if (message.contains("成功")) {
                                BlacklistHelper.syncBlacklists();
                            }
                            UIUtils.toast(message);
                        } catch (Exception e) {
                            UIUtils.toast(OkHttpHelper.getErrorMessage(e).getMessage());
                        }
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class UserInfoCallback implements OkHttpHelper.ResultCallback {
        @Override
        public void onError(Request request, Exception e) {
            Logger.e(e);
            UIUtils.toast(OkHttpHelper.getErrorMessage(e).getMessage());
        }

        @Override
        public void onResponse(String response) {
            UserInfoBean info = HiParser.parseUserInfo(response);
            if (info != null) {
                if (HiSettingsHelper.getInstance().isLoadAvatar()) {
                    mAvatarView.setVisibility(View.VISIBLE);
                    GlideHelper.loadAvatar(UserinfoFragment.this, mAvatarView, info.getAvatarUrl());
                    mAvatarUrl = info.getAvatarUrl();
                } else {
                    mAvatarView.setVisibility(View.GONE);
                }
                mUsername = info.getUsername();
                mUsernameView.setText(mUsername);
                mFormhash = info.getFormhash();

                if (info.isOnline()) {
                    mOnlineView.setText("在线");
                } else {
                    mOnlineView.setText("离线");
                }

                mUserInfos = info.getInfos();
                mUserInfoAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onSmsPrePost() {
        smsPostProgressDialog = HiProgressDialog.show(getActivity(), "正在发送...");
    }

    @Override
    public void onSmsPostDone(int status, final String message, AlertDialog dialog) {
        if (status == Constants.STATUS_SUCCESS) {
            smsPostProgressDialog.dismiss(message);
            if (dialog != null)
                dialog.dismiss();
        } else {
            smsPostProgressDialog.dismissError(message);
        }
    }

    private class UserInfoAdapter extends RecyclerView.Adapter {

        private LayoutInflater mInflater;

        UserInfoAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolderImpl(mInflater.inflate(R.layout.item_userinfo, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

            ViewHolderImpl holder = (ViewHolderImpl) viewHolder;

            String key = mUserInfos.keySet().toArray(new String[mUserInfos.size()])[position];
            String value = mUserInfos.get(key);

            if (TextUtils.isEmpty(value)) {
                holder.tvTitle.setText(key);
                holder.tvInfo.setText("");
                holder.tvTitle.setTypeface(null, Typeface.BOLD);
            } else {
                holder.tvTitle.setText(key);
                holder.tvTitle.setTypeface(null, Typeface.NORMAL);
                holder.tvInfo.setRichText(value);
            }
        }

        @Override
        public int getItemCount() {
            return mUserInfos == null ? 0 : mUserInfos.size();
        }

        private class ViewHolderImpl extends RecyclerView.ViewHolder {
            TextView tvTitle;
            TextViewWithEmoticon tvInfo;

            ViewHolderImpl(View itemView) {
                super(itemView);
                tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
                tvInfo = (TextViewWithEmoticon) itemView.findViewById(R.id.tv_info);
            }
        }

    }

}
