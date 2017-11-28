package com.greenskinmonster.a51nb.job;

import com.greenskinmonster.a51nb.async.PostHelper;
import com.greenskinmonster.a51nb.bean.PostBean;
import com.greenskinmonster.a51nb.bean.PrePostInfoBean;
import com.greenskinmonster.a51nb.ui.HiApplication;
import com.greenskinmonster.a51nb.utils.Constants;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by GreenSkinMonster on 2016-03-28.
 */
public class PostJob extends BaseJob {

    private final static int MIN_JOB_TIME_MS = 500;

    private PostBean mPostArg;
    private PrePostInfoBean mPrePostInfo;
    private int mMode;
    private PostEvent mEvent;

    public PostJob(String sessionId, int mode, PrePostInfoBean prePostInfo, PostBean postArg, boolean fromQuickReply) {

        super(sessionId, JobMgr.PRIORITY_HIGH);

        mPostArg = postArg;
        mPrePostInfo = prePostInfo;
        mMode = mode;

        mEvent = new PostEvent();
        mEvent.mMode = mMode;
        mEvent.mSessionId = mSessionId;
        mEvent.fromQuickReply = fromQuickReply;
    }

    @Override
    public void onAdded() {
        mEvent.mStatus = Constants.STATUS_IN_PROGRESS;
        EventBus.getDefault().postSticky(mEvent);
    }

    @Override
    public void onRun() throws Throwable {
        long start = System.currentTimeMillis();

        PostHelper postHelper = new PostHelper(HiApplication.getAppContext(), mMode, mPrePostInfo, mPostArg);
        PostBean postResult = postHelper.post();

        mEvent.mPostResult = postResult;
        mEvent.mStatus = postResult.getStatus();
        mEvent.mMessage = postResult.getMessage();

        long delta = System.currentTimeMillis() - start;
        if (delta < MIN_JOB_TIME_MS) {
            Thread.sleep(MIN_JOB_TIME_MS - delta);
        }

        EventBus.getDefault().postSticky(mEvent);
    }

}
