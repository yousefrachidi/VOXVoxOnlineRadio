package nemosofts.vox.radio.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.nemosofts.lk.ProCompatActivity;
import androidx.nemosofts.lk.view.BlurImage;
import androidx.palette.graphics.Palette;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import nemosofts.vox.radio.R;
import nemosofts.vox.radio.asyncTask.LoadStatus;
import nemosofts.vox.radio.callback.Callback;
import nemosofts.vox.radio.ifSupported.IsRTL;
import nemosofts.vox.radio.ifSupported.IsScreenshot;
import nemosofts.vox.radio.interfaces.SuccessListener;
import nemosofts.vox.radio.item.ItemRadio;
import nemosofts.vox.radio.utils.ApplicationUtil;
import nemosofts.vox.radio.utils.GlobalBus;
import nemosofts.vox.radio.utils.Helper;
import nemosofts.vox.radio.utils.MessageEvent;
import nemosofts.vox.radio.utils.SharedPref;


public class DriveModeActivity extends YouraCompatActivity implements View.OnClickListener {

    Helper helper;
    SharedPref sharedPref;
    TextView tv_music_title;
    ImageView iv_blur_bg;
    ImageView iv_music_previous, iv_music_next, iv_music_play, iv_drive_mode_fav;
    ProgressBar pb_music_loading;
    ImageView iv_drive_mode_close;
    RoundedImageView iv_drive_mode_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideStatusBar();

        IsRTL.ifSupported(this);
        IsScreenshot.ifSupported(this);

        helper = new Helper(this);
        sharedPref = new SharedPref(this);

        tv_music_title = findViewById(R.id.tv_title_drive_mode);
        iv_blur_bg = findViewById(R.id.iv_drive_mode);

        pb_music_loading = findViewById(R.id.pb_music_loading);
        iv_music_play = findViewById(R.id.iv_music_play);
        iv_music_previous = findViewById(R.id.iv_music_previous);
        iv_music_next = findViewById(R.id.iv_music_next);
        iv_drive_mode_fav = findViewById(R.id.iv_drive_mode_fav);
        iv_drive_mode_add = findViewById(R.id.iv_drive_mode_add);

        iv_music_play.setOnClickListener(this);
        iv_music_previous.setOnClickListener(this);
        iv_music_next.setOnClickListener(this);
        iv_drive_mode_fav.setOnClickListener(this);

