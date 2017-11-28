package com.vanniktech.emoji.emoji;

import com.vanniktech.emoji.R;

/**
 * Created by GreenSkinMonster on 2016-04-11.
 */
public class Default {
    public final static String[] EMOJIS = {
            ":)",
            "^v^",
            "^,^",
            ":'(",

            "^:^",
            "^q^",
            "^^?",
            ":$",

            ":-|",
            ":-^|",
            "^x^",
            ":D",

            ":(",
            ":')",
            ":o",
            "^c^",

            ":bs(",
            "^s^",
            "^t^",
            "^y^"
    };

    public final static String[] IMG_SRCS = {
            "smile",
            "face7",
            "face10",
            "cry",

            "face14",
            "face16",
            "face18",
            "shy",

            "10",
            "11",
            "face4",
            "biggrin",

            "sad",
            "face12",
            "shocked",
            "043",

            "smile_bs",
            "33",
            "titter",
            "51"
    };

    public final static int[] DRAWABLES = {
            R.drawable.default_smile,
            R.drawable.default_face7,
            R.drawable.default_face10,
            R.drawable.default_cry,

            R.drawable.default_face14,
            R.drawable.default_face16,
            R.drawable.default_face18,
            R.drawable.default_shy,

            R.drawable.default_10,
            R.drawable.default_11,
            R.drawable.default_face4,
            R.drawable.default_biggrin,

            R.drawable.default_sad,
            R.drawable.default_face12,
            R.drawable.default_shocked,
            R.drawable.default_043,

            R.drawable.default_smile_bs,
            R.drawable.default_33,
            R.drawable.default_titter,
            R.drawable.default_51
    };

    public final static Emoji[] DATA = new Emoji[EMOJIS.length];

    static {
        for (int i = 0; i < EMOJIS.length; i++) {
            DATA[i] = Emoji.fromEmoji(EMOJIS[i]);
        }
    }
}
