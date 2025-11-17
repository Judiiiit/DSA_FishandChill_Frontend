package com.example.android_proyecto.Services;

import com.example.android_proyecto.Models.FishingRod;
import com.example.android_proyecto.Models.Token;
import com.example.android_proyecto.Models.User;
import com.example.android_proyecto.Models.UserLogIn;
import com.example.android_proyecto.Models.UserRegister;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    //Registro
    @POST("auth/register")
    Call<User> register(@Body UserRegister body);

    //Login
    @POST("auth/login")
    Call<Token> login(@Body UserLogIn body);

    // Catálogo de cañas
    @GET("catalog/rods")
    Call<List<FishingRod>> getRods();

    // balance del usuario
    @GET("me/balance")
    Call<ResponseBody> getBalance(@Header("Authorization") String token);

    @GET("catalog/rods/loadAll")
    Call<ResponseBody> loadRodsDictionary();

    //Perfil
    @GET("me")
    Call<User> getProfile(@Header("Authorization") String token);

    // comprar caña
    @POST("shop/rods/{rodId}/buy")
    Call<ResponseBody> buyRod(
            @Header("Authorization") String token,
            @Path("rodId") String rodId
    );
}
