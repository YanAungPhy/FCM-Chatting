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
import com.chatapp.chatapplication.model.ChatModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends FirestoreRecyclerAdapter<ChatModel, ChatAdapter.ChatViewHolder> {

    private Context context;

    public ChatAdapter(@NonNull FirestoreRecyclerOptions<ChatModel> options) {
        super(options);
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_list, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatViewHolder holder, int position, @NonNull ChatModel model) {

        holder.txtMessage.setText(model.getMessage());
        Glide.with(holder.userPhoto.getContext().getApplicationContext())
                .load(model.getUser_image_url())
                .into(holder.userPhoto);

        Log.d("CheckingAdapterImage",model.getChat_image()+"Adapter");
        if(!model.getChat_image().equals("")){
            holder.chatImg.setVisibility(View.VISIBLE);
            holder.txtMessage.setVisibility(View.GONE);
            Glide.with(holder.chatImg.getContext().getApplicationContext())
                    .load(model.getChat_image())
                    .into(holder.chatImg);
        }else {
            holder.chatImg.setVisibility(View.GONE);
        }
    }


    class ChatViewHolder extends RecyclerView.ViewHolder {

        private TextView txtMessage;
        private CircleImageView userPhoto;
        private ImageView chatImg;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            userPhoto = itemView.findViewById(R.id.userPhoto);
            chatImg = itemView.findViewById(R.id.chatImg);
        }
    }
}
