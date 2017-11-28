package com.greenskinmonster.a51nb.async;

import android.content.Context;
import android.text.TextUtils;

import com.greenskinmonster.a51nb.bean.CommentListBean;
import com.greenskinmonster.a51nb.bean.DetailListBean;
import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.bean.PostBean;
import com.greenskinmonster.a51nb.bean.PrePostInfoBean;
import com.greenskinmonster.a51nb.okhttp.OkHttpHelper;
import com.greenskinmonster.a51nb.okhttp.ParamsMap;
import com.greenskinmonster.a51nb.parser.HiParser;
import com.greenskinmonster.a51nb.parser.ParserUtil;
import com.greenskinmonster.a51nb.parser.ThreadDetailParser;
import com.greenskinmonster.a51nb.utils.Constants;
import com.greenskinmonster.a51nb.utils.HiUtils;
import com.greenskinmonster.a51nb.utils.Logger;
import com.greenskinmonster.a51nb.utils.Utils;
import com.vdurmont.emoji.EmojiParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import okhttp3.Response;

public class PostHelper {

    public static final int MODE_REPLY_THREAD = 0;
    public static final int MODE_REPLY_POST = 1;
    public static final int MODE_NEW_THREAD = 3;
    public static final int MODE_EDIT_POST = 5;
    public static final int MODE_REPLY_COMMENT = 7;

    public static final String SPECIAL_NORM = "";
    public static final String SPECIAL_POLL = "1";
    public static final String SPECIAL_TRADE = "2";

    private static long LAST_POST_TIME = 0;
    private static final long POST_DELAY_IN_SECS = 15;

    private int mMode;
    private String mResult;
    private int mStatus = Constants.STATUS_FAIL;
    private DetailListBean mDetailListBean;
    private Context mCtx;
    private PrePostInfoBean mInfo;
    private PostBean mPostArg;
    private CommentListBean mCommentList;

    private String mTid;
    private String mTitle;
    private int mFloor;

    public PostHelper(Context ctx, int mode, PrePostInfoBean info, PostBean postArg) {
        mCtx = ctx;
        mMode = mode;
        mInfo = info;
        mPostArg = postArg;
    }

    public PostBean post() {
        PostBean postBean = mPostArg;
        String replyText = postBean.getContent();
        String tid = postBean.getTid();
        String pid = postBean.getPid();
        int fid = postBean.getFid();
        int floor = postBean.getFloor();
        String subject = postBean.getSubject();
        String typeid = postBean.getTypeid();

        int count = 0;
        while (mInfo == null && count < 3) {
            count++;
            mInfo = new PrePostAsyncTask(null, mMode, null).doInBackground(postBean);
        }

        mFloor = floor;

        replyText = replaceToTags(replyText);
        replyText = EmojiParser.parseToHtmlDecimal(replyText);
        if (!TextUtils.isEmpty(subject))
            subject = EmojiParser.parseToHtmlDecimal(subject);

        String url;
        switch (mMode) {
            case MODE_REPLY_THREAD:
                url = HiUtils.ReplyUrl
                        .replace("{fid}", String.valueOf(fid))
                        .replace("{tid}", tid);
                doPost(url, replyText, null, null);
                break;
            case MODE_REPLY_POST:
                url = HiUtils.ReplyUrl
                        .replace("{fid}", String.valueOf(fid))
                        .replace("{tid}", tid);
                doPost(url, replyText, null, null);
                break;
            case MODE_NEW_THREAD:
                url = HiUtils.NewThreadUrl.replace("{fid}", String.valueOf(fid));
                doPost(url, replyText, subject, typeid);
                break;
            case MODE_EDIT_POST:
                url = HiUtils.EditUrl;
                doPost(url, replyText, subject, typeid);
                break;
            case MODE_REPLY_COMMENT:
                replyComment();
                break;
        }

        postBean.setSubject(mTitle);
        postBean.setFloor(mFloor);
        postBean.setPid(mPostArg.getPid());
        postBean.setTid(mTid);

        postBean.setMessage(mResult);
        postBean.setStatus(mStatus);
        postBean.setDetailListBean(mDetailListBean);
        postBean.setCommentListBean(mCommentList);

        return postBean;
    }

