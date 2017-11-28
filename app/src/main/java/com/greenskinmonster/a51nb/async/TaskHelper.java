package com.greenskinmonster.a51nb.async;

import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.db.ContentDao;
import com.greenskinmonster.a51nb.db.HistoryDao;
import com.greenskinmonster.a51nb.utils.Utils;

import java.util.Date;

/**
 * Created by GreenSkinMonster on 2016-07-24.
 */
public class TaskHelper {

    private static final String SETTING_URL = "https://coding.net/u/GreenSkinMonster/p/hipda/git/raw/master/hipda.json";

    public static void runDailyTask(boolean force) {
        String millis = HiSettingsHelper.getInstance()
                .getStringValue(HiSettingsHelper.PERF_LAST_TASK_TIME, "0");
        Date last = null;
        if (millis.length() > 0) {
            try {
                last = new Date(Long.parseLong(millis));
            } catch (Exception ignored) {
            }
        }
        if (force || last == null || System.currentTimeMillis() > last.getTime() + 24 * 60 * 60 * 1000) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ContentDao.cleanup();
                    HistoryDao.cleanup();
                    Utils.cleanPictures();
                    //FavoriteHelper.getInstance().fetchMyFavorites();
                    //FavoriteHelper.getInstance().fetchMyAttention();
                }
            }).start();
            HiSettingsHelper.getInstance()
                    .setStringValue(HiSettingsHelper.PERF_LAST_TASK_TIME, System.currentTimeMillis() + "");
        }
        Date bSyncDate = HiSettingsHelper.getInstance().getBlacklistSyncTime();
        if (force || bSyncDate == null
                || System.currentTimeMillis() > bSyncDate.getTime() + 24 * 60 * 60 * 1000) {
            BlacklistHelper.syncBlacklists();
        }
    }
}
