package com.greenskinmonster.a51nb.bean;

import java.util.List;

/**
 * Created by GreenSkinMonster on 2017-08-05.
 */

public class CommentListBean {

    public final static int MAX_COMMENTS_IN_PAGE = 5;

    private int mPage;
    private List<CommentBean> mComments;
    private boolean hasNextPage;

    public int getPage() {
        return mPage;
    }

    public void setPage(int page) {
        mPage = page;
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    public List<CommentBean> getComments() {
        return mComments;
    }

    public void setComments(List<CommentBean> comments) {
        mComments = comments;
    }
}
