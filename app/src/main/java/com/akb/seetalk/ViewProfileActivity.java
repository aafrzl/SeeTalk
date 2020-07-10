package com.akb.seetalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.akb.seetalk.Model.User;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ViewProfileActivity extends AppCompatActivity {

     DatabaseReference reference, ChatRequestRef, chatlist;

     TextView username, bio_et;
     ImageView profile_img;
     Button kirimpesan, batalkanpesan;
     FirebaseAuth mAuth;
     Toolbar toolbar;

    Intent intent;
    String userid, Current_State, senderUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        toolbar = findViewById(R.id.settingprofile);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        mAuth = FirebaseAuth.getInstance();

        intent = getIntent();
        userid = intent.getStringExtra("userid");

        profile_img = findViewById(R.id.view_profile_image);
        username = findViewById(R.id.view_username);
        bio_et = findViewById(R.id.view_bio_et);
//        kirimpesan = findViewById(R.id.addkontak);
//        batalkanpesan = findViewById(R.id.cancelMessage);
        Current_State = "new";

        senderUserID = mAuth.getCurrentUser().getUid();

        reference = FirebaseDatabase.getInstance().getReference("User");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference("Chat Request");
        chatlist = FirebaseDatabase.getInstance().getReference("Chatlist");

        RetrieveUserInfo();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void RetrieveUserInfo() {

        reference.child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                username.setText(user.getUsername());
                bio_et.setText(user.getBio());
                if(user.getImageURL().equals("default")){
                    profile_img.setImageResource(R.drawable.profile_img);
                }else{
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_img);
                }

//                ManageChatRequest();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

//    private void ManageChatRequest() {
//        ChatRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener()
//        {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot)
//            {
//                if(snapshot.hasChild(userid))
//                {
//                    String request_type = snapshot.child(userid).child("request_type").getValue().toString();
//                    if(request_type.equals("send"))
//                    {
//                        Current_State = "request_sent";
//                        kirimpesan.setText("Batal Permintaan");
//                    }else if(request_type.equals("received")){
//                        Current_State = "request_received";
//                        kirimpesan.setText("Terima Kontak");
//
//                        batalkanpesan.setVisibility(View.VISIBLE);
//                        batalkanpesan.setEnabled(true);
//
//                        batalkanpesan.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                CancelChatRequest();
//                            }
//                        });
//                    }
//                }else {
//                    chatlist.child(senderUserID)
//                            .addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                    if(snapshot.hasChild(userid)){
//                                        Current_State = "friends";
//                                        kirimpesan.setText("Hapus Kontak ini");
//                                    }
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError error) {
//
//                                }
//                            });
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//            if(!senderUserID.equals(userid))
//            {
//                kirimpesan.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        kirimpesan.setEnabled(false);
//                        if(Current_State.equals("new")){
//                            SendChatRequest();
//                        }
//                        if(Current_State.equals("request_sent")){
//                            CancelChatRequest();
//                        }
//                        if(Current_State.equals("request_received")){
//                            AcceptChatRequest();
//                        }
//                        if(Current_State.equals("friends")){
//                            RemoveSpecificContact();
//                        }
//                    }
//                });
//            }else{
//                kirimpesan.setVisibility(View.INVISIBLE);
//            }
//    }
//
//    private void SendChatRequest() {
//        ChatRequestRef.child(senderUserID).child(userid)
//                .child("request_type").setValue("send")
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if(task.isSuccessful())
//                        {
//                            ChatRequestRef.child(userid).child(senderUserID)
//
//                            .child("request_type").setValue("received")
//
//                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task)
//                                {
//                                    if(task.isSuccessful())
//                                    {
//                                        kirimpesan.setEnabled(true);
//                                        Current_State = "request_type";
//                                        kirimpesan.setText("Batal Permintaan");
//                                    }
//                                }
//                            });
//                        }
//                    }
//                });
//             }
//
//    private void CancelChatRequest() {
//        ChatRequestRef.child(senderUserID).child(userid)
//        .removeValue()
//        .addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if(task.isSuccessful()){
//                    ChatRequestRef.child(userid).child(senderUserID)
//                            .removeValue()
//                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//                                    if(task.isSuccessful()){
//                                        kirimpesan.setEnabled(true);
//                                        Current_State = "new";
//                                        kirimpesan.setText("Tambah Kontak");
//
//                                        batalkanpesan.setVisibility(View.INVISIBLE);
//                                        batalkanpesan.setEnabled(false);
//                                    }
//                                }
//                            });
//                }
//            }
//        });
//    }
//
//
//    private void AcceptChatRequest() {
//        chatlist.child(senderUserID).child(userid).child("id")
//                .setValue(userid)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if(task.isSuccessful()){
//                            chatlist.child(userid).child(senderUserID)
//                                    .child("id").setValue(senderUserID)
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if(task.isSuccessful())
//                                            {
//                                                ChatRequestRef.child(senderUserID).child(userid)
//                                                        .removeValue()
//                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                            @Override
//                                                            public void onComplete(@NonNull Task<Void> task) {
//                                                                if(task.isSuccessful()){
//                                                                    ChatRequestRef.child(userid).child(senderUserID)
//                                                                            .removeValue()
//                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                                                @Override
//                                                                                public void onComplete(@NonNull Task<Void> task) {
//                                                                                    kirimpesan.setEnabled(true);
//                                                                                    Current_State = "friends";
//                                                                                    kirimpesan.setText("Hapus Kontak ini");
//
//                                                                                    batalkanpesan.setVisibility(View.INVISIBLE);
//                                                                                    batalkanpesan.setEnabled(false);
//                                                                                }
//                                                                            });
//                                                                }
//                                                            }
//                                                        });
//                                            }
//                                        }
//                                    });
//                        }
//                    }
//                });
//
//    }
//
//
//    private void RemoveSpecificContact() {
//        chatlist.child(senderUserID).child(userid)
//                .removeValue()
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if(task.isSuccessful()){
//                            chatlist.child(userid).child(senderUserID)
//                                    .removeValue()
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if(task.isSuccessful()){
//                                                kirimpesan.setEnabled(true);
//                                                Current_State = "new";
//                                                kirimpesan.setText("Tambah Kontak");
//
//                                                batalkanpesan.setVisibility(View.INVISIBLE);
//                                                batalkanpesan.setEnabled(false);
//                                            }
//                                        }
//                                    });
//                        }
//                    }
//                });
//
//    }

}