package info.javaway.sternradio.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import info.javaway.sternradio.Utils;
import okhttp3.OkHttpClient;
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
        httpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
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
