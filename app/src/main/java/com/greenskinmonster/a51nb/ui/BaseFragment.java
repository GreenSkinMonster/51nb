package com.greenskinmonster.a51nb.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.greenskinmonster.a51nb.utils.Utils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.listeners.OnEmojiClickedListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;

import java.util.UUID;

/**
 * a base fragment
 * Created by GreenSkinMonster on 2015-05-09.
 */
public abstract class BaseFragment extends Fragment {

    public String mSessionId;
    protected EmojiPopup mEmojiPopup;
    protected IconicsDrawable mKeyboardDrawable;
    protected IconicsDrawable mFaceDrawable;
    protected FloatingActionButton mMainFab;
    protected FloatingActionButton mNotificationFab;

    protected void setActionBarTitle(CharSequence title) {
        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            String t = Utils.nullToText(title);
            if (actionBar != null && !t.equals(actionBar.getTitle())) {
                actionBar.setTitle(t);
            }
        }
    }

    protected void setActionBarTitle(@StringRes int resId) {
        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null)
                actionBar.setTitle(resId);
        }
    }

    protected void setActionBarSubtitle(CharSequence title) {
        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            String t = Utils.nullToText(title);
            if (actionBar != null && !t.equals(actionBar.getTitle())) {
                actionBar.setSubtitle(t);
            }
        }
    }

    void setupFab() {
        if (getActivity() != null) {
            if (mMainFab != null) {
                mMainFab.setVisibility(View.GONE);
            }
            if (mNotificationFab != null) {
                mNotificationFab.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSessionId = UUID.randomUUID().toString();
        setRetainInstance(true);

        if (getActivity() instanceof BaseActivity) {
            BaseActivity activity = ((BaseActivity) getActivity());
            mMainFab = activity.getMainFab();
            mNotificationFab = activity.getNotificationFab();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() instanceof BaseActivity) {
            BaseActivity activity = ((BaseActivity) getActivity());
            if (activity != null) {
                mMainFab = activity.getMainFab();
                mNotificationFab = activity.getNotificationFab();
                setupFab();
            }
        }
    }

    @Override
    public void onDestroy() {
        if (mEmojiPopup != null)
            mEmojiPopup.cleanup();
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        setupFab();
    }

    public void scrollToTop() {
    }

    public void stopScroll() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        stopScroll();
        super.onDestroyView();
    }

    public boolean onBackPressed() {
        return false;
    }

    protected void setUpEmojiPopup(final EmojiEditText mEtContent, final ImageButton mIbEmojiSwitch) {
        if (mKeyboardDrawable == null)
            mKeyboardDrawable = new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_keyboard).sizeDp(28).color(Color.GRAY);
        if (mFaceDrawable == null)
            mFaceDrawable = new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_tag_faces).sizeDp(28).color(Color.GRAY);

        mIbEmojiSwitch.setImageDrawable(mFaceDrawable);
        mIbEmojiSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEmojiPopup.toggle();
            }
        });

        mEtContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEmojiPopup.isShowing())
                    mEmojiPopup.dismiss();
            }
        });

        mEmojiPopup = ((BaseActivity) getActivity()).getEmojiBuilder()
                .setOnEmojiClickedListener(new OnEmojiClickedListener() {
                    @Override
                    public void onEmojiClicked(final Emoji emoji) {
                        mEtContent.requestFocus();
                    }
                }).setOnEmojiPopupShownListener(new OnEmojiPopupShownListener() {
                    @Override
                    public void onEmojiPopupShown() {
                        mIbEmojiSwitch.setImageDrawable(mKeyboardDrawable);
                    }
                }).setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
                    @Override
                    public void onEmojiPopupDismiss() {
                        mIbEmojiSwitch.setImageDrawable(mFaceDrawable);
                    }
                }).setOnSoftKeyboardCloseListener(new OnSoftKeyboardCloseListener() {
                    @Override
                    public void onKeyboardClose() {
                        mEmojiPopup.dismiss();
                    }
                }).build(mEtContent);
    }

}
