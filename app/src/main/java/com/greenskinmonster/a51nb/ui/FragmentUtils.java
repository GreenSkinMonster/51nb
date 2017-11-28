package com.greenskinmonster.a51nb.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import com.greenskinmonster.a51nb.R;
import com.greenskinmonster.a51nb.async.PostHelper;
import com.greenskinmonster.a51nb.job.SimpleListJob;
import com.greenskinmonster.a51nb.ui.setting.PasswordFragment;
import com.greenskinmonster.a51nb.utils.Constants;
import com.greenskinmonster.a51nb.utils.HiUtils;
import com.greenskinmonster.a51nb.utils.Utils;

/**
 * utils to deal with fragments
 * Created by GreenSkinMonster on 2015-09-01.
 */
public class FragmentUtils {

    public static FragmentArgs parse(Intent intent) {
        if (intent != null) {
            if (Constants.INTENT_NOTIFICATION.equals(intent.getAction())) {
                return parseNotification(
                        intent.getIntExtra(Constants.EXTRA_SMS_COUNT, -1),
                        intent.getIntExtra(Constants.EXTRA_NOTI_COUNT, -1),
                        intent.getStringExtra(Constants.EXTRA_UID),
                        intent.getStringExtra(Constants.EXTRA_USERNAME)
                );
            } else if (Constants.INTENT_SMS.equals(intent.getAction())) {
                FragmentArgs args = new FragmentArgs();
                args.setType(FragmentArgs.TYPE_SMS);
                return args;
            } else if (Constants.INTENT_SEARCH.equals(intent.getAction())) {
                FragmentArgs args = new FragmentArgs();
                args.setType(FragmentArgs.TYPE_SEARCH);
                return args;
            } else if (Constants.INTENT_FAVORITE.equals(intent.getAction())) {
                FragmentArgs args = new FragmentArgs();
                args.setType(FragmentArgs.TYPE_FAVORITE);
                return args;
            } else if (Constants.INTENT_NEW_THREAD.equals(intent.getAction())) {
                FragmentArgs args = new FragmentArgs();
                args.setType(FragmentArgs.TYPE_NEW_THREAD);
                return args;
            } else if (Constants.INTENT_NEW_POSTS.equals(intent.getAction())) {
                FragmentArgs args = new FragmentArgs();
                args.setType(FragmentArgs.TYPE_NEW_POSTS);
                return args;
            } else {
                Uri data = intent.getData();
                if (data != null) {
                    return FragmentUtils.parseUrl(data.toString());
                }
            }
        }
        return null;
    }

    private static FragmentArgs parseNotification(int smsCount, int notiCount, String uid, String username) {
        FragmentArgs args = null;
        if (smsCount == 1
                && HiUtils.isValidId(uid)
                && !TextUtils.isEmpty(username)) {
            args = new FragmentArgs();
            args.setType(FragmentArgs.TYPE_SMS_DETAIL);
            args.setUid(uid);
            args.setUsername(username);
        } else if (smsCount > 0) {
            args = new FragmentArgs();
            args.setType(FragmentArgs.TYPE_SMS);
        } else if (notiCount > 0) {
            args = new FragmentArgs();
            args.setType(FragmentArgs.TYPE_THREAD_NOTIFY);
            args.setExtra(SimpleListJob.NOTIFY_UNREAD);
        }
        return args;
    }

