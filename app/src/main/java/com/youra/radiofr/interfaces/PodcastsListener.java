package com.youra.radiofr.interfaces;

import java.util.ArrayList;

import com.youra.radiofr.item.ItemPodcasts;

public interface PodcastsListener {
    void onStart();
    void onEnd(String success, String verifyStatus, String message, ArrayList<ItemPodcasts> arrayListPodcasts);
}