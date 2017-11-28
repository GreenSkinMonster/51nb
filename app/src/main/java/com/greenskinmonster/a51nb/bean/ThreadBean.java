package com.greenskinmonster.a51nb.bean;


import com.greenskinmonster.a51nb.async.PostHelper;
import com.greenskinmonster.a51nb.utils.HiUtils;

public class ThreadBean {

    private String mTitle;
    private String mTitleColor;
    private String mTid;
    private String mForum;
    private int mFid;

    private String mAuthor;
    private String mAuthorId;
    private String mCreateTime;

    private String mReplier;
    private String mReplierId;
    private String mReplyTime;

    private int mViewCount;
    private int mReplyCount;

    private boolean mHaveAttach;
    private boolean mHaveImage;
    private boolean mNew;
    private boolean mLocked;
    private boolean mStick;
    private String mSpecial;

    private int mMaxPage;
    private String mReadPerm;
    private String mCredit;
    private int mCreditLeft;

    public ThreadBean() {
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getTitleColor() {
        return mTitleColor;
    }

    public void setTitleColor(String titleColor) {
        this.mTitleColor = titleColor;
    }

    public String getTid() {
        return mTid;
    }

    public void setTid(String tid) {
        this.mTid = tid;
    }

    public boolean isStick() {
        return mStick;
    }

    public void setStick(boolean stick) {
        this.mStick = stick;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        this.mAuthor = author;
    }

    public String getAuthorId() {
        return mAuthorId;
    }

    public void setAuthorId(String authorId) {
        this.mAuthorId = authorId;
    }

    public int getViewCount() {
        return mViewCount;
    }

    public void setViewCount(int viewCount) {
        mViewCount = viewCount;
    }

    public int getReplyCount() {
        return mReplyCount;
    }

    public void setReplyCount(int replyCount) {
        mReplyCount = replyCount;
    }

    public String getReplier() {
        return mReplier;
    }

    public void setReplier(String replier) {
        this.mReplier = replier;
    }

    public String getReplierId() {
        return mReplierId;
    }

    public void setReplierId(String replierId) {
        mReplierId = replierId;
    }

    public String getReplyTime() {
        return mReplyTime;
    }

    public void setReplyTime(String replyTime) {
        mReplyTime = replyTime;
    }

    public String getCreateTime() {
        return mCreateTime;
    }

    public void setCreateTime(String createTime) {
        this.mCreateTime = createTime;
    }

    public boolean getHaveAttach() {
        return mHaveAttach;
    }

    public void setHaveAttach(boolean haveAttach) {
        this.mHaveAttach = haveAttach;
    }

    public boolean getHaveImage() {
        return mHaveImage;
    }

    public void setHaveImage(boolean haveImage) {
        this.mHaveImage = haveImage;
    }

    public boolean isNew() {
        return mNew;
    }

    public void setNew(boolean isNew) {
        this.mNew = isNew;
    }

    public boolean isLocked() {
        return mLocked;
    }

    public void setLocked(boolean locked) {
        mLocked = locked;
    }

    public String getAvatarUrl() {
        return HiUtils.getAvatarUrlByUid(mAuthorId);
    }

    public int getMaxPage() {
        return mMaxPage;
    }

    public void setMaxPage(int lastPage) {
        this.mMaxPage = lastPage;
    }

    public String getForum() {
        return mForum;
    }

    public void setForum(String forum) {
        mForum = forum;
    }

    public int getFid() {
        return mFid;
    }

    public void setFid(int fid) {
        mFid = fid;
    }

    public String getSpecial() {
        return mSpecial;
    }

    public void setSpecial(String special) {
        mSpecial = special;
    }

    public String getReadPerm() {
        return mReadPerm;
    }

    public void setReadPerm(String readPerm) {
        mReadPerm = readPerm;
    }

    public String getCredit() {
        return mCredit;
    }

    public void setCredit(String credit) {
        mCredit = credit;
    }

    public int getCreditLeft() {
        return mCreditLeft;
    }

    public void setCreditLeft(int creditLeft) {
        mCreditLeft = creditLeft;
    }

    public boolean isPoll() {
        return PostHelper.SPECIAL_POLL.equals(mSpecial);
    }

    public boolean isTrade() {
        return PostHelper.SPECIAL_TRADE.equals(mSpecial);
    }

}