    public static FragmentArgs parseUrl(String url) {
        if (TextUtils.isEmpty(url))
            return null;
        if (url.startsWith(HiUtils.ForumServerHttp))
            url = url.replace(HiUtils.ForumServerHttp, HiUtils.ForumServer);

        if (url.startsWith(HiUtils.ForumUrlPattern) || !url.contains("://")) {
            if (url.contains("forum.php?mod=forumdisplay")) {
                int fid = Utils.getMiddleInt(url, "fid=", "&");
                if (HiUtils.isForumValid(fid)) {
                    FragmentArgs args = new FragmentArgs();
                    args.setType(FragmentArgs.TYPE_FORUM);
                    args.setFid(fid);
                    return args;
                }
            } else if (url.contains("forum.php?mod=viewthread")) {
                String tid = Utils.getMiddleString(url, "tid=", "&");
                if (HiUtils.isValidId(tid)) {
                    FragmentArgs args = new FragmentArgs();
                    args.setType(FragmentArgs.TYPE_THREAD);
                    args.setTid(tid);

                    String page = Utils.getMiddleString(url, "page=", "&");
                    if (!TextUtils.isEmpty(page) && TextUtils.isDigitsOnly(page))
                        args.setPage(Integer.parseInt(page));

                    return args;
                }
            } else if (url.contains("forum.php?mod=redirect")) {
                String gotoStr = Utils.getMiddleString(url, "goto=", "&");
                if (!TextUtils.isEmpty(gotoStr)) {
                    if ("lastpost".equals(gotoStr)) {
                        //goto last post
                        String tid = Utils.getMiddleString(url, "tid=", "&");
                        if (HiUtils.isValidId(tid)) {
                            FragmentArgs args = new FragmentArgs();
                            args.setType(FragmentArgs.TYPE_THREAD);

                            args.setTid(tid);
                            args.setPage(ThreadDetailFragment.LAST_PAGE);
                            args.setFloor(ThreadDetailFragment.LAST_FLOOR);

                            return args;
                        }
                    } else if ("findpost".equals(gotoStr)) {
                        //goto specific post by post id
                        String tid = Utils.getMiddleString(url, "ptid=", "&");
                        String postId = Utils.getMiddleString(url, "pid=", "&");

                        if (HiUtils.isValidId(tid) && HiUtils.isValidId(postId)) {
                            FragmentArgs args = new FragmentArgs();
                            args.setType(FragmentArgs.TYPE_THREAD);

                            args.setTid(tid);
                            args.setPostId(postId);

                            return args;
                        }
                    }
                }
            } else if (url.contains("home.php?mod=space")) {
                //goto post by post id
                String uid = Utils.getMiddleString(url, "uid=", "&");

                if (HiUtils.isValidId(uid)) {
                    FragmentArgs args = new FragmentArgs();
                    args.setType(FragmentArgs.TYPE_USER_INFO);
                    args.setUid(uid);
                    return args;
                }
            } else if (url.contains("thread-") && url.contains(".html")) {
                //thread-1620589-2-2.html , {tid}-{page}-?
                String tid = Utils.getMiddleString(url, "thread-", "-");
                if (HiUtils.isValidId(tid)) {
                    FragmentArgs args = new FragmentArgs();
                    args.setType(FragmentArgs.TYPE_THREAD);
                    args.setTid(tid);

                    int page = Utils.getMiddleInt(url, tid + "-", "-");
                    if (page > 0)
                        args.setPage(page);

                    return args;
                }
            } else if (url.contains("forum-") && url.contains(".html")) {
                //forum-thinkpad-1.html
                //forum-54-1.html
                String fidKeyword = Utils.getMiddleString(url, "forum-", "-");
                int fid = 0;
                if (HiUtils.StaticKeywordMap.containsKey(fidKeyword)) {
                    fid = HiUtils.StaticKeywordMap.get(fidKeyword);
                } else if (HiUtils.isValidId(fidKeyword)) {
                    fid = Utils.parseInt(fidKeyword);
                }
                if (HiUtils.isForumValid(fid)) {
                    FragmentArgs args = new FragmentArgs();
                    args.setType(FragmentArgs.TYPE_FORUM);
                    args.setFid(fid);
                    return args;
                }
            }
        }
        return null;
    }

    public static void showForum(FragmentManager fragmentManager, int fid) {
        //show forum always use Transaction.replace
        Bundle argments = new Bundle();
        if (HiUtils.isForumValid(fid))
            argments.putInt(ThreadListFragment.ARG_FID_KEY, fid);
        ThreadListFragment fragment = new ThreadListFragment();
        fragment.setArguments(argments);
        fragmentManager.beginTransaction()
                .replace(R.id.main_frame_container, fragment, fragment.getClass().getName())
                .commitAllowingStateLoss();
    }

