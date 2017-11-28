package com.greenskinmonster.a51nb.job;

import android.graphics.Bitmap;
import android.net.Uri;

import com.greenskinmonster.a51nb.async.UploadImgHelper;
import com.greenskinmonster.a51nb.ui.HiApplication;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by GreenSkinMonster on 2016-04-01.
 */
public class ImageUploadJob extends BaseJob implements UploadImgHelper.UploadImgListener {

    private String mHash;
    private Uri[] mUris;
    private Collection<ImageUploadEvent> mHoldEvents = new ArrayList<>();

    public ImageUploadJob(String sessionId, String hash, Uri[] uris) {
        super(sessionId);
        mHash = hash;
        mUris = uris;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        new UploadImgHelper(getApplicationContext(), this, mHash, mUris).upload();

        ImageUploadEvent event = new ImageUploadEvent();
        event.mSessionId = mSessionId;
        event.type = ImageUploadEvent.ALL_DONE;
        event.holdEvents = mHoldEvents;
        EventBus.getDefault().postSticky(event);
    }

    @Override
    public void updateProgress(int total, int current, int percentage) {
        ImageUploadEvent event = new ImageUploadEvent();
        event.mSessionId = mSessionId;
        event.type = ImageUploadEvent.UPLOADING;
        event.total = total;
        event.current = current;
        event.percentage = percentage;
        EventBus.getDefault().postSticky(event);
    }

    @Override
    public void itemComplete(Uri uri, int total, int current, String currentFileName, String message, String imgId, Bitmap thumbtail) {
        UploadImage image = new UploadImage();
        image.setFileName(currentFileName);
        image.setImgId(imgId);
        image.setThumb(thumbtail);
        image.setUri(uri);

        ImageUploadEvent event = new ImageUploadEvent();
        event.mSessionId = mSessionId;
        event.type = ImageUploadEvent.ITEM_DONE;
        event.total = total;
        event.current = current;
        event.message = message;
        event.mImage = image;

        if (HiApplication.isAppVisible()) {
            EventBus.getDefault().post(event);
        } else {
            mHoldEvents.add(event);
        }
    }

}
