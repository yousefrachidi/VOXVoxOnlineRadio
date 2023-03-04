package nemosofts.vox.radio.interfaces;

public interface DialogListener {
    void onShow();
    void onDismiss(String success, String message);
}