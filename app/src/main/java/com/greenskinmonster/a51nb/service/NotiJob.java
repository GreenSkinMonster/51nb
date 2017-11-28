package com.greenskinmonster.a51nb.service;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.greenskinmonster.a51nb.async.LoginHelper;
import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.ui.HiApplication;
import com.greenskinmonster.a51nb.utils.Logger;

/**
 * Created by GreenSkinMonster on 2017-07-19.
 */

public class NotiJob extends Job {

    public static final String TAG = "noti_job_tag";

    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        if (!LoginHelper.isLoggedIn()) {
            NotiHelper.cancelJob();
        } else {
            if (!HiApplication.isAppVisible()
                    && !HiSettingsHelper.getInstance().isInSilentMode()) {
                HiSettingsHelper.getInstance().setNotiJobLastRunTime();
                checkNotifications();
            }
        }
        return Result.SUCCESS;
    }

    private void checkNotifications() {
        try {
            NotiHelper.fetchNotification(null);
            NotiHelper.showNotification();
        } catch (Exception e) {
            Logger.e(e);
        }
    }

}
