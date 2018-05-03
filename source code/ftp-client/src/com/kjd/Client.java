package com.kjd;

import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Client {
    private Socket socket = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;
    boolean connected = false;

    final String loginGood = "Authenticated.  Logging you in.";
    String base = System.getProperty("user.dir");
    String fullpath = base + "/files/";
    String toreturn = "";
    String workingDirectory = fullpath;
    String sendDirectory = fullpath + "/send/";
    public Client() {
        try{
            Scanner scn = new Scanner(System.in);

            // get localhost ip
            InetAddress ip = null;

            Socket s = null;

            // perform info exchange between client + handler
            while(true){
                if(connected){
                    System.out.print(in.readUTF());
                    String tosend = scn.nextLine();
                    out.writeUTF(tosend);

                    String[] splited = tosend.split("\\s+");
                    String received = "";
                    // if client sends exit, close connection + break from loop
                    if(tosend.equals("Exit")){
                        System.out.println("Closing connection: " + s);
                        s.close();
                        System.out.println("Connection closed");
                        break;
                    }

                    switch(splited[0]){
                        case "cd":
                            received = in.readUTF();
                            if(received.equals("At base allowable directory.") ||
                                    received.equals("Directory does not exist.") || received.equals("")){
                                break;
                            }
                            else{
                                workingDirectory = received;
                            }
                            break;
                        case "rget":
                            received = in.readUTF();
                            if(received.equals("FOUND")){
                                received = in.readUTF();
                                System.out.println(received);
                                File file = new File(fullpath + splited[1]);
                                System.out.println(file.getAbsolutePath());
                                file.createNewFile();
                                FileOutputStream fos = new FileOutputStream(file);
                                out.writeUTF("READY");
                                int length = in.readInt();
                                byte byteToWrite;
                                for(int i = 0; i < length; i++){
                                    byteToWrite = in.readByte();
                                    fos.write(byteToWrite);
                                }
                                //received = in.readUTF();
                            }
                            break;
                        case "rput":
                            toreturn = "";
                            if (splited.length < 2) {
                                toreturn = "Usage: rget <filename>";
                            } else {
                                String filename = splited[1];
                                String ret = findDirectory(filename, sendDirectory);

                                if(ret.isEmpty()){
                                    toreturn = "File does not exist";
                                }
                                else{
                                    File retFile = new File(ret);
                                    if(retFile.isDirectory()){
                                        toreturn = "Cannot get a directory.";
                                    }
                                    else{
                                        toreturn = "FOUND";
                                        out.writeUTF(toreturn);
                                        toreturn = "Found file: " + retFile.getName() + " writing now.";
                                        out.writeUTF(toreturn);
                                        String proceed = in.readUTF();
                                        if(proceed.equals("READY")){
                                            writeFile(retFile);
                                        }
                                        toreturn = "Got you file: " + retFile.getName();
                                    }
                                }
                            }
                            out.writeUTF(toreturn);
                            break;
                        default:
                            break;
                    }

                    received = in.readUTF();
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

    private String findDirectory(String name, String start){
        File file = new File(start);
        if(!file.isDirectory())
        {
            return "";
        }
        File[] listOfFiles = file.listFiles();
        String ret = "";
        for(int i = 0; i < listOfFiles.length; i++) {
            //System.out.println(name + ", " + listOfFiles[i].getName());
            if(name.equals(listOfFiles[i].getName())){
                ret = listOfFiles[i].getAbsolutePath();
                return ret;
            }
            ret = findDirectory(name, start+listOfFiles[i].getName());
        }
        return ret;
    }

    private void writeFile(File file){
        try {
            Path path = Paths.get(file.getAbsolutePath());
            //System.out.println(path.toString());
            byte[] data = Files.readAllBytes(path);

            if(data == null){
                System.out.println("Empty file.");
                out.writeInt(0);
                return;
            }
            out.writeInt(data.length);
            for(int i = 0; i < data.length; i++)
            {
                out.writeByte(data[i]);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
