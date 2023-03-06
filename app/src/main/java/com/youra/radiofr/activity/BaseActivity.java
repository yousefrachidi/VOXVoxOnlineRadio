package com.youra.radiofr.activity;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.nemosofts.lk.view.BlurImage;
import androidx.nemosofts.lk.view.PlayPauseView;
import androidx.nemosofts.lk.view.ToggleView;
import androidx.palette.graphics.Palette;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.jetradarmobile.snowfall.SnowfallView;
import com.makeramen.roundedimageview.RoundedImageView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.youra.radiofr.BuildConfig;
import com.youra.radiofr.R;
import com.youra.radiofr.asyncTask.LoadStatus;
import com.youra.radiofr.callback.Callback;
import com.youra.radiofr.dialog.FeedBackDialog;
import com.youra.radiofr.dialog.PremiumDialog;
import com.youra.radiofr.dialog.ReviewDialog;
import com.youra.radiofr.dialog.TimerCancelDialog;
import com.youra.radiofr.dialog.TimerStartDialog;
import com.youra.radiofr.ifSupported.IsRTL;
import com.youra.radiofr.ifSupported.IsScreenshot;
import com.youra.radiofr.interfaces.SuccessListener;
import com.youra.radiofr.item.ItemCategory;
import com.youra.radiofr.item.ItemRadio;
import com.youra.radiofr.utils.ApplicationUtil;
import com.youra.radiofr.utils.AudioRecorder;
import com.youra.radiofr.utils.GlobalBus;
import com.youra.radiofr.utils.Helper;
import com.youra.radiofr.utils.LoadColor;
import com.youra.radiofr.utils.MessageEvent;
import com.youra.radiofr.utils.PreferenceUtil;
import com.youra.radiofr.utils.SharedPref;
import com.youra.radiofr.utils.TimeReceiver;

public class BaseActivity extends YouraCompatActivity implements View.OnClickListener {

    Helper helper;
    SharedPref sharedPref;
    Toolbar toolbar;
    DrawerLayout drawer;
    NavigationView navigationView;
    LinearLayout ll_bottom_nav;
    LinearLayout ll_adView_player;
    ToggleView tv_nav_home, tv_nav_latest, tv_nav_most, tv_nav_category, tv_nav_restore;

    AudioManager am;
    String deviceId;
    Boolean isExpand = false;
    public SlidingUpPanelLayout mLayout;

    // BOTTOM PLAYER
    ProgressBar pb_min;
    public RelativeLayout min_header;
    MaterialTextView tv_min_title;
    ImageView iv_min_previous, iv_min_play, iv_min_next;
    TextView textView_timer;
    ProgressBar progressBar_min;
    CircularProgressIndicator circular_min;

    // MAIN PLAYER
    public ViewPager viewpager;
    ImagePagerAdapter imagePagerAdapter;
    RatingBar ratingBar;
    TextView tv_radio_title, tv_radio_cat, tv_radio_count;
    PlayPauseView ppv_radio_play;
    RelativeLayout rl_radio_loading;
    ImageView iv_radio_previous, iv_radio_next, iv_radio_timer, iv_microphone, volumeDown, volumeUp;
    ImageView iv_equalizer, iv_rate, iv_fav, iv_share, iv_bug, iv_option;
    SeekBar volumeSeekBar;
    AudioManager audioManager;
    Boolean isExpandVolume = true;
    SnowfallView view_snow_fall;
    private final Handler seekHandler = new Handler();
    View music_player_options, media_seekbar;
    TextView tv_current_time, tv_total_time;
    SeekBar seekBar_music;
    int nowPlayingScreen;

    AppUpdateManager appUpdateManager;
    private static final int MY_REQUEST_CODE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        sharedPref.getThemeDetails();
        nowPlayingScreen = PreferenceUtil.getInstance(this).getNowPlayingScreen().ordinal();
        super.onCreate(savedInstanceState);

        Callback.context = this;
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        helper = new Helper(this);

        IsRTL.ifSupported(this);
        IsScreenshot.ifSupported(this);

        toolbar = findViewById(R.id.toolbar_offline_music);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setToolbarNavigationClickListener(view -> drawer.openDrawer(GravityCompat.START));
        if (ApplicationUtil.isDarkMode()) {
            toggle.setHomeAsUpIndicator(R.drawable.ic_menu_white);
        } else {
            toggle.setHomeAsUpIndicator(R.drawable.ic_menu_black);
        }
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        toggle.setDrawerIndicatorEnabled(false);

        ll_bottom_nav = findViewById(R.id.ll_bottom_nav);
        ll_adView_player = findViewById(R.id.ll_adView_player);
        tv_nav_home = findViewById(R.id.tv_nav_home);
        tv_nav_latest = findViewById(R.id.tv_nav_latest);
        tv_nav_most = findViewById(R.id.tv_nav_most);
        tv_nav_category = findViewById(R.id.tv_nav_category);
        tv_nav_restore = findViewById(R.id.tv_nav_restore);
        ll_adView_player.setVisibility(View.GONE);
        tv_nav_home.setBadgeText("");

        // BOTTOM PLAYER
        tv_min_title = findViewById(R.id.tv_min_title);
        pb_min = findViewById(R.id.pb_min);
        iv_min_previous = findViewById(R.id.iv_min_previous);
        iv_min_play = findViewById(R.id.iv_min_play);
        iv_min_next = findViewById(R.id.iv_min_next);
        textView_timer = findViewById(R.id.textView_timer);
        progressBar_min = findViewById(R.id.progressBar_min);
        progressBar_min.setPadding(0, 0, 0, 0);
        circular_min = findViewById(R.id.circular_min);
        circular_min.setPadding(0, 0, 0, 0);

