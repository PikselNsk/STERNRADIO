package info.javaway.sternradio.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import info.javaway.sternradio.Utils;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DownloadTrackController {

    static String BASE_URL = "https://a1.radioheart.ru/api/";
    private static OkHttpClient httpClient;

    public static RadioApi getApi() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(Utils::simpleLog
        );
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        Interceptor interceptorCache = new Interceptor() {
            @Override public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Request.Builder builder = request.newBuilder().addHeader("Cache-Control", "no-cache");
                request = builder.build();
                return chain.proceed(request);
            }
        };

        httpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptorCache)
                .build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        RadioApi bloknoteApi = retrofit.create(RadioApi.class);
        return bloknoteApi;

    }
}
