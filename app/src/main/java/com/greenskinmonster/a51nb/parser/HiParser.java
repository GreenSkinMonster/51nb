package com.greenskinmonster.a51nb.parser;

import android.text.TextUtils;

import com.greenskinmonster.a51nb.async.FavoriteHelper;
import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.bean.NotificationBean;
import com.greenskinmonster.a51nb.bean.SimpleListBean;
import com.greenskinmonster.a51nb.bean.SimpleListItemBean;
import com.greenskinmonster.a51nb.bean.SimplePostItemBean;
import com.greenskinmonster.a51nb.bean.UserBean;
import com.greenskinmonster.a51nb.bean.UserInfoBean;
import com.greenskinmonster.a51nb.job.SimpleListJob;
import com.greenskinmonster.a51nb.service.NotiHelper;
import com.greenskinmonster.a51nb.utils.HiUtils;
import com.greenskinmonster.a51nb.utils.HtmlCompat;
import com.greenskinmonster.a51nb.utils.Utils;
import com.vdurmont.emoji.EmojiParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HiParser {

    public static SimpleListBean parseSimpleList(int type, Document doc) {

        NotiHelper.fetchNotification(doc);

        switch (type) {
            case SimpleListJob.TYPE_MYPOST:
                return parseMyPost(doc);
            case SimpleListJob.TYPE_SMS:
                return parseSMS(doc);
            case SimpleListJob.TYPE_THREAD_NOTIFY:
                return parseNotify(doc);
            case SimpleListJob.TYPE_SMS_DETAIL:
                return parseSmsDetail(doc);
            case SimpleListJob.TYPE_SEARCH:
                return parseSearch(doc);
            case SimpleListJob.TYPE_NEW_POSTS:
                return parseGuide(doc);
            case SimpleListJob.TYPE_SEARCH_USER_THREADS:
                return parseSearch(doc);
            case SimpleListJob.TYPE_FAVORITES:
                return parseFavorites(doc);
        }

        return null;
    }

    public static SimpleListBean parseGuide(Document doc) {
        if (doc == null) {
            return null;
        }

        SimpleListBean list = new SimpleListBean();

        Elements pagesES = doc.select("div#pgt div.pg");
        int last_page = 1;
        if (pagesES.size() != 0) {
            for (Node n : pagesES.first().childNodes()) {
                int tmp = Utils.getIntFromString(((Element) n).text());
                if (tmp > last_page) {
                    last_page = tmp;
                }
            }
        }
        list.setMaxPage(last_page);

        Elements tbodyES = doc.select("div#threadlist tbody[id]");
        for (int i = 0; i < tbodyES.size(); ++i) {

            Element tbodyE = tbodyES.get(i);
            SimpleListItemBean thread = new SimpleListItemBean();

			/* title and tid */
            String[] idSpil = tbodyE.attr("id").split("_");
            if (idSpil.length != 2) {
                continue;
            }
            String idType = idSpil[0];
            String idNum = idSpil[1];

            thread.setTid(idNum);

            Element titleTh = tbodyE.select("th").first();
            if (titleTh == null)
                continue;
            Elements titleES = titleTh.select("a[href*=viewthread]");
            if (titleES.size() == 0) {
                continue;
            }
            Element titleLink = titleES.first();
            String title = titleLink.text();
            thread.setTitle(EmojiParser.parseToUnicode(title));

            if (titleES.size() > 1) {
                int lastpage = Utils.parseInt(Utils.getMiddleString(titleES.attr("href"), "page=", "&"));
//                if (lastpage > 1)
//                    thread.setMaxPage(lastpage);
            }

            String linkStyle = titleLink.attr("style");
//            if (!TextUtils.isEmpty(linkStyle)) {
//                thread.setTitleColor(Utils.getMiddleString(linkStyle, "color:", ";").trim());
//            }

            // attachment and picture
            Elements imageAttachs = titleTh.select("th img[alt=attach_img]");
            Elements attachments = titleTh.select("th img[alt=attachment]");
            thread.setHaveImage(imageAttachs.size() > 0);
            thread.setHaveAttach(attachments.size() > 0);

            Elements tdES = tbodyE.select("td");
            if (tdES.size() != 5) {
                continue;
            }
            //共5个td
            //0，帖子类型
            //1，版块
            //2，作者，发帖时间
            //3，回复数，查看数
            //4，最后回帖用户，及时间

//            Elements typeES = tbodyE.select("th.subject em a");
//            if (typeES.size() > 0) {
//                thread.setType(typeES.text());
//            }

            Elements threadIsNewES = tbodyE.select("td.folder img");
            if (threadIsNewES.size() > 0) {
                String imgSrc = Utils.nullToText(threadIsNewES.first().attr("src"));
                thread.setNew(imgSrc.contains("new"));
            }

            //2，作者，发帖时间
            Element authorLinkEl = tdES.get(2).select("cite a").first();
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

            thread.setForum(tdES.get(1).text());

            //发帖时间
            String threadCreateTime = tdES.get(2).select("em").text();
            thread.setCreateTime(threadCreateTime);

            Elements threadUpdateTimeES = tbodyE.select("td.lastpost em a");
            if (threadUpdateTimeES.size() > 0) {
                String threadUpdateTime = threadUpdateTimeES.first().text();
                thread.setCreateTime(threadUpdateTime);
            }

            //3，回复数，查看数
            thread.setReplyCount(Utils.parseInt(tdES.get(3).select("a").text()));
            thread.setViewCount(Utils.parseInt(tdES.get(3).select("em").text()));

            //4，最后回帖用户，及时间
            String lastReplier = tdES.get(4).select("cite").text();
            thread.setReplier(lastReplier);

            list.add(thread);
        }
        return list;
    }

    private static SimpleListBean parseMyPost(Document doc) {
        if (doc == null) {
            return null;
        }

        //tr class=th header
        //tr child(td/th) size=5 主题
        //tr td size=1 回复内容
        Elements trES = doc.select("form#delform table tr");
        SimpleListBean list = new SimpleListBean();

        Element nextPageLink = doc.select("div.pg a.nxt").first();
        if (nextPageLink != null)
            list.setMaxPage(Utils.getMiddleInt(nextPageLink.attr("href"), "page=", "&"));

        //first tr is title, skip
        for (int i = 1; i < trES.size(); ++i) {
            Elements tdES = trES.get(i).select("td,th");
            if (tdES.size() == 5) {
                //0, icon
                //1, 主题，页数
                //2, 版块
                //3, 回复/查看
                //4, 最后发帖作者,时间

                SimpleListItemBean item = new SimpleListItemBean();

                String title;
                String tid;
                String pid;
                Elements linkES = tdES.get(1).select("a");
                if (linkES.size() > 0) {
                    title = linkES.first().text();
                    String href = linkES.first().attr("href");
                    tid = Utils.getMiddleString(href, "tid=", "&");
                    pid = Utils.getMiddleString(href, "pid=", "&");
                } else {
                    continue;
                }
                Element forumLinkEl = tdES.get(2).select("a").first();
                //int fid = Utils.getMiddleInt(forumLinkEl.attr("href"), "fid=", "&");
                String forum = forumLinkEl.text();

                //int replyCount = Utils.parseInt(tdES.get(3).select("a").first().text());
                //int viewCount = Utils.parseInt(tdES.get(3).select("em").text());

                String time = tdES.get(4).select("em a").text();

                item.setTid(tid);
                item.setPid(pid);
                item.setTitle(title);
                item.setCreateTime(time);
                item.setForum(forum);

                list.add(item);
            } else if (tdES.size() == 1) {
                Elements postLinks = tdES.first().select("a[href*=pid]");
                for (Element postLink : postLinks) {
                    String href = postLink.attr("href");
                    String tid = Utils.getMiddleString(href, "ptid=", "&");
                    String pid = Utils.getMiddleString(href, "pid=", "&");
                    SimpleListItemBean lastItem = list.getAll().get(list.getCount() - 1);
                    if (lastItem != null && tid.equals(lastItem.getTid())) {
                        SimplePostItemBean postItem = new SimplePostItemBean();
                        postItem.setContent(postLink.text());
                        postItem.setPostId(pid);
                        lastItem.addPostItem(postItem);
                        lastItem.setPid(pid);
                        if (!TextUtils.isEmpty(lastItem.getInfo())) {
                            lastItem.setInfo(lastItem.getInfo() + "<br>" + getRedirectUrl(tid, pid, postLink.text()));
                        } else {
                            lastItem.setInfo(getRedirectUrl(tid, pid, postLink.text()));
                        }
                    }

                }
            }
        }
        return list;
    }

    private static String getRedirectUrl(String tid, String pid, String title) {
        return "<a href=\"" + HiUtils.RedirectToPostUrl.replace("{tid}", tid).replace("{pid}", pid) + "\">" + title + "</a>";
    }

    public static SimpleListBean parseSMS(Document doc) {
        if (doc == null) {
            return null;
        }

        Elements pmlistES = doc.select("form#deletepmform dl[id^=pmlist]");
        if (pmlistES.size() == 0) {
            return null;
        }

        SimpleListBean list = new SimpleListBean();
        for (Element dlEl : pmlistES) {
            SimpleListItemBean item = new SimpleListItemBean();

            boolean isNew = dlEl.select("dd.newpm_avt").size() > 0;
            item.setNew(isNew);

            Element spaceLink = dlEl.select("a[href*=\"&uid=\"]").last();
            if (spaceLink == null)
                continue;
            // author and author uid
            item.setAuthor(spaceLink.text());
            item.setAuthorId(Utils.getMiddleString(spaceLink.attr("href"), "uid=", "&"));

            // time
            Element timeEl = dlEl.select("span.xg1").first();
            if (timeEl != null)
                item.setCreateTime(timeEl.text());

            // info
            Element infoEl = dlEl.select("dd.ptm").first();
            if (infoEl == null) {
                continue;
            }
            //get content from 2 brs
            String info = infoEl.html();
            int lastBr = info.lastIndexOf("<br>");
            if (lastBr > 0) {
                String text = HtmlCompat.fromHtml(info.substring(0, lastBr)).toString();
                int returnIndex = text.indexOf("\n");
                if (returnIndex > 0) {
                    String sayTo = text.substring(0, returnIndex).trim();
                    String title = text.substring(returnIndex + 1).trim();
                    item.setSmsSayTo(sayTo);
                    item.setTitle(title);
                } else {
                    item.setTitle(text);
                }
            }
            list.add(item);
        }

        return list;
    }

    public static SimpleListBean parseNotify(Document doc) {
        if (doc == null) {
            return null;
        }

        Elements notiES = doc.select("div#ct div.nts dl.cl");
        if (notiES.size() == 0) {
            return null;
        }

        Element notiTabEl = doc.select("div#ct > div.mn > div > ul > li.a").first();
        String title = notiTabEl.text();
        if (title.contains("("))
            title = title.substring(0, title.indexOf("("));

        SimpleListBean list = new SimpleListBean();
        for (Element notiEl : notiES) {
            SimpleListItemBean item = parseNotificationItem(title, notiEl);
            if (item != null) {
                list.add(item);
            }
        }
        return list;
    }

    private static SimpleListItemBean parseNotificationItem(String title, Element root) {
        SimpleListItemBean item = new SimpleListItemBean();
        item.setTitle(title);
        Element imgEl = root.select("dd.avt img").first();
        if (imgEl != null) {
            if ("systempm".equals(imgEl.attr("alt"))) {
                item.setAuthorId(HiUtils.SYSTEM_UID);
            } else if (imgEl.attr("src").contains("uid=")) {
                item.setAuthorId(Utils.getMiddleString(imgEl.attr("src"), "uid=", "&"));
            }
        }

        Element noticeEl = root.select("dd.ntc_body").first();
        if (noticeEl != null) {
            item.setInfo(noticeEl.html());
            Elements aES = root.select("a[href*=tid]");
            if (aES.size() > 0) {
                item.setTid(Utils.getMiddleString(aES.first().attr("href"), "tid=", "&"));
            }
            if (noticeEl.attr("style").contains("bold")) {
                item.setNew(true);
            }
        }

        Element timeSpanEl = root.select("dt > span.xg1").first();
        if (timeSpanEl != null) {
            item.setCreateTime(timeSpanEl.text());
        }
        return item;
    }

    private static SimpleListBean parseSmsDetail(Document doc) {
        if (doc == null) {
            return null;
        }

        Elements smslistES = doc.select("div#pm_ul dl[id^=pmlist_]");
        if (smslistES.size() < 1) {
            return null;
        }

        SimpleListBean list = new SimpleListBean();

        Element formEl = doc.select("form#pmform").first();
        if (formEl != null) {
            String pmid = Utils.getMiddleString(formEl.attr("action"), "pmid=", "&");
            list.setPmid(pmid);
        }
        list.setFormhash(parseFormhash(doc));

        for (Element smsEl : smslistES) {
            SimpleListItemBean item = new SimpleListItemBean();
            String pmid = Utils.getMiddleString(smsEl.attr("id"), "pmlist_", "");
            if (!HiUtils.isValidId(pmid))
                continue;
            item.setPmid(pmid);

            boolean isNew = smsEl.select("dd.newpm_avt").size() > 0;
            item.setNew(isNew);

            Element spaceLink = smsEl.select("a[href*=\"&uid=\"]").last();
            if (spaceLink == null)
                continue;
            // author and author uid
            item.setAuthor(spaceLink.text());
            item.setAuthorId(Utils.getMiddleString(spaceLink.attr("href"), "uid=", "&"));

            // time
            Element timeEl = smsEl.select("span.xg1").first();
            if (timeEl != null && !TextUtils.isEmpty(timeEl.text()))
                item.setCreateTime(Utils.getMiddleString(timeEl.text(), "发表于 ", ""));

            // info
            Element infoEl = smsEl.select("dd.ptm").first();
            if (infoEl == null) {
                continue;
            }
            String html = infoEl.html();
            int firstBr = html.indexOf("<br>");
            if (firstBr != -1)
                item.setInfo(html.substring(firstBr + 4));

            list.add(item);
        }

        return list;
    }

    private static SimpleListBean parseSearch(Document doc) {
        if (doc == null) {
            return null;
        }

        SimpleListBean list = new SimpleListBean();
        int last_page = 1;

        //if this is the last page, page number is in <strong>
        Elements pagesES = doc.select("div.pg a[href^=search.php]");

        if (pagesES.size() > 0) {
            String searchUrl = pagesES.first().attr("href");
            list.setSearchId(Utils.getMiddleString(searchUrl, "searchid=", "&"));
            for (Element pageLink : pagesES) {
                int page = Utils.parseInt(Utils.getMiddleString(pageLink.attr("href"), "page=", "&"));
                if (last_page < page)
                    last_page = page;
            }
        }
        list.setMaxPage(last_page);

        Elements liES = doc.select("div#threadlist li.pbw");
        for (Element liEl : liES) {
            SimpleListItemBean item = new SimpleListItemBean();

            Elements linkES = liEl.select("a");
            String title = "";
            String tid = "";
            String author = "";
            String uid = "";
            String fid = "";
            String forum = "";
            for (Element linkEl : linkES) {
                String href = linkEl.attr("href");
                if (href.contains("viewthread")) {
                    title = linkEl.text();
                    tid = Utils.getMiddleString(linkEl.attr("href"), "tid=", "&");
                } else if (href.contains("space")) {
                    author = linkEl.text();
                    uid = Utils.getMiddleString(linkEl.attr("href"), "uid=", "&");
                } else if (href.contains("forumdisplay")) {
                    forum = linkEl.text();
                    fid = Utils.getMiddleString(linkEl.attr("href"), "fid=", "&");
                }
            }

            if (TextUtils.isEmpty(title)
                    || TextUtils.isEmpty(author)
                    || TextUtils.isEmpty(forum)
                    || !HiUtils.isValidId(tid)
                    || !HiUtils.isValidId(uid)
                    || !HiUtils.isValidId(fid))
                continue;

            item.setTid(tid);
            item.setTitle(title);
            item.setAuthor(author);
            item.setAuthorId(uid);
            item.setForum(forum);

            String time = "";
            String info = "";
            int viewCount = -1;
            int replyCount = -1;
            Elements pES = liEl.select("p");
            if (pES.size() == 3) {
                String viewAndReply = pES.get(0).text();
                if (viewAndReply.contains("个回复"))
                    replyCount = Utils.parseInt(Utils.trim(viewAndReply.substring(0, viewAndReply.indexOf("个回复"))));
                if (viewAndReply.contains("次查看"))
                    viewCount = Utils.parseInt(Utils.trim(Utils.getMiddleString(viewAndReply, "-", "次查看")));

                info = pES.get(1).text();

                Element spanTime = pES.get(2).select("span").first();
                if (spanTime != null)
                    time = spanTime.text();
            }
            item.setInfo(info);
            item.setViewCount(viewCount);
            item.setReplyCount(replyCount);
            item.setCreateTime(time);

            list.add(item);
        }

        return list;
    }

    private static SimpleListBean parseSearchFullText(Document doc) {
        if (doc == null) {
            return null;
        }

        SimpleListBean list = new SimpleListBean();
        int last_page = 1;

        //if this is the last page, page number is in <strong>
        Elements pagesES = doc.select("div.pages_btns div.pages a");
        pagesES.addAll(doc.select("div.pages_btns div.pages strong"));
        String searchIdUrl;
        if (pagesES.size() > 0) {
            searchIdUrl = pagesES.first().attr("href");
            list.setSearchId(Utils.getMiddleString(searchIdUrl, "searchid=", "&"));
            for (Node n : pagesES) {
                int tmp = Utils.getIntFromString(((Element) n).text());
                if (tmp > last_page) {
                    last_page = tmp;
                }
            }
        }
        list.setMaxPage(last_page);

        Elements tbodyES = doc.select("table.datatable tr");
        for (int i = 0; i < tbodyES.size(); ++i) {
            Element trowE = tbodyES.get(i);
            SimpleListItemBean item = new SimpleListItemBean();

            Elements subjectES = trowE.select("div.sp_title a");
            if (subjectES.size() == 0) {
                continue;
            }
            item.setTitle(subjectES.first().text());
            //gotopost.php?pid=12345
            String postUrl = Utils.nullToText(subjectES.first().attr("href"));
            item.setPid(Utils.getMiddleString(postUrl, "pid=", "&"));
            if (TextUtils.isEmpty(item.getPid())) {
                continue;
            }

            Elements contentES = trowE.select("div.sp_content");
            if (contentES.size() > 0) {
                item.setInfo(contentES.text());
            }

//            <div class="sp_theard">
//            <span class="sp_w200">版块: <a href="forumdisplay.php?fid=2">Discovery</a></span>
//            <span>作者: <a href="space.php?uid=189027">tsonglin</a></span>
//            <span>查看: 1988</span>
//            <span>回复: 56</span>
//            <span class="sp_w200">最后发表: 2015-4-4 21:58</span>
//            </div>
            Elements postInfoES = trowE.select("div.sp_theard span");
            if (postInfoES.size() != 5) {
                continue;
            }
            Elements authorES = postInfoES.get(1).select("a");
            if (authorES.size() > 0) {
                item.setAuthor(authorES.first().text());
                String spaceUrl = authorES.first().attr("href");
                if (!TextUtils.isEmpty(spaceUrl)) {
                    String uid = Utils.getMiddleString(spaceUrl, "uid=", "&");
                    item.setAuthorId(uid);
//                    item.setAvatarUrl(HiUtils.getAvatarUrlByUid(uid));
                }
            }

            item.setCreateTime(Utils.getMiddleString(postInfoES.get(4).text(), ":", "&"));

            Elements forumES = postInfoES.get(0).select("a");
            if (forumES.size() > 0)
                item.setForum(forumES.first().text());

            list.add(item);
        }

        return list;
    }

    private static SimpleListBean parseFavorites(Document doc) {
        if (doc == null) {
            return null;
        }

        SimpleListBean list = new SimpleListBean();

        int last_page = 1;
        //if this is the last page, page number is in <strong>
        Elements pagesES = doc.select("div.pages a");
        pagesES.addAll(doc.select("div.pages strong"));
        if (pagesES.size() > 0) {
            for (Node n : pagesES) {
                int tmp = Utils.getIntFromString(((Element) n).text());
                if (tmp > last_page) {
                    last_page = tmp;
                }
            }
        }
        list.setMaxPage(last_page);

        Elements liEs = doc.select("ul#favorite_ul li[id^=fav_]");
        for (Element liEl : liEs) {
            SimpleListItemBean item = new SimpleListItemBean();

            String favid = Utils.getMiddleString(liEl.attr("id"), "fav_", "");
            Element linkEl = liEl.select("a[href*=viewthread]").first();
            if (linkEl == null)
                continue;
            item.setTitle(linkEl.text());
            String tid = Utils.getMiddleString(linkEl.attr("href"), "tid=", "&");
            item.setTid(tid);

            if (TextUtils.isEmpty(favid) || TextUtils.isEmpty(tid))
                continue;

            FavoriteHelper.getInstance().addToCahce(tid, favid);

            Element timeEl = liEl.select("span.xg1").first();
            if (timeEl != null)
                item.setCreateTime(timeEl.text());

//            Elements forumES = liEl.select("td.forum");
//            if (forumES.size() > 0) {
//                item.setForum(forumES.first().text().trim());
//            }

            list.add(item);
        }

        return list;
    }

    public static UserInfoBean parseUserInfo(String rsp) {
        Document doc = Jsoup.parse(rsp);
        if (doc == null) {
            return null;
        }

        UserInfoBean info = new UserInfoBean();

        Element usernameEl = doc.select("div#uhd h2.mt").first();
        if (usernameEl != null) {
            info.setUsername(Utils.nullToText(usernameEl.text()).trim());
        }

        Element onlineEl = doc.select("h2 > img[alt=online]").first();
        if (onlineEl != null) {
            info.setOnline(true);
        }

        Element uidEl = doc.select("div#uhd a[href*=space]").first();
        if (uidEl != null) {
            info.setUid(Utils.getMiddleString(uidEl.attr("href"), "uid=", "&"));
        }
        info.setFormhash(HiParser.parseFormhash(doc));

        Map<String, String> infos = new LinkedHashMap<>();
        Elements infoES = doc.select("div.pbm, div#psts");
        for (Element infoDiv : infoES) {
            Element keyEl = infoDiv.select("h2").first();
            if (keyEl != null) {
                String title = Utils.nullToText(keyEl.text()).trim();
                if (TextUtils.isEmpty(title)) continue;

                if (title.equals("新浪微博账号")) {
                    Element weiboLink = infoDiv.select("a").first();
                    if (weiboLink != null)
                        infos.put(title, weiboLink.attr("href"));
                    continue;
                } else if (title.equals("勋章")) {
                    Element img = infoDiv.select("img[src*=medal]").first();
                    if (img != null)
                        infos.put(title, img.attr("alt"));
                    continue;
                } else if (title.equals("管理以下版块")) {
                    infos.put(title, infoDiv.select("a").text());
                    continue;
                }
                infos.put(title, "");
                Elements infoLiES = infoDiv.select("li");
                for (Element liEl : infoLiES) {
                    Element infoKeyEl = infoLiES.select("em").first();
                    if (infoKeyEl != null) {
                        String infoKey = infoKeyEl.text().trim();
                        infoKeyEl.remove();
                        String infoValue = liEl.html();
                        if (!TextUtils.isEmpty(infoKey) && !TextUtils.isEmpty(infoValue)) {
                            infos.put(infoKey, infoValue);
                        }
                    }
                }
            }
        }

        info.setInfos(infos);
        return info;
    }

    public static String parseFormhash(Document doc) {
        if (doc == null) {
            return null;
        }
        Elements inputs = doc.select("input[name=formhash]");
        if (inputs.size() > 0)
            return inputs.get(0).val();
        return null;
    }

    public static String parseErrorMessage(Document doc) {
        //div id="messagetext"  (class="alert_error" or class="alert_info")
        Element divMessage = doc.select("div#messagetext p").first();
        if (divMessage != null)
            return divMessage.text();
        return null;
    }

    public static List<UserBean> parseBlacklist(Document doc) throws Exception {
        List<UserBean> blacklists = new ArrayList<>();
        Elements liES = doc.select("div#friend_ul li[id^=friend_]");
        for (Element liEl : liES) {
            String uid = Utils.getMiddleString(liEl.attr("id"), "friend_", "_");
            String username = "";
            Elements usernameLinkES = liEl.select("h4 > a");
            for (Element usernameLink : usernameLinkES) {
                if (!TextUtils.isEmpty(usernameLink.text())) {
                    username = usernameLink.text();
                    break;
                }
            }
            if (!TextUtils.isEmpty(username) && HiUtils.isValidId(uid)) {
                UserBean user = new UserBean();
                user.setUid(uid);
                user.setUsername(username);
                blacklists.add(user);
            }
        }

        if (blacklists.size() == 0) {
            Element divCt = doc.select("div#ct").first();
            if (divCt == null || !divCt.text().contains("没有相关用户列表")) {
                throw new Exception("黑名单数据解析错误");
            }
        }
        return blacklists;
    }

    public static NotificationBean parseNotification(Document doc) {
        if (doc == null) {
            return null;
        }

        NotificationBean bean = new NotificationBean();
        Element umEl = doc.select("div#um").first();
        if (umEl == null)
            return null;

        Element smsLink = umEl.select("a#pm_ntc").first();
        //Element notiLink = umEl.select("a#myprompt").first();

        if (smsLink != null && smsLink.hasClass("new")) {
            bean.setHasSms(true);
        }
        Element qiandaoLink = umEl.select("a[onclick*=dsu_paulsign]").first();
        if (qiandaoLink != null)
            bean.setQiandao(true);

        Element notiMenuEl = doc.select("ul#myprompt_menu").first();
        if (notiMenuEl != null) {
            for (Element liEl : notiMenuEl.select("li")) {
                String liText = liEl.text();
                if (liText.contains("我的帖子")) {
                    bean.setThreadCount(Utils.getMiddleInt(liText, "(", ")"));
                } else if (liText.contains("系统提醒")) {
                    bean.setSysNotiCount(Utils.getMiddleInt(liText, "(", ")"));
                }
            }
        }

        return bean;
    }

    public static Map<String, String> parseWarrantyInfo(String rsp) throws Exception {
        Map<String, String> infos = new LinkedHashMap<>();
        Document doc = Jsoup.parse(rsp);

        String errormsg = HiParser.parseErrorMessage(doc);
        if (!TextUtils.isEmpty(errormsg))
            throw new Exception(errormsg);

        Element tableEl = doc.select("div#wp table").get(1);
        if (tableEl != null) {
            Elements trEs = tableEl.select("tr");
            for (Element trEl : trEs) {
                Elements tdES = trEl.select("td");
                if (tdES.size() == 2 || tdES.size() == 3) {
                    if ("相关信息".equals(tdES.get(0).text())) {
                        infos.put("相关信息", "部件等其它信息请通过网页版查询");
                        break;
                    }
                    infos.put(tdES.get(0).text(), tdES.get(1).text());
                } else if (tdES.size() == 4) {
                    infos.put(tdES.get(0).text(), tdES.get(1).text());
                    infos.put(tdES.get(2).text(), tdES.get(3).text());
                }
            }
        }
        return infos;
    }

}
