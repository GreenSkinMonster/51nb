package com.greenskinmonster.a51nb.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.greenskinmonster.a51nb.BuildConfig;
import com.greenskinmonster.a51nb.R;
import com.greenskinmonster.a51nb.bean.Forum;
import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.glide.GlideHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HiUtils {
    public static final String ForumServer = "https://forum.51nb.com";
    public static final String ForumServerHttp = "http://forum.51nb.com";
    public static final String CookieDomain = "forum.51nb.com";

    public static final String SmiliesPattern = CookieDomain + "/static/image/smiley";
    public static final String SmiliesPattern2 = "app/data/phiz/default";
    public static final String ForumCommonSmiliesPattern = CookieDomain + "static/image/common";
    public static final String ForumImagePattern = "data/attachment";

    public static final int CLIENT_TID = 1579403;
    public static final String SYSTEM_UID = "system";
    public static final String AuthCookie = "Uf3r_2132_auth";

    private final static String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.40 Safari/537.36 51NB/" + BuildConfig.VERSION_NAME;

    public final static String BaseUrl = "https://forum.51nb.com/";
    public static final String ForumUrlPattern = ForumServer + "/";
    public static final String ForumAttatchUrlPattern = CookieDomain + "/forum.php?mod=attachment";

    public final static String AvatarBaseUrl = BaseUrl + "uc_server/avatar.php?size=middle&uid=";

    public final static String ForumListUrl = BaseUrl + "forum.php";
    public final static String ThreadListUrl = BaseUrl + "forum.php?mod=forumdisplay&fid=";
    public final static String DetailListUrl = BaseUrl + "forum.php?mod=viewthread&tid=";
    public final static String PreReplyUrl = BaseUrl + "forum.php?mod=post&action=reply&fid={fid}&tid={tid}";
    public final static String PreReplyPostUrl = BaseUrl + "forum.php?mod=post&action=reply&fid={fid}&extra=&tid={tid}&repquote={pid}";
    public final static String ReplyUrl = BaseUrl + "forum.php?mod=post&action=reply&fid={fid}&tid={tid}&extra=&replysubmit=yes";
    public final static String PreEditUrl = BaseUrl + "forum.php?mod=post&action=edit&fid={fid}&tid={tid}&pid={pid}&page={page}";
    public final static String EditUrl = BaseUrl + "forum.php?mod=post&action=edit&extra=&editsubmit=yes";
    public final static String PreNewThreadUrl = BaseUrl + "forum.php?mod=post&action=newthread&fid={fid}&special={special}";
    public final static String NewThreadUrl = BaseUrl + "forum.php?mod=post&action=newthread&fid={fid}&extra=&topicsubmit=yes";
    //public final static String MyPostUrl = BaseUrl + "forum.php?mod=guide&view=my";
    public final static String MyPostUrl = BaseUrl + "home.php?mod=space&uid={uid}&do=thread&view=me&from=space&type={type}";
    public final static String LastPageUrl = BaseUrl + "forum.php?mod=redirect&tid={tid}&goto=lastpost#lastpost";
    public final static String RedirectToPostUrl = BaseUrl + "forum.php?mod=redirect&goto=findpost&ptid={tid}&pid={pid}";
    public final static String GotoPostUrl = BaseUrl + "gotopost.php?pid={pid}";
    public final static String SMSUrl = BaseUrl + "home.php?mod=space&do=pm";
    public final static String SMSSendUrl = BaseUrl + "home.php?mod=spacecp&ac=pm&op=send&pmid={pmid}&daterange=0&handlekey=pmsend&pmsubmit=yes&inajax=1";
    public final static String SMSSendToUidUrl = BaseUrl + "home.php?mod=spacecp&ac=pm&op=send&touid={uid}&handlekey=pmsend&inajax=1";
    public final static String SMSDetailUrl = BaseUrl + "home.php?mod=space&do=pm&subop=view&touid=";
    public final static String SMSPostByUsername = BaseUrl + "pm.php?action=send&pmsubmit=yes&infloat=yes&inajax=1";
    public final static String SystemNotifyUrl = BaseUrl + "home.php?mod=space&do=notice&view=system";
    public final static String ThreadNotifyUrl = BaseUrl + "home.php?mod=space&do=notice&view=mypost";
    public final static String ThreadNotifyByTypeUrl = BaseUrl + "home.php?mod=space&do=notice&view=mypost&type={type}";
    public final static String LoginSubmit = BaseUrl + "member.php?mod=logging&action=login&infloat=yes&frommessage";
    public final static String CheckSMS = BaseUrl + "home.php?mod=spacecp&ac=pm&op=checknewpm&rand=1501059369";
    public final static String NewSMS = BaseUrl + "home.php?mod=space&do=pm&filter=newpm";
    public final static String DeleteAllSMS = BaseUrl + "home.php?mod=spacecp&ac=pm&op=delete&folder=";
    public final static String DeleteSingleSMS = BaseUrl + "home.php?mod=spacecp&ac=pm&op=delete&deletepm_pmid[]={pmid}&touid={uid}&deletesubmit=1&handlekey=pmdeletehk_{pmid}&formhash={formhash}&inajax=1&ajaxtarget=";
    //    public final static String UploadImgUrl = BaseUrl + "misc.php?mod=swfupload&operation=upload&simple=1&type=image";
    public final static String UploadImgUrl = BaseUrl + "misc.php?mod=swfupload&action=swfupload&operation=upload";
    public final static String DeleteImgUrl = BaseUrl + "forum.php?mod=ajax&action=deleteattach&inajax=yes&tid={tid}&pid={pid}&aids[]={aid}";
    public final static String SearchUrl = BaseUrl + "search.php?mod=forum&adv=yes";
    public final static String SearchUserThreads = BaseUrl + "search.php?srchfid=all&srchfrom=0&searchsubmit=yes&srchuid={srchuid}";
    public final static String FavoritesUrl = BaseUrl + "home.php?mod=space&do=favorite&type=thread";
    public final static String FavoriteAddUrl = BaseUrl + "home.php?mod=spacecp&ac=favorite&type=thread&id={tid}&formhash={formhash}&infloat=yes&handlekey=k_favorite&inajax=1&ajaxtarget=fwin_content_k_favorite";
    public final static String FavoriteRemoveUrl = BaseUrl + "home.php?mod=spacecp&ac=favorite&op=delete&favid={favid}&type=all&inajax=1";
    public final static String UserInfoUrl = BaseUrl + "home.php?mod=space&uid=";
    public final static String AddBlackUrl = BaseUrl + "home.php?mod=spacecp&ac=friend&op=blacklist&start=";
    public final static String DelBlackUrl = BaseUrl + "home.php?mod=spacecp&ac=friend&op=blacklist&subop=delete&uid={uid}&start=";
    public final static String ViewBlackUrl = BaseUrl + "home.php?mod=space&do=friend&view=blacklist";
    public final static String NewPostsUrl = BaseUrl + "forum.php?mod=guide&view={type}";
    public final static String SearchByIdUrl = BaseUrl + "search.php?mod=forum&searchid={searchid}&orderby=lastpost&ascdesc=desc&searchsubmit=yes";
    private final static String SysNoticeUrl = BaseUrl + "home.php?mod=space&do=notice&view=system";
    public final static String QianDaoUrl = BaseUrl + "plugin.php?id=dsu_paulsign:sign&operation=qiandao&infloat=1&sign_as=1&inajax=1";
    public final static String WarrantyUrl = BaseUrl + "report.php?action=getdetails&submit=yes";
    public final static String ReplyCommentUrl = BaseUrl + "forum.php?mod=post&action=reply&comment=yes&tid={tid}&pid={pid}&extra=page%3D1&page={page}&commentsubmit=yes&infloat=yes&inajax=1&tocid=";
    public final static String GetCommentsUrl = BaseUrl + "forum.php?mod=misc&action=commentmore&tid={tid}&pid={pid}&page={page}&inajax=1&ajaxtarget=comment_123456";
    public final static String SupportUrl = BaseUrl + "forum.php?mod=misc&action=postreview&do=support&tid={tid}&pid={pid}&hash={formhash}&ajaxmenu=1&inajax=1&ajaxtarget=_menu_content";
    public final static String AgainstUrl = BaseUrl + "forum.php?mod=misc&action=postreview&do=against&tid={tid}&pid={pid}&hash={formhash}&ajaxmenu=1&inajax=1&ajaxtarget=_menu_content";
    public final static String PreRateUrl = BaseUrl + "forum.php?mod=misc&action=rate&tid={tid}&pid={pid}&infloat=yes&handlekey=rate&t={time}&inajax=1&ajaxtarget=fwin_content_rate";
    public final static String RateUrl = BaseUrl + "forum.php?mod=misc&action=rate&ratesubmit=yes&infloat=yes&inajax=1";
    public final static String PasswordUrl = BaseUrl + "home.php?mod=spacecp&ac=profile&op=password";
    public final static String PasswordSaveUrl = HiUtils.BaseUrl + "home.php?mod=spacecp&ac=profile";
    public final static String SecCodeUpdateUrl = HiUtils.BaseUrl + "misc.php?mod=seccode&action=update&idhash={idhash}&modid=undefined";
    public final static String SecCodeImageUrl = HiUtils.BaseUrl + "misc.php?mod=seccode&update={update}&idhash={idhash}";
    public final static String VotePollUrl = HiUtils.BaseUrl + "forum.php?mod=misc&action=votepoll&fid={fid}&tid={tid}&pollsubmit=yes&quickforward=yes&inajax=1";


    // max upload file size : 8M
    public final static int DEFAULT_MAX_UPLOAD_FILE_SIZE = 8 * 1024 * 1024;

    public final static int FID_THINPAD = 1;
    public final static int FID_TRADE = 41;
    public final static int FID_TEST = 138;
    public final static int FID_RECOMMEND = 113;

    public final static Map<String, Integer> StaticKeywordMap = new HashMap<>();

    static {
        StaticKeywordMap.put("thinkpad", FID_THINPAD);
        StaticKeywordMap.put("x62", 117);
    }

    public final static Forum[] FORUMS = {
            new Forum(136, 0, "ThinkPad技术论坛"),
            new Forum(1, 1, "ThinkPad专区"),
            new Forum(117, 1, "T50/X62/T70/X210定制专区"),
            new Forum(137, 1, "ThinkPad Retro/Classic专区"),
            new Forum(38, 0, "其它笔记本技术论坛"),
            new Forum(54, 1, "HP/Compaq(惠普)专区"),
            new Forum(106, 2, "HP版有奖征文专区"),
            new Forum(59, 1, "DELL(戴尔)专区"),
            new Forum(60, 1, "Mac(苹果)专区"),
            new Forum(69, 1, "ASUS(华硕)专区"),
            new Forum(43, 1, "其它品牌笔记本"),
            new Forum(71, 2, "FUJITSU(富士通)专区"),
            new Forum(86, 2, "Lenovo(联想)专区"),
            new Forum(66, 2, "Acer(宏碁)专区"),
            new Forum(67, 2, "Toshiba(东芝)专区"),
            new Forum(68, 2, "SONY(索尼)专区"),
            new Forum(110, 2, "Panasonic(松下)笔记本电脑专区"),
            new Forum(80, 2, "◇Essentials of 51nb.com in English◇"),
            new Forum(47, 1, "评机品机--笔记本产品库"),
            new Forum(58, 1, "选件与外设"),
            new Forum(104, 0, "智能手机与平板论坛"),
            new Forum(2, 1, "智能手机讨论区"),
            new Forum(112, 1, "手机拆解与维护"),
            new Forum(103, 1, "ThinkPad Slate Tablet平板电脑"),
            new Forum(114, 1, "Galaxy Tab平板专区"),
            new Forum(78, 1, "iPad, Surface, Kindle Fire等平板与电子书"),
            new Forum(40, 0, "交易与市场论坛"),
            new Forum(41, 1, "认证交易区"),
            new Forum(3, 1, "市场行情与购机交流"),
            new Forum(51, 1, "保修与维修"),
            new Forum(113, 1, "专门推荐"),
            new Forum(119, 1, "专门抢购"),
            new Forum(105, 1, "海淘交流区"),
            new Forum(61, 0, "笔记本相关论坛"),
            new Forum(9, 1, "Windows平台软件交流"),
            new Forum(17, 1, "Linux与BSD等平台"),
            new Forum(21, 1, "网络技术"),
            new Forum(62, 1, "数码、摄影"),
            new Forum(28, 1, "台式机与服务器专区"),
            new Forum(111, 1, "存储交流专区"),
            new Forum(44, 0, "论坛与站务公告"),
            new Forum(37, 1, "论坛与站务公告区"),
            new Forum(135, 2, "秀出不一样的ThinkPad专区"),
            new Forum(133, 2, "ThinkPad答题王专区"),
            new Forum(134, 2, "我为ThinkPad证言专区"),
            new Forum(121, 2, "“最美的笔记本”摄影大赛专区"),
            new Forum(84, 2, "ThinkPad十五周年设计大赛专区"),
            new Forum(82, 2, "2011年度“写体会、拿工具”征文专区"),
            new Forum(118, 2, "“ThinkPad，一路有你”活动专区"),
            new Forum(88, 1, "网友联谊区"),
            new Forum(96, 1, "区域事务论坛封存"),
            new Forum(75, 2, "北京区"),
            new Forum(7, 2, "华南区"),
            new Forum(16, 2, "西南区"),
            new Forum(18, 2, "西北区"),
            new Forum(15, 2, "东北区"),
            new Forum(13, 2, "华东区"),
            new Forum(22, 2, "山东区"),
            new Forum(14, 2, "华中区"),
            new Forum(6, 2, "华北区封存"),
            new Forum(35, 2, "江西区封存"),
            new Forum(42, 2, "海外区封存"),
            new Forum(34, 2, "浙江区"),
            new Forum(98, 0, "内部事项"),
            new Forum(70, 1, "时刻准备着"),
            new Forum(53, 1, "回收站"),
            new Forum(138, 1, "test")
    };

    public final static int[] DEFAULT_FORUMS = {FID_THINPAD, 117, 59, 43};

    public static String getForumNameByFid(int fid) {
        Forum forum = getForumByFid(fid);
        return forum != null ? forum.getName() : "";
    }

    public static boolean isForumValid(int fid) {
        return getForumByFid(fid) != null;
    }

    public static Forum getForumByFid(int fid) {
        if (HiSettingsHelper.getInstance() == null) {
            for (Forum forum : HiUtils.FORUMS) {
                if (forum.getId() == fid)
                    return forum;
            }
        } else {
            for (Forum forum : HiSettingsHelper.getInstance().getAllForums()) {
                if (forum.getId() == fid)
                    return forum;
            }
        }
        return null;
    }

    public static String getAvatarUrlByUid(String uid) {
        if (SYSTEM_UID.equals(uid))
            return GlideHelper.SYSTEM_AVATAR_FILE.getAbsolutePath();
        return AvatarBaseUrl + uid;
    }

    public static int getThemeValue(Context context, String theme, int primaryColor) {
        if (HiSettingsHelper.THEME_DARK.equals(theme)) {
            return R.style.ThemeDark;
        } else if (HiSettingsHelper.THEME_BLACK.equals(theme)) {
            return R.style.ThemeBlack;
        } else if (HiSettingsHelper.THEME_LIGHT.equals(theme)) {
            if (primaryColor == ContextCompat.getColor(context, R.color.md_red_700))
                return R.style.ThemeLight_Red;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_pink_700))
                return R.style.ThemeLight_Pink;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_purple_700))
                return R.style.ThemeLight_Purple;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_deep_purple_700))
                return R.style.ThemeLight_DeepPurple;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_indigo_700))
                return R.style.ThemeLight_Indigo;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_blue_700))
                return R.style.ThemeLight_Blue;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_light_blue_700))
                return R.style.ThemeLight_LightBlue;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_cyan_700))
                return R.style.ThemeLight_Cyan;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_teal_700))
                return R.style.ThemeLight_Teal;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_green_700))
                return R.style.ThemeLight_Green;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_light_green_700))
                return R.style.ThemeLight_LightGreen;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_lime_700))
                return R.style.ThemeLight_Lime;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_yellow_700))
                return R.style.ThemeLight_Yellow;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_amber_700))
                return R.style.ThemeLight_Amber;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_orange_700))
                return R.style.ThemeLight_Orange;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_deep_orange_700))
                return R.style.ThemeLight_DeepOrange;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_brown_700))
                return R.style.ThemeLight_Brown;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_grey_700))
                return R.style.ThemeLight_Grey;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_blue_grey_700))
                return R.style.ThemeLight_BlueGrey;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_grey_200))
                return R.style.ThemeLight_White;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_black_1000))
                return R.style.ThemeLight_Black;
        }
        HiSettingsHelper.getInstance().setTheme(HiSettingsHelper.THEME_LIGHT);
        HiSettingsHelper.getInstance().setPrimaryColor(ContextCompat.getColor(context, R.color.md_blue_grey_700));
        return R.style.ThemeLight_BlueGrey;
    }

    public static boolean isValidId(String id) {
        return !TextUtils.isEmpty(id) && Utils.parseInt(id) > 0;
    }

    public static String getUserAgent() {
        return userAgent;
    }

    public static String getForumsSummary() {
        List<Forum> forums = HiSettingsHelper.getInstance().getFreqForums();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < forums.size(); i++) {
            Forum forum = forums.get(i);
            sb.append(forum.getName());
            if (i != forums.size() - 1)
                sb.append(", ");
        }
        return sb.toString();
    }
}
