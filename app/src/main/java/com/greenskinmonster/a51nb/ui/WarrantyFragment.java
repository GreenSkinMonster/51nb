package com.greenskinmonster.a51nb.ui;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.greenskinmonster.a51nb.R;
import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.okhttp.OkHttpHelper;
import com.greenskinmonster.a51nb.okhttp.ParamsMap;
import com.greenskinmonster.a51nb.parser.HiParser;
import com.greenskinmonster.a51nb.ui.widget.HiProgressDialog;
import com.greenskinmonster.a51nb.ui.widget.OnSingleClickListener;
import com.greenskinmonster.a51nb.utils.HiUtils;
import com.greenskinmonster.a51nb.utils.Logger;
import com.greenskinmonster.a51nb.utils.UIUtils;
import com.greenskinmonster.a51nb.utils.Utils;

import java.util.Map;

import okhttp3.Request;

/**
 * Created by GreenSkinMonster on 2017-08-09.
 */

public class WarrantyFragment extends BaseFragment {

    private Map<String, String> mInfos;
    private HiProgressDialog mProgressDialog;
    private LinearLayout mResulLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarTitle(R.string.title_drawer_warranty);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_warranty, container, false);
        final RadioButton radio1 = (RadioButton) view.findViewById(R.id.radio_1);
        final RadioButton radio2 = (RadioButton) view.findViewById(R.id.radio_2);
        final TextView tvDesc = (TextView) view.findViewById(R.id.tv_desc);
        Button btnSearch = (Button) view.findViewById(R.id.btn_search);
        final EditText etSearch = (EditText) view.findViewById(R.id.et_search);
        mResulLayout = (LinearLayout) view.findViewById(R.id.result_layout);

        radio1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvDesc.setText("7位或10位格式如: 2007CA2或或20AA0000JJ");
            }
        });

        radio2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvDesc.setText("机型号加序列号如: 2842ELCLRZGPK9");
            }
        });

        btnSearch.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                UIUtils.hideSoftKeyboard(getActivity());
                etSearch.clearFocus();
                mResulLayout.removeAllViews();
                mProgressDialog = HiProgressDialog.show(getActivity(), "正在查询，请稍候...");
                ParamsMap params = new ParamsMap();
                params.put("searchtype", radio1.isChecked() ? "1" : "2");
                params.put("urltype1", radio1.isChecked() ? etSearch.getText().toString() : "");
                params.put("urltype2", radio2.isChecked() ? etSearch.getText().toString() : "");
                OkHttpHelper.getInstance().asyncPost(HiUtils.WarrantyUrl, params, new WarrantyCallback());
            }
        });

        return view;
    }

    private class WarrantyCallback implements OkHttpHelper.ResultCallback {
        @Override
        public void onError(Request request, Exception e) {
            if (mProgressDialog != null && !Utils.isDestroyed(getActivity()))
                mProgressDialog.dismiss();
            Logger.e(e);
            UIUtils.toast(OkHttpHelper.getErrorMessage(e).getMessage());
        }

        @Override
        public void onResponse(String response) {
            if (mProgressDialog != null && !Utils.isDestroyed(getActivity()))
                mProgressDialog.dismiss();
            try {
                mInfos = HiParser.parseWarrantyInfo(response);
                mResulLayout.removeAllViews();
                int i = mInfos.size() % 2;
                for (String key : mInfos.keySet()) {
                    TextView textView = new TextView(getActivity());
                    textView.setText(key + " : " + mInfos.get(key));
                    textView.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
                    textView.setBackgroundColor(ContextCompat.getColor(getActivity(), i % 2 == 0 ? R.color.background_silver : android.R.color.transparent));
                    textView.setPadding(8, 8, 8, 8);
                    textView.setTextIsSelectable(true);
                    mResulLayout.addView(textView);
                    i++;
                }
            } catch (Exception e) {
                Logger.e(e);
                UIUtils.toast(OkHttpHelper.getErrorMessage(e).getMessage());
            }
        }
    }

}
