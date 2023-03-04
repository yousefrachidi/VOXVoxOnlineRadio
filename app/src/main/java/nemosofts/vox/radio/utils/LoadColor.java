package nemosofts.vox.radio.utils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;

import androidx.palette.graphics.Palette;

public class LoadColor extends AsyncTask<String, String, String> {

    Bitmap bitmap;
    @SuppressLint("StaticFieldLeak")
    View view;

    public LoadColor(View view) {
        this.view = view;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            bitmap = ApplicationUtil.getBitmapFromURL(strings[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        try {
            Palette.from(bitmap).generate(palette -> {
                if (palette != null){
                    int defaultValue = 0x000000;
                    int vibrant = palette.getVibrantColor(defaultValue);
                    try{
                        view.setBackground(ApplicationUtil.getGradientDrawable(vibrant, Color.parseColor("#00000000")));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            super.onPostExecute(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