    public static void showThreadActivity(Activity activity, boolean skipEnterAnim, String tid, String title, int page, int floor, String pid, int maxPage) {
        Intent intent = new Intent(activity, ThreadDetailActivity.class);
        intent.putExtra(ThreadDetailFragment.ARG_TID_KEY, tid);
        intent.putExtra(ThreadDetailFragment.ARG_TITLE_KEY, title);
        intent.putExtra(ThreadDetailFragment.ARG_MAX_PAGE_KEY, maxPage);
        if (page != -1)
            intent.putExtra(ThreadDetailFragment.ARG_PAGE_KEY, page);
        if (floor != -1)
            intent.putExtra(ThreadDetailFragment.ARG_FLOOR_KEY, floor);
        if (HiUtils.isValidId(pid))
            intent.putExtra(ThreadDetailFragment.ARG_PID_KEY, pid);

        ActivityCompat.startActivity(activity, intent, getAnimBundle(activity, skipEnterAnim));
    }

    public static Bundle getAnimBundle(Activity activity, boolean skipEnterAnim) {
        ActivityOptionsCompat options;
        if (skipEnterAnim) {
            options = ActivityOptionsCompat.makeCustomAnimation(activity, R.anim.activity_open_enter, 0);
        } else {
            options = ActivityOptionsCompat.makeCustomAnimation(activity, R.anim.slide_in_right, 0);
        }
        return options.toBundle();
    }

    public static void showUserInfoActivity(Activity activity, boolean skipEnterAnim, String uid, String username) {
        Intent intent = new Intent(activity, UserInfoActivity.class);
        intent.putExtra(UserinfoFragment.ARG_UID, uid);
        intent.putExtra(UserinfoFragment.ARG_USERNAME, username);
        ActivityCompat.startActivity(activity, intent, getAnimBundle(activity, skipEnterAnim));
    }

    public static void showPasswordActivity(Activity activity, boolean skipEnterAnim, boolean forceSetSecQuestion) {
        Intent intent = new Intent(activity, SettingActivity.class);
        intent.putExtra(PasswordFragment.TAG_KEY, PasswordFragment.TAG_KEY);
        intent.putExtra(PasswordFragment.FORCE_SECQUEST_KEY, forceSetSecQuestion);
        ActivityCompat.startActivity(activity, intent, getAnimBundle(activity, skipEnterAnim));
    }

    public static void showWarrantyActivity(Activity activity, boolean skipEnterAnim) {
        Intent intent = new Intent(activity, CommonActivity.class);
        intent.putExtra(CommonActivity.FRAGMENT_KEY, WarrantyFragment.class.getName());
        ActivityCompat.startActivity(activity, intent, getAnimBundle(activity, skipEnterAnim));
    }

    public static void showSmsActivity(Activity activity, boolean skipEnterAnim, String uid, String author) {
        Intent intent = new Intent(activity, SmsActivity.class);
        intent.putExtra(SmsFragment.ARG_AUTHOR, author);
        intent.putExtra(SmsFragment.ARG_UID, uid);
        ActivityCompat.startActivity(activity, intent, getAnimBundle(activity, skipEnterAnim));
    }

    public static void showSimpleListActivity(Activity activity, boolean skipEnterAnim, int type) {
        showSimpleListActivity(activity, skipEnterAnim, type, null);
    }

    public static void showNotifyListActivity(Activity activity, boolean skipEnterAnim, int type, String extra) {
        Bundle bundle = null;
        if (!TextUtils.isEmpty(extra)) {
            bundle = new Bundle();
            bundle.putString(SimpleListFragment.ARG_EXTRA, extra);
        }
        showSimpleListActivity(activity, skipEnterAnim, type, bundle);
    }

    public static void showSimpleListActivity(Activity activity, boolean skipEnterAnim, int type, Bundle bundle) {
        Intent intent = new Intent(activity, SimpleListActivity.class);
        intent.putExtra(SimpleListFragment.ARG_TYPE, type);
        if (bundle != null)
            intent.putExtras(bundle);
        ActivityCompat.startActivity(activity, intent, getAnimBundle(activity, skipEnterAnim));
    }

