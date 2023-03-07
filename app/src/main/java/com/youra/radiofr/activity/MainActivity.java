package com.youra.radiofr.activity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import com.youra.radiofr.R;
import com.youra.radiofr.asyncTask.LoadAbout;
import com.youra.radiofr.callback.Callback;
import com.youra.radiofr.dialog.ExitDialog;
import com.youra.radiofr.fragment.FragmentCategory;
import com.youra.radiofr.fragment.FragmentCountries;
import com.youra.radiofr.fragment.FragmentDashBoard;
import com.youra.radiofr.fragment.FragmentLatest;
import com.youra.radiofr.fragment.FragmentMost;
import com.youra.radiofr.fragment.FragmentPodcasts;
import com.youra.radiofr.fragment.FragmentRecent;
import com.youra.radiofr.interfaces.AboutListener;
import com.youra.radiofr.utils.AdManagerInterAdmob;
import com.youra.radiofr.utils.AdManagerInterApplovin;
import com.youra.radiofr.utils.AdManagerInterStartApp;
import com.youra.radiofr.utils.AdManagerInterWortise;
import com.youra.radiofr.utils.AudioRecorder;
import com.youra.radiofr.utils.SharedPref;
import com.youra.radiofr.utils.TimeReceiver;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    FragmentManager fm;
    String selectedFragment = "";
    MenuItem menu_login;
    MenuItem menu_profile;
    ReviewManager manager;
    ReviewInfo reviewInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        sharedPref.getThemeDetails();
        super.onCreate(savedInstanceState);

        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.content_main, contentFrameLayout);

        fm = getSupportFragmentManager();

        Callback.isAppOpen = true;

        Menu menu = navigationView.getMenu();
        menu_login = menu.findItem(R.id.nav_login);
        menu_profile = menu.findItem(R.id.nav_profile);

        navigationView.setNavigationItemSelectedListener(this);

