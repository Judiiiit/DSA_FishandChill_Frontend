package com.example.android_proyecto
import retrofit2.Call
import retrofit2.http.*
import com.example.android_proyecto.User

interface GameApi {

    // ---------- REGISTER ----------
    // Del código del backend se ve @POST @Path("/user")
    @POST("game/user")
    fun register(@Body user: User): Call<User>

    // ---------- LOGIN (elige 1 de las 3 variantes) ----------

    // Variante 1: endpoint dedicado (si tu backend lo tiene, p.ej. POST /game/login)
    @POST("game/login")
    fun login(@Body credentials: LoginRequest): Call<User>

    // Variante 2: por username (si existiera algo estilo GET /game/user/byUsername/{username})
    @GET("game/user/byUsername/{username}")
    fun getByUsername(@Path("username") username: String): Call<User>

    // Variante 3: lista completa (si existiera GET /game/users)
    @GET("game/users")
    fun listUsers(): Call<List<User>>

    // Auxiliares útiles:
    @GET("game/user/{id}")
    fun getUserById(@Path("id") id: String): Call<User>
}

data class LoginRequest(val username: String, val password: String)
