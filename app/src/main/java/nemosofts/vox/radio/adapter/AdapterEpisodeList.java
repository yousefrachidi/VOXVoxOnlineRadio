package nemosofts.vox.radio.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.startapp.sdk.ads.nativead.NativeAdPreferences;
import com.startapp.sdk.ads.nativead.StartAppNativeAd;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.wortise.ads.natives.GoogleNativeAd;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import nemosofts.vox.radio.R;
import nemosofts.vox.radio.activity.PlayerService;
import nemosofts.vox.radio.callback.Callback;
import nemosofts.vox.radio.interfaces.ClickListenerPlayList;
import nemosofts.vox.radio.item.ItemRadio;
import nemosofts.vox.radio.utils.ApplicationUtil;


public class AdapterEpisodeList extends RecyclerView.Adapter {

    private final Context context;
    private ArrayList<ItemRadio> arrayList;
    private final ArrayList<ItemRadio> filteredArrayList;
    private final ClickListenerPlayList recyclerClickListener;
    private NameFilter filter;
    private final int VIEW_PROG = -1;

    Boolean isAdLoaded = false;
    List<NativeAd> mNativeAdsAdmob = new ArrayList<>();
    public AdapterEpisodeList(Context context, ArrayList<ItemRadio> arrayList, ClickListenerPlayList recyclerClickListener) {
        this.arrayList = arrayList;
        this.filteredArrayList = arrayList;
        this.context = context;
        this.recyclerClickListener = recyclerClickListener;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private final RelativeLayout rl_podcasts_list;
        private final RoundedImageView iv_podcasts_list;
        private final TextView tv_podcasts_name, tv_podcasts_cat;
        private final ImageView iv_podcasts_play;
        private final RelativeLayout rl_native_ad;
        boolean isAdRequested = false;

        MyViewHolder(View view) {
            super(view);
            rl_podcasts_list = view.findViewById(R.id.rl_podcasts_list);
            iv_podcasts_list = view.findViewById(R.id.iv_podcasts_list);
            tv_podcasts_name = view.findViewById(R.id.tv_podcasts_list_name);
            tv_podcasts_cat = view.findViewById(R.id.tv_podcasts_list_cat);
            iv_podcasts_play = view.findViewById(R.id.iv_podcasts_play);

            rl_native_ad = view.findViewById(R.id.rl_native_ad);
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

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_PROG) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_progressbar, parent, false);
            return new ProgressViewHolder(v);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_podcasts_vertical, parent, false);
            return new MyViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof MyViewHolder) {

            ((MyViewHolder) holder).tv_podcasts_name.setText(arrayList.get(position).getRadioTitle());
            ((MyViewHolder) holder).tv_podcasts_cat.setText(arrayList.get(position).getCategoryName());

            Picasso.get()
                    .load(arrayList.get(position).getImage())
                    .placeholder(R.drawable.material_design_default)
                    .into(((MyViewHolder) holder).iv_podcasts_list);

            if (PlayerService.getIsPlayling() && Callback.playPos <= holder.getAdapterPosition() && Callback.arrayList_play.get(Callback.playPos).getId().equals(arrayList.get(position).getId()) && !Callback.isRadio) {
                ((MyViewHolder) holder).iv_podcasts_play.setImageResource(R.drawable.ic_pause);
                ((MyViewHolder) holder).rl_podcasts_list.setBackgroundColor(ApplicationUtil.accent_50ColorUtils(context));
                ((MyViewHolder) holder).iv_podcasts_play.setImageTintList(ColorStateList.valueOf(ApplicationUtil.accentColorUtils(context)));
            } else {
                ((MyViewHolder) holder).iv_podcasts_play.setImageResource(R.drawable.ic_play);
                ((MyViewHolder) holder).rl_podcasts_list.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                if (ApplicationUtil.isDarkMode()){
                    ((MyViewHolder) holder).iv_podcasts_play.setImageTintList(ColorStateList.valueOf(ApplicationUtil.colorUtilsWhite(context)));
                } else {
                    ((MyViewHolder) holder).iv_podcasts_play.setImageTintList(ColorStateList.valueOf(ApplicationUtil.colorUtilsBlack(context)));
                }
            }
            ((MyViewHolder) holder).rl_podcasts_list.setOnClickListener(view -> recyclerClickListener.onClick(getPosition(arrayList.get(holder.getAdapterPosition()).getId())));

            if (Callback.isNativeAd && isAdLoaded && (position != arrayList.size() - 1) && (position + 1) % Callback.nativeAdShow == 0 && !Callback.isPurchases) {
                try {
                    if (((MyViewHolder) holder).rl_native_ad.getChildCount() == 0) {
                        switch (Callback.adNetwork) {
                            case Callback.AD_TYPE_ADMOB:
                            case Callback.AD_TYPE_FACEBOOK:
                                if (isAdLoaded) {
                                    if (mNativeAdsAdmob.size() >= 5) {

                                        int i = new Random().nextInt(mNativeAdsAdmob.size() - 1);

                                        NativeAdView adView = (NativeAdView) ((Activity) context).getLayoutInflater().inflate(R.layout.layout_native_ad_admob, null);
                                        populateUnifiedNativeAdView(mNativeAdsAdmob.get(i), adView);
                                        ((MyViewHolder) holder).rl_native_ad.removeAllViews();
                                        ((MyViewHolder) holder).rl_native_ad.addView(adView);

                                        ((MyViewHolder) holder).rl_native_ad.setVisibility(View.VISIBLE);
                                    }
                                }
                                break;
                            case Callback.AD_TYPE_STARTAPP:
                                if (!((MyViewHolder) holder).isAdRequested) {
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

                                                    ((MyViewHolder) holder).rl_native_ad.removeAllViews();
                                                    ((MyViewHolder) holder).rl_native_ad.addView(nativeAdView);
                                                    ((MyViewHolder) holder).rl_native_ad.setVisibility(View.VISIBLE);
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onFailedToReceiveAd(Ad ad) {
                                            ((MyViewHolder) holder).isAdRequested = false;
                                        }
                                    });
                                    ((MyViewHolder) holder).isAdRequested = true;
                                }
                                break;
                            case Callback.AD_TYPE_WORTISE:
                                if (!((MyViewHolder) holder).isAdRequested) {
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
                                            ((MyViewHolder) holder).rl_native_ad.removeAllViews();
                                            ((MyViewHolder) holder).rl_native_ad.addView(adView);

                                            ((MyViewHolder) holder).rl_native_ad.setVisibility(View.VISIBLE);
                                        }
                                    });
                                    googleNativeAd.load();
                                    ((MyViewHolder) holder).isAdRequested = true;
                                }
                                break;case Callback.AD_TYPE_IRONSOURCE:
                                if (!((MyViewHolder) holder).isAdRequested) {
                                    IronSource.init((Activity) context, Callback.ironAdsId, IronSource.AD_UNIT.BANNER);
                                    IronSourceBannerLayout banner = IronSource.createBanner((Activity) context, ISBannerSize.BANNER);
                                    banner.setBannerListener(new com.ironsource.mediationsdk.sdk.BannerListener() {
                                        @Override
                                        public void onBannerAdLoaded() {
                                            ((MyViewHolder) holder).rl_native_ad.removeAllViews();
                                            ((MyViewHolder) holder).rl_native_ad.addView(banner);

                                            ((MyViewHolder) holder).rl_native_ad.setVisibility(View.VISIBLE);
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
                                    ((MyViewHolder) holder).isAdRequested = true;
                                }
                                break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (arrayList.get(position) != null) {
            return position;
        } else {
            return VIEW_PROG;
        }
    }

    private int getPosition(String id) {
        int count = 0;
        for (int i = 0; i < filteredArrayList.size(); i++) {
            if (id.equals(filteredArrayList.get(i).getId())) {
                count = i;
                break;
            }
        }
        return count;
    }

    public Filter getFilter() {
        if (filter == null) {
            filter = new NameFilter();
        }
        return filter;
    }

    private class NameFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            constraint = constraint.toString().toLowerCase();
            FilterResults result = new FilterResults();
            if (constraint.toString().length() > 0) {
                ArrayList<ItemRadio> filteredItems = new ArrayList<>();

                for (int i = 0, l = filteredArrayList.size(); i < l; i++) {
                    String nameList = filteredArrayList.get(i).getRadioTitle();
                    if (nameList.toLowerCase().contains(constraint))
                        filteredItems.add(filteredArrayList.get(i));
                }
                result.count = filteredItems.size();
                result.values = filteredItems;
            } else {
                synchronized (this) {
                    result.values = filteredArrayList;
                    result.count = filteredArrayList.size();
                }
            }
            return result;
        }

        @SuppressLint("NotifyDataSetChanged")
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            arrayList = (ArrayList<ItemRadio>) results.values;
            notifyDataSetChanged();
        }
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