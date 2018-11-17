package com.greenskinmonster.a51nb.ui.setting;

import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.greenskinmonster.a51nb.R;
import com.greenskinmonster.a51nb.bean.Forum;
import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.job.ForumChangedEvent;
import com.greenskinmonster.a51nb.parser.ForumParser;
import com.greenskinmonster.a51nb.ui.BaseFragment;
import com.greenskinmonster.a51nb.utils.ColorHelper;
import com.greenskinmonster.a51nb.utils.UIUtils;
import com.greenskinmonster.a51nb.utils.Utils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by GreenSkinMonster on 2017-07-28.
 */

public class AllForumsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG_KEY = "ALL_FORUMS_KEY";

    private LayoutInflater mInflater;
    private View.OnClickListener mCheckboxClickListener;
    private View.OnClickListener mOnClickListener;
    private int mBackgroundResource;
    private boolean mChanged;
    private int mFid;
    private boolean mShowAllForums;

    private RvAdapter mAdapter;
    private FreqRvAdapter mFreqRvAdapter;
    private List<Forum> mForums;
    private List<Forum> mFreqForums;

    private SwipeRefreshLayout mSwipeLayout;
    private Drawable mIbDrawable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mForums = new ArrayList<>();
        addForums();
        mFreqForums = HiSettingsHelper.getInstance().getFreqForums();

        mIbDrawable = new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_close).sizeDp(16).color(Color.GRAY);
    }

    private void addForums() {
        mForums.clear();
        for (Forum forum : HiSettingsHelper.getInstance().getAllForums()) {
            if (mShowAllForums || forum.getLevel() < Forum.SUB_FORUM) {
                mForums.add(forum);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_forums, container, false);
        mInflater = inflater;

        int[] attrs = new int[]{R.attr.selectableItemBackground};
        TypedArray typedArray = getActivity().obtainStyledAttributes(attrs);
        mBackgroundResource = typedArray.getResourceId(0, 0);
        typedArray.recycle();

        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeColors(ColorHelper.getSwipeColor(getActivity()));
        mSwipeLayout.setProgressBackgroundColorSchemeColor(ColorHelper.getSwipeBackgroundColor(getActivity()));

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new RvAdapter();

        mCheckboxClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v instanceof CompoundButton) {
                    int position = (Integer) v.getTag();
                    Forum forum = mForums.get(position);
                    if (((CompoundButton) v).isChecked()) {
                        if (!mFreqForums.contains(forum))
                            mFreqForums.add(forum);
                    } else {
                        mFreqForums.remove(forum);
                    }
                    HiSettingsHelper.getInstance().setFreqForums(mFreqForums);
                    mAdapter.notifyItemChanged(position);
                    mChanged = true;
                }
            }
        };

        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Forum forum = (Forum) v.getTag();
                if (forum != null) {
                    mFid = forum.getId();
                    getActivity().finish();
                }
            }
        };

        recyclerView.setAdapter(mAdapter);
        onRefresh();

        setActionBarTitle(R.string.title_drawer_all_forums);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_all_forums, menu);
        MenuItem sortMenuItem = menu.findItem(R.id.action_sort_freq_forums);
        sortMenuItem.setIcon(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_sort_by_alpha)
                .color(HiSettingsHelper.getInstance().getToolbarTextColor()).actionBar());
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort_freq_forums:
                showSortDialog();
                return true;
            case R.id.action_show_all_forums:
                item.setChecked(!item.isChecked());
                mShowAllForums = item.isChecked();
                addForums();
                mAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRefresh() {
        if (!mSwipeLayout.isRefreshing())
            mSwipeLayout.setRefreshing(true);

//        new AsyncTask<Void, Void, List<Forum>>() {
//
//            @Override
//            protected List<Forum> doInBackground(Void... voids) {
//                return ForumParser.fetchAllForums();
//            }
//
//            @Override
//            protected void onPostExecute(List<Forum> forums) {
//                mSwipeLayout.setRefreshing(false);
//                if (forums != null && forums.size() > 0) {
//                    HiSettingsHelper.getInstance().setAllForums(forums);
//                    addForums();
//                    mAdapter.notifyDataSetChanged();
//                }
//            }
//        }.execute();

    }

    private class RvAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(mInflater.inflate(R.layout.item_forum_list, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            ViewHolder holder = (ViewHolder) viewHolder;

            Forum forum = mForums.get(position);
            holder.itemView.setTag(forum);
            holder.tv_name.setText(forum.getName() + " " + ForumParser.getForumNewPostCount(forum.getId()));

            if (mFreqForums.contains(forum)) {
                holder.tv_name.setTextColor(ColorHelper.getColorAccent(getActivity()));
            } else if (forum.getLevel() == Forum.SUB_FORUM) {
                holder.tv_name.setTextColor(ColorHelper.getTextColorSecondary(getActivity()));
            } else {
                holder.tv_name.setTextColor(ColorHelper.getTextColorPrimary(getActivity()));
            }

            if (forum.getLevel() == Forum.GROUP) {
                holder.cb_forum.setVisibility(View.GONE);
                holder.itemView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.background_grey));
                holder.itemView.setOnClickListener(null);
            } else {
                holder.itemView.setOnClickListener(mOnClickListener);
                holder.itemView.setBackgroundResource(mBackgroundResource);
                holder.cb_forum.setVisibility(View.VISIBLE);
                holder.cb_forum.setChecked(HiSettingsHelper.getInstance().getFreqForums().contains(forum));
                holder.cb_forum.setTag(position);
                holder.cb_forum.setOnClickListener(mCheckboxClickListener);
            }

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.tv_name.getLayoutParams();
            lp.setMargins(Utils.dpToPx(getActivity(), forum.getLevel() * 16), 12, 12, 12);
            holder.tv_name.setLayoutParams(lp);
        }

        @Override
        public int getItemCount() {
            return mForums.size();
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cb_forum;
        TextView tv_name;

        ViewHolder(View itemView) {
            super(itemView);
            cb_forum = (CheckBox) itemView.findViewById(R.id.cb_forum);
            tv_name = (TextView) itemView.findViewById(R.id.tv_name);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mChanged || mFid > 0) {
            HiSettingsHelper.getInstance().setFreqForums(HiSettingsHelper.getInstance().getFreqForums());
            ForumChangedEvent event = new ForumChangedEvent();
            event.mFid = mFid;
            event.mForumChanged = mChanged;
            EventBus.getDefault().postSticky(event);
        }
    }

    private void showSortDialog() {
        RecyclerView recyclerView = new RecyclerView(getActivity());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        mFreqRvAdapter = new FreqRvAdapter();

        ItemTouchHelper.Callback ithCallback = new ItemTouchHelper.Callback() {
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                Collections.swap(mFreqForums, viewHolder.getAdapterPosition(), target.getAdapterPosition());
                mFreqRvAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                        ItemTouchHelper.DOWN | ItemTouchHelper.UP);
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }
        };

        ItemTouchHelper touchHelper = new ItemTouchHelper(ithCallback);
        touchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setAdapter(mFreqRvAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(recyclerView);
        builder.setTitle("常用版块排序");

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                HiSettingsHelper.getInstance().setFreqForums(mFreqForums);
                mChanged = true;
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private class FreqRvAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new FreqViewHolder(mInflater.inflate(R.layout.item_forum_sort, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof FreqViewHolder) {
                TextView tvForum = ((FreqViewHolder) holder).tv_forum;
                ImageButton ibRemove = ((FreqViewHolder) holder).ib_remove;
                final Forum forum = mFreqForums.get(position);
                tvForum.setText(forum.getName());
                tvForum.setTag(position);
                ibRemove.setImageDrawable(mIbDrawable);
                ibRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mFreqForums.size() <= 1) {
                            UIUtils.toast("至少保留一个常用版块");
                            return;
                        }
                        mFreqForums.remove(forum);
                        mFreqRvAdapter.notifyDataSetChanged();
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mFreqForums.size();
        }
    }

    private static class FreqViewHolder extends RecyclerView.ViewHolder {
        TextView tv_forum;
        ImageButton ib_remove;

        FreqViewHolder(View itemView) {
            super(itemView);
            tv_forum = (TextView) itemView.findViewById(R.id.tv_forum);
            ib_remove = (ImageButton) itemView.findViewById(R.id.ib_remove);
        }
    }

}
