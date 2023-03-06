package com.youra.radiofr.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.youra.radiofr.BuildConfig;
import com.youra.radiofr.R;
import com.youra.radiofr.callback.Callback;
import com.youra.radiofr.ifSupported.IsRTL;
import com.youra.radiofr.ifSupported.IsScreenshot;
import com.youra.radiofr.utils.ApplicationUtil;


public class AboutActivity extends YouraCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IsRTL.ifSupported(this);
        IsScreenshot.ifSupported(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        TextView author = findViewById(R.id.tv_company);
        TextView email = findViewById(R.id.tv_email);
        TextView website = findViewById(R.id.tv_website);
        TextView contact = findViewById(R.id.tv_contact);
        TextView description = findViewById(R.id.tv_app_des);
        TextView version = findViewById(R.id.tv_version);

        author.setText(Callback.itemAbout.getAuthor());
        email.setText(Callback.itemAbout.getEmail());
        website.setText(Callback.itemAbout.getWebsite());
        contact.setText(Callback.itemAbout.getContact());
        description.setText(Callback.itemAbout.getAppDesc());
        version.setText(BuildConfig.VERSION_NAME);

        findViewById(R.id.ll_share).setOnClickListener(v -> {
            final String appName = getPackageName();
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.app_name));
            sendIntent.putExtra(Intent.EXTRA_TEXT, "http://play.google.com/store/apps/details?id=" + appName);
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Share"));
        });

        findViewById(R.id.ll_rate).setOnClickListener(v -> {
            final String appName = getPackageName();
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appName)));
        });
    }

    @Override
    protected int setLayoutResourceId() {
        return R.layout.activity_about;
    }

    @Override
    protected int setApplicationThemes() {
        return ApplicationUtil.isTheme();
    }
}