package nemosofts.vox.radio.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.ads.mediation.facebook.FacebookMediationAdapter;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import nemosofts.vox.radio.R;
import nemosofts.vox.radio.activity.PostIDActivity;
import nemosofts.vox.radio.adapter.AdapterCategory;
import nemosofts.vox.radio.asyncTask.LoadCategory;
import nemosofts.vox.radio.callback.Callback;
import nemosofts.vox.radio.interfaces.CategoryListener;
import nemosofts.vox.radio.item.ItemCategory;
import nemosofts.vox.radio.utils.EndlessRecyclerViewScrollListener;
import nemosofts.vox.radio.utils.Helper;

public class FragmentCategory extends Fragment {

    private Helper helper;
    private RecyclerView rv;
    private AdapterCategory adapter;
    private ArrayList<ItemCategory> arrayList;
    private Boolean isOver = false, isScroll = false;
    private int page = 1;
    private GridLayoutManager grid;
    private ProgressBar pb;
    private FloatingActionButton fab;
    private String error_msg;
    private FrameLayout frameLayout;

    private int  nativeAdPos = 0;
    AdLoader adLoader;
    ArrayList<NativeAd> arrayListNativeAds = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_latest, container, false);

        helper = new Helper(getActivity(), (position, type) -> {
            Intent intent = new Intent(getActivity(), PostIDActivity.class);
            intent.putExtra("page_type", getString(R.string.category));
            intent.putExtra("id", arrayList.get(position).getId());
            intent.putExtra("name", arrayList.get(position).getTitle());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        arrayList = new ArrayList<>();

        frameLayout = rootView.findViewById(R.id.fl_empty);
        fab = rootView.findViewById(R.id.fab);
        pb = rootView.findViewById(R.id.pb);
        rv = rootView.findViewById(R.id.rv);
        rv.setHasFixedSize(true);

        grid = new GridLayoutManager(getActivity(), 1);
        grid.setSpanCount(2);
        grid.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (adapter.getItemViewType(position) >= 1000 || adapter.isHeader(position)) ? grid.getSpanCount() : 1;
            }
        });

        rv.setLayoutManager(grid);
        rv.addOnScrollListener(new EndlessRecyclerViewScrollListener(grid) {
            @Override
            public void onLoadMore(int p, int totalItemsCount) {
                if (getActivity() != null) {
                    if (!isOver) {
                        new Handler().postDelayed(() -> {
                            isScroll = true;
                            getData();
                        }, 0);
                    } else {
                        adapter.hideHeader();
                    }
                }
            }
        });

        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisibleItem = grid.findFirstVisibleItemPosition();
                if (firstVisibleItem > 6) {
                    fab.show();
                } else {
                    fab.hide();
                }
            }
        });

        fab.setOnClickListener(v -> rv.smoothScrollToPosition(0));


        if(Callback.isNativeAd) {
            if(Callback.nativeAdShow%2 != 0) {
                nativeAdPos = Callback.nativeAdShow + 1;
            } else {
                nativeAdPos = Callback.nativeAdShow;
            }
        }
        getData();

        setHasOptionsMenu(true);
        return rootView;
    }

    private void getData() {
        if (helper.isNetworkAvailable()) {
            LoadCategory loadCategory = new LoadCategory(new CategoryListener() {
                @Override
                public void onStart() {
                    if (arrayList.size() == 0) {
                        frameLayout.setVisibility(View.GONE);
                        rv.setVisibility(View.GONE);
                        pb.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onEnd(String success, String verifyStatus, String message, ArrayList<ItemCategory> arrayListCat) {
                    if (getActivity() != null) {
                        if (success.equals("1")) {
                            if (!verifyStatus.equals("-1")) {
                                if (arrayListCat.size() == 0) {
                                    isOver = true;
                                    try {
                                        adapter.hideHeader();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    error_msg = getString(R.string.error_no_data_found);
                                    setEmpty();
                                } else {
                                    for (int i = 0; i < arrayListCat.size(); i++) {
                                        arrayList.add(arrayListCat.get(i));
                                        if (Callback.isNativeAd) {
                                            int abc = arrayList.lastIndexOf(null);
                                            if (((arrayList.size() - (abc + 1)) % nativeAdPos == 0)) {
                                                arrayList.add(null);
                                            }
                                        }
                                    }
                                    page = page + 1;
                                    setAdapter();
                                }
                            } else {
                                helper.getVerifyDialog(getString(R.string.error_unauthorized_access), message);
                            }
                        } else {
                            error_msg = getString(R.string.error_server_not_connected);
                            setEmpty();
                        }
                    }
                }
            }, helper.callAPI(Callback.METHOD_CAT, page, "", "", "", "", "", "", "", "", "", "", "", "", null));
            loadCategory.execute();
        } else {
            error_msg = getString(R.string.error_internet_not_connected);
            setEmpty();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setAdapter() {
        if(!isScroll) {
            adapter = new AdapterCategory(getActivity(),  arrayList, (itemSong, position) -> helper.showInterAd(position, ""));
            rv.setAdapter(adapter);
            rv.scheduleLayoutAnimation();
            setEmpty();
            loadNativeAds();
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private void loadNativeAds() {
        if (Callback.isNativeAd && !Callback.adNetwork.equals(Callback.AD_TYPE_WORTISE)
                && !Callback.adNetwork.equals(Callback.AD_TYPE_STARTAPP) && !Callback.adNetwork.equals(Callback.AD_TYPE_IRONSOURCE) && arrayList.size() >= 10) {
            AdLoader.Builder builder = new AdLoader.Builder(getActivity(), Callback.nativeAdID);

            Bundle extras = new Bundle();
            if (ConsentInformation.getInstance(getActivity()).getConsentStatus() != ConsentStatus.PERSONALIZED) {
                extras.putString("npa", "1");
            }

            AdRequest adRequest;
            if(Callback.adNetwork.equals(Callback.AD_TYPE_ADMOB)) {
                adRequest = new AdRequest.Builder()
                        .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                        .build();
            } else {
                adRequest = new AdRequest.Builder()
                        .addNetworkExtrasBundle(AdMobAdapter.class, new Bundle())
                        .addNetworkExtrasBundle(FacebookMediationAdapter.class, extras)
                        .build();
            }

            adLoader = builder.forNativeAd(nativeAd -> {
                try {
                    if(adLoader.isLoading()) {
                        arrayListNativeAds.add(nativeAd);
                    } else {
                        arrayListNativeAds.add(nativeAd);
                        adapter.addAds(arrayListNativeAds);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).build();
            adLoader.loadAds(adRequest, 5);
        }
    }

    @SuppressLint("InflateParams")
    private void setEmpty() {
        if (arrayList.size() > 0) {
            rv.setVisibility(View.VISIBLE);
            pb.setVisibility(View.INVISIBLE);
            frameLayout.setVisibility(View.GONE);
        } else {
            pb.setVisibility(View.INVISIBLE);
            rv.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);

            frameLayout.removeAllViews();

            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View myView = inflater.inflate(R.layout.layout_empty, null);

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(error_msg);

            myView.findViewById(R.id.btn_empty_try).setOnClickListener(v -> getData());

            frameLayout.addView(myView);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }
}