package com.greenskinmonster.a51nb.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.greenskinmonster.a51nb.R;
import com.greenskinmonster.a51nb.async.ThreadActionHelper;
import com.greenskinmonster.a51nb.bean.CommentBean;
import com.greenskinmonster.a51nb.bean.CommentListBean;
import com.greenskinmonster.a51nb.bean.DetailBean;
import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.glide.GlideHelper;
import com.greenskinmonster.a51nb.utils.HiUtils;
import com.greenskinmonster.a51nb.utils.UIUtils;
import com.greenskinmonster.a51nb.utils.Utils;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.FooterAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter_extensions.scroll.EndlessRecyclerOnScrollListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GreenSkinMonster on 2017-08-13.
 */

public class CommentsDialog extends Dialog {

    private DetailBean mDetailBean;
    private String mTid;
    private RequestManager mGlide;
    private Context mCtx;

    private RecyclerView mRecyclerView;
    private View.OnClickListener mItemClickListener;
    private FastItemAdapter<CommentItem> mFastAdapter;
    private FooterAdapter<FooterItem> mFooterAdapter;

    private int mCurrentPage = 1;
    private boolean mHasNextPage;
    private boolean mFooterLoading;

    public CommentsDialog(@NonNull Context context) {
        super(context);
        mCtx = context;
        mGlide = Glide.with(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mRecyclerView = new RecyclerView(mCtx);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new SimpleDivider(getContext()));

        mFastAdapter = new FastItemAdapter<>();
        mFooterAdapter = new FooterAdapter<>();

        mRecyclerView.setAdapter(mFooterAdapter.wrap(mFastAdapter));
        List<CommentItem> items = new ArrayList<>();
        for (CommentBean bean : mDetailBean.getCommentLists().getComments()) {
            CommentItem item = new CommentItem(bean);
            items.add(item);
        }
        mFastAdapter.add(items);

        mFastAdapter.withSelectable(true);
        mFastAdapter.withOnClickListener(new FastAdapter.OnClickListener<CommentItem>() {
            @Override
            public boolean onClick(View v, IAdapter<CommentItem> adapter, CommentItem item, int position) {
                v.setTag(R.id.avatar_tag_username, item.getBean().getAuthor());
                mItemClickListener.onClick(v);
                return true;
            }
        });

        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(mFooterAdapter) {
            @Override
            public void onLoadMore(int currentPage) {
                if (!mHasNextPage)
                    return;
                mRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        mFooterAdapter.clear();
                        mFooterAdapter.add(new FooterItem().withLoading());
                        fetchNextPage();
                    }
                });
            }
        });
        setContentView(mRecyclerView);
    }

    public void setDetailBean(String tid, DetailBean detailBean, View.OnClickListener listener) {
        mTid = tid;
        mDetailBean = detailBean;
        mItemClickListener = listener;
        mHasNextPage = detailBean.getCommentLists().isHasNextPage();
    }

    private void fetchNextPage() {
        if (mFooterLoading)
            return;
        mFooterLoading = true;
        mCurrentPage++;
        new AsyncTask<Void, Void, CommentListBean>() {

            @Override
            protected CommentListBean doInBackground(Void... params) {
                return ThreadActionHelper.fetchComments(mTid, mDetailBean.getPostId(), mCurrentPage);
            }

            @Override
            protected void onPostExecute(CommentListBean commentListBean) {
                if (!isShowing())
                    return;
                if (commentListBean != null) {
                    List<CommentItem> items = new ArrayList<>();
                    for (CommentBean bean : commentListBean.getComments()) {
                        CommentItem item = new CommentItem(bean);
                        items.add(item);
                    }
                    mFastAdapter.add(items);
                    mHasNextPage = commentListBean.isHasNextPage();
                }
                mFooterAdapter.clear();
                if (!mHasNextPage) {
                    UIUtils.toast("已全部加载");
                }
                mFooterLoading = false;
            }
        }.execute();
    }


    private class CommentItem extends AbstractItem<CommentItem, CommentItem.ViewHolder> {

        private CommentBean mBean;

        CommentItem(CommentBean bean) {
            mBean = bean;
        }

        public CommentBean getBean() {
            return mBean;
        }

        @Override
        public int getType() {
            return R.id.comment_item;
        }

        @Override
        public int getLayoutRes() {
            return R.layout.item_comment;
        }

        @Override
        public void bindView(CommentItem.ViewHolder holder, List<Object> payloads) {
            super.bindView(holder, payloads);

            holder.tvAuthor.setText(mBean.getAuthor());
            holder.tvTime.setText(Utils.shortyTime(mBean.getTime()));
            holder.tvComment.setRichText(mBean.getConent());
            if (HiSettingsHelper.getInstance().isLoadAvatar()) {
                holder.ivAvatar.setVisibility(View.VISIBLE);
                GlideHelper.loadAvatar(mGlide, holder.ivAvatar, HiUtils.getAvatarUrlByUid(mBean.getUid()));
            } else {
                holder.ivAvatar.setVisibility(View.GONE);
            }
        }

        @Override
        public void unbindView(CommentItem.ViewHolder holder) {
            super.unbindView(holder);
            holder.tvAuthor.setText(null);
            holder.tvTime.setText(null);
            holder.tvComment.setText(null);
        }

        @Override
        public CommentItem.ViewHolder getViewHolder(View v) {
            return new CommentItem.ViewHolder(v);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivAvatar;
            TextView tvAuthor;
            TextViewWithEmoticon tvComment;
            TextView tvTime;

            ViewHolder(View itemView) {
                super(itemView);
                ivAvatar = (ImageView) itemView.findViewById(R.id.iv_avatar);
                tvAuthor = (TextView) itemView.findViewById(R.id.tv_author);
                tvTime = (TextView) itemView.findViewById(R.id.tv_time);
                tvComment = (TextViewWithEmoticon) itemView.findViewById(R.id.tv_comment_content);
            }
        }

    }

}
