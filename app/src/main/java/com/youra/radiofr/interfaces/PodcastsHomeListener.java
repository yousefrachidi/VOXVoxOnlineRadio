package com.youra.radiofr.interfaces;

import java.util.ArrayList;

import com.youra.radiofr.item.ItemPodcasts;
import com.youra.radiofr.item.ItemRadio;

public interface PodcastsHomeListener {
    void onStart();
    void onEnd(String success, ArrayList<ItemPodcasts> arrayListLatest, ArrayList<ItemRadio> arrayListRecent);
}