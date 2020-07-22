package com.akb.seetalk.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.akb.seetalk.Model.Notification;
import com.akb.seetalk.Model.Post;
import com.akb.seetalk.Model.User;
import com.akb.seetalk.R;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder>{

    private Context mContext;
    private List<Notification> mNotification;

    public NotificationAdapter(Context context, List<Notification> notification){
        mContext = context;
        mNotification = notification;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
        return new NotificationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Notification notification = mNotification.get(position);

        holder.text.setText(notification.getText());

        getUserInfo(holder.imageView, holder.username, notification.getUserid());

        if (notification.isIspost()) {
            holder.post_image.setVisibility(View.VISIBLE);
            getPostImage(holder.post_image, notification.getPostid());
        } else {
            holder.post_image.setVisibility(View.GONE);
        }

        /*
         *setOnclick itemnya masih crash
        */
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                    if(notification.isIspost()){
//                        Intent intent = new Intent(mContext, myPostDetailActivity.class);
//                        intent.putExtra("postid", notification.getPostid());
//                        mContext.startActivity(intent);
//                    }else{
//                        Intent intent = new Intent(mContext, ViewProfileActivity.class);
//                        intent.putExtra("userid", notification.getUserid());
//                        mContext.startActivity(intent);
//                    }
//            }
//        });

    }

    @Override
    public int getItemCount() {
        return mNotification.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView imageView, post_image;
        public TextView username, text;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.image_profile);
            post_image = itemView.findViewById(R.id.post_image);
            username = itemView.findViewById(R.id.username);
            text = itemView.findViewById(R.id.text);
        }
    }

    private void getUserInfo(ImageView imageView, TextView username, String publisherid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("User").child(publisherid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if(user.getImageURL().equals("default")){
                    imageView.setImageResource(R.drawable.profile_img);
                } else {
                    Glide.with(mContext).load(user.getImageURL()).into(imageView);
                }
                username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getPostImage(ImageView imageView, String postid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post = snapshot.getValue(Post.class);
                Glide.with(mContext).load(post.getPostimage()).into(imageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
