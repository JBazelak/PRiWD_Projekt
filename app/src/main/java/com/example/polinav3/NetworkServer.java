package com.example.polinav3;

import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class NetworkServer extends Thread {
    private ServerSocket serverSocket;
    private int port = 30000;


    private Socket clientSocket;
    private BufferedReader br;

    //przechowuje i emituje Socket'y
//    private List<Thread> controllers;

    public void run(){
//        controllers = new ArrayList<Thread>();
        try {
            Log.d("nep", "thread start");
            serverSocket = new ServerSocket(port);
            clientSocket = serverSocket.accept();
            Log.d("nep", "socket accept");
            br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String out;
//            out = br.readLine();
            while((out = br.readLine())!=null){
                Log.d("nep", out);
            }

        } catch(Exception e) {
            System.out.println(e);
        }
        Log.d("nep", "test");
    }
    public void shutdown(){
        try {
            br.close();
            clientSocket.close();
            serverSocket.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }


}
