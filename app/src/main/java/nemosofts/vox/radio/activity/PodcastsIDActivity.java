package nemosofts.vox.radio.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.ads.mediation.facebook.FacebookMediationAdapter;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jetradarmobile.snowfall.SnowfallView;
import com.makeramen.roundedimageview.RoundedImageView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;


import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import nemosofts.vox.radio.BuildConfig;
import nemosofts.vox.radio.R;
import nemosofts.vox.radio.adapter.AdapterEpisodeList;
import nemosofts.vox.radio.asyncTask.LoadEpisode;
import nemosofts.vox.radio.callback.Callback;
import nemosofts.vox.radio.interfaces.RadioListener;
import nemosofts.vox.radio.item.ItemCategory;
import nemosofts.vox.radio.item.ItemRadio;
import nemosofts.vox.radio.utils.ApplicationUtil;
import nemosofts.vox.radio.utils.EndlessRecyclerViewScrollListenerRadio;
import nemosofts.vox.radio.utils.GlobalBus;
import nemosofts.vox.radio.utils.Helper;

public class PodcastsIDActivity extends BaseActivity {

    Helper helper;
    RecyclerView rv;
    AdapterEpisodeList adapter;
    ArrayList<ItemRadio> arrayList;
    Boolean isOver = false, isScroll = false, isLoading = false;
    int page = 1;
    ProgressBar pb;
    FloatingActionButton fab;
    String error_msg;
    FrameLayout frameLayout;
    String id = "", name = "", image = "", imageThumb = "";
    String addedFrom = "";
    SearchView searchView;

    RoundedImageView iv_episode;
    View vi_row_play_all;

