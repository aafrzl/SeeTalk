package com.akb.seetalk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.akb.seetalk.Model.User;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
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

public class ProfileActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView email;
    TextInputEditText username;
    EditText bio_et;
    ImageView btnChange, edit_img;
    Button save;
    Toolbar toolbar;

    DatabaseReference reference;
    FirebaseUser fuser;
    StorageReference storageReference;
    private ProgressDialog loadingBar;

    String userid;

//    private static final int IMAGE_REQUEST = 1;
    private static final int GalleryPick = 1;


    Uri imageUri;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = findViewById(R.id.settingprofile);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Profile");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        profile_image = findViewById(R.id.profile_image);
        btnChange = findViewById(R.id.add_image);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        bio_et = findViewById(R.id.bio_et);
        loadingBar = new ProgressDialog(this);

        edit_img = findViewById(R.id.edit_image);
        save = findViewById(R.id.save_btn);

        storageReference = FirebaseStorage.getInstance().getReference().child("Profile Image");

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("User").child(fuser.getUid());

        edit_img.setOnClickListener(view1 -> {
            save.setVisibility(View.VISIBLE);
            username.setEnabled(true);
            bio_et.setEnabled(true);
            username.setSelection(username.getText().length());
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username.setEnabled(false);
                bio_et.setEnabled(false);

                reference.child("bio").setValue(bio_et.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //   Toast.makeText(getContext(),"Profile Updated...", Toast.LENGTH_SHORT);
                        } else {
                            //   Toast.makeText(getContext(),"Unable to Save...", Toast.LENGTH_SHORT);

                        }
                    }
                });

                reference.child("username").setValue(username.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Profile di update", Toast.LENGTH_SHORT);
                        } else {
                            Toast.makeText(ProfileActivity.this, "Tidak dapat menyimpan", Toast.LENGTH_SHORT);

                        }
                    }
                });
            }
        });
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;
                username.setText(user.getUsername());
                email.setText(user.getEmail());
                bio_et.setText(user.getBio());
                if (this == null) {
                    return;
                }
                if (user.getImageURL().equals("default")) {
                    profile_image.setImageResource(R.drawable.profile_img);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });
    }


//    private void openImage() {
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(intent, IMAGE_REQUEST);
//    }

//    private String getFileExtension(Uri uri) {
//        ContentResolver contentResolver = this.getContentResolver();
//        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
//        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));
//    }

    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading...");
        pd.show();

        File actualImage = new File(imageUri.getPath());
        try {
            Bitmap compressedImage = new Compressor(this)
                    .setMaxWidth(640)
                    .setMaxHeight(480)
                    .setQuality(75)
                    .compressToBitmap(actualImage);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            compressedImage.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] final_image = baos.toByteArray();

            if (imageUri != null) {
                final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                        + ".jpg");

                uploadTask = fileReference.putBytes(final_image);
                uploadTask = fileReference.putFile(imageUri);
                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return fileReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            String mUri = downloadUri.toString();

                            reference = FirebaseDatabase.getInstance().getReference("User").child(fuser.getUid());
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("imageURL", "" + mUri);
                            reference.updateChildren(map);
                            pd.dismiss();
                        } else {
                            Toast.makeText(ProfileActivity.this, "Gagal", Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                });
            } else {
                Toast.makeText(ProfileActivity.this, "Tidak ada item yang dipilih", Toast.LENGTH_SHORT).show();
            }

        }catch (IOException e ){
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GalleryPick  &&  resultCode==RESULT_OK  &&  data!=null)
        {
            imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();

                if(uploadTask != null && uploadTask.isInProgress()){
                    Toast.makeText(ProfileActivity.this,"Upload in Progres..,",Toast.LENGTH_SHORT).show();
                }else{
                    uploadImage();
                }
            }
        }
    }
}