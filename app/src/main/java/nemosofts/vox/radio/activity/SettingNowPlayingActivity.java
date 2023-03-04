package nemosofts.vox.radio.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.nemosofts.lk.ProCompatActivity;
import androidx.nemosofts.lk.view.SwitchButton;
import androidx.viewpager.widget.ViewPager;

import nemosofts.vox.radio.R;
import nemosofts.vox.radio.adapter.NowPlayingScreenAdapter;
import nemosofts.vox.radio.callback.Callback;
import nemosofts.vox.radio.ifSupported.IsRTL;
import nemosofts.vox.radio.ifSupported.IsScreenshot;
import nemosofts.vox.radio.utils.ApplicationUtil;
import nemosofts.vox.radio.utils.NowPlayingScreen;
import nemosofts.vox.radio.utils.PreferenceUtil;
import nemosofts.vox.radio.utils.SharedPref;

public class SettingNowPlayingActivity extends YouraCompatActivity {

    private SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IsRTL.ifSupported(this);
        IsScreenshot.ifSupported(this);

        sharedPref = new SharedPref(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getResources().getString(R.string.settings));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        SwitchButton switch_snow_fall = findViewById(R.id.switch_snow_fall);
        switch_snow_fall.setChecked(sharedPref.isSnowFall());
        switch_snow_fall.setOnCheckedChangeListener((view, isChecked) -> sharedPref.setSnowFall(isChecked));

        SwitchButton switch_volume = findViewById(R.id.switch_volume);
        switch_volume.setChecked(sharedPref.isVolume());
        switch_volume.setOnCheckedChangeListener((view, isChecked) -> sharedPref.setVolume(isChecked));

        TextView tv_blur = findViewById(R.id.tv_blur_now);
        SeekBar sb_blur = findViewById(R.id.sb_blur_now);
        sb_blur.setMax(80);
        sb_blur.setProgress(sharedPref.getBlurAmount());
        tv_blur.setText(String.valueOf(sharedPref.getBlurAmount()));
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
                sharedPref.setBlurAmount(progress);
                tv_blur.setText(String.valueOf(progress));
            }
        });

        findViewById(R.id.ll_now_playing).setOnClickListener(view -> openNowPlaying());
    }


    private void openNowPlaying() {
        final Dialog dialog_player;
        final int[] viewPagerPosition = new int[1];
        dialog_player = new Dialog(SettingNowPlayingActivity.this);
        dialog_player.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_player.setContentView(R.layout.dialog_now_playing_screen);

        final ViewPager viewPager = dialog_player.findViewById(R.id.now_playing_screen_view_pager);
        viewPager.setAdapter(new NowPlayingScreenAdapter(this));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // this method is empty
            }

            @Override
            public void onPageSelected(int position) {
                viewPagerPosition[0] = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // this method is empty
            }
        });
        viewPager.setPageMargin((int) ApplicationUtil.convertDpToPixel(32, getResources()));
        viewPager.setCurrentItem(PreferenceUtil.getInstance(this).getNowPlayingScreen().ordinal());

        dialog_player.findViewById(R.id.tv_submit_btn).setOnClickListener(view -> {
            NowPlayingScreen nowPlayingScreen = NowPlayingScreen.values()[viewPagerPosition[0]];
            PreferenceUtil.getInstance(SettingNowPlayingActivity.this).setNowPlayingScreen(nowPlayingScreen);
            dialog_player.dismiss();
        });

        dialog_player.findViewById(R.id.tv_cancel_btn).setOnClickListener(view -> dialog_player.dismiss());
        dialog_player.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog_player.show();
        Window window = dialog_player.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected int setLayoutResourceId() {
        return R.layout.activity_setting_now_playing;
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