package com.vanniktech.emoji.emoji;

import com.vanniktech.emoji.R;

/**
 * Created by GreenSkinMonster on 2017-08-17.
 */

public class Comcom1 {
    public final static String[] EMOJIS = {
            "{:1_236:}",
            "{:1_237:}",
            "{:1_238:}",
            "{:1_239:}",
            "{:1_240:}",
            "{:1_241:}",
            "{:1_242:}",
            "{:1_243:}",
            "{:1_244:}",
            "{:1_245:}",
            "{:1_246:}",
            "{:1_247:}",
            "{:1_248:}",
            "{:1_249:}",
            "{:1_250:}",
            "{:1_251:}",
            "{:1_252:}",
            "{:1_253:}",
            "{:1_254:}",
            "{:1_255:}",
            "{:1_256:}",
            "{:1_257:}",
            "{:1_258:}",
            "{:1_259:}",
            "{:1_260:}",
            "{:1_261:}",
            "{:1_262:}",
            "{:1_263:}",
            "{:1_264:}",
            "{:1_265:}",
            "{:1_266:}",
            "{:1_267:}",
            "{:1_268:}",
            "{:1_269:}",
            "{:1_270:}",
            "{:1_271:}",
            "{:1_272:}",
            "{:1_273:}",
            "{:1_274:}",
            "{:1_275:}"
    };

    public final static String[] IMG_SRCS = {
            "em78",
            "em80",
            "em94",
            "em77",
            "em39",
            "em79",
            "em16",
            "em81",
            "em101",
            "em66",
            "em29",
            "em84",
            "em73",
            "em63",
            "em55",
            "em10",
            "em100",
            "em35",
            "em110",
            "em83",
            "em69",
            "em20",
            "em07",
            "em71",
            "em98",
            "em34",
            "em74",
            "em108",
            "em24",
            "em87",
            "em44",
            "em92",
            "em60",
            "em96",
            "em91",
            "em95",
            "em23",
            "em109",
            "em53",
            "em38"
    };

    public final static int[] DRAWABLES = {
            R.drawable.em78,
            R.drawable.em80,
            R.drawable.em94,
            R.drawable.em77,
            R.drawable.em39,
            R.drawable.em79,
            R.drawable.em16,
            R.drawable.em81,
            R.drawable.em101,
            R.drawable.em66,
            R.drawable.em29,
            R.drawable.em84,
            R.drawable.em73,
            R.drawable.em63,
            R.drawable.em55,
            R.drawable.em10,
            R.drawable.em100,
            R.drawable.em35,
            R.drawable.em110,
            R.drawable.em83,
            R.drawable.em69,
            R.drawable.em20,
            R.drawable.em07,
            R.drawable.em71,
            R.drawable.em98,
            R.drawable.em34,
            R.drawable.em74,
            R.drawable.em108,
            R.drawable.em24,
            R.drawable.em87,
            R.drawable.em44,
            R.drawable.em92,
            R.drawable.em60,
            R.drawable.em96,
            R.drawable.em91,
            R.drawable.em95,
            R.drawable.em23,
            R.drawable.em109,
            R.drawable.em53,
            R.drawable.em38
    };

    public final static Emoji[] DATA = new Emoji[EMOJIS.length];

    static {
        for (int i = 0; i < EMOJIS.length; i++) {
            DATA[i] = Emoji.fromEmoji(EMOJIS[i]);
        }
    }
}
