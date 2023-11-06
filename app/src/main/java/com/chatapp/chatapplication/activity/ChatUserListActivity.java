package com.chatapp.chatapplication.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.chatapp.chatapplication.R;
import com.chatapp.chatapplication.adapter.ChatUserListAdapter;
import com.chatapp.chatapplication.model.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatUserListActivity extends AppCompatActivity {

    private ArrayList<Users> usersList;
    private ChatUserListAdapter chatUserListAdapter;
    private RecyclerView recyclerView;
    private  FirebaseUser firebaseUser;
    private EditText edtSearchUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_user_list);

        initView();
        readUserList();
    }

    private void initView(){
        usersList = new ArrayList<>();
        recyclerView = findViewById(R.id.chatUserListRecyclerView);
        edtSearchUser = findViewById(R.id.edtSearch);

        edtSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUserList(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void searchUserList(String s){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("search")
                .startAt(s)
                .endAt(s+"|ut8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Users user = dataSnapshot.getValue(Users.class);

                    Log.d("CheckSearchList",user.getUserName()+"");

                    if(!user.getId().equals(firebaseUser.getUid())){
                        usersList.add(user);
                    }
                }

                chatUserListAdapter  = new ChatUserListAdapter(getApplicationContext(), usersList,false);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                recyclerView.setAdapter(chatUserListAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readUserList(){
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){

                    Users users = dataSnapshot.getValue(Users.class);
                    if(!users.getId().equals(firebaseUser.getUid())){ // firebase data တေထဲက me ကို ဖယ်ပြီးကျန်တာတေပြတာ
                      usersList.add(users);
                      Log.d("CheckingUserList",users+"");
                    }
                }

                chatUserListAdapter = new ChatUserListAdapter(getApplicationContext(),usersList,true);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                recyclerView.setAdapter(chatUserListAdapter);
                chatUserListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseDatabaseError", "Failed to read value.", error.toException());
                Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void updateOnlineStatus(String status){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status",status);

        reference.updateChildren(hashMap);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateOnlineStatus("offline");
    }

    @Override
    protected void onStop() {
        super.onStop();
       updateOnlineStatus("offline");
    }

    @Override
    protected void onResume() {
        super.onResume();
       //updateOnlineStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
       // updateOnlineStatus("online");
    }
}