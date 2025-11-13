package com.example.android_proyecto.Services;

import com.example.android_proyecto.Models.Token;
import com.example.android_proyecto.Models.User;
import com.example.android_proyecto.Models.UserLogIn;
import com.example.android_proyecto.Models.UserRegister;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    // POST http://10.0.2.2:8080/dsaApp/auth/register
    @POST("auth/register")
    Call<User> register(@Body UserRegister body);

    // POST http://10.0.2.2:8080/dsaApp/auth/login
    @POST("auth/login")
    Call<Token> login(@Body UserLogIn body);
}
