package com.greenskinmonster.a51nb.bean;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * bean for getting pre post info
 * Created by GreenSkinMonster on 2015-04-15.
 */
public class PrePostInfoBean {
    private String mFormhash;
    private String mHash;
    private String mSubject;
    private String mText;
    private String mQuoteText;
    private String mTypeId;
    private String mTopic;
    private String mNoticeAuthor;
    private String mNoticeAuthorMsg;
    private String mNoticeTrimStr;
    private List<String> mAttaches = new ArrayList<>(0);
    private List<String> mNewAttaches = new ArrayList<>(0);
    private List<String> mDeleteAttaches = new ArrayList<>(0);
    private List<String> mAllImages = new ArrayList<>(0);
    private Map<String, String> mTypeValues = new LinkedHashMap<>();
    private Map<String, String> mTopicValues = new LinkedHashMap<>();
    private boolean mTypeRequired = false;
    private String mReadPerm;
    private Map<String, String> mReadPerms;

    private int mExtCredit = -1;
    private int mCreditTimes = 0;
    private int mCreditMemberTimes = 0;
    private int mCreditRandom = 0;
    private int mCreditLeft = 0;

    private String mSpecial;

    private String itemName;
    private String itemLocus;
    private String itemNumber = "1";
    private String itemPrice;
    private String itemQuality;
    private String itemExpiration;
    private String transport;
    private String itemCostPrice;
    private String itemCredit;
    private String itemCostCredit;
    private String paymethod;

    private String mPollMaxChoices;
    private String mPollDays;
    private boolean mPollVisibility;
    private boolean mPollOvert;
    private List<String> mPollChoices = new ArrayList<>(0);


    public List<String> getDeleteAttaches() {
        return mDeleteAttaches;
    }

    public void addDeleteAttach(String attach) {
        if (!mDeleteAttaches.contains(attach))
            mDeleteAttaches.add(attach);
    }

    public List<String> getNewAttaches() {
        return mNewAttaches;
    }

    public void addNewAttach(String attach) {
        if (!mNewAttaches.contains(attach))
            mNewAttaches.add(attach);
    }

    public List<String> getAttaches() {
        return mAttaches;
    }

    public void addAttach(String attach) {
        if (!mAttaches.contains(attach))
            mAttaches.add(attach);
    }

    public String getFormhash() {
        return mFormhash;
    }

    public void setFormhash(String formhash) {
        this.mFormhash = formhash;
    }

    public String getHash() {
        return mHash;
    }

    public void setHash(String hash) {
        this.mHash = hash;
    }

    public String getSubject() {
        return mSubject;
    }

    public void setSubject(String subject) {
        this.mSubject = subject;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        this.mText = text;
    }

    public String getQuoteText() {
        return mQuoteText;
    }

    public void setQuoteText(String quoteText) {
        mQuoteText = quoteText;
    }

    public String getTypeId() {
        return mTypeId;
    }

    public void setTypeId(String typeId) {
        this.mTypeId = typeId;
    }

    public String getTopic() {
        return mTopic;
    }

    public void setTopic(String topic) {
        mTopic = topic;
    }

    public Map<String, String> getTopicValues() {
        return mTopicValues;
    }

    public void setTopicValues(Map<String, String> topicValues) {
        mTopicValues = topicValues;
    }

    public List<String> getAllImages() {
        return mAllImages;
    }

    public void addImage(String imgId) {
        if (!mAllImages.contains(imgId))
            mAllImages.add(imgId);
    }

    public String getNoticeAuthor() {
        return mNoticeAuthor;
    }

    public void setNoticeAuthor(String noticeAuthor) {
        this.mNoticeAuthor = noticeAuthor;
    }

    public String getNoticeAuthorMsg() {
        return mNoticeAuthorMsg;
    }

    public void setNoticeAuthorMsg(String noticeAuthorMsg) {
        this.mNoticeAuthorMsg = noticeAuthorMsg;
    }

    public String getNoticeTrimStr() {
        return mNoticeTrimStr;
    }

