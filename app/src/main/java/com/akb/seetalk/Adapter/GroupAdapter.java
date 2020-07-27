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

import com.akb.seetalk.GroupChatActivity;
import com.akb.seetalk.Model.Group;
import com.akb.seetalk.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

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

        holder.groupTitleTv.setText(groupTitle);

            if(group.equals("default")){
                holder.groupIconIv.setImageResource(R.drawable.profile_img);
            }else {
                Glide.with(context.getApplicationContext()).load(group.getGroupIcon()).into(holder.groupIconIv);
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
