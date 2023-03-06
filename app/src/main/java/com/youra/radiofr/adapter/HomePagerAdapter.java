package com.youra.radiofr.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.squareup.picasso.Picasso;
import com.tiagosantos.enchantedviewpager.EnchantedViewPager;
import com.tiagosantos.enchantedviewpager.EnchantedViewPagerAdapter;

import java.util.ArrayList;

import com.youra.radiofr.R;
import com.youra.radiofr.activity.PostIDActivity;
import com.youra.radiofr.item.ItemBanner;
import com.youra.radiofr.utils.LoadColor;

public class HomePagerAdapter extends EnchantedViewPagerAdapter {

    private final Context mContext;
    private final LayoutInflater inflater;
    private final ArrayList<ItemBanner> arrayList;

    public HomePagerAdapter(Context context, ArrayList<ItemBanner> arrayList) {
        super(arrayList);
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        this.arrayList = arrayList;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        View mCurrentView = inflater.inflate(R.layout.layout_home_banner, container, false);

        TextView tv_title, tv_desc;
        ImageView iv_banner;
        View view_gradient;

        tv_title = mCurrentView.findViewById(R.id.tv_home_banner);
        tv_desc = mCurrentView.findViewById(R.id.tv_home_banner_desc);
        iv_banner = mCurrentView.findViewById(R.id.iv_home_banner);
        view_gradient = mCurrentView.findViewById(R.id.view_home_banner);


        tv_title.setText(arrayList.get(position).getTitle());
        tv_desc.setText(arrayList.get(position).getInfo());

        Picasso.get()
                .load(arrayList.get(position).getImage())
                .placeholder(R.drawable.logo)
                .into(iv_banner);

        new LoadColor(view_gradient).execute(arrayList.get(position).getImage());

        iv_banner.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, PostIDActivity.class);
            intent.putExtra("page_type", "banner");
            intent.putExtra("id", arrayList.get(position).getId());
            intent.putExtra("name", arrayList.get(position).getTitle());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        });

        mCurrentView.setTag(EnchantedViewPager.ENCHANTED_VIEWPAGER_POSITION + position);
        container.addView(mCurrentView);

        return mCurrentView;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

}