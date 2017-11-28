package com.greenskinmonster.a51nb.async;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.greenskinmonster.a51nb.okhttp.OkHttpHelper;
import com.greenskinmonster.a51nb.okhttp.ParamsMap;
import com.greenskinmonster.a51nb.ui.HiApplication;
import com.greenskinmonster.a51nb.utils.HiUtils;
import com.greenskinmonster.a51nb.utils.Logger;
import com.greenskinmonster.a51nb.utils.UIUtils;
import com.greenskinmonster.a51nb.utils.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import okhttp3.Request;


public class FavoriteHelper {

    private final static int MAX_CACHE_PAGE = 3;
    private final static String FAV_CACHE_PREFS = "favorites";
    private final static String FAVORITES_KEY = "favorites";
    private final static String FAVORITE_FAVID_KEY = "favids";

    private SharedPreferences mCachePref;

    private Map<String, String> mFavorties;

    private FavoriteHelper() {
        mCachePref = HiApplication.getAppContext().getSharedPreferences(FAV_CACHE_PREFS, 0);
        String v = mCachePref.getString(FAVORITES_KEY, "");
        try {
            Gson gson = new Gson();
            mFavorties = gson.fromJson(v, new TypeToken<Map<String, String>>() {
            }.getType());
        } catch (Exception e) {
            Logger.e(e);
        }
        if (mFavorties == null)
            mFavorties = new HashMap<>();
    }

    private void saveFavorties() {
        Gson gson = new Gson();
        String v = gson.toJson(mFavorties, new TypeToken<Map<String, String>>() {
        }.getType());
        mCachePref.edit().putString(FAVORITES_KEY, v).apply();
    }

    private static class SingletonHolder {
        static final FavoriteHelper INSTANCE = new FavoriteHelper();
    }

    public static FavoriteHelper getInstance() {
        return SingletonHolder.INSTANCE;
    }

//    public void fetchMyFavorites() {
//        Set<String> favTids = new HashSet<>();
//        for (int i = 1; i <= MAX_CACHE_PAGE; i++) {
//            ParseResult result = fetchMyFavorites(i);
//            if (result.error)
//                return;
//            if (result.tids == null || result.tids.size() == 0)
//                break;
//            favTids.addAll(result.tids);
//            if (i + 1 > result.lastPage)
//                break;
//        }
//        mTidsCache = favTids;
//        SharedPreferences.Editor editor = mCachePref.edit();
//        editor.remove(FAVORITE_TID_KEY).apply();
//        editor.putStringSet(FAVORITE_TID_KEY, mTidsCache).apply();
//    }

    private ParseResult fetchMyFavorites(int page) {
        ParseResult result = new ParseResult();
        if (page <= 1) page = 1;

        String url = HiUtils.FavoritesUrl;
        if (page > 1)
            url += "&page=" + page;

        try {
            String response = OkHttpHelper.getInstance().get(url);
            Document doc = Jsoup.parse(response);
            int last_page = 1;
            //if this is the last page, page number is in <strong>
            Elements divPageES = doc.select("div.pages");
            if (divPageES.size() > 0) {
                Element divPage = divPageES.first();
                Elements pagesES = divPage.select("div.pages a");
                pagesES.addAll(divPage.select("div.pages strong"));
                if (pagesES.size() > 0) {
                    for (Node n : pagesES) {
                        int tmp = Utils.getIntFromString(((Element) n).text());
                        if (tmp > last_page) {
                            last_page = tmp;
                        }
                    }
                }
            }
            result.lastPage = last_page;

            //get favories tid
            Set<String> tids = new HashSet<>();
            Elements checkboxes = doc.select("input.checkbox[name=delete[]]");
            for (Node n : checkboxes) {
                String tid = n.attr("value");
                if (HiUtils.isValidId(tid)) {
                    tids.add(tid);
                }
            }
            result.tids = tids;
        } catch (Exception e) {
            Logger.e(e);
            result.error = true;
        }
        return result;
    }

    public void addToCahce(String tid, String favid) {
        if (HiUtils.isValidId(tid) && HiUtils.isValidId(favid)) {
            mFavorties.put(tid, favid);
            saveFavorties();
        }
    }

    public void removeFromCahce(String tid) {
        mFavorties.remove(tid);
        saveFavorties();
    }

    public void clearAll() {
        mCachePref.edit().clear().apply();
    }

    public boolean isInFavorite(String tid) {
        return mFavorties.containsKey(tid);
    }

    public void addFavorite(String formhash, final String tid) {
        if (TextUtils.isEmpty(formhash) || TextUtils.isEmpty(tid)) {
            UIUtils.toast("参数错误");
            return;
        }
        String url = HiUtils.FavoriteAddUrl.replace("{tid}", tid).replace("{formhash}", formhash);

        OkHttpHelper.getInstance().asyncGet(url, new OkHttpHelper.ResultCallback() {
            @Override
            public void onError(Request request, Exception e) {
                UIUtils.toast("添加失败 : " + OkHttpHelper.getErrorMessage(e));
            }

            @Override
            public void onResponse(String response) {
                String result;
                if (response.contains("succeedhandle")) {
                    result = "信息收藏成功";
                    //{'id':'1791389','favid':'418858'}
                    String favid = Utils.getMiddleString(response, "'favid':'", "'");
                    addToCahce(tid, favid);
                } else {
                    result = Utils.getMiddleString(response, "errorhandle_k_favorite('", "'");
                }
                UIUtils.toast(result);
            }
        });
    }

    public void removeFavorite(String formhash, final String tid) {
        String favid = mFavorties.get(tid);
        if (TextUtils.isEmpty(formhash) || TextUtils.isEmpty(favid)) {
            UIUtils.toast("参数错误");
            return;
        }

        removeFromCahce(tid);

        String url = HiUtils.FavoriteRemoveUrl.replace("{favid}", favid);
        ParamsMap params = new ParamsMap();
        params.put("referer", HiUtils.BaseUrl + "home.php?mod=space&do=favorite&type=all");
        params.put("deletesubmit", "true");
        params.put("formhash", formhash);
        params.put("handlekey", "a_delete_" + tid);
        try {
            OkHttpHelper.getInstance().asyncPost(url, params, new OkHttpHelper.ResultCallback() {
                @Override
                public void onError(Request request, Exception e) {
                    UIUtils.toast("移除失败 : " + OkHttpHelper.getErrorMessage(e));
                }

                @Override
                public void onResponse(String response) {
                    String result;
                    if (response.contains("succeedhandle")) {
                        result = "已取消收藏";
                    } else {
                        result = Utils.getMiddleString(response, "CDATA[", "<");
                    }
                    UIUtils.toast(result);
                }
            });
        } catch (Exception e) {
            UIUtils.toast("移除失败 : " + OkHttpHelper.getErrorMessage(e));
        }
    }

    private class ParseResult {
        Set<String> tids;
        int lastPage = 1;
        boolean error = false;
    }

}