package nemosofts.vox.radio.adapter.home;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.like.LikeButton;
import com.like.OnLikeListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

import es.claucookie.miniequalizerlibrary.EqualizerView;
import nemosofts.vox.radio.R;
import nemosofts.vox.radio.activity.PlayerService;
import nemosofts.vox.radio.asyncTask.LoadStatus;
import nemosofts.vox.radio.callback.Callback;
import nemosofts.vox.radio.dialog.PremiumDialog;
import nemosofts.vox.radio.interfaces.SuccessListener;
import nemosofts.vox.radio.item.ItemRadio;
import nemosofts.vox.radio.utils.ApplicationUtil;
import nemosofts.vox.radio.utils.Helper;
import nemosofts.vox.radio.utils.SharedPref;

public class AdapterHome extends RecyclerView.Adapter<AdapterHome.ViewHolder> {

    private final Helper helper;
    private final SharedPref sharedPref;
    private final List<ItemRadio> arrayList;
    private final Context context;
    private final RecyclerItemClickListener listener;
    private final Boolean recently;

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final RoundedImageView iv_radio;
        private final TextView tv_name;
        private final TextView tv_radio_cat;
        private final TextView tv_premium;
        private final RelativeLayout rl_radio_home;
        private final LikeButton likeButton;
        private final EqualizerView equalizer;
        private final ImageView iv_play;

        public ViewHolder(View itemView) {
            super(itemView);
            iv_radio = itemView.findViewById(R.id.iv_radio);
            tv_name = itemView.findViewById(R.id.tv_radio_name);
            rl_radio_home = itemView.findViewById(R.id.rl_radio_home);
            tv_radio_cat = itemView.findViewById(R.id.tv_radio_cat);
            likeButton = itemView.findViewById(R.id.iv_radio_fav);
            tv_premium = itemView.findViewById(R.id.tv_radio_premium);
            equalizer = itemView.findViewById(R.id.equalizer_view);
            iv_play = itemView.findViewById(R.id.iv_play);
        }
    }

    public AdapterHome(Context context, List<ItemRadio> arrayList, Boolean isRecently, RecyclerItemClickListener listener) {
        this.arrayList = arrayList;
        this.context = context;
        this.listener = listener;
        helper = new Helper(context);
        sharedPref = new SharedPref(context);
        recently = isRecently;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home,parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final ItemRadio item = arrayList.get(position);

        if (ApplicationUtil.isDarkMode()){
            Picasso.get()
                    .load(item.getImage())
                    .placeholder(R.drawable.placeholder_song_night)
                    .into(holder.iv_radio);
        } else {
            Picasso.get()
                    .load(item.getImage())
                    .placeholder(R.drawable.placeholder_song_light)
                    .into(holder.iv_radio);
        }

        holder.tv_name.setText(item.getRadioTitle());
        holder.tv_radio_cat.setText(item.getCategoryName());
        holder.likeButton.setLiked(item.IsFav());

        holder.likeButton.setVisibility(recently ? View.GONE : View.VISIBLE);
        holder.tv_premium.setVisibility(item.IsPremium() ? View.VISIBLE : View.GONE);

        if (PlayerService.getIsPlayling() && Callback.arrayList_play.get(Callback.playPos).getId().equals(item.getId()) && Callback.arrayList_play.get(Callback.playPos).getRadioTitle().equals(item.getRadioTitle()) && Callback.isRadio) {
            holder.equalizer.setVisibility(View.VISIBLE);
            holder.iv_play.setVisibility(View.GONE);
            holder.equalizer.animateBars();
        } else {
            holder.equalizer.setVisibility(View.GONE);
            holder.iv_play.setVisibility(View.VISIBLE);
            holder.equalizer.stopBars();
        }

        if(sharedPref.isLogged()) {
            holder.likeButton.setOnLikeListener(new OnLikeListener() {
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
            holder.likeButton.setOnClickListener(view -> {
                if(!sharedPref.isLogged()) {
                    helper.clickLogin();
                }
            });
        }

        holder.rl_radio_home.setOnClickListener(v -> {
            try {
                if (Callback.isPurchases){
                    listener.onClickListener(item, holder.getAdapterPosition());
                } else {
                    if (item.IsPremium() && !Callback.isPurchases){
                        new PremiumDialog((Activity) context);
                    } else {
                        listener.onClickListener(item, holder.getAdapterPosition());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public interface RecyclerItemClickListener{
        void onClickListener(ItemRadio itemSong, int position);
    }

    private void loadFav(String id, int adapterPosition) {
        if (helper.isNetworkAvailable()) {
            LoadStatus loadFav = new LoadStatus(new SuccessListener() {
                @Override
                public void onStart() {
                    // TODO document why this method is empty
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

}
