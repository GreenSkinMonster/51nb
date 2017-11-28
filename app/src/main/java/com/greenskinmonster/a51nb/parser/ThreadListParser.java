package com.greenskinmonster.a51nb.parser;

import android.text.TextUtils;

import com.greenskinmonster.a51nb.async.PostHelper;
import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.bean.RecommendThreadBean;
import com.greenskinmonster.a51nb.bean.ThreadBean;
import com.greenskinmonster.a51nb.bean.ThreadListBean;
import com.greenskinmonster.a51nb.bean.TradeThreadBean;
import com.greenskinmonster.a51nb.utils.HiUtils;
import com.greenskinmonster.a51nb.utils.Logger;
import com.greenskinmonster.a51nb.utils.Utils;
import com.vdurmont.emoji.EmojiParser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ThreadListParser {

    public static ThreadListBean parse(Document doc, int fid) throws Exception {
        ThreadListBean threads = new ThreadListBean();
        threads.setFormhash(HiParser.parseFormhash(doc));
        threads.setTypes(parseForumTypes(doc));

        Element tableEl = doc.select("table#threadlisttableid").first();
        if (tableEl == null) return null;

        int typeIndex = -1;
        int viewCountIndex = -1;
        int subjectIndex = -1;
        int authorIndex = -1;
        int replyCountIndex = -1;
        int threadTimeIndex = -1;

        int priceIndex = -1;
        int locationIndex = -1;
        int tdSize = -1;

        Elements tbodyES = tableEl.select("tbody[id*=thread_]");
        for (int i = 0; i < tbodyES.size(); ++i) {
            Element tbodyE = tbodyES.get(i);
            ThreadBean thread;
            if (fid != HiUtils.FID_TRADE) {
                thread = new ThreadBean();
            } else {
                thread = new TradeThreadBean();
            }

            Elements tdthES = tbodyE.select("td,th");
            if (i == 0) {
                if (fid != HiUtils.FID_TRADE) {
                    if (tdthES.size() == 8) {
                        tdSize = 8;
                        typeIndex = 0;
                        viewCountIndex = 1;
                        subjectIndex = 2;
                        authorIndex = 3;
                        replyCountIndex = 4;
                        threadTimeIndex = 6;
                    } else if (tdthES.size() == 9) {
                        tdSize = 9;
                        typeIndex = 0;
                        viewCountIndex = 1;
                        subjectIndex = 3;
                        authorIndex = 4;
                        replyCountIndex = 5;
                        threadTimeIndex = 7;
                    } else {
                        throw new Exception("帖子解析错误");
                    }
                } else {
                    if (tdthES.size() == 10) {
                        tdSize = 10;
                        typeIndex = 0;
                        viewCountIndex = 1;
                        subjectIndex = 2;
                        authorIndex = 3;
                        priceIndex = 4;
                        locationIndex = 5;
                        replyCountIndex = 6;
                        threadTimeIndex = 8;

                    } else if (tdthES.size() == 11) {
                        tdSize = 11;
                        typeIndex = 0;
                        viewCountIndex = 1;
                        subjectIndex = 3;
                        authorIndex = 4;
                        priceIndex = 5;
                        locationIndex = 6;
                        replyCountIndex = 7;
                        threadTimeIndex = 9;
                    } else {
                        throw new Exception("帖子解析错误");
                    }
                }
            }

            if (tdthES.size() != tdSize) {
                continue;
            }

            if (tbodyE.attr("id").startsWith("stickthread")) {
                thread.setStick(true);
                if (!HiSettingsHelper.getInstance().isShowStickThreads())
                    continue;
            }

            Element titleTh = tdthES.get(subjectIndex);
            if (titleTh == null)
                continue;
            Elements linksES = titleTh.select("a[href*=viewthread]");
            Element titleLink = linksES.first();
            if (titleLink == null) {
                continue;
            }
            String title = titleLink.text();
            thread.setTitle(EmojiParser.parseToUnicode(title));
            String tid = ParserUtil.parseTid(titleLink.attr("href"));
            if (!HiUtils.isValidId(tid)) {
                continue;
            }
            thread.setTid(tid);

            thread.setReadPerm(Utils.getMiddleString(titleTh.text(), "[阅读权限 ", "]").trim());
            thread.setCredit(Utils.getMiddleString(titleTh.text(), "[自动悬赏 ", "]").trim());

            if (linksES.size() > 0) {
                int lastpage = Utils.parseInt(Utils.getMiddleString(linksES.last().attr("href"), "page=", "&"));
                if (lastpage > 1)
                    thread.setMaxPage(lastpage);
            }

            String linkStyle = titleLink.attr("style");
            if (!TextUtils.isEmpty(linkStyle)) {
                thread.setTitleColor(Utils.getMiddleString(linkStyle, "color:", ";").trim());
            }

            // attachment and picture
            Elements imageAttachs = titleTh.select("img[alt=attach_img]");
            Elements attachments = titleTh.select("img[alt=attachment]");
            thread.setHaveImage(imageAttachs.size() > 0);
            thread.setHaveAttach(attachments.size() > 0);

            Elements threadIsNewES = tbodyE.select("td.folder img");
            if (threadIsNewES.size() > 0) {
                String imgSrc = Utils.nullToText(threadIsNewES.first().attr("src"));
                thread.setNew(imgSrc.contains("new"));
            }

            Element openLinkEl = tdthES.get(typeIndex).select("a").first();
            if (openLinkEl != null) {
                String atitle = openLinkEl.attr("title");
                if (!TextUtils.isEmpty(atitle)) {
                    if (atitle.contains("投票")) {
                        thread.setSpecial(PostHelper.SPECIAL_POLL);
                    } else if (atitle.contains("商品")) {
                        thread.setSpecial(PostHelper.SPECIAL_TRADE);
                    }
                    if (atitle.contains("新回复")) {
                        thread.setNew(true);
                    }
                    if (atitle.contains("关闭的主题")) {
                        thread.setLocked(true);
                    }
                }
            }

            //  author, authorId
            Element authorLinkEl = tdthES.get(authorIndex).select("cite a").first();
            String author = authorLinkEl.text();
            thread.setAuthor(author);

            String userLink = authorLinkEl.attr("href");
            String authorId = Utils.getMiddleString(userLink, "uid=", "&");
            if (!HiUtils.isValidId(authorId)) {
                continue;
            }
            if (HiSettingsHelper.getInstance().isInBlacklist(authorId))
                continue;
            thread.setAuthorId(authorId);

            //发帖时间
            String threadCreateTime = tdthES.get(threadTimeIndex).text();
            thread.setCreateTime(threadCreateTime);

            thread.setReplyCount(Utils.parseInt(tdthES.get(replyCountIndex).text()));
            thread.setViewCount(Utils.parseInt(tdthES.get(viewCountIndex).text()));

            if (thread instanceof TradeThreadBean) {
                ((TradeThreadBean) thread).setPrice(tdthES.get(priceIndex).text());
                ((TradeThreadBean) thread).setLocation(tdthES.get(locationIndex).text());
            }

            threads.add(thread);
        }

        return threads;
    }

    public static ThreadListBean parseTradeForum(Document doc) {
        ThreadListBean threads = new ThreadListBean();
        threads.setFormhash(HiParser.parseFormhash(doc));
        threads.setTypes(parseForumTypes(doc));

        Element divCt = doc.select("div#ct > div.mn").first();
        if (divCt == null) return null;

        Elements pagesES = divCt.select("div#pgt div.pg");
        int last_page = 1;
        int page = 1;
        if (pagesES.size() > 0) {
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

        List<TradeThreadBean> tradeItems = new ArrayList<>();
        List<TradeThreadBean> memberItems = new ArrayList<>();

        for (Element el : divCt.children()) {
            if (HiSettingsHelper.getInstance().isShowStickThreads() && page == 1 && "table".equals(el.tagName())) {
                Element trHeader = el.select("tr.header").first();
                if (trHeader != null && trHeader.children().size() == 6) {
                    Elements trES = el.select("tr");
                    for (Element trEl : trES) {
                        if (trEl.hasClass("header")) continue;
                        parseStickTradeItem(threads, trEl);
                    }
                }
            }
            if ("div".equals(el.tagName()) && "threadlist".equals(el.attr("id"))) {
                Elements traderTrES = divCt.select("#moderate > table > tbody > tr > td:nth-child(1) > table tr");
                for (Element trEl : traderTrES) {
                    TradeThreadBean bean = parseTradeItem(trEl, true);
                    if (bean != null)
                        tradeItems.add(bean);
                }

                Elements memberTrES = divCt.select("#moderate > table > tbody > tr > td:nth-child(2) > table tr");
                for (Element trEl : memberTrES) {
                    TradeThreadBean bean = parseTradeItem(trEl, false);
                    if (bean != null)
                        memberItems.add(bean);
                }
            }
        }

        for (int i = 0; i < Math.max(tradeItems.size(), memberItems.size()); i++) {
            if (i < tradeItems.size()) {
                threads.add(tradeItems.get(i));
            }
            if (i < memberItems.size()) {
                threads.add(memberItems.get(i));
            }
        }

        return threads;
    }

    private static TradeThreadBean parseTradeItem(Element trEl, boolean isTrader) {
        Elements tds = trEl.children();
        if (tds.size() == 3) {
            Element linkEl = tds.get(0).select("a").first();
            if (linkEl == null)
                return null;
            String title = linkEl.text();
            String tid = ParserUtil.parseTid(linkEl.attr("href"));
            String price = tds.get(1).text();
            Element authorEl = tds.get(2).select("a").first();
            String author = "", authorId = "";
            if (authorEl != null) {
                author = authorEl.text();
                authorId = Utils.getMiddleString(authorEl.attr("href"), "uid=", "&");
            }
            TradeThreadBean bean = new TradeThreadBean();
//            bean.setStick(title.contains("【公告】"));
//            if (bean.isStick() && !HiSettingsHelper.getInstance().isShowStickThreads()) {
//                return null;
//            }
            bean.setTraderType(isTrader ? TradeThreadBean.TRADER : TradeThreadBean.MEMBER);
            bean.setTitle(title);
            bean.setTid(tid);
            bean.setPrice(price);
            bean.setTid(tid);
            bean.setAuthor(author);
            bean.setAuthorId(authorId);

            String tdTitle = tds.get(0).attr("title");
            bean.setCreateTime(Utils.getMiddleString(tdTitle, "发布时间：", "\n").trim());
            bean.setLocation(Utils.getMiddleString(tdTitle, "交易地点：", "\n").trim());
            bean.setReplyTime(Utils.getMiddleString(tdTitle, "最后回复：", "by").trim());
            bean.setReplyCount(Utils.getMiddleInt(tdTitle, "回复帖子：", "\n"));
            int lastLineIndex = tdTitle.lastIndexOf("\n");
            if (lastLineIndex > 0)
                bean.setReplier(Utils.getMiddleString(tdTitle.substring(lastLineIndex), "by", "").trim());
            return bean;
        }
        return null;
    }

    private static void parseStickTradeItem(ThreadListBean threads, Element trEl) {
        //推介商品	价格	商家	推介商品	价格	商家
        Elements tdES = trEl.select("td");
        Logger.e(tdES.size() + "");
        if (tdES.size() == 6 || tdES.size() == 3) {
            for (int i = 0; i < tdES.size() / 3; i++) {
                TradeThreadBean bean = new TradeThreadBean();
                bean.setStick(true);
                bean.setTraderType(TradeThreadBean.TRADER);
                Element linkEl = tdES.get(i * 3 + 0).select("a").first();
                if (linkEl == null) continue;
                String title = linkEl.text();
                String tid = ParserUtil.parseTid(linkEl.attr("href"));
                bean.setTitle(title);
                bean.setTid(tid);
                String price = tdES.get(i * 3 + 1).text();
                String author = tdES.get(i * 3 + 2).text();
                bean.setPrice(price);
                bean.setAuthor(author);
                threads.add(bean);
            }
        }
    }


    public static ThreadListBean parseRecommendForum(Document doc) {

        ThreadListBean threads = new ThreadListBean();
        threads.setFormhash(HiParser.parseFormhash(doc));
        threads.setTypes(parseForumTypes(doc));

        Element tableList = doc.select("table#threadlisttableid").first();
        if (tableList == null) return null;
        Elements tdES = tableList.select("#threadlisttableid > tbody > tr > td");
        for (Element tdEl : tdES) {
            Element linkEl = tdEl.select("a[href*=viewthread]").first();
            if (linkEl == null) continue;

            RecommendThreadBean bean = new RecommendThreadBean();
            bean.setTitle(linkEl.html());
            bean.setTid(Utils.getMiddleString(linkEl.attr("href"), "tid=", "&"));

            Element postInfoEl = tdEl.select("div.titleinfo").first();
            if (postInfoEl != null)
                bean.setPostInfo(postInfoEl.text());

            Element contentEl = tdEl.select("div.p_excerpt").first();
            if (contentEl != null) {
                String content = contentEl.html();
                int attachimgIndex = content.indexOf("[attachimg]");
                if (attachimgIndex != -1) {
                    int attachimgEndIndex = content.indexOf("[/attachimg]", attachimgIndex);
                    if (attachimgEndIndex != -1) {
                        content = content.replace(
                                content.substring(attachimgIndex, attachimgEndIndex + "[/attachimg]".length()),
                                "");
                    }
                }
                bean.setContent(content);
            }

            Element imageEl = tdEl.select("div.conRightPic img").first();
            if (imageEl != null)
                bean.setItemImageUrl(ParserUtil.getAbsoluteUrl(imageEl.attr("src")));

            Element itemLinkEl = tdEl.select("div.bugBlock a").first();
            if (itemLinkEl != null)
                bean.setItemUrl(itemLinkEl.attr("href"));

            threads.add(bean);
        }
        return threads;
    }

    private static Map<String, String> parseForumTypes(Document doc) {
        Map<String, String> types = new LinkedHashMap<>();
        Elements typeLinkES = doc.select("div#second1 a");
        for (Element typeLinkEl : typeLinkES) {
            typeLinkEl.select("span").remove();
            String href = typeLinkEl.attr("href");
            String typeId = Utils.getMiddleString(href, "typeid=", "&");
            String typeName = typeLinkEl.text();
            if (!TextUtils.isEmpty(typeId) && !TextUtils.isEmpty(typeName)) {
                if (types.size() == 0)
                    types.put("", "全部");
                types.put(typeId, typeName);
            }
        }
        return types;
    }

}
