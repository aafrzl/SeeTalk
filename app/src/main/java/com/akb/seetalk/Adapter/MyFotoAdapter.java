package com.akb.seetalk.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.akb.seetalk.Model.Post;
import com.akb.seetalk.R;
import com.akb.seetalk.Activity.myPostDetailActivity;
import com.bumptech.glide.Glide;

import java.util.List;

public class MyFotoAdapter extends RecyclerView.Adapter<MyFotoAdapter.ViewHolder>{
    private Context context;
    private List<Post> mPosts;

    public MyFotoAdapter(Context context, List<Post> mPosts) {
        this.context = context;
        this.mPosts = mPosts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fotos_item,
                parent, false);
        return new MyFotoAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Post post = mPosts.get(position);

        Glide.with(context).load(post.getPostimage()).into(holder.post_image);

        holder.post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, myPostDetailActivity.class);
                intent.putExtra("postid", post.getPostid());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView post_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            post_image = itemView.findViewById(R.id.post_image);
        }
    }
}
