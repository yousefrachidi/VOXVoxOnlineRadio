package nemosofts.vox.radio.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.like.LikeButton;
import com.like.OnLikeListener;
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

import es.claucookie.miniequalizerlibrary.EqualizerView;
import nemosofts.vox.radio.R;
import nemosofts.vox.radio.activity.PlayerService;
import nemosofts.vox.radio.asyncTask.LoadStatus;
import nemosofts.vox.radio.callback.Callback;
import nemosofts.vox.radio.dialog.PremiumDialog;
import nemosofts.vox.radio.interfaces.ClickListenerPlayList;
import nemosofts.vox.radio.interfaces.SuccessListener;
import nemosofts.vox.radio.item.ItemRadio;
import nemosofts.vox.radio.utils.ApplicationUtil;
import nemosofts.vox.radio.utils.Helper;
import nemosofts.vox.radio.utils.SharedPref;


public class AdapterRadioList extends RecyclerView.Adapter {

    private final Context context;
    private ArrayList<ItemRadio> arrayList;
    private final ArrayList<ItemRadio> filteredArrayList;
    private final ClickListenerPlayList recyclerClickListener;
    private NameFilter filter;
    private final Helper helper;
    private final SharedPref sharedPref;
    private final int VIEW_PROG = -1;

    Boolean isAdLoaded = false;
    List<NativeAd> mNativeAdsAdmob = new ArrayList<>();

