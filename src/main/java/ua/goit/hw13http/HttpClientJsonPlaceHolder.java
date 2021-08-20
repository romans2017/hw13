package ua.goit.hw13http;

import java.net.http.HttpClient;

public class HttpClientJsonPlaceHolder {
    private static HttpClientJsonPlaceHolder instance;

    private HttpClientJsonPlaceHolder() {
    }

    public static HttpClientJsonPlaceHolder getInstance() {
        synchronized (HttpClientJsonPlaceHolder.class) {
            if (instance == null) {
                instance = new HttpClientJsonPlaceHolder();
            }
        }
        return instance;
    }

}
