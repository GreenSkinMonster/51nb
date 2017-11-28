package com.greenskinmonster.a51nb.ui.widget;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.greenskinmonster.a51nb.R;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

/**
 * Created by GreenSkinMonster on 2017-08-18.
 */

public class FooterItem extends AbstractItem<FooterItem, FooterItem.ViewHolder> {

    private boolean mLoading;

    public FooterItem withLoading() {
        withEnabled(false);
        mLoading = true;
        return this;
    }

    public FooterItem withEnd() {
        withEnabled(false);
        mLoading = false;
        return this;
    }

    @Override
    public int getType() {
        return R.id.footer_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.vw_footer_item;
    }

    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);
        holder.mTextView.setVisibility(mLoading ? View.GONE : View.VISIBLE);
        holder.mProgressBar.setVisibility(mLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void unbindView(ViewHolder holder) {
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        ProgressBar mProgressBar;
        TextView mTextView;

        public ViewHolder(View view) {
            super(view);
            mProgressBar = (ProgressBar) view.findViewById(R.id.footer_progressbar);
            mProgressBar.getIndeterminateDrawable()
                    .setColorFilter(Color.LTGRAY, android.graphics.PorterDuff.Mode.SRC_IN);
            mTextView = (TextView) view.findViewById(R.id.footer_text);
        }
    }
}
