package com.akb.seetalk.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewPropertyAnimatorListenerAdapter;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.akb.seetalk.Adapter.GroupAdapter;
import com.akb.seetalk.Model.Group;
import com.akb.seetalk.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class GrupFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseAuth firebaseAuth;

    private ArrayList<Group> groupArrayList;
    private GroupAdapter groupAdapter;

    public GrupFragment(){
        //constructor kosong
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        menu.findItem(R.id.addpost).setVisible(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_grup, container, false);
        
        recyclerView = view.findViewById(R.id.recycler_view);
        
        firebaseAuth = firebaseAuth.getInstance();
        
        loadGroupChatsList();
        
        return view;
    }

    private void loadGroupChatsList() {
        groupArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupArrayList.clear();
                for(DataSnapshot ds :snapshot.getChildren()){
                    //jika user uid tersedia di articipants list maka group menampilkan Group chat itu
                    if(ds.child("Participans").child(firebaseAuth.getUid()).exists()){
                        Group group = ds.getValue(Group.class);
                        groupArrayList.add(group);
                    }
                }
                groupAdapter = new GroupAdapter(getContext(), groupArrayList);
                recyclerView.setAdapter(groupAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

//    private void searchGroupChat(final String query) {
//        groupArrayList = new ArrayList<>();
//
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                groupArrayList.clear();
//                for(DataSnapshot ds :snapshot.getChildren()){
//                    //jika user uid tersedia di articipants list maka group menampilkan Group chat itu
//                    if(ds.child("Participants").child(firebaseAuth.getUid()).exists()){
//
//                        //search by title grouple
//                        if(ds.child("groupTitle").toString().toLowerCase().contains(query.toLowerCase())){
//                            Group group = ds.getValue(Group.class);
//                            groupArrayList.add(group);
//                        }
//                    }
//                }
//                groupAdapter = new GroupAdapter(getContext(), groupArrayList);
//                recyclerView.setAdapter(groupAdapter);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
}