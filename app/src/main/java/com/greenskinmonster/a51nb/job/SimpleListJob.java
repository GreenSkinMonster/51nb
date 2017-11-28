package com.greenskinmonster.a51nb.job;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.greenskinmonster.a51nb.async.LoginHelper;
import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.bean.SearchBean;
import com.greenskinmonster.a51nb.bean.SimpleListBean;
import com.greenskinmonster.a51nb.bean.SimpleListItemBean;
import com.greenskinmonster.a51nb.db.History;
import com.greenskinmonster.a51nb.db.HistoryDao;
import com.greenskinmonster.a51nb.okhttp.NetworkError;
import com.greenskinmonster.a51nb.okhttp.OkHttpHelper;
import com.greenskinmonster.a51nb.okhttp.ParamsMap;
import com.greenskinmonster.a51nb.parser.HiParser;
import com.greenskinmonster.a51nb.utils.Constants;
import com.greenskinmonster.a51nb.utils.HiUtils;
import com.greenskinmonster.a51nb.utils.Logger;
import com.greenskinmonster.a51nb.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;

/**
 * Created by GreenSkinMonster on 2016-11-16.
 */

public class SimpleListJob extends BaseJob {

    public static final int TYPE_MYPOST = 1;
    public static final int TYPE_SEARCH = 2;
    public static final int TYPE_SMS = 3;
    public static final int TYPE_THREAD_NOTIFY = 4;
    public static final int TYPE_SMS_DETAIL = 5;
    public static final int TYPE_FAVORITES = 6;
    public static final int TYPE_SEARCH_USER_THREADS = 7;
    public static final int TYPE_HISTORIES = 9;
    public static final int TYPE_NEW_POSTS = 10;

    public static final String NEW_POSTS_REPLY = "new";
    public static final String NEW_POSTS_THREAD = "newthread";
    public static final String NEW_POSTS_DIGEST = "digest";
    public static final String NEW_POSTS_HOT = "hot";
    public static final String NEW_POSTS_SOFA = "sofa";

    public static final String MY_POSTS_THREAD = "thread";
    public static final String MY_POSTS_REPLY = "reply";

    public static final String NOTIFY_UNREAD = "unread";
    public static final String NOTIFY_THREAD = "thread";
    public static final String NOTIFY_COMMENT = "pcomment";
    public static final String NOTIFY_AT = "at";
    public static final String NOTIFY_SYSTEM = "system";

    private Context mCtx;
    private int mType;
    private int mPage = 1;
    private SearchBean mSearchBean;
    private Bundle mBundle;

    private SimpleListEvent mEvent;

    public SimpleListJob(Context context, String sessionId, int type, int page, SearchBean searchBean) {
        super(sessionId);
        mCtx = context;
        mType = type;
        mPage = page;
        mSearchBean = searchBean;

        mEvent = new SimpleListEvent();
        mEvent.mSessionId = mSessionId;
        mEvent.mPage = page;
        mEvent.mType = mType;
    }

    public SimpleListJob(Context context, String sessionId, int type, int page, Bundle bundle) {
        super(sessionId);
        mCtx = context;
        mType = type;
        mPage = page;
        mBundle = bundle;

        mEvent = new SimpleListEvent();
        mEvent.mSessionId = mSessionId;
        mEvent.mPage = page;
        mEvent.mType = mType;
    }

    @Override
    public void onAdded() {
        mEvent.mStatus = Constants.STATUS_IN_PROGRESS;
        EventBus.getDefault().postSticky(mEvent);
    }

    @Override
    public void onRun() throws Throwable {
        if (mType == TYPE_HISTORIES) {
            processHistories();
        } else if (mType == TYPE_THREAD_NOTIFY) {
            processNotifyList();
        } else {
            processSimpleList();
        }

        EventBus.getDefault().postSticky(mEvent);
    }

    private void processHistories() {
        mEvent.mData = fetchHistories();
        mEvent.mStatus = Constants.STATUS_SUCCESS;
        mEvent.mMessage = "";
        mEvent.mDetail = "";
        mEvent.mFormhash = "";
    }

