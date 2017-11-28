package com.greenskinmonster.a51nb.parser;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.greenskinmonster.a51nb.bean.CommentBean;
import com.greenskinmonster.a51nb.bean.CommentListBean;
import com.greenskinmonster.a51nb.bean.ContentAttach;
import com.greenskinmonster.a51nb.bean.ContentImg;
import com.greenskinmonster.a51nb.bean.ContentTradeInfo;
import com.greenskinmonster.a51nb.bean.DetailBean;
import com.greenskinmonster.a51nb.bean.DetailBean.Contents;
import com.greenskinmonster.a51nb.bean.DetailListBean;
import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.bean.PollBean;
import com.greenskinmonster.a51nb.bean.PollOptionBean;
import com.greenskinmonster.a51nb.bean.RateBean;
import com.greenskinmonster.a51nb.bean.RateListBean;
import com.greenskinmonster.a51nb.ui.ThreadDetailFragment;
import com.greenskinmonster.a51nb.ui.textstyle.TextStyle;
import com.greenskinmonster.a51nb.ui.textstyle.TextStyleHolder;
import com.greenskinmonster.a51nb.utils.HiUtils;
import com.greenskinmonster.a51nb.utils.Logger;
import com.greenskinmonster.a51nb.utils.Utils;
import com.vdurmont.emoji.EmojiParser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class ThreadDetailParser {

    public static String getThreadAuthorId(Document doc) {
        Elements authorLinksEs = doc.select("td.postauthor div.postinfo a");
        if (authorLinksEs.size() > 0) {
            String uidUrl = authorLinksEs.first().attr("href");
            return Utils.getMiddleString(uidUrl, "uid=", "&");
        }
        return "";
    }

    public static DetailListBean parse(Document doc, String tid) {
        // get last page
        Elements pagesES = doc.select("div#pgt div.pg");
        // thread have only 1 page don't have "div.pages"
        int last_page = 1;
        int page = 1;
        if (pagesES.size() != 0) {
            for (Node n : pagesES.first().childNodes()) {
                int tmp = Utils.getIntFromString(((Element) n).text());
                if (tmp > last_page) {
                    last_page = tmp;
                }
                if ("strong".equals(n.nodeName())) {
                    page = tmp;
                }
            }
        }

        DetailListBean details = new DetailListBean();
        details.setPage(page);
        details.setLastPage(last_page);
        details.setFormhash(HiParser.parseFormhash(doc));

        if (HiUtils.isValidId(tid))
            details.setTid(tid);

        //get forum id
        Element forumLinkEl = doc.select("div#pt div.z a[href*=forumdisplay]").last();
        if (forumLinkEl != null) {
            int fid = Utils.parseInt(Utils.getMiddleString(forumLinkEl.attr("href"), "fid=", "&"));
            if (fid > 0)
                details.setFid(fid);
        }

        Element subjectSpanEl = doc.select("span#thread_subject").first();
        if (subjectSpanEl != null)
            details.setTitle(EmojiParser.parseToUnicode(subjectSpanEl.text()));

        Element auditEl = doc.select("#postlist > table > tbody > tr > td.vwthd > span.xg1").first();
        if (auditEl != null && auditEl.text().contains("审核")) {
            String s = Utils.getMiddleString(auditEl.text(), "(", ")");
            if (!TextUtils.isEmpty(s))
                details.setTitle("(" + s + ")" + details.getTitle());
        }

        Elements postES = doc.select("div#postlist table[id^=pid]");
        if (postES.size() == 0) {
            return null;
        }

        //update max posts in page, this is controlled by user setting
        if (last_page > page) {
            HiSettingsHelper.getInstance().setMaxPostsInPage(postES.size());
        }

        for (Element postE : postES) {
            DetailBean detail = new DetailBean();
            detail.setPage(page);
            Contents content = detail.getContents();

            if ((details.getFid() == HiUtils.FID_TRADE || details.getFid() == HiUtils.FID_TEST)
                    && page == 1 && details.getCount() == 0) {
                Element divTradeInfo = doc.select("div[id^=trade]").first();
                if (divTradeInfo != null) {
                    Map<String, String> infos = parseTradeInfo(divTradeInfo);
                    ContentTradeInfo tradeInfo = new ContentTradeInfo();
                    tradeInfo.setTradeInfo(infos);
                    content.addTradeInfo(tradeInfo);
                }
            }

            //id
            String postId = Utils.getMiddleString(postE.attr("id"), "pid", "");
            if (TextUtils.isEmpty(postId)) continue;

            detail.setPostId(postId);

            //time
            Element timeEl = postE.select("div.authi em").first();
            if (timeEl == null) {
                continue;
            }
            String time = Utils.getMiddleString(timeEl.text(), "发表于", "").trim();
            detail.setTimePost(time);

            Element divAuthorEl = postE.select("#pid" + postId + " > tbody > tr:nth-child(1) > td.plc > div.pi > div > div.authi").first();
            if (divAuthorEl != null) {
                Element clientLinkEl = divAuthorEl.select("div.authi > a").first();
                if (clientLinkEl != null) {
                    if (clientLinkEl.text().contains("来自安卓客户端")) {
                        detail.setClientType(DetailBean.CLIENT_ANDROID);
                        detail.setClientUrl(ParserUtil.getAbsoluteUrl(clientLinkEl.attr("href")));
                    } else if (clientLinkEl.text().contains("来自苹果客户端")) {
                        detail.setClientType(DetailBean.CLIENT_IOS);
                        detail.setClientUrl(ParserUtil.getAbsoluteUrl(clientLinkEl.attr("href")));
                    }
                }
                String s = divAuthorEl.text().trim();
                detail.setThreadAuthor(divAuthorEl.text().contains("楼主|"));
            }

            //floor, 推荐帖子可能显示在第二楼位置，但是没有楼层号
            int floor = 0;
            Element floorEl = postE.select("div.pi strong").first();
            if (floorEl != null) {
                floor = Utils.parseInt(floorEl.text().replace("#", ""));
                if (floor == 0 && "推荐".equals(floorEl.text())) {
                    detail.setFloor(ThreadDetailFragment.RECOMMEND_FLOOR);
                } else {
                    detail.setFloor(floor);
                }
            }

            //author
            Element authorDiv = postE.select("div[id^=favatar]").first();
            if (authorDiv == null) {
                continue;
            }
            Element authorLink = authorDiv.select("div.authi a").first();
            if (authorLink != null) {
                String uid = Utils.getMiddleString(authorLink.attr("href"), "uid=", "&");
                if (!TextUtils.isEmpty(uid)) {
                    detail.setUid(uid);
                } else {
                    continue;
                }
                String author = authorLink.text();
                if (HiSettingsHelper.getInstance().isInBlacklist(uid)) {
                    detail.setAuthor("[[黑名单用户]]");
                    details.add(detail);
                    continue;
                } else {
                    detail.setAuthor(author);
                }
            } else {
                detail.setAuthor(authorDiv.text());
            }
            Element onlineImg = authorDiv.select("img[src*=userstatus]").first();
            if (onlineImg != null) {
                String src = onlineImg.attr("src");
                if (src.contains("online")) {
                    detail.setOnlineStatus(1);
                } else if (src.contains("offline")) {
                    detail.setOnlineStatus(0);
                }
            }

            Element nicknameEl = authorDiv.select("div.authi > span").first();
            if (nicknameEl != null) {
                detail.setNickname(nicknameEl.text());
            }

            //content
            Elements postMessageES = postE.select("td[id^=postmessage_]");

            //locked user content
            if (postMessageES.size() == 0) {
                postMessageES = postE.select("table tbody tr td.postcontent div.defaultpost div.postmessage div.locked");
                if (postMessageES.size() > 0) {
                    content.addNotice(postMessageES.text());
                    details.add(detail);
                    continue;
                }
            }

            if (postMessageES.size() == 0) {
                content.addNotice("[[!!找不到帖子内容!!]]");
                details.add(detail);
                continue;
            }

            Element postmessageE = postMessageES.first();
            if (postmessageE.childNodeSize() == 0) {
                content.addNotice("[[无内容]]");
                details.add(detail);
                continue;
            }

            //post info
            Element divInfoEl = postE.select("#pid" + postId + " > tbody > tr:nth-child(1) > td.plc > div.pct > div > div.info").first();
            if (divInfoEl != null && !TextUtils.isEmpty(divInfoEl.text())) {
                content.addInfo(divInfoEl.text());
            }

            Element creditInfoEl = postE.select("#pid" + postId + " > tbody > tr:nth-child(1) > td.plc > div.pct > div.cm > h3").first();
            if (creditInfoEl != null && !TextUtils.isEmpty(creditInfoEl.text())) {
                content.addInfo(creditInfoEl.text());
            }

            //post status
            Element postStatusEl = postmessageE.select("i.pstatus").first();
            if (postStatusEl != null) {
                content.addNotice(postStatusEl.text());
                postStatusEl.remove();
            }

            //评论
            Element divCommtnetEl = postE.select("div#comment_" + postId).first();
            if (divCommtnetEl != null) {
                Elements divCommentES = divCommtnetEl.select("div.pstl");
                List<CommentBean> comments = new ArrayList<>(divCommentES.size());
                for (Element divComment : divCommentES) {
                    CommentBean commentBean = parseComment(divComment);
                    if (commentBean != null)
                        comments.add(commentBean);
                }
                if (comments.size() > 0) {
                    int commentPage = 1;
                    int nextCommentPage = 1;
                    if (comments.size() >= 5) {
                        if (postE.select("div#comment_" + postId + " a.nxt").first() != null)
                            nextCommentPage = 2;
                    }
                    CommentListBean commentListBean = new CommentListBean();
                    commentListBean.setPage(commentPage);
                    commentListBean.setHasNextPage(nextCommentPage > 1);
                    commentListBean.setComments(comments);
                    detail.setCommentLists(commentListBean);
                }
                divCommtnetEl.remove();
            }

            //评分
            Element divRateEl = postE.select("dl#ratelog_" + postId).first();
            if (divRateEl != null) {
                Elements trRateES = divRateEl.select("tr[id^=rate_]");
                if (trRateES.size() > 0) {
                    int[] scoreIndexes = new int[3];
                    Element headerTr = divRateEl.select("#ratelog_" + postId + " > dd > table > tbody:nth-child(1) > tr ").first();
                    RateListBean listBean = new RateListBean();
                    if (headerTr != null) {
                        Elements thEs = headerTr.select("th");
                        if (thEs.size() >= 3) {
                            for (int i = 0; i < thEs.size(); i++) {
                                Element thEl = thEs.get(i);
                                if (thEl.text().contains("参与人数")) {
                                    listBean.setRatorCount(Utils.getIntFromString(thEl.text()));
                                } else if (thEl.text().contains("技术分")) {
                                    listBean.setTotalScore1(thEl.text());
                                    scoreIndexes[0] = i;
                                } else if (thEl.text().contains("资产值")) {
                                    listBean.setTotalScore2(thEl.text());
                                    scoreIndexes[1] = i;
                                } else if (thEl.text().contains("联谊分")) {
                                    listBean.setTotalScore3(thEl.text());
                                    scoreIndexes[2] = i;
                                }
                            }
                        }
                    }

                    List<RateBean> rates = new ArrayList<>(trRateES.size());
                    for (Element trEl : trRateES) {
                        RateBean bean = parseRate(trEl, scoreIndexes);
                        if (bean != null)
                            rates.add(bean);
                    }

                    listBean.setRates(rates);

                    detail.setRateListBean(listBean);
                }
                divRateEl.remove();
            }

            Element replyToEl = postE.select("div.pcb > h2").first();
            if (replyToEl != null) {
                content.addText("<b>" + replyToEl.text() + "</b><br><br>");
            }

            // Nodes including Elements(have tag) and text without tag
            TextStyleHolder textStyles = new TextStyleHolder();
            Node contentN = postmessageE.childNode(0);
            int level = 1;
            boolean processChildren;
            while (level > 0 && contentN != null) {

                textStyles.addLevel(level);

                processChildren = parseNode(contentN, content, level, textStyles);

                if (processChildren && contentN.childNodeSize() > 0) {
                    contentN = contentN.childNode(0);
                    level++;
                } else if (contentN.nextSibling() != null) {
                    contentN = contentN.nextSibling();
                    textStyles.removeLevel(level);
                } else {
                    while (contentN.parent().nextSibling() == null) {
                        contentN = contentN.parent();
                        textStyles.removeLevel(level);
                        textStyles.removeLevel(level - 1);
                        level--;
                    }
                    contentN = contentN.parent().nextSibling();
                    textStyles.removeLevel(level);
                    textStyles.removeLevel(level - 1);
                    level--;
                }
            }

            //管理员操作信息
            Element divModActEl = postE.select("div.modact").first();
            if (divModActEl != null)
                content.addNotice(divModActEl.text());

            //附件
            Elements dlES = postE.select("div.pattl dl.tattl");
            for (Element dlEl : dlES) {
                Element sizeEl = dlEl.select("em.xg1").first();
                Element imgEl = dlEl.select("img[id^=aimg_]").first();

                if (imgEl != null) {
                    long size = 0;
                    if (sizeEl != null) {
                        String sizeText = Utils.getMiddleString(sizeEl.text(), "(", ",");
                        size = Utils.parseSizeText(sizeText);
                    }
                    ContentImg contentImg = getContentImg(imgEl, size);
                    content.addImg(contentImg);
                } else {
                    Element attachLinkEl = dlEl.select("a[id^=aid]").first();
                    if (attachLinkEl != null) {
                        attachLinkEl.remove();
                        dlEl.select("div.tip_c").remove();
                        String url = ParserUtil.getAbsoluteUrl(attachLinkEl.attr("href"));
                        String desc = dlEl.text();
                        String title = attachLinkEl.text();
                        ContentAttach attach = new ContentAttach(url, title, desc);
                        content.addAttach(attach);
                    }
                }
            }

            //投票
            if (floor == 1) {
                Element pollEl = doc.select("form#poll").first();
                if (pollEl != null) {
                    PollBean pollBean = new PollBean();
                    Element divTitle = pollEl.select("div.pinf").first();
                    divTitle.select("a").remove();
                    pollBean.setTitle(divTitle.html());
                    Element ptmrEl = pollEl.select("#poll p.ptmr").first();
                    if (ptmrEl != null) {
                        pollBean.setTitle(pollBean.getTitle() + "<br>" + ptmrEl.html());
                    }
                    if (divTitle.text().trim().startsWith("多选投票")) {
                        int maxAnswer = Utils.getMiddleInt(divTitle.text(), "最多可选", "项");
                        if (maxAnswer > 1)
                            pollBean.setMaxAnswer(maxAnswer);
                    }

                    List<PollOptionBean> options = new ArrayList<>();
                    Elements imgTdES = pollEl.select("td[id^=polloption_]");
                    if (imgTdES.size() > 0) {
                        for (Element tdEl : imgTdES) {
                            Element checkEl = tdEl.select("td.pslt input").first();
                            Element imgEl = tdEl.select("img[id]").first();
                            Element textEl = tdEl.select("p.xi2").first();
                            Element rateEl = tdEl.select("p.imgfc").first();
                            PollOptionBean option = new PollOptionBean();
                            option.setOptionId(checkEl != null ? checkEl.attr("value") : "");
                            option.setText(textEl != null ? textEl.html() : "");
                            option.setRates(rateEl != null ? rateEl.text() : "");
                            if (imgEl != null) {
                                ContentImg img = getContentImg(imgEl, 0);
                                option.setImage(img);
                            }
                            options.add(option);
                        }
                    } else {
                        Elements optionsES = pollEl.select("div.pcht > table > tbody > tr");
                        for (Element optionEl : optionsES) {
                            Element checkEl = optionEl.select("td.pslt input").first();
                            Element textEl = optionEl.select("td.pvt label").first();
                            if (textEl != null) {
                                PollOptionBean option = new PollOptionBean();
                                option.setOptionId(checkEl != null ? checkEl.attr("value") : "");
                                option.setText(textEl != null ? textEl.text() : "");
                                options.add(option);
                            } else {
                                Element ratesTdEl = optionEl.select("td").last();
                                if (options.size() > 0 && ratesTdEl.select("em").size() > 0) {
                                    options.get(options.size() - 1).setRates(ratesTdEl.text());
                                }
                            }
                        }
                    }
                    Element lastTr = pollEl.select("div.pcht > table > tbody > tr").last();
                    if (lastTr != null && lastTr.select("td").size() == 1) {
                        pollBean.setFooter(lastTr.select("td").first().html());
                    }

                    pollBean.setPollOptions(options);
                    detail.setPoll(pollBean);
                }
            }


            Element divActionEl = postE.select("div.pob").first();
            if (divActionEl != null) {
                detail.setRateable(divActionEl.select("a.rateicon").size() > 0);
                detail.setCommentable(divActionEl.select("a.cmmnt").size() > 0);
                detail.setSupportable(divActionEl.select("a.replyadd").size() > 0);
                if (detail.isSupportable()) {
                    Element supportEl = divActionEl.select("a.replyadd").first();
                    if (supportEl != null)
                        detail.setSupportCount(Utils.getIntFromString(supportEl.text()));
                    Element againstEl = divActionEl.select("a.replysubtract").first();
                    if (supportEl != null)
                        detail.setAgainstCount(Utils.getIntFromString(againstEl.text()));
                }
            }

            details.add(detail);
        }
        return details;
    }

    private static Map<String, String> parseTradeInfo(Element divTradeInfo) {
        Map<String, String> infos = new LinkedHashMap<>();
        Elements tableES = divTradeInfo.select("#" + divTradeInfo.attr("id") + " > div > table  table tr");
        for (Element tr : tableES) {
            Elements tdES = tr.select("td");
            if (tdES.size() == 1) {
                infos.put(tr.text(), "");
            } else if (tdES.size() == 2) {
                infos.put(tdES.get(0).text(), tdES.get(1).text());
            }
        }
        return infos;
    }

    private static RateBean parseRate(Element trEl, int[] scoreIndexes) {
        String uid = "";
        String rator = "";

        Elements tdES = trEl.select("td");
        if (tdES.size() < 3)
            return null;

        Element linkEl = tdES.get(0).select("a").last();
        if (linkEl != null) {
            rator = linkEl.text();
            uid = Utils.getMiddleString(linkEl.attr("href"), "uid=", "&");
        }

        String reason = tdES.get(tdES.size() - 1).text();

        RateBean bean = new RateBean();
        bean.setRator(rator);
        bean.setRatorId(uid);
        bean.setReason(reason);
        if (scoreIndexes[0] > 0)
            bean.setScore1(tdES.get(scoreIndexes[0]).text());
        if (scoreIndexes[1] > 0)
            bean.setScore2(tdES.get(scoreIndexes[1]).text());
        if (scoreIndexes[2] > 0)
            bean.setScore3(tdES.get(scoreIndexes[2]).text());

        return bean;
    }

    public static CommentBean parseComment(Element divComment) {
        String uid = "";
        String author = "";
        String content = "";
        String time = "";
        String tocId = "";
        Element linkEl = divComment.select("a.xi2").first();
        if (linkEl != null) {
            author = linkEl.text();
            uid = Utils.getMiddleString(linkEl.attr("href"), "uid=", "&");
            if (TextUtils.isEmpty(uid))
                uid = Utils.getMiddleString(linkEl.attr("href"), "space-uid-", ".");
            linkEl.remove();
        }
        Element contentDiv = divComment.select("div.psti").first();
        if (contentDiv != null) {
            Element spanTime = contentDiv.select("span.xg1").first();
            if (spanTime != null) {
                time = Utils.getMiddleString(spanTime.text(), "发表于", "回复").trim();
                Element replyLinkEl = spanTime.select("a").first();
                if (replyLinkEl != null)
                    tocId = Utils.getMiddleString(replyLinkEl.attr("href"), "tocid=", "&").trim();
                spanTime.remove();
            }
            //去掉 回复 和 详情 链接
            Elements links = contentDiv.select("a");
            for (Element link : links) {
                if ("回复".equals(link.text()) || "详情".equals(link.text())) {
                    link.remove();
                }
            }
            content = contentDiv.html();
        }

        CommentBean commentBean = new CommentBean();
        commentBean.setAuthor(author);
        commentBean.setUid(uid);
        commentBean.setConent(content);
        commentBean.setTime(time);
        commentBean.setTocId(tocId);
        return commentBean;
    }

    // return true for continue children, false for ignore children
    private static boolean parseNode(Node contentN, DetailBean.Contents content, int level, @NonNull TextStyleHolder textStyles) {

        if (contentN.nodeName().equals("font")) {
            Element elemFont = (Element) contentN;
            textStyles.setColor(level, Utils.nullToText(elemFont.attr("color")).trim());
            return true;
        } else if (contentN.nodeName().equals("i")    //text in an alternate voice or mood
                || contentN.nodeName().equals("u")    //text that should be stylistically different from normal text
                || contentN.nodeName().equals("em")    //text emphasized
                || contentN.nodeName().equals("strike")    //text strikethrough
                || contentN.nodeName().equals("ol")    //ordered list
                || contentN.nodeName().equals("ul")    //unordered list
                || contentN.nodeName().equals("hr")   //a thematic change in the content(h line)
                || contentN.nodeName().equals("blockquote")) {
            textStyles.addStyle(level, contentN.nodeName());
            //continue parse child node
            return true;
        } else if (contentN.nodeName().equals("strong")) {
            String tmp = ((Element) contentN).text();
            String postId = "";
            String tid = "";
            Elements floorLink = ((Element) contentN).select("a[href]");
            if (floorLink.size() > 0) {
                postId = Utils.getMiddleString(floorLink.first().attr("href"), "pid=", "&");
                tid = Utils.getMiddleString(floorLink.first().attr("href"), "ptid=", "&");
            }
            if (tmp.startsWith("回复 ") && tmp.contains("#")) {
                int floor = Utils.getIntFromString(tmp.substring(0, tmp.indexOf("#")));
                String author = tmp.substring(tmp.lastIndexOf("#") + 1).trim();
                if (!TextUtils.isEmpty(author) && HiUtils.isValidId(postId) && floor > 0) {
                    content.addGoToFloor(tmp, tid, postId, floor, author);
                    return false;
                }
            }
            textStyles.addStyle(level, contentN.nodeName());
            return true;
        } else if (contentN.nodeName().equals("#text")) {
            //replace  < >  to &lt; &gt; , or they will become to unsupported tag
            String text = ((TextNode) contentN).text()
                    .replace("<", "&lt;")
                    .replace(">", "&gt;");

            TextStyle ts = null;
            if (textStyles.getTextStyle(level - 1) != null)
                ts = textStyles.getTextStyle(level - 1).newInstance();

            Matcher matcher = Utils.URL_PATTERN.matcher(text);

            int lastPos = 0;
            while (matcher.find()) {
                String t = text.substring(lastPos, matcher.start());
                String url = text.substring(matcher.start(), matcher.end());

                if (!TextUtils.isEmpty(t.trim())) {
                    content.addText(t, ts);
                }
                if (url.contains("@") && !url.contains("/")) {
                    content.addEmail(url);
                } else {
                    content.addLink(url, url);
                }
                lastPos = matcher.end();
            }
            if (lastPos < text.length()) {
                String t = text.substring(lastPos);
                if (!TextUtils.isEmpty(t.trim())) {
                    content.addText(t, ts);
                }
            }
            return false;
        } else if (contentN.nodeName().equals("li")) {    // list item
            return true;
        } else if (contentN.nodeName().equals("br")) {    // single line break
            content.addText("<br>");
            return false;
        } else if (contentN.nodeName().equals("p")) {    // paragraph
            Element pE = (Element) contentN;
            if (pE.hasClass("imgtitle")) {
                return false;
            }
            return true;
        } else if (contentN.nodeName().equals("img")) {
            parseImageElement((Element) contentN, content);
            return false;
        } else if (contentN.nodeName().equals("span")) {    // a section in a document
            Elements attachAES = ((Element) contentN).select("a");
            Boolean isInternalAttach = false;
            for (int attIdx = 0; attIdx < attachAES.size(); attIdx++) {
                Element attachAE = attachAES.get(attIdx);
                //it is an attachment and not an image attachment
                if (attachAE.attr("href").contains("forum.php?mod=attachment")
                        && !attachAE.attr("href").contains("nothumb=")) {
                    String desc = "";
                    Node sibNode = contentN.nextSibling();
                    if (sibNode != null && sibNode.nodeName().equals("#text")) {
                        desc = sibNode.toString();
                        sibNode.remove();
                    }
                    ContentAttach attach = new ContentAttach(ParserUtil.getAbsoluteUrl(attachAE.attr("href")), attachAE.text(), desc);
                    content.addAttach(attach);
                    isInternalAttach = true;
                }
            }
            if (isInternalAttach) {
                return false;
            }
            return true;
        } else if (contentN.nodeName().equals("a")) {
            Element aE = (Element) contentN;
            String text = aE.text();
            String url = aE.attr("href");
            if (aE.childNodeSize() > 0 && aE.childNode(0).nodeName().equals("img")) {
                if (!url.startsWith("javascript:"))
                    content.addLink(url, url);
                return true;
            }

            if (url.contains("forum.php?mod=attachment")) {
                ContentAttach attach = new ContentAttach(url, text, null);
                content.addAttach(attach);
                return false;
            }

            content.addLink(text, url);
            //rare case, link tag contains images
            Elements imgEs = aE.select("img");
            if (imgEs.size() > 0) {
                for (int i = 0; i < imgEs.size(); i++) {
                    parseImageElement(imgEs.get(i), content);
                }
            }
            return false;
        } else if (contentN.nodeName().equals("div")) {    // a section in a document
            Element divE = (Element) contentN;
            if (divE.hasClass("modact")) {
                content.addNotice(divE.text());
                return false;
            } else if (divE.hasClass("msgbody")) {
                String tid = "";
                String postId = "";
                String author = "";
                String postTime = "";
                Element authorEl = divE.select("font").first();
                if (authorEl != null) {
                    String authorAndTime = authorEl.text();
                    if (authorAndTime.contains("发表于")) {
                        int index = authorAndTime.indexOf("发表于");
                        author = authorAndTime.substring(0, index);
                        postTime = authorAndTime.substring(index + "发表于".length()).trim();
                    }
                }
                Element redirectEl = divE.select("a[href*=findpost]").first();
                if (redirectEl != null) {
                    String href = redirectEl.attr("href");
                    postId = Utils.getMiddleString(href, "pid=", "&");
                    tid = Utils.getMiddleString(href, "ptid=", "&");
                    redirectEl.remove();
                }
                if (authorEl != null)
                    authorEl.remove();
                Element quoteTextEl = divE.select("div.msgborder").first();
                String quoteText;
                if (quoteTextEl != null) {
                    quoteText = quoteTextEl.html();
                    content.addQuote(Utils.clean(quoteText), author, postTime, tid, postId);
                }
                return false;
            } else if (divE.hasClass("attach_popup")) {
                // remove div.attach_popup
                return false;
            }
            return true;
        } else if (contentN.nodeName().equals("ignore_js_op")) {
            ContentAttach attach = parseAttach((Element) contentN);
            if (attach != null) {
                content.addAttach(attach);
                return false;
            }
            return true;
        } else if (contentN.nodeName().equals("table")) {
            return true;
        } else if (contentN.nodeName().equals("tbody")) {    //Groups the body content in a table
            return true;
        } else if (contentN.nodeName().equals("tr")) {    //a row in a table
            content.addText("<br>");
            return true;
        } else if (contentN.nodeName().equals("td")) {    //a cell in a table
            content.addText(" ");
            return true;
        } else if (contentN.nodeName().equals("dl")) {    //a description list
            return true;
        } else if (contentN.nodeName().equals("dt")) {    //a term/name in a description list
            return true;
        } else if (contentN.nodeName().equals("dd")) {    //a description/value of a term in a description list
            return true;
        } else if (contentN.nodeName().equals("script") || contentN.nodeName().equals("#data")) {
            // video
            String html = contentN.toString();
            String url = Utils.getMiddleString(html, "'src', '", "'");
            if (url.startsWith("http://player.youku.com/player.php")) {
                //http://player.youku.com/player.php/sid/XNzIyMTUxMzEy.html/v.swf
                //http://v.youku.com/v_show/id_XNzIyMTUxMzEy.html
                url = Utils.getMiddleString(url, "sid/", "/v.swf");
                url = "http://v.youku.com/v_show/id_" + url;
                if (!url.endsWith(".html")) {
                    url = url + ".html";
                }
                content.addLink("YouKu视频自动转换手机通道 " + url, url);
            } else if (url.startsWith("http")) {
                content.addLink("FLASH VIDEO,手机可能不支持 " + url, url);
            }
            return false;
        } else if (contentN.nodeName().equals("style")) {
            return false;
        } else if (contentN.nodeName().equals("h2")) {
            return true;
        } else {
            if (HiSettingsHelper.getInstance().isErrorReportMode()
                    && !"#comment".equals(contentN.nodeName())) {
                content.addNotice("[[ERROR:UNPARSED TAG:" + contentN.nodeName() + ":" + contentN.toString() + "]]");
                Logger.e("[[ERROR:UNPARSED TAG:" + contentN.nodeName() + "]]");
            }
            return false;
        }
    }

    private static ContentAttach parseAttach(Element contentN) {
        //ignore_js_op Node
        Element spanEl = contentN.select("span[id^=attach_]").first();
        if (spanEl != null) {
            String attachId = Utils.getMiddleString(spanEl.attr("id"), "attach_", "");
            if (!HiUtils.isValidId(attachId)) return null;

            Element linkEl = spanEl.select("a").first();
            if (linkEl == null) return null;
            String url = ParserUtil.getAbsoluteUrl(linkEl.attr("href"));
            String name = linkEl.text();

            Element sizeEl = spanEl.select("em.xg1").first();
            if (sizeEl == null) return null;
            return new ContentAttach(url, name, sizeEl.text());
        }
        return null;
    }

    private static void parseImageElement(Element e, Contents content) {
        String src = ParserUtil.getAbsoluteUrl(e.attr("src"));
        String id = e.attr("id");

        if (id.startsWith("aimg") && src.startsWith(HiUtils.BaseUrl)) {
            //internal image
            long size = 0;
            Elements divES = (e.parent().parent()).select("div#" + id + "_menu");
            if (divES.size() > 0) {
                String sizeText = Utils.getMiddleString(divES.first().text(), "(", ",");
                size = Utils.parseSizeText(sizeText);
                divES.remove();
            }

            ContentImg contentImg = getContentImg(e, size);
            content.addImg(contentImg);
        } else if (src.contains(HiUtils.SmiliesPattern)
                || src.contains(HiUtils.SmiliesPattern2)) {
            //emotion added as img tag, will be parsed in TextViewWithEmoticon later
            content.addText("<img src=\"" + src + "\"/>");
        } else if (src.contains(HiUtils.ForumCommonSmiliesPattern)) {
            //skip common/default/attach icons
        } else if (src.contains("://")) {
            //external image
            content.addImg(src);
        } else {
            content.addNotice("[[ERROR:UNPARSED IMG:" + src + "]]");
        }
    }

    @NonNull
    private static ContentImg getContentImg(Element e, long size) {
        String src = ParserUtil.getAbsoluteUrl(e.attr("src"));
        String file = ParserUtil.getAbsoluteUrl(e.attr("file"));
        String zoomfile = ParserUtil.getAbsoluteUrl(e.attr("zoomfile"));

        String thumbUrl = "";
        if (file.contains(HiUtils.ForumImagePattern)) {
            thumbUrl = file;
        } else if (src.contains(HiUtils.ForumImagePattern)) {
            thumbUrl = src;
        }
        String fullUrl = zoomfile;
        if (!fullUrl.contains(HiUtils.ForumImagePattern))
            fullUrl = thumbUrl;
        return new ContentImg(fullUrl, size, thumbUrl);
    }

}
