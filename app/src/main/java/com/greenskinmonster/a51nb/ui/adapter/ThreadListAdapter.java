package com.greenskinmonster.a51nb.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.RequestManager;
import com.greenskinmonster.a51nb.bean.RecommendThreadBean;
import com.greenskinmonster.a51nb.bean.ThreadBean;
import com.greenskinmonster.a51nb.bean.TradeThreadBean;
import com.greenskinmonster.a51nb.ui.widget.ItemLayout;
import com.greenskinmonster.a51nb.ui.widget.ThreadItemLayout;
import com.greenskinmonster.a51nb.ui.widget.ThreadItemRecommendLayout;
import com.greenskinmonster.a51nb.ui.widget.ThreadItemTradeLayout;

public class ThreadListAdapter extends BaseRvAdapter<ThreadBean> {

    private final static int NORM_ITEM = 0;
    private final static int TRADE_ITEM = 1;
    private final static int RECOMMEND_ITEM = 2;

    private RequestManager mGlide;

    public ThreadListAdapter(RequestManager glide, RecyclerItemClickListener listener) {
        mGlide = glide;
        mListener = listener;
    }

    @Override
    public ViewHolderImpl onCreateViewHolderImpl(ViewGroup parent, int viewType) {
        if (viewType == TRADE_ITEM) {
            return new ViewHolderImpl(new ThreadItemTradeLayout(parent.getContext(), mGlide));
        } else if (viewType == RECOMMEND_ITEM) {
            return new ViewHolderImpl(new ThreadItemRecommendLayout(parent.getContext(), mGlide));
        }
        return new ViewHolderImpl(new ThreadItemLayout(parent.getContext(), mGlide));
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= getDatas().size())
            return super.getItemViewType(position);
        ThreadBean bean = getDatas().get(position);
        if (bean instanceof TradeThreadBean) {
            return TRADE_ITEM;
        } else if (bean instanceof RecommendThreadBean) {
            return RECOMMEND_ITEM;
        }
        return NORM_ITEM;
    }

    @Override
    public void onBindViewHolderImpl(RecyclerView.ViewHolder viewHolder, int position) {
        ThreadBean thread = getItem(position);
        if (viewHolder instanceof ViewHolderImpl)
            ((ViewHolderImpl) viewHolder).mItemLayout.fillData(thread);
    }

    private static class ViewHolderImpl extends RecyclerView.ViewHolder {
        ItemLayout mItemLayout;

        ViewHolderImpl(View itemView) {
            super(itemView);
            mItemLayout = (ItemLayout) itemView;
        }
    }

}
