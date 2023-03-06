package com.youra.radiofr.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.nemosofts.lk.utils.NemosoftsUtil;

import android.os.Bundle;

public abstract class YouraCompatActivity extends AppCompatActivity {

    public YouraCompatActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        this.setTheme(NemosoftsUtil.isViewTheme(this.setApplicationThemes()));
        super.onCreate(savedInstanceState);
        this.setContentView(this.setLayoutResourceId());
       // new ContentViewCreate(this);
    }

    protected abstract int setLayoutResourceId();

    protected abstract int setApplicationThemes();
}