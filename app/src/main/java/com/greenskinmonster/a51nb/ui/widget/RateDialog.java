package com.greenskinmonster.a51nb.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.greenskinmonster.a51nb.R;
import com.greenskinmonster.a51nb.async.ThreadActionHelper;
import com.greenskinmonster.a51nb.bean.PreRateBean;
import com.greenskinmonster.a51nb.okhttp.OkHttpHelper;
import com.greenskinmonster.a51nb.parser.ParserUtil;
import com.greenskinmonster.a51nb.ui.ThreadDetailFragment;
import com.greenskinmonster.a51nb.utils.Logger;

import okhttp3.Request;

/**
 * Created by GreenSkinMonster on 2017-08-13.
 */

public class RateDialog extends Dialog {

    private Context mCtx;

    private PreRateBean mPreRateBean;
    private HiProgressDialog mProgressDialog;
    private ThreadDetailFragment mFragment;

    private EditText etScore1Value;
    private EditText etScore2Value;
    private EditText etScore3Value;
    private EditText etRateReason;
    private Spinner spRateReason;

    public RateDialog(@NonNull Context context, PreRateBean preRateBean, ThreadDetailFragment fragment) {
        super(context);
        mCtx = context;
        mPreRateBean = preRateBean;
        mFragment = fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_rate, null);

        View score1Layout = view.findViewById(R.id.score1_layout);
        View score3Layout = view.findViewById(R.id.score3_layout);

        TextView tvScore1Info = (TextView) view.findViewById(R.id.tv_score1info);
        TextView tvScore2Info = (TextView) view.findViewById(R.id.tv_score2info);
        TextView tvScore3Info = (TextView) view.findViewById(R.id.tv_score3info);
        etScore1Value = (EditText) view.findViewById(R.id.et_score1_value);
        etScore2Value = (EditText) view.findViewById(R.id.et_score2_value);
        etScore3Value = (EditText) view.findViewById(R.id.et_score3_value);
        etRateReason = (EditText) view.findViewById(R.id.et_rate_reason);
        spRateReason = (Spinner) view.findViewById(R.id.sp_rate_reasons);

        tvScore1Info.setText(mPreRateBean.getScore1Limit() + " / "
                + mPreRateBean.getScore1Left());
        tvScore2Info.setText(mPreRateBean.getScore2Limit() + " / "
                + mPreRateBean.getScore2Left());
        tvScore3Info.setText(mPreRateBean.getScore3Limit() + " / "
                + mPreRateBean.getScore3Left());

        if (TextUtils.isEmpty(mPreRateBean.getScore1Left()) && TextUtils.isEmpty(mPreRateBean.getScore1Limit()))
            score1Layout.setVisibility(View.GONE);

        if (TextUtils.isEmpty(mPreRateBean.getScore3Left()) && TextUtils.isEmpty(mPreRateBean.getScore3Limit()))
            score3Layout.setVisibility(View.GONE);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter(mCtx,
                R.layout.spinner_row,
                mCtx.getResources().getStringArray(R.array.rate_reasons));
        spRateReason.setAdapter(spinnerAdapter);
        spRateReason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selCat = spRateReason.getItemAtPosition(position).toString();
                etRateReason.setText(selCat);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button btnLogin = (Button) view.findViewById(R.id.btn_rate);
        btnLogin.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                mProgressDialog = HiProgressDialog.show(mCtx, "请稍侯...");
                String score1 = etScore1Value.getText().toString();
                String score2 = etScore2Value.getText().toString();
                String score3 = etScore3Value.getText().toString();
                String reason = etRateReason.getText().toString();
                ThreadActionHelper.postRate(
                        mPreRateBean.getFormhash(),
                        mPreRateBean.getTid(),
                        mPreRateBean.getPid(),
                        score1,
                        score2,
                        score3,
                        reason,
                        new RateCallback());
            }
        });

        setContentView(view);
    }

    private class RateCallback implements OkHttpHelper.ResultCallback {

        @Override
        public void onError(Request request, Exception e) {
            Logger.e(e);
            mProgressDialog.dismissError(OkHttpHelper.getErrorMessage(e).getMessage());
        }

        @Override
        public void onResponse(String response) {
            if (response.contains("succeedhandle")) {
                mProgressDialog.dismiss("评分成功");
                dismiss();
                if (mFragment != null)
                    mFragment.refresh();
            } else {
                mProgressDialog.dismissError(ParserUtil.parseXmlErrorMessage(response));
            }
        }
    }

}
