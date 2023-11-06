package com.chatapp.chatapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chatapp.chatapplication.R;
import com.chatapp.chatapplication.activity.MessageActivity;
import com.chatapp.chatapplication.model.ChatMessage;
import com.chatapp.chatapplication.model.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatUserListAdapter extends RecyclerView.Adapter<ChatUserListAdapter.ChatListViewHolder> {

    private Context context;
    private ArrayList<Users> arrayList;
    private Boolean isOnline;
    private String theLastMessage;

   public ChatUserListAdapter(Context context, ArrayList<Users> arrayList,boolean isOnline) {
        this.context = context;
        this.arrayList = arrayList;
        this.isOnline = isOnline;
    }


    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_user_list_item, parent, false);
        return new ChatListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListViewHolder holder, int position) {
        Users user = arrayList.get(position);
        holder.txtUserName.setText(user.getUserName());
        Log.d("Adapter",user.getUserName()+"");

        if(user.getImageUrl().equals("default")){
           holder.userPhoto.setImageResource(R.mipmap.ic_launcher);
        }else {
            Glide.with(context)
                    .load(user.getImageUrl())
                    .into(holder.userPhoto);
        }

        if(isOnline){
           checkLastMessage(user.getId(),holder.txtLastMessage);
        }else {
            holder.txtLastMessage.setVisibility(View.GONE);
        }

        if(isOnline){
            if(user.getStatus().equals("online")){
                holder.imgOnline.setVisibility(View.VISIBLE);
            }else {
                holder.imgOnline.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("userId",user.getId());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class  ChatListViewHolder extends RecyclerView.ViewHolder{

        private ImageView userPhoto;
        private TextView txtUserName;
        private ImageView imgOnline;
        private TextView txtLastMessage;

        public ChatListViewHolder(@NonNull View itemView) {
            super(itemView);
            userPhoto = itemView.findViewById(R.id.userPhoto);
            txtUserName = itemView.findViewById(R.id.userName);
            imgOnline = itemView.findViewById(R.id.imgOnlineStatus);
            txtLastMessage = itemView.findViewById(R.id.txtLastMessage);
        }
    }

    //check for last message

    private void checkLastMessage(final String userId, TextView lat_msg){
       theLastMessage = "default";
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                    if(chatMessage.getReceiver().equals(firebaseUser.getUid()) && chatMessage.getSender().equals(userId) ||
                            chatMessage.getReceiver().equals(userId) && chatMessage.getSender().equals(firebaseUser.getUid())){
                        theLastMessage = chatMessage.getMessage();
                    }
                }
                switch (theLastMessage){
                    case "default":
                        lat_msg.setText("No Message");
                        break;
                    default:
                        lat_msg.setText(theLastMessage);
                        break;
                }
                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
