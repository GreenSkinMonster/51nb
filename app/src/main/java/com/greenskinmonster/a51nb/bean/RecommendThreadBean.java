package com.greenskinmonster.a51nb.bean;

/**
 * Created by GreenSkinMonster on 2017-08-01.
 */

public class RecommendThreadBean extends ThreadBean {

    private String mPostInfo;
    private String mContent;
    private String mItemImageUrl;
    private String mItemUrl;

    public String getPostInfo() {
        return mPostInfo;
    }

    public void setPostInfo(String postInfo) {
        mPostInfo = postInfo;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public String getItemImageUrl() {
        return mItemImageUrl;
    }

    public void setItemImageUrl(String itemImageUrl) {
        mItemImageUrl = itemImageUrl;
    }

    public String getItemUrl() {
        return mItemUrl;
    }

    public void setItemUrl(String itemUrl) {
        mItemUrl = itemUrl;
    }
}
