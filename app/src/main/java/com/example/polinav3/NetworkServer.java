package com.example.polinav3;

import android.util.Log;
import android.widget.TextView;

import com.example.polinav3.gamepad.ButtonConnector;
import com.example.polinav3.gamepad.ButtonInput;
import com.example.polinav3.gamepad.ButtonListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class NetworkServer extends Thread implements ButtonConnector {
    private ServerSocket serverSocket;
    private int port = 30000;

    private Socket clientSocket;

    //przechowuje i emituje Socket'y
    private List<NetworkController> controllers;
    private List<ButtonListener> buttonListeners;

    public void run(){
        controllers = new ArrayList<NetworkController>();
        buttonListeners = new ArrayList<ButtonListener>();
        try {
            Log.d("nep", "thread start");
            serverSocket = new ServerSocket(port);

            while(true){
                clientSocket = serverSocket.accept();
                Log.d("nep", "client accept");
                controllers.add(new NetworkController(clientSocket, this));
                controllers.get(controllers.size()-1).run();
            }


        } catch(Exception e) {
            System.out.println(e);
            Log.d("nep", e.toString());
        }
//        Log.d("nep", "test");
    }
    public void shutdown(){
        try {
            int i=0;
            for(NetworkController nc : controllers){
                nc.shutdown();
                nc.interrupt();
                Log.d("nep", "Controller "+i);
                i++;
            }
            serverSocket.close();
        } catch (Exception e) {
            System.out.println(e);
            Log.d("nep", e.toString());
        }
    }


    @Override
    public void addListener(ButtonListener listener) {
        buttonListeners.add(listener);
    }
    public void Emit(ButtonInput input){
        for (ButtonListener button : buttonListeners) {
            button.onButtonPressed(input);
        }
    }
}
