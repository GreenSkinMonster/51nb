package com.greenskinmonster.a51nb.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.greenskinmonster.a51nb.bean.DetailBean;
import com.greenskinmonster.a51nb.ui.BaseFragment;
import com.greenskinmonster.a51nb.ui.FragmentArgs;
import com.greenskinmonster.a51nb.ui.FragmentUtils;
import com.greenskinmonster.a51nb.ui.ThreadDetailFragment;
import com.greenskinmonster.a51nb.ui.textstyle.HiHtmlTagHandler;
import com.greenskinmonster.a51nb.utils.ColorHelper;
import com.greenskinmonster.a51nb.utils.HiUtils;
import com.greenskinmonster.a51nb.utils.HtmlCompat;
import com.greenskinmonster.a51nb.utils.Logger;
import com.greenskinmonster.a51nb.utils.UIUtils;
import com.greenskinmonster.a51nb.utils.Utils;
import com.vanniktech.emoji.EmojiHandler;

public class TextViewWithEmoticon extends AppCompatTextView {
    private Context mCtx;
    private BaseFragment mFragment;

    private static final long MIN_CLICK_INTERVAL = 600;

    private long mLastClickTime;

    public TextViewWithEmoticon(Context context) {
        super(context);
        mCtx = context;
        init();
    }

    public TextViewWithEmoticon(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCtx = context;
        init();
    }

    private void init() {
        setTextColor(ColorHelper.getTextColorPrimary(mCtx));
        setLinkTextColor(ColorHelper.getColorAccent(mCtx));

        UIUtils.setLineSpacing(this);
    }

    public void setFragment(BaseFragment fragment) {
        mFragment = fragment;
    }

    public void setRichText(CharSequence text) {
        String t = text.toString().trim();
        SpannableStringBuilder b = (SpannableStringBuilder) HtmlCompat.fromHtml(t, imageGetter, new HiHtmlTagHandler());
        for (URLSpan s : b.getSpans(0, b.length(), URLSpan.class)) {
            String url = s.getURL();
            if (url.contains(HiUtils.ForumAttatchUrlPattern)) {
                URLSpan newSpan = getDownloadUrlSpan(url);
                b.setSpan(newSpan, b.getSpanStart(s), b.getSpanEnd(s), b.getSpanFlags(s));
                b.removeSpan(s);
            } else {
                FragmentArgs args = FragmentUtils.parseUrl(url);
                if (args != null) {
                    URLSpan newSpan = getFragmentArgsUrlSpan(url);
                    b.setSpan(newSpan, b.getSpanStart(s), b.getSpanEnd(s), b.getSpanFlags(s));
                    b.removeSpan(s);
                }
            }
        }
        setText(trimSpannable(b));
    }

    private Handler mHandler;

    private Html.ImageGetter imageGetter = new Html.ImageGetter() {
        public Drawable getDrawable(String src) {
            src = Utils.nullToText(src);
            Drawable drawable = TextViewWithEmoticon.this.getDrawable(src);
            if (drawable == null)
                drawable = TextViewWithEmoticon.this.getDrawable2(src);
            return drawable;
        }
    };

    @Nullable
    private Drawable getDrawable(String src) {
        int idx = src.indexOf(HiUtils.SmiliesPattern);
        Drawable icon = null;
        if (idx != -1 && src.indexOf(".", idx) != -1) {
            int lastSlash = src.lastIndexOf("/");
            int lastDot = src.lastIndexOf(".");
            if (lastDot > lastSlash && lastSlash > 0) {
                String s = src.substring(lastSlash + 1, lastDot);
                int id = EmojiHandler.getDrawableResId(s);
                if (id != 0) {
                    icon = ContextCompat.getDrawable(mCtx, id);
                    if (icon != null)
                        icon.setBounds(0, 0, (int) (getLineHeight() * 1.2), (int) (getLineHeight() * 1.2));
                }
            }
        }
        return icon;
    }


