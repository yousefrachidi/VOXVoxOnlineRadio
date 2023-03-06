package com.youra.radiofr.activity;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
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
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import com.youra.radiofr.R;
import com.youra.radiofr.adapter.AdapterRadioList;
import com.youra.radiofr.asyncTask.LoadRadio;
import com.youra.radiofr.callback.Callback;
import com.youra.radiofr.interfaces.RadioListener;
import com.youra.radiofr.item.ItemCategory;
import com.youra.radiofr.item.ItemRadio;
import com.youra.radiofr.utils.EndlessRecyclerViewScrollListenerRadio;
import com.youra.radiofr.utils.GlobalBus;
import com.youra.radiofr.utils.Helper;
import com.youra.radiofr.utils.SharedPref;

public class SearchActivity extends BaseActivity {

    Toolbar toolbar_search;
    Helper helper;
    SharedPref sharedPref;
    RecyclerView rv;
    AdapterRadioList adapter;
    ArrayList<ItemRadio> arrayList;
    Boolean isOver = false, isScroll = false, isLoading = false;
    int page = 1;
    ProgressBar pb;
    FloatingActionButton fab;
    String error_msg;
    FrameLayout frameLayout;

    private int  nativeAdPos = 0;
    AdLoader adLoader;
    ArrayList<NativeAd> arrayListNativeAds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.fragment_search, contentFrameLayout);

        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        ll_bottom_nav.setVisibility(View.GONE);
        ll_adView_player.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.GONE);

        sharedPref = new SharedPref(this);
        helper = new Helper(this, (position, type) -> {
            Intent playerIntent = new Intent(this, PlayerService.class);
            playerIntent.setAction(PlayerService.ACTION_PLAY);
            startService(playerIntent);
        });

        toolbar_search = findViewById(R.id.toolbar);
        toolbar_search.setTitle(getString(R.string.search));
        setSupportActionBar(toolbar_search);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar_search.setNavigationOnClickListener(view -> onBackPressed());

        arrayList = new ArrayList<>();

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

        helper.showBannerAd(ll_adView_player);

        if(Callback.isNativeAd) {
            if(Callback.nativeAdShow%2 != 0) {
                nativeAdPos = Callback.nativeAdShow + 1;
            } else {
                nativeAdPos = Callback.nativeAdShow;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menu_search);
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setOnQueryTextListener(queryTextListener);
        return super.onCreateOptionsMenu(menu);
    }

    SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public boolean onQueryTextSubmit(String s) {
            if (helper.isNetworkAvailable()) {
                Callback.search_item = s.replace(" ", "%20");
                page = 1;
                isScroll = false;
                isOver = false;
                arrayList.clear();
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
                getData();
            } else {
                Toast.makeText(SearchActivity.this, getResources().getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
            }
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return true;
        }
    };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
    private void getData() {
        if (helper.isNetworkAvailable()) {
            LoadRadio loadRadio = new LoadRadio(new RadioListener() {
                @Override
                public void onStart() {
                    if (arrayList.size() == 0) {
                        frameLayout.setVisibility(View.GONE);
                        rv.setVisibility(View.GONE);
                        pb.setVisibility(View.VISIBLE);
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
            }, helper.callAPI(Callback.METHOD_SEARCH, page, "", "", Callback.search_item, "", sharedPref.getUserId(), "", "", "", "", "", "", "", null));
            loadRadio.execute();
        } else {
            error_msg = getString(R.string.error_internet_not_connected);
            setEmpty();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setAdapter() {
        if(!isScroll) {
            adapter = new AdapterRadioList(this, arrayList, position -> {
                Callback.isRadio = true;
                Callback.arrayList_play.clear();
                Callback.arrayList_play.addAll(arrayList);
                Callback.addedFrom = "latest_page";
                Callback.isNewAdded = true;
                Callback.playPos = position;

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
                && !Callback.adNetwork.equals(Callback.AD_TYPE_STARTAPP) && !Callback.adNetwork.equals(Callback.AD_TYPE_IRONSOURCE) && arrayList.size() >= 10 && !Callback.isPurchases) {
            AdLoader.Builder builder = new AdLoader.Builder(SearchActivity.this, Callback.nativeAdID);

            Bundle extras = new Bundle();
            if (ConsentInformation.getInstance(SearchActivity.this).getConsentStatus() != ConsentStatus.PERSONALIZED) {
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
            frameLayout.setVisibility(View.GONE);
        } else {
            pb.setVisibility(View.INVISIBLE);
            rv.setVisibility(View.GONE);
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