        iv_min_play.setOnClickListener(this);
        iv_min_next.setOnClickListener(this);
        iv_min_previous.setOnClickListener(this);

        // MAIN PLAYER
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        ratingBar = findViewById(R.id.rb_music);
        tv_radio_title = findViewById(R.id.tv_radio_title);
        tv_radio_cat = findViewById(R.id.tv_radio_cat);
        tv_radio_count = findViewById(R.id.tv_radio_count);
        rl_radio_loading = findViewById(R.id.rl_radio_loading);
        volumeSeekBar = findViewById(R.id.volumeSeekBar);
        volumeDown = findViewById(R.id.volumeDown);
        volumeUp = findViewById(R.id.volumeUp);
        ppv_radio_play = findViewById(R.id.ppv_radio_play);
        iv_radio_previous = findViewById(R.id.iv_radio_previous);
        iv_radio_next = findViewById(R.id.iv_radio_next);
        iv_radio_timer = findViewById(R.id.iv_radio_timer);
        iv_microphone = findViewById(R.id.iv_radio_microphone);
        iv_equalizer = findViewById(R.id.iv_player_equalizer);
        iv_rate = findViewById(R.id.iv_player_rate);
        iv_fav = findViewById(R.id.iv_player_fav);
        iv_share = findViewById(R.id.iv_player_share);
        iv_bug = findViewById(R.id.iv_player_bug);
        iv_option = findViewById(R.id.iv_player_option);
        music_player_options = findViewById(R.id.music_player_options);
        media_seekbar = findViewById(R.id.media_seekbar);
        seekBar_music = findViewById(R.id.seekbar_music);
        tv_current_time = findViewById(R.id.tv_music_time);
        tv_total_time = findViewById(R.id.tv_music_total_time);

        tv_current_time.setText("00:00");
        tv_total_time.setText("00:00");

        ppv_radio_play.setOnClickListener(this);
        iv_radio_previous.setOnClickListener(this);
        iv_radio_next.setOnClickListener(this);
        iv_radio_timer.setOnClickListener(this);
        iv_microphone.setOnClickListener(this);
        iv_equalizer.setOnClickListener(this);
        iv_rate.setOnClickListener(this);
        iv_fav.setOnClickListener(this);
        iv_share.setOnClickListener(this);
        iv_bug.setOnClickListener(this);
        iv_option.setOnClickListener(this);

        mLayout = findViewById(R.id.sliding_layout);
        min_header = findViewById(R.id.rl_min_header);

        findViewById(R.id.rl_c).setOnClickListener(v -> {
        });

