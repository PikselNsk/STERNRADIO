package info.javaway.sternradio.retrofit;

import info.javaway.sternradio.model.ContainerTracks;
import info.javaway.sternradio.model.OldVersionTrack;
import info.javaway.sternradio.model.Track;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface RadioApi {
    @GET("json?userlogin=user8011&api=getFile")
    @Streaming
    Call<ResponseBody> apiDownloadTrack(@Query("id") int id);

    @GET("json?userlogin=user8011&api=air_last_tracks&count=1")
    Call<Track[]> loadLastTracks();

    @GET("json?userlogin=user8011&api=nexttrack&count=1")
    Call<OldVersionTrack[]> loadNextTracks();

    @GET("json?userlogin=user8011&api=filesList")
    Call<Track[]> loadAllFiles();
}
