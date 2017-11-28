package com.greenskinmonster.a51nb.ui.setting;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.greenskinmonster.a51nb.R;
import com.greenskinmonster.a51nb.async.LoginHelper;
import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.okhttp.OkHttpHelper;
import com.greenskinmonster.a51nb.okhttp.ParamsMap;
import com.greenskinmonster.a51nb.parser.HiParser;
import com.greenskinmonster.a51nb.ui.BaseFragment;
import com.greenskinmonster.a51nb.ui.SwipeBaseActivity;
import com.greenskinmonster.a51nb.ui.adapter.KeyValueArrayAdapter;
import com.greenskinmonster.a51nb.ui.widget.HiProgressDialog;
import com.greenskinmonster.a51nb.ui.widget.OnSingleClickListener;
import com.greenskinmonster.a51nb.utils.Constants;
import com.greenskinmonster.a51nb.utils.HiUtils;
import com.greenskinmonster.a51nb.utils.Logger;
import com.greenskinmonster.a51nb.utils.UIUtils;
import com.greenskinmonster.a51nb.utils.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.InputStream;

import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by GreenSkinMonster on 2017-08-16.
 */

public class PasswordFragment extends BaseFragment {

    public static final String TAG_KEY = "PASSWORD_KEY";
    public static final String FORCE_SECQUEST_KEY = "FORCE_SECQUEST_KEY";

    private HiProgressDialog mProgressDialog;

    private EditText mTvOldPassword;
    private EditText mTvNewPassword;
    private EditText mTvNewPassword2;
    private Spinner mSpSecQuestion;
    private EditText mTvSecAnswer;
    private EditText mTvCode;
    private ImageView mImageView;
    private Bitmap mCodeBitmap;

    private String mEmail;
    private String mFormhash;
    private boolean mForceSecQuestion;

