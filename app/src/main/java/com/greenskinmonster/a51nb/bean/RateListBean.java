package com.greenskinmonster.a51nb.bean;

import java.util.List;

/**
 * Created by GreenSkinMonster on 2017-08-05.
 */

public class RateListBean {

    private int mRatorCount;
    private String mTotalScore1;
    private String mTotalScore2;
    private String mTotalScore3;
    private List<RateBean> mRates;

    public int getRatorCount() {
        return mRatorCount;
    }

    public void setRatorCount(int ratorCount) {
        mRatorCount = ratorCount;
    }

    public String getTotalScore1() {
        return mTotalScore1;
    }

    public void setTotalScore1(String totalScore1) {
        mTotalScore1 = totalScore1;
    }

    public String getTotalScore2() {
        return mTotalScore2;
    }

    public void setTotalScore2(String totalScore2) {
        mTotalScore2 = totalScore2;
    }

    public String getTotalScore3() {
        return mTotalScore3;
    }

    public void setTotalScore3(String totalScore3) {
        mTotalScore3 = totalScore3;
    }

    public List<RateBean> getRates() {
        return mRates;
    }

    public void setRates(List<RateBean> rates) {
        mRates = rates;
    }
}
