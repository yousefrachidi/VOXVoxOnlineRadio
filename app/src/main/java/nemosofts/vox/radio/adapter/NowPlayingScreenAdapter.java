package nemosofts.vox.radio.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.squareup.picasso.Picasso;

import nemosofts.vox.radio.R;
import nemosofts.vox.radio.utils.NowPlayingScreen;

public class NowPlayingScreenAdapter extends PagerAdapter {

    private final Context ctx;

    public NowPlayingScreenAdapter(Context context) {
        this.ctx = context;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup collection, int position) {
        NowPlayingScreen nowPlayingScreen = NowPlayingScreen.values()[position];

        LayoutInflater inflater = LayoutInflater.from(ctx);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.row_now_playing_screen, collection, false);
        collection.addView(layout);

        ImageView image = layout.findViewById(R.id.image);
        TextView title = layout.findViewById(R.id.title);
        Picasso.get()
                .load(nowPlayingScreen.drawableResId)
                .into(image);

        title.setText(nowPlayingScreen.titleRes);

        return layout;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup collection, int position, @NonNull Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return NowPlayingScreen.values().length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return ctx.getString(NowPlayingScreen.values()[position].titleRes);
    }
}
