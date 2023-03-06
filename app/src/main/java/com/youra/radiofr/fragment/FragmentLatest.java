package com.youra.radiofr.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
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
import com.youra.radiofr.activity.BaseActivity;
import com.youra.radiofr.activity.PlayerService;
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

public class FragmentLatest extends Fragment {

    private Helper helper;
    private SharedPref sharedPref;
    private RecyclerView rv;
    private AdapterRadioList adapter;
    private ArrayList<ItemRadio> arrayList;
    private Boolean isOver = false, isScroll = false, isLoading = false;
    private int page = 1;
    private ProgressBar pb;
    private FloatingActionButton fab;
    private String error_msg;
    private FrameLayout frameLayout;
    private final String addedFrom = "latest_page";
    private SearchView searchView;

    AdLoader adLoader;
    ArrayList<NativeAd> arrayListNativeAds = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_latest, container, false);

        sharedPref = new SharedPref(getActivity());
        helper = new Helper(getActivity(), (position, type) -> {
            Callback.isRadio = true;
            if (!Callback.addedFrom.equals(addedFrom)) {
                Callback.arrayList_play.clear();
                Callback.arrayList_play.addAll(arrayList);
                Callback.addedFrom = addedFrom;
                Callback.isNewAdded = true;
            }
            Callback.playPos = position;

            Intent intent = new Intent(getActivity(), PlayerService.class);
            intent.setAction(PlayerService.ACTION_PLAY);
            getActivity().startService(intent);
        });

        arrayList = new ArrayList<>();

        frameLayout = rootView.findViewById(R.id.fl_empty);
        fab = rootView.findViewById(R.id.fab);
        pb = rootView.findViewById(R.id.pb);
        rv = rootView.findViewById(R.id.rv);
        rv.setHasFixedSize(true);

        LinearLayoutManager llm_banner = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm_banner);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setHasFixedSize(true);
        rv.setNestedScrollingEnabled(false);

        rv.addOnScrollListener(new EndlessRecyclerViewScrollListenerRadio(llm_banner) {
            @Override
            public void onLoadMore(int p, int totalItemsCount) {
                if (getActivity() != null) {
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

        getData();

        setHasOptionsMenu(true);
        return rootView;
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
                    if (getActivity() != null) {
                        if (success.equals("1")) {
                            if (!verifyStatus.equals("-1")) {
                                if (arrayListRadio.size() == 0) {
                                    isOver = true;
                                    error_msg = getString(R.string.error_no_data_found);
                                    setEmpty();
                                } else {
                                    if (page == Callback.recentLimit){
                                        isOver = true;
                                    }else {
                                        arrayList.addAll(arrayListRadio);
                                        page = page + 1;
                                        setAdapter();
                                    }
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
                }
            }, helper.callAPI(Callback.METHOD_LATEST, page, "", "", "", "", sharedPref.getUserId(), "", "", "", "", "", "", "", null));
            loadRadio.execute();
        } else {
            error_msg = getString(R.string.error_internet_not_connected);
            setEmpty();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setAdapter() {
        if(!isScroll) {
            adapter = new AdapterRadioList(getActivity(), arrayList, position -> {
                helper.showInterAd(position, "");

                    if (( (BaseActivity) requireActivity()).mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                        ( (BaseActivity) requireActivity()).mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                    }


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
            AdLoader.Builder builder = new AdLoader.Builder(getActivity(), Callback.nativeAdID);

            Bundle extras = new Bundle();
            if (ConsentInformation.getInstance(getActivity()).getConsentStatus() != ConsentStatus.PERSONALIZED) {
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

            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View myView = inflater.inflate(R.layout.layout_empty, null);

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(error_msg);

            myView.findViewById(R.id.btn_empty_try).setOnClickListener(v -> getData());

            frameLayout.addView(myView);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem search_item = menu.findItem(R.id.menu_search);
        MenuItemCompat.setShowAsAction(search_item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setOnQueryTextListener(queryTextListener);
        super.onCreateOptionsMenu(menu, inflater);
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
}