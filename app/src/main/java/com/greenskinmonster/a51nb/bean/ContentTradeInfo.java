package com.greenskinmonster.a51nb.bean;

import java.util.Map;

/**
 * Created by GreenSkinMonster on 2017-08-08.
 */

public class ContentTradeInfo extends ContentAbs {

    private Map<String, String> mTradeInfo;

    @Override
    public String getContent() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : mTradeInfo.entrySet()) {
            sb.append(entry.getKey()).append(" ").append(entry.getValue()).append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String getCopyText() {
        return getContent();
    }

    public Map<String, String> getTradeInfo() {
        return mTradeInfo;
    }

    public void setTradeInfo(Map<String, String> tradeInfo) {
        mTradeInfo = tradeInfo;
    }
}
