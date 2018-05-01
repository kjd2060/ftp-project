package com.kjd;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.net.*;
import java.io.*;
import java.text.*;
import java.util.*;

public class ClientHandler extends Thread {

    DateFormat fordate = new SimpleDateFormat("yyyy/MM/dd");
    DateFormat fortime = new SimpleDateFormat("hh:mm:ss");

    final DataInputStream in;
    final DataOutputStream out;
    final Socket s;

    public ClientHandler(Socket s, DataInputStream in, DataOutputStream out){
        this.s = s;
        this.in = in;
        this.out = out;
    }

    @Override
    public void run(){
        String received;
        String toreturn;
        while(true) {
            try {
                // ask user what want to do
                out.writeUTF("What do you want?;[Data | Time]..\n" +
                        "Type Exit to terminate connection.");

                // receive anser from client
                received = in.readUTF();

                if (received.equals("Exit")) {
                    System.out.println("Client " + this.s + " sends exit...");
                    System.out.println("Closing this connection.");
                    this.s.close();
                    System.out.println("Connection closed.");
                    break;
                }

                // creatin date object
                Date date = new Date();

                // write on output based on answer from client
                switch (received) {
                    case "Date":
                        toreturn = fordate.format(date);
                        out.writeUTF(toreturn);
                        break;
                    case "Time":
                        toreturn = fortime.format(date);
                        out.writeUTF(toreturn);
                        break;
                    default:
                        out.writeUTF("Invalid input");
                        break;
                }
            } catch (SocketException s){
                System.out.println(s);
                break;
            } catch (IOException i) {
                i.printStackTrace();
                break;
            } catch(Exception e){
                e.printStackTrace();
                break;
            }
        }
        try{
            // close resources
            this.in.close();
            this.out.close();
        } catch(IOException i){
            i.printStackTrace();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

}
