package nemosofts.vox.radio.interfaces;

import java.util.ArrayList;

import nemosofts.vox.radio.item.ItemPodcasts;
import nemosofts.vox.radio.item.ItemRadio;

public interface PodcastsHomeListener {
    void onStart();
    void onEnd(String success, ArrayList<ItemPodcasts> arrayListLatest, ArrayList<ItemRadio> arrayListRecent);
}