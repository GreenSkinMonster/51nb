package com.greenskinmonster.a51nb.bean;

import com.greenskinmonster.a51nb.ui.HiApplication;
import com.greenskinmonster.a51nb.utils.ColorHelper;

/**
 * Created by GreenSkinMonster on 2017-08-05.
 */

public class CommentBean {

    private String mTocId;
    private String mUid;
    private String mAuthor;
    private String mConent;
    private String mTime;

    public String getTocId() {
        return mTocId;
    }

    public void setTocId(String tocId) {
        mTocId = tocId;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        mUid = uid;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        mAuthor = author;
    }

    public String getConent() {
        return mConent;
    }

    public void setConent(String conent) {
        mConent = conent;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public String toHtml() {
        return "<font color=" + ColorHelper.getColorAccent(HiApplication.getAppContext()) + ">" + getAuthor() + "</font> " + getConent();
    }
}