    private void doPost(String url, String replyText, String subject, String typeid) {
        String formhash = mInfo != null ? mInfo.getFormhash() : null;

        if (TextUtils.isEmpty(formhash)) {
            mResult = "发表失败，无法获取必要信息 ！";
            mStatus = Constants.STATUS_FAIL;
            return;
        }

        ParamsMap params = new ParamsMap();
        params.put("formhash", formhash);
        params.put("posttime", String.valueOf(System.currentTimeMillis()));
        params.put("wysiwyg", "0");
        params.put("message", replyText);

        for (String attach : mInfo.getNewAttaches()) {
            params.put("attachnew[" + attach + "][description]", "");
        }
        for (String attach : mInfo.getDeleteAttaches()) {
            deleteImage(mPostArg.getTid(), mPostArg.getPid(), attach);
        }
        if (mMode == MODE_NEW_THREAD || (mMode == MODE_EDIT_POST && mFloor == 1)) {
            params.put("subject", subject);
            params.put("typeid", typeid);
            params.put("readperm", mInfo.getReadPerm());
            params.put("allownoticeauthor", "1");

            if (!TextUtils.isEmpty(mInfo.getSpecial()))
                params.put("special", mInfo.getSpecial());

            mTitle = subject;

            if (PostHelper.SPECIAL_POLL.equals(mInfo.getSpecial())
                    && mInfo.getPollChoices().size() > 0) {
                for (String polloption : mInfo.getPollChoices()) {
                    params.put("polloption[]", polloption);
                }
                params.put("polloption[]", "");
                params.put("polloptions", "");
                params.put("polls", "yes");
                params.put("tpolloption", "1");
                params.put("maxchoices", mInfo.getPollMaxChoices());
                params.put("expiration", mInfo.getPollDays());
                params.put("visibilitypoll", mInfo.isPollVisibility() ? "1" : "");
                params.put("overt", mInfo.isPollOvert() ? "1" : "");
            } else if (PostHelper.SPECIAL_TRADE.equals(mInfo.getSpecial())
                    && !TextUtils.isEmpty(mInfo.getItemName())) {
                params.put("item_name", mInfo.getItemName());
                params.put("item_locus", mInfo.getItemLocus());
                params.put("item_number", mInfo.getItemNumber());
                params.put("item_quality", mInfo.getItemQuality());
                params.put("item_expiration", mInfo.getItemExpiration());
                params.put("transport", mInfo.getTransport());
                params.put("item_price", mInfo.getItemPrice());
                params.put("item_costprice", mInfo.getItemCostPrice());
                params.put("item_credit", mInfo.getItemCredit());
                params.put("item_costcredit", mInfo.getItemCostCredit());
                params.put("paymethod", mInfo.getPaymethod());
            }
        }

        if (mMode == MODE_EDIT_POST) {
            params.put("page", String.valueOf(mPostArg.getPage()));
            params.put("fid", String.valueOf(mPostArg.getFid()));
            params.put("tid", mPostArg.getTid());
            params.put("pid", mPostArg.getPid());

            if (!TextUtils.isEmpty(subject)) {
                params.put("subject", subject);
                mTitle = subject;
                if (!TextUtils.isEmpty(typeid)) {
                    params.put("typeid", typeid);
                }
            }
        }

        if (mInfo.getExtCredit() >= 0) {
            //new thread or edit first floor
            params.put("replycredit_extcredits", mInfo.getExtCredit());
            params.put("replycredit_times", mInfo.getCreditTimes());
            params.put("replycredit_membertimes", mInfo.getCreditMemberTimes());
            params.put("replycredit_random", mInfo.getCreditRandom());
        }

        if (mMode == MODE_REPLY_POST) {
            String noticeauthor = mInfo.getNoticeAuthor();
            String noticeauthormsg = mInfo.getNoticeAuthorMsg();
            String noticetrimstr = mInfo.getNoticeTrimStr();
            if (!TextUtils.isEmpty(noticeauthor)) {
                params.put("noticeauthor", noticeauthor);
                params.put("noticeauthormsg", Utils.nullToText(noticeauthormsg));
                params.put("noticetrimstr", Utils.nullToText(noticetrimstr));
            }
        }

        try {
            Response response = OkHttpHelper.getInstance().postAsResponse(url, params);
            String resp = OkHttpHelper.getResponseBody(response);
            String requestUrl = response.request().url().toString();
            Document doc = Jsoup.parse(resp);

            //when success, okhttp will follow 302 redirect get the page content
            String tid = ParserUtil.parseTid(requestUrl);
            if (HiUtils.isValidId(tid)) {
                mTid = tid;
                mResult = "发表成功!";
                mStatus = Constants.STATUS_SUCCESS;
                try {
                    HiSettingsHelper.updateMobileNetworkStatus(mCtx);
                    //parse resp to get redirected page content

                    DetailListBean data = ThreadDetailParser.parse(doc, tid);
                    if (data != null && data.getCount() > 0) {
                        mDetailListBean = data;
                    }
                } catch (Exception e) {
                    Logger.e(e);
                }
            } else {
                mStatus = Constants.STATUS_FAIL;
                mResult = HiParser.parseErrorMessage(doc);
                if (TextUtils.isEmpty(mResult)) {
                    mResult = "发表失败! ";
                }
                if (mResult.contains("需要审核")) {
                    mStatus = Constants.STATUS_SUCCESS;
                    Element linkEl = doc.select("div#messagetext a[href*=thread]").first();
                    if (linkEl != null)
                        mTid = ParserUtil.parseTid(linkEl.attr("href"));
                }
            }
        } catch (Exception e) {
            Logger.e(e);
            mResult = "发表失败 : " + OkHttpHelper.getErrorMessage(e);
            mStatus = Constants.STATUS_FAIL;
        }

        if (mStatus == Constants.STATUS_SUCCESS && (mMode != MODE_EDIT_POST))
            LAST_POST_TIME = System.currentTimeMillis();
    }

