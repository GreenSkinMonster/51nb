package com.greenskinmonster.a51nb.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ThreadListBean {

    private String mFormhash;
    private List<ThreadBean> mThreads = new ArrayList<>();
    private Map<String, String> mTypes;

    public ThreadListBean() {
    }

    public void add(ThreadBean thread) {
        mThreads.add(thread);
    }

    public int getCount() {
        return mThreads.size();
    }

    public List<ThreadBean> getThreads() {
        return mThreads;
    }

    public void setThreads(List<ThreadBean> threads) {
        mThreads = threads;
    }

    public String getFormhash() {
        return mFormhash;
    }

    public void setFormhash(String formhash) {
        mFormhash = formhash;
    }

    public Map<String, String> getTypes() {
        return mTypes;
    }

    public void setTypes(Map<String, String> types) {
        mTypes = types;
    }
}
