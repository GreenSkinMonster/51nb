package com.greenskinmonster.a51nb.glide;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.StringSignature;
import com.greenskinmonster.a51nb.R;
import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.cache.LRUCache;
import com.greenskinmonster.a51nb.ui.BaseFragment;
import com.greenskinmonster.a51nb.ui.HiApplication;
import com.greenskinmonster.a51nb.utils.HiUtils;
import com.greenskinmonster.a51nb.utils.Logger;
import com.greenskinmonster.a51nb.utils.Utils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class GlideHelper {

    private static LRUCache<String, String> NOT_FOUND_AVATARS = new LRUCache<>(1024);

    private static File AVATAR_CACHE_DIR;

    private static Drawable DEFAULT_USER_ICON;
    public static File SYSTEM_AVATAR_FILE;
    public static File DEFAULT_AVATAR_FILE;
    private final static String AVATAR_VER = "-v1";

    final static long AVATAR_CACHE_MILLS = 3 * 24 * 60 * 60 * 1000;
    final static long AVATAR_404_CACHE_MILLS = 24 * 60 * 60 * 1000;

    private static Map<String, String> AVATAR_CACHE_KEYS = new HashMap<>();

    public static void loadAvatar(BaseFragment fragment, ImageView view, String avatarUrl) {
        if (isOkToLoad(fragment)) {
            loadAvatar(Glide.with(fragment), view, avatarUrl);
        }
    }

    public static void loadAvatar(RequestManager glide, ImageView view, String avatarUrl) {
        String cacheKey = null;
        if (avatarUrl != null)
            cacheKey = AVATAR_CACHE_KEYS.get(avatarUrl);
        if (cacheKey == null) {
            if (avatarUrl == null || NOT_FOUND_AVATARS.containsKey(avatarUrl)) {
                avatarUrl = DEFAULT_AVATAR_FILE.getAbsolutePath();
            }
            cacheKey = avatarUrl;
        }
        if (HiSettingsHelper.getInstance().isCircleAvatar()) {
            glide.load(avatarUrl)
                    .signature(new StringSignature(cacheKey))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .bitmapTransform(new CropCircleTransformation(HiApplication.getAppContext()))
                    .error(DEFAULT_USER_ICON)
                    .crossFade()
                    .into(view);
        } else {
            glide.load(avatarUrl)
                    .signature(new StringSignature(cacheKey))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .centerCrop()
                    .error(DEFAULT_USER_ICON)
                    .crossFade()
                    .into(view);
        }
    }

    public static File getAvatarFile(Context ctx, String avatarUrl) {
        File f = null;
        try {
            FutureTarget<File> future = Glide.with(ctx)
                    .load(avatarUrl)
                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            f = future.get();
            Glide.clear(future);
        } catch (Exception ignored) {
        }
        return f;
    }

    public static void markAvatarNotFound(String avatarUrl) {
        NOT_FOUND_AVATARS.put(avatarUrl, "");
    }

    public static File getAvatarFile(String url) {
        if (url.startsWith(HiUtils.AvatarBaseUrl)) {
            return new File(AVATAR_CACHE_DIR, url.substring(HiUtils.AvatarBaseUrl.length()));
        }
        return null;
    }

    public static void clearAvatarCache(String url) {
        File f = getAvatarFile(url);
        if (f != null && f.exists()) {
            f.delete();
        }
        AVATAR_CACHE_KEYS.put(url, System.currentTimeMillis() + "");
    }

    public static void clearAvatarFiles() throws Exception {
        AVATAR_CACHE_DIR.delete();
    }

    public static boolean isOkToLoad(Context activity) {
        if (activity instanceof Activity) {
            return !Utils.isDestroyed((Activity) activity);
        }
        return true;
    }

    public static boolean isOkToLoad(Fragment fragment) {
        return fragment != null && fragment.getActivity() != null && !fragment.isDetached();
    }

    public static void initDefaultFiles() {
        AVATAR_CACHE_DIR = Glide.getPhotoCacheDir(HiApplication.getAppContext(), "avatar");

        HashMap<String, Drawable> avatars = new HashMap<>();
        avatars.put("circle", ContextCompat.getDrawable(HiApplication.getAppContext(), R.drawable.avatar_circle));
        avatars.put("default", ContextCompat.getDrawable(HiApplication.getAppContext(), R.drawable.avatar_default));
        avatars.put("system", new IconicsDrawable(HiApplication.getAppContext(), GoogleMaterial.Icon.gmd_info_outline).color(Color.LTGRAY).sizeDp(64));

        for (String key : avatars.keySet()) {
            Drawable drawable = avatars.get(key);
            File file = new File(AVATAR_CACHE_DIR, key + AVATAR_VER + ".png");
            if (!file.exists()) {
                try {
                    Bitmap b = drawableToBitmap(drawable);
                    if ("circle".equals(key)) {
                        b = getCroppedBitmap(b);
                    }
                    b.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
//                    b.recycle();
                } catch (Exception e) {
                    Logger.e(e);
                }
            }
        }

        if (HiSettingsHelper.getInstance().isCircleAvatar()) {
            DEFAULT_USER_ICON = avatars.get("circle");
            DEFAULT_AVATAR_FILE = new File(AVATAR_CACHE_DIR, "circle" + AVATAR_VER + ".png");
        } else {
            DEFAULT_USER_ICON = avatars.get("default");
            DEFAULT_AVATAR_FILE = new File(AVATAR_CACHE_DIR, "default" + AVATAR_VER + ".png");
        }
        SYSTEM_AVATAR_FILE = new File(AVATAR_CACHE_DIR, "system" + AVATAR_VER + ".png");
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private static Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

}
