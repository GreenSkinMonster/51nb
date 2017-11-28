package com.greenskinmonster.a51nb.ui.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.greenskinmonster.a51nb.R;
import com.greenskinmonster.a51nb.bean.CommentBean;
import com.greenskinmonster.a51nb.bean.CommentListBean;
import com.greenskinmonster.a51nb.bean.ContentAbs;
import com.greenskinmonster.a51nb.bean.ContentAttach;
import com.greenskinmonster.a51nb.bean.ContentGoToFloor;
import com.greenskinmonster.a51nb.bean.ContentImg;
import com.greenskinmonster.a51nb.bean.ContentInfo;
import com.greenskinmonster.a51nb.bean.ContentNotice;
import com.greenskinmonster.a51nb.bean.ContentQuote;
import com.greenskinmonster.a51nb.bean.ContentText;
import com.greenskinmonster.a51nb.bean.ContentTradeInfo;
import com.greenskinmonster.a51nb.bean.DetailBean;
import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.bean.PollBean;
import com.greenskinmonster.a51nb.bean.PollOptionBean;
import com.greenskinmonster.a51nb.bean.RateBean;
import com.greenskinmonster.a51nb.bean.RateListBean;
import com.greenskinmonster.a51nb.cache.ImageContainer;
import com.greenskinmonster.a51nb.cache.ImageInfo;
import com.greenskinmonster.a51nb.glide.GlideHelper;
import com.greenskinmonster.a51nb.ui.BaseActivity;
import com.greenskinmonster.a51nb.ui.FragmentArgs;
import com.greenskinmonster.a51nb.ui.FragmentUtils;
import com.greenskinmonster.a51nb.ui.ThreadDetailFragment;
import com.greenskinmonster.a51nb.ui.widget.ImageLayout;
import com.greenskinmonster.a51nb.ui.widget.OnSingleClickListener;
import com.greenskinmonster.a51nb.ui.widget.TextViewWithEmoticon;
import com.greenskinmonster.a51nb.ui.widget.ThreadImageLayout;
import com.greenskinmonster.a51nb.utils.ColorHelper;
import com.greenskinmonster.a51nb.utils.HiUtils;
import com.greenskinmonster.a51nb.utils.HtmlCompat;
import com.greenskinmonster.a51nb.utils.UIUtils;
import com.greenskinmonster.a51nb.utils.Utils;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by GreenSkinMonster on 2016-11-08.
 */

public class ThreadDetailAdapter extends BaseRvAdapter<DetailBean> {

    private Context mCtx;
    private LayoutInflater mInflater;
    private ThreadDetailFragment mDetailFragment;
    private int mBackgroundResource;
    private int mBackgroundColor;
    private ThreadDetailListener mDetailListener;
    private Drawable mAndroidIcon;
    private Drawable mIosIcon;

