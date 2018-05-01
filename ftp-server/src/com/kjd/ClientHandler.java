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
        boolean authenticated = false;

        while(true) {
            try {

                if(!authenticated){
                    // receive anser from client
                    received = in.readUTF();

                    String[] splited = received.split("\\s+");

                    // write on output based on answer from client
                    String filename;
                    switch(splited[0]){
                        case "rftp":
                            toreturn = "";
                            if(splited.length < 4){
                                toreturn = "Usage: rftp <FTP Server Address><Username><Password>";
                            }
                            else{
                                String serverAddr = splited[1];
                                String user = splited[2];
                                String password = splited[3];

                                FileReader fileReader = new FileReader("users.txt");
                                BufferedReader fileBuffer = new BufferedReader(fileReader);

                                String line = "";
                                while((line = fileBuffer.readLine()) != null){
                                    if(line.contains("//")){
                                        // ignore line
                                        continue;
                                    }
                                    else {
                                        String[] arr = line.split(",");
                                        String uname = arr[0].split(":")[1];
                                        uname = uname.replaceAll("\\s+", ""); // replace whitespace
                                        String pw = arr[1].split(":")[1];
                                        pw = pw.replaceAll("\\s+", "");
                                        System.out.println(uname + ", " + pw);
                                        if(uname.equals(user) && pw.equals(password)){
                                            authenticated = true;
                                            toreturn = "Authenticated.  Logging you in.";
                                        }
                                        else{
                                            toreturn = "Invalid username or password";
                                            authenticated = false;
                                            out.writeUTF(toreturn);
                                            s.close();
                                        }
                                    }
                                }
                            }
                            out.writeUTF(toreturn);
                            break;
                        default:
                            toreturn = "Please log in to use features.";
                            out.writeUTF(toreturn);
                            break;
                    }
                }
                else {
                    // ask user what want to do
                    out.writeUTF("~");

                    // receive anser from client
                    received = in.readUTF();

                    String[] splited = received.split("\\s+");

                    // write on output based on answer from client
                    String filename;
                    if (received.equals("Exit")) {
                        System.out.println("Client " + this.s + " sends exit...");
                        System.out.println("Closing this connection.");
                        this.s.close();
                        System.out.println("Connection closed.");
                        break;
                    }

                    switch (splited[0]) {
                        case "ls":
                            toreturn = "list the files in the directory here";
                            out.writeUTF(toreturn);
                            break;
                        case "cd":
                            if (splited.length < 2) {
                                toreturn = "Usage: cd <directory name>";
                            } else {
                                String directoryDest = splited[1];
                                toreturn = directoryDest + "/";
                            }
                            out.writeUTF(toreturn);
                            break;
                        case "rget":
                            toreturn = "";
                            if (splited.length < 2) {
                                toreturn = "Usage: rget <filename>";
                            } else {
                                filename = splited[1];
                            }
                            out.writeUTF(toreturn);
                            break;
                        case "rput":
                            toreturn = "";
                            if (splited.length < 2) {
                                toreturn = "Usage: rput <filename>";
                            } else {
                                filename = splited[1];
                            }
                            out.writeUTF(toreturn);
                            break;
                        default:
                            out.writeUTF("Invalid input");
                            break;
                    }
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
