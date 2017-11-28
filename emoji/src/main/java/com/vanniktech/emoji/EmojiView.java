package com.vanniktech.emoji;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.vanniktech.emoji.emoji.Comcom1;
import com.vanniktech.emoji.emoji.Comcom2;
import com.vanniktech.emoji.emoji.Comcom3;
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiClickedListener;
import com.vanniktech.emoji.listeners.RepeatListener;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


@SuppressLint("ViewConstructor")
final class EmojiView extends FrameLayout implements ViewPager.OnPageChangeListener {
    private static final int RECENT_INDEX = 0;
    private static final int COMCOM1_INDEX = 1;
    private static final int COMCOM2_INDEX = 2;
    private static final int COMCOM3_INDEX = 3;

    private static final long INITIAL_INTERVAL = TimeUnit.SECONDS.toMillis(1) / 2;
    private static final int NORMAL_INTERVAL = 50;

    @ColorInt
    private final int themeAccentColor;
    @Nullable
    private OnEmojiBackspaceClickListener onEmojiBackspaceClickListener;

    private int emojiTabLastSelectedIndex = -1;

    private final ImageView[] emojiTabs;

    private RecentEmojiGridView recentGridView;

    EmojiView(final Context context, final OnEmojiClickedListener onEmojiClickedListener, @NonNull final RecentEmoji recentEmoji) {
        super(context);

        View.inflate(context, R.layout.emoji_view, this);

        final ViewPager emojisPager = (ViewPager) findViewById(R.id.emojis_pager);
        emojisPager.addOnPageChangeListener(this);

        final List<FrameLayout> views = getViews(context, onEmojiClickedListener, recentEmoji);
        final EmojiPagerAdapter emojisAdapter = new EmojiPagerAdapter(views);
        emojisPager.setAdapter(emojisAdapter);

        emojiTabs = new ImageView[4];
        emojiTabs[RECENT_INDEX] = (ImageView) findViewById(R.id.emojis_tab_0_recent);
        emojiTabs[COMCOM1_INDEX] = (ImageView) findViewById(R.id.emojis_tab_1);
        emojiTabs[COMCOM2_INDEX] = (ImageView) findViewById(R.id.emojis_tab_2);
        emojiTabs[COMCOM3_INDEX] = (ImageView) findViewById(R.id.emojis_tab_3);

        handleOnClicks(emojisPager);

        findViewById(R.id.emojis_backspace).setOnTouchListener(new RepeatListener(INITIAL_INTERVAL, NORMAL_INTERVAL, new OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (onEmojiBackspaceClickListener != null) {
                    onEmojiBackspaceClickListener.onEmojiBackspaceClicked(view);
                }
            }
        }));

        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        themeAccentColor = value.data;

        final int startIndex = recentGridView.numberOfRecentEmojis() > 0 ? RECENT_INDEX : COMCOM1_INDEX;
        emojisPager.setCurrentItem(startIndex);
        onPageSelected(startIndex);
    }

    private void handleOnClicks(final ViewPager emojisPager) {
        for (int i = 0; i < emojiTabs.length; i++) {
            final int position = i;
            emojiTabs[i].setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    emojisPager.setCurrentItem(position);
                }
            });
        }
    }

    public void setOnEmojiBackspaceClickListener(@Nullable final OnEmojiBackspaceClickListener onEmojiBackspaceClickListener) {
        this.onEmojiBackspaceClickListener = onEmojiBackspaceClickListener;
    }

    @NonNull
    private List<FrameLayout> getViews(final Context context, @Nullable final OnEmojiClickedListener onEmojiClickedListener, @NonNull final RecentEmoji recentEmoji) {
        recentGridView = new RecentEmojiGridView(context, recentEmoji).init(onEmojiClickedListener);
        final EmojiGridView comcom1GridView = new EmojiGridView(context).init(Comcom1.DATA, onEmojiClickedListener);
        final EmojiGridView comcom2GridView = new EmojiGridView(context).init(Comcom2.DATA, onEmojiClickedListener);
        final EmojiGridView comcom3GridView = new EmojiGridView(context).init(Comcom3.DATA, onEmojiClickedListener);
        return Arrays.asList(recentGridView, comcom1GridView, comcom2GridView, comcom3GridView);
    }

    @Override
    public void onPageSelected(final int i) {
        if (emojiTabLastSelectedIndex != i) {
            if (i == RECENT_INDEX) {
                recentGridView.invalidateEmojis();
            }

            switch (i) {
                case RECENT_INDEX:
                case COMCOM1_INDEX:
                case COMCOM2_INDEX:
                case COMCOM3_INDEX:
                    if (emojiTabLastSelectedIndex >= 0 && emojiTabLastSelectedIndex < emojiTabs.length) {
                        emojiTabs[emojiTabLastSelectedIndex].setSelected(false);
                        emojiTabs[emojiTabLastSelectedIndex].clearColorFilter();
                    }

                    emojiTabs[i].setSelected(true);
                    emojiTabs[i].setColorFilter(themeAccentColor, PorterDuff.Mode.SRC_IN);

                    emojiTabLastSelectedIndex = i;
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onPageScrolled(final int i, final float v, final int i2) {
        // Don't care
    }

    @Override
    public void onPageScrollStateChanged(final int i) {
        // Don't care
    }
}
