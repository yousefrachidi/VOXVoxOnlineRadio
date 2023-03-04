package nemosofts.vox.radio.ifSupported;

import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;

import nemosofts.vox.radio.callback.Callback;

public class IsScreenshot {
    public static void ifSupported(Activity mContext) {
        if (Callback.isScreenshot) {
            try {
                Window window = mContext.getWindow();
                window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
