package com.akb.seetalk.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.akb.seetalk.Model.GroupChat;
import com.akb.seetalk.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<GroupChat> mGroupChat;

    FirebaseUser fuser;

    public GroupChatAdapter(Context mContext, List<GroupChat> mGroupChat) {
        this.mContext = mContext;
        this.mGroupChat = mGroupChat;
    }



    @NonNull
    @Override
    public GroupChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_chat_group_right, parent, false);
            return new GroupChatAdapter.ViewHolder(view);
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_chat_group_left, parent, false);
        return new GroupChatAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupChatAdapter.ViewHolder holder, int position) {
        GroupChat model = mGroupChat.get(position);

        //set data
        holder.show_message.setText(model.getMessage());

        if(model.getTimestamp()!=null && !model.getTimestamp().trim().equals("")){
            holder.time_tv.setText(holder.convertTime(model.getTimestamp()));
        }

        setProfileImage(model, holder);

    }


    private void setProfileImage(GroupChat model, ViewHolder holder) {
        //get info sender from uid
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("User");
        reference.orderByChild("id").equalTo(model.getSender()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    String imageurl=""+ds.child("imageURL").getValue();

                    if(imageurl.equals("default")){
                        holder.profile_image.setImageResource(R.drawable.profile_img);
                    }else{
                        Glide.with(mContext).load(imageurl).into(holder.profile_image);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mGroupChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView show_message;
        public ImageView profile_image;
        public TextView txt_seen;
        public TextView time_tv;
        public RelativeLayout messageLayout; //untuk click listener

        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
            txt_seen = itemView.findViewById(R.id.txt_seen); // nanti
            time_tv = itemView.findViewById(R.id.time_tv);
            messageLayout = itemView.findViewById(R.id.messageLayout);
        }

        public String convertTime(String time){
            SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
            String dateString = formatter.format(new Date(Long.parseLong(time)));
            return dateString;
        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if(mGroupChat.get(position).getSender().equals(fuser.getUid())){
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}
