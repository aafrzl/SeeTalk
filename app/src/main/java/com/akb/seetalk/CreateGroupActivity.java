package com.akb.seetalk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.Group;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class CreateGroupActivity extends AppCompatActivity {

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
    private FloatingActionButton createGroupBtn;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        Toolbar toolbar = findViewById(R.id.barLayout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Buat Grup");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth = firebaseAuth.getInstance();

        //init UI views
        groupIconIv = findViewById(R.id.groupIconIv);
        groupTitleEt = findViewById( R.id.groupTitleEt);
        groupDescriptionEt = findViewById( R.id.groupDescriptionEt);
        createGroupBtn = findViewById(R.id.createGroupBtn);

        storageRef = FirebaseStorage.getInstance().getReference("Group Icon");

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
        createGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            startcreategroup();
            }
        });
    }
    private void startcreategroup() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Membuat Grup...");

        String groupTitle = groupTitleEt.getText().toString().trim();
        String groupDescription = groupDescriptionEt.getText().toString().trim();

        if(TextUtils.isEmpty(groupTitle)){
            Toast.makeText(this, "Nama group tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        String timestamp =""+System.currentTimeMillis();
        if(image_uri == null){
            creategroup(""+timestamp,
                    ""+groupTitle,
                    ""+groupDescription,
                    "");
        }else{
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

                        creategroup(""+timestamp,
                                ""+groupTitle,
                                ""+groupDescription,
                                ""+mUri);
                    }else{
                        Toast.makeText(CreateGroupActivity.this, "Gagal", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(CreateGroupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void creategroup(String timestamp, String groupTitle, String groupDescription, String groupIcon) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("groupId", ""+timestamp);
        hashMap.put("groupTitle", ""+groupTitle);
        hashMap.put("groupDescription", ""+groupDescription);
        hashMap.put("groupIcon", ""+groupIcon);
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
                                        Toast.makeText(CreateGroupActivity.this, "Group berhasil dibuat", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(CreateGroupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(CreateGroupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
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
        return super.onSupportNavigateUp();
    }
}