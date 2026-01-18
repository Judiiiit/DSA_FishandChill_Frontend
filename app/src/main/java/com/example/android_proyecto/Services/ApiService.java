package com.example.android_proyecto.Services;

import com.example.android_proyecto.Models.CapturedFish;
import com.example.android_proyecto.Models.EventUser;
import com.example.android_proyecto.Models.Faq;
import com.example.android_proyecto.Models.FishingRod;
import com.example.android_proyecto.Models.Group;
import com.example.android_proyecto.Models.GroupUser;
import com.example.android_proyecto.Models.LeaderboardEntry;
import com.example.android_proyecto.Models.QuestionRequest;
import com.example.android_proyecto.Models.SellCapturedFish;
import com.example.android_proyecto.Models.TeamRanking;
import com.example.android_proyecto.Models.TeamResponse;
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
import retrofit2.http.Query;

public interface ApiService {

    @POST("auth/register")
    Call<User> register(@Body UserRegister body);

    @POST("auth/login")
    Call<Token> login(@Body UserLogIn body);

    @DELETE("auth/logout")
    Call<ResponseBody> logout(@Header("Authorization") String token);

    @GET("me")
    Call<User> getProfile(@Header("Authorization") String token);

    @GET("me/captured_fishes")
    Call<List<CapturedFish>> getMyCapturedFishes(@Header("Authorization") String token);

    @GET("me/owned_fishing_rods")
    Call<List<FishingRod>> getMyOwnedFishingRods(@Header("Authorization") String token);

    @DELETE("me")
    Call<ResponseBody> deleteMe(@Header("Authorization") String token);

    @GET("catalog/fishing_rods")
    Call<List<FishingRod>> getRods();

    @GET("catalog/fishing_rods/{fishing_rod_name}")
    Call<FishingRod> getRodByName(@Path("fishing_rod_name") String rodName);

    @GET("catalog/fishes")
    Call<ResponseBody> getFishes();

    @GET("catalog/fishes/{species_name}")
    Call<ResponseBody> getFishByName(@Path("species_name") String speciesName);

    @POST("shop/captured_fishes/sell")
    Call<ResponseBody> sellCapturedFish(
            @Header("Authorization") String token,
            @Body SellCapturedFish body
    );

    @POST("shop/fishing_rods/{fishing_rod_name}/buy")
    Call<ResponseBody> buyRod(
            @Header("Authorization") String token,
            @Path("fishing_rod_name") String rodName
    );

    @POST("shop/fishing_rods/{fishing_rod_name}/equip")
    Call<ResponseBody> equipRod(
            @Header("Authorization") String token,
            @Path("fishing_rod_name") String rodName
    );

    @POST("game/captured")
    Call<ResponseBody> captureFish(@Header("Authorization") String token);

    @GET("info/faqs")
    Call<List<Faq>> getFaqs();

    @POST("info/question")
    Call<QuestionRequest> postQuestion(@Body QuestionRequest question);

    @GET("info/videos")
    Call<List<Video>> getVideos();

    @GET("info/groups")
    Call<List<Group>> getGroups();

    @POST("info/groups/{groupId}/")
    Call<ResponseBody> joinGroup(
            @Header("Authorization") String token,
            @Path("groupId") int groupId
    );

    @GET("info/groups/{groupId}/users")
    Call<List<GroupUser>> getGroupUsers(
            @Header("Authorization") String token,
            @Path("groupId") int groupId
    );

    @GET("teams")
    Call<List<TeamRanking>> getTeams();

    @GET("teams/{teamName}")
    Call<TeamResponse> getTeam(@Path("teamName") String teamName);

    @GET("me/teams/{teamName}/join")
    Call<ResponseBody> joinTeam(
            @Header("Authorization") String token,
            @Path("teamName") String teamName
    );

    @POST("me/teams/{teamName}/create")
    Call<ResponseBody> createTeam(
            @Header("Authorization") String token,
            @Path("teamName") String teamName
    );

    @GET("me/teams/leave")
    Call<ResponseBody> leaveTeam(@Header("Authorization") String token);

    @GET("info/teams/ranking")
    Call<List<TeamRanking>> getTeamsRankingInfo();

    @GET("info/teams/{teamName}")
    Call<TeamResponse> getTeamInfo(@Path("teamName") String teamName);

    @POST("me/events/{eventId}/subscribe")
    Call<ResponseBody> subscribeToEvent(
            @Header("Authorization") String token,
            @Path("eventId") int eventId
    );

    @GET("info/events/{eventId}")
    Call<List<EventUser>> getRegisteredUsersInEvent(
            @Path("eventId") int eventId
    );

    @GET("info/leaderboard")
    Call<List<LeaderboardEntry>> getFishLeaderboard();

    @GET("me/change_avatar")
    Call<ResponseBody> changeAvatar(@Header("Authorization") String token);

    @GET("me/teams/{teamName}/members")
    Call<TeamResponse> getTeamMembersMe(
            @Header("Authorization") String token,
            @Path("teamName") String teamName
    );
}
