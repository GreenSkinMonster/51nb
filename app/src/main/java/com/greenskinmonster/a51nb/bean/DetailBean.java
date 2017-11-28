package com.greenskinmonster.a51nb.bean;

import com.greenskinmonster.a51nb.ui.ThreadDetailFragment;
import com.greenskinmonster.a51nb.ui.textstyle.TextStyle;
import com.greenskinmonster.a51nb.utils.HiUtils;
import com.greenskinmonster.a51nb.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;

public class DetailBean {

    public final static int CLIENT_ANDROID = 1;
    public final static int CLIENT_IOS = 2;

    private String mAuthor;
    private String mUid;
    private int mOnlineStatus = -1;
    private String mNickname;
    private String mPostId;
    private String mTimePost;
    private int mFloor;
    private Contents mContents;
    private int mPage;
    private Collection<ContentImg> mImages = new ArrayList<>();
    private boolean mSelectMode;
    private boolean mHighlightMode;
    private CommentListBean mCommentLists;
    private RateListBean mRateListBean;
    private boolean mRateable;
    private boolean mCommentable;
    private boolean mSupportable;
    private int mSupportCount;
    private int mAgainstCount;
    private PollBean mPoll;
    private int mClientType;
    private String mClientUrl;
    private boolean mThreadAuthor;

    public DetailBean() {
        mContents = new Contents();
    }

    public class Contents {
        private ArrayList<ContentAbs> list;
        private int lastTextIdx;
        private Boolean newString;

        public Contents() {
            list = new ArrayList<>();
            lastTextIdx = -1;
            newString = true;
        }

        public void addText(String text) {
            addText(text, null);
        }

        public void addText(String text, TextStyle textStyle) {
            if (textStyle != null)
                text = textStyle.toHtml(text);
            if (newString) {
                ContentText ct = new ContentText(text);
                list.add(ct);
                lastTextIdx = list.size() - 1;
                newString = false;
            } else {
                ContentText ct = (ContentText) list.get(lastTextIdx);
                ct.append(text);
            }
        }

        public void addNotice(String text) {
            ContentText ct = new ContentNotice(text);
            list.add(ct);
            lastTextIdx = list.size() - 1;
            newString = true;
        }

        public void addInfo(String text) {
            ContentInfo ct = new ContentInfo(text);
            list.add(ct);
            lastTextIdx = list.size() - 1;
            newString = true;
        }

        public void addTradeInfo(ContentTradeInfo tradeInfo) {
            list.add(tradeInfo);
            lastTextIdx = list.size() - 1;
            newString = true;
        }

        public void addLink(String text, String url) {
            String link;
            if (!url.toLowerCase().startsWith("http://")
                    && !url.toLowerCase().startsWith("https://")) {
                url = "http://" + url;
                link = " <a href=\"" + url + "\">" + text + "</a> ";
            } else {
                link = "<a href=\"" + url + "\">" + text + "</a>";
            }
            if (newString) {
                list.add(new ContentText(link));
                lastTextIdx = list.size() - 1;
                newString = false;
            } else {
                ContentText ct = (ContentText) list.get(lastTextIdx);
                ct.append(link);
            }
        }

        public void addEmail(String email) {
            String link = " <a href=\"mailto:" + email + "\">" + email + "</a> ";
            if (newString) {
                list.add(new ContentText(link));
                lastTextIdx = list.size() - 1;
                newString = false;
            } else {
                ContentText ct = (ContentText) list.get(lastTextIdx);
                ct.append(link);
            }
        }

        public void addImg(String url) {
            addImg(new ContentImg(url, 0, ""));
        }

        public void addImg(ContentImg contentImg) {
            list.add(contentImg);
            mImages.add(contentImg);
            newString = true;
        }

        public void addAttach(ContentAttach attach) {
            list.add(attach);
            newString = true;
        }

        public void addQuote(String text, String author, String postTime, String tid, String postId) {
            list.add(new ContentQuote(text, author, postTime, tid, postId));
            newString = true;
        }

        public void addGoToFloor(String text, String tid, String postId, int floor, String author) {
            list.add(new ContentGoToFloor(text, tid, postId, floor, author));
            newString = true;
        }

        public int getSize() {
            return list.size();
        }

        public ContentAbs get(int idx) {
            return list.get(idx);
        }

