package nemosofts.vox.radio.interfaces;

import java.util.ArrayList;

import nemosofts.vox.radio.item.ItemRadio;

public interface RadioListener {
    void onStart();
    void onEnd(String success, String verifyStatus, String message, ArrayList<ItemRadio> arrayListRadio);
}