    @NonNull
    private Drawable getDrawable2(String source) {
        final LevelListDrawable mDrawable = new LevelListDrawable();
        Glide.with(mCtx).load(source).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                BitmapDrawable d = new BitmapDrawable(mCtx.getResources(), resource);
                mDrawable.addLevel(1, 1, d);
                mDrawable.setBounds(0, 0, (int) (getLineHeight() * 1.2), (int) (getLineHeight() * 1.2));
                mDrawable.setLevel(1);
                if (mHandler == null) {
                    mHandler = new Handler();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setText(getText());
                            invalidate();
                            mHandler = null;
                        }
                    }, 30);
                }
            }
        });
        return mDrawable;
    }

    private SpannableStringBuilder trimSpannable(SpannableStringBuilder spannable) {
        int trimStart = 0;
        int trimEnd = 0;

        String text = spannable.toString();

        while (text.length() > 0 && text.startsWith("\n")) {
            text = text.substring(1);
            trimStart += 1;
        }

        while (text.length() > 0 && text.endsWith("\n")) {
            text = text.substring(0, text.length() - 1);
            trimEnd += 1;
        }

        return spannable.delete(0, trimStart).delete(spannable.length() - trimEnd, spannable.length());
    }

    private URLSpan getFragmentArgsUrlSpan(final String url) {
        return new URLSpan(url) {
            public void onClick(View view) {
                if (mFragment == null) {
                    return;
                }
                FragmentArgs args = FragmentUtils.parseUrl(url);
                if (args != null) {

                    int floor = 0;
                    if (args.getType() == FragmentArgs.TYPE_THREAD
                            && mFragment instanceof ThreadDetailFragment) {
                        //redirect by goto floor in same fragment
                        ThreadDetailFragment detailFragment = (ThreadDetailFragment) mFragment;
                        if (!TextUtils.isEmpty(args.getTid()) && args.getTid().equals(detailFragment.getTid())) {
                            if (args.getFloor() != 0) {
                                floor = args.getFloor();
                            } else if (!TextUtils.isEmpty(args.getPostId())) {
                                //get floor if postId is cached
                                DetailBean detailBean = detailFragment.getCachedPost(args.getPostId());
                                if (detailBean != null)
                                    floor = detailBean.getFloor();
                            } else {
                                floor = 1;
                            }
                        }
                    }

                    if (floor > 0 || floor == ThreadDetailFragment.LAST_FLOOR) {
                        //redirect in same thread
                        ((ThreadDetailFragment) mFragment).gotoFloor(floor);
                    } else {
                        if (args.getType() == FragmentArgs.TYPE_THREAD) {
                            FragmentUtils.showThreadActivity(mFragment.getActivity(), args.isSkipEnterAnim(), args.getTid(), "", args.getPage(), args.getFloor(), args.getPostId(), -1);
                        } else {
                            FragmentUtils.show(mFragment.getActivity(), args);
                        }
                    }
                }
            }
        };
    }

    private URLSpan getDownloadUrlSpan(final String s_url) {
        return new URLSpan(s_url) {
            public void onClick(View view) {
                try {
                    String fileName = "";

                    //clean way to get fileName
                    SpannableStringBuilder b = new SpannableStringBuilder(((TextView) view).getText());
                    URLSpan[] urls = b.getSpans(0, b.length(), URLSpan.class);
                    if (urls.length > 0) {
                        fileName = b.toString().substring(b.getSpanStart(urls[0]), b.getSpanEnd(urls[0]));
                    }
                    if (TextUtils.isEmpty(fileName)) {
                        //failsafe dirty way,  to get rid of ( xxx K ) file size string
                        fileName = ((TextView) view).getText().toString();
                        if (fileName.contains(" ("))
                            fileName = fileName.substring(0, fileName.lastIndexOf(" (")).trim();
                    }
                    UIUtils.toast("开始下载 " + fileName + " ...");
                    Utils.download(mCtx, getURL(), fileName);
                } catch (Exception e) {
                    Logger.e(e);
                    UIUtils.toast("下载出现错误，请使用浏览器下载\n" + e.getMessage());
                }
            }
        };
    }

    /**
     * http://stackoverflow.com/a/17246463/2299887
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = false;
        CharSequence text = getText();
        Spannable stext = Spannable.Factory.getInstance().newSpannable(text);
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= getTotalPaddingLeft();
            y -= getTotalPaddingTop();

            x += getScrollX();
            y += getScrollY();

            Layout layout = getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] link = stext.getSpans(off, off, ClickableSpan.class);

            if (link.length != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    long currentClickTime = System.currentTimeMillis();
                    long elapsedTime = currentClickTime - mLastClickTime;
                    mLastClickTime = currentClickTime;

                    if (elapsedTime > MIN_CLICK_INTERVAL) {
                        try {
                            link[0].onClick(this);
                        } catch (Exception e) {
                            UIUtils.toast("发生错误 : " + e.getMessage());
                        }
                    }
                }
                ret = true;
            }
        }
        return ret;
    }

}
