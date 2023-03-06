package com.youra.radiofr.activity;

import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.nemosofts.lk.view.SwitchButton;

import com.youra.radiofr.R;
import com.youra.radiofr.ifSupported.IsRTL;
import com.youra.radiofr.ifSupported.IsScreenshot;
import com.youra.radiofr.utils.ApplicationUtil;
import com.youra.radiofr.utils.SharedPref;

public class SettingDriveModeActivity extends YouraCompatActivity {

    private SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        super.onCreate(savedInstanceState);

        IsRTL.ifSupported(this);
        IsScreenshot.ifSupported(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getResources().getString(R.string.settings));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        SwitchButton switch_drive_color = findViewById(R.id.switch_drive_color);
        switch_drive_color.setChecked(sharedPref.isDriveColor());
        switch_drive_color.setOnCheckedChangeListener((view, isChecked) -> sharedPref.setDriveColor(isChecked));

        TextView tv_blur = findViewById(R.id.tv_blur);
        SeekBar sb_blur = findViewById(R.id.sb_blur);
        sb_blur.setMax(15);
        sb_blur.setProgress(sharedPref.getBlurAmountDrive());
        tv_blur.setText(String.valueOf(sharedPref.getBlurAmountDrive()));
        sb_blur.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_blur.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // this method is empty
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                sharedPref.setBlurAmountDrive(progress);
                tv_blur.setText(String.valueOf(progress));
            }
        });
    }

    @Override
    protected int setLayoutResourceId() {
        return R.layout.activity_setting_drive_mode;
    }

    @Override
    protected int setApplicationThemes() {
        return ApplicationUtil.isTheme();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}