package com.akb.seetalk.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;

import com.akb.seetalk.Adapter.ParticipantAddAdapter;
import com.akb.seetalk.Model.Group;
import com.akb.seetalk.Model.User;
import com.akb.seetalk.Notifications.Data;
import com.akb.seetalk.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupInfoActivity extends AppCompatActivity {

    private String groupId;
    private String myGroupRole="";

    FirebaseAuth firebaseAuth;

    private CircleImageView groupIcon;
    private TextView descriptionTv, createdByTv, editGroupTv, addMemberTv, leaveGroupTv, memberList;

    private RecyclerView memberRv;

    private ArrayList<User> userArrayList;
    private ParticipantAddAdapter participantAddAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        Toolbar toolbar= findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //get id group
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");

        groupIcon = findViewById(R.id.groupIconIv);
        descriptionTv = findViewById(R.id.descriptionTv);
        createdByTv = findViewById(R.id.createdByTv);
        editGroupTv = findViewById(R.id.editGroupTv);
        addMemberTv = findViewById(R.id.addMemberTv);
        leaveGroupTv = findViewById(R.id.leaveGroupTv);
        memberList = findViewById(R.id.memberList);

        memberRv = findViewById(R.id.memberRv);
//        recycler_view.setHasFixedSize(true);
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
//        linearLayoutManager.setStackFromEnd(true);
//        recycler_view.setLayoutManager(linearLayoutManager);

        firebaseAuth = FirebaseAuth.getInstance();

        loadGroupInfo();
        loadMyGroupRole();

        addMemberTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfoActivity.this, GroupInfoActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

    }

    private void loadMyGroupRole() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participans").orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            myGroupRole = ""+ds.child("role").getValue();
                            getSupportActionBar().setTitle(firebaseAuth.getCurrentUser().getDisplayName()+ "("+myGroupRole+")");

                            if (myGroupRole.equals("member")){
                                editGroupTv.setVisibility(View.GONE);
                                addMemberTv.setVisibility(View.GONE);
                                leaveGroupTv.setText("Keluar Grup");

                            }else if (myGroupRole.equals("admin")){
                                editGroupTv.setVisibility(View.GONE);
                                addMemberTv.setVisibility(View.VISIBLE);
                                leaveGroupTv.setText("Keluar Grup");

                            }else if(myGroupRole.equals("creator")){
                                editGroupTv.setVisibility(View.VISIBLE);
                                addMemberTv.setVisibility(View.VISIBLE);
                                leaveGroupTv.setText("Hapus Grup");

                            }
                        }

                        loadListMember();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadListMember() {
        userArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participans").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userArrayList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    //get uid dari Group > participans
                    String uid =""+ds.child("uid").getValue();

                    //get info user dengan uid yang sudah di get
                    DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("User");
                    reference1.orderByChild("id").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot ds : snapshot.getChildren()){
                                User user = ds.getValue(User.class);
                                userArrayList.add(user);
                            }
                            participantAddAdapter = new ParticipantAddAdapter(GroupInfoActivity.this, userArrayList, groupId, myGroupRole);
                            memberRv.setAdapter(participantAddAdapter);

                            memberList.setText("Member saat ini : "+userArrayList.size());
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

    private void loadGroupInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            String createBy = ""+ds.child("createdBy").getValue();
                            String GroupTitle = ""+ds.child("groupTitle").getValue();
                            String GroupDescription = ""+ds.child("groupDescription").getValue();
                            String GroupIcon = ""+ds.child("groupIcon").getValue();
                            String timestamp = ""+ds.child("timestamp").getValue();

                            getSupportActionBar().setTitle(GroupTitle);
                            descriptionTv.setText(GroupDescription);

                            //set timestamp
                            Calendar cal = Calendar.getInstance(Locale.getDefault());
                            cal.setTimeInMillis(Long.parseLong(timestamp));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

                            loadCreatorInfo(dateTime, createBy);

                            try {
                                Glide.with(getApplicationContext()).load(GroupIcon).into(groupIcon);
                            }catch (Exception e){
                                groupIcon.setImageResource(R.drawable.profile_img);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadCreatorInfo(String dateTime, String createBy){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("User");
        reference.orderByChild("id").equalTo(createBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    String username =""+ds.child("username").getValue();
                    createdByTv.setText("Dibuat pada "+username+" on "+dateTime);
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