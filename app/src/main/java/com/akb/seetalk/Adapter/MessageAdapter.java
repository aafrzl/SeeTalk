package com.akb.seetalk.Adapter;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.akb.seetalk.Model.Chat;
import com.akb.seetalk.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Chat> mChat;
    private String imageurl;

    private ClipboardManager myClipboard;
    private ClipData myClip;

    FirebaseUser fuser;

    public MessageAdapter(Context mContext, List<Chat> mChat, String imageurl){
        this.mChat = mChat;
        this.mContext = mContext;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {

            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, final int position) {

        Chat chat = mChat.get(position);

        holder.show_message.setText(chat.getMessage());

        if(chat.getTime()!=null && !chat.getTime().trim().equals("")){
            holder.time_tv.setText(holder.convertTime(chat.getTime()));
        }

        if(imageurl.equals("default")){
            holder.profile_image.setImageResource(R.drawable.profile_img);
        } else {
            Glide.with(mContext).load(imageurl).into(holder.profile_image);
        }

        if(position == mChat.size() -1){
            if (chat.isIsseen()){
                holder.txt_seen.setText("Dilihat");
            } else {
                holder.txt_seen.setText("Terkirim");
            }
        } else {
            holder.txt_seen.setVisibility(View.GONE);
        }

        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //delete message
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Hapus Pesan");
                builder.setMessage("Anda yakin mau hapus pesan?");
                //delete button
                builder.setPositiveButton("Hapus", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        deleteMessage(position);

                    }
                });
                //batal hapus
                builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //batal hapus pesan
                        dialog.dismiss();

                    }
                });
                builder.create().show();
            }

            private void deleteMessage(int position) {
                String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                String msgTimeStamp = mChat.get(position).getTime();
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
                Query query = dbRef.orderByChild("time").equalTo(msgTimeStamp);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            //delete only his message
                            if(ds.child("sender").getValue().equals(myUID)){

                                //set value of message "Pesan ini telah dihapus"
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("message", "Pesan ini telah dihapus");
                                ds.getRef().updateChildren(hashMap);

                                Toast.makeText(mContext, "Pesan Terhapus",Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(mContext, "Kamu hanya bisa hapus pesan kamu sendiri",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        myClipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);

        holder.show_message.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String text;
                text = holder.show_message.getText().toString();

                myClip = ClipData.newPlainText("text", text);
                myClipboard.setPrimaryClip(myClip);

                Toast.makeText(mContext, "Berhasil dicopy", Toast.LENGTH_SHORT).show();

                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{


        public TextView show_message;
        public ImageView profile_image;
        public TextView txt_seen;
        public TextView time_tv;
        public RelativeLayout messageLayout; //untuk click listener

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
            txt_seen = itemView.findViewById(R.id.txt_seen);
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
        if(mChat.get(position).getSender().equals(fuser.getUid())){
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }


}
