package com.greenskinmonster.a51nb.async;

import android.text.TextUtils;

import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.bean.UserBean;
import com.greenskinmonster.a51nb.okhttp.OkHttpHelper;
import com.greenskinmonster.a51nb.okhttp.ParamsMap;
import com.greenskinmonster.a51nb.parser.HiParser;
import com.greenskinmonster.a51nb.utils.HiUtils;
import com.greenskinmonster.a51nb.utils.Logger;
import com.greenskinmonster.a51nb.utils.UIUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;

import okhttp3.Request;

/**
 * Created by GreenSkinMonster on 2017-07-14.
 */

public class BlacklistHelper {

    public static void addBlacklist(String formhash, final String username, OkHttpHelper.ResultCallback callback) {
        ParamsMap params = new ParamsMap();
        params.put("formhash", formhash);
        params.put("username", username);
        params.put("blacklistsubmit", "true");
        params.put("blacklistsubmit_btn", "true");
        try {
            OkHttpHelper.getInstance().asyncPost(HiUtils.AddBlackUrl, params, callback);
        } catch (Exception e) {
            UIUtils.toast(OkHttpHelper.getErrorMessage(e).getMessage());
        }
    }

    public static void delBlacklist(final String uid, OkHttpHelper.ResultCallback callback) {
        try {
            OkHttpHelper.getInstance().asyncGet(HiUtils.DelBlackUrl.replace("{uid}", uid), callback);
        } catch (Exception e) {
            callback.onError(null, e);
        }
    }

    public static void getBlacklists(OkHttpHelper.ResultCallback callback) {
        OkHttpHelper.getInstance().asyncGet(HiUtils.ViewBlackUrl, callback);
    }

    public static void syncBlacklists() {
        if (!LoginHelper.isLoggedIn())
            return;
        BlacklistHelper.getBlacklists(new OkHttpHelper.ResultCallback() {
            @Override
            public void onError(Request request, Exception e) {
            }

            @Override
            public void onResponse(String response) {
                try {
                    Document doc = Jsoup.parse(response);
                    String errorMsg = HiParser.parseErrorMessage(doc);
                    if (TextUtils.isEmpty(errorMsg)) {
                        List<UserBean> list = HiParser.parseBlacklist(doc);
                        HiSettingsHelper.getInstance().setBlacklists(list);
                        HiSettingsHelper.getInstance().setBlacklistSyncTime();
                    }
                } catch (Exception e) {
                    Logger.e(e);
                }
            }
        });
    }

}
