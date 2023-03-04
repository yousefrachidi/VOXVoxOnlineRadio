package nemosofts.vox.radio.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.nemosofts.lk.ProCompatActivity;

import com.squareup.picasso.Picasso;

import nemosofts.vox.radio.R;
import nemosofts.vox.radio.callback.Callback;
import nemosofts.vox.radio.ifSupported.IsRTL;
import nemosofts.vox.radio.ifSupported.IsScreenshot;

public class CustomAdsActivity extends YouraCompatActivity {

    private ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IsRTL.ifSupported(this);
        IsScreenshot.ifSupported(this);

        pb = findViewById(R.id.pb_ads);
        ImageView iv_ads = findViewById(R.id.iv_ads);

        Picasso.get()
                .load(Callback.custom_ads_img)
                .into(iv_ads, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        pb.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        pb.setVisibility(View.GONE);
                    }
                });

        iv_ads.setOnClickListener(view -> {
            String url = Callback.custom_ads_link;
            if (!url.startsWith("http://") && !url.startsWith("https://")){
                url = "http://" + url;
            }
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        });

        findViewById(R.id.iv_ads_close).setOnClickListener(view -> finish());
    }

    @Override
    protected int setLayoutResourceId() {
        return R.layout.activity_custom_ads;
    }

    @Override
    protected int setApplicationThemes() {
        return 1;
    }
}