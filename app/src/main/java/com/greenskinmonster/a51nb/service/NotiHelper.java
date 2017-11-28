package com.greenskinmonster.a51nb.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.greenskinmonster.a51nb.R;
import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.bean.NotificationBean;
import com.greenskinmonster.a51nb.bean.SimpleListBean;
import com.greenskinmonster.a51nb.bean.SimpleListItemBean;
import com.greenskinmonster.a51nb.glide.GlideHelper;
import com.greenskinmonster.a51nb.okhttp.OkHttpHelper;
import com.greenskinmonster.a51nb.parser.HiParser;
import com.greenskinmonster.a51nb.ui.HiApplication;
import com.greenskinmonster.a51nb.ui.IntentActivity;
import com.greenskinmonster.a51nb.utils.Constants;
import com.greenskinmonster.a51nb.utils.HiUtils;
import com.greenskinmonster.a51nb.utils.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * parse and fetch notifications
 * Created by GreenSkinMonster on 2015-09-08.
 */
public class NotiHelper {

    private static long HOLD_FETCH_NOTIFY = 0;
    public final static int NOTI_REPEAT_MINUTTE = 15;
    public final static String DEFAUL_SLIENT_BEGIN = "22:00";
    public final static String DEFAUL_SLIENT_END = "08:00";

    private static NotificationBean mCurrentBean = new NotificationBean();

    public static NotificationBean getCurrentNotification() {
        return mCurrentBean;
    }

    public static void fetchNotification(Document doc) {
        if (System.currentTimeMillis() <= HOLD_FETCH_NOTIFY + 10 * 1000) {
            return;
        }

        int smsCount = -1;
        SimpleListItemBean smsBean = null;
        if (doc == null || HiSettingsHelper.getInstance().isCheckSms()) {
            HiSettingsHelper.getInstance().setLastCheckSmsTime(System.currentTimeMillis());

            try {
                String response = OkHttpHelper.getInstance().get(HiUtils.NewSMS);
                if (!TextUtils.isEmpty(response)) {
                    doc = Jsoup.parse(response);
                    SimpleListBean listBean = HiParser.parseSMS(doc);
                    if (listBean != null) {
                        smsCount = listBean.getCount();
                        if (smsCount == 1) {
                            smsBean = listBean.getAll().get(0);
                        }
                    }
                }
            } catch (Exception e) {
                Logger.e(e);
            }
        }
        if (doc == null) {
            return;
        }
        NotificationBean bean = HiParser.parseNotification(doc);
        if (bean != null) {
            mCurrentBean = bean;
            if (smsCount >= 0) {
                mCurrentBean.setSmsCount(smsCount);
                if (smsCount == 1 && smsBean != null) {
                    mCurrentBean.setUsername(smsBean.getAuthor());
                    mCurrentBean.setUid(smsBean.getAuthorId());
                    mCurrentBean.setContent(smsBean.getTitle());
                } else {
                    mCurrentBean.setUsername("");
                    mCurrentBean.setUid("");
                    mCurrentBean.setContent("");
                }
            }
        }
    }

    public static void showNotification() {
        if (!HiApplication.isAppVisible()) {
            if (mCurrentBean.getTotalNotiCount() > 0) {
                sendNotification();
                HiApplication.setNotified(true);

                //clean count to avoid notification button on start up
                mCurrentBean.setSmsCount(0);
                mCurrentBean.setThreadCount(0);

            } else {
                cancelNotification(HiApplication.getAppContext());
            }
        }
    }

    private static void cancelNotification(Context context) {
        if (HiApplication.isNotified()) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(0);
            HiApplication.setNotified(false);
        }
    }

    private static String getContentText(int notiCount, int smsCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("您有 ");
        if (smsCount > 0)
            sb.append(smsCount).append(" 条新的悄悄话");
        if (smsCount > 0 && notiCount > 0)
            sb.append("， ");
        if (notiCount > 0)
            sb.append(notiCount).append(" 条新的通知");
        return sb.toString();
    }


    public static void sendNotification() {
        NotificationBean bean = NotiHelper.getCurrentNotification();
        Intent intent = new Intent(HiApplication.getAppContext(), IntentActivity.class);
        intent.setAction(Constants.INTENT_NOTIFICATION);
        intent.putExtra(Constants.EXTRA_SMS_COUNT, bean.getSmsCount());
        intent.putExtra(Constants.EXTRA_NOTI_COUNT, bean.getTotalNotiCount());
        if (!TextUtils.isEmpty(bean.getUsername()))
            intent.putExtra(Constants.EXTRA_USERNAME, bean.getUsername());
        if (HiUtils.isValidId(bean.getUid()))
            intent.putExtra(Constants.EXTRA_UID, bean.getUid());
        PendingIntent pendingIntent = PendingIntent.getActivity(HiApplication.getAppContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String title = "51NB论坛提醒";
        String content = getContentText(bean.getTotalNotiCount(), bean.getSmsCount());
        Bitmap icon = null;

        int color = ContextCompat.getColor(HiApplication.getAppContext(), R.color.icon_blue);

        if (bean.getSmsCount() == 1 && bean.getThreadCount() == 0) {
            title = bean.getUsername() + " 的悄悄话";
            content = bean.getContent();
            File avatarFile = GlideHelper.getAvatarFile(HiApplication.getAppContext(), HiUtils.getAvatarUrlByUid(bean.getUid()));
            if (avatarFile != null && avatarFile.exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                icon = BitmapFactory.decodeFile(avatarFile.getPath(), options);
            }
        }

        if (icon == null)
            icon = BitmapFactory.decodeResource(HiApplication.getAppContext().getResources(), R.mipmap.ic_launcher);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(HiApplication.getAppContext())
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.ic_stat_nb)
                .setLargeIcon(icon)
                .setColor(color);

        String sound = HiSettingsHelper.getInstance().getStringValue(HiSettingsHelper.PERF_NOTI_SOUND, "");
        if (!TextUtils.isEmpty(sound))
            builder.setSound(Uri.parse(sound));
        if (HiSettingsHelper.getInstance().isNotiLedLight())
            builder.setLights(color, 1000, 3000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            builder.setPriority(Notification.PRIORITY_HIGH)
                    .setVibrate(new long[0]);
        NotificationManager notificationManager = (NotificationManager) HiApplication.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    public static void scheduleJob() {
        new JobRequest.Builder(NotiJob.TAG)
                .setPeriodic(TimeUnit.MINUTES.toMillis(NOTI_REPEAT_MINUTTE), TimeUnit.MINUTES.toMillis(5))
                .setPersisted(true)
                .setUpdateCurrent(true)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .build()
                .schedule();
    }

    public static void cancelJob() {
        JobManager.instance().cancelAll();
    }

    public static void holdFetchNotify() {
        HOLD_FETCH_NOTIFY = System.currentTimeMillis();
    }

}