    private MenuItem mSaveMenuItem;
    private KeyValueArrayAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            mForceSecQuestion = getArguments().getBoolean(FORCE_SECQUEST_KEY);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_password, menu);
        mSaveMenuItem = menu.findItem(R.id.action_save);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                savePassword();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_password, container, false);

        mTvOldPassword = (EditText) view.findViewById(R.id.et_old_password);
        mTvNewPassword = (EditText) view.findViewById(R.id.et_new_password);
        mTvNewPassword2 = (EditText) view.findViewById(R.id.et_new_password2);
        mSpSecQuestion = (Spinner) view.findViewById(R.id.sp_sec_question);
        mTvSecAnswer = (EditText) view.findViewById(R.id.et_sec_answer);
        mTvCode = (EditText) view.findViewById(R.id.et_code);
        mImageView = (ImageView) view.findViewById(R.id.iv_code);

        mImageView.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                refreshSecCode();
            }
        });

        String[] values = getActivity().getResources().getStringArray(R.array.pref_login_question_list_values);
        String[] entries = getActivity().getResources().getStringArray(R.array.pref_login_question_list_titles);
        values[0] = "";
        entries[0] = "保持原有的安全提问和答案";

        adapter = new KeyValueArrayAdapter(getActivity(), R.layout.spinner_row);
        adapter.setEntryValues(values);
        adapter.setEntries(entries);
        mSpSecQuestion.setAdapter(adapter);

        loadPage();

        ((SwipeBaseActivity) getActivity()).setSwipeBackEnable(false);

        setActionBarTitle("密码安全");
        return view;
    }


    private String mUpdate = "";
    private String mIdhash = "";
    private boolean mImageRefreshing = false;

    private void refreshSecCode() {

        if (TextUtils.isEmpty(mIdhash)) {
            UIUtils.toast("无法获得参数1");
            mSaveMenuItem.setEnabled(false);
            return;
        }
        if (mImageRefreshing)
            return;

        mImageRefreshing = true;

        new AsyncTask<Void, Void, Void>() {

            private String mMessage;

            @Override
            protected void onPreExecute() {
                mImageView.setImageBitmap(null);
                if (mCodeBitmap != null) {
                    mCodeBitmap.recycle();
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    String resp = OkHttpHelper.getInstance()
                            .get(HiUtils.SecCodeUpdateUrl.replace("{idhash}", mIdhash));
                    mUpdate = Utils.getMiddleString(resp, "update=", "&");
                    if (!TextUtils.isEmpty(mUpdate)) {
                        String url = HiUtils.SecCodeImageUrl
                                .replace("{idhash}", mIdhash).replace("{update}", mUpdate);

                        Response response = OkHttpHelper.getInstance().getResponse(url);
                        if (response.isSuccessful()) {
                            InputStream inputStream = response.body().byteStream();
                            mCodeBitmap = BitmapFactory.decodeStream(inputStream);
                            inputStream.close();
                        } else {
                            mMessage = "错误代码 : " + response.code();
                        }
                    } else {
                        mMessage = "无法获得参数2";
                    }
                } catch (Exception e) {
                    Logger.e(e);
                    mMessage = OkHttpHelper.getErrorMessage(e).getMessage();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mImageRefreshing = false;
                if (!TextUtils.isEmpty(mMessage)) {
                    mSaveMenuItem.setEnabled(false);
                    UIUtils.toast(mMessage);
                } else {
                    mSaveMenuItem.setEnabled(true);
                    mImageView.setImageBitmap(mCodeBitmap);
                }
            }
        }.execute();

    }

    private void loadPage() {
        mProgressDialog = HiProgressDialog.show(getActivity(), "请稍候...");
        OkHttpHelper.getInstance().asyncGet(HiUtils.PasswordUrl, new OkHttpHelper.ResultCallback() {
            @Override
            public void onError(Request request, Exception e) {
                mProgressDialog.dismiss(OkHttpHelper.getErrorMessage(e).getMessage());
            }

            @Override
            public void onResponse(String response) {
                mProgressDialog.dismiss();
                mIdhash = Utils.getMiddleString(response, "\"seccode_", "\"");
                Document doc = Jsoup.parse(response);
                Element emailEl = doc.select("input#emailnew").first();
                mFormhash = HiParser.parseFormhash(doc);
                if (emailEl == null || TextUtils.isEmpty(mFormhash) || TextUtils.isEmpty(mIdhash)) {
                    String error = HiParser.parseErrorMessage(doc);
                    if (TextUtils.isEmpty(error)) {
                        error = "无法获得参数";
                    }
                    UIUtils.toast(error);
                    return;
                }
                mEmail = emailEl.attr("value");
                refreshSecCode();
            }
        });
    }

    private void savePassword() {
        final String newpassword = mTvNewPassword.getText().toString();
        final String newpassword2 = mTvNewPassword2.getText().toString();
        final String questionidnew = adapter.getEntryValue(mSpSecQuestion.getSelectedItemPosition());
        final String answernew = mTvSecAnswer.getText().toString();
        final String code = mTvCode.getText().toString();

        if (mTvOldPassword.getText().toString().length() == 0) {
            UIUtils.toast("必须填写旧密码");
            mTvOldPassword.requestFocus();
            return;
        }

        if (newpassword.length() > 0 && newpassword.length() < 6) {
            UIUtils.toast("新密码至少为6位");
            mTvNewPassword.requestFocus();
            return;
        }
        if (!newpassword2.equals(newpassword)) {
            UIUtils.toast("确认新密码需要与新密码相同");
            mTvNewPassword2.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(newpassword) && TextUtils.isEmpty(questionidnew)) {
            UIUtils.toast("密码或者安全提问请至少选择更改一项");
            mTvNewPassword.requestFocus();
            return;
        }

        if (mForceSecQuestion && TextUtils.isEmpty(questionidnew)) {
            UIUtils.toast("论坛要求必需填写安全提问");
            return;
        }

        if (!TextUtils.isEmpty(questionidnew) && TextUtils.isEmpty(answernew)) {
            UIUtils.toast("请填写安全提问答案");
            mTvSecAnswer.requestFocus();
            return;
        }

        if (mTvCode.getText().toString().length() == 0) {
            UIUtils.toast("请填写验证码");
            mTvCode.requestFocus();
            return;
        }

        UIUtils.hideSoftKeyboard(getActivity());

        final ParamsMap params = new ParamsMap();
        params.put("formhash", mFormhash);
        params.put("oldpassword", mTvOldPassword.getText().toString());
        params.put("newpassword", newpassword);
        params.put("newpassword2", newpassword2);
        params.put("emailnew", mEmail);
        params.put("questionidnew", questionidnew);
        params.put("answernew", answernew);
        params.put("seccodehash", mIdhash);
        params.put("seccodemodid", "home::spacecp");
        params.put("seccodeverify", code);
        params.put("pwdsubmit", "true");
        params.put("passwordsubmit", "true");

        new AsyncTask<Void, Void, Void>() {

            private String mMessage;
            private int mStatus = Constants.STATUS_FAIL;

            @Override
            protected void onPreExecute() {
                mProgressDialog = HiProgressDialog.show(getActivity(), "请稍候...");
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (mStatus == Constants.STATUS_SUCCESS) {
                    mProgressDialog.dismiss(mMessage, 2000);
                } else {
                    mProgressDialog.dismissError(mMessage);
                }
            }

            @Override
            protected Void doInBackground(Void... args) {
                try {
                    String resp = OkHttpHelper.getInstance().post(HiUtils.PasswordSaveUrl, params);
                    Document doc = Jsoup.parse(resp);
                    mMessage = Utils.nullToText(HiParser.parseErrorMessage(doc));
                    if (mMessage.contains("成功")) {
                        if (!TextUtils.isEmpty(newpassword))
                            HiSettingsHelper.getInstance().setPassword(newpassword);
                        if (!TextUtils.isEmpty(questionidnew)) {
                            HiSettingsHelper.getInstance().setSecQuestion(questionidnew);
                            HiSettingsHelper.getInstance().setSecAnswer(answernew);
                        }
                        LoginHelper loginHelper = new LoginHelper();
                        mStatus = loginHelper.login();
                        if (mStatus == Constants.STATUS_SUCCESS) {
                            mMessage = "个人信息保存成功";
                        } else {
                            mMessage = loginHelper.getErrorMsg();
                        }
                    }
                } catch (Exception e) {
                    mMessage = OkHttpHelper.getErrorMessage(e).getMessage();
                    Logger.e(e);
                }
                return null;
            }
        }.execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (mCodeBitmap != null)
                mCodeBitmap.recycle();
        } catch (Exception ignored) {
            Logger.e(ignored);
        }
    }
}
