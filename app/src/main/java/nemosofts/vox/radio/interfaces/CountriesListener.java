package nemosofts.vox.radio.interfaces;

import java.util.ArrayList;

import nemosofts.vox.radio.item.ItemCountries;

public interface CountriesListener {
    void onStart();
    void onEnd(String success, String verifyStatus, String message, ArrayList<ItemCountries> arrayListCountries);
}