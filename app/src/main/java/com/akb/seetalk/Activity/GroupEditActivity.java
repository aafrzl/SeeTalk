package com.akb.seetalk.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.akb.seetalk.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupEditActivity extends AppCompatActivity {

    private  String groupId;

    //image pick constants
    private static final int GalleryPick = 1;
    //picked image uri
    private Uri image_uri;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;
    StorageReference storageRef;


    //firebase auth
    private FirebaseAuth firebaseAuth;

    //UI views
    private CircleImageView groupIconIv;
    private EditText groupTitleEt, groupDescriptionEt;
    private FloatingActionButton updateGroupBtn;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);

        Toolbar toolbar= findViewById(R.id.toolbar);
        getSupportActionBar().setTitle("Edit Group");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //init UI views
        groupIconIv = findViewById(R.id.groupIconIv);
        groupTitleEt = findViewById( R.id.groupTitleEt);
        groupDescriptionEt = findViewById( R.id.groupDescriptionEt);
        updateGroupBtn = findViewById(R.id.updateGroupBtn);

        storageRef = FirebaseStorage.getInstance().getReference("Group Icon");

        groupId = getIntent().getStringExtra("groupId");
        progressDialog = new ProgressDialog(getApplicationContext()) ;
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = firebaseAuth.getInstance();
        loadGroupInfo();

        //pick image
        groupIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, GalleryPick);
            }

        });

        //handle click event
        updateGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startupdatinggroup();
            }
        });
    }

    private void startupdatinggroup() {
        //input dataa
        String groupTitle = groupTitleEt.getText().toString().trim();
        String groupDescription = groupDescriptionEt.getText().toString().trim();

        //validasi data
        if (TextUtils.isEmpty(groupTitle)){
            Toast.makeText(this,"Group title is required...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (image_uri==null){
            //update grup tanpa icon
            progressDialog.setMessage("Updating Group Info...");
            progressDialog.show();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("groupTitle", groupTitle);
            hashMap.put("groupDescription", groupDescription);


            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
            ref.child(groupId).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //updated
                            progressDialog.dismiss();
                            Toast.makeText(GroupEditActivity.this, "Group info updated...", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //update falide
                            progressDialog.dismiss();
                            Toast.makeText(GroupEditActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else {
            //update pakai ikon
            String timestamp = ""+System.currentTimeMillis();
            String fileNameAndPath = "Group_Imgs/" + "image" + timestamp;

            StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileNameAndPath);
            uploadTask = storageReference.putFile(image_uri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("groupTitle", groupTitle);
                        hashMap.put("groupDescription", groupDescription);
                        hashMap.put("groupIcon", ""+mUri);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                        ref.child(groupId).updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //updated
                                        progressDialog.dismiss();
                                        Toast.makeText(GroupEditActivity.this, "Group info updated...", Toast.LENGTH_SHORT).show();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //update falide
                                        progressDialog.dismiss();
                                        Toast.makeText(GroupEditActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }else{
                        Toast.makeText(GroupEditActivity.this, "Gagal", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(GroupEditActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }


        }

    private void updategroup(String timestamp, String groupTitle, String groupDescription, String groupIcon) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("groupId", ""+timestamp);
        hashMap.put("groupTitle", ""+groupTitle);
        hashMap.put("groupDescription", ""+groupDescription);
        hashMap.put("groupIcon", ""+groupIcon);
        hashMap.put("timestamp", ""+timestamp);
        hashMap.put("createdBy", ""+firebaseAuth.getUid());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        HashMap<String, String> hashMap1 = new HashMap<>();
                        hashMap1.put("uid", firebaseAuth.getUid());
                        hashMap1.put("role", "creator");
                        hashMap1.put("timestamp", timestamp);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
                        reference.child(timestamp).child("Participans").child(firebaseAuth.getUid())
                                .setValue(hashMap1)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        Toast.makeText(GroupEditActivity.this, "Group berhasil dibuat", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(GroupEditActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(GroupEditActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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


                            //set timestamp
                            Calendar cal = Calendar.getInstance(Locale.getDefault());
                            cal.setTimeInMillis(Long.parseLong(timestamp));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();


                            try {
                                Glide.with(getApplicationContext()).load(GroupIcon).into(groupIconIv);
                            }catch (Exception e){
                                groupIconIv.setImageResource(R.drawable.profile_img);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GalleryPick  &&  resultCode==RESULT_OK  &&  data!=null)
        {
            image_uri = data.getData();

            CropImage.activity()
                    .setAspectRatio(1, 1)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                image_uri = result.getUri();

                groupIconIv.setImageURI(image_uri);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}