package com.kjd;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
    private Socket socket = null;
    private DataInputStream input = null;
    private DataOutputStream out = null;
    boolean connected = false;

    final String loginGood = "Authenticated.  Logging you in.";

    public Client() {
        try{
            Scanner scn = new Scanner(System.in);

            // get localhost ip
            InetAddress ip = null;

            Socket s = null;

            // obtain in / out
            DataInputStream in = null;
            DataOutputStream out = null;

            // perform info exchange between client + handler
            while(true){
                if(connected){
                    System.out.print(in.readUTF());
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
                else{
                    String tosend = scn.nextLine();
                    if(tosend.equals("Exit")){
                        System.out.println("Exiting!");
                        break;
                    }
                    else{
                        String[] splited = tosend.split("\\s+");
                        if(splited[0].equals("rftp")){
                            if(splited.length < 4){
                                System.out.println("Usage: rftp<server addr><username><password>");
                                continue;
                            }
                            else{
                                String serverAddr = splited[1];
                                String uname = splited[2];
                                String password = splited[3];
                                ip = InetAddress.getByName(serverAddr);
                                s = new Socket(ip, 5000);
                                in = new DataInputStream(s.getInputStream());
                                out = new DataOutputStream(s.getOutputStream());
                                out.writeUTF(tosend);
                                String response = in.readUTF();
                                System.out.println(response);
                                if(response.equals(loginGood)){
                                    connected = true;
                                }
                                else{
                                    connected = false;
                                }
                            }
                        }
                        else{
                            System.out.println("Commands: rftp <server addr><username><password>, Exit");
                        }
                    }
                }
            }

            // close resources
            if(scn != null){
                scn.close();
            }
            if(in != null){
                in.close();
            }
            if(out != null){
                out.close();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
