package com.greenskinmonster.a51nb.job;

import com.greenskinmonster.a51nb.bean.PostBean;

/**
 * Created by GreenSkinMonster on 2016-03-28.
 */
public class PostEvent extends BaseEvent {
    public PostBean mPostResult;
    public int mMode;
    public boolean fromQuickReply;
}