        public String getCopyText() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                ContentAbs o = list.get(i);
                if (o instanceof ContentText
                        || o instanceof ContentQuote
                        || o instanceof ContentTradeInfo)
                    sb.append(o.getCopyText());
            }
            return Utils.trim(sb.toString());
        }

        public String getContent() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                ContentAbs o = list.get(i);
                if (o instanceof ContentText)
                    sb.append(o.getContent());
            }
            return sb.toString();
        }
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String mAuthor) {
        this.mAuthor = mAuthor;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String mUid) {
        this.mUid = mUid;
    }

    public int getOnlineStatus() {
        return mOnlineStatus;
    }

    public String getNickname() {
        return mNickname;
    }

    public void setNickname(String nickname) {
        mNickname = nickname;
    }

    public void setOnlineStatus(int onlineStatus) {
        mOnlineStatus = onlineStatus;
    }

    public String getPostId() {
        return mPostId;
    }

    public void setPostId(String mPostId) {
        this.mPostId = mPostId;
    }

    public String getTimePost() {
        return mTimePost;
    }

    public void setTimePost(String mTimePost) {
        this.mTimePost = mTimePost;
    }

    public int getFloor() {
        return mFloor;
    }

    public void setFloor(int floor) {
        mFloor = floor;
    }

    public String getFloorText() {
        return mFloor == ThreadDetailFragment.RECOMMEND_FLOOR ? "推荐" : String.valueOf(mFloor);
    }

    public Contents getContents() {
        return mContents;
    }

    public void setContents(Contents contents) {
        this.mContents = contents;
    }

    public Collection<ContentImg> getImages() {
        return mImages;
    }

    public String getAvatarUrl() {
        return HiUtils.getAvatarUrlByUid(mUid);
    }

    public int getPage() {
        return mPage;
    }

    public void setPage(int page) {
        mPage = page;
    }

    public boolean isSelectMode() {
        return mSelectMode;
    }

    public void setSelectMode(boolean selectMode) {
        mSelectMode = selectMode;
    }

    public boolean isHighlightMode() {
        return mHighlightMode;
    }

    public void setHighlightMode(boolean highlightMode) {
        mHighlightMode = highlightMode;
    }

    public CommentListBean getCommentLists() {
        return mCommentLists;
    }

    public void setCommentLists(CommentListBean commentLists) {
        mCommentLists = commentLists;
    }

    public RateListBean getRateListBean() {
        return mRateListBean;
    }

    public void setRateListBean(RateListBean rateListBean) {
        mRateListBean = rateListBean;
    }

    private String unEscapeHtml(String str) {
        str = str.replaceAll("&nbsp;", " ");
        str = str.replaceAll("&quot;", "\"");
        str = str.replaceAll("&amp;", "&");
        str = str.replaceAll("&lt;", "<");
        str = str.replaceAll("&gt;", ">");

        return str;
    }

    public boolean isRateable() {
        return mRateable;
    }

    public void setRateable(boolean rateable) {
        mRateable = rateable;
    }

    public boolean isCommentable() {
        return mCommentable;
    }

    public void setCommentable(boolean commentable) {
        mCommentable = commentable;
    }

    public int getSupportCount() {
        return mSupportCount;
    }

    public void setSupportCount(int supportCount) {
        mSupportCount = supportCount;
    }

    public int getAgainstCount() {
        return mAgainstCount;
    }

    public void setAgainstCount(int againstCount) {
        mAgainstCount = againstCount;
    }

    public boolean isSupportable() {
        return mSupportable;
    }

    public void setSupportable(boolean supportable) {
        mSupportable = supportable;
    }

    public PollBean getPoll() {
        return mPoll;
    }

    public void setPoll(PollBean poll) {
        mPoll = poll;
    }

    public int getClientType() {
        return mClientType;
    }

    public void setClientType(int clientType) {
        mClientType = clientType;
    }

    public String getClientUrl() {
        return mClientUrl;
    }

    public void setClientUrl(String clientUrl) {
        mClientUrl = clientUrl;
    }

    public boolean isThreadAuthor() {
        return mThreadAuthor;
    }

    public void setThreadAuthor(boolean threadAuthor) {
        mThreadAuthor = threadAuthor;
    }
}
