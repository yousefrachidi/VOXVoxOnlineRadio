package nemosofts.vox.radio.interfaces;

import java.util.ArrayList;

import nemosofts.vox.radio.item.ItemBanner;
import nemosofts.vox.radio.item.ItemRadio;

public interface HomeListener {
    void onStart();
    void onEnd(String success, ArrayList<ItemBanner> arrayListBanner, ArrayList<ItemRadio> arrayListLatest, ArrayList<ItemRadio> arrayLisMost);
}