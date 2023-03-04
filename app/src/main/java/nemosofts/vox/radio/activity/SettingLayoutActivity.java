package nemosofts.vox.radio.activity;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.nemosofts.lk.ProCompatActivity;
import androidx.nemosofts.lk.view.SwitchButton;

import nemosofts.vox.radio.R;
import nemosofts.vox.radio.ifSupported.IsRTL;
import nemosofts.vox.radio.ifSupported.IsScreenshot;
import nemosofts.vox.radio.utils.ApplicationUtil;
import nemosofts.vox.radio.utils.SharedPref;

public class SettingLayoutActivity extends YouraCompatActivity {

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

        SwitchButton switch_scale = findViewById(R.id.switch_scale);
        switch_scale.setChecked(sharedPref.isScaleBanner());
        switch_scale.setOnCheckedChangeListener((view, isChecked) -> sharedPref.setScaleBanner(isChecked));
    }

    @Override
    protected int setLayoutResourceId() {
        return R.layout.activity_setting_layout;
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