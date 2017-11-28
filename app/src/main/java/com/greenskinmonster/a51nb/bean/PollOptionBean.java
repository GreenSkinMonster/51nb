package com.greenskinmonster.a51nb.bean;

/**
 * Created by GreenSkinMonster on 2017-08-20.
 */

public class PollOptionBean {

    private String mOptionId = "";
    private String mText = "";
    private String mRates = "";
    private ContentImg mImage;

    public String getOptionId() {
        return mOptionId;
    }

    public void setOptionId(String optionId) {
        mOptionId = optionId;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public String getRates() {
        return mRates;
    }

    public void setRates(String rates) {
        mRates = rates;
    }

    public ContentImg getImage() {
        return mImage;
    }

    public void setImage(ContentImg image) {
        mImage = image;
    }
}