    private WeakHashMap<CompoundButton, Object> mPollButtons;
    private View.OnClickListener mOnRadioCheckedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mPollButtons != null) {
                for (CompoundButton radioButton : mPollButtons.keySet()) {
                    if (radioButton != null && !radioButton.equals(v)) {
                        radioButton.setChecked(false);
                    }
                }
            }
        }
    };

    public ThreadDetailAdapter(Context context,
                               ThreadDetailFragment detailFragment,
                               ThreadDetailListener detailListener) {
        mCtx = context;
        mInflater = LayoutInflater.from(context);
        mDetailFragment = detailFragment;
        mDetailListener = detailListener;

        mListener = detailListener.getRecyclerItemClickListener();

        int[] attrs = new int[]{R.attr.selectableItemBackground};
        TypedArray typedArray = mCtx.obtainStyledAttributes(attrs);
        mBackgroundResource = typedArray.getResourceId(0, 0);
        typedArray.recycle();

        mBackgroundColor = HiSettingsHelper.getInstance().getActiveTheme().equals(HiSettingsHelper.THEME_LIGHT)
                ? R.color.md_light_green_100 : R.color.md_blue_grey_900;

        mAndroidIcon = new IconicsDrawable(mCtx, FontAwesome.Icon.faw_android)
                .color(ColorHelper.getColorAccent(mCtx)).sizeDp(24);
        mIosIcon = new IconicsDrawable(mCtx, FontAwesome.Icon.faw_apple)
                .color(ColorHelper.getColorAccent(mCtx)).sizeDp(24);
    }

    @Override
    public ViewHolderImpl onCreateViewHolderImpl(ViewGroup parent, int position) {
        return new ViewHolderImpl(mInflater.inflate(R.layout.item_thread_detail, parent, false));
    }

    @Override
    public void onBindViewHolderImpl(RecyclerView.ViewHolder viewHolder, final int position) {
        ViewHolderImpl holder;
        if (viewHolder instanceof ViewHolderImpl)
            holder = (ViewHolderImpl) viewHolder;
        else return;

        viewHolder.itemView.setTag(position);
        viewHolder.itemView.setOnTouchListener(mListener);

        final DetailBean detail = getItem(position);

        int pLeft = viewHolder.itemView.getPaddingLeft();
        int pRight = viewHolder.itemView.getPaddingRight();
        int pTop = viewHolder.itemView.getPaddingTop();
        int pBottom = viewHolder.itemView.getPaddingBottom();
        if (detail.isHighlightMode()) {
            viewHolder.itemView.setBackgroundColor(ContextCompat.getColor(mCtx, mBackgroundColor));
        } else {
            viewHolder.itemView.setBackgroundResource(mBackgroundResource);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            //set background cause padding values lost, reset them
            viewHolder.itemView.setPadding(pLeft, pTop, pRight, pBottom);
        }

        holder.ivMenu.setTag(detail);
        holder.ivMenu.setOnClickListener(mDetailListener.getMenuListener());

        holder.author.setText(detail.getAuthor());
        if (TextUtils.isEmpty(detail.getNickname())) {
            holder.nickname.setText("");
        } else {
            holder.nickname.setText(" · " + detail.getNickname());
        }
        if (detail.isThreadAuthor()) {
            holder.time.setText(HtmlCompat.fromHtml("<font color=" + ColorHelper.getColorAccent(mCtx) + ">楼主</font> · " + Utils.shortyTime(detail.getTimePost())));
        } else {
            holder.time.setText(Utils.shortyTime(detail.getTimePost()));
        }

        if (detail.isSelectMode()) {
            holder.floor.setText("X");
            holder.floor.setTag(null);
            holder.floor.setTextColor(ContextCompat.getColor(mCtx, R.color.md_amber_900));
            holder.floor.setClickable(false);
            holder.floor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    detail.setSelectMode(false);
                    notifyItemChanged(position);
                }
            });
        } else {
            holder.floor.setText(detail.getFloorText());
            holder.floor.setTag(null);
            holder.floor.setTextColor(ColorHelper.getTextColorSecondary(mCtx));
            holder.floor.setClickable(false);
            holder.floor.setOnClickListener(null);
        }

        if (DetailBean.CLIENT_ANDROID == detail.getClientType()) {
            holder.ivClient.setVisibility(View.VISIBLE);
            holder.ivClient.setImageDrawable(mAndroidIcon);
            holder.ivClient.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    FragmentArgs args = FragmentUtils.parseUrl(detail.getClientUrl());
                    if (args != null && mCtx instanceof BaseActivity)
                        FragmentUtils.show((BaseActivity) mCtx, args);
                }
            });
        } else if (DetailBean.CLIENT_IOS == detail.getClientType()) {
            holder.ivClient.setVisibility(View.VISIBLE);
            holder.ivClient.setImageDrawable(mIosIcon);
            holder.ivClient.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    FragmentArgs args = FragmentUtils.parseUrl(detail.getClientUrl());
                    if (args != null && mCtx instanceof BaseActivity)
                        FragmentUtils.show((BaseActivity) mCtx, args);
                }
            });
        } else {
            holder.ivClient.setVisibility(View.GONE);
        }

        boolean trimBr = false;

        if (HiSettingsHelper.getInstance().isLoadAvatar()) {
            holder.avatar.setVisibility(View.VISIBLE);
            loadAvatar(detail.getAvatarUrl(), holder.avatar);
        } else {
            holder.avatar.setVisibility(View.GONE);
        }
        holder.avatar.setTag(R.id.avatar_tag_uid, detail.getUid());
        holder.avatar.setTag(R.id.avatar_tag_username, detail.getAuthor());
        holder.avatar.setOnClickListener(mDetailListener.getAvatarListener());

        holder.author.setTag(R.id.avatar_tag_uid, detail.getUid());
        holder.author.setTag(R.id.avatar_tag_username, detail.getAuthor());
        holder.author.setOnClickListener(mDetailListener.getAvatarListener());

        if (detail.isCommentable()) {
            holder.ivComment.setVisibility(View.VISIBLE);
            holder.ivComment.setOnClickListener(mDetailListener.getCommentListener());
            holder.ivComment.setTag(detail);
        } else {
            holder.ivComment.setVisibility(View.GONE);
        }

        holder.ivReply.setTag(detail);
        holder.ivReply.setOnClickListener(mDetailListener.getReplyListener());

        LinearLayout contentView = holder.contentView;
        contentView.removeAllViews();

        if (detail.isSelectMode()) {
            TextView tv = (TextView) mInflater.inflate(R.layout.item_textview, null, false);
            tv.setText(detail.getContents().getCopyText());
            tv.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
            tv.setPadding(8, 8, 8, 8);
            UIUtils.setLineSpacing(tv);
            tv.setTextIsSelectable(true);

            contentView.addView(tv);
        } else {
            for (int i = 0; i < detail.getContents().getSize(); i++) {
                ContentAbs content = detail.getContents().get(i);
                if (content instanceof ContentTradeInfo) {
                    Map<String, String> tradeInfos = ((ContentTradeInfo) content).getTradeInfo();
                    if (tradeInfos != null && tradeInfos.size() > 0) {
                        int i1 = tradeInfos.size() % 2;
                        for (String key : tradeInfos.keySet()) {
                            TextView tvInfo = new TextView(mCtx);
                            tvInfo.setText(key + " " + tradeInfos.get(key));
                            tvInfo.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
                            if ("商品信息".equals(key) || "商家信息".equals(key)) {
                                tvInfo.setTypeface(null, Typeface.BOLD);
                            } else {
                                tvInfo.setTypeface(null, Typeface.NORMAL);
                            }
                            tvInfo.setBackgroundColor(ContextCompat.getColor(mCtx, i1 % 2 == 1 ? R.color.background_silver : android.R.color.transparent));
                            tvInfo.setPadding(8, 8, 8, 8);
                            contentView.addView(tvInfo);
                            i1++;
                        }
                    }
                    TextView tvInfo = new TextView(mCtx);
                    tvInfo.setText("  ");
                    tvInfo.setPadding(8, 8, 8, 8);
                    contentView.addView(tvInfo);
                } else if (content instanceof ContentNotice) {
                    TextView tv = (TextView) mInflater.inflate(R.layout.item_textview_notice, null, false);
                    String cnt = content.getContent();
                    if (!TextUtils.isEmpty(cnt)) {
                        tv.setText(cnt);
                        contentView.addView(tv);
                    }
                } else if (content instanceof ContentInfo) {
                    TextView tv = new TextView(mCtx);
                    String cnt = content.getContent();
                    if (!TextUtils.isEmpty(cnt)) {
                        tv.setText(cnt);
                        ViewCompat.setBackground(tv, ContextCompat.getDrawable(mCtx, R.drawable.rounded_corner));
                        tv.setTextColor(ColorHelper.getTextColorSecondary(mCtx));
                        tv.setPadding(12, 12, 12, 12);
                        contentView.addView(tv);
                    }
                } else if (content instanceof ContentText) {
                    TextViewWithEmoticon tv = (TextViewWithEmoticon) mInflater.inflate(R.layout.item_textview_withemoticon, null, false);
                    tv.setFragment(mDetailFragment);
                    tv.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
                    tv.setPadding(8, 8, 8, 8);

                    String cnt = content.getContent();
                    if (trimBr)
                        cnt = Utils.removeLeadingBlank(cnt);
                    if (!TextUtils.isEmpty(cnt)) {
                        tv.setRichText(cnt);
                        tv.setFocusable(false);
                        contentView.addView(tv);
                    }
                } else if (content instanceof ContentImg) {
                    final ContentImg contentImg = ((ContentImg) content);

                    String imageUrl;
                    String thumbUrl = contentImg.getThumbUrl();
                    String fullUrl = contentImg.getContent();
                    if (ImageContainer.getImageInfo(fullUrl).getStatus() == ImageInfo.SUCCESS) {
                        imageUrl = fullUrl;
                    } else {
                        imageUrl = TextUtils.isEmpty(thumbUrl) ? fullUrl : thumbUrl;
                    }

                    int imageIndex = contentImg.getIndexInPage();

                    ThreadImageLayout threadImageLayout = new ThreadImageLayout(mDetailFragment, imageUrl);
                    threadImageLayout.setParsedFileSize(contentImg.getFileSize());
                    threadImageLayout.setParentSessionId(mDetailFragment.mSessionId);
                    threadImageLayout.setImageIndex(imageIndex);
                    threadImageLayout.setContentImg(contentImg);

                    contentView.addView(threadImageLayout);
                } else if (content instanceof ContentAttach) {
                    TextViewWithEmoticon tv = (TextViewWithEmoticon) mInflater.inflate(R.layout.item_textview_withemoticon, null, false);
                    tv.setFragment(mDetailFragment);
                    tv.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
                    tv.setRichText(content.getContent());
                    tv.setFocusable(false);
                    contentView.addView(tv);
                } else if (content instanceof ContentGoToFloor || content instanceof ContentQuote) {

                    String author = "";
                    String time = "";
                    String note = "";
                    String text = "";

                    int floor = -1;
                    if (content instanceof ContentGoToFloor) {
                        //floor is not accurate if some user deleted post
                        //use floor to get page, then get cache by postid
                        ContentGoToFloor goToFloor = (ContentGoToFloor) content;
                        author = goToFloor.getAuthor();
                        floor = goToFloor.getFloor();
                        String postId = goToFloor.getPostId();
                        DetailBean detailBean = mDetailFragment.getCachedPost(postId);
                        if (detailBean != null) {
                            text = detailBean.getContents().getContent();
                            floor = detailBean.getFloor();
                            note = detailBean.getFloorText() + "#";
                        } else if (floor > 0) {
                            note = floor + "#";
                        }
                    } else {
                        ContentQuote contentQuote = (ContentQuote) content;
                        DetailBean detailBean = null;
                        String postId = contentQuote.getPostId();
                        if (HiUtils.isValidId(postId)) {
                            detailBean = mDetailFragment.getCachedPost(postId);
                        }
                        author = contentQuote.getAuthor();
                        text = contentQuote.getContent();
                        time = contentQuote.getTime();
                        if (detailBean != null) {
                            floor = detailBean.getFloor();
                            note = detailBean.getFloorText() + "#";
                        }
                    }

                    text = Utils.removeLeadingBlank(text);

                    LinearLayout quoteLayout = (LinearLayout) mInflater.inflate(R.layout.item_quote_text, null, false);
                    View view = quoteLayout.findViewById(R.id.quote_header);
                    if (TextUtils.isEmpty(author) && floor <= 0) {
                        view.setVisibility(View.GONE);
                    } else {
                        view.setVisibility(View.VISIBLE);
                    }
                    TextView tvAuthor = (TextView) quoteLayout.findViewById(R.id.quote_author);
                    TextView tvNote = (TextView) quoteLayout.findViewById(R.id.quote_note);
                    TextViewWithEmoticon tvContent = (TextViewWithEmoticon) quoteLayout.findViewById(R.id.quote_content);
                    TextView tvTime = (TextView) quoteLayout.findViewById(R.id.quote_post_time);

                    tvContent.setFragment(mDetailFragment);

                    tvAuthor.setText(Utils.nullToText(author));
                    tvNote.setText(Utils.nullToText(note));
                    tvContent.setRichText(Utils.nullToText(text));
                    tvTime.setText(Utils.shortyTime(time));

                    tvAuthor.setTextSize(HiSettingsHelper.getInstance().getPostTextSize() - 2);
                    tvNote.setTextSize(HiSettingsHelper.getInstance().getPostTextSize() - 2);
                    tvContent.setTextSize(HiSettingsHelper.getInstance().getPostTextSize() - 1);
                    tvTime.setTextSize(HiSettingsHelper.getInstance().getPostTextSize() - 4);

                    if (floor > 0) {
                        tvNote.setTag(floor);
                        tvNote.setOnClickListener(mDetailListener.getGotoFloorListener());
                        tvNote.setFocusable(false);
                        tvNote.setClickable(true);
                    } else {
                        tvNote.setOnClickListener(null);
                    }

                    contentView.addView(quoteLayout);
                    trimBr = true;
                }
            }
        }

        renderPollLayout(holder, detail);

        renderCommnetLayout(holder, detail);

        renderRateLayout(holder, detail);
    }

    private void renderRateLayout(ViewHolderImpl holder, DetailBean detail) {
        if (detail.getRateListBean() != null && detail.getRateListBean().getRates().size() > 0) {
            holder.ratingView.setVisibility(View.VISIBLE);
            holder.ratingView.removeAllViews();
            RateListBean listBean = detail.getRateListBean();
            List<RateBean> rates = listBean.getRates();

            TextView tvHeader = new TextView(mCtx);
            String header = "评分 "
                    + (Utils.getIntFromString(listBean.getTotalScore1()) > 0
                    ? " · " + listBean.getTotalScore1() : "")
                    + (Utils.getIntFromString(listBean.getTotalScore2()) > 0
                    ? " · " + listBean.getTotalScore2() : "")
                    + (Utils.getIntFromString(listBean.getTotalScore3()) > 0
                    ? " · " + listBean.getTotalScore3() : "");
            tvHeader.setText(header);
            tvHeader.setTextColor(ContextCompat.getColor(mCtx, R.color.comment_title_font_color));
            tvHeader.setBackgroundColor(ContextCompat.getColor(mCtx, R.color.rating_header_background_color));
            tvHeader.setTextSize(HiSettingsHelper.getInstance().getPostTextSize() - 1);
            tvHeader.setGravity(Gravity.CENTER);
            tvHeader.setPadding(8, 12, 8, 12);
            holder.ratingView.addView(tvHeader);

            int i = 0;
            for (RateBean bean : rates) {
                TextViewWithEmoticon tv = new TextViewWithEmoticon(mCtx);
                tv.setTextSize(HiSettingsHelper.getInstance().getPostTextSize() - 1);
                tv.setPadding(8, 12, 8, 12);
                tv.setRichText(bean.toHtml());
                if (i % 2 == 1)
                    tv.setBackgroundColor(ContextCompat.getColor(mCtx, R.color.rating_background_color));
                holder.ratingView.addView(tv);
                i++;
            }
        } else {
            holder.ratingView.setVisibility(View.GONE);
        }
    }

    private void renderCommnetLayout(ViewHolderImpl holder, DetailBean detail) {
        if (detail.getCommentLists() != null && detail.getCommentLists() != null) {
            holder.commentView.setVisibility(View.VISIBLE);
            holder.commentView.removeAllViews();
            CommentListBean commentListBean = detail.getCommentLists();
            List<CommentBean> comments = commentListBean.getComments();

            TextView tvHeader = new TextView(mCtx);
            tvHeader.setPadding(8, 12, 8, 12);
            tvHeader.setText("点评");
            tvHeader.setTextSize(HiSettingsHelper.getInstance().getPostTextSize() - 1);
            tvHeader.setTextColor(ContextCompat.getColor(mCtx, R.color.comment_title_font_color));
            tvHeader.setBackgroundColor(ColorHelper.getCommentTitleBackgroundColor(mCtx));
            tvHeader.setGravity(Gravity.CENTER);
            tvHeader.setTag(detail);
            tvHeader.setOnClickListener(mDetailListener.getViewAllCommemtsLisener());
            holder.commentView.addView(tvHeader);

            int i = 0;
            for (CommentBean bean : comments) {
                TextViewWithEmoticon tv = new TextViewWithEmoticon(mCtx);
                tv.setTextSize(HiSettingsHelper.getInstance().getPostTextSize() - 1);
                if (i % 2 == 1)
                    tv.setBackgroundColor(ContextCompat.getColor(mCtx, R.color.comment_background_color));
                tv.setPadding(8, 8, 8, 8);
                tv.setRichText(bean.toHtml());
                holder.commentView.addView(tv);
                i++;
            }
            if (commentListBean.isHasNextPage()) {
                TextView tvFooter = new TextView(mCtx);
                tvFooter.setPadding(8, 12, 8, 12);
                tvFooter.setText("查看全部点评");
                tvFooter.setTextSize(HiSettingsHelper.getInstance().getPostTextSize() - 1);
                tvFooter.setTextColor(ContextCompat.getColor(mCtx, R.color.comment_title_font_color));
                tvFooter.setBackgroundColor(ColorHelper.getCommentTitleBackgroundColor(mCtx));
                tvFooter.setGravity(Gravity.CENTER);
                tvFooter.setTag(detail);
                tvFooter.setOnClickListener(mDetailListener.getViewAllCommemtsLisener());
                holder.commentView.addView(tvFooter);
            }
        } else {
            holder.commentView.setVisibility(View.GONE);
        }
    }

    private void renderPollLayout(ViewHolderImpl holder, DetailBean detail) {
        if (detail.getPoll() != null) {
            holder.pollView.setVisibility(View.VISIBLE);
            holder.pollView.removeAllViews();

            mPollButtons = new WeakHashMap<>();

            final PollBean pollBean = detail.getPoll();
            TextViewWithEmoticon tvPoll = new TextViewWithEmoticon(mCtx);
            tvPoll.setRichText(pollBean.getTitle());
            tvPoll.setPadding(8, 12, 8, 12);
            tvPoll.setTextSize(HiSettingsHelper.getInstance().getPostTextSize() - 1);
            holder.pollView.addView(tvPoll);

            List<PollOptionBean> options = pollBean.getPollOptions();
            if (options != null && options.size() > 1) {
                boolean voteable = !TextUtils.isEmpty(options.get(0).getOptionId());
                int i = 0;
                for (PollOptionBean option : options) {
                    RelativeLayout optionLayout;
                    if (option.getImage() == null) {
                        optionLayout = (RelativeLayout) mInflater.inflate(R.layout.item_poll_option, null, false);
                    } else {
                        optionLayout = (RelativeLayout) mInflater.inflate(R.layout.item_poll_option_image, null, false);
                    }

                    CheckBox checkBox = (CheckBox) optionLayout.findViewById(R.id.cb_option);
                    RadioButton radioButton = (RadioButton) optionLayout.findViewById(R.id.rb_option);
                    TextView tvText = (TextView) optionLayout.findViewById(R.id.tv_text);
                    TextView tvRates = (TextView) optionLayout.findViewById(R.id.tv_rates);
                    ImageLayout imageLayout = (ImageLayout) optionLayout.findViewById(R.id.image_layout);

                    if (voteable) {
                        voteable = !TextUtils.isEmpty(option.getOptionId());
                        if (pollBean.getMaxAnswer() > 1) {
                            checkBox.setVisibility(View.VISIBLE);
                            radioButton.setVisibility(View.GONE);
                            tvText.setVisibility(View.GONE);
                            checkBox.setText(option.getText());
                            checkBox.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
                            checkBox.setTextColor(ColorHelper.getTextColorPrimary(mCtx));
                            checkBox.setTag(option.getOptionId());
                            mPollButtons.put(checkBox, null);
                        } else {
                            checkBox.setVisibility(View.GONE);
                            radioButton.setVisibility(View.VISIBLE);
                            tvText.setVisibility(View.GONE);
                            radioButton.setText(option.getText());
                            radioButton.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
                            radioButton.setTextColor(ColorHelper.getTextColorPrimary(mCtx));
                            radioButton.setTag(option.getOptionId());
                            radioButton.setOnClickListener(mOnRadioCheckedListener);
                            mPollButtons.put(radioButton, null);
                        }
                    } else {
                        checkBox.setVisibility(View.GONE);
                        radioButton.setVisibility(View.GONE);
                        tvText.setText(option.getText());
                        tvText.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
                    }
                    if (imageLayout != null) {
                        imageLayout.setUrl(option.getImage().getThumbUrl());
                        imageLayout.setGlide(Glide.with(mDetailFragment));
                        imageLayout.setImageInfo(i++, pollBean.getImages());
                    }

                    tvRates.setText(option.getRates());
                    tvRates.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());

                    holder.pollView.addView(optionLayout);

                }

                if (!TextUtils.isEmpty(pollBean.getFooter())) {
                    TextView footer = new TextView(mCtx);
                    footer.setPadding(8, 8, 8, 8);
                    footer.setText(pollBean.getFooter());
                    footer.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
                    holder.pollView.addView(footer);
                }

                if (voteable) {
                    TextView button = new TextView(mCtx);
                    button.setText("投票");
                    button.setTextColor(ContextCompat.getColor(mCtx, R.color.md_white_1000));
                    button.setTextSize(HiSettingsHelper.getInstance().getPostTextSize() - 1);
                    ViewCompat.setBackground(button, ContextCompat.getDrawable(mCtx, R.drawable.lable_background));
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Utils.dpToPx(mCtx, 40));
                    layoutParams.setMargins(8, 8, 8, 8);
                    button.setLayoutParams(layoutParams);
                    button.setGravity(Gravity.CENTER);
                    button.setOnClickListener(new OnSingleClickListener() {
                        @Override
                        public void onSingleClick(View v) {
                            List<String> values = new ArrayList<>();
                            for (CompoundButton button : mPollButtons.keySet()) {
                                if (button.isChecked()) {
                                    values.add(button.getTag().toString());
                                }
                            }
                            if (values.size() == 0) {
                                UIUtils.toast("请选择选项");
                            } else if (values.size() > pollBean.getMaxAnswer()) {
                                UIUtils.toast("最多只能选择 " + pollBean.getMaxAnswer() + " 个选项");
                            } else {
                                v.setTag(values);
                                mDetailListener.getVotePollListener().onClick(v);
                            }
                        }
                    });
                    holder.pollView.addView(button);
                }
            }
        } else {
            holder.pollView.setVisibility(View.GONE);
        }
    }

    private void loadAvatar(final String avatarUrl, final ImageView imageView) {
        GlideHelper.loadAvatar(mDetailFragment, imageView, avatarUrl);
    }

    public int getPositionByFloor(int floor) {
        List<DetailBean> datas = getDatas();
        for (int i = 0; i < datas.size(); i++) {
            DetailBean bean = datas.get(i);
            if (bean.getFloor() == floor) {
                return i + getHeaderCount();
            }
        }
        return -1;
    }

    public int getPositionByPostId(String postId) {
        List<DetailBean> datas = getDatas();
        for (int i = 0; i < datas.size(); i++) {
            DetailBean bean = datas.get(i);
            if (bean.getPostId().equals(postId)) {
                return i + getHeaderCount();
            }
        }
        return -1;
    }

    private static class ViewHolderImpl extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView author;
        TextView nickname;
        TextView floor;
        TextView time;
        LinearLayout contentView;
        LinearLayout pollView;
        LinearLayout commentView;
        LinearLayout ratingView;
        ImageView ivMenu;
        ImageView ivComment;
        ImageView ivReply;
        ImageView ivClient;

        ViewHolderImpl(View itemView) {
            super(itemView);
            avatar = (ImageView) itemView.findViewById(R.id.iv_avatar);
            author = (TextView) itemView.findViewById(R.id.tv_author);
            nickname = (TextView) itemView.findViewById(R.id.tv_nickname);
            time = (TextView) itemView.findViewById(R.id.time);
            floor = (TextView) itemView.findViewById(R.id.floor);
            contentView = (LinearLayout) itemView.findViewById(R.id.content_layout);
            pollView = (LinearLayout) itemView.findViewById(R.id.poll_layout);
            commentView = (LinearLayout) itemView.findViewById(R.id.comment_layout);
            ratingView = (LinearLayout) itemView.findViewById(R.id.rating_layout);
            ivMenu = (ImageView) itemView.findViewById(R.id.iv_menu);
            ivComment = (ImageView) itemView.findViewById(R.id.iv_comment);
            ivReply = (ImageView) itemView.findViewById(R.id.iv_reply);
            ivClient = (ImageView) itemView.findViewById(R.id.iv_client);
        }
    }

}
