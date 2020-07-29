package com.akb.seetalk.Model;

import com.akb.seetalk.Notifications.MyResponse;
import com.akb.seetalk.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA0UzZz2s:APA91bGdI9wH3jjbo_rJdA53Or41_otyPvOonMb7DR8RYRL-2Id19taUfk8X-aTDxhPqbxnR1PIRC4b4XV2NxuxeydhnWiDXKkwkrt78O211LpIcT-9dZ0opjq-nOibOdWiPHIehOU3C",
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}