package com.akb.seetalk.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.akb.seetalk.Model.User;
import com.akb.seetalk.R;
import com.akb.seetalk.ViewProfileActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class CariTemanAdapter extends RecyclerView.Adapter<CariTemanAdapter.ViewHolder>{

    private Context mContext;
    private List<User> mUsers;
    private boolean ischat;
    private OnItemClick onItemClick;

    private FirebaseUser firebaseUser;

//    String theLastMessage;

    public CariTemanAdapter(Context mContext, List<User>mUsers, boolean ischat, OnItemClick onItemClick){
        this.mUsers = mUsers;
        this.mContext = mContext;
        this.ischat = ischat;
        this.onItemClick = onItemClick;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.cariteman_item, parent, false);
        return new CariTemanAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final User user = mUsers.get(position);

        holder.followBtn.setVisibility(View.VISIBLE);
        holder.username.setText(user.getUsername());
        holder.bio.setText(user.getBio());

        if(user.getImageURL().equals("default")){
            holder.profile_image.setImageResource(R.drawable.profile_img);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
        }

        isFollowing(user.getId(), holder.followBtn);

        if(user.getId().equals(firebaseUser.getUid())){
            holder.followBtn.setVisibility(View.GONE);
        }


        if(ischat){
            if(user.getStatus().equals("online")){
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            }else{
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
        }else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ViewProfileActivity.class);
                intent.putExtra("userid", user.getId());
                mContext.startActivity(intent);
            }
        });

        holder.profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ViewProfileActivity.class);
                intent.putExtra("userid", user.getId());
                mContext.startActivity(intent);
            }
        });

        holder.followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.followBtn.getText().toString().equals("Follow")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("Following").child(user.getId()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId())
                            .child("Following").child(firebaseUser.getUid()).setValue(true);

                    DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                            .child(firebaseUser.getUid())
                            .child(user.getId());
                    chatRef.child("id").setValue(user.getId());

                    DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                            .child(user.getId())
                            .child(firebaseUser.getUid());
                    chatRefReceiver.child("id").setValue(firebaseUser.getUid());
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("Following").child(user.getId()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId())
                            .child("Following").child(firebaseUser.getUid()).removeValue();

                    DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                            .child(firebaseUser.getUid())
                            .child(user.getId());
                    chatRef.child("id").removeValue();

                    DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                            .child(user.getId())
                            .child(firebaseUser.getUid());
                    chatRefReceiver.child("id").removeValue();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public ImageView profile_image;
        public ImageView img_on;
        public ImageView img_off;
        public TextView bio;
        public Button followBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            bio = itemView.findViewById(R.id.bio);
            followBtn = itemView.findViewById(R.id.btn_follow);
        }
    }

    private void isFollowing(String userid, Button button){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("Following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(userid).exists()){
                    button.setText("Following");
                }

                else {
                    button.setText("Follow");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
