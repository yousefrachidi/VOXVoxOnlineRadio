package nemosofts.vox.radio.interfaces;

import java.util.ArrayList;

import nemosofts.vox.radio.item.ItemPodcasts;

public interface PodcastsListener {
    void onStart();
    void onEnd(String success, String verifyStatus, String message, ArrayList<ItemPodcasts> arrayListPodcasts);
}