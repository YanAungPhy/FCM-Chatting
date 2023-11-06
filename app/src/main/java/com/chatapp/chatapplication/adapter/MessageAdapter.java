package com.chatapp.chatapplication.adapter;

import android.content.Context;
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
import com.chatapp.chatapplication.model.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    private Context context;
    private List<ChatMessage> chatMessageList;
    private String imageUrl;
    FirebaseUser firebaseUser;

    public MessageAdapter(Context context, List<ChatMessage> chatMessageList, String imageUrl) {
        this.context = context;
        this.chatMessageList = chatMessageList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MSG_TYPE_LEFT){
             View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_left_item, parent, false);
            return new MessageViewHolder(view);
        }else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_right_item, parent, false);
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = chatMessageList.get(position);
        holder.message.setText(message.getMessage());

        if(imageUrl.equals("default")){
            holder.userImage.setImageResource(R.mipmap.ic_launcher);
        }else {
            Glide.with(context)
                    .load(imageUrl)
                    .into(holder.userImage);
        }
        if(position == chatMessageList.size()-1){//check for lat message
                if(message.getIsSeen() == 1){
                    holder.txtSeen.setText("Seen");
                }else {
                    holder.txtSeen.setText("Delivered");
                }

        }else {
            holder.txtSeen.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

       private ImageView userImage;
       private TextView message;
       private TextView txtSeen;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.userImage);
            message = itemView.findViewById(R.id.message);
            txtSeen = itemView.findViewById(R.id.txtSeen);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
      if(chatMessageList.get(position).getSender().equals(firebaseUser.getUid())){
          return  MSG_TYPE_RIGHT;
      }else {
          return MSG_TYPE_LEFT;
      }
    }
}
