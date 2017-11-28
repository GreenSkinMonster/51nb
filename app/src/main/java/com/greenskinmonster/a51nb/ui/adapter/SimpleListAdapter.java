package com.greenskinmonster.a51nb.ui.adapter;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.greenskinmonster.a51nb.R;
import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.bean.SimpleListItemBean;
import com.greenskinmonster.a51nb.glide.GlideHelper;
import com.greenskinmonster.a51nb.job.SimpleListJob;
import com.greenskinmonster.a51nb.ui.BaseFragment;
import com.greenskinmonster.a51nb.ui.widget.TextViewWithEmoticon;
import com.greenskinmonster.a51nb.utils.ColorHelper;
import com.greenskinmonster.a51nb.utils.Utils;

/**
 * Created by GreenSkinMonster on 2016-11-14.
 */

public class SimpleListAdapter extends BaseRvAdapter<SimpleListItemBean> {

    private LayoutInflater mInflater;
    private BaseFragment mFragment;
    private int mType;

    public SimpleListAdapter(BaseFragment fragment, int type, RecyclerItemClickListener itemClickListener) {
        mInflater = LayoutInflater.from(fragment.getActivity());
        mFragment = fragment;
        mType = type;
        mListener = itemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolderImpl(ViewGroup parent, int viewType) {
        if (mType == SimpleListJob.TYPE_THREAD_NOTIFY) {
            return new ViewHolderImpl(mInflater.inflate(R.layout.item_simple_list_notify, parent, false));
        } else {
            return new ViewHolderImpl(mInflater.inflate(R.layout.item_simple_list, parent, false));
        }
    }

    @Override
    public void onBindViewHolderImpl(RecyclerView.ViewHolder viewHolder, int position) {
        ViewHolderImpl holder = (ViewHolderImpl) viewHolder;

        SimpleListItemBean item = getItem(position);

        holder.tv_title.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
        holder.tv_title.setText(Utils.trim(item.getTitle()));
        if (item.isNew()) {
            holder.tv_title.setTextColor(ContextCompat.getColor(mFragment.getActivity(), R.color.red));
        } else {
            holder.tv_title.setTextColor(ColorHelper.getTextColorPrimary(mFragment.getActivity()));
        }

        if (TextUtils.isEmpty(item.getInfo())) {
            holder.tv_info.setVisibility(View.GONE);
        } else {
            holder.tv_info.setVisibility(View.VISIBLE);
            holder.tv_info.setFragment(mFragment);
            holder.tv_info.setRichText(item.getInfo());
            holder.tv_info.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
        }

        if (TextUtils.isEmpty(item.getCreateTime())) {
            holder.tv_time.setVisibility(View.GONE);
        } else {
            holder.tv_time.setVisibility(View.VISIBLE);
            holder.tv_time.setText(Utils.shortyTime(item.getCreateTime()));
        }

        if (TextUtils.isEmpty(item.getForum()) && TextUtils.isEmpty(item.getAuthor())) {
            holder.tv_forum.setVisibility(View.GONE);
            holder.tv_author.setVisibility(View.GONE);
        } else {
            holder.tv_forum.setVisibility(View.VISIBLE);
            holder.tv_author.setVisibility(View.VISIBLE);
            holder.tv_time.setText(Utils.shortyTime(item.getCreateTime()));
            if (mType == SimpleListJob.TYPE_SMS || mType == SimpleListJob.TYPE_THREAD_NOTIFY) {
                if (TextUtils.isEmpty(item.getSmsSayTo()))
                    holder.tv_forum.setText(item.getAuthor());
                else
                    holder.tv_forum.setText(item.getSmsSayTo());
            } else {
                holder.tv_forum.setText(item.getForum());
                holder.tv_author.setText(item.getAuthor());
            }
        }

        if (HiSettingsHelper.getInstance().isLoadAvatar()
                && mType != SimpleListJob.TYPE_SEARCH_USER_THREADS
                && mType != SimpleListJob.TYPE_FAVORITES
                && mType != SimpleListJob.TYPE_MYPOST) {
            holder.iv_avatar.setVisibility(View.VISIBLE);
            GlideHelper.loadAvatar(mFragment, holder.iv_avatar, item.getAvatarUrl());
        } else {
            holder.iv_avatar.setVisibility(View.GONE);
        }
    }

    private static class ViewHolderImpl extends RecyclerView.ViewHolder {
        TextView tv_title;
        TextView tv_forum;
        TextViewWithEmoticon tv_info;
        TextView tv_author;
        TextView tv_time;
        ImageView iv_avatar;

        ViewHolderImpl(View itemView) {
            super(itemView);
            tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            tv_info = (TextViewWithEmoticon) itemView.findViewById(R.id.tv_info);
            tv_forum = (TextView) itemView.findViewById(R.id.tv_forum);
            tv_time = (TextView) itemView.findViewById(R.id.tv_time);
            tv_author = (TextView) itemView.findViewById(R.id.tv_author);
            iv_avatar = (ImageView) itemView.findViewById(R.id.iv_avatar);
        }
    }

}
