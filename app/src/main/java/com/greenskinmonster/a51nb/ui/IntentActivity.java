package com.greenskinmonster.a51nb.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.greenskinmonster.a51nb.BuildConfig;
import com.greenskinmonster.a51nb.bean.NotificationBean;
import com.greenskinmonster.a51nb.service.NotiHelper;

/**
 * Created by GreenSkinMonster on 2017-06-22.
 */

public class IntentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent();
    }

    private void handleIntent() {
        Intent intent = new Intent(this, MainFrameActivity.class);
        Intent srcIntent = getIntent();
        if (srcIntent != null) {
            intent.setAction(srcIntent.getAction());
            intent.putExtras(srcIntent);
            intent.setData(srcIntent.getData());
        }

        //to send a test notification
        boolean finished = false;
        if (BuildConfig.DEBUG) {
            NotificationBean bean = NotiHelper.getCurrentNotification();
            if ("test_sms".equals(intent.getAction())) {
                bean.setSmsCount(1);
                bean.setContent("测试悄悄话内容");
                bean.setUsername("绿皮怪兽");
                bean.setUid("31329");
                NotiHelper.sendNotification();
                finished = true;
                finish();
            } else if ("test_noti".equals(intent.getAction())) {
                bean.setThreadCount(1);
                NotiHelper.sendNotification();
                finished = true;
                finish();
            } else if ("test_all".equals(intent.getAction())) {
                bean.setThreadCount(1);
                bean.setSmsCount(1);
                NotiHelper.sendNotification();
                finished = true;
                finish();
            }
        }

        if (!finished) {
            boolean clearActivities = !HiApplication.isAppVisible();
            FragmentArgs args = FragmentUtils.parse(intent);
            if (!clearActivities) {
                clearActivities = args != null && args.getType() == FragmentArgs.TYPE_FORUM;
            }
            if (clearActivities) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                ActivityCompat.startActivity(this, intent, null);
            } else {
                if (args != null) {
                    FragmentUtils.show(this, args);
                }
            }
            finish();
        }
    }
}
