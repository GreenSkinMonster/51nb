package com.greenskinmonster.a51nb.bean;

import java.util.ArrayList;
import java.util.List;

public class SimpleListItemBean extends ThreadBean {

    private String mPid;
    private String mInfo;
    private String mPmid;

    private String mSmsSayTo;
    private int mStatus;

    private List<SimplePostItemBean> mPostBeans;

    public String getPid() {
        return mPid;
    }

    public void setPid(String pid) {
        mPid = pid;
    }

    public String getInfo() {
        return mInfo;
    }

    public void setInfo(String info) {
        mInfo = info;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public String getSmsSayTo() {
        return mSmsSayTo;
    }

    public void setSmsSayTo(String smsSayTo) {
        mSmsSayTo = smsSayTo;
    }

    public String getPmid() {
        return mPmid;
    }

    public void setPmid(String pmid) {
        mPmid = pmid;
    }


    public void addPostItem(SimplePostItemBean bean) {
        if (mPostBeans == null)
            mPostBeans = new ArrayList<>(1);
        mPostBeans.add(bean);
    }

    public SimplePostItemBean getPostItem(int i) {
        if (mPostBeans == null)
            mPostBeans = new ArrayList<>(1);
        return mPostBeans.get(i);
    }

    public List<SimplePostItemBean> getPostItems() {
        return mPostBeans;
    }

}
