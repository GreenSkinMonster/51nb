package com.greenskinmonster.a51nb.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.greenskinmonster.a51nb.R;
import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.bean.ThreadBean;
import com.greenskinmonster.a51nb.glide.GlideHelper;
import com.greenskinmonster.a51nb.utils.ColorHelper;
import com.greenskinmonster.a51nb.utils.Utils;

/**
 * Created by GreenSkinMonster on 2016-04-21.
 */
public class ThreadItemLayout extends LinearLayout implements ItemLayout {

    private ImageView mAvatar;
    private TextView mTvAuthor;
    private TextView mTvTitle;
    private TextView mTvReplycounter;
    private TextView mTvCreateTime;
    private TextView mTvImageIndicator;

    private RequestManager mGlide;

    public ThreadItemLayout(Context context, RequestManager glide) {
        super(context, null, 0);
        inflate(context, R.layout.item_thread_list, this);

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(layoutParams);
        setOrientation(VERTICAL);
        setPadding(Utils.dpToPx(context, 8), Utils.dpToPx(context, 4), Utils.dpToPx(context, 8), Utils.dpToPx(context, 4));

        mAvatar = (ImageView) findViewById(R.id.iv_avatar);
        mTvAuthor = (TextView) findViewById(R.id.tv_author);
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mTvReplycounter = (TextView) findViewById(R.id.tv_replycounter);
        mTvCreateTime = (TextView) findViewById(R.id.tv_create_time);
        mTvImageIndicator = (TextView) findViewById(R.id.tv_image_indicator);
        mGlide = glide;
    }

    public void fillData(final ThreadBean thread) {
        mTvAuthor.setText(thread.getAuthor());

        mTvTitle.setTextSize(HiSettingsHelper.getInstance().getTitleTextSize());
        mTvTitle.setText(thread.getTitle());

        String titleColor = Utils.nullToText(thread.getTitleColor()).trim();

        if (titleColor.startsWith("#")) {
            try {
                mTvTitle.setTextColor(Color.parseColor(titleColor));
            } catch (Exception ignored) {
                mTvTitle.setTextColor(ColorHelper.getTextColorPrimary(getContext()));
            }
        } else
            mTvTitle.setTextColor(ColorHelper.getTextColorPrimary(getContext()));

        mTvReplycounter.setText(
                Utils.toCountText(thread.getReplyCount())
                        + "/"
                        + Utils.toCountText(thread.getViewCount()));

        StringBuilder sb = new StringBuilder();
        if (thread.isStick()) {
            sb.append("置顶");
        }
        if (thread.isPoll()) {
            if (sb.length() > 0)
                sb.append(" · ");
            sb.append("投票");
        }
        if (sb.length() > 0)
            sb.append(" · ");
        sb.append(Utils.shortyTime(thread.getCreateTime()));
        if (!TextUtils.isEmpty(thread.getReadPerm())) {
            sb.append(" · ");
            sb.append("阅读权限 ").append(thread.getReadPerm());
        }
        if (!TextUtils.isEmpty(thread.getCredit())) {
            sb.append(" · ");
            sb.append("自动悬赏 ").append(thread.getCredit());
        }

        mTvCreateTime.setText(sb.toString());

        if (thread.getHaveImage()) {
            mTvImageIndicator.setVisibility(View.VISIBLE);
        } else {
            mTvImageIndicator.setVisibility(View.GONE);
        }

        if (HiSettingsHelper.getInstance().isLoadAvatar()) {
            mAvatar.setVisibility(View.VISIBLE);
            GlideHelper.loadAvatar(mGlide, mAvatar, thread.getAvatarUrl());
        } else {
            mAvatar.setVisibility(View.GONE);
        }
        mAvatar.setTag(R.id.avatar_tag_uid, thread.getAuthorId());
        mAvatar.setTag(R.id.avatar_tag_username, thread.getAuthor());
    }

}
