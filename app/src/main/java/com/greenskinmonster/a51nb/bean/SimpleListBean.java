package com.greenskinmonster.a51nb.bean;

import java.util.ArrayList;
import java.util.List;

public class SimpleListBean {
    private List<SimpleListItemBean> mSimpleListItemBeans = new ArrayList<>();
    private String mSearchId;
    private String mPmid;
    private String mFormhash;
    private int mMaxPage;

    public void add(SimpleListItemBean item) {
        mSimpleListItemBeans.add(item);
    }

    public int getCount() {
        return mSimpleListItemBeans.size();
    }

    public List<SimpleListItemBean> getAll() {
        return mSimpleListItemBeans;
    }

    public String getSearchId() {
        return mSearchId;
    }

    public void setSearchId(String searchId) {
        mSearchId = searchId;
    }

    public int getMaxPage() {
        return mMaxPage;
    }

    public void setMaxPage(int maxPage) {
        mMaxPage = maxPage;
    }

    public String getPmid() {
        return mPmid;
    }

    public void setPmid(String pmid) {
        mPmid = pmid;
    }

    public String getFormhash() {
        return mFormhash;
    }

    public void setFormhash(String formhash) {
        mFormhash = formhash;
    }
}
