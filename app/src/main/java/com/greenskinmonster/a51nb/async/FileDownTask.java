package com.greenskinmonster.a51nb.async;

import android.content.Context;
import android.os.AsyncTask;

import com.greenskinmonster.a51nb.job.JobMgr;
import com.greenskinmonster.a51nb.ui.widget.HiProgressDialog;
import com.greenskinmonster.a51nb.job.GlideImageJob;

/**
 * Created by GreenSkinMonster on 2016-11-27.
 */

public class FileDownTask extends AsyncTask<String, Void, Void> {

    private final Context mContext;
    protected Throwable mException;
    private HiProgressDialog mDialog;

    public FileDownTask(Context context) {
        this.mContext = context;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        mDialog.dismiss();
    }

    @Override
    protected void onPreExecute() {
        mDialog = HiProgressDialog.show(mContext, "请稍候...");
    }

    @Override
    protected Void doInBackground(String... params) {
        String url = params[0];
        try {
            new GlideImageJob(url, JobMgr.PRIORITY_HIGH, null, true).onRun();
        } catch (Throwable ex) {
            mException = ex;
        }
        return null;
    }

}