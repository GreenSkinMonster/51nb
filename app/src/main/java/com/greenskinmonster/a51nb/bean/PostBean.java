package com.greenskinmonster.a51nb.bean;

/**
 * Used for post arguments
 * Created by GreenSkinMonster on 2015-03-14.
 */
public class PostBean {

    private String mTid;
    private String mPid;
    private int mFid;
    private int mFloor;
    private String mSubject;
    private String mContent;
    private String mTypeid;
    private int mStatus;
    private String mMessage;
    private int mPage;
    private String mFormhash;
    private DetailListBean mDetailListBean;
    private CommentListBean mCommentListBean;

    public String getTid() {
        return mTid;
    }

    public void setTid(String tid) {
        this.mTid = tid;
    }

    public String getPid() {
        return mPid;
    }

    public void setPid(String pid) {
        this.mPid = pid;
    }

    public int getFid() {
        return mFid;
    }

    public void setFid(int fid) {
        this.mFid = fid;
    }

    public int getFloor() {
        return mFloor;
    }

    public void setFloor(int floor) {
        this.mFloor = floor;
    }

    public String getSubject() {
        return mSubject;
    }

    public void setSubject(String subject) {
        this.mSubject = subject;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }

    public String getTypeid() {
        return mTypeid;
    }

    public void setTypeid(String typeid) {
        this.mTypeid = typeid;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        this.mMessage = message;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        this.mStatus = status;
    }

    public int getPage() {
        return mPage;
    }

    public void setPage(int page) {
        mPage = page;
    }

    public String getFormhash() {
        return mFormhash;
    }

    public void setFormhash(String formhash) {
        mFormhash = formhash;
    }

    public DetailListBean getDetailListBean() {
        return mDetailListBean;
    }

    public void setDetailListBean(DetailListBean detailListBean) {
        this.mDetailListBean = detailListBean;
    }

    public CommentListBean getCommentListBean() {
        return mCommentListBean;
    }

    public void setCommentListBean(CommentListBean commentListBean) {
        mCommentListBean = commentListBean;
    }

}
