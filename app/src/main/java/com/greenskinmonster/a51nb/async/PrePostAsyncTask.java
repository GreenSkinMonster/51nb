package com.greenskinmonster.a51nb.async;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.bean.PostBean;
import com.greenskinmonster.a51nb.bean.PrePostInfoBean;
import com.greenskinmonster.a51nb.okhttp.NetworkError;
import com.greenskinmonster.a51nb.okhttp.OkHttpHelper;
import com.greenskinmonster.a51nb.parser.HiParser;
import com.greenskinmonster.a51nb.utils.HiUtils;
import com.greenskinmonster.a51nb.utils.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.Map;

public class PrePostAsyncTask extends AsyncTask<PostBean, Void, PrePostInfoBean> {

    private PrePostListener mListener;
    private int mFid;
    private int mMode;
    private String mMessage;
    private String mSpecial;

    public PrePostAsyncTask(PrePostListener listener, int mode, String special) {
        mListener = listener;
        mMode = mode;
        mSpecial = special;
    }

    @Override
    public PrePostInfoBean doInBackground(PostBean... postBeans) {
        PostBean postBean = postBeans[0];
        String tid = postBean.getTid();
        String pid = postBean.getPid();
        mFid = postBean.getFid();
        String url = "";
        switch (mMode) {
            case PostHelper.MODE_REPLY_THREAD:
                url = HiUtils.PreReplyUrl
                        .replace("{fid}", String.valueOf(mFid))
                        .replace("{tid}", tid);
                break;
            case PostHelper.MODE_REPLY_POST:
                url = HiUtils.PreReplyPostUrl
                        .replace("{fid}", String.valueOf(mFid))
                        .replace("{tid}", tid)
                        .replace("{pid}", pid)
                        .replace("{page}", String.valueOf(postBean.getPage()));
                break;
            case PostHelper.MODE_NEW_THREAD:
                url = HiUtils.PreNewThreadUrl
                        .replace("{fid}", String.valueOf(mFid))
                        .replace("{special}", Utils.nullToText(mSpecial));
                break;
            case PostHelper.MODE_EDIT_POST:
                //fid is not really needed, just put a value here
                url = HiUtils.PreEditUrl
                        .replace("{fid}", String.valueOf(mFid))
                        .replace("{tid}", tid)
                        .replace("{pid}", pid)
                        .replace("{page}", String.valueOf(postBean.getPage()));
                break;
        }

        for (int i = 0; i < OkHttpHelper.MAX_RETRY_TIMES; i++) {
            try {
                String resp = OkHttpHelper.getInstance().get(url);
                if (resp != null) {
                    return parseRsp(resp);
                }
            } catch (Exception e) {
                NetworkError message = OkHttpHelper.getErrorMessage(e);
                mMessage = message.getMessage();
            }
        }
        return null;
    }

