package com.akb.seetalk.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.akb.seetalk.Model.User;
import com.akb.seetalk.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ParticipantAddAdapter extends RecyclerView.Adapter<ParticipantAddAdapter.ViewHolder>{

    private Context context;
    private List<User> userList;

    private String groupId, myGroupRole; //creator/admin/member

    public ParticipantAddAdapter(Context context, List<User> userList, String groupId, String myGroupRole) {
        this.context = context;
        this.userList = userList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.participant_add_item, parent, false);
        return new ParticipantAddAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //get data
        User modeluser = userList.get(position);

        String username = modeluser.getUsername();
        String bio = modeluser.getBio();
        String image = modeluser.getImageURL();
        String id = modeluser.getId();

        //set data
        holder.username.setText(username);
        holder.bio.setText(bio);

        if(image.equals("default")){
            holder.profile_image.setImageResource(R.drawable.profile_img);
        }else{
            Glide.with(context).load(image).into(holder.profile_image);
        }

        CheckUserAlreadyExists(modeluser, holder);

        //onclick
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Cek jika user sudah ditambahkan atau belum
                *  jika ditambahkan : tampilkan remove-member/make-admin- option (Admin tidak bisa mengganti role kreator)
                * jika belum ditambahkan, tampilkan tambah member option */

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
                reference.child(groupId).child("Participans").child(id)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    //user exists/member
                                    String HisprevRole = ""+snapshot.child("role").getValue();

                                    //option
                                    String[] options;

                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Pilih Role yang akan diberikan");
                                    if(myGroupRole.equals("creator")){
                                        if(HisprevRole.equals("admin")){
                                            //im creator, he is admin
                                            options = new String[]{"Hapus Admin", "Hapus User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //handler item click
                                                    if(which ==0){
                                                        //hapus admin
                                                        removeAdmin(modeluser);
                                                    }else{
                                                        //hapus user
                                                        removeUser(modeluser);
                                                    }
                                                }
                                            }).show();
                                        } else if (HisprevRole.equals("member")){
                                            //im creator, he is member
                                            options = new String[]{"Tambah Admin", "Hapus User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //handler item click
                                                    if(which ==0){
                                                        //buat admin
                                                        MakeAdmin(modeluser);
                                                    }else{
                                                        //hapus user
                                                        removeUser(modeluser);
                                                    }
                                                }
                                            }).show();
                                        }
                                    }
                                    else if(myGroupRole.equals("admin")){
                                        if(HisprevRole.equals("creator")){
                                            //im admin, he is creator
                                            Toast.makeText(context, "Pemilik Grup Chat", Toast.LENGTH_SHORT).show();
                                        }
                                        else if (HisprevRole.equals("admin")){
                                            //im admin, he is admin to
                                            options = new String[]{"Hapus User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //handler item click
                                                    if(which ==0){
                                                        //buat admin
                                                        removeUser(modeluser);
                                                    }
                                                }
                                            }).show();
                                        }
                                        else if (HisprevRole.equals("member")){
                                            //im admin, he is member
                                            options = new String[]{"Hapus User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //handler item click
                                                    if(which ==0){
                                                        //buat admin
                                                        removeUser(modeluser);
                                                    }
                                                }
                                            }).show();
                                        }
                                    }

                                }
                                else {
                                    // user doesn't exists / not - participant : add
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Tambah Member")
                                            .setMessage("Anda yakin akan menambahkan Pengguna ini ke dalam grup ini?")
                                            .setPositiveButton("Tambah", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //add user
                                                    addMember(modeluser);
                                                }
                                            })
                                            .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();

                                                }
                                            }).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

            }
        });

    }

    private void addMember(User modeluser) {
        //setup user data - add user in group
        String timestamp =""+System.currentTimeMillis();

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", modeluser.getId());
        hashMap.put("role", "member");
        hashMap.put("timestamp",""+timestamp);

        //add user in Groups>GroupId>Participans
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participans").child(modeluser.getId()).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void MakeAdmin(User modeluser) {
        //setup data - member to admin
        String timestamp =""+System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "admin"); //role admin update

        //update in db
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participans").child(modeluser.getId()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Member ini sekarang menjadi admin...", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void removeUser(User modeluser) {
        //remove member from grup
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participans").child(modeluser.getId()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Berhasil dihapus", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeAdmin(User modeluser) {
        //setup data = remove admin - member
        String timestamp =""+System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "member"); //role admin update

        //update in db
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participans").child(modeluser.getId()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Member ini bukan lagi admin...", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void CheckUserAlreadyExists(User modeluser, ViewHolder holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participans").child(modeluser.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            //already exists
                            String hisRole = ""+snapshot.child("role").getValue();
                            holder.status.setText(hisRole);
                        }else{
                            //doest't exits
                            holder.status.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private CircleImageView profile_image;
        private TextView username, bio, status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profile_image = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.username);
            bio = itemView.findViewById(R.id.bio);
            status = itemView.findViewById(R.id.statusUser);
        }
    }

}
