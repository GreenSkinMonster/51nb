package com.greenskinmonster.a51nb.async;

import android.text.TextUtils;

import com.greenskinmonster.a51nb.bean.CommentBean;
import com.greenskinmonster.a51nb.bean.CommentListBean;
import com.greenskinmonster.a51nb.bean.PreRateBean;
import com.greenskinmonster.a51nb.okhttp.OkHttpHelper;
import com.greenskinmonster.a51nb.okhttp.ParamsMap;
import com.greenskinmonster.a51nb.parser.ParserUtil;
import com.greenskinmonster.a51nb.parser.ThreadDetailParser;
import com.greenskinmonster.a51nb.utils.HiUtils;
import com.greenskinmonster.a51nb.utils.Logger;
import com.greenskinmonster.a51nb.utils.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GreenSkinMonster on 2017-08-12.
 */

public class ThreadActionHelper {

    public static void support(String tid, String pid, String formhash, OkHttpHelper.ResultCallback callback) {
        OkHttpHelper.getInstance().asyncGet(
                HiUtils.SupportUrl
                        .replace("{tid}", tid)
                        .replace("{pid}", pid)
                        .replace("{formhash}", formhash),
                callback);
    }

    public static void against(String tid, String pid, String formhash, OkHttpHelper.ResultCallback callback) {
        OkHttpHelper.getInstance().asyncGet(
                HiUtils.AgainstUrl
                        .replace("{tid}", tid)
                        .replace("{pid}", pid)
                        .replace("{formhash}", formhash),
                callback);
    }

    public static CommentListBean fetchComments(String tid, String pid, int page) {
        CommentListBean commentList = null;
        try {
            String commentResp = OkHttpHelper.getInstance().get(
                    HiUtils.GetCommentsUrl
                            .replace("{tid}", tid)
                            .replace("{pid}", pid)
                            .replace("{page}", String.valueOf(page)));
            Document commentDoc = Jsoup.parse(ParserUtil.parseXmlMessage(commentResp));
            Elements divCommentES = commentDoc.select("div.pstl");
            List<CommentBean> comments = new ArrayList<>(divCommentES.size());
            for (Element divComment : divCommentES) {
                CommentBean commentBean = ThreadDetailParser.parseComment(divComment);
                if (commentBean != null)
                    comments.add(commentBean);
            }
            int nextPage = 1;
            if (commentDoc.select("a.nxt").first() != null)
                nextPage = Utils.getMiddleInt(commentDoc.select("a.nxt").first().attr("href"), "page=", "&");
            commentList = new CommentListBean();
            commentList.setComments(comments);
            commentList.setHasNextPage(nextPage > page);
            commentList.setPage(page);
        } catch (Exception e) {
            Logger.e(e);
        }
        return commentList;
    }

    public static PreRateBean fetchPreRateInfo(String tid, String pid) throws Exception {
        String xml = OkHttpHelper.getInstance().get(HiUtils.PreRateUrl
                .replace("{tid}", tid)
                .replace("{pid}", pid)
                .replace("{time}", String.valueOf(System.currentTimeMillis())));
        String html = ParserUtil.parseXmlMessage(xml);
        Document doc = Jsoup.parse(html);

        PreRateBean bean = new PreRateBean();
        Element tableEl = doc.select("table.dt").first();
        if (tableEl != null) {
            Elements trES = tableEl.select("tr");
            for (Element trEl : trES) {
                Elements tdES = trEl.select("td");
                if (tdES.size() == 4) {
                    String type = tdES.get(0).text().trim();
                    switch (type) {
                        case "技术分":
                            bean.setScore1Left(tdES.get(3).text());
                            bean.setScore1Limit(tdES.get(2).text());
                            break;
                        case "资产值":
                            bean.setScore2Left(tdES.get(3).text());
                            bean.setScore2Limit(tdES.get(2).text());
                            break;
                        case "联谊分":
                            bean.setScore3Left(tdES.get(3).text());
                            bean.setScore3Limit(tdES.get(2).text());
                            break;
                    }
                }
            }
        }

        if (TextUtils.isEmpty(bean.getScore2Left()) || TextUtils.isEmpty(bean.getScore2Limit())) {
            return null;
        }
        return bean;
    }

    public static void postRate(String formhash, String tid, String pid, String score1, String score2, String score3, String reason, OkHttpHelper.ResultCallback callback) {
        ParamsMap params = new ParamsMap();
        params.put("formhash", formhash);
        params.put("tid", tid);
        params.put("pid", pid);
        params.put("reason", reason);
        params.put("score1", score1);
        params.put("score2", score2);
        params.put("score3", score3);
        OkHttpHelper.getInstance().asyncPost(HiUtils.RateUrl, params, callback);
    }

    public static void votepoll(String formhash, int fid, String tid, List<String> answers, OkHttpHelper.ResultCallback callback) {

        ParamsMap params = new ParamsMap();
        params.put("formhash", formhash);
        for (String answer : answers) {
            params.put("pollanswers[]", answer);
        }

        OkHttpHelper.getInstance().asyncPost(
                HiUtils.VotePollUrl
                        .replace("{fid}", String.valueOf(fid))
                        .replace("{tid}", tid),
                params, callback);

    }

}
