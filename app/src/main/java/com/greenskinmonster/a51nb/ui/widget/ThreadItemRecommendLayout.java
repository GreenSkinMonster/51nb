package com.greenskinmonster.a51nb.ui.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.RequestManager;
import com.greenskinmonster.a51nb.R;
import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.bean.RecommendThreadBean;
import com.greenskinmonster.a51nb.bean.ThreadBean;
import com.greenskinmonster.a51nb.utils.UIUtils;
import com.greenskinmonster.a51nb.utils.Utils;

/**
 * Created by GreenSkinMonster on 2017-08-01.
 */

public class ThreadItemRecommendLayout extends RelativeLayout implements ItemLayout {

    private ImageLayout mImageLayout;
    private Button mBtnLink;
    private TextViewWithEmoticon mTvTitle;
    private TextViewWithEmoticon mTvPostInfo;
    private TextViewWithEmoticon mTvContent;

    private RequestManager mGlide;

    public ThreadItemRecommendLayout(Context context, RequestManager glide) {
        super(context, null, 0);
        inflate(context, R.layout.item_thread_list_recommend, this);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(layoutParams);
        setPadding(Utils.dpToPx(context, 8), Utils.dpToPx(context, 4), Utils.dpToPx(context, 8), Utils.dpToPx(context, 4));

        mImageLayout = (ImageLayout) findViewById(R.id.image_layout);
        mBtnLink = (Button) findViewById(R.id.btn_item_link);
        mTvTitle = (TextViewWithEmoticon) findViewById(R.id.tv_title);
        mTvPostInfo = (TextViewWithEmoticon) findViewById(R.id.tv_post_info);
        mTvContent = (TextViewWithEmoticon) findViewById(R.id.tv_content);

        mTvTitle.setTextSize(HiSettingsHelper.getInstance().getTitleTextSize());
        mTvPostInfo.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
        mTvContent.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());

        OnClickListener onClickListener = new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (v.getTag(R.id.avatar_tag_uid) != null)
                    UIUtils.openUrl(getContext(), v.getTag(R.id.avatar_tag_uid).toString());
            }
        };

        mBtnLink.setOnClickListener(onClickListener);

        mGlide = glide;
    }

    @Override
    public void fillData(ThreadBean data) {
        RecommendThreadBean thread = (RecommendThreadBean) data;

        mTvTitle.setRichText(thread.getTitle());

        mImageLayout.setGlide(mGlide);
        mImageLayout.setUrl(thread.getItemImageUrl());

        mBtnLink.setTag(R.id.avatar_tag_uid, thread.getItemUrl());
        mImageLayout.setTag(R.id.avatar_tag_uid, thread.getItemUrl());

        mTvPostInfo.setRichText(thread.getPostInfo());
        mTvContent.setRichText(thread.getContent());
    }

}
