package com.example.polinav3_SGB;
import android.util.Base64;
import android.util.Log;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiController {
    private static final String TAG = "RobotAPI";
    private String API_URL = "http://10.91.146.102:8000/play"; // 10.0.2.2 to IP emulatora

    // Tworzenie watku
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void sendToServer(final List<File> PhotoFiles) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    List<String> basePhoto = new ArrayList<>();

                    // Convert to Byte64
                    for (File file : PhotoFiles) {
                        if (file.exists()) {
                            ByteArrayOutputStream ous = new ByteArrayOutputStream();
                            try (FileInputStream ios = new FileInputStream(file)) {
                                byte[] buffer = new byte[4096];
                                int read;
                                while ((read = ios.read(buffer)) != -1) {
                                    ous.write(buffer, 0, read);
                                }
                                byte[] filesBytes = ous.toByteArray();
                                String baza64 = Base64.encodeToString(filesBytes, Base64.NO_WRAP);
                                basePhoto.add("\"" + baza64 + "\"");
                            } catch (IOException e) {
                                Log.e(TAG, "Error reading file: " + file.getName(), e);
                                executor.wait(100);
                            }
                        }
                    }

                    if (basePhoto.isEmpty()) {
                        Log.e(TAG, "No files to send");
                        return;
                    }

                    // Json
                    StringBuilder jsonBuilder = new StringBuilder();
                    jsonBuilder.append("{\"images\": [");
                    for (int i = 0; i < basePhoto.size(); i++) {
                        jsonBuilder.append(basePhoto.get(i));
                        if (i < basePhoto.size() - 1) {
                            jsonBuilder.append(",");
                        }
                    }
                    jsonBuilder.append("]}");
                    String jsonBody = jsonBuilder.toString();

                    // HTTP config
                    URL url = new URL(API_URL);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setConnectTimeout(15000); // connect
                    conn.setReadTimeout(15000);    // response wait
                    conn.setDoOutput(true);        // Output data
                    conn.setDoInput(true);         // Input data

                    // Send JSON
                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = jsonBody.getBytes("UTF-8");
                        os.write(input, 0, input.length);
                        os.flush();
                    }

                    // FastAPI response
                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) { // Kod 200

                        // Read API output
                        try (BufferedReader br = new BufferedReader(
                                new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                            StringBuilder response = new StringBuilder();
                            String responseLine;
                            while ((responseLine = br.readLine()) != null) {
                                response.append(responseLine.trim());
                            }

                            Log.d(TAG, "Server output data: " + response.toString());

                            // Miejsce na przetwarzanie pliku JSON
                        }
                    } else {
                        Log.e(TAG, "Server error: " + responseCode);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Network error", e);
                } finally {
                    // Close connection
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
        });
    }
}