        findViewById(R.id.iv_open_player).setOnClickListener(v -> {
            if (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        });

        findViewById(R.id.iv_open_player_2).setOnClickListener(v -> {
            if (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        });

        imagePagerAdapter = new ImagePagerAdapter();
        viewpager = findViewById(R.id.viewPager_player);
        viewpager.setOffscreenPageLimit(5);
        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // this method is empty
            }

            @Override
            public void onPageSelected(int position) {
                changeTextPager(Callback.arrayList_play.get(position));
                View view = viewpager.findViewWithTag("myview" + position);
                if (view != null) {
                    ImageView iv = view.findViewById(R.id.iv_vp_play);
                    if (Callback.playPos == position) {
                        iv.setVisibility(View.GONE);
                    } else {
                        iv.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // this method is empty
            }
        });

        seekBar_music.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // this method is empty
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // this method is empty
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                try {
                    Intent intent = new Intent(BaseActivity.this, PlayerService.class);
                    intent.setAction(PlayerService.ACTION_SEEKTO);
                    intent.putExtra("seekto", helper.getSeekFromPercentage(progress, PlayerService.getInstance().getDuration()));
                    startService(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (slideOffset == 0.0f) {
                    isExpand = false;
                    min_header.setVisibility(View.VISIBLE);
                } else if (slideOffset > 0.0f && slideOffset < 1.0f) {
                    min_header.setVisibility(View.VISIBLE);
                    min_header.setAlpha(1.0f - slideOffset);
                } else {
                    isExpand = true;
                    min_header.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    try {
                        if (viewpager.getAdapter() == null || Callback.isNewAdded || !Callback.addedFrom.equals(imagePagerAdapter.getIsLoadedFrom())) {
                            viewpager.setAdapter(imagePagerAdapter);
                        }
                        viewpager.setCurrentItem(Callback.playPos);
                    } catch (Exception e) {
                        imagePagerAdapter.notifyDataSetChanged();
                        viewpager.setCurrentItem(Callback.playPos);
                    }
                    if (sharedPref.isVolume() && volumeSeekBar.getVisibility() == View.VISIBLE && volumeSeekBar.getProgress() != audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)) {
                        ValueAnimator anim = ValueAnimator.ofInt(volumeSeekBar.getProgress(), audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
                        anim.setDuration(400);
                        anim.addUpdateListener(animation -> {
                            int animProgress = (Integer) animation.getAnimatedValue();
                            volumeSeekBar.setProgress(animProgress);
                        });
                        anim.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animator) {
                                isExpandVolume = false;
                            }

                            @Override
                            public void onAnimationEnd(Animator animator) {
                                isExpandVolume = true;
                            }

                            @Override
                            public void onAnimationCancel(Animator animator) {
                                isExpandVolume = true;
                            }

                            @Override
                            public void onAnimationRepeat(Animator animator) {
                                // this method is empty
                            }
                        });
                        anim.start();
                    }
                }
            }
        });
        volumeSeekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volumeSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (isExpandVolume) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // this method is empty
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // this method is empty
            }
        });
        if (sharedPref.isVolume()) {
            volumeSeekBar.setVisibility(View.VISIBLE);
            volumeDown.setVisibility(View.VISIBLE);
            volumeUp.setVisibility(View.VISIBLE);
        } else {
            volumeSeekBar.setVisibility(View.GONE);
            volumeDown.setVisibility(View.GONE);
            volumeUp.setVisibility(View.GONE);
        }

        view_snow_fall = findViewById(R.id.view_snow_fall);
        if (ApplicationUtil.isDarkMode()) {
            if (sharedPref.isSnowFall()) {
                view_snow_fall.restartFalling();
                view_snow_fall.setVisibility(View.VISIBLE);
            } else {
                view_snow_fall.stopFalling();
                view_snow_fall.setVisibility(View.GONE);
            }
        } else {
            view_snow_fall.stopFalling();
            view_snow_fall.setVisibility(View.GONE);
        }

        getTimeData();

        if (nowPlayingScreen == 3 || nowPlayingScreen == 4) {
            loadIconColor();
        }


        //// update
        inUpdate();
    }

    private void inUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this);

        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {

                try {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, MY_REQUEST_CODE);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                    Log.d("TAG", "inUpdate: " + e.getMessage());
                }
            }
        });
        appUpdateManager.registerListener(listener);
    }

    InstallStateUpdatedListener listener = state -> {
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            popupSnackbarForCompleteUpdate("An update has just been downloaded.", true);
        } else if (state.installStatus() == InstallStatus.FAILED) {
            popupSnackbarForCompleteUpdate("An update hasn't  been downloaded", false);
        } else if (state.installStatus() == InstallStatus.PENDING) {
            popupSnackbarForCompleteUpdate("An update has been PENDING", false);
        } else if (state.installStatus() == InstallStatus.UNKNOWN) {
            popupSnackbarForCompleteUpdate("An update has been UNKNOWN", false);
        }
    };

    private void popupSnackbarForCompleteUpdate(String content, boolean status) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                content,
                Snackbar.LENGTH_INDEFINITE);
        if (status) {
            snackbar.setAction("RESTART", view -> appUpdateManager.completeUpdate());
            snackbar.setActionTextColor(getResources().getColor(R.color.titleBlue));
        }
        snackbar.show();
    }

    @Override
    public void onStop() {
        super.onStop();
        appUpdateManager.unregisterListener(listener);
    }

    private void loadIconColor() {
        tv_radio_title.setTextColor(ApplicationUtil.colorUtilsWhite(this));
        tv_radio_cat.setTextColor(ApplicationUtil.colorUtilsWhite(this));
        tv_radio_count.setTextColor(ApplicationUtil.colorUtilsWhite(this));
        tv_current_time.setTextColor(ApplicationUtil.colorUtilsWhite(this));
        tv_total_time.setTextColor(ApplicationUtil.colorUtilsWhite(this));
        seekBar_music.setBackgroundTintList(ColorStateList.valueOf(ApplicationUtil.colorUtilsWhite(this)));

        volumeDown.setColorFilter(ApplicationUtil.colorUtilsWhite(this));
        volumeUp.setColorFilter(ApplicationUtil.colorUtilsWhite(this));

        iv_equalizer.setColorFilter(ApplicationUtil.colorUtilsWhite(this));
        iv_rate.setColorFilter(ApplicationUtil.colorUtilsWhite(this));
        iv_fav.setColorFilter(ApplicationUtil.colorUtilsWhite(this));
        iv_option.setColorFilter(ApplicationUtil.colorUtilsWhite(this));
        iv_share.setColorFilter(ApplicationUtil.colorUtilsWhite(this));
        iv_bug.setColorFilter(ApplicationUtil.colorUtilsWhite(this));

        iv_radio_next.setColorFilter(ApplicationUtil.colorUtilsWhite(this));
        iv_radio_previous.setColorFilter(ApplicationUtil.colorUtilsWhite(this));
        iv_radio_timer.setColorFilter(ApplicationUtil.colorUtilsWhite(this));
        iv_microphone.setImageResource(R.drawable.ic_microphone2);
    }

    @Override
    protected int setLayoutResourceId() {
        if (nowPlayingScreen == 2) {
            return R.layout.activity_base_flat;
        }
        return R.layout.activity_base_normal;
    }

    @Override
    protected int setApplicationThemes() {
        SharedPref shared = new SharedPref(this);
        return shared.getModeTheme();
    }

    @SuppressLint("SetTextI18n")
    private void changeText(@NonNull ItemRadio itemRadio) {
        // BOTTOM PLAYER
        String subName, category_name = "";
        if (!itemRadio.getCategoryName().isEmpty()) {
            category_name = itemRadio.getCategoryName();
        }
        subName = itemRadio.getRadioTitle() + " • " + category_name;
        tv_min_title.setText(subName);
        if (!tv_min_title.getText().toString().isEmpty()) {
            tv_min_title.setSelected(true);
        }

        // MAIN PLAYER
        changeFav(itemRadio.IsFav());
        ratingBar.setRating(Integer.parseInt(itemRadio.getAverageRating()));
        tv_radio_title.setText(itemRadio.getRadioTitle());
        tv_radio_cat.setText(itemRadio.getCategoryName());
        tv_radio_count.setText((Callback.playPos + 1) + "/" + Callback.arrayList_play.size());

        ratingBar.setVisibility(Callback.isRadio ? View.VISIBLE : View.INVISIBLE);
        music_player_options.setVisibility(Callback.isRadio ? View.VISIBLE : View.INVISIBLE);

        media_seekbar.setVisibility(Callback.isRadio ? View.INVISIBLE : View.VISIBLE);
        progressBar_min.setVisibility(Callback.isRadio ? View.INVISIBLE : View.VISIBLE);
        circular_min.setVisibility(Callback.isRadio ? View.INVISIBLE : View.VISIBLE);

        if (viewpager.getAdapter() == null || Callback.isNewAdded || !Callback.addedFrom.equals(imagePagerAdapter.getIsLoadedFrom())) {
            viewpager.setAdapter(imagePagerAdapter);
            Callback.isNewAdded = false;
        }
        try {
            viewpager.setCurrentItem(Callback.playPos);
        } catch (Exception e) {
            imagePagerAdapter.notifyDataSetChanged();
            viewpager.setCurrentItem(Callback.playPos);
        }

        if (nowPlayingScreen == 2) {
            LoadColorTitle(itemRadio);
        } else if (nowPlayingScreen == 3) {
            LoadBgBlur(itemRadio);
        } else if (nowPlayingScreen == 4) {
            ImageView imageViewBackground = findViewById(R.id.iv_bg_player_blur);
            imageViewBackground.setImageResource(R.drawable.shadow_up_now_play);
            new LoadColor(imageViewBackground).execute(itemRadio.getImage());
        }
    }

    @SuppressLint("SetTextI18n")
    private void changeTextPager(@NonNull ItemRadio itemRadio) {
        // MAIN PLAYER
        ratingBar.setRating(Integer.parseInt(itemRadio.getAverageRating()));
        tv_radio_title.setText(itemRadio.getRadioTitle());
        tv_radio_cat.setText(itemRadio.getCategoryName());
        tv_radio_count.setText((viewpager.getCurrentItem() + 1) + "/" + Callback.arrayList_play.size());
        try {
            changeFav(itemRadio.IsFav());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void LoadColorTitle(@NonNull ItemRadio itemRadio) {
        String lodeURL = String.valueOf(Uri.parse(itemRadio.getImageThumb()));
        Picasso.get()
                .load(lodeURL)
                .centerCrop()
                .resize(100, 100)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        try {
                            Palette.from(bitmap).generate(palette -> {
                                try {
                                    if (palette != null) {
                                        Palette.Swatch textSwatch = palette.getVibrantSwatch();
                                        if (textSwatch == null) {
                                            return;
                                        }
                                        tv_radio_title.setBackgroundTintList(ColorStateList.valueOf(textSwatch.getRgb()));
                                        tv_radio_cat.setBackgroundTintList(ColorStateList.valueOf(textSwatch.getRgb()));
                                        tv_radio_title.setTextColor(ContextCompat.getColor(BaseActivity.this, R.color.titleNight));
                                        tv_radio_cat.setTextColor(ContextCompat.getColor(BaseActivity.this, R.color.titleNight));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        // this method is empty
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        // this method is empty
                    }
                });
    }

    private void LoadBgBlur(@NonNull ItemRadio itemRadio) {
        String lodeURL = String.valueOf(Uri.parse(itemRadio.getImage()));
        final ImageView imageViewBackground = findViewById(R.id.iv_bg_player_blur);
        try {
            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    try {
                        int blur_amount = sharedPref.getBlurAmount();
                        if (blur_amount >= 0 && blur_amount < 6) {
                            imageViewBackground.setImageBitmap(BlurImage.fastBlur(bitmap, 1f, 5));
                        } else {
                            imageViewBackground.setImageBitmap(BlurImage.fastBlur(bitmap, 1f, blur_amount));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    imageViewBackground.setImageResource(R.drawable.placeholder_song_night);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    // this method is empty
                }
            };
            imageViewBackground.setTag(target);
            Picasso.get()
                    .load(lodeURL)
                    .placeholder(ApplicationUtil.placeholderRadio())
                    .into(target);

        } catch (Exception e) {
            e.printStackTrace();
            imageViewBackground.setImageResource(R.drawable.placeholder_song_night);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_search_item:
                startActivity(new Intent(BaseActivity.this, SearchActivity.class));
                break;
            case R.id.menu_not:
                if (sharedPref.isLogged()) {
                    startActivity(new Intent(BaseActivity.this, NotificationActivity.class));
                } else {
                    helper.clickLogin();
                }
                break;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(@NonNull View view) {
        switch (view.getId()) {
            case R.id.iv_min_play:
            case R.id.ppv_radio_play:
                playPause();
                break;
            case R.id.iv_min_next:
            case R.id.iv_radio_next:
                next();
                break;
            case R.id.iv_min_previous:
            case R.id.iv_radio_previous:
                previous();
                break;
            case R.id.iv_radio_timer:
                if (!sharedPref.getIsSleepTimeOn()) {
                    setStartTimer();
                } else {
                    setCancelTimer();
                }
                break;
            case R.id.iv_radio_microphone:
                recordRadio();
                break;
            case R.id.iv_player_equalizer:
                openEqualizer();
                break;
            case R.id.iv_player_rate:
                showRateDialog();
                break;
            case R.id.iv_player_fav:
                loadFav();
                break;
            case R.id.iv_player_share:
                shareIntent();
                break;
            case R.id.iv_player_bug:
                feedBackDialog();
                break;
            case R.id.iv_player_option:
                openOptionPopUp();
                break;
            default:
                break;
        }
    }

    private void recordRadio() {
        if (checkPer()) {
            if (!AudioRecorder.getIsRecord()) {
                if (PlayerService.getInstance() != null && Callback.isPlayed && Callback.isTogglePlay && Callback.isRadio) {
                    new AudioRecorder(this).onStartRecord();
                    if (ApplicationUtil.isDarkMode()) {
                        iv_microphone.setImageResource(R.drawable.ic_microphone_b);
                    } else {
                        if (nowPlayingScreen == 3 || nowPlayingScreen == 4) {
                            iv_microphone.setImageResource(R.drawable.ic_microphone_b);
                        } else {
                            iv_microphone.setImageResource(R.drawable.ic_microphone_b2);
                        }
                    }
                    Toast.makeText(BaseActivity.this, getString(R.string.recording_start), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BaseActivity.this, getString(R.string.not_start_fm), Toast.LENGTH_SHORT).show();
                }
            } else {
                AudioRecorder.onStopRecord();
                if (ApplicationUtil.isDarkMode()) {
                    iv_microphone.setImageResource(R.drawable.ic_microphone2);
                } else {
                    if (nowPlayingScreen == 3 || nowPlayingScreen == 4) {
                        iv_microphone.setImageResource(R.drawable.ic_microphone2);
                    } else {
                        iv_microphone.setImageResource(R.drawable.ic_microphone);
                    }
                }
                Toast.makeText(BaseActivity.this, getString(R.string.recording_complete), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setStartTimer() {
        TimerStartDialog timerStart = new TimerStartDialog(this, (hours, minute) -> {
            String totalTime = hours + ":" + minute;
            long total_timer = ApplicationUtil.convert_long(totalTime) + System.currentTimeMillis();

            Random random = new Random();
            int id = random.nextInt(100);

            sharedPref.setSleepTime(true, total_timer, id);

            Intent intent = new Intent(BaseActivity.this, TimeReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(BaseActivity.this, id, intent, PendingIntent.FLAG_IMMUTABLE);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, total_timer, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, total_timer, pendingIntent);
            }
            updateTimer(total_timer);
        });
        timerStart.showDialog();
    }

    private void updateTimer(long total_timer) {
        long time_left = total_timer - System.currentTimeMillis();
        if (time_left > 0) {

            @SuppressLint("DefaultLocale")
            String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(time_left),
                    TimeUnit.MILLISECONDS.toMinutes(time_left) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(time_left) % TimeUnit.MINUTES.toSeconds(1));
            textView_timer.setVisibility(View.VISIBLE);
            textView_timer.setText(hms);

            new Handler().postDelayed(() -> {
                if (sharedPref.getIsSleepTimeOn()) {
                    updateTimer(total_timer);
                } else {
                    textView_timer.setVisibility(View.GONE);
                }
            }, 1000);
        } else {
            textView_timer.setVisibility(View.GONE);
        }
    }

    private void getTimeData() {
        if (sharedPref.getIsSleepTimeOn()) {
            sharedPref.setCheckSleepTime();
            updateTimer(sharedPref.getSleepTime());
        }
        getRecording();
    }

    private void setCancelTimer() {
        TimerCancelDialog timerCancel = new TimerCancelDialog(this, () -> {
            textView_timer.setVisibility(View.GONE);
            Intent intent = new Intent(BaseActivity.this, TimeReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(BaseActivity.this, sharedPref.getSleepID(), intent, PendingIntent.FLAG_IMMUTABLE);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            pendingIntent.cancel();
            alarmManager.cancel(pendingIntent);
            sharedPref.setSleepTime(false, 0, 0);
            Toast.makeText(BaseActivity.this, getString(R.string.stop_timer), Toast.LENGTH_SHORT).show();
        });
        timerCancel.showDialog();
    }

    private void showRateDialog() {
        if (Callback.arrayList_play.size() > 0) {
            ReviewDialog reviewDialog = new ReviewDialog(this, new ReviewDialog.RatingDialogListener() {
                @Override
                public void onShow() {
                    // this method is empty
                }

                @Override
                public void onGetRating(String rating, String message) {
                    Callback.arrayList_play.get(viewpager.getCurrentItem()).setUserRating(String.valueOf(rating));
                    Callback.arrayList_play.get(viewpager.getCurrentItem()).setUserMessage(message);
                }

                @Override
                public void onDismiss(String success, String rateSuccess, String message, int rating, String userRating, String userMessage) {
                    if (success.equals("1")) {
                        if (rateSuccess.equals("1")) {
                            try {
                                Callback.arrayList_play.get(viewpager.getCurrentItem()).setAverageRating(String.valueOf(rating));
                                Callback.arrayList_play.get(viewpager.getCurrentItem()).setTotalRate(String.valueOf(Integer.parseInt(Callback.arrayList_play.get(viewpager.getCurrentItem()).getTotalRate() + 1)));
                                Callback.arrayList_play.get(viewpager.getCurrentItem()).setUserRating(String.valueOf(userRating));
                                Callback.arrayList_play.get(viewpager.getCurrentItem()).setUserMessage(String.valueOf(userMessage));
                                ratingBar.setRating(rating);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Toast.makeText(BaseActivity.this, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(BaseActivity.this, getString(R.string.error_server_not_connected), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            reviewDialog.showDialog(
                    Callback.arrayList_play.get(viewpager.getCurrentItem()).getId(),
                    Callback.arrayList_play.get(viewpager.getCurrentItem()).getUserRating(),
                    Callback.arrayList_play.get(viewpager.getCurrentItem()).getUserMessage()
            );
        } else {
            Toast.makeText(BaseActivity.this, getString(R.string.error_no_data_found), Toast.LENGTH_SHORT).show();
        }
    }

    private void feedBackDialog() {
        if (Callback.arrayList_play.size() > 0) {
            new FeedBackDialog(this).showDialog(
                    Callback.arrayList_play.get(viewpager.getCurrentItem()).getId(),
                    Callback.arrayList_play.get(viewpager.getCurrentItem()).getRadioTitle()
            );
        } else {
            Toast.makeText(BaseActivity.this, getString(R.string.error_no_data_found), Toast.LENGTH_SHORT).show();
        }
    }

    private void openOptionPopUp() {
        if (Callback.arrayList_play.size() > 0) {
            ContextThemeWrapper ctw;
            if (ApplicationUtil.isDarkMode()) {
                ctw = new ContextThemeWrapper(BaseActivity.this, R.style.PopupMenuDark);
            } else {
                ctw = new ContextThemeWrapper(BaseActivity.this, R.style.PopupMenuLight);
            }
            PopupMenu popup = new PopupMenu(ctw, iv_option);
            popup.getMenuInflater().inflate(R.menu.popup_player_option, popup.getMenu());
            popup.setForceShowIcon(true);
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.popup_description) {
                    if (Callback.arrayList_play.size() > 0) {
                        String radio_id = Callback.arrayList_play.get(viewpager.getCurrentItem()).getId();
                        String radio_title = Callback.arrayList_play.get(viewpager.getCurrentItem()).getRadioTitle();
                        Intent intent = new Intent(BaseActivity.this, WebActivity.class);
                        intent.putExtra("web_url", BuildConfig.BASE_URL + "radio_description.php?radio_id=" + radio_id);
                        intent.putExtra("page_title", radio_title);
                        ActivityCompat.startActivity(BaseActivity.this, intent, null);
                    } else {
                        Toast.makeText(BaseActivity.this, getString(R.string.error_no_selected), Toast.LENGTH_SHORT).show();
                    }
                } else if (item.getItemId() == R.id.popup_go_to_category) {
                    if (Callback.arrayList_play.size() > 0) {
                        Intent intent = new Intent(BaseActivity.this, PostIDActivity.class);
                        intent.putExtra("page_type", getString(R.string.category));
                        intent.putExtra("id", Callback.arrayList_play.get(viewpager.getCurrentItem()).getCatID());
                        intent.putExtra("name", Callback.arrayList_play.get(viewpager.getCurrentItem()).getCategoryName());
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(BaseActivity.this, getString(R.string.error_no_selected), Toast.LENGTH_SHORT).show();
                    }
                } else if (item.getItemId() == R.id.popup_drive_mode) {
                    if (Callback.arrayList_play.size() > 0) {
                        startActivity(new Intent(BaseActivity.this, DriveModeActivity.class));
                    } else {
                        Toast.makeText(BaseActivity.this, getString(R.string.error_no_selected), Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            });
            popup.show();
        } else {
            Toast.makeText(BaseActivity.this, getString(R.string.error_no_data_found), Toast.LENGTH_SHORT).show();
        }
    }

    private void openEqualizer() {
        Intent intent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
        if ((intent.resolveActivity(getPackageManager()) != null)) {
            startActivity(intent);
        } else {
            Toast.makeText(BaseActivity.this, "Not supported this device", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareIntent() {
        if (Callback.arrayList_play.size() > 0) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    Callback.arrayList_play.get(viewpager.getCurrentItem()).getRadioTitle() + "\n\n" +
                            Callback.arrayList_play.get(viewpager.getCurrentItem()).getCategoryName() + "\n" +
                            "https://play.google.com/store/apps/details?id=" + getPackageName());
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } else {
            Toast.makeText(BaseActivity.this, getString(R.string.error_no_selected), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFav() {
        if (Callback.arrayList_play.size() > 0) {
            if (sharedPref.isLogged()) {
                if (Callback.arrayList_play.size() > 0) {
                    if (helper.isNetworkAvailable()) {
                        LoadStatus loadFav = new LoadStatus(new SuccessListener() {
                            @Override
                            public void onStart() {
                                changeFav(!Callback.arrayList_play.get(viewpager.getCurrentItem()).IsFav());
                            }

                            @Override
                            public void onEnd(String success, String favSuccess, String message) {
                                if (success.equals("1")) {
                                    Callback.arrayList_play.get(viewpager.getCurrentItem()).setIsFav(message.equals("Added to Favourite"));
                                    changeFav(Callback.arrayList_play.get(viewpager.getCurrentItem()).IsFav());
                                    Toast.makeText(BaseActivity.this, message, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(BaseActivity.this, getString(R.string.error_server_not_connected), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, helper.callAPI(Callback.METHOD_DO_FAV, 0, Callback.arrayList_play.get(viewpager.getCurrentItem()).getId(), "", "", "", sharedPref.getUserId(), "", "", "", "", "", "", "", null));
                        loadFav.execute();
                    } else {
                        Toast.makeText(BaseActivity.this, getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(BaseActivity.this, getString(R.string.error_no_selected), Toast.LENGTH_SHORT).show();
                }
            } else {
                helper.clickLogin();
            }
        } else {
            Toast.makeText(BaseActivity.this, getString(R.string.error_no_selected), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void changeFav(@NonNull Boolean isFav) {
        if (isFav) {
            iv_fav.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_do));
        } else {
            iv_fav.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_border));
        }
        if (nowPlayingScreen == 3 || nowPlayingScreen == 4) {
            iv_fav.setColorFilter(ApplicationUtil.colorUtilsWhite(BaseActivity.this));
        } else {
            iv_fav.setColorFilter(ApplicationUtil.colorUtils(BaseActivity.this));
        }
    }

    private void getRecording() {
        if (AudioRecorder.getIsRecord()) {
            if (ApplicationUtil.isDarkMode()) {
                iv_microphone.setImageResource(R.drawable.ic_microphone_b);
            } else {
                if (nowPlayingScreen == 3 || nowPlayingScreen == 4) {
                    iv_microphone.setImageResource(R.drawable.ic_microphone_b);
                } else {
                    iv_microphone.setImageResource(R.drawable.ic_microphone_b2);
                }
            }
        } else {
            if (ApplicationUtil.isDarkMode()) {
                iv_microphone.setImageResource(R.drawable.ic_microphone2);
            } else {
                if (nowPlayingScreen == 3 || nowPlayingScreen == 4) {
                    iv_microphone.setImageResource(R.drawable.ic_microphone2);
                } else {
                    iv_microphone.setImageResource(R.drawable.ic_microphone);
                }
            }
        }
    }

    private class ImagePagerAdapter extends PagerAdapter {

        private final LayoutInflater inflater;
        private String loadedPage = "";

        private ImagePagerAdapter() {
            inflater = getLayoutInflater();
        }

        @Override
        public int getCount() {
            return Callback.arrayList_play.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view.equals(object);
        }

        String getIsLoadedFrom() {
            return loadedPage;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {
            View imageLayout;
            final boolean isTextColor;
            switch (nowPlayingScreen) {
                case 1:
                    imageLayout = inflater.inflate(R.layout.row_viewpager_player_circle, container, false);
                    isTextColor = false;
                    break;
                case 2:
                    imageLayout = inflater.inflate(R.layout.row_viewpager_player_flat, container, false);
                    isTextColor = true;
                    break;
                default:
                    imageLayout = inflater.inflate(R.layout.row_viewpager_player_normal, container, false);
                    isTextColor = true;
                    break;
            }
            final RoundedImageView imageView_my = imageLayout.findViewById(R.id.image);
            final ImageView imageView_play = imageLayout.findViewById(R.id.iv_vp_play);
            final ProgressBar spinner = imageLayout.findViewById(R.id.loading);
            final RelativeLayout my = imageLayout.findViewById(R.id.background);

            loadedPage = Callback.addedFrom;

            if (Callback.playPos == position) {
                imageView_play.setVisibility(View.GONE);
            }

            Picasso.get()
                    .load(Callback.arrayList_play.get(position).getImage())
                    .placeholder(ApplicationUtil.placeholderRadio())
                    .into(imageView_my, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            spinner.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            spinner.setVisibility(View.GONE);
                        }
                    });
            if (isTextColor) {
                Picasso.get()
                        .load(Callback.arrayList_play.get(position).getImage())
                        .centerCrop()
                        .resize(100, 100)
                        .into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                try {
                                    Palette.from(bitmap).generate(palette -> {
                                        if (palette != null) {
                                            Palette.Swatch textSwatch = palette.getVibrantSwatch();
                                            if (textSwatch == null) {
                                                return;
                                            }
                                            try {
                                                my.setBackgroundTintList(ColorStateList.valueOf(textSwatch.getRgb()));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                // this method is empty
                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {
                                // this method is empty
                            }
                        });
            }

            imageView_play.setOnClickListener(v -> {
                if (helper.isNetworkAvailable()) {
                    try {
                        if (Callback.isPurchases) {
                            Callback.playPos = viewpager.getCurrentItem();
                            Intent intent = new Intent(BaseActivity.this, PlayerService.class);
                            intent.setAction(PlayerService.ACTION_PLAY);
                            startService(intent);
                            imageView_play.setVisibility(View.GONE);
                        } else {
                            if (Callback.arrayList_play.get(viewpager.getCurrentItem()).IsPremium() && !Callback.isPurchases) {
                                new PremiumDialog(BaseActivity.this);
                            } else {
                                Callback.playPos = viewpager.getCurrentItem();
                                Intent intent = new Intent(BaseActivity.this, PlayerService.class);
                                intent.setAction(PlayerService.ACTION_PLAY);
                                startService(intent);
                                imageView_play.setVisibility(View.GONE);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(BaseActivity.this, getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
                }
            });

            imageLayout.setTag("myview" + position);
            container.addView(imageLayout, 0);
            return imageLayout;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    private final Runnable run = this::seekUpdating;

    public void seekUpdating() {
        try {
            if (!Callback.isRadio) {
                try {
                    circular_min.setProgress(helper.getProgressPercentage(PlayerService.exoPlayer.getCurrentPosition(), PlayerService.getInstance().getDuration()));
                    progressBar_min.setProgress(helper.getProgressPercentage(PlayerService.exoPlayer.getCurrentPosition(), PlayerService.getInstance().getDuration()));

                    seekBar_music.setProgress(helper.getProgressPercentage(PlayerService.exoPlayer.getCurrentPosition(), PlayerService.getInstance().getDuration()));
                    tv_current_time.setText(helper.milliSecondsToTimer(PlayerService.exoPlayer.getCurrentPosition(), PlayerService.getInstance().getDuration()));
                    tv_total_time.setText(helper.milliSecondsToTimer(PlayerService.exoPlayer.getDuration(), PlayerService.getInstance().getDuration()));
                    seekBar_music.setSecondaryProgress(PlayerService.exoPlayer.getBufferedPercentage());

                    if (PlayerService.exoPlayer.getPlayWhenReady() && Callback.isAppOpen) {
                        seekHandler.removeCallbacks(run);
                        seekHandler.postDelayed(run, 1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if (seekHandler != null) {
                    seekHandler.removeCallbacks(run);
                    seekHandler.postDelayed(run, 1000);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void playPause() {
        if (Callback.arrayList_play.size() > 0) {
            Intent intent = new Intent(BaseActivity.this, PlayerService.class);
            if (Callback.isPlayed) {
                intent.setAction(PlayerService.ACTION_TOGGLE);
                startService(intent);
            } else {
                if (helper.isNetworkAvailable()) {
                    intent.setAction(PlayerService.ACTION_PLAY);
                    startService(intent);
                } else {
                    Toast.makeText(BaseActivity.this, getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(BaseActivity.this, getString(R.string.not_start_fm), Toast.LENGTH_SHORT).show();
        }
    }


    public void next() {
        if (Callback.arrayList_play.size() > 0) {
            if (helper.isNetworkAvailable()) {
                Intent intent = new Intent(BaseActivity.this, PlayerService.class);
                intent.setAction(PlayerService.ACTION_NEXT);
                startService(intent);
            } else {
                Toast.makeText(BaseActivity.this, getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(BaseActivity.this, getString(R.string.not_start_fm), Toast.LENGTH_SHORT).show();
        }
    }

    public void previous() {
        if (Callback.arrayList_play.size() > 0) {
            if (helper.isNetworkAvailable()) {
                Intent intent = new Intent(BaseActivity.this, PlayerService.class);
                intent.setAction(PlayerService.ACTION_PREVIOUS);
                startService(intent);
            } else {
                Toast.makeText(BaseActivity.this, getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(BaseActivity.this, getString(R.string.not_start_fm), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void changePlayPauseIcon(Boolean isPlay) {
        if (!isPlay) {
            iv_min_play.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
        } else {
            iv_min_play.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
        }
        ppv_radio_play.change(!isPlay);
        seekUpdating();
    }

    public void isBuffering(@NonNull Boolean isBuffer) {
        if (isBuffer) {
            ppv_radio_play.setVisibility(View.INVISIBLE);
            rl_radio_loading.setVisibility(View.VISIBLE);
            pb_min.setVisibility(View.VISIBLE);
        } else {
            ppv_radio_play.setVisibility(View.VISIBLE);
            rl_radio_loading.setVisibility(View.INVISIBLE);
            pb_min.setVisibility(View.INVISIBLE);
            changePlayPauseIcon(true);
        }
        iv_min_next.setEnabled(!isBuffer);
        iv_min_previous.setEnabled(!isBuffer);

        ppv_radio_play.setEnabled(!isBuffer);
        iv_radio_previous.setEnabled(!isBuffer);
        iv_radio_next.setEnabled(!isBuffer);
        seekBar_music.setEnabled(!isBuffer);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSongChange(ItemRadio itemRadio) {
        changeText(itemRadio);
        Callback.context = this;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onBufferChange(@NonNull MessageEvent messageEvent) {
        if (messageEvent.message.equals("buffer")) {
            isBuffering(messageEvent.flag);
        } else {
            changePlayPauseIcon(messageEvent.flag);
        }
        getRecording();
    }

    @SuppressLint("SetTextI18n")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onViewPagerChanged(ItemCategory itemCategory) {
        imagePagerAdapter.notifyDataSetChanged();
        tv_radio_count.setText(Callback.playPos + 1 + "/" + Callback.arrayList_play.size());
        GlobalBus.getBus().removeStickyEvent(itemCategory);
    }

    @Override
    public void onStart() {
        super.onStart();
        GlobalBus.getBus().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(
                        appUpdateInfo -> {
                            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                                // If an in-app update is already running, resume the update.
                                try {
                                    appUpdateManager.startUpdateFlowForResult(
                                            appUpdateInfo, AppUpdateType.IMMEDIATE,
                                            this,
                                            MY_REQUEST_CODE);
                                } catch (IntentSender.SendIntentException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
    }

    @Override
    protected void onPause() {
        if (!Callback.isRadio) {
            try {
                seekHandler.removeCallbacks(run);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onPause();
    }

    public Boolean checkPer() {
        if (Build.VERSION.SDK_INT >= 33) {
            if ((ContextCompat.checkSelfPermission(BaseActivity.this, READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(new String[]{READ_MEDIA_AUDIO}, 101);
                return false;
            } else {
                return true;
            }
        } else if (android.os.Build.VERSION.SDK_INT >= 29) {
            if ((ContextCompat.checkSelfPermission(BaseActivity.this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, 101);
                return false;
            } else {
                return true;
            }
        } else {
            if ((ContextCompat.checkSelfPermission(BaseActivity.this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, 101);
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean canUseExternalStorage = false;
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                canUseExternalStorage = true;
            }
            if (!canUseExternalStorage) {
                Toast.makeText(BaseActivity.this, getString(R.string.error_cannot_use_features), Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}