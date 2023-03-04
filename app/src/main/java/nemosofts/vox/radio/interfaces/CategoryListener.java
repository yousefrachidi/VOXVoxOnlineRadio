package nemosofts.vox.radio.interfaces;

import java.util.ArrayList;

import nemosofts.vox.radio.item.ItemCategory;

public interface CategoryListener {
    void onStart();
    void onEnd(String success, String verifyStatus, String message, ArrayList<ItemCategory> arrayListCat);
}