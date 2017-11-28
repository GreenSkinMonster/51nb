package com.greenskinmonster.a51nb.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GreenSkinMonster on 2017-08-20.
 */

public class PollBean {

    private String mTitle;
    private String mFooter;
    private List<PollOptionBean> mPollOptions;
    private int mMaxAnswer = 1;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getFooter() {
        return mFooter;
    }

    public void setFooter(String footer) {
        mFooter = footer;
    }

    public List<PollOptionBean> getPollOptions() {
        return mPollOptions;
    }

    public void setPollOptions(List<PollOptionBean> pollOptions) {
        mPollOptions = pollOptions;
    }

    public int getMaxAnswer() {
        return mMaxAnswer;
    }

    public void setMaxAnswer(int maxAnswer) {
        mMaxAnswer = maxAnswer;
    }

    public ArrayList<ContentImg> getImages() {
        ArrayList<ContentImg> imgs = new ArrayList<>();
        for (PollOptionBean optionBean : mPollOptions) {
            imgs.add(optionBean.getImage());
        }
        return imgs;
    }
}