    AdLoader adLoader;
    ArrayList<NativeAd> arrayListNativeAds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.fragment_podcasts_by, contentFrameLayout);

        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        ll_bottom_nav.setVisibility(View.GONE);
        ll_adView_player.setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        name = intent.getStringExtra("name");
        image = intent.getStringExtra("image");
        imageThumb = intent.getStringExtra("imageThumb");

        addedFrom = "podcasts" + name;

        helper = new Helper(this, (position, type) -> {

            Callback.isRadio = false;
            if (!Callback.addedFrom.equals(addedFrom)) {
                Callback.arrayList_play.clear();
                Callback.arrayList_play.addAll(arrayList);
                Callback.addedFrom = addedFrom;
                Callback.isNewAdded = true;
            }
            Callback.playPos = position;

            Intent playerIntent = new Intent(this, PlayerService.class);
            playerIntent.setAction(PlayerService.ACTION_PLAY);
            startService(playerIntent);
        });

        toolbar.setTitle(name);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if (ApplicationUtil.isDarkMode()) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backspace_white);
        } else {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backspace_black);
        }
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        arrayList = new ArrayList<>();

        vi_row_play_all = findViewById(R.id.vi_row_play_all);
        SnowfallView snowfallView = findViewById(R.id.view_snow);
        try {
            snowfallView.restartFalling();
            snowfallView.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        iv_episode = findViewById(R.id.iv_episode);

        if(image.equals("")) {
            image = "null";
        }
        Picasso.get()
                .load(image)
                .placeholder(R.drawable.material_design_default)
                .into(iv_episode);

        frameLayout = findViewById(R.id.fl_empty);
        fab = findViewById(R.id.fab);
        pb = findViewById(R.id.pb);
        rv = findViewById(R.id.rv);
        rv.setHasFixedSize(true);

        LinearLayoutManager llm_banner = new LinearLayoutManager(this);
        rv.setLayoutManager(llm_banner);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setHasFixedSize(true);
        rv.setNestedScrollingEnabled(false);

        rv.addOnScrollListener(new EndlessRecyclerViewScrollListenerRadio(llm_banner) {
            @Override
            public void onLoadMore(int p, int totalItemsCount) {
                if (!isOver) {
                    if (!isLoading) {
                        isLoading = true;
                        new Handler().postDelayed(() -> {
                            isScroll = true;
                            getData();
                        }, 0);
                    }
                }
            }
        });

        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisibleItem = llm_banner.findFirstVisibleItemPosition();
                if (firstVisibleItem > 6) {
                    fab.show();
                } else {
                    fab.hide();
                }
            }
        });

        fab.setOnClickListener(v -> rv.smoothScrollToPosition(0));

        findViewById(R.id.rl_play_all).setOnClickListener(view -> {
            Callback.isRadio = false;
            if (!Callback.addedFrom.equals(addedFrom)) {
                Callback.arrayList_play.clear();
                Callback.arrayList_play.addAll(arrayList);
                Callback.addedFrom = addedFrom;
                Callback.isNewAdded = true;
            }
            Callback.playPos = 0;
            helper.showInterAd(0, "");
        });

        findViewById(R.id.rl_lyrics).setOnClickListener(view -> {
            Intent intent2 = new Intent(PodcastsIDActivity.this, WebActivity.class);
            intent2.putExtra("web_url", BuildConfig.BASE_URL+"radio_description.php?pod_id="+id);
            intent2.putExtra("page_title", name);
            ActivityCompat.startActivity(PodcastsIDActivity.this, intent2, null);
        });

        findViewById(R.id.rl_share).setOnClickListener(view -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    name+ "\n\n" +
                            "https://play.google.com/store/apps/details?id=" + getPackageName());
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        });

        getData();
        helper.showBannerAd(ll_adView_player);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem search_item = menu.findItem(R.id.menu_search);
        MenuItemCompat.setShowAsAction(search_item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setOnQueryTextListener(queryTextListener);
        return super.onCreateOptionsMenu(menu);
    }

    SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            return false;
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public boolean onQueryTextChange(String s) {
            if (adapter != null) {
                if (!searchView.isIconified()) {
                    adapter.getFilter().filter(s);
                    adapter.notifyDataSetChanged();
                }
            }
            return true;
        }
    };

    private void getData() {
        if (helper.isNetworkAvailable()) {
            LoadEpisode loadEpisode = new LoadEpisode(new RadioListener() {
                @Override
                public void onStart() {
                    if (arrayList.size() == 0) {
                        frameLayout.setVisibility(View.GONE);
                        rv.setVisibility(View.GONE);
                        pb.setVisibility(View.VISIBLE);
                        vi_row_play_all.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onEnd(String success, String verifyStatus, String message, ArrayList<ItemRadio> arrayListRadio) {
                    if (success.equals("1")) {
                        if (!verifyStatus.equals("-1")) {
                            if (arrayListRadio.size() == 0) {
                                isOver = true;
                                error_msg = getString(R.string.error_no_data_found);
                                setEmpty();
                            } else {
                                arrayList.addAll(arrayListRadio);
                                page = page + 1;
                                setAdapter();
                            }
                        } else {
                            helper.getVerifyDialog(getString(R.string.error_unauthorized_access), message);
                        }
                    } else {
                        error_msg = getString(R.string.error_server_not_connected);
                        setEmpty();
                    }
                    isLoading = false;
                }
            }, helper.callAPI(Callback.METHOD_PODCASTS_ID, page, id, "", "", "", "", "", "", "", "", "", "", "", null));
            loadEpisode.execute();
        } else {
            error_msg = getString(R.string.error_internet_not_connected);
            setEmpty();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setAdapter() {
        if(!isScroll) {
            adapter = new AdapterEpisodeList(this, arrayList, position -> {
                helper.showInterAd(position, "");
            });
            rv.setAdapter(adapter);
            rv.scheduleLayoutAnimation();
            setEmpty();
            loadNativeAds();
        } else {
            adapter.notifyDataSetChanged();
        }
    }
    private void loadNativeAds() {
        if (Callback.isNativeAd && !Callback.adNetwork.equals(Callback.AD_TYPE_WORTISE)
                && !Callback.adNetwork.equals(Callback.AD_TYPE_STARTAPP) && !Callback.adNetwork.equals(Callback.AD_TYPE_IRONSOURCE) && arrayList.size() >= 10) {
            AdLoader.Builder builder = new AdLoader.Builder(PodcastsIDActivity.this, Callback.nativeAdID);

            Bundle extras = new Bundle();
            if (ConsentInformation.getInstance(PodcastsIDActivity.this).getConsentStatus() != ConsentStatus.PERSONALIZED) {
                extras.putString("npa", "1");
            }

            AdRequest adRequest;
            if(Callback.adNetwork.equals(Callback.AD_TYPE_ADMOB)) {
                adRequest = new AdRequest.Builder()
                        .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                        .build();
            } else {
                adRequest = new AdRequest.Builder()
                        .addNetworkExtrasBundle(AdMobAdapter.class, new Bundle())
                        .addNetworkExtrasBundle(FacebookMediationAdapter.class, extras)
                        .build();
            }

            adLoader = builder.forNativeAd(nativeAd -> {
                try {
                    if(adLoader.isLoading()) {
                        arrayListNativeAds.add(nativeAd);
                    } else {
                        arrayListNativeAds.add(nativeAd);
                        adapter.addAds(arrayListNativeAds);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).build();
            adLoader.loadAds(adRequest, 5);
        }
    }

    @SuppressLint("InflateParams")
    private void setEmpty() {
        if (arrayList.size() > 0) {
            rv.setVisibility(View.VISIBLE);
            pb.setVisibility(View.INVISIBLE);
            vi_row_play_all.setVisibility(View.VISIBLE);
            frameLayout.setVisibility(View.GONE);
        } else {
            pb.setVisibility(View.INVISIBLE);
            rv.setVisibility(View.GONE);
            vi_row_play_all.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);

            frameLayout.removeAllViews();

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View myView = inflater.inflate(R.layout.layout_empty, null);

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(error_msg);

            myView.findViewById(R.id.btn_empty_try).setOnClickListener(v -> getData());

            frameLayout.addView(myView);
        }
    }

    @Override
    public void onBackPressed() {
        if (mLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEquilizerChange(ItemCategory itemCategory) {
        try {
            if (!arrayList.isEmpty() && adapter != null){
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        GlobalBus.getBus().removeStickyEvent(itemCategory);
    }
}