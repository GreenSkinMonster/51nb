package com.greenskinmonster.a51nb.job;

import android.content.Context;
import android.text.TextUtils;

import com.greenskinmonster.a51nb.async.LoginHelper;
import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.bean.ThreadListBean;
import com.greenskinmonster.a51nb.okhttp.NetworkError;
import com.greenskinmonster.a51nb.okhttp.OkHttpHelper;
import com.greenskinmonster.a51nb.parser.HiParser;
import com.greenskinmonster.a51nb.parser.ThreadListParser;
import com.greenskinmonster.a51nb.service.NotiHelper;
import com.greenskinmonster.a51nb.utils.Constants;
import com.greenskinmonster.a51nb.utils.HiUtils;
import com.greenskinmonster.a51nb.utils.Logger;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import okhttp3.HttpUrl;
import okhttp3.Response;

/**
 * Created by GreenSkinMonster on 2016-11-16.
 */

public class ThreadListJob extends BaseJob {

    public final static String ORDER_BY_THREAD = "dateline";
    public final static String ORDER_BY_REPLY = "";

    private Context mCtx;
    private int mForumId;
    private int mPage;
    private String mTypeId;
    private String mOrderBy;
    private String mUrl;

    private ThreadListEvent mEvent;

    public ThreadListJob(Context context, String sessionId, int forumId, int page, String typeId, String orderBy) {
        super(sessionId);
        mCtx = context;
        mForumId = forumId;
        mPage = page;
        mTypeId = typeId;
        mOrderBy = orderBy;

        mEvent = new ThreadListEvent();
        mEvent.mSessionId = mSessionId;
        mEvent.mForumId = forumId;
        mEvent.mPage = page;
    }

    @Override
    public void onAdded() {
        mEvent.mStatus = Constants.STATUS_IN_PROGRESS;
        EventBus.getDefault().postSticky(mEvent);
    }

    @Override
    public void onRun() throws Throwable {
        ThreadListBean data = null;
        int eventStatus = Constants.STATUS_SUCCESS;
        String eventMessage = "";
        String eventDetail = "";

        for (int i = 0; i < OkHttpHelper.MAX_RETRY_TIMES; i++) {
            try {
                Response response = fetchForumList();

                HttpUrl httpUrl = response.request().url();
                String respUrl = httpUrl.toString();
                String resp = OkHttpHelper.getResponseBody(response);

                if (respUrl.startsWith(HiUtils.PasswordUrl)) {
                    eventMessage = "您必须设置安全提问";
                    eventStatus = Constants.STATUS_FAIL_SEC_QUESTION;
                    break;
                }

                Document doc = Jsoup.parse(resp);

                if (!LoginHelper.checkLoggedin(doc)) {
                    if (HiSettingsHelper.getInstance().isLoginInfoValid()) {
                        int status = new LoginHelper().login();
                        if (status == Constants.STATUS_SUCCESS) {
                            continue;
                        }
                    }
                }

                HiSettingsHelper.updateMobileNetworkStatus(mCtx);
                NotiHelper.fetchNotification(doc);
                if (mForumId == HiUtils.FID_TRADE && TextUtils.isEmpty(mTypeId)) {
                    data = ThreadListParser.parseTradeForum(doc);
                } else if (mForumId == HiUtils.FID_RECOMMEND) {
                    data = ThreadListParser.parseRecommendForum(doc);
                } else {
                    data = ThreadListParser.parse(doc, mForumId);
                }

                if (data == null) {
                    String error = HiParser.parseErrorMessage(doc);
                    if (!TextUtils.isEmpty(error)) {
                        eventMessage = error;
                    } else {
                        eventMessage = "页面加载失败";
                    }
                    eventStatus = Constants.STATUS_FAIL_ABORT;
                }

//                if (eventStatus != Constants.STATUS_SUCCESS && HiSettingsHelper.getInstance().isErrorReportMode()) {
//                    StringBuilder sb = new StringBuilder();
//                    sb.append("\n\n=======================================================\n\n");
//                    sb.append(Utils.formatDate(new Date()));
//                    sb.append("\nrequset url :").append(mUrl);
//                    sb.append("\nresponse url :").append(respUrl);
//                    sb.append("\ncookies :").append(OkHttpHelper.getInstance().printCookies(respUrl));
//                    sb.append("\n");
//                    sb.append(resp);
//                    sb.append("\n\n=======================================================\n\n");
//                    File destFile = new File(UIUtils.getSaveFolder(), "51nb_debug.log");
//                    Utils.writeFile(destFile, sb.toString());
//                    eventMessage += " 日志文件:" + destFile.getAbsolutePath();
//                }

                break;
            } catch (Exception e) {
                Logger.e(e);
                NetworkError networkError = OkHttpHelper.getErrorMessage(e);
                eventStatus = Constants.STATUS_FAIL;
                eventMessage = networkError.getMessage();
                eventDetail = networkError.getDetail();
                if (isCancelled())
                    break;
            }
        }

        mEvent.mData = data;
        mEvent.mStatus = eventStatus;
        mEvent.mMessage = eventMessage;
        mEvent.mDetail = eventDetail;
        EventBus.getDefault().postSticky(mEvent);
    }

    private Response fetchForumList() throws Exception {
        mUrl = HiUtils.ThreadListUrl + mForumId + "&page=" + mPage;
        if (!TextUtils.isEmpty(mOrderBy) && !TextUtils.isEmpty(mTypeId)) {
            mUrl += "&filter=author&orderby=dateline&typeid=" + mTypeId;
        } else if (!TextUtils.isEmpty(mOrderBy)) {
            mUrl += "&filter=author&orderby=dateline";
        } else if (!TextUtils.isEmpty(mTypeId)) {
            mUrl += "&filter=typeid&typeid=" + mTypeId;
        }
        return OkHttpHelper.getInstance().getResponse(mUrl, mSessionId);
    }
}
