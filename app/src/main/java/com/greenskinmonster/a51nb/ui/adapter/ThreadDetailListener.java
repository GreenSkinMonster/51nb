package com.greenskinmonster.a51nb.ui.adapter;

import android.view.View;
import android.widget.Button;

/**
 * Created by GreenSkinMonster on 2017-08-12.
 */

public interface ThreadDetailListener {

    RecyclerItemClickListener getRecyclerItemClickListener();

    Button.OnClickListener getGotoFloorListener();

    View.OnClickListener getAvatarListener();

    View.OnClickListener getMenuListener();

    View.OnClickListener getCommentListener();

    View.OnClickListener getViewAllCommemtsLisener();

    View.OnClickListener getVotePollListener();

    View.OnClickListener getReplyListener();

}
