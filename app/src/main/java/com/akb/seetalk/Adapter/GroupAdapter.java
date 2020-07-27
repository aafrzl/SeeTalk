package com.akb.seetalk.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.akb.seetalk.Activity.GroupChatActivity;
import com.akb.seetalk.Model.Group;
import com.akb.seetalk.R;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.HolderGroupChatList>{

    private Context context;
    private ArrayList<Group> mGroup;

    public GroupAdapter(Context context, ArrayList<Group> mGroup) {
        this.context = context;
        this.mGroup = mGroup;
    }

    @NonNull
    @Override
    public HolderGroupChatList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grupchat, parent, false);
        return new GroupAdapter.HolderGroupChatList(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChatList holder, int position) {

        Group group = mGroup.get(position);
        String groupId = group.getGroupId();
        String groupIcon = group.getGroupIcon();
        String groupTitle = group.getGroupTitle();

        holder.usernameTv.setText("");
        holder.timeTv.setText("");
        holder.messageTv.setText("");

        //load pesan terakhir dan waktunya
        loadLastMessage(group, holder);

        holder.groupTitleTv.setText(groupTitle);

        try {
            Glide.with(context).load(groupIcon).into(holder.groupIconIv);
        }catch (Exception e){
            holder.groupIconIv.setImageResource(R.drawable.profile_img);
        }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, GroupChatActivity.class);
                    intent.putExtra("groupId", groupId);
                    context.startActivity(intent);
                }
            });

    }

    private void loadLastMessage(Group group, HolderGroupChatList holder) {
        //get last message from group
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(group.getGroupId()).child("Messages").limitToLast(1) //get from last item
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            String message = ""+ds.child("message").getValue();
                            String timestamp = ""+ds.child("timestamp").getValue();
                            String sender = ""+ds.child("sender").getValue();

                            //convert time
                            Calendar cal = Calendar.getInstance(Locale.getDefault());
                            cal.setTimeInMillis(Long.parseLong(timestamp));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm a",cal).toString();

                            holder.messageTv.setText(message);
                            holder.timeTv.setText(dateTime);

                            //get info from sender
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User");
                            ref.orderByChild("id").equalTo(sender)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds : snapshot.getChildren()){
                                                String username = ""+ds.child("username").getValue();
                                                holder.usernameTv.setText(username);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return mGroup.size();
    }

    class HolderGroupChatList extends RecyclerView.ViewHolder{

        public CircleImageView groupIconIv;
        public TextView groupTitleTv, usernameTv, messageTv, timeTv;

        public HolderGroupChatList(@NonNull View itemView) {
            super(itemView);

            groupIconIv = itemView.findViewById(R.id.groupIconIv);
            groupTitleTv = itemView.findViewById(R.id.groupTitleTv);
            usernameTv = itemView.findViewById(R.id.usernameTv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);

        }
    }
}
