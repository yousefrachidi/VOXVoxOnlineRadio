package com.youra.radiofr.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import com.youra.radiofr.R;
import com.youra.radiofr.activity.PlayerService;
import com.youra.radiofr.activity.PodcastsIDActivity;
import com.youra.radiofr.adapter.AdapterEpisodeList;
import com.youra.radiofr.adapter.home.AdapterHomePodcasts;
import com.youra.radiofr.asyncTask.LoadHomePodcasts;
import com.youra.radiofr.callback.Callback;
import com.youra.radiofr.interfaces.PodcastsHomeListener;
import com.youra.radiofr.item.ItemCategory;
import com.youra.radiofr.item.ItemPodcasts;
import com.youra.radiofr.item.ItemRadio;
import com.youra.radiofr.utils.DBHelper;
import com.youra.radiofr.utils.GlobalBus;
import com.youra.radiofr.utils.Helper;
import com.youra.radiofr.utils.SharedPref;

public class FragmentPodcasts extends Fragment {

    private Helper helper;
    private SharedPref sharedPref;
    private DBHelper dbHelper;
    private LinearLayout ll_latest, ll_recent;
    private RecyclerView rv_latest, rv_recent;
    private ArrayList<ItemPodcasts> arrayList_podcasts;
    private ArrayList<ItemRadio> arrayList_episode;
    private ProgressBar pb;
    private String recentSongIDs = "";
    private AdapterEpisodeList episode;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_podcasts, container, false);

        sharedPref = new SharedPref(getActivity());
        helper = new Helper(getContext(), (position, type) -> {
            if (type.equals("podcasts")){
                Intent intent = new Intent(getActivity(), PodcastsIDActivity.class);
                intent.putExtra("id", arrayList_podcasts.get(position).getId());
                intent.putExtra("name", arrayList_podcasts.get(position).getTitle());
                intent.putExtra("image", arrayList_podcasts.get(position).getImage());
                intent.putExtra("imageThumb", arrayList_podcasts.get(position).getImageThumb());
                intent.putExtra("description", arrayList_podcasts.get(position).getDescription());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                Callback.isRadio = true;
                if (!Callback.addedFrom.equals("episode_home")) {
                    Callback.arrayList_play.clear();
                    Callback.arrayList_play.addAll(arrayList_episode);
                    Callback.addedFrom = "episode_home";
                    Callback.isNewAdded = true;
                }
                Callback.playPos = position;

                Intent playerIntent = new Intent(getActivity(), PlayerService.class);
                playerIntent.setAction(PlayerService.ACTION_PLAY);
                getActivity().startService(playerIntent);
            }
        });
        dbHelper = new DBHelper(getActivity());

        arrayList_podcasts = new ArrayList<>();
        arrayList_episode = new ArrayList<>();

        pb = rootView.findViewById(R.id.pb_home_podcasts);

        ll_latest = rootView.findViewById(R.id.ll_latest_podcasts);
        rv_latest = rootView.findViewById(R.id.rv_latest_podcasts);
        LinearLayoutManager llm_latest = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rv_latest.setLayoutManager(llm_latest);
        rv_latest.setItemAnimator(new DefaultItemAnimator());
        rv_latest.setHasFixedSize(true);

        ll_recent = rootView.findViewById(R.id.ll_recent_podcasts);
        rv_recent = rootView.findViewById(R.id.rv_recent_podcasts);
        rv_recent.setHasFixedSize(true);
        LinearLayoutManager llm_banner = new LinearLayoutManager(getActivity());
        rv_recent.setLayoutManager(llm_banner);
        rv_recent.setItemAnimator(new DefaultItemAnimator());
        rv_recent.setHasFixedSize(true);
        rv_recent.setNestedScrollingEnabled(false);

        rootView.findViewById(R.id.ll_see_podcasts).setOnClickListener(view -> {
//            Intent intent = new Intent(getActivity(), PodcastsActivity.class);
//            startActivity(intent);
        });

        recentSongIDs = dbHelper.getRecentEpisodeIDs("15");
        if (recentSongIDs.isEmpty()){
            recentSongIDs = "0";
        }
        loadHome();

        setHasOptionsMenu(true);
        return rootView;
    }

    private void loadHome() {
        if (helper.isNetworkAvailable()){
            LoadHomePodcasts loadHome = new LoadHomePodcasts(new PodcastsHomeListener() {
                @Override
                public void onStart() {
                    ll_latest.setVisibility(View.GONE);
                    ll_recent.setVisibility(View.GONE);
                    pb.setVisibility(View.VISIBLE);
                }

                @Override
                public void onEnd(String success, ArrayList<ItemPodcasts> arrayListLatest, ArrayList<ItemRadio> arrayListRecent) {
                    if (getActivity() != null) {
                        if (success.equals("1")) {
                            if (!arrayListLatest.isEmpty()){
                                arrayList_podcasts.addAll(arrayListLatest);

                            }
                            if (!arrayListRecent.isEmpty()){
                                arrayList_episode.addAll(arrayListRecent);
                            }
                            setAdapter();
                        }
                        pb.setVisibility(View.GONE);
                    }
                }
            }, helper.callAPI(Callback.METHOD_HOME_PODCASTS, 0,recentSongIDs,"","","", sharedPref.getUserId(),"","","","","","","",null));
            loadHome.execute();
        }else {
            pb.setVisibility(View.GONE);
        }
    }

    private void setAdapter() {
        if (!arrayList_episode.isEmpty()){
            episode = new AdapterEpisodeList(getActivity(), arrayList_episode, position -> {
                helper.showInterAd(position, "");
            });
            rv_recent.setAdapter(episode);
            ll_recent.setVisibility(View.VISIBLE);
        }else {
            ll_recent.setVisibility(View.GONE);
        }

        if (!arrayList_podcasts.isEmpty()){
            AdapterHomePodcasts adapterHomePodcasts = new AdapterHomePodcasts(arrayList_podcasts, (itemPodcasts, position) -> helper.showInterAd(position, "podcasts"));
            rv_latest.setAdapter(adapterHomePodcasts);
            ll_latest.setVisibility(View.VISIBLE);
        }else {
            ll_latest.setVisibility(View.GONE);
        }
        pb.setVisibility(View.GONE);

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEquilizerChange(ItemCategory itemCategory) {
        try {
            episode.notifyDataSetChanged();
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