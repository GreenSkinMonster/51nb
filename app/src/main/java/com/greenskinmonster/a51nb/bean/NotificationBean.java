package com.greenskinmonster.a51nb.bean;

/**
 * bean for notification
 * Created by GreenSkinMonster on 2015-09-08.
 */
public class NotificationBean {
    private int mSmsCount;
    private int mThreadCount;
    private int mSysNotiCount;
    private String mUsername;
    private String mUid;
    private String mContent;
    private boolean mHasSms;
    private boolean mQiandao;

    public int getSmsCount() {
        return mSmsCount;
    }

    public void setSmsCount(int smsCount) {
        this.mSmsCount = smsCount;
    }

    public void clearSmsCount() {
        this.mSmsCount = 0;
        this.mUid = "";
        this.mUsername = "";
        this.mContent = "";
    }

    public void clearNotiCount() {
        mThreadCount = 0;
        mSysNotiCount = 0;
    }

    public int getThreadCount() {
        return mThreadCount;
    }

    public void setThreadCount(int threadCount) {
        this.mThreadCount = threadCount;
    }

    public String toString() {
        return "SMS=" + mSmsCount + ", THREAD=" + mThreadCount;
    }

    public boolean hasNew() {
        return mSmsCount > 0 || mThreadCount > 0 || mHasSms || mQiandao;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        this.mUid = uid;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        this.mUsername = username;
    }

    public boolean isHasSms() {
        return mHasSms;
    }

    public void setHasSms(boolean hasSms) {
        mHasSms = hasSms;
    }

    public boolean isQiandao() {
        return mQiandao;
    }

    public void setQiandao(boolean qiandao) {
        mQiandao = qiandao;
    }

    public int getSysNotiCount() {
        return mSysNotiCount;
    }

    public void setSysNotiCount(int sysNotiCount) {
        mSysNotiCount = sysNotiCount;
    }

    public int getTotalNotiCount() {
        return getThreadCount() + getSysNotiCount();
    }

}
