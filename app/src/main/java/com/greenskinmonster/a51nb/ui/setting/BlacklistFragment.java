package com.greenskinmonster.a51nb.ui.setting;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.greenskinmonster.a51nb.R;
import com.greenskinmonster.a51nb.async.BlacklistHelper;
import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.bean.UserBean;
import com.greenskinmonster.a51nb.okhttp.OkHttpHelper;
import com.greenskinmonster.a51nb.parser.HiParser;
import com.greenskinmonster.a51nb.ui.BaseFragment;
import com.greenskinmonster.a51nb.ui.widget.ContentLoadingView;
import com.greenskinmonster.a51nb.ui.widget.OnSingleClickListener;
import com.greenskinmonster.a51nb.ui.widget.SimpleDivider;
import com.greenskinmonster.a51nb.utils.ColorHelper;
import com.greenskinmonster.a51nb.utils.UIUtils;
import com.greenskinmonster.a51nb.utils.Utils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;

/**
 * Created by GreenSkinMonster on 2017-07-15.
 */

public class BlacklistFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG_KEY = "BLACKLIST_KEY";

    private List<UserBean> mBlacklists = new ArrayList<>();

    private String mFormhash;
    private LayoutInflater mInflater;
    private Drawable mDrawable;

    private View.OnClickListener mOnClickListener;
    private SwipeRefreshLayout mSwipeLayout;
    private ContentLoadingView mLoadingView;

    private RvAdapter mAdapter = new RvAdapter();
    private List<String> mRemoving = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blacklist, container, false);
        mInflater = inflater;

        mDrawable = new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_close)
                .color(Color.GRAY)
                .sizeDp(12);

        mOnClickListener = new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                removeFromBlacklist((UserBean) v.getTag());
                v.setVisibility(View.INVISIBLE);
            }
        };

        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeColors(ColorHelper.getSwipeColor(getActivity()));
        mSwipeLayout.setProgressBackgroundColorSchemeColor(ColorHelper.getSwipeBackgroundColor(getActivity()));

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new SimpleDivider(getActivity()));

        mLoadingView = (ContentLoadingView) view.findViewById(R.id.content_loading);

        recyclerView.setAdapter(mAdapter);

        refresh();

        setActionBarTitle("黑名单");
        return view;
    }

    protected void refresh() {
        if (!mSwipeLayout.isRefreshing())
            mSwipeLayout.setRefreshing(true);

        mRemoving.clear();
        BlacklistHelper.getBlacklists(new OkHttpHelper.ResultCallback() {
            @Override
            public void onError(Request request, Exception e) {
                UIUtils.toast("获取黑名单发生错误 : " + OkHttpHelper.getErrorMessage(e).getMessage());
                mSwipeLayout.setRefreshing(false);
                mLoadingView.setState(mBlacklists.size() > 0 ? ContentLoadingView.CONTENT : ContentLoadingView.NO_DATA);
            }

            @Override
            public void onResponse(String response) {
                mSwipeLayout.setRefreshing(false);
                try {
                    Document doc = Jsoup.parse(response);
                    mFormhash = HiParser.parseFormhash(doc);
                    String errorMsg = HiParser.parseErrorMessage(doc);
                    if (TextUtils.isEmpty(errorMsg)) {
                        mBlacklists = HiParser.parseBlacklist(doc);
                        mAdapter.notifyDataSetChanged();
                        HiSettingsHelper.getInstance().setBlacklists(mBlacklists);
                        HiSettingsHelper.getInstance().setBlacklistSyncTime();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                UIUtils.toast("黑名单数据已同步");
                            }
                        }, 200);
                    } else {
                        UIUtils.toast(errorMsg);
                    }
                } catch (Exception e) {
                    UIUtils.toast(OkHttpHelper.getErrorMessage(e).getMessage());
                }
                mLoadingView.setState(mBlacklists.size() > 0 ? ContentLoadingView.CONTENT : ContentLoadingView.NO_DATA);
            }
        });
    }

    private void removeFromBlacklist(final UserBean user) {
        mRemoving.add(user.getUid());
        BlacklistHelper.delBlacklist(user.getUid(), new OkHttpHelper.ResultCallback() {
            @Override
            public void onError(Request request, Exception e) {
                UIUtils.toast(OkHttpHelper.getErrorMessage(e).getMessage());
            }

            @Override
            public void onResponse(String response) {
                try {
                    Document doc = Jsoup.parse(response);
                    String errorMsg = Utils.nullToText(HiParser.parseErrorMessage(doc));
                    if (!TextUtils.isEmpty(errorMsg))
                        UIUtils.toast(errorMsg);
                    if (errorMsg.contains("成功")) {
                        int pos = -1;
                        for (int i = 0; i < mBlacklists.size(); i++) {
                            UserBean userbean = mBlacklists.get(i);
                            if (userbean.getUid().equals(user.getUid())) {
                                pos = i;
                                break;
                            }
                        }
                        if (pos != -1) {
                            mBlacklists.remove(pos);
                            mAdapter.notifyItemRemoved(pos);
                            if (mAdapter.getItemCount() - pos - 1 > 0)
                                mAdapter.notifyItemRangeChanged(pos, mAdapter.getItemCount() - pos - 1);

                            mLoadingView.setState(mBlacklists.size() > 0 ? ContentLoadingView.CONTENT : ContentLoadingView.NO_DATA);
                        } else {
                            refresh();
                        }
                        HiSettingsHelper.getInstance().removeFromBlacklist(user);
                    } else {
                        refresh();
                    }
                } catch (Exception e) {
                    UIUtils.toast(OkHttpHelper.getErrorMessage(e).getMessage());
                }

            }
        });
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    private class RvAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(mInflater.inflate(R.layout.item_blacklist, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ViewHolder viewHolder = (ViewHolder) holder;
            UserBean user = mBlacklists.get(position);

            viewHolder.tv_username.setText(user.getUsername());
            viewHolder.ib_remove.setImageDrawable(mDrawable);
            viewHolder.ib_remove.setTag(user);
            viewHolder.ib_remove.setOnClickListener(mOnClickListener);
            viewHolder.ib_remove.setVisibility(mRemoving.contains(user.getUid()) ? View.INVISIBLE : View.VISIBLE);
        }

        @Override
        public int getItemCount() {
            return mBlacklists != null ? mBlacklists.size() : 0;
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_username;
        ImageButton ib_remove;

        ViewHolder(View itemView) {
            super(itemView);
            tv_username = (TextView) itemView.findViewById(R.id.tv_author);
            ib_remove = (ImageButton) itemView.findViewById(R.id.ib_remove);
        }
    }

}
