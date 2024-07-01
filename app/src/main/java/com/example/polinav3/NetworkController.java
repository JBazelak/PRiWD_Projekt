package com.example.polinav3;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class NetworkController extends Thread{
    private Socket clientSocket;
    private BufferedReader br;


    public NetworkController(Socket client){
        clientSocket=client;
    }

    @Override
    public void run(){
        try{
            Log.d("nep", "socket listening");
            br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String out;
            while((out = br.readLine())!=null){
                Log.d("nep", out);
            }
        }
        catch(Exception e){
            System.out.println(e);
            Log.d("nep", e.toString());
        }
    }
    public void shutdown(){
        try{
            br.close();
            clientSocket.close();
        }
        catch(Exception e){
            System.out.println(e);
            Log.d("nep", e.toString());
        }
    }

}
