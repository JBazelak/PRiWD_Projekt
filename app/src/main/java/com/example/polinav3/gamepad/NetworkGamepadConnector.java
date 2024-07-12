package com.example.polinav3.gamepad;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class NetworkGamepadConnector implements ButtonConnector, AutoCloseable {
    private static int port = 30000;
    private ButtonListener listener;
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private ServerSocket server;
    private AtomicReference<Socket> client = new AtomicReference<>(null);
    private BufferedReader reader;
    private BufferedWriter writer;

    @Override
    public void addListener(ButtonListener listener) {
        this.listener = listener;
    }

    public void start() throws IOException {
        if (isRunning.compareAndSet(false, true)) {
            server = new ServerSocket(port);
            isRunning.set(true);
            new Thread(this::pollRequests).start();
        }
    }

    @Override
    public void close() throws Exception {
        isRunning.set(false);
        Socket cl = client.get();
        if (cl != null) {
            cl.close();
        }
    }

    private void pollRequests() {
        Log.d("net", "start poll");
        while (isRunning.get()) {
            Log.d("net", "busy wait");
            try {
                if (client.get() == null) {
                    Socket sock = server.accept();
                    Log.d("net", "accepted");
                    if (sock == null) continue;
                    Log.d("net", "client non null");
                    client.set(sock);
                    reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                    writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
                } else {
                    String line = reader.readLine();
                    Log.d("net", "received: " + line);
                    if (line == null) {
                        Log.d("net", "setting client to null");
                        Socket cl = client.getAndSet(null);
                        cl.close();
                        continue;
                    }
                    String[] parts = line.split(" ");
                    Log.d("net", "parts: " + Arrays.toString(parts));
                    float ax = 0;
                    float ay = 0;
                    ButtonType button = ButtonType.byId(Integer.parseInt(parts[0]));
                    Log.d("net", "button: " + button);
                    if (button == ButtonType.LStick || button == ButtonType.RStick) {
                        if (parts.length < 3) {
                            Log.d("error", "stick but not enough params");
                        } else {
                            ax = Float.parseFloat(parts[1]);
                            ay = Float.parseFloat(parts[2]);
                        }
                    }
                    Log.d("net", "about to on button press");
                    listener.onButtonPressed(new ButtonInput(button, ax, ay));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