//        changeLoginName();
//        loadAboutData();

        manager = ReviewManagerFactory.create(this);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                reviewInfo = task.getResult();
            }
        });

        tv_nav_home.setOnClickListener(view -> {
            if (!tv_nav_home.isActive()){
                FragmentDashBoard f_home = new FragmentDashBoard();
                loadFrag(f_home, getString(R.string.dashboard), fm);
            }
            bottomNavigationView(0);
        });
        tv_nav_latest.setOnClickListener(view -> {
            if (!tv_nav_latest.isActive()){
                FragmentLatest f_latest = new FragmentLatest();
                loadFrag(f_latest, getString(R.string.latest), fm);

            }
            bottomNavigationView(1);
        });
        tv_nav_most.setOnClickListener(view -> {
            if (!tv_nav_most.isActive()){
                FragmentMost f_most = new FragmentMost();
                loadFrag(f_most, getString(R.string.most), fm);
            }
            bottomNavigationView(2);
        });
        tv_nav_category.setOnClickListener(view -> {
            if (!tv_nav_category.isActive()){
                FragmentCategory f_category = new FragmentCategory();
                loadFrag(f_category, getString(R.string.category), fm);
            }
            bottomNavigationView(3);
        });
        tv_nav_restore.setOnClickListener(view -> {
            if (!tv_nav_restore.isActive()){
                FragmentRecent f_recent = new FragmentRecent();
                loadFrag(f_recent, getString(R.string.recent), fm);
            }
            bottomNavigationView(4);
        });

        loadDashboardFrag();
        changeLoginName();
        loadAboutData();
    }

    private void loadDashboardFrag() {
        FragmentDashBoard f1 = new FragmentDashBoard();
        loadFrag(f1, getResources().getString(R.string.dashboard), fm);
        navigationView.setCheckedItem(R.id.nav_home);
    }

    public void loadFrag(Fragment f1, String name, @NonNull FragmentManager fm) {
        selectedFragment = name;
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStackImmediate();
        }

        FragmentTransaction ft = fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (!name.equals(getString(R.string.dashboard))) {
            ft.hide(fm.getFragments().get(fm.getBackStackEntryCount()));
            ft.add(R.id.fragment, f1, name);
            ft.addToBackStack(name);
        } else {
            ft.replace(R.id.fragment, f1, name);
        }
        ft.commit();

        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle(name);
        }

        if (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void changeLoginName() {
        if (menu_login != null) {
            if (sharedPref.isLogged()) {
                menu_profile.setVisible(true);
                menu_login.setTitle(getResources().getString(R.string.logout));
                menu_login.setIcon(getResources().getDrawable(R.drawable.ic_logout));

            } else {
                menu_profile.setVisible(false);
                menu_login.setTitle(getResources().getString(R.string.login));
                menu_login.setIcon(getResources().getDrawable(R.drawable.ic_login));
            }
        }
    }

    public void loadAboutData() {
        if (helper.isNetworkAvailable()) {
            LoadAbout loadAbout = new LoadAbout(MainActivity.this, new AboutListener() {
                @Override
                public void onStart() {
                    // this method is empty
                }

                @Override
                public void onEnd(String success, String verifyStatus, String message) {
                    if (success.equals("1")) {
                      /*  helper.initializeAds();
                        sharedPref.setAdsDetails(Callback.isBannerAd, Callback.isInterAd, Callback.isNativeAd, Callback.adNetwork,
                                Callback.publisherAdID, Callback.startappAppId, "", Callback.ironAdsId,
                                Callback.bannerAdID, Callback.interstitialAdID, Callback.nativeAdID, Callback.interstitialAdShow, Callback.nativeAdShow);

                        if (Callback.isInterAd) {
                            switch (Callback.adNetwork) {
                                case Callback.AD_TYPE_ADMOB:
                                    AdManagerInterAdmob adManagerInterAdmob = new AdManagerInterAdmob(getApplicationContext());
                                    adManagerInterAdmob.createAd();
                                    break;
                                case Callback.AD_TYPE_STARTAPP:
                                    AdManagerInterStartApp adManagerInterStartApp = new AdManagerInterStartApp(getApplicationContext());
                                    adManagerInterStartApp.createAd();
                                    break;
                                case Callback.AD_TYPE_APPLOVIN:
                                    AdManagerInterApplovin adManagerInterApplovin = new AdManagerInterApplovin(MainActivity.this);
                                    adManagerInterApplovin.createAd();
                                    break;
                                case Callback.AD_TYPE_WORTISE:
                                    AdManagerInterWortise adManagerInterWortise = new AdManagerInterWortise(MainActivity.this);
                                    adManagerInterWortise.createAd();
                                    break;
                            }
                        }
                        */
                    }
                }
            });
            loadAbout.execute();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_home:
                if (!tv_nav_home.isActive()){
                    FragmentDashBoard f_home = new FragmentDashBoard();
                    loadFrag(f_home, getString(R.string.dashboard), fm);
                }
                bottomNavigationView(0);
                break;
            case R.id.nav_latest:
                if (!tv_nav_latest.isActive()){
                    FragmentLatest f_latest = new FragmentLatest();
                    loadFrag(f_latest, getString(R.string.latest), fm);
                }
                bottomNavigationView(1);
                break;
            case R.id.nav_most:
                if (!tv_nav_most.isActive()){
                    FragmentMost f_most = new FragmentMost();
                    loadFrag(f_most, getString(R.string.most), fm);
                }
                bottomNavigationView(2);
                break;
            case R.id.nav_category:
                if (!tv_nav_category.isActive()){
                    FragmentCategory f_category = new FragmentCategory();
                    loadFrag(f_category, getString(R.string.category), fm);
                }
                bottomNavigationView(3);
                break;
            case R.id.nav_restore:
                if (!tv_nav_restore.isActive()){
                    FragmentRecent f_recent = new FragmentRecent();
                    loadFrag(f_recent, getString(R.string.recent), fm);
                }
                bottomNavigationView(4);
                break;
            case R.id.nav_countries:
                String title = String.valueOf(getSupportActionBar().getTitle());
                if (!title.equals(getString(R.string.countries))) {
                    FragmentCountries f_countries = new FragmentCountries();
                    loadFrag(f_countries, getString(R.string.countries), fm);
                }
                bottomNavigationView(5);
                break;
            case R.id. nav_podcasts:
                String title2 = String.valueOf(getSupportActionBar().getTitle());
                if (!title2.equals(getString(R.string.podcasts))) {
                    FragmentPodcasts f_podcasts = new FragmentPodcasts();
                    loadFrag(f_podcasts, getString(R.string.podcasts), fm);
                }
                bottomNavigationView(5);
                break;
            case R.id.nav_fav:
                Intent intent = new Intent(MainActivity.this, PostIDActivity.class);
                intent.putExtra("page_type", getString(R.string.favourite));
                intent.putExtra("id", "");
                intent.putExtra("name", getString(R.string.favourite));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
          /*  case R.id.nav_rec:
                if (checkPer()) {
                    if (Callback.isRecording) {
                        Toast.makeText(this, getResources().getString(R.string.stop_record_first), Toast.LENGTH_SHORT).show();
                    } else {
                        ActivityCompat.startActivity(MainActivity.this, new Intent(MainActivity.this, RecorderActivity.class), null);
                    }
                }
                break;*/
            case R.id.nav_suggest:
                startActivity(new Intent(MainActivity.this, SuggestionActivity.class));
                break;
            case R.id.nav_profile:
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                break;
            case R.id.nav_settings:
                overridePendingTransition(0, 0);
                overridePendingTransition(0, 0);
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
                finish();
                break;
            case R.id.nav_login:
                helper.clickLogin();
                break;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void bottomNavigationView(int pos) {
        if (pos == 0){
            if (!tv_nav_home.isActive()){
                tv_nav_home.activate();
                tv_nav_home.setBadgeText("");
            }
        } else {
            if (tv_nav_home.isActive()){
                tv_nav_home.deactivate();
                tv_nav_home.setBadgeText(null);
            }
        }

        if (pos == 1){
            if (!tv_nav_latest.isActive()){
                tv_nav_latest.activate();
                tv_nav_latest.setBadgeText("");
            }
        } else {
            if (tv_nav_latest.isActive()){
                tv_nav_latest.deactivate();
                tv_nav_latest.setBadgeText(null);
            }
        }

        if (pos == 2){
            if (!tv_nav_most.isActive()){
                tv_nav_most.activate();
                tv_nav_most.setBadgeText("");
            }
        } else {
            if (tv_nav_most.isActive()){
                tv_nav_most.deactivate();
                tv_nav_most.setBadgeText(null);
            }
        }

        if (pos == 3){
            if (!tv_nav_category.isActive()){
                tv_nav_category.activate();
                tv_nav_category.setBadgeText("");
            }
        } else {
            if (tv_nav_category.isActive()){
                tv_nav_category.deactivate();
                tv_nav_category.setBadgeText(null);
            }
        }

        if (pos == 4){
            if (!tv_nav_restore.isActive()){
                tv_nav_restore.activate();
                tv_nav_restore.setBadgeText("");
            }
        } else {
            if (tv_nav_restore.isActive()){
                tv_nav_restore.deactivate();
                tv_nav_restore.setBadgeText(null);
            }
        }
        if (pos == 5){
            if (tv_nav_home.isActive()){
                tv_nav_home.deactivate();
                tv_nav_home.setBadgeText(null);
            }
            if (tv_nav_latest.isActive()){
                tv_nav_latest.deactivate();
                tv_nav_latest.setBadgeText(null);
            }
            if (tv_nav_most.isActive()){
                tv_nav_most.deactivate();
                tv_nav_most.setBadgeText(null);
            }
            if (tv_nav_category.isActive()){
                tv_nav_category.deactivate();
                tv_nav_category.setBadgeText(null);
            }
            if (tv_nav_restore.isActive()){
                tv_nav_restore.deactivate();
                tv_nav_restore.setBadgeText(null);
            }
        }
    }

    @Override
    public void onResume() {
        changeLoginName();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (mLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else if (fm.getBackStackEntryCount() != 0) {
            String title = fm.getFragments().get(fm.getBackStackEntryCount()).getTag();
            if (title != null){
                if (title.equals(getString(R.string.dashboard)) || title.equals(getString(R.string.home)) || title.equals(getString(R.string.latest)) || title.equals(getString(R.string.most)) ||
                        title.equals(getString(R.string.category)) || title.equals(getString(R.string.recent)) || title.equals(getString(R.string.countries)) || title.equals(getString(R.string.podcasts))) {
                    navigationView.setCheckedItem(R.id.nav_home);
                    bottomNavigationView(0);
                }
            }
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
            }
            super.onBackPressed();
        } else if (reviewInfo != null){
            Task<Void> flow = manager.launchReviewFlow(MainActivity.this, reviewInfo);
            flow.addOnCompleteListener(task1 -> new ExitDialog(this));
        } else {
            new ExitDialog(this);
        }
    }

    @Override
    protected void onDestroy() {
        Callback.isAppOpen = false;
        if (PlayerService.exoPlayer != null && !PlayerService.exoPlayer.getPlayWhenReady()) {
            Intent intent = new Intent(getApplicationContext(), PlayerService.class);
            intent.setAction(PlayerService.ACTION_STOP);
            startService(intent);
            SharedPref time_end = new SharedPref(this);
            if (time_end.getIsSleepTimeOn()) {
                Intent intent1 = new Intent(MainActivity.this, TimeReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, sharedPref.getSleepID(), intent1, PendingIntent.FLAG_IMMUTABLE);
                AlarmManager alarmManager = (AlarmManager) MainActivity.this.getSystemService(Context.ALARM_SERVICE);
                pendingIntent.cancel();
                alarmManager.cancel(pendingIntent);
                time_end.setSleepTime(false, 0, 0);
            }
        }
        try {
            AudioRecorder.onStopRecord();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}