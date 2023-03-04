package nemosofts.vox.radio.adapter.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

import nemosofts.vox.radio.R;
import nemosofts.vox.radio.item.ItemPodcasts;

public class AdapterHomePodcasts extends RecyclerView.Adapter<AdapterHomePodcasts.ViewHolder> {

    private final List<ItemPodcasts> arrayList;
    private final RecyclerItemClickListener listener;

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final RoundedImageView iv_podcasts;
        private final TextView tv_podcasts;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_podcasts = itemView.findViewById(R.id.tv_podcasts);
            iv_podcasts = itemView.findViewById(R.id.iv_podcasts);
        }
    }

    public AdapterHomePodcasts(List<ItemPodcasts> arrayList, RecyclerItemClickListener listener) {
        this.arrayList = arrayList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_podcasts,parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final ItemPodcasts item = arrayList.get(position);

        Picasso.get()
                .load(item.getImage())
                .placeholder(R.drawable.material_design_default)
                .into(holder.iv_podcasts);

        holder.tv_podcasts.setText(item.getTitle());

        holder.iv_podcasts.setOnClickListener(v -> {
            try {
                listener.onClickListener(item, holder.getAdapterPosition());
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
        void onClickListener(ItemPodcasts itemPodcasts, int position);
    }

}
