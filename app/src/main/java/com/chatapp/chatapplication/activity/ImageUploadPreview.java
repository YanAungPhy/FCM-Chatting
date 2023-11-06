package com.chatapp.chatapplication.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chatapp.chatapplication.R;
import com.chatapp.chatapplication.cords.FirebaseCords;
import com.chatapp.chatapplication.utility.Constant;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class ImageUploadPreview extends AppCompatActivity implements View.OnClickListener {

    private Uri imgUrl;
    private ImageView imgPreview;
    private TextView imgSend;
    private ProgressDialog dialog;
    private StorageReference storageReference;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload_preview);

        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        imgPreview = findViewById(R.id.imgPreview);
        imgSend = findViewById(R.id.imgSend);
        auth = FirebaseAuth.getInstance();


        Intent intent = getIntent();
        if(intent != null){
            imgUrl = Uri.parse(intent.getStringExtra(Constant.IMAGE_CROP));
        }
        imgPreview.setImageURI(imgUrl);

        imgSend.setOnClickListener(this);
    }

    private void addPhotoMessage(){
        dialog.setMessage("Uploading...");
        dialog.show();

        /*Generate messageId using the current date*/
        Date today = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String messageId = dateFormat.format(today);

        storageReference = FirebaseStorage.getInstance().getReference().child("chat_image");
        StorageReference imgPath = storageReference.child(messageId+".jpg");
        Log.d("CheckingUplaodImage",storageReference+"");
        Log.d("CheckingUplaodImage",imgUrl+"ImgUrl");

        imgPath.putFile(imgUrl).addOnCompleteListener(task -> {
            Log.d("CheckingUplaodImage",task.isSuccessful()+"");
            if(task.isSuccessful()){
                dialog.setMessage("Finalizing Data...");
                imgPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        addMesageToDatabase(uri);
                    }

                });
            }else {
                Log.d("CheckingUplaodImage",task.getException().toString()+"");
            }
        });

    }

    private void addMesageToDatabase(Uri uri){
        FirebaseUser user = auth.getCurrentUser();

        /*Generate messageId using the current date*/
        Date today = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String messageId = dateFormat.format(today);

        /*Getting user image from google account*/
        String user_image_url = "";
        Uri photoUrl = user.getPhotoUrl();
        String originalUrl = "s96-c/photo.jpg";
        String resizeImageUrl = "s400-c/photo.jpg";
        if (photoUrl != null) {
            String photoPath = photoUrl.toString();
            user_image_url = photoPath.replace(originalUrl, resizeImageUrl);
        }

        HashMap<String, Object> messageObj = new HashMap<>();
        messageObj.put("message", "Hello");
        messageObj.put("user_name", user.getDisplayName());
        messageObj.put("timestamp", FieldValue.serverTimestamp());
        messageObj.put("messageId", messageId);
        messageObj.put("chat_image", uri.toString());
        messageObj.put("user_image_url", user_image_url);
        Log.d("CheckingUserImge",user_image_url);

        FirebaseCords.MAIN_CHAT_DATABASE.document(messageId).set(messageObj).addOnCompleteListener(task -> {

            Log.d("CheckingError",task.isSuccessful()+"");
            if (task.isSuccessful()) {
                Toast.makeText(getApplicationContext(), "Photo Send", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                finish();
                //chatBox.setText("");
            } else {
                Toast.makeText(getApplicationContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                Log.d("CheckError",task.getException()+"");
                dialog.dismiss();
                finish();
            }

        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imgSend:
                addPhotoMessage();
                break;
        }
    }
}