    private void processSimpleList() {
        SimpleListBean data = null;

        int eventStatus = Constants.STATUS_SUCCESS;
        String eventMessage = "";
        String eventDetail = "";
        String formhash = "";

        for (int i = 0; i < OkHttpHelper.MAX_RETRY_TIMES; i++) {
            try {
                String resp = fetchSimpleList(mType);
                Document doc = Jsoup.parse(resp);

                if (!LoginHelper.checkLoggedin(doc)) {
                    if (HiSettingsHelper.getInstance().isLoginInfoValid()) {
                        int status = new LoginHelper().login();
                        if (status == Constants.STATUS_SUCCESS) {
                            continue;
                        }
                    }
                }

                data = HiParser.parseSimpleList(mType, doc);
                formhash = HiParser.parseFormhash(doc);
                String message = HiParser.parseErrorMessage(doc);
                if (!TextUtils.isEmpty(message)) {
                    eventMessage = message;
                }
                break;
            } catch (Exception e) {
                Logger.e(e);
                NetworkError message = OkHttpHelper.getErrorMessage(e);
                eventStatus = Constants.STATUS_FAIL;
                eventMessage = message.getMessage();
                eventDetail = message.getDetail();
                if (isCancelled())
                    break;
            }
        }

        mEvent.mData = data;
        mEvent.mStatus = eventStatus;
        mEvent.mMessage = eventMessage;
        mEvent.mDetail = eventDetail;
        mEvent.mFormhash = formhash;
    }

    private void processNotifyList() {
        SimpleListBean data = null;

        int eventStatus = Constants.STATUS_SUCCESS;
        String eventMessage = "";
        String eventDetail = "";
        String formhash = "";

        String extra = getString("extra");
        for (int i = 0; i < OkHttpHelper.MAX_RETRY_TIMES; i++) {
            try {
                if (NOTIFY_UNREAD.equals(extra)) {
                    Document doc = Jsoup.parse(OkHttpHelper.getInstance().get(HiUtils.ThreadNotifyByTypeUrl.replace("{type}", NOTIFY_THREAD), mSessionId));
                    SimpleListBean threads = HiParser.parseSimpleList(mType, doc);

                    doc = Jsoup.parse(OkHttpHelper.getInstance().get(HiUtils.ThreadNotifyByTypeUrl.replace("{type}", NOTIFY_AT), mSessionId));
                    SimpleListBean ats = HiParser.parseSimpleList(mType, doc);

                    doc = Jsoup.parse(OkHttpHelper.getInstance().get(HiUtils.SystemNotifyUrl, mSessionId));
                    SimpleListBean systems = HiParser.parseSimpleList(mType, doc);

                    formhash = HiParser.parseFormhash(doc);

                    data = new SimpleListBean();
                    if (threads != null) {
                        for (SimpleListItemBean bean : threads.getAll()) {
                            if (bean.isNew()) {
                                data.getAll().add(bean);
                            }
                        }
                    }
                    if (ats != null) {
                        for (SimpleListItemBean bean : ats.getAll()) {
                            if (bean.isNew()) {
                                data.getAll().add(bean);
                            }
                        }
                    }
                    if (systems != null) {
                        for (SimpleListItemBean bean : systems.getAll()) {
                            if (bean.isNew()) {
                                data.getAll().add(bean);
                            }
                        }
                    }
                    data.setMaxPage(1);
                } else {
                    String url;
                    if (NOTIFY_SYSTEM.equals(extra)) {
                        url = HiUtils.SystemNotifyUrl;
                    } else if (!TextUtils.isEmpty(extra)) {
                        url = HiUtils.ThreadNotifyByTypeUrl.replace("{type}", extra);
                    } else {
                        url = HiUtils.ThreadNotifyUrl;
                    }
                    String resp = OkHttpHelper.getInstance().get(url, mSessionId);
                    Document doc = Jsoup.parse(resp);
                    data = HiParser.parseSimpleList(mType, doc);
                    formhash = HiParser.parseFormhash(doc);
                }
                break;
            } catch (Exception e) {
                Logger.e(e);
                NetworkError message = OkHttpHelper.getErrorMessage(e);
                eventStatus = Constants.STATUS_FAIL;
                eventMessage = message.getMessage();
                eventDetail = message.getDetail();
                if (isCancelled())
                    break;
            }
        }

        mEvent.mData = data;
        mEvent.mStatus = eventStatus;
        mEvent.mMessage = eventMessage;
        mEvent.mDetail = eventDetail;
        mEvent.mFormhash = formhash;
    }

