package com.kjd;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
    private Socket socket = null;
    private DataInputStream input = null;
    private DataOutputStream out = null;

    public Client() {
        try{
            Scanner scn = new Scanner(System.in);

            // get localhost ip
            InetAddress ip = InetAddress.getByName("localhost");

            Socket s = new Socket(ip, 5000);

            // obtain in / out
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            // perform info exchange between client + handler
            while(true){
                System.out.println(in.readUTF());
                String tosend = scn.nextLine();
                out.writeUTF(tosend);

                // if client sends exit, close connection + break from loop
                if(tosend.equals("Exit")){
                    System.out.println("Closing connection: " + s);
                    s.close();
                    System.out.println("Connection closed");
                    break;
                }

                String received = in.readUTF();
                System.out.println(received);
            }

            // close resources
            scn.close();
            in.close();
            out.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
