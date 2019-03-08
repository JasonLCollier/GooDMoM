package za.ac.uct.goodmom;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyViewHolder> {

    private List<PostData> mPostsList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView, dateTextView;
        public ImageView thumbImageView;

        public MyViewHolder(View view) {
            super(view);
            thumbImageView = (ImageView) view.findViewById(R.id.post_thumb);
            titleTextView = (TextView) view.findViewById(R.id.post_title);
            dateTextView = (TextView) view.findViewById(R.id.post_date);
        }
    }


    public PostAdapter(List<PostData> postsList) {
        mPostsList = postsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        PostData post = mPostsList.get(position);

        boolean isThumbUrl = post.getThumbUrl() != null;
        if(isThumbUrl) {
            Glide.with(holder.thumbImageView.getContext())
                    .load(post.getThumbUrl())
                    .into(holder.thumbImageView);
        }
        else {
            // holder image
        }

        holder.titleTextView.setText(post.getTitle());
        holder.dateTextView.setText(post.getDate());
    }

    @Override
    public int getItemCount() {
        return mPostsList.size();
    }
}
