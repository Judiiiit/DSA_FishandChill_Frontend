package com.example.android_proyecto
import retrofit2.awaitResponse
import com.example.android_proyecto.User

class AuthRepository(
    private val api: GameApi = Network.retrofit.create(GameApi::class.java)
) {
    // REGISTER
    suspend fun register(username: String, email: String, password: String): Result<User> {
        val req = User(
            id = null, // lo generará el backend si corresponde
            username = username,
            password = password,
            email = email,
            userPosition = null,
            userInventory = null
        )
        return try {
            val res = api.register(req).awaitResponse()
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
            else Result.failure(Exception("Error ${res.code()}: ${res.errorBody()?.string()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // LOGIN — activa UNA de estas rutas según tu backend:

    // Opción A: endpoint dedicado
    suspend fun loginWithEndpoint(username: String, password: String): Result<User> {
        return try {
            val res = api.login(LoginRequest(username, password)).awaitResponse()
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
            else Result.failure(Exception("Error ${res.code()}: ${res.errorBody()?.string()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Opción B: buscar por username y validar password en cliente
    suspend fun loginByUsername(username: String, password: String): Result<User> {
        return try {
            val res = api.getByUsername(username).awaitResponse()
            val user = if (res.isSuccessful) res.body() else null
            if (user != null && user.password == password) Result.success(user)
            else Result.failure(Exception("Usuario o contraseña incorrectos"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Opción C: recorrer lista y validar
    suspend fun loginFromList(username: String, password: String): Result<User> {
        return try {
            val res = api.listUsers().awaitResponse()
            val list = if (res.isSuccessful) res.body() else emptyList()
            val user = list?.firstOrNull { it.username == username && it.password == password }
            if (user != null) Result.success(user)
            else Result.failure(Exception("Usuario o contraseña incorrectos"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