    public static int getWaitTimeToPost() {
        long delta = (System.currentTimeMillis() - LAST_POST_TIME) / 1000;
        if (POST_DELAY_IN_SECS > delta) {
            return (int) (POST_DELAY_IN_SECS - delta);
        }
        return 0;
    }

    private void replyComment() {
        String url = HiUtils.ReplyCommentUrl
                .replace("{tid}", mPostArg.getTid())
                .replace("{pid}", mPostArg.getPid())
                .replace("{page}", String.valueOf(mPostArg.getPage()));

        try {
            ParamsMap params = new ParamsMap();
            params.put("formhash", mPostArg.getFormhash());
            params.put("handlekey", "comment");
            params.put("message", mPostArg.getContent());
            String resp = OkHttpHelper.getInstance().post(url, params);
            if (resp.contains("succeedhandle")) {
                mCommentList = ThreadActionHelper.fetchComments(mPostArg.getTid(), mPostArg.getPid(), 1);
                mStatus = Constants.STATUS_SUCCESS;
                mResult = "帖子点评成功";
            } else {
                mStatus = Constants.STATUS_FAIL;
                mResult = "发表点评失败";
            }
        } catch (Exception e) {
            Logger.e(e);
            mResult = "发表点评失败 : " + OkHttpHelper.getErrorMessage(e);
            mStatus = Constants.STATUS_FAIL;
        }
    }

    private String replaceToTags(final String replyText) {
        String text = replyText;
        StringBuilder sb = new StringBuilder();
        try {
            while (!TextUtils.isEmpty(text)) {
                int tagStart = text.indexOf("[");
                if (tagStart == -1) {
                    sb.append(Utils.replaceUrlWithTag(text));
                    break;
                }
                int tagEnd = text.indexOf("]", tagStart);
                if (tagEnd == -1) {
                    sb.append(Utils.replaceUrlWithTag(text));
                    break;
                }
                String tag = text.substring(tagStart + 1, tagEnd);
                if (tag.contains("=")) {
                    tag = tag.substring(0, tag.indexOf("="));
                }
                String tagE = "[/" + tag + "]";
                int tagEIndex = text.indexOf(tagE);
                if (tagEIndex != -1) {
                    tagEIndex = tagEIndex + tagE.length();
                } else {
                    sb.append(Utils.replaceUrlWithTag(text));
                    break;
                }
                sb.append(Utils.replaceUrlWithTag(text.substring(0, tagStart)));
                sb.append(text.substring(tagStart, tagEIndex));
                text = text.substring(tagEIndex);
            }
        } catch (Exception e) {
            Logger.e(e);
            return replyText;
        }
        return sb.toString();
    }

    public static String deleteImage(String tid, String pid, String attachId) {
        if (TextUtils.isEmpty(tid) || TextUtils.isEmpty(pid)) {
            tid = "0";
            pid = "0";
        }
        try {
            String resp = OkHttpHelper.getInstance().get(
                    HiUtils.DeleteImgUrl
                            .replace("{tid}", tid)
                            .replace("{pid}", pid)
                            .replace("{aid}", attachId));
            return ParserUtil.parseXmlErrorMessage(resp);
        } catch (Exception e) {
            Logger.e(e);
            return OkHttpHelper.getErrorMessage(e).getMessage();
        }
    }

}