    @NonNull
    private SimpleListBean fetchHistories() {
        SimpleListBean data;
        data = new SimpleListBean();
        List<History> histories = HistoryDao.getHistories();
        for (History history : histories) {
            SimpleListItemBean bean = new SimpleListItemBean();
            String forumName = "";
            if (!TextUtils.isEmpty(history.getFid()) && TextUtils.isDigitsOnly(history.getFid()))
                forumName = HiUtils.getForumNameByFid(Integer.parseInt(history.getFid()));
            bean.setTid(history.getTid());
            bean.setAuthorId(history.getUid());
            bean.setTitle(history.getTitle());
            bean.setAuthor(history.getUsername());
            bean.setCreateTime(history.getPostTime());
            bean.setForum(forumName);
            data.add(bean);
        }
        return data;
    }

    private String fetchSimpleList(int type) throws Exception {
        String url = null;
        String extra = getString("extra");
        switch (type) {
            case TYPE_MYPOST:
                url = HiUtils.MyPostUrl
                        .replace("{uid}", getString("uid"))
                        .replace("{type}", getString("extra", MY_POSTS_THREAD));
                if (mPage > 1)
                    url += "&page=" + mPage;
                break;
            case TYPE_SMS:
                url = HiUtils.SMSUrl;
                break;
            case TYPE_THREAD_NOTIFY:
                if (NOTIFY_AT.equals(extra)) {
                    url = HiUtils.ThreadNotifyByTypeUrl.replace("{type}", extra);
                } else if (NOTIFY_THREAD.equals(extra)) {
                    url = HiUtils.ThreadNotifyByTypeUrl.replace("{type}", extra);
                } else if (NOTIFY_SYSTEM.equals(extra)) {
                    url = HiUtils.SystemNotifyUrl;
                } else {
                    url = HiUtils.ThreadNotifyUrl;
                }
                break;
            case TYPE_SMS_DETAIL:
                url = HiUtils.SMSDetailUrl + getString("extra");
                break;
            case TYPE_NEW_POSTS:
                url = HiUtils.NewPostsUrl.replace("{type}", TextUtils.isEmpty(extra) ? NEW_POSTS_REPLY : extra);
                if (mPage > 1)
                    url += "&page=" + mPage;
                break;
            case TYPE_SEARCH:
                if (TextUtils.isEmpty(mSearchBean.getSearchId())) {
                    url = HiUtils.SearchUrl;
                    ParamsMap params = new ParamsMap();
                    params.put("formhash", mSearchBean.getFormhash());
                    params.put("srchtxt", mSearchBean.getQuery());
                    params.put("srchuname", mSearchBean.getAuthor());
                    params.put("srchfilter", "all");
                    params.put("srchfrom", "0");
                    params.put("before", "");
                    params.put("orderby", "lastpost");
                    params.put("ascdesc", "desc");
                    params.put("srchfid[]", "all");
                    params.put("searchsubmit", "yes");

                    if (mPage > 1)
                        url += "&page=" + mPage;
                    return OkHttpHelper.getInstance().post(url, params);
                } else {
                    url = HiUtils.SearchByIdUrl.replace("{searchid}", mSearchBean.getSearchId());
                    if (mPage > 1)
                        url += "&page=" + mPage;
                }
                break;
            case TYPE_SEARCH_USER_THREADS:
                if (TextUtils.isEmpty(mSearchBean.getSearchId())) {
                    url = HiUtils.SearchUserThreads.replace("{srchuid}", mSearchBean.getUid());
                } else {
                    url = HiUtils.SearchByIdUrl.replace("{searchid}", mSearchBean.getSearchId());
                }
                if (mPage > 1)
                    url += "&page=" + mPage;
                break;
            case TYPE_FAVORITES:
                url = HiUtils.FavoritesUrl;
                if (mPage > 1)
                    url += "&page=" + mPage;
                break;
            default:
                break;
        }
        return OkHttpHelper.getInstance().get(url, mSessionId);
    }

    private String getString(String key) {
        return getString(key, "");
    }

    private String getString(String key, String def) {
        if (mBundle != null && mBundle.containsKey(key)) {
            return Utils.nullToText(mBundle.getString(key));
        }
        return def;
    }

    private int getInt(String key) {
        if (mBundle != null && mBundle.containsKey(key)) {
            return mBundle.getInt(key);
        }
        return -1;
    }

}
