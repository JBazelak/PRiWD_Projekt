package com.example.polinav3_SGB;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class KlientApi {

    private static final String TAG = "KlientApi";
    private static final String SERVER_URL = "http://192.168.1.5:8000/play";

    private final OkHttpClient client;
    private final Handler mainHandler;

    public KlientApi(){
        this.client = new OkHttpClient();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    public interface GameCallback{
        void onResult(String playerGesture, String robotGesture, String result);
         void onError(String errMessage);

    }

    public void sendFramesAndPlay(List<byte[]> frames, GameCallback callback){
        if (frames == null || frames.isEmpty()){
            callback.onError("Brak klatek");
            return; // DODANE: Warto dodać return, żeby kod nie próbował wysyłać pustej listy
        }

        // DODANE: .setType(MultipartBody.FORM)
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        for (int i = 0; i < frames.size(); i++) {
            builder.addFormDataPart("files", "frame_" + i + ".jpg",
                    RequestBody.create(MediaType.parse("image/jpeg"), frames.get(i)));
        }

        Request request = new Request.Builder()
                .url(SERVER_URL)
                .post(builder.build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError("Błąd sieci: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    try {
                        JSONObject json = new JSONObject(jsonResponse);
                        String playerGesture = json.getString("player_gesture");
                        String robotGesture = json.getString("robot_gesture");
                        String result = json.getString("result");
                        String debugVotes = json.optString("debug_votes", "");

                        Log.d(TAG, "Głosowanie modelu: " + debugVotes);
                        mainHandler.post(() -> callback.onResult(playerGesture, robotGesture, result));
                    } catch (Exception e) {
                        mainHandler.post(() -> callback.onError("Błąd parsowania JSON"));
                    }
                } else {
                    mainHandler.post(() -> callback.onError("Błąd serwera: " + response.code()));
                }
            }
        });

    }
}