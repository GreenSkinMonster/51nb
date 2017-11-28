package com.greenskinmonster.a51nb.async;

import android.text.TextUtils;

import com.greenskinmonster.a51nb.okhttp.OkHttpHelper;
import com.greenskinmonster.a51nb.okhttp.ParamsMap;
import com.greenskinmonster.a51nb.utils.HiUtils;
import com.greenskinmonster.a51nb.utils.Logger;
import com.greenskinmonster.a51nb.utils.UIUtils;
import com.greenskinmonster.a51nb.utils.Utils;

import okhttp3.Request;

/**
 * Created by GreenSkinMonster on 2017-07-27.
 */

public class QianDaoHelper {

    public static void qiandao(String formhash) {
        try {
            ParamsMap params = new ParamsMap();
            params.put("formhash", formhash);
            params.put("qdxq", "kx");
            OkHttpHelper.getInstance().asyncPost(HiUtils.QianDaoUrl, params, new OkHttpHelper.ResultCallback() {
                @Override
                public void onError(Request request, Exception e) {
                    Logger.e(e);
                    UIUtils.toast(OkHttpHelper.getErrorMessage(e).getMessage());
                }

                @Override
                public void onResponse(String response) {
                    String message = Utils.getMiddleString(response, "<div class=\"c\">", "</div>");
                    if (TextUtils.isEmpty(message)) {
                        message = "返回未知签到结果";
                    }
                    UIUtils.toast(message.trim());
                }
            });
        } catch (Exception e) {
            Logger.e(e);
            UIUtils.toast(OkHttpHelper.getErrorMessage(e).getMessage());
        }
    }

}
