package com.greenskinmonster.a51nb.bean;

import android.text.TextUtils;

public class ContentAttach extends ContentAbs {
    private String mUrl;
    private String mTitle;
    private String mDesc;

    public ContentAttach(String url, String title, String desc) {
        mUrl = url;
        mTitle = title;
        mDesc = desc;
    }

    @Override
    public String getContent() {
        String cnt = "<a href=\"" + mUrl + "\">" + mTitle + "</a>";
        if (!TextUtils.isEmpty(mDesc))
            cnt += "    " + mDesc;
        return cnt;
    }

    @Override
    public String getCopyText() {
        return "[附件:" + mTitle + "]";
    }

}
