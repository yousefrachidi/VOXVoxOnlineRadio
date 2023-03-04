package nemosofts.vox.radio.utils;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import nemosofts.vox.radio.R;

public enum NowPlayingScreen {

    NORMAL(R.string.player_normal, R.drawable.np_1, 0),
    CIRCLE(R.string.player_circle, R.drawable.np_2, 1),
    FLAT(R.string.player_flat, R.drawable.np_3, 2),
    BLUR(R.string.player_blur, R.drawable.np_4, 3),
    GRADIENT(R.string.player_gradient, R.drawable.np_5, 4);

    @StringRes
    public final int titleRes;
    @DrawableRes
    public final int drawableResId;
    public final int id;

    NowPlayingScreen(@StringRes int titleRes, @DrawableRes int drawableResId, int id) {
        this.titleRes = titleRes;
        this.drawableResId = drawableResId;
        this.id = id;
    }
}
