package com.chatapp.chatapplication.retrofit;

import com.chatapp.chatapplication.model.Noti_Obj;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface FCMService {

    @Headers({
            "Content-Type: application/json",
            "Authorization: key=AAAA2EvzGI4:APA91bEoCGpHMDXoLupLOaznmZQX06d3JnmFiz7vheevd3218aZYG1NKSuneoK2JctQ60ez16ssFN5mT4S-hSf1c2QZCA9KQGhu7Anl5EuTaeMysjugUCOA9IZCaIzwhua6AyzKatBiy"
    })
    @POST("fcm/send")
    Call<FCMResponseBody> sendFCMMessage(@Body Noti_Obj message);
}
