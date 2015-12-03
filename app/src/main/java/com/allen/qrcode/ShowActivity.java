package com.allen.qrcode;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.allen.myqrcode.R;
import com.qq.e.ads.banner.ADSize;
import com.qq.e.ads.banner.AbstractBannerADListener;
import com.qq.e.ads.banner.BannerView;
import com.qq.e.ads.interstitial.AbstractInterstitialADListener;
import com.qq.e.ads.interstitial.InterstitialAD;


public class ShowActivity extends BaseActivity {
    private TextView txt1, TV_show_type, iamgeTV;
    private String message;
    private Button btn_URL, btnMore, btn_TEXT;
    private Bundle bundle;
    private BannerView bv;
    private InterstitialAD iad;
    private LinearLayout miniLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        initView();
        initBanner();
        initIntent();

    }

    private void initView() {
        txt1 = (TextView) findViewById(R.id.txt1);
        TV_show_type = (TextView) findViewById(R.id.show_type);
        btn_URL = (Button) findViewById(R.id.url_button);
        btn_TEXT = (Button) findViewById(R.id.text_button);
        btnMore = (Button) findViewById(R.id.btn_set);
        iamgeTV = (TextView) findViewById(R.id.image_title);

        miniLayout = (LinearLayout) findViewById(R.id.miniAdLinearLayout);

        btn_URL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                playVibrate();
                Intent urlIntent = new Intent("android.intent.action.VIEW", Uri
                        .parse(message));
                startActivity(urlIntent);

            }
        });
        btn_TEXT.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                playVibrate();
                copy(message, ShowActivity.this);
            }
        });
        btnMore.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                playVibrate();
                Toast.makeText(ShowActivity.this, "本软件由腾讯广点通平台赞助支持！", Toast.LENGTH_LONG).show();
                showInterstitialAd();

                Intent intent = new Intent();
                intent.setClass(ShowActivity.this, SetingActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initIntent() {
        bundle = getIntent().getExtras();
        message = bundle.getString("msg");
        if (isURL(message)) {
            this.bv.loadAD();
            TV_show_type.setText("网址:");
            iamgeTV.setBackgroundResource(R.drawable.url72);
            btn_URL.setVisibility(View.VISIBLE);
        } else {
            btn_TEXT.setVisibility(View.VISIBLE);
        }
        if (bundle != null) {
            txt1.setText(message);

        }
    }

    private boolean isURL(String msg) {

        if (msg.startsWith("HTTP://") || msg.startsWith("HTTPS://")
                || msg.startsWith("http://") || msg.startsWith("https://")) {
            return true;
        } else {
            return false;
        }

    }

    private static final long VIBRATE_DURATION = 50;

    private void playVibrate() {

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(VIBRATE_DURATION);

    }

    public static void copy(String str, Context context) {
        // 得到剪贴板管理器
        ClipboardManager cmb = (ClipboardManager) context
                .getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(str);
        Toast.makeText(context, "复制成功", Toast.LENGTH_SHORT).show();
    }


    private InterstitialAD getIAD() {
        if (iad == null) {
            iad = new InterstitialAD(this, Constants.APPId,
                    Constants.InterteristalPosId);
        }
        return iad;
    }

    private void showInterstitialAd() {
        getIAD().setADListener(new AbstractInterstitialADListener() {

            @Override
            public void onNoAD(int arg0) {
                Log.i("AD_DEMO", "LoadInterstitialAd Fail:" + arg0);
            }

            @Override
            public void onADReceive() {
                iad.show();
            }
        });
        iad.loadAD();
    }

    private void initBanner() {
        this.bv = new BannerView(this, ADSize.BANNER, Constants.APPId,
                Constants.BannerPosId);
        bv.setRefresh(30);
        bv.setADListener(new AbstractBannerADListener() {

            @Override
            public void onNoAD(int arg0) {
                Log.i("AD_DEMO", "BannerNoAD，eCode=" + arg0);
            }

            @Override
            public void onADReceiv() {
                Log.i("AD_DEMO", "ONBannerReceive");
            }
        });
        miniLayout.addView(bv);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }
}
