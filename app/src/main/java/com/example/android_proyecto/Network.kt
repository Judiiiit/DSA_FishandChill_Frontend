import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object Network {
    // TODO: AJUSTA esto a la URL que imprime tu servidor al arrancar (Main.java).
    // Suele ser algo como: "http://localhost:8080/" o "http://10.0.2.2:8080/" en emulador.
    private const val BASE_URL = "http://10.0.2.2:8080/dsaApp/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL) // debe acabar en /
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
}
