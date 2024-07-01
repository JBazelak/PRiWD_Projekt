package com.example.polinav3;

import android.util.Log;

import com.example.polinav3.gamepad.ButtonInput;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class NetworkController extends Thread{
    private Socket clientSocket;
    private BufferedReader br;
    private NetworkServer networkServer;

    public NetworkController(Socket client, NetworkServer networkServer){
        clientSocket=client;
        this.networkServer = networkServer;
    }

    @Override
    public void run(){
        try{
            Log.d("nep", "socket listening");
            br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String out;
            while((out = br.readLine())!=null){
                networkServer.Emit(Convert(out));
                Log.d("nep", out);
            }
        }
        catch(Exception e){
            System.out.println(e);
            Log.d("nep", e.toString());
        }
    }

    //convert String received via Internet into button type
    private ButtonInput Convert(String out) {


        return null;
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
