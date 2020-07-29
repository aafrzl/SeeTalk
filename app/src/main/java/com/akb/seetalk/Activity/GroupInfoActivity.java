package com.akb.seetalk.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.akb.seetalk.Adapter.ParticipantAddAdapter;
import com.akb.seetalk.Model.Group;
import com.akb.seetalk.Model.User;
import com.akb.seetalk.Notifications.Data;
import com.akb.seetalk.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
        firebaseAuth = FirebaseAuth.getInstance();

        loadGroupInfo();
        loadMyGroupRole();

        addMemberTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfoActivity.this, GroupParticipantAddActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        editGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfoActivity.this,GroupEditActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

                leaveGroupTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //kalau pengguna adalah member/admin: leave group
                        //kalau pengguna adalah creator maka delete grup
                        String dialogTitle = "";
                        String dialogDescription = "";
                        String positiveButtonTitle = "";
                        if (myGroupRole.equals("creator")) {
                            dialogTitle = "Hapus Grup";
                            dialogDescription = "Apakah kamu ingin menghapus grup secara permanen?";
                            positiveButtonTitle = "HAPUS";
                        } else {
                            dialogTitle = "Tinggalkan Grup";
                            dialogDescription = "Apakah kamu ingin meninggalkan grup?";
                            positiveButtonTitle = "TINGGALKAN";
                        }
                        Context context;
                        AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfoActivity.this);
                        builder.setTitle(dialogTitle)
                                .setMessage(dialogDescription)
                                .setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (myGroupRole.equals("creator")) {
                                            //saya creator grup, hapus grup
                                            deleteGroup();
                                        } else {
                                            //saya bukan creator, tinggalkan grup
                                            leaveGroup();
                                        }
                                    }
                                })
                                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    }
                });

    }

    private void leaveGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participans").child(firebaseAuth.getUid())
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //group left succesfully
                        Toast.makeText(GroupInfoActivity.this, "Berhasil keluar dari grup...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupInfoActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to leave group
                        Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //group berhasil dihapus
                        Toast.makeText(GroupInfoActivity.this, "Grup Berhasil Dihapus...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupInfoActivity.this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //gagal hapus grup
                        Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    createdByTv.setText("Dibuat oleh "+username+" pada "+dateTime);
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

    