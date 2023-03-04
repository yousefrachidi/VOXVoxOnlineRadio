package nemosofts.vox.radio.ifSupported;

import android.app.Activity;
import android.view.View;

import nemosofts.vox.radio.callback.Callback;

public class IsRTL {

    public static void ifSupported(Activity mContext) {
        if (Callback.isRTL) {
            try {
                mContext.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
