package nemosofts.vox.radio.utils;

import android.content.Context;

import com.startapp.sdk.adsbase.StartAppAd;

public class AdManagerInterStartApp {
    static StartAppAd startAppAd;
    private final Context ctx;

    public AdManagerInterStartApp(Context ctx) {
        this.ctx = ctx;
    }

    public void createAd() {
        startAppAd = new StartAppAd(ctx);
        startAppAd.loadAd();
    }

    public StartAppAd getAd() {
        return startAppAd;
    }

    public static void setAd(StartAppAd startAppAdInter) {
        startAppAd = startAppAdInter;
    }
}