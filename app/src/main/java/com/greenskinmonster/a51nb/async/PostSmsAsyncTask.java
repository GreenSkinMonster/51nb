package com.greenskinmonster.a51nb.async;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.greenskinmonster.a51nb.okhttp.ParamsMap;
import com.greenskinmonster.a51nb.utils.Constants;
import com.greenskinmonster.a51nb.utils.HiUtils;
import com.greenskinmonster.a51nb.utils.Logger;
import com.greenskinmonster.a51nb.utils.UIUtils;
import com.greenskinmonster.a51nb.utils.Utils;

import static com.greenskinmonster.a51nb.okhttp.OkHttpHelper.getErrorMessage;
import static com.greenskinmonster.a51nb.okhttp.OkHttpHelper.getInstance;

public class PostSmsAsyncTask extends AsyncTask<String, Void, Void> {

    private static long LAST_SMS_TIME = 0;
    private static final long SMS_DELAY_IN_SECS = 15;

    private Context mCtx;
    private String mUid;
    private String mUsername;
    private String mPmid;

    private String mFormhash;
    private int mStatus = Constants.STATUS_FAIL;
    private String mResult = "";
    private SmsPostListener mPostListenerCallback;
    private AlertDialog mDialog;

    public PostSmsAsyncTask(Context ctx, String formhash, String pmid, String uid, String username, SmsPostListener postListener, AlertDialog dialog) {
        mCtx = ctx;
        mFormhash = formhash;
        mPmid = pmid;
        mUid = uid;
        mUsername = username;
        mPostListenerCallback = postListener;
        mDialog = dialog;
    }

    @Override
    protected Void doInBackground(String... arg0) {
        String content = arg0[0];

        ParamsMap params = new ParamsMap();
        String url;
        if (!TextUtils.isEmpty(mPmid)) {
            url = HiUtils.SMSSendUrl;
            url = url.replace("{pmid}", mPmid);
            params.put("formhash", mFormhash);
            params.put("message", content);
            params.put("topmuid", mUid);
        } else {
            if (TextUtils.isEmpty(mUid)) {
                mUid = "0";
            }
            url = HiUtils.SMSSendToUidUrl.replace("{uid}", mUid);
            params.put("formhash", mFormhash);
            params.put("message", content);
            params.put("pmsubmit", "true");
            if (HiUtils.isValidId(mUid)) {
                params.put("touid", mUid);
            } else {
                params.put("username", mUsername);
            }
        }

        params.put("formhash", mFormhash);
        params.put("message", content);
        params.put("topmuid", mUid);

        String response;
        try {
            response = getInstance().post(url, params);

            //response is in xml format
            if (TextUtils.isEmpty(response)) {
                mResult = "发送失败 :  无返回结果";
            } else if (response.contains("errorhandle_pmsend('")) {
                mResult = Utils.getMiddleString(response, "errorhandle_pmsend('", "'");
                mResult = mResult.replace("，点击这里查看权限", "");
            } else {
                mResult = "发送成功.";
                mStatus = Constants.STATUS_SUCCESS;
                LAST_SMS_TIME = System.currentTimeMillis();
            }
        } catch (Exception e) {
            Logger.e(e);
            mResult = "发送失败 :  " + getErrorMessage(e);
        }
        return null;
    }

    public static int getWaitTimeToSendSms() {
        long delta = (System.currentTimeMillis() - LAST_SMS_TIME) / 1000;
        if (SMS_DELAY_IN_SECS > delta) {
            return (int) (SMS_DELAY_IN_SECS - delta);
        }
        return 0;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mPostListenerCallback != null) {
            mPostListenerCallback.onSmsPrePost();
        } else {
            UIUtils.toast("正在发送...");
        }
    }

    @Override
    protected void onPostExecute(Void avoid) {
        super.onPostExecute(avoid);
        if (mPostListenerCallback != null) {
            mPostListenerCallback.onSmsPostDone(mStatus, mResult, mDialog);
        } else {
            UIUtils.toast(mResult);
        }
    }

    public interface SmsPostListener {
        void onSmsPrePost();

        void onSmsPostDone(int status, String message, AlertDialog dialog);
    }

}
