package com.greenskinmonster.a51nb.bean;

/**
 * Created by GreenSkinMonster on 2017-08-01.
 */

public class TradeThreadBean extends ThreadBean {

    public final static String TRADER = "商家";
    public final static String MEMBER = "会员";

    private String mTraderType;
    private String mPrice;
    private String mLocation;


    public String getTraderType() {
        return mTraderType;
    }

    public void setTraderType(String traderType) {
        mTraderType = traderType;
    }

    public String getPrice() {
        return mPrice;
    }

    public void setPrice(String price) {
        mPrice = price;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        mLocation = location;
    }
}
