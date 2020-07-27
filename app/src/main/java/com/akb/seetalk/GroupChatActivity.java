package com.akb.seetalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.akb.seetalk.Adapter.GroupChatAdapter;
import com.akb.seetalk.Model.Group;
import com.akb.seetalk.Model.GroupChat;
import com.akb.seetalk.Notifications.Data;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity {

    private String groupId;

    private FirebaseAuth firebaseAuth;

    private CircleImageView groupIconIv;
    private TextView groupTitleTv;
    private ImageButton btnsend;
    private TextInputEditText sendText;
    private RecyclerView recyclerView;

    GroupChatAdapter groupChatAdapter;
    List<GroupChat> mGroupChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);



        //get id group
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");

        groupIconIv = findViewById(R.id.groupIconIv);
        groupTitleTv = findViewById(R.id.groupTitleTv);
        btnsend = findViewById(R.id.btn_send);
        sendText = findViewById(R.id.text_send);
        recyclerView = findViewById(R.id.recycler_view);

        Toolbar toolbar= findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        loadGroupInfo();
        loadGroupMessage();

        btnsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = sendText.getText().toString();
                String timestamp = String.valueOf(System.currentTimeMillis());
                if(TextUtils.isEmpty(message)){
                    Toast.makeText(GroupChatActivity.this, "Kamu tidak bisa mengirim pesan kosong!", Toast.LENGTH_SHORT).show();
                }else{
                    //kirim pesan
                    sendMessage(message,timestamp);
                }
                sendText.setText("");
            }

        });

    }

    private void loadGroupMessage() {
        mGroupChat = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ChatGroup");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mGroupChat.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    GroupChat groupChat = ds.getValue(GroupChat.class);
                    mGroupChat.add(groupChat);
                }
                groupChatAdapter = new GroupChatAdapter(GroupChatActivity.this, mGroupChat);
                recyclerView.setAdapter(groupChatAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message, String timestamp) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", firebaseAuth.getUid());
        hashMap.put("message", message);
        hashMap.put("timestamp", timestamp);

        reference.child("ChatGroup").push().setValue(hashMap);

    }

    private void loadGroupInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups").child(groupId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Group group = snapshot.getValue(Group.class);
                groupTitleTv.setText(group.getGroupTitle());
                try {
                    Glide.with(getApplicationContext()).load(group.getGroupIcon()).into(groupIconIv);
                }catch (Exception e){
                    groupIconIv.setImageResource(R.drawable.profile_img);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}