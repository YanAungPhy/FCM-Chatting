package com.chatapp.chatapplication.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chatapp.chatapplication.R;
import com.chatapp.chatapplication.adapter.ChatAdapter;
import com.chatapp.chatapplication.adapter.MessageAdapter;
import com.chatapp.chatapplication.cords.FirebaseCords;
import com.chatapp.chatapplication.model.ChatMessage;
import com.chatapp.chatapplication.model.ChatModel;
import com.chatapp.chatapplication.model.Data;
import com.chatapp.chatapplication.model.Noti_Obj;
import com.chatapp.chatapplication.model.Notification;
import com.chatapp.chatapplication.model.Users;
import com.chatapp.chatapplication.retrofit.FCMResponseBody;
import com.chatapp.chatapplication.retrofit.FCMService;
import com.chatapp.chatapplication.utility.Constant;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MessageActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView txtSend;
    private EditText chatBox;
    private FirebaseAuth auth;
    private ChatAdapter chatAdapter;
    private RecyclerView chatListRecyclerview;
    private ImageView imgSend;
    private String token;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private ImageView userPhoto;
    private ImageView imgBack;
    private TextView userName;
    private String userId;

    //second way
    private MessageAdapter messageAdapter;
    private List<ChatMessage> chatMessageList;
    private ValueEventListener seenListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        //Utils.FullScreen(this);

        FirebaseMessaging.getInstance().subscribeToTopic("global_chat");

        txtSend = findViewById(R.id.imgSend);
        chatBox = findViewById(R.id.edtChat);
        imgSend = findViewById(R.id.imgPhoto);
        chatListRecyclerview = findViewById(R.id.chatListRecyclerView);
        userPhoto = findViewById(R.id.userPhoto);
        imgBack = findViewById(R.id.imgBack);
        userName = findViewById(R.id.txtUseName);
        auth = FirebaseAuth.getInstance();
        userId = getIntent().getStringExtra("userId");

        txtSend.setOnClickListener(this);
        imgSend.setOnClickListener(this);
        imgBack.setOnClickListener(this);
        individualChatList();
        //initChatList();
        getFcmToken();

    }

    private void individualChatList(){
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users users = snapshot.getValue(Users.class);
                userName.setText(users.getUserName());
                if (users.getImageUrl().equals("default")){
                    userPhoto.setImageResource(R.mipmap.ic_launcher);
                }else {
                    Glide.with(MessageActivity.this)
                            .load(users.getImageUrl())
                            .into(userPhoto);
                }
                showMessageList(firebaseUser.getUid(),userId,users.getImageUrl());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        sendMessage(userId);
    }

    private void getFcmToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.d("CheckFcmToken", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        token = task.getResult();
                        Log.d("CheckFcmToken", token);
                    }
                });

    }
    private void senNoit(String message) {
        Data data = new Data();
        data.setNotiData("chattingapp");

        Notification notification = new Notification();
        notification.setTitle("Chatting");
        notification.setBody(message);

        Noti_Obj notiObj = new Noti_Obj();
        notiObj.setPriority("HIGH");
        notiObj.setData(data);
        notiObj.setNotification(notification);
        notiObj.setTo(token);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        FCMService service = retrofit.create(FCMService.class);

        Call<FCMResponseBody> call = service.sendFCMMessage(notiObj);

        call.enqueue(new Callback<FCMResponseBody>() {
            @Override
            public void onResponse(Call<FCMResponseBody> call, retrofit2.Response<FCMResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Noti Send Message", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FCMResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initChatList() {
        chatListRecyclerview.setHasFixedSize(true);
        chatListRecyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));

        Query query = FirebaseCords.MAIN_CHAT_DATABASE.orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<ChatModel> options = new FirestoreRecyclerOptions.Builder<ChatModel>()
                .setQuery(query, ChatModel.class)
                .build();
        chatAdapter = new ChatAdapter(options);
        chatListRecyclerview.setAdapter(chatAdapter);
        chatAdapter.startListening();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgSend:
                String message = chatBox.getText().toString();
                if(!message.equals("null")){
                    senNoit(message);
                    sendMessage(message,firebaseUser.getUid(),userId);//(message, ပို့သူ Id, လက်ခံသူ Id)
                }else {
                    Toast.makeText(getApplicationContext(),"You can't send empty message",Toast.LENGTH_SHORT).show();
                }
                chatBox.setText("");
                break;
            case R.id.imgPhoto:
                openExplorer();
                break;
            case R.id.imgBack:
                finish();
                break;

        }
    }

    private void sendMessage(String userId){
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                    if(chatMessage.getReceiver().equals(firebaseUser.getUid()) && chatMessage.getSender().equals(userId)){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isSeen",1);
                        dataSnapshot.getRef().updateChildren(hashMap);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //second way send message
    private void sendMessage(String message, String sender, String receiver){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isSeen",0); // if 0 is delivered/ 1 is seen.

        reference.child("Chats").push().setValue(hashMap);
    }

   //second may show message list
    private void showMessageList(String myid,String toUserId, String imageUrl){
        chatMessageList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatMessageList.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    ChatMessage message = snapshot.getValue(ChatMessage.class);
                    if(message.getReceiver().equals(myid) && message.getSender().equals(toUserId) ||
                    message.getReceiver().equals(toUserId) && message.getSender().equals(myid)){
                        chatMessageList.add(message);
                    }
                }
                messageAdapter = new MessageAdapter(getApplicationContext(),chatMessageList,imageUrl);
                chatListRecyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                chatListRecyclerview.setAdapter(messageAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
   /* private void sendMessage() {
        String message = chatBox.getText().toString();
        FirebaseUser user = auth.getCurrentUser();
        chatBox.setText("");

        if (!TextUtils.isEmpty(message)) {

            *//*Generate messageId using the current date*//*
            Date today = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String messageId = dateFormat.format(today);

            *//*Getting user image from google account*//*
            String user_image_url = "";
            Uri photoUrl = user.getPhotoUrl();
            String originalUrl = "s96-c/photo.jpg";
            String resizeImageUrl = "s400-c/photo.jpg";
            if (photoUrl != null) {
                String photoPath = photoUrl.toString();
                user_image_url = photoPath.replace(originalUrl, resizeImageUrl);
            }

            HashMap<String, Object> messageObj = new HashMap<>();
            messageObj.put("message", message);
            messageObj.put("user_name", user.getDisplayName());
            messageObj.put("timestamp", FieldValue.serverTimestamp());
            messageObj.put("messageId", messageId);
            messageObj.put("chat_image", "");
            messageObj.put("user_image_url", user_image_url);

            FirebaseCords.MAIN_CHAT_DATABASE.document(messageId).set(messageObj).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {
                    senNoit(message);
                    SendPushNotification sendPushNotification = new SendPushNotification(this);
                    sendPushNotification.startPush(user.getDisplayName(), message, "global_chat");
                } else {
                    Toast.makeText(getApplicationContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                    Log.d("CheckError", task.getException() + "");
                }

            });
        }

    }*/

    private void openExplorer() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            cropImage();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 20);
            } else {
                Toast.makeText(getApplicationContext(), "Storage permission needed", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 20);
            }
        }
    }

    private void cropImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                Intent intent = new Intent(MessageActivity.this, ImageUploadPreview.class);
                intent.putExtra(Constant.IMAGE_CROP, resultUri.toString());
                startActivity(intent);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        }
       // chatAdapter.startListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        chatAdapter.startListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
    }
}