    public void setNoticeTrimStr(String noticeTrimStr) {
        this.mNoticeTrimStr = noticeTrimStr;
    }

    public Map<String, String> getTypeValues() {
        return mTypeValues;
    }

    public void setTypeValues(Map<String, String> typeValues) {
        this.mTypeValues = typeValues;
    }

    public boolean isTypeRequired() {
        return mTypeRequired;
    }

    public void setTypeRequired(boolean typeRequired) {
        mTypeRequired = typeRequired;
    }

    public String getReadPerm() {
        return mReadPerm;
    }

    public void setReadPerm(String readPerm) {
        mReadPerm = readPerm;
    }

    public Map<String, String> getReadPerms() {
        return mReadPerms;
    }

    public void setReadPerms(Map<String, String> readPerms) {
        mReadPerms = readPerms;
    }

    public int getExtCredit() {
        return mExtCredit;
    }

    public void setExtCredit(int extCredit) {
        mExtCredit = extCredit;
    }

    public int getCreditTimes() {
        return mCreditTimes;
    }

    public void setCreditTimes(int creditTimes) {
        mCreditTimes = creditTimes;
    }

    public int getCreditMemberTimes() {
        return mCreditMemberTimes;
    }

    public void setCreditMemberTimes(int creditMemberTimes) {
        mCreditMemberTimes = creditMemberTimes;
    }

    public int getCreditRandom() {
        return mCreditRandom;
    }

    public void setCreditRandom(int creditRandom) {
        mCreditRandom = creditRandom;
    }

    public int getCreditLeft() {
        return mCreditLeft;
    }

    public void setCreditLeft(int creditLeft) {
        mCreditLeft = creditLeft;
    }

    public String getSpecial() {
        return mSpecial;
    }

    public void setSpecial(String special) {
        mSpecial = special;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemLocus() {
        return itemLocus;
    }

    public void setItemLocus(String itemLocus) {
        this.itemLocus = itemLocus;
    }

    public String getItemNumber() {
        return TextUtils.isEmpty(itemNumber) ? "1" : itemNumber;
    }

    public void setItemNumber(String itemNumber) {
        this.itemNumber = itemNumber;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemQuality() {
        return itemQuality;
    }

    public void setItemQuality(String itemQuality) {
        this.itemQuality = itemQuality;
    }

    public String getItemExpiration() {
        return itemExpiration;
    }

    public void setItemExpiration(String itemExpiration) {
        this.itemExpiration = itemExpiration;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public String getItemCostPrice() {
        return itemCostPrice;
    }

    public void setItemCostPrice(String itemCostPrice) {
        this.itemCostPrice = itemCostPrice;
    }

    public String getItemCredit() {
        return itemCredit;
    }

    public void setItemCredit(String itemCredit) {
        this.itemCredit = itemCredit;
    }

    public String getItemCostCredit() {
        return itemCostCredit;
    }

    public void setItemCostCredit(String itemCostCredit) {
        this.itemCostCredit = itemCostCredit;
    }

    public String getPaymethod() {
        return paymethod;
    }

    public void setPaymethod(String paymethod) {
        this.paymethod = paymethod;
    }

    public String getPollMaxChoices() {
        return mPollMaxChoices;
    }

    public void setPollMaxChoices(String pollMaxChoices) {
        mPollMaxChoices = pollMaxChoices;
    }

    public String getPollDays() {
        return mPollDays;
    }

    public void setPollDays(String pollDays) {
        mPollDays = pollDays;
    }

    public boolean isPollVisibility() {
        return mPollVisibility;
    }

    public void setPollVisibility(boolean pollVisibility) {
        mPollVisibility = pollVisibility;
    }

    public boolean isPollOvert() {
        return mPollOvert;
    }

    public void setPollOvert(boolean pollOvert) {
        mPollOvert = pollOvert;
    }

    public List<String> getPollChoices() {
        return mPollChoices;
    }

    public void setPollChoices(List<String> pollChoices) {
        mPollChoices = pollChoices;
    }
}
