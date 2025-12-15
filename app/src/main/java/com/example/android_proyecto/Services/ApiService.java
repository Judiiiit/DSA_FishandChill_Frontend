package com.example.android_proyecto.Services;

import com.example.android_proyecto.Models.Faq;
import com.example.android_proyecto.Models.FishingRod;
import com.example.android_proyecto.Models.QuestionRequest;
import com.example.android_proyecto.Models.Token;
import com.example.android_proyecto.Models.User;
import com.example.android_proyecto.Models.UserLogIn;
import com.example.android_proyecto.Models.UserRegister;
import com.example.android_proyecto.Models.Video;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    // --- AUTH ---

    @POST("auth/register")
    Call<User> register(@Body UserRegister body);

    @POST("auth/login")
    Call<Token> login(@Body UserLogIn body);

    @DELETE("auth/logout")
    Call<ResponseBody> logout(@Header("Authorization") String token);

    // --- ME ---

    @GET("me")
    Call<User> getProfile(@Header("Authorization") String token);

    @GET("me/captured_fishes")
    Call<ResponseBody> getMyCapturedFishes(@Header("Authorization") String token);

    @GET("me/owned_fishing_rods")
    Call<List<FishingRod>> getMyOwnedFishingRods(@Header("Authorization") String token);

    @DELETE("me")
    Call<ResponseBody> deleteMe(@Header("Authorization") String token);


    // --- CATALOG ---

    @GET("catalog/fishing_rods")
    Call<List<FishingRod>> getRods();

    @GET("catalog/fishing_rods/{fishing_rod_name}")
    Call<FishingRod> getRodByName(@Path("fishing_rod_name") String rodName);

    @GET("catalog/fishes")
    Call<ResponseBody> getFishes();

    @GET("catalog/fishes/{species_name}")
    Call<ResponseBody> getFishByName(@Path("species_name") String speciesName);

    // --- SHOP ---

    @POST("shop/fishing_rods/{fishing_rod_name}/buy")
    Call<ResponseBody> buyRod(
            @Header("Authorization") String token,
            @Path("fishing_rod_name") String rodName
    );

    // --- GAME ---

    @POST("game/captured")
    Call<ResponseBody> captureFish(@Header("Authorization") String token);

    // --- INFO ---

    @GET("info/faqs")
    Call<List<Faq>> getFaqs();

    @POST("info/question")
    Call<QuestionRequest> postQuestion(@Body QuestionRequest question);

    @GET("info/videos")
    Call<List<Video>> getVideos();
}