        iv_drive_mode_close = findViewById(R.id.iv_drive_mode_close);
        iv_drive_mode_close.setOnClickListener(view -> onBackPressed());
    }

    @Override
    protected int setLayoutResourceId() {
        return R.layout.activity_drive_mode;
    }

    @Override
    protected int setApplicationThemes() {
        return 1;
    }

    @SuppressLint("SetTextI18n")
    public void changeText(@NonNull ItemRadio itemRadio) {
        tv_music_title.setText(itemRadio.getRadioTitle());
        changeFav(itemRadio.IsFav());
        try {
            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    try {
                        int blur_amount = sharedPref.getBlurAmountDrive();
                        iv_blur_bg.setImageBitmap(BlurImage.fastBlur(bitmap, 1f, blur_amount));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    iv_blur_bg.setImageResource(R.drawable.placeholder_song_night);
                }
                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    // this method is empty
                }
            };
            iv_blur_bg.setTag(target);
            Picasso.get()
                    .load(itemRadio.getImage())
                    .placeholder(R.drawable.placeholder_song_night)
                    .into(target);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Picasso.get()
                .load(itemRadio.getImage())
                .placeholder(R.drawable.placeholder_song_night)
                .into(iv_drive_mode_add);

        if (sharedPref.isDriveColor()){
            setColorText(itemRadio);
        }
    }

    private void setColorText(ItemRadio itemRadio) {
        try {
            Picasso.get()
                    .load(itemRadio.getImageThumb())
                    .centerCrop()
                    .resize(100, 100)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            Palette.from(bitmap).generate(palette -> {
                                if (palette != null){
                                    Palette.Swatch textSwatch = palette.getVibrantSwatch();
                                    if (textSwatch == null) {
                                        return;
                                    }
                                    try {
                                        View view_1 = findViewById(R.id.view_1);
                                        View view_2 = findViewById(R.id.view_2);
                                        view_1.setBackgroundTintList(ColorStateList.valueOf(textSwatch.getRgb()));
                                        view_2.setBackgroundTintList(ColorStateList.valueOf(textSwatch.getRgb()));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void changeFav(@NonNull Boolean isFav) {
        if (isFav) {
            iv_drive_mode_fav.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_do));
        } else {
            iv_drive_mode_fav.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_border));
        }
        iv_drive_mode_fav.setColorFilter(ApplicationUtil.colorUtilsWhite(this));
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(@NonNull View view) {
        switch (view.getId()) {
            case R.id.iv_music_play:
                playPause();
                break;
            case R.id.iv_music_previous:
                previous();
                break;
            case R.id.iv_music_next:
                next();
                break;
            case R.id.iv_drive_mode_fav:
                doFav();
                break;
            default:
                break;
        }
    }

    private void doFav() {
        if (sharedPref.isLogged()) {
            if (Callback.arrayList_play.size() > 0) {
                if (helper.isNetworkAvailable()) {
                    LoadStatus loadFav = new LoadStatus(new SuccessListener() {
                        @Override
                        public void onStart() {
                            changeFav(!Callback.arrayList_play.get(Callback.playPos).IsFav());
                        }

                        @Override
                        public void onEnd(String success, String favSuccess, String message) {
                            if (success.equals("1")) {
                                Callback.arrayList_play.get(Callback.playPos).setIsFav(message.equals("Added to Favourite"));
                                changeFav(Callback.arrayList_play.get(Callback.playPos).IsFav());
                                showSnackBar(message);
                            } else {
                                showSnackBar(getString(R.string.error_server_not_connected));
                            }
                        }
                    }, helper.callAPI(Callback.METHOD_DO_FAV, 0, Callback.arrayList_play.get(Callback.playPos).getId(), "", "", "", sharedPref.getUserId(), "", "", "", "", "", "", "", null));
                    loadFav.execute();
                } else {
                    showSnackBar(getString(R.string.error_internet_not_connected));
                }
            } else {
                showSnackBar(getString(R.string.error_no_selected));
            }
        } else {
            helper.clickLogin();
        }
    }

    private void showSnackBar(@NonNull String message) {
        Toast.makeText(DriveModeActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    public void playPause() {
        if (Callback.arrayList_play.size() > 0) {
            Intent intent = new Intent(DriveModeActivity.this, PlayerService.class);
            if (Callback.isPlayed) {
                intent.setAction(PlayerService.ACTION_TOGGLE);
                startService(intent);
            } else {
                if (helper.isNetworkAvailable()) {
                    intent.setAction(PlayerService.ACTION_PLAY);
                    startService(intent);
                } else {
                    showSnackBar(getString(R.string.error_internet_not_connected));
                }
            }
        } else {
            showSnackBar(getString(R.string.error_no_selected));
        }
    }

    public void previous() {
        if (Callback.arrayList_play.size() > 0) {
            if (helper.isNetworkAvailable()) {
                Intent intent = new Intent(DriveModeActivity.this, PlayerService.class);
                intent.setAction(PlayerService.ACTION_PREVIOUS);
                startService(intent);
            } else {
                showSnackBar(getString(R.string.error_internet_not_connected));
            }
        } else {
           showSnackBar(getString(R.string.error_no_selected));
        }
    }

    public void next() {
        if (Callback.arrayList_play.size() > 0) {
            if (helper.isNetworkAvailable()) {
                Intent intent = new Intent(DriveModeActivity.this, PlayerService.class);
                intent.setAction(PlayerService.ACTION_NEXT);
                startService(intent);
            } else {
                showSnackBar(getString(R.string.error_internet_not_connected));
            }
        } else {
            showSnackBar(getString(R.string.error_no_selected));
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void changePlayPauseIcon(Boolean isPlay) {
        if (!isPlay) {
            iv_music_play.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
        } else {
            iv_music_play.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
        }
    }

    public void isBuffering(@NonNull Boolean isBuffer) {
        if (isBuffer) {
            iv_music_play.setVisibility(View.GONE);
            pb_music_loading.setVisibility(View.VISIBLE);
        } else {
            iv_music_play.setVisibility(View.VISIBLE);
            pb_music_loading.setVisibility(View.GONE);
            changePlayPauseIcon(true);
        }
        iv_music_play.setEnabled(!isBuffer);
        iv_music_next.setEnabled(!isBuffer);
        iv_music_previous.setEnabled(!isBuffer);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSongChange(ItemRadio itemRadio) {
        changeText(itemRadio);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onBufferChange(@NonNull MessageEvent messageEvent) {
        if (messageEvent.message.equals("buffer")) {
            isBuffering(messageEvent.flag);
        } else {
            changePlayPauseIcon(messageEvent.flag);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        GlobalBus.getBus().register(this);
    }

    @Override
    public void onStop() {
        GlobalBus.getBus().unregister(this);
        super.onStop();
    }

    private void hideStatusBar() {
        try {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        } catch (Exception e) {
            e.printStackTrace();
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
}