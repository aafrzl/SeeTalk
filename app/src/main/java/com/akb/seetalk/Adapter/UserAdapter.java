package com.akb.seetalk.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.akb.seetalk.Activity.MessageActivity;
import com.akb.seetalk.Model.Chat;
import com.akb.seetalk.Model.User;
import com.akb.seetalk.R;
import com.akb.seetalk.Activity.ViewProfileActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

    public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

        private Context mContext;
        private List<User> mUsers;
        private boolean ischat;
        private OnItemClick onItemClick;

        String theLastMessage;

        public UserAdapter(Context mContext, List<User>mUsers, boolean ischat, OnItemClick onItemClick){
            this.mUsers = mUsers;
            this.mContext = mContext;
            this.ischat = ischat;
            this.onItemClick = onItemClick;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
            return new UserAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            User user = mUsers.get(position);
            holder.username.setText(user.getUsername());
            if(user.getImageURL().equals("default")){
                holder.profile_image.setImageResource(R.drawable.profile_img);
            } else {
                Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
            }

            if (ischat){
                lastMessage(user.getId(), holder.last_msg);
            } else {
                holder.last_msg.setVisibility(View.GONE);
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
                    Intent intent = new Intent(mContext, MessageActivity.class);
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

        }

        @Override
        public int getItemCount() {
            return mUsers.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{

            public TextView username;
            public ImageView profile_image;
            public ImageView img_on;
            public ImageView img_off;
            public TextView last_msg;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                username = itemView.findViewById(R.id.username);
                profile_image = itemView.findViewById(R.id.profile_image);
                img_on = itemView.findViewById(R.id.img_on);
                img_off = itemView.findViewById(R.id.img_off);
                last_msg = itemView.findViewById(R.id.last_msg);
            }
        }

        //cek jika ada pesan terakhir
        private void lastMessage(String userid, TextView last_msg){
            theLastMessage = "default";
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Chat chat = snapshot1.getValue(Chat.class);
                        if (firebaseUser != null) {
                            assert chat != null;
                            assert firebaseUser != null;
                            if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ||
                                    chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())) {
                                theLastMessage = chat.getMessage();
                            }
                        }
                    }
                    switch (theLastMessage){
                        case "default":
                            last_msg.setText("");
                            break;

                        default:
                            last_msg.setText(theLastMessage);
                            break;
                    }
                    theLastMessage = "default";
                }


                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }
