package com.vanniktech.emoji.emoji;

import com.vanniktech.emoji.R;

/**
 * Created by GreenSkinMonster on 2017-08-17.
 */

public class Comcom2 {
    public final static String[] EMOJIS = {
            "{:1_276:}",
            "{:1_277:}",
            "{:1_278:}",
            "{:1_279:}",
            "{:1_280:}",
            "{:1_281:}",
            "{:1_282:}",
            "{:1_283:}",
            "{:1_284:}",
            "{:1_285:}",
            "{:1_286:}",
            "{:1_287:}",
            "{:1_288:}",
            "{:1_289:}",
            "{:1_290:}",
            "{:1_291:}",
            "{:1_292:}",
            "{:1_293:}",
            "{:1_294:}",
            "{:1_295:}",
            "{:1_296:}",
            "{:1_297:}",
            "{:1_298:}",
            "{:1_299:}",
            "{:1_300:}",
            "{:1_301:}",
            "{:1_302:}",
            "{:1_303:}",
            "{:1_304:}",
            "{:1_305:}",
            "{:1_306:}",
            "{:1_307:}",
            "{:1_308:}",
            "{:1_309:}",
            "{:1_310:}",
            "{:1_311:}",
            "{:1_312:}",
            "{:1_313:}",
            "{:1_314:}",
            "{:1_315:}"
    };

    public final static String[] IMG_SRCS = {
            "em01",
            "em33",
            "em30",
            "em36",
            "em62",
            "em57",
            "em19",
            "em64",
            "em03",
            "em09",
            "em49",
            "em11",
            "em82",
            "em28",
            "em50",
            "em70",
            "em37",
            "em45",
            "em76",
            "em54",
            "em25",
            "em75",
            "em12",
            "em06",
            "em102",
            "em58",
            "em47",
            "em15",
            "em65",
            "em21",
            "em48",
            "em88",
            "em46",
            "em32",
            "em56",
            "em14",
            "em51",
            "em68",
            "em26",
            "em67"
    };

    public final static int[] DRAWABLES = {
            R.drawable.em01,
            R.drawable.em33,
            R.drawable.em30,
            R.drawable.em36,
            R.drawable.em62,
            R.drawable.em57,
            R.drawable.em19,
            R.drawable.em64,
            R.drawable.em03,
            R.drawable.em09,
            R.drawable.em49,
            R.drawable.em11,
            R.drawable.em82,
            R.drawable.em28,
            R.drawable.em50,
            R.drawable.em70,
            R.drawable.em37,
            R.drawable.em45,
            R.drawable.em76,
            R.drawable.em54,
            R.drawable.em25,
            R.drawable.em75,
            R.drawable.em12,
            R.drawable.em06,
            R.drawable.em102,
            R.drawable.em58,
            R.drawable.em47,
            R.drawable.em15,
            R.drawable.em65,
            R.drawable.em21,
            R.drawable.em48,
            R.drawable.em88,
            R.drawable.em46,
            R.drawable.em32,
            R.drawable.em56,
            R.drawable.em14,
            R.drawable.em51,
            R.drawable.em68,
            R.drawable.em26,
            R.drawable.em67
    };

    public final static Emoji[] DATA = new Emoji[EMOJIS.length];

    static {
        for (int i = 0; i < EMOJIS.length; i++) {
            DATA[i] = Emoji.fromEmoji(EMOJIS[i]);
        }
    }
}
