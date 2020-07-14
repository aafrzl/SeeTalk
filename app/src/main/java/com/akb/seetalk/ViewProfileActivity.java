package com.akb.seetalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.akb.seetalk.Adapter.ViewFotoAdapter;
import com.akb.seetalk.Model.Post;
import com.akb.seetalk.Model.User;
import com.akb.seetalk.Notifications.Data;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ViewProfileActivity extends AppCompatActivity {

     DatabaseReference reference;

     TextView username, bio_et, posts, following, followers;
     ImageView profile_img;
     ImageButton my_fotos;
     Button FollowBtn;

     FirebaseAuth mAuth;
     FirebaseUser firebaseUser;
     Toolbar toolbar;

     RecyclerView recyclerView;
     ViewFotoAdapter viewFotoAdapter;
     List<Post> postList;

    Intent intent;
    String userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        toolbar = findViewById(R.id.settingprofile);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        intent = getIntent();
        userid = intent.getStringExtra("userid");

        profile_img = findViewById(R.id.view_profile_image);
        username = findViewById(R.id.view_username);
        posts = findViewById(R.id.posts);
        following = findViewById(R.id.following);
        followers = findViewById(R.id.followers);
        my_fotos = findViewById(R.id.my_fotos);
        FollowBtn = findViewById(R.id.FollowBtn);
        bio_et = findViewById(R.id.view_bio_et);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        recyclerView.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<>();
        viewFotoAdapter = new ViewFotoAdapter(getApplicationContext(), postList);
        recyclerView.setAdapter(viewFotoAdapter);

        reference = FirebaseDatabase.getInstance().getReference("User");

        RetrieveUserInfo();
        getFollowers();
        getNrPosts();
        setfotos();

        if(userid.equals(firebaseUser.getUid())){
            FollowBtn.setText("Follow");
        }else{
            checkFollow();
        }

        FollowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btn = FollowBtn.getText().toString();

                if(btn.equals("Follow")){

                    //setvalue ke database follow
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("Following").child(userid).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(userid)
                            .child("Followers").child(firebaseUser.getUid()).setValue(true);

                    //add ke fragment chatlist
                    DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                            .child(firebaseUser.getUid())
                            .child(userid);
                    chatRef.child("id").setValue(userid);

                    DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                            .child(userid)
                            .child(firebaseUser.getUid());
                    chatRefReceiver.child("id").setValue(firebaseUser.getUid());

                }else if(btn.equals("Following")){
                    //setvalue ke database follow
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("Following").child(userid).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(userid)
                            .child("Followers").child(firebaseUser.getUid()).removeValue();

                    //add ke fragment chatlist
                    DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                            .child(firebaseUser.getUid())
                            .child(userid);
                    chatRef.child("id").removeValue();

                    DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                            .child(userid)
                            .child(firebaseUser.getUid());
                    chatRefReceiver.child("id").removeValue();

                }

            }
        });
    }

    private void setfotos() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Post post = ds.getValue(Post.class);
                    if(post.getPublisher().equals(userid)){
                        postList.add(post);
                    }
                }
                Collections.reverse(postList);
                viewFotoAdapter.notifyDataSetChanged();
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
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void checkFollow() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("Following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(userid).exists()){
                    FollowBtn.setText("Following");
                }else{
                    FollowBtn.setText("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(userid).child("Followers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followers.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Follow")
                .child(userid).child("Following");
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                following.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getNrPosts(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    if (post.getPublisher().equals(userid)){
                        i++;
                    }
                }
                posts.setText(""+i);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}