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
                    "Authorization:key=AAAAr6xNLz0:APA91bE4se7mfUq407qj6BPJVTk1ggqaOOVXvvhzJvcCAPuFWQLfzm1Q-bbDvfvXLi3yv5iKaaZD64DgnjdsgIae4pG4-8kXSwkrf5YreRY8WUoATDgAIBQ1hDEPL8g4yLHW-OTD6xYu"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}