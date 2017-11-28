package com.greenskinmonster.a51nb.bean;

import com.greenskinmonster.a51nb.utils.HiUtils;

import java.util.Map;

public class UserInfoBean {

    private String mUsername;
    private String mUid;
    private String mFormhash;
    private Map<String, String> mInfos;
    private boolean online;

    public UserInfoBean() {
    }

    public String getAvatarUrl() {
        return HiUtils.getAvatarUrlByUid(mUid);
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String mUid) {
        this.mUid = mUid;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String mUsername) {
        this.mUsername = mUsername;
    }

    public String getFormhash() {
        return mFormhash;
    }

    public void setFormhash(String formhash) {
        mFormhash = formhash;
    }

    public Map<String, String> getInfos() {
        return mInfos;
    }

    public void setInfos(Map<String, String> infos) {
        mInfos = infos;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
