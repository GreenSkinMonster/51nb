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
import com.greenskinmonster.a51nb.bean.TradeThreadBean;
import com.greenskinmonster.a51nb.glide.GlideHelper;
import com.greenskinmonster.a51nb.utils.ColorHelper;
import com.greenskinmonster.a51nb.utils.Utils;

/**
 * Created by GreenSkinMonster on 2017-08-01.
 */

public class ThreadItemTradeLayout extends LinearLayout implements ItemLayout {

    private ImageView mAvatar;
    private TextView mTvAuthor;
    private TextView mTvTitle;
    private TextView mTvPrice;
    private TextView mTvLocation;
    private TextView mTvAuthorInfo;

    private RequestManager mGlide;

    public ThreadItemTradeLayout(Context context, RequestManager glide) {
        super(context, null, 0);
        inflate(context, R.layout.item_thread_list_trade, this);

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(layoutParams);
        setOrientation(VERTICAL);
        setPadding(Utils.dpToPx(context, 8), Utils.dpToPx(context, 4), Utils.dpToPx(context, 8), Utils.dpToPx(context, 4));

        mAvatar = (ImageView) findViewById(R.id.iv_avatar);
        mTvAuthor = (TextView) findViewById(R.id.tv_author);
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mTvPrice = (TextView) findViewById(R.id.tv_price);
        mTvLocation = (TextView) findViewById(R.id.tv_location);
        mTvAuthorInfo = (TextView) findViewById(R.id.tv_author_info);
        mGlide = glide;
    }

    public void fillData(final ThreadBean bean) {
        TradeThreadBean thread = (TradeThreadBean) bean;
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

        String location = Utils.nullToText(thread.getLocation());
        if (location.length() > 8) {
            location = location.substring(0, 8) + "..";
        }
        mTvLocation.setText(location);

        mTvPrice.setText("¥" + thread.getPrice());

        StringBuilder sb = new StringBuilder();
        if (thread.isStick()) {
            sb.append("置顶");
        }
        if (!TextUtils.isEmpty(thread.getTraderType())) {
            if (sb.length() > 0)
                sb.append(" · ");
            sb.append(thread.getTraderType());
        }
        if (!TextUtils.isEmpty(bean.getCreateTime())) {
            if (sb.length() > 0)
                sb.append(" · ");
            sb.append(Utils.shortyTime(bean.getCreateTime()));
        }
        mTvAuthorInfo.setText(sb.toString());

        if (HiSettingsHelper.getInstance().isLoadAvatar() && !TextUtils.isEmpty(thread.getAuthorId())) {
            mAvatar.setVisibility(View.VISIBLE);
            GlideHelper.loadAvatar(mGlide, mAvatar, thread.getAvatarUrl());
        } else {
            mAvatar.setVisibility(View.GONE);
        }
        mAvatar.setTag(R.id.avatar_tag_uid, thread.getAuthorId());
        mAvatar.setTag(R.id.avatar_tag_username, thread.getAuthor());
    }
}
