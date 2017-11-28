package com.vanniktech.emoji.emoji;

import com.vanniktech.emoji.R;

/**
 * Created by GreenSkinMonster on 2017-08-17.
 */

public class Comcom3 {
    public final static String[] EMOJIS = {
            "{:1_316:}",
            "{:1_317:}",
            "{:1_318:}",
            "{:1_319:}",
            "{:1_320:}",
            "{:1_321:}",
            "{:1_322:}",
            "{:1_323:}",
            "{:1_324:}",
            "{:1_325:}",
            "{:1_326:}",
            "{:1_327:}",
            "{:1_328:}",
            "{:1_329:}",
            "{:1_330:}",
            "{:1_331:}",
            "{:1_332:}",
            "{:1_333:}",
            "{:1_334:}",
            "{:1_335:}",
            "{:1_336:}",
            "{:1_337:}",
            "{:1_338:}",
            "{:1_339:}",
            "{:1_340:}",
            "{:1_341:}",
            "{:1_342:}",
            "{:1_343:}",
            "{:1_344:}",
            "{:1_345:}",
            "{:1_346:}"
    };

    public final static String[] IMG_SRCS = {
            "em111",
            "em61",
            "em43",
            "em106",
            "em86",
            "em107",
            "em08",
            "em04",
            "em89",
            "em42",
            "em27",
            "em05",
            "em90",
            "em18",
            "em104",
            "em85",
            "em99",
            "em40",
            "em97",
            "em93",
            "em31",
            "em13",
            "em59",
            "em41",
            "em22",
            "em72",
            "em103",
            "em17",
            "em52",
            "em105",
            "em02"
    };

    public final static int[] DRAWABLES = {
            R.drawable.em111,
            R.drawable.em61,
            R.drawable.em43,
            R.drawable.em106,
            R.drawable.em86,
            R.drawable.em107,
            R.drawable.em08,
            R.drawable.em04,
            R.drawable.em89,
            R.drawable.em42,
            R.drawable.em27,
            R.drawable.em05,
            R.drawable.em90,
            R.drawable.em18,
            R.drawable.em104,
            R.drawable.em85,
            R.drawable.em99,
            R.drawable.em40,
            R.drawable.em97,
            R.drawable.em93,
            R.drawable.em31,
            R.drawable.em13,
            R.drawable.em59,
            R.drawable.em41,
            R.drawable.em22,
            R.drawable.em72,
            R.drawable.em103,
            R.drawable.em17,
            R.drawable.em52,
            R.drawable.em105,
            R.drawable.em02
    };

    public final static Emoji[] DATA = new Emoji[EMOJIS.length];

    static {
        for (int i = 0; i < EMOJIS.length; i++) {
            DATA[i] = Emoji.fromEmoji(EMOJIS[i]);
        }
    }
}
