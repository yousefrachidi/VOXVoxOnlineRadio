package com.youra.radiofr.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.squareup.picasso.Picasso;
import com.startapp.sdk.ads.nativead.NativeAdPreferences;
import com.startapp.sdk.ads.nativead.StartAppNativeAd;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.wortise.ads.natives.GoogleNativeAd;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.youra.radiofr.R;
import com.youra.radiofr.callback.Callback;
import com.youra.radiofr.item.ItemCategory;
import com.youra.radiofr.utils.ApplicationUtil;

public class AdapterCategory extends RecyclerView.Adapter {

    private final ArrayList<ItemCategory> arrayList;
    private final Context context;
    private final RecyclerItemClickListener listener;
    private final int VIEW_PROG = -1;

    Boolean isAdLoaded = false;
    List<NativeAd> mNativeAdsAdmob = new ArrayList<>();

    private static class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView cat_text, tv_total_post;
        public ImageView cat_image;
        public RelativeLayout layout_cat;

        private MyViewHolder(View view) {
            super(view);
            cat_text = itemView.findViewById(R.id.cat_text);
            tv_total_post = itemView.findViewById(R.id.tv_total_post);
            cat_image = itemView.findViewById(R.id.cat_image);
            layout_cat = itemView.findViewById(R.id.layout_cat);
        }
    }

    private static class ProgressViewHolder extends RecyclerView.ViewHolder {
        @SuppressLint("StaticFieldLeak")
        private static ProgressBar progressBar;

        private ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }
    }

    private static class ADViewHolder extends RecyclerView.ViewHolder {
        private final RelativeLayout rl_native_ad;
        boolean isAdRequested = false;

        private ADViewHolder(View view) {
            super(view);
            rl_native_ad = view.findViewById(R.id.rl_native_ad);
        }
    }

    public AdapterCategory(Context context, ArrayList<ItemCategory> arrayList, RecyclerItemClickListener listener) {
        this.arrayList = arrayList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_PROG) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_progressbar, parent, false);
            return new ProgressViewHolder(v);
        } else if (viewType >= 1000) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_ads, parent, false);
            return new ADViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_cat, parent, false);
            return new MyViewHolder(itemView);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {
            final ItemCategory item = arrayList.get(position);

            ((MyViewHolder) holder).cat_text.setText(item.getTitle());
            ((MyViewHolder) holder).tv_total_post.setText("Radios "+ ApplicationUtil.viewFormat(Integer.valueOf(arrayList.get(position).getTotalPost())));

            //load cover image using picasso
            Picasso.get()
                    .load(arrayList.get(position).getImage())
                    .placeholder(R.drawable.material_design_default)
                    .into(((MyViewHolder) holder).cat_image);

            ((MyViewHolder) holder).layout_cat.setOnClickListener(v -> listener.onClickListener(item, holder.getAdapterPosition()));

        } else if (holder instanceof ADViewHolder) {
            if (((ADViewHolder) holder).rl_native_ad.getChildCount() == 0) {
                switch (Callback.adNetwork) {
                    case Callback.AD_TYPE_ADMOB:
                    case Callback.AD_TYPE_FACEBOOK:
                        if (isAdLoaded) {
                            if (mNativeAdsAdmob.size() >= 5) {

                                int i = new Random().nextInt(mNativeAdsAdmob.size() - 1);

                                NativeAdView adView = (NativeAdView) ((Activity) context).getLayoutInflater().inflate(R.layout.layout_native_ad_admob, null);
                                populateUnifiedNativeAdView(mNativeAdsAdmob.get(i), adView);
                                ((ADViewHolder) holder).rl_native_ad.removeAllViews();
                                ((ADViewHolder) holder).rl_native_ad.addView(adView);

                                ((ADViewHolder) holder).rl_native_ad.setVisibility(View.VISIBLE);
                            }
                        }
                        break;
                    case Callback.AD_TYPE_STARTAPP:
                        if (!((ADViewHolder) holder).isAdRequested) {
                            StartAppNativeAd nativeAd = new StartAppNativeAd(context);

                            nativeAd.loadAd(new NativeAdPreferences()
                                    .setAdsNumber(1)
                                    .setAutoBitmapDownload(true)
                                    .setPrimaryImageSize(2), new AdEventListener() {
                                @Override
                                public void onReceiveAd(com.startapp.sdk.adsbase.Ad ad) {
                                    try {
                                        if (!nativeAd.getNativeAds().isEmpty()) {
                                            RelativeLayout nativeAdView = (RelativeLayout) ((Activity) context).getLayoutInflater().inflate(R.layout.layout_native_ad_startapp, null);

                                            ImageView icon = nativeAdView.findViewById(R.id.icon);
                                            TextView title = nativeAdView.findViewById(R.id.title);
                                            TextView description = nativeAdView.findViewById(R.id.description);
                                            Button button = nativeAdView.findViewById(R.id.button);

                                            Picasso.get()
                                                    .load(nativeAd.getNativeAds().get(0).getImageUrl())
                                                    .into(icon);
                                            title.setText(nativeAd.getNativeAds().get(0).getTitle());
                                            description.setText(nativeAd.getNativeAds().get(0).getDescription());
                                            button.setText(nativeAd.getNativeAds().get(0).isApp() ? "Install" : "Open");

                                            ((ADViewHolder) holder).rl_native_ad.removeAllViews();
                                            ((ADViewHolder) holder).rl_native_ad.addView(nativeAdView);
                                            ((ADViewHolder) holder).rl_native_ad.setVisibility(View.VISIBLE);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailedToReceiveAd(Ad ad) {
                                    ((ADViewHolder) holder).isAdRequested = false;
                                }
                            });
                            ((ADViewHolder) holder).isAdRequested = true;
                        }
                        break;
                    case Callback.AD_TYPE_WORTISE:
                        if (!((ADViewHolder) holder).isAdRequested) {
                            GoogleNativeAd googleNativeAd = new GoogleNativeAd(
                                    context, Callback.nativeAdID, new GoogleNativeAd.Listener() {
                                @Override
                                public void onNativeClicked(@NonNull GoogleNativeAd googleNativeAd) {

                                }

                                @Override
                                public void onNativeFailed(@NonNull GoogleNativeAd googleNativeAd, @NonNull com.wortise.ads.AdError adError) {

                                }

                                @Override
                                public void onNativeImpression(@NonNull GoogleNativeAd googleNativeAd) {

                                }

                                @Override
                                public void onNativeLoaded(@NonNull GoogleNativeAd googleNativeAd, @NonNull NativeAd nativeAd) {
                                    NativeAdView adView = (NativeAdView) ((Activity) context).getLayoutInflater().inflate(R.layout.layout_native_ad_admob, null);
                                    populateUnifiedNativeAdView(nativeAd, adView);
                                    ((ADViewHolder) holder).rl_native_ad.removeAllViews();
                                    ((ADViewHolder) holder).rl_native_ad.addView(adView);

                                    ((ADViewHolder) holder).rl_native_ad.setVisibility(View.VISIBLE);
                                }
                            });
                            googleNativeAd.load();
                            ((ADViewHolder) holder).isAdRequested = true;
                        }
                        break;case Callback.AD_TYPE_IRONSOURCE:
                        if (!((ADViewHolder) holder).isAdRequested) {
                            IronSource.init((Activity) context, Callback.ironAdsId, IronSource.AD_UNIT.BANNER);
                            IronSourceBannerLayout banner = IronSource.createBanner((Activity) context, ISBannerSize.BANNER);
                            banner.setBannerListener(new com.ironsource.mediationsdk.sdk.BannerListener() {
                                @Override
                                public void onBannerAdLoaded() {
                                    ((ADViewHolder) holder).rl_native_ad.removeAllViews();
                                    ((ADViewHolder) holder).rl_native_ad.addView(banner);

                                    ((ADViewHolder) holder).rl_native_ad.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onBannerAdLoadFailed(IronSourceError error) {

                                }

                                @Override
                                public void onBannerAdClicked() {
                                }

                                @Override
                                public void onBannerAdScreenPresented() {
                                }

                                @Override
                                public void onBannerAdScreenDismissed() {
                                }

                                @Override
                                public void onBannerAdLeftApplication() {
                                }
                            });
                            IronSource.loadBanner(banner);
                            ((ADViewHolder) holder).isAdRequested = true;
                        }
                        break;
                }
            }
        } else {
            if (getItemCount() == 1) {
                ProgressViewHolder.progressBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size() + 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void hideHeader() {
        try {
            ProgressViewHolder.progressBar.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isHeader(int position) {
        return position == arrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeader(position)) {
            return VIEW_PROG;
        } else if (arrayList.get(position) == null) {
            return 1000 + position;
        } else {
            return position;
        }
    }

    public interface RecyclerItemClickListener{
        void onClickListener(ItemCategory itemCategory, int position);
    }

    public void addAds(ArrayList<NativeAd> arrayListNativeAds) {
        isAdLoaded = true;
        mNativeAdsAdmob.addAll(arrayListNativeAds);
        for (int i = 0; i < arrayList.size(); i++) {
            if(arrayList.get(i) == null) {
                notifyItemChanged(i);
            }
        }
    }

    private void populateUnifiedNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        MediaView mediaView = adView.findViewById(R.id.ad_media);
        adView.setMediaView(mediaView);

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline is guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad. The SDK will populate the adView's MediaView
        // with the media content from this native ad.
        adView.setNativeAd(nativeAd);
    }
}