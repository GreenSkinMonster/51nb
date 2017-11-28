package com.vanniktech.emoji;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Spannable;
import android.text.style.ImageSpan;

import com.vanniktech.emoji.emoji.Comcom1;
import com.vanniktech.emoji.emoji.Comcom2;
import com.vanniktech.emoji.emoji.Comcom3;
import com.vanniktech.emoji.emoji.Default;

import java.util.HashMap;
import java.util.Map;

public final class EmojiHandler {
    private static final Map<String, Integer> EMOJIS_MAP = new HashMap<>(Default.EMOJIS.length + Comcom1.EMOJIS.length + Comcom2.EMOJIS.length + Comcom3.EMOJIS.length);
    private static final Map<String, Integer> DRAWABLE_MAP = new HashMap<>(Default.EMOJIS.length + Comcom1.EMOJIS.length + Comcom2.EMOJIS.length + Comcom3.EMOJIS.length);
    private static Map<String, Bitmap> IMAGE_MAP;

    private final static String IMG_MATCH_START = "[attachimg]";
    private final static String IMG_MATCH_END = "[/attachimg]";

    public static void init() {
        EMOJIS_MAP.clear();
        DRAWABLE_MAP.clear();
        for (int i = 0; i < Default.EMOJIS.length; i++) {
            EMOJIS_MAP.put(Default.EMOJIS[i], Default.DRAWABLES[i]);
            DRAWABLE_MAP.put(Default.IMG_SRCS[i], Default.DRAWABLES[i]);
        }
        for (int i = 0; i < Comcom1.EMOJIS.length; i++) {
            EMOJIS_MAP.put(Comcom1.EMOJIS[i], Comcom1.DRAWABLES[i]);
            DRAWABLE_MAP.put(Comcom1.IMG_SRCS[i], Comcom1.DRAWABLES[i]);
        }
        for (int i = 0; i < Comcom2.EMOJIS.length; i++) {
            EMOJIS_MAP.put(Comcom2.EMOJIS[i], Comcom2.DRAWABLES[i]);
            DRAWABLE_MAP.put(Comcom2.IMG_SRCS[i], Comcom2.DRAWABLES[i]);
        }
        for (int i = 0; i < Comcom3.EMOJIS.length; i++) {
            EMOJIS_MAP.put(Comcom3.EMOJIS[i], Comcom3.DRAWABLES[i]);
            DRAWABLE_MAP.put(Comcom3.IMG_SRCS[i], Comcom3.DRAWABLES[i]);
        }
    }

    public static void addEmojis(final Context context, final Spannable text, final int emojiSize) {
        final int textLength = text.length();

        // remove spans throughout all text
        final EmojiSpan[] oldSpans = text.getSpans(0, textLength, EmojiSpan.class);
        // noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < oldSpans.length; i++) {
            text.removeSpan(oldSpans[i]);
        }

        int index = 0;
        while (index < textLength) {
            int icon = 0;
            Bitmap bitmap = null;
            int skip = 1;

            String matchStr = text.subSequence(index, Math.min(index + 32, textLength)).toString();
            if (EMOJIS_MAP.containsKey(matchStr)) {
                icon = EMOJIS_MAP.get(matchStr);
                skip = matchStr.length();
            } else if (matchStr.charAt(0) == ':' || matchStr.charAt(0) == '^') {
                //match default emoji, max length is 12
                for (int j = 0; j < Default.DATA.length; j++) {
                    if (matchStr.startsWith(Default.EMOJIS[j])) {
                        skip = Default.EMOJIS[j].length();
                        icon = Default.DRAWABLES[j];
                        break;
                    }
                }
            } else if (matchStr.length() >= 9
                    && matchStr.subSequence(0, 2).equals("{:")) {
                //other emoji, fixed length = 8
                String emoji = matchStr.substring(0, 9);
                if (EMOJIS_MAP.containsKey(emoji)) {
                    skip = emoji.length();
                    icon = EMOJIS_MAP.get(emoji);
                }
            } else if (IMAGE_MAP != null
                    && IMAGE_MAP.size() > 0
                    && matchStr.startsWith(IMG_MATCH_START)
                    && matchStr.indexOf(IMG_MATCH_END) > 0) {
                String imgId = matchStr.substring(IMG_MATCH_START.length(), matchStr.indexOf(IMG_MATCH_END));
                bitmap = IMAGE_MAP.get(imgId);
                if (bitmap != null) {
                    skip = IMG_MATCH_START.length() + imgId.length() + IMG_MATCH_END.length();
                }
            }

            if (icon > 0) {
                text.setSpan(new EmojiSpan(context, icon, emojiSize), index, index + skip, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (bitmap != null) {
                text.setSpan(new ImageSpan(context, bitmap), index, index + skip, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            index += skip;
        }
    }

    public static void addImage(String imgId, Bitmap bitmap) {
        if (IMAGE_MAP == null)
            IMAGE_MAP = new HashMap<>();
        IMAGE_MAP.put(imgId, bitmap);
    }

    public static int getDrawableResId(String imgSrc) {
        return DRAWABLE_MAP.containsKey(imgSrc) ? DRAWABLE_MAP.get(imgSrc) : 0;
    }

    private EmojiHandler() {
        throw new AssertionError("No instances.");
    }

    public static void cleanup() {
        if (IMAGE_MAP == null)
            return;
        for (String key : IMAGE_MAP.keySet()) {
            Bitmap bitmap = IMAGE_MAP.get(key);
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
                bitmap = null;
            }
        }
        IMAGE_MAP.clear();
    }
}
