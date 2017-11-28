package com.greenskinmonster.a51nb.bean;

import android.text.TextUtils;

import com.greenskinmonster.a51nb.utils.HtmlCompat;
import com.greenskinmonster.a51nb.utils.Utils;

public class ContentQuote extends ContentAbs {

    private String mAuthor;
    private String mTo;
    private String mTime;
    private String mText;
    private String mPostId;
    private String mTid;

    public ContentQuote(String postText, String author, String postTime, String tid, String postId) {
        mPostId = postId;
        mTid = tid;
        //replace chinese space and trim
        mText = HtmlCompat.fromHtml(postText).toString().replace("　", " ").replace(String.valueOf((char) 160), " ").trim();
        mAuthor = author;
        mTime = postTime;
        if (mText.startsWith("回复")) {
            mText = mText.substring("回复".length()).trim();
            //this is not accurate, will use postId if available
            int idx = mText.indexOf("    ");
            if (idx > 0 && idx < 10) {
                mTo = mText.substring(0, idx).trim();
            } else if (mText.indexOf(" ") > 0) {
                mTo = mText.substring(0, mText.indexOf(" ")).trim();
            }
            if (!TextUtils.isEmpty(mTo))
                mText = mText.substring(mTo.length() + 1).trim();
        }
    }

    @Override
    public String getContent() {
        return mText;
    }

    @Override
    public String getCopyText() {
        return "『" + Utils.fromHtmlAndStrip(mText) + "』";
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        mAuthor = author;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public String getTo() {
        return mTo;
    }

    public void setTo(String to) {
        this.mTo = to;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        this.mText = text;
    }

    public String getPostId() {
        return mPostId;
    }

    public String getTid() {
        return mTid;
    }

//    public boolean isReplyQuote() {
//        return true;
//    }
}