    private PrePostInfoBean parseRsp(String resp) {
        Document doc = Jsoup.parse(resp);
        PrePostInfoBean prePostInfo = new PrePostInfoBean();

        String formhash = HiParser.parseFormhash(doc);
        if (TextUtils.isEmpty(formhash)) {
            mMessage = "页面解析错误";
            return prePostInfo;
        } else {
            prePostInfo.setFormhash(formhash);
        }

        Element messageEl = doc.select("textarea#e_textarea").first();
        if (messageEl != null) {
            prePostInfo.setText(messageEl.text());
        }

        Element hashEl = doc.select("input[name=hash]").first();
        if (hashEl != null) {
            prePostInfo.setHash(hashEl.attr("value"));
        }

        //for edit post
        Element subjectEl = doc.select("input#subject").first();
        if (subjectEl != null)
            prePostInfo.setSubject(subjectEl.attr("value"));

        Elements uploadInfoES = doc.select("div.uploadinfo");
        if (uploadInfoES.size() > 0) {
            String uploadInfo = uploadInfoES.first().text();
            if (uploadInfo.contains("文件尺寸")) {
                String sizeText = Utils.getMiddleString(uploadInfo.toUpperCase(), "小于", "B").trim();
                //sizeText : 100KB 8MB
                try {
                    float size = Float.parseFloat(sizeText.substring(0, sizeText.length() - 1));
                    String unit = sizeText.substring(sizeText.length() - 1, sizeText.length());
                    if (size > 0) {
                        int maxFileSize = 0;
                        if ("K".equals(unit)) {
                            maxFileSize = (int) (size * 1024);
                        } else if ("M".equals(unit)) {
                            maxFileSize = (int) (size * 1024 * 1024);
                        }
                        if (maxFileSize > 1024 * 1024)
                            HiSettingsHelper.getInstance().setMaxUploadFileSize(maxFileSize);
                    }
                } catch (Exception ignored) {
                }
            }
        }

        Element divQuote = doc.select("div.msgbody div.msgborder").first();
        if (divQuote != null) {
            divQuote.select("font[size=2]").remove();
            prePostInfo.setQuoteText(divQuote.text());
        }

        Element authorEl = doc.select("input[name=noticeauthor]").first();
        if (authorEl != null)
            prePostInfo.setNoticeAuthor(authorEl.attr("value"));
        Element authorMsgEl = doc.select("input[name=noticeauthormsg]").first();
        if (authorMsgEl != null)
            prePostInfo.setNoticeAuthorMsg(authorMsgEl.attr("value"));
        Element noticeTrimEl = doc.select("input[name=noticetrimstr]").first();
        if (noticeTrimEl != null)
            prePostInfo.setNoticeTrimStr(noticeTrimEl.attr("value").replace("[/url][/size]", "[/url][/size]\n"));

        //image as attachments
        Elements attachmentImages = doc.select("div#e_attachlist span a");
        for (int i = 0; i < attachmentImages.size(); i++) {
            Element aTag = attachmentImages.get(i);
            String href = Utils.nullToText(aTag.attr("href"));
            String onclick = Utils.nullToText(aTag.attr("onclick"));
            if (href.startsWith("javascript")) {
                if (onclick.startsWith("insertAttachimgTag")) {
                    //<a href="javascript:;" class="lighttxt" onclick="insertAttachimgTag('2810014')" title="...">Hi_160723_2240.jpg</a>
                    String imgId = Utils.getMiddleString(onclick, "insertAttachimgTag('", "'");
                    if (!TextUtils.isEmpty(imgId) && TextUtils.isDigitsOnly(imgId)) {
                        prePostInfo.addImage(imgId);
                    }
                } else if (onclick.startsWith("insertAttachTag")) {
                    String attachId = Utils.getMiddleString(onclick, "insertAttachTag('", "'");
                    if (!TextUtils.isEmpty(attachId) && TextUtils.isDigitsOnly(attachId)) {
                        prePostInfo.addAttach(attachId);
                    }
                }
            }
        }

        Element divTopicEl = doc.select("#postbox div.ftid").first();
        if (divTopicEl != null) {
            Elements typeidES = divTopicEl.select("#typeid > option");
            Map<String, String> values = new LinkedHashMap<>();
            for (int i = 0; i < typeidES.size(); i++) {
                Element typeidEl = typeidES.get(i);
                values.put(typeidEl.val(), typeidEl.text());
                if (i == 0 || "selected".equals(typeidEl.attr("selected")))
                    prePostInfo.setTypeId(typeidEl.val());
            }
            prePostInfo.setTypeValues(values);
            prePostInfo.setTypeRequired(resp.contains("var typerequired = parseInt('1')"));

            Elements topicES = divTopicEl.select("select[name=select] > option");
            Map<String, String> topicValuess = new LinkedHashMap<>();
            for (int i = 0; i < topicES.size(); i++) {
                Element topicEl = topicES.get(i);
                topicValuess.put(topicEl.val(), topicEl.text());
                if (i == 0 || "selected".equals(topicEl.attr("selected")))
                    prePostInfo.setTopic(topicEl.val());
            }
            prePostInfo.setTopicValues(topicValuess);
        }

        if (mMode == PostHelper.MODE_NEW_THREAD || mMode == PostHelper.MODE_EDIT_POST) {
            Map<String, String> perms = new LinkedHashMap<>();
            Elements readPerms = doc.select("select#readperm > option");
            for (Element readPermEl : readPerms) {
                perms.put(readPermEl.val(), readPermEl.text());
                if (prePostInfo.getReadPerm() == null || "selected".equals(readPermEl.attr("selected"))) {
                    prePostInfo.setReadPerm(readPermEl.val());
                }
            }
            prePostInfo.setReadPerms(perms);

            Element extCreditEl = doc.select("input#replycredit_extcredits").first();
            if (extCreditEl != null) {
                int extCredit = Utils.parseInt(extCreditEl.val());
                int creditTimes = Utils.parseInt(doc.select("input#replycredit_times").first().val());
                int creditMemberTimes = Utils.parseInt(doc.select("select#replycredit_membertimes").first().val());
                int creditLeft = Utils.getMiddleInt(doc.select("div#extra_replycredit_c > div.xg1").first().text(), "您有 资产值", "nb");

                int creditRandom = 100;
                Elements creditRandomES = doc.select("select#replycredit_random > option");
                for (Element optionEl : creditRandomES) {
                    if ("selected".equals(optionEl.attr("selected"))) {
                        creditRandom = Utils.parseInt(optionEl.val());
                    }
                }

                prePostInfo.setExtCredit(extCredit);
                prePostInfo.setCreditTimes(creditTimes);
                prePostInfo.setCreditMemberTimes(creditMemberTimes < 1 ? 1 : creditMemberTimes);
                prePostInfo.setCreditRandom(creditRandom);
                prePostInfo.setCreditLeft(creditLeft);

            } else {
                prePostInfo.setExtCredit(-1);
            }

            if (doc.select("#item_name").first() != null) {
                prePostInfo.setSpecial(PostHelper.SPECIAL_TRADE);
                prePostInfo.setItemName(getElementValue(doc.select("#item_name").first()));
                prePostInfo.setItemLocus(getElementValue(doc.select("#item_locus").first()));
                prePostInfo.setItemNumber(getElementValue(doc.select("#item_number").first()));
                prePostInfo.setItemQuality(getElementValue(doc.select("#item_quality").first()));
                prePostInfo.setItemExpiration(getElementValue(doc.select("#item_expiration").first()));
                prePostInfo.setTransport(getElementValue(doc.select("#transport").first()));
                prePostInfo.setItemPrice(getElementValue(doc.select("#item_price").first()));
                prePostInfo.setItemCostPrice(getElementValue(doc.select("#item_costprice").first()));
                prePostInfo.setItemCredit(getElementValue(doc.select("#item_credit").first()));
                prePostInfo.setItemCostCredit(getElementValue(doc.select("#item_costcredit").first()));
                prePostInfo.setPaymethod(getElementValue(doc.select("#paymethod").first()));
            }

            if (doc.select("#maxchoices").first() != null) {
                prePostInfo.setSpecial(PostHelper.SPECIAL_POLL);
                prePostInfo.setPollMaxChoices(getElementValue(doc.select("#maxchoices").first()));
                prePostInfo.setPollDays(getElementValue(doc.select("#polldatas").first()));
                prePostInfo.setPollVisibility(!TextUtils.isEmpty(getElementValue(doc.select("#visibilitypoll").first())));
                prePostInfo.setPollOvert(!TextUtils.isEmpty(getElementValue(doc.select("#overt").first())));
            }
        }
        return prePostInfo;
    }

    @Override
    protected void onPostExecute(PrePostInfoBean info) {
        if (info != null && !TextUtils.isEmpty(info.getFormhash()))
            mListener.PrePostComplete(mMode, true, null, info);
        else
            mListener.PrePostComplete(mMode, false, mMessage, null);
    }

    public interface PrePostListener {
        void PrePostComplete(int mode, boolean result, String message, PrePostInfoBean info);
    }

    public String getMessage() {
        return mMessage;
    }

    private String getElementValue(Element element) {
        if (element == null)
            return "";
        if ("input".equalsIgnoreCase(element.tagName()) && element.attr("type").equalsIgnoreCase("checkbox")) {
            return element.attr("checked").equalsIgnoreCase("checked") ? element.val() : "";
        } else if ("input".equalsIgnoreCase(element.tagName())) {
            return element.val();
        } else if ("select".equalsIgnoreCase(element.tagName())) {
            String value = null;
            Elements options = element.select("option");
            for (Element optionEl : options) {
                if (value == null || "selected".equalsIgnoreCase(optionEl.attr("selected"))) {
                    value = optionEl.val();
                }
            }
            return Utils.nullToText(value);
        }
        return "";
    }

}
