package com.greenskinmonster.a51nb.bean;

import android.text.TextUtils;

import com.greenskinmonster.a51nb.ui.HiApplication;
import com.greenskinmonster.a51nb.utils.ColorHelper;

/**
 * Created by GreenSkinMonster on 2017-08-05.
 */

public class RateBean {

    private String mRator;
    private String mRatorId;
    private String mScore1;
    private String mScore2;
    private String mScore3;
    private String mReason;

    public String getRator() {
        return mRator;
    }

    public void setRator(String rator) {
        mRator = rator;
    }

    public String getRatorId() {
        return mRatorId;
    }

    public void setRatorId(String ratorId) {
        mRatorId = ratorId;
    }

    public String getScore1() {
        return mScore1;
    }

    public void setScore1(String score1) {
        mScore1 = score1;
    }

    public String getScore2() {
        return mScore2;
    }

    public void setScore2(String score2) {
        mScore2 = score2;
    }

    public String getScore3() {
        return mScore3;
    }

    public void setScore3(String score3) {
        mScore3 = score3;
    }

    public String getReason() {
        return mReason;
    }

    public void setReason(String reason) {
        mReason = reason;
    }

    public String toHtml() {
        return "<font color=" + ColorHelper.getColorAccent(HiApplication.getAppContext()) + ">" + getRator() + "</font> "
                + (TextUtils.isEmpty(mScore1) ? "" : "技术分 " + mScore1 + " · ")
                + (TextUtils.isEmpty(mScore2) ? "" : "资产 " + mScore2 + " · ")
                + (TextUtils.isEmpty(mScore3) ? "" : "联谊分 " + mScore3 + " · ")
                + getReason();
    }

}
