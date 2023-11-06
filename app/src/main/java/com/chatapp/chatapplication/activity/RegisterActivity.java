package com.chatapp.chatapplication.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.chatapp.chatapplication.R;
import com.chatapp.chatapplication.model.Users;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    ImageView imgBtn;
    GoogleSignInClient googleSignInClient;
    int RC_SIGN_IN = 100;
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    ProgressDialog progressDialog;
    private EditText edtUserName;
    private EditText edtGmail;
    private EditText edtPassword;
    private Button btnRegister;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Utils.FullScreen(this);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        imgBtn = findViewById(R.id.imgLogin);
        edtUserName = findViewById(R.id.edtUserName);
        edtGmail = findViewById(R.id.edtGmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnRegister);

        FirebaseUser  currentUser = mAuth.getCurrentUser();
        Log.d("CheckingCurrentUser",currentUser+"");
        if(currentUser != null){
            startActivity(new Intent(this, ChatUserListActivity.class));
            finish();
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("we are creating you account");

        // R.string.default_web_client_id က class path ကို မြှင့်တဲ့ခါ auto generate ထုတ်ပေးတာ
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);


        imgBtn.setOnClickListener(v -> {
            progressDialog.show();
            Intent intent = googleSignInClient.getSignInIntent();
            startActivityForResult(intent,RC_SIGN_IN);
        });

        btnRegister.setOnClickListener(v -> {
            register();
        });
    }

    private void register(){
        String userName = edtUserName.getText().toString();
        String mail = edtGmail.getText().toString();
        String password = edtPassword.getText().toString();
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(mail,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            String userId = firebaseUser.getUid();

                            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id",userId);
                            hashMap.put("userName",userName);
                            hashMap.put("imageUrl","default");
                            hashMap.put("status","offline");
                            hashMap.put("search",userName.toLowerCase());

                            databaseReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressDialog.dismiss();
                                    if(task.isSuccessful()){
                                        startActivity(new Intent(RegisterActivity.this,ChatUserListActivity.class));
                                    }
                                    Log.d("CheckUserRegiser",task.isSuccessful()+"");
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("ChekUserError",e.toString());
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN ){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            Log.d("CheckingAccessToken",task+"");
            try {
                GoogleSignInAccount   account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
                progressDialog.dismiss();

            } catch (ApiException e) {
                e.printStackTrace();
                Log.d("CheckingAccessToken",e.toString()+"Error");
            }

        }
    }

    private void firebaseAuthWithGoogle(String token) {
        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(token, null);
        mAuth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete( Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser fbUser = mAuth.getCurrentUser();
                            Log.d("CheckingResult",fbUser.getEmail()+"Null");
                            Users users = new Users();
                            users.setId(fbUser.getUid());
                            users.setUserName(fbUser.getDisplayName());
                            users.setImageUrl(fbUser.getPhotoUrl().toString());
                            database.getReference().child("Users").child(fbUser.getUid()).setValue(users);
                            updateUI(fbUser);

                        } else {
                            Log.w("Check", "signInWithCredential:failure", task.getException());
                            //updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        startActivity(new Intent(this, MessageActivity.class));
        finish();
    }
}