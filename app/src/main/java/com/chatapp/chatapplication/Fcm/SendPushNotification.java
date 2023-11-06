package com.chatapp.chatapplication.Fcm;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.chatapp.chatapplication.model.Noti_Obj;
import com.chatapp.chatapplication.retrofit.FCMMessage;
import com.chatapp.chatapplication.retrofit.FCMResponseBody;
import com.chatapp.chatapplication.retrofit.FCMService;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SendPushNotification {
    private Context context;
    private String FCM_KEY = "AAAA2EvzGI4:APA91bEoCGpHMDXoLupLOaznmZQX06d3JnmFiz7vheevd3218aZYG1NKSuneoK2JctQ60ez16ssFN5mT4S-hSf1c2QZCA9KQGhu7Anl5EuTaeMysjugUCOA9IZCaIzwhua6AyzKatBiy";

    public SendPushNotification(Context context) {
        this.context = context;
    }

    public void startPush(String userName, String messageBody, String token){
        Log.d("FcmNotiError",userName);
        HashMap<String,String >  message = new HashMap<>();
        message.put("message",messageBody);
        message.put("title", userName);
        sendPushToSingleInstance(message,token);
    }



    private void sendPushToSingleInstance(HashMap<String, String> message, String token) {
        String FCM_USER = "https://fcm.googleapis.com/fcm/send";
        StringRequest request = new StringRequest(Request.Method.POST, FCM_USER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(context,response,Toast.LENGTH_SHORT).show();
                        Log.d("FcmNotiError",response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("FcmNotiError","onErrorResponse"+error.toString());
                    }
                }){
            @Override
            public byte[] getBody() throws AuthFailureError {
               Map<String,Object> raw = new Hashtable<>();
               raw.put("data",new JSONObject(message));
               raw.put("to","topics/"+token);
               return new JSONObject(raw).toString().getBytes();
            }

            public String getBodyContentType(){
                return "application/json: charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> header = new HashMap<>();
                header.put("Authorization","key="+FCM_KEY);
                header.put("Content_Type","application/json");
                return header;
            }
        };

        Volley.newRequestQueue(context).add(request);
    }

   /* private void sendPushToSingleInstance(HashMap<String, String> message, String token) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        FCMService service = retrofit.create(FCMService.class);

// Create the FCMMessage object with the recipient's token and message data
        FCMMessage fcmMessage = new FCMMessage(token, message);

        Call<FCMResponseBody> call = service.sendFCMMessage(fcmMessage);

        call.enqueue(new Callback<FCMResponseBody>() {
            @Override
            public void onResponse(Call<FCMResponseBody> call, retrofit2.Response<FCMResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Noti Send Message", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FCMResponseBody> call, Throwable t) {
                Toast.makeText(context, t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }*/


}
