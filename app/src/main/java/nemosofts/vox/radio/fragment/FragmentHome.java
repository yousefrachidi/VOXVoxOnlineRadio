package nemosofts.vox.radio.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.tiagosantos.enchantedviewpager.EnchantedViewPager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import nemosofts.vox.radio.R;
import nemosofts.vox.radio.activity.BaseActivity;
import nemosofts.vox.radio.activity.MainActivity;
import nemosofts.vox.radio.activity.PlayerService;
import nemosofts.vox.radio.adapter.home.AdapterHome;
import nemosofts.vox.radio.adapter.HomePagerAdapter;
import nemosofts.vox.radio.asyncTask.LoadHome;
import nemosofts.vox.radio.callback.Callback;
import nemosofts.vox.radio.interfaces.HomeListener;
import nemosofts.vox.radio.item.ItemBanner;
import nemosofts.vox.radio.item.ItemCategory;
import nemosofts.vox.radio.item.ItemRadio;
import nemosofts.vox.radio.utils.DBHelper;
import nemosofts.vox.radio.utils.GlobalBus;
import nemosofts.vox.radio.utils.Helper;
import nemosofts.vox.radio.utils.SharedPref;

public class FragmentHome extends Fragment {

    private Helper helper;
    private DBHelper dbHelper;
    private SharedPref sharedPref;
    private AdapterHome adapterMost, adapterLatest, adapterRecent;
    private NestedScrollView nsv_home;
    private ProgressBar pb_home;
    private LinearLayout ll_banner;
    private EnchantedViewPager enchantedViewPager;
    private LinearLayout ll_most, ll_latest, ll_recent;
    private RecyclerView rv_most, rv_latest, rv_recent;
    private ArrayList<ItemRadio> arrayList_recently;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        helper = new Helper(getContext(), (position, type) -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), PlayerService.class);
                intent.setAction(PlayerService.ACTION_PLAY);
                getActivity().startService(intent);
            }
        });

        dbHelper = new DBHelper(getActivity());
        sharedPref = new SharedPref(getActivity());

        arrayList_recently = new ArrayList<>();

        nsv_home = rootView.findViewById(R.id.nsv_home);
        pb_home = rootView.findViewById(R.id.pb_home);

        ll_banner = rootView.findViewById(R.id.ll_banner);
        enchantedViewPager = rootView.findViewById(R.id.viewPager_home);
        enchantedViewPager.useAlpha();
        if (sharedPref.isScaleBanner()){
            enchantedViewPager.useScale();
        } else {
            enchantedViewPager.removeScale();
        }

        ll_most = rootView.findViewById(R.id.ll_most);
        rv_most = rootView.findViewById(R.id.rv_most);
        LinearLayoutManager llm_latest_songs = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rv_most.setLayoutManager(llm_latest_songs);
        rv_most.setItemAnimator(new DefaultItemAnimator());
        rv_most.setHasFixedSize(true);

        ll_latest = rootView.findViewById(R.id.ll_latest);
        rv_latest = rootView.findViewById(R.id.rv_latest);
        LinearLayoutManager llm_latest = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rv_latest.setLayoutManager(llm_latest);
        rv_latest.setItemAnimator(new DefaultItemAnimator());
        rv_latest.setHasFixedSize(true);

        ll_recent = rootView.findViewById(R.id.ll_recent);
        rv_recent = rootView.findViewById(R.id.rv_recent);
        LinearLayoutManager llm_recent = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rv_recent.setLayoutManager(llm_recent);
        rv_recent.setItemAnimator(new DefaultItemAnimator());
        rv_recent.setHasFixedSize(true);

        rootView.findViewById(R.id.ll_see_latest).setOnClickListener(v -> {
            FragmentLatest f1 = new FragmentLatest();
            loadFragHome(f1, getResources().getString(R.string.latest), 1);
        });

        rootView.findViewById(R.id.ll_see_most).setOnClickListener(v -> {
            FragmentMost f1 = new FragmentMost();
            loadFragHome(f1, getResources().getString(R.string.most), 2);
        });

        rootView.findViewById(R.id.ll_see_recent).setOnClickListener(v -> {
            FragmentRecent f1 = new FragmentRecent();
            loadFragHome(f1, getResources().getString(R.string.recent), 4);
        });

        if (helper.isNetworkAvailable() && !Callback.arrayList_banner.isEmpty() && !Callback.arrayList_latest.isEmpty() && !Callback.arrayList_most.isEmpty()){
            setAdapter();
            nsv_home.setVisibility(View.VISIBLE);
            pb_home.setVisibility(View.GONE);
        } else {
            loadHome();
        }

        LinearLayout adView = rootView.findViewById(R.id.ll_adView);
        helper.showBannerAd(adView);

        setHasOptionsMenu(true);
        return rootView;
    }

    public void loadFragHome(Fragment f1, String name, int pos) {
        if (getFragmentManager() != null){
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.hide(getFragmentManager().getFragments().get(getFragmentManager().getBackStackEntryCount()));
            ft.add(R.id.content_frame, f1, name);
            ft.addToBackStack(name);
            ft.commit();
            if (getActivity() != null){
                ((MainActivity) getActivity()).getSupportActionBar().setTitle(name);
                ((MainActivity) getActivity()).bottomNavigationView(pos);
            }
        }
    }

    private void loadHome() {
        if (helper.isNetworkAvailable()){
            LoadHome loadHome = new LoadHome(new HomeListener() {
                @Override
                public void onStart() {
                    if (!Callback.arrayList_latest.isEmpty()){
                        Callback.arrayList_latest.clear();
                    }
                    if (!Callback.arrayList_most.isEmpty()){
                        Callback.arrayList_most.clear();
                    }
                    if (!Callback.arrayList_banner.isEmpty()){
                        Callback.arrayList_banner.clear();
                    }
                    nsv_home.setVisibility(View.GONE);
                    pb_home.setVisibility(View.VISIBLE);
                }

                @Override
                public void onEnd(String success, ArrayList<ItemBanner> arrayListBanner, ArrayList<ItemRadio> arrayListLatest, ArrayList<ItemRadio> arrayLisMost) {
                    if (getActivity() != null) {
                        pb_home.setVisibility(View.GONE);
                        if (success.equals("1")) {
                            if (!arrayListLatest.isEmpty()){
                                Callback.arrayList_latest.addAll(arrayListLatest);
                            }
                            if (!arrayLisMost.isEmpty()){
                                Callback.arrayList_most.addAll(arrayLisMost);
                            }
                            if (!arrayListBanner.isEmpty()){
                                Callback.arrayList_banner.addAll(arrayListBanner);
                            }
                            setAdapter();
                            nsv_home.setVisibility(View.VISIBLE);
                        } else {
                            nsv_home.setVisibility(View.GONE);
                        }
                    }
                }
            }, helper.callAPI(Callback.METHOD_HOME, 0,"","","","", sharedPref.getUserId(),"","","","","","","",null));
            loadHome.execute();
        } else {
            nsv_home.setVisibility(View.GONE);
            pb_home.setVisibility(View.VISIBLE);
        }
    }

    private void setAdapter() {
        if (!Callback.arrayList_banner.isEmpty()){
            HomePagerAdapter homePagerAdapter = new HomePagerAdapter(getActivity(), Callback.arrayList_banner);
            enchantedViewPager.setAdapter(homePagerAdapter);
            if (homePagerAdapter.getCount() > 2) {
                enchantedViewPager.setCurrentItem(1);
            }
            ll_banner.setVisibility(View.VISIBLE);
        } else {
            ll_banner.setVisibility(View.GONE);
        }

        if (!Callback.arrayList_latest.isEmpty()){
            adapterLatest = new AdapterHome(getContext(), Callback.arrayList_latest, false, (itemSong, position) -> {
                Callback.isRadio = true;
                if (!Callback.addedFrom.equals("home_latest")) {
                    Callback.arrayList_play.clear();
                    Callback.arrayList_play.addAll(Callback.arrayList_latest);
                    Callback.addedFrom = "home_latest";
                    Callback.isNewAdded = true;
                }
                Callback.playPos = position;

                helper.showInterAd(position,"latest");
                open();

            });
            rv_latest.setAdapter(adapterLatest);
            ll_latest.setVisibility(View.VISIBLE);
        } else {
            ll_latest.setVisibility(View.GONE);
        }

        if (!Callback.arrayList_most.isEmpty()){
            adapterMost = new AdapterHome(getContext(), Callback.arrayList_most , false, (itemSong, position) -> {
                Callback.isRadio = true;
                if (!Callback.addedFrom.equals("home_most")) {
                    Callback.arrayList_play.clear();
                    Callback.arrayList_play.addAll(Callback.arrayList_most);
                    Callback.addedFrom = "home_most";
                    Callback.isNewAdded = true;
                }
                Callback.playPos = position;

                helper.showInterAd(position,"most");
                open();
            });
            rv_most.setAdapter(adapterMost);
            ll_most.setVisibility(View.VISIBLE);
        } else {
            ll_most.setVisibility(View.GONE);
        }

        try {
            new LoadRecentlyRadio().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void open(){
        if (( (BaseActivity) requireActivity()).mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            ( (BaseActivity) requireActivity()).mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadRecentlyRadio extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                arrayList_recently.addAll(dbHelper.loadDataRecent("10"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (!arrayList_recently.isEmpty()){
                adapterRecent = new AdapterHome(getContext(), arrayList_recently, true, (itemSong, position) -> {
                    Callback.isRadio = true;
                    if (!Callback.addedFrom.equals("home_recent")) {
                        Callback.arrayList_play.clear();
                        Callback.arrayList_play.addAll(arrayList_recently);
                        Callback.addedFrom = "home_recent";
                        Callback.isNewAdded = true;
                    }
                    Callback.playPos = position;
                    helper.showInterAd(position,"recent");
                });
                rv_recent.setAdapter(adapterRecent);
                ll_recent.setVisibility(View.VISIBLE);
            } else {
                ll_recent.setVisibility(View.GONE);
            }
            if (arrayList_recently.isEmpty()){
                if (Callback.arrayList_play.size() == 0 && Callback.arrayList_latest.size() > 0) {
                    Callback.arrayList_play.addAll(Callback.arrayList_latest);
                    Callback.playPos = 0;
                    try {
                        GlobalBus.getBus().postSticky(Callback.arrayList_play.get(0));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }else {
                if (Callback.arrayList_play.size() == 0) {
                    Callback.arrayList_play.addAll(arrayList_recently);
                    Callback.playPos = 0;
                    try {
                        GlobalBus.getBus().postSticky(Callback.arrayList_play.get(0));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_home, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEquilizerChange(ItemCategory itemCategory) {
        try {
            adapterLatest.notifyDataSetChanged();
            adapterMost.notifyDataSetChanged();
            adapterRecent.notifyDataSetChanged();
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

    @Override
    public void onDestroy() {
        try {
            dbHelper.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}