    public static void showNewPostActivity(Activity activity, int fid, String special, String parentSessionId) {
        Intent intent = new Intent(activity, PostActivity.class);
        intent.putExtra(PostFragment.ARG_MODE_KEY, PostHelper.MODE_NEW_THREAD);
        intent.putExtra(PostFragment.ARG_FID_KEY, fid);
        intent.putExtra(PostFragment.ARG_SPECIAL_ID, special);
        intent.putExtra(PostFragment.ARG_PARENT_ID, parentSessionId);
        ActivityCompat.startActivity(activity, intent, getAnimBundle(activity, true));
    }

    public static void showPostActivity(Activity activity, int mode, String parentSessionId,
                                        int fid, String tid, String postId, int floor,
                                        String author, String text, String quoteText) {
        Intent intent = new Intent(activity, PostActivity.class);
        intent.putExtra(PostFragment.ARG_MODE_KEY, mode);
        intent.putExtra(PostFragment.ARG_FID_KEY, fid);
        intent.putExtra(PostFragment.ARG_PARENT_ID, parentSessionId);
        intent.putExtra(PostFragment.ARG_TID_KEY, tid);
        intent.putExtra(PostFragment.ARG_PID_KEY, postId);
        intent.putExtra(PostFragment.ARG_FLOOR_KEY, floor);
        if (text != null)
            intent.putExtra(PostFragment.ARG_TEXT_KEY, text);
        if (author != null)
            intent.putExtra(PostFragment.ARG_FLOOR_AUTHOR_KEY, author);
        if (quoteText != null)
            intent.putExtra(PostFragment.ARG_QUOTE_TEXT_KEY, quoteText);
        ActivityCompat.startActivity(activity, intent, getAnimBundle(activity, true));
    }

    public static void showFragment(FragmentManager fragmentManager, Fragment fragment, boolean skipEnterAnim) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (skipEnterAnim) {
            transaction.setCustomAnimations(0, 0, 0, R.anim.slide_out_right);
        } else {
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right);
        }

        transaction.add(R.id.main_frame_container, fragment, fragment.getClass().getName())
                .addToBackStack(fragment.getClass().getName())
                .commitAllowingStateLoss();
    }

    public static void show(FragmentActivity activity, FragmentArgs args) {
        if (args == null)
            return;
        if (args.getType() == FragmentArgs.TYPE_THREAD) {
            showThreadActivity(activity, args.isSkipEnterAnim(), args.getTid(), "", args.getPage(), args.getFloor(), args.getPostId(), -1);
        } else if (args.getType() == FragmentArgs.TYPE_USER_INFO) {
            showUserInfoActivity(activity, args.isSkipEnterAnim(), args.getUid(), args.getUsername());
        } else if (args.getType() == FragmentArgs.TYPE_SMS) {
            showSimpleListActivity(activity, args.isSkipEnterAnim(), SimpleListJob.TYPE_SMS);
        } else if (args.getType() == FragmentArgs.TYPE_SEARCH) {
            showSimpleListActivity(activity, args.isSkipEnterAnim(), SimpleListJob.TYPE_SEARCH);
        } else if (args.getType() == FragmentArgs.TYPE_FAVORITE) {
            showSimpleListActivity(activity, args.isSkipEnterAnim(), SimpleListJob.TYPE_FAVORITES);
        } else if (args.getType() == FragmentArgs.TYPE_SMS_DETAIL) {
            showSmsActivity(activity, args.isSkipEnterAnim(), args.getUid(), args.getUsername());
        } else if (args.getType() == FragmentArgs.TYPE_THREAD_NOTIFY) {
            showNotifyListActivity(activity, args.isSkipEnterAnim(), SimpleListJob.TYPE_THREAD_NOTIFY, args.getExtra());
        } else if (args.getType() == FragmentArgs.TYPE_FORUM) {
            showForum(activity.getSupportFragmentManager(), args.getFid());
        } else if (args.getType() == FragmentArgs.TYPE_NEW_THREAD) {
            showNewPostActivity(activity, args.getFid(), "1", args.getParentId());
        } else if (args.getType() == FragmentArgs.TYPE_NEW_POSTS) {
            showSimpleListActivity(activity, args.isSkipEnterAnim(), SimpleListJob.TYPE_NEW_POSTS);
        }
    }

}
