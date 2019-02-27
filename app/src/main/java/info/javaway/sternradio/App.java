package info.javaway.sternradio;

import android.app.Application;
import android.content.Context;

public class App extends Application {

    private static Context context;

    public App() {
        context = this;
    }

    public static Context getContext() {
        return context;
    }
}
