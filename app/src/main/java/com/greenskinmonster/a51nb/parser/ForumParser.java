package com.greenskinmonster.a51nb.parser;

import com.greenskinmonster.a51nb.bean.Forum;
import com.greenskinmonster.a51nb.okhttp.OkHttpHelper;
import com.greenskinmonster.a51nb.utils.HiUtils;
import com.greenskinmonster.a51nb.utils.Logger;
import com.greenskinmonster.a51nb.utils.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by GreenSkinMonster on 2017-07-27.
 */

public class ForumParser {

    private static Map<Integer, String> ForumNewPostCount = new HashMap<>();

    public static List<Forum> fetchAllForums() {
        List<Forum> allForums = new ArrayList<>();
        ForumNewPostCount.clear();
        try {
            String resp = OkHttpHelper.getInstance().get(HiUtils.ForumListUrl);
            Document doc = Jsoup.parse(resp);
            Elements fourmGroups = doc.select("div.bmw");
            for (Element forumGroup : fourmGroups) {
                Element groupLink = forumGroup.select("div.bm_h h2 a").first();
                if (groupLink == null)
                    continue;

                int gid = Utils.getMiddleInt(groupLink.attr("href"), "gid=", "");
                if (gid <= 0)
                    continue;
                String gname = groupLink.text();

                allForums.add(new Forum(gid, Forum.GROUP, gname));

                Elements forumES = forumGroup.select("div[id=category_" + gid + "] tr");
                for (Element forumTrEl : forumES) {
                    List<Forum> forums = parseForum(forumTrEl);
                    for (Forum f : forums) {
                        if (f.getId() > 0)
                            allForums.add(f);
                    }
                }
            }
        } catch (Exception e) {
            Logger.e(e);
        }
        return allForums;
    }

    private static List<Forum> parseForum(Element forumTrEl) {
        if (forumTrEl.children().size() == 7) {
            return parseForumNormal(forumTrEl);
        } else {
            return parseForumCompat(forumTrEl);
        }
    }

    private static List<Forum> parseForumNormal(Element forumEl) {
        //7个TD，第2个是论坛链接
        List<Forum> forums = new ArrayList<>();
        Element forumLink = forumEl.child(1).select("h2 a[href*=forum]").first();
        if (forumLink != null) {
            Forum forum = parseForumLink(forumLink, Forum.FORUM);
            forums.add(forum);
            if (forum.getId() > 0) {
                Element newPostEl = forumLink.nextElementSibling();
                if (newPostEl != null && newPostEl.tagName().equals("em")) {
                    ForumNewPostCount.put(forum.getId(), newPostEl.text());
                }
            }
            forumLink.remove();
            for (Element subForumLink : forumEl.select("p a[href*=forum]")) {
                forums.add(parseForumLink(subForumLink, Forum.SUB_FORUM));
            }
        }
        return forums;
    }

    private static List<Forum> parseForumCompat(Element forumEl) {
        List<Forum> forums = new ArrayList<>();
        for (Element tdEl : forumEl.children()) {
            Element forumLink = tdEl.select("dt a[href*=forum]").first();
            if (forumLink != null) {
                Forum forum = parseForumLink(forumLink, Forum.FORUM);
                forums.add(forum);
                if (forum.getId() > 0) {
                    Element newPostEl = forumLink.nextElementSibling();
                    if (newPostEl != null && newPostEl.tagName().equals("em")) {
                        ForumNewPostCount.put(forum.getId(), newPostEl.text());
                    }
                }
                forumLink.remove();
                for (Element subForumLink : forumEl.select("p a[href*=forum]")) {
                    forums.add(parseForumLink(subForumLink, Forum.SUB_FORUM));
                }
            }
        }
        return forums;
    }

    private static Forum parseForumLink(Element forumLink, int level) {
        String name = forumLink.text();
        int id = ParserUtil.parseForumId(forumLink.attr("href"));
        return new Forum(id, level, name);
    }

    public static String getForumNewPostCount(int fid) {
        return Utils.nullToText(ForumNewPostCount.get(fid));
    }

}