    public AdapterRadioList(Context context, ArrayList<ItemRadio> arrayList, ClickListenerPlayList recyclerClickListener) {
        this.arrayList = arrayList;
        this.filteredArrayList = arrayList;
        this.context = context;
        this.recyclerClickListener = recyclerClickListener;
        helper = new Helper(context);
        sharedPref = new SharedPref(context);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private final RelativeLayout rl_radio_list;
        private final RoundedImageView iv_radio_list;
        private final EqualizerView equalizer;
        private final TextView radioTitle,catName, tv_avg_rate, tv_views, tv_premium;
        private final RatingBar ratingBar;
        private final ImageView iv_list_option;
        private final LikeButton likeButton;
        private final RelativeLayout rl_native_ad;

        MyViewHolder(View view) {
            super(view);
            rl_radio_list = view.findViewById(R.id.rl_radio_list);
            iv_radio_list = view.findViewById(R.id.iv_radio_list);
            equalizer = view.findViewById(R.id.equalizer_radio_list);
            radioTitle = view.findViewById(R.id.tv_radio_list_name);
            catName = view.findViewById(R.id.tv_radio_list_cat);
            ratingBar = view.findViewById(R.id.rb_radio_list);
            tv_avg_rate = view.findViewById(R.id.tv_radio_list_avg_rate);
            tv_views = view.findViewById(R.id.tv_radio_list_views);
            likeButton = itemView.findViewById(R.id.iv_radio_fav_list);
            iv_list_option = view.findViewById(R.id.iv_list_option);
            tv_premium = view.findViewById(R.id.tv_radio_list_premium);

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
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_radio_vertical, parent, false);
            return new MyViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof MyViewHolder) {

            ((MyViewHolder) holder).radioTitle.setText(arrayList.get(position).getRadioTitle());
            ((MyViewHolder) holder).catName.setText(arrayList.get(position).getCategoryName());
            ((MyViewHolder) holder).likeButton.setLiked(arrayList.get(position).IsFav());
            ((MyViewHolder) holder).tv_views.setText(ApplicationUtil.viewFormat(Integer.valueOf(arrayList.get(position).getTotalViews())));
            ((MyViewHolder) holder).tv_premium.setVisibility(arrayList.get(position).IsPremium() ? View.VISIBLE : View.GONE);

            Picasso.get()
                    .load(arrayList.get(position).getImage())
                    .placeholder(ApplicationUtil.placeholderRadio())
                    .into(((MyViewHolder) holder).iv_radio_list);

            Log.d("dataLog", "onBindViewHolder: "+arrayList.get(position).getImage());

            ((MyViewHolder) holder).tv_avg_rate.setTypeface(((MyViewHolder) holder).tv_avg_rate.getTypeface(), Typeface.BOLD);
            ((MyViewHolder) holder).tv_avg_rate.setText(arrayList.get(position).getAverageRating());
            ((MyViewHolder) holder).ratingBar.setRating(Float.parseFloat(arrayList.get(position).getAverageRating()));

            if (PlayerService.getIsPlayling() && Callback.playPos <= holder.getAdapterPosition() && Callback.arrayList_play.get(Callback.playPos).getId().equals(arrayList.get(position).getId()) && Callback.isRadio) {
                ((MyViewHolder) holder).equalizer.setVisibility(View.VISIBLE);
                ((MyViewHolder) holder).equalizer.animateBars();

                ((MyViewHolder) holder).rl_radio_list.setBackgroundColor(ApplicationUtil.accent_50ColorUtils(context));
                ((MyViewHolder) holder).radioTitle.setTextColor(ApplicationUtil.accentColorUtils(context));

            } else {
                ((MyViewHolder) holder).equalizer.setVisibility(View.GONE);
                ((MyViewHolder) holder).equalizer.stopBars();

                ((MyViewHolder) holder).rl_radio_list.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                ((MyViewHolder) holder).radioTitle.setTextColor(ApplicationUtil.titleColorUtils(context));
            }

            ((MyViewHolder) holder).rl_radio_list.setOnClickListener(view -> {
                try {
                    if (Callback.isPurchases){
                        recyclerClickListener.onClick(getPosition(arrayList.get(holder.getAdapterPosition()).getId()));
                    } else {
                        if (arrayList.get(position).IsPremium() && !Callback.isPurchases){
                            new PremiumDialog((Activity) context);
                        } else {
                            recyclerClickListener.onClick(getPosition(arrayList.get(holder.getAdapterPosition()).getId()));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            ((MyViewHolder) holder).iv_list_option.setOnClickListener(view -> {
                try {
                    openOptionPopUp(((MyViewHolder) holder).iv_list_option, holder.getAdapterPosition());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            if(sharedPref.isLogged()) {
                ((MyViewHolder) holder).likeButton.setOnLikeListener(new OnLikeListener() {
                    @Override
                    public void liked(LikeButton likeButton) {
                        try {
                            loadFav(arrayList.get(holder.getAdapterPosition()).getId(), holder.getAdapterPosition());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void unLiked(LikeButton likeButton) {
                        try {
                            loadFav(arrayList.get(holder.getAdapterPosition()).getId(), holder.getAdapterPosition());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                ((MyViewHolder) holder).likeButton.setOnClickListener(view -> {
                    if(!sharedPref.isLogged()) {
                        helper.clickLogin();
                    }
                });
            }

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
                                    }
                                });
                                break;
                            case Callback.AD_TYPE_WORTISE:
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
                                break;
                                case Callback.AD_TYPE_IRONSOURCE:
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

    private void loadFav(String id, int adapterPosition) {
        if (helper.isNetworkAvailable()) {
            LoadStatus loadFav = new LoadStatus(new SuccessListener() {
                @Override
                public void onStart() {
                    // this method is empty
                }

                @Override
                public void onEnd(String success, String favSuccess, String message) {
                    if (success.equals("1")) {
                        arrayList.get(adapterPosition).setIsFav(message.equals("Added to Favourite"));
                        notifyItemChanged(adapterPosition);
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, context.getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
                    }
                }
            }, helper.callAPI(Callback.METHOD_DO_FAV, 0, id, "", "", "", sharedPref.getUserId(), "", "", "", "", "", "", "", null));
            loadFav.execute();
        } else {
            Toast.makeText(context, context.getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NonConstantResourceId")
    private void openOptionPopUp(ImageView iv_option, int adapterPosition) {
        ContextThemeWrapper ctw;
        if (ApplicationUtil.isDarkMode()) {
            ctw = new ContextThemeWrapper(context, R.style.PopupMenuDark);
        } else {
            ctw = new ContextThemeWrapper(context, R.style.PopupMenuLight);
        }
        PopupMenu popup = new PopupMenu(ctw, iv_option);
        popup.getMenuInflater().inflate(R.menu.popup_radio, popup.getMenu());
        popup.setForceShowIcon(true);

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.popup_youtube) {
                Intent intent = new Intent(Intent.ACTION_SEARCH);
                intent.setPackage("com.google.android.youtube");
                intent.putExtra("query", arrayList.get(adapterPosition).getRadioTitle());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else if (itemId == R.id.popup_share) {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.share_radio));
                sharingIntent.putExtra(Intent.EXTRA_TEXT, context.getResources().getString(R.string.listening) + " - " + arrayList.get(adapterPosition).getRadioTitle() + "\n\nvia " + context.getResources().getString(R.string.app_name) + " - http://play.google.com/store/apps/details?id=" + context.getPackageName());
                context.startActivity(Intent.createChooser(sharingIntent, context.getResources().getString(R.string.share_radio)));
            }
            return true;
        });
        popup.show();
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