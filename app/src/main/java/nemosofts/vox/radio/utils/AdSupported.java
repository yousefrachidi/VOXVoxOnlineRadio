package nemosofts.vox.radio.utils;

import nemosofts.vox.radio.callback.Callback;

public class AdSupported {

    public static boolean isAdmobFBAds() {
        return Callback.adNetwork.equals(Callback.AD_TYPE_ADMOB) || Callback.adNetwork.equals(Callback.AD_TYPE_FACEBOOK);
    }

    public static boolean isStartAppAds() {
        return Callback.adNetwork.equals(Callback.AD_TYPE_STARTAPP);
    }

    public static boolean isApplovinAds() {
        return Callback.adNetwork.equals(Callback.AD_TYPE_APPLOVIN);
    }

    public static boolean isIronSourceAds() {
        return Callback.adNetwork.equals(Callback.AD_TYPE_IRONSOURCE);
    }

    public static boolean isWortiseAds() {
        return Callback.adNetwork.equals(Callback.AD_TYPE_WORTISE);
    }

}
