package com.kjd;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.net.*;
import java.io.*;
import java.nio.*;

import java.text.*;
import java.util.*;

public class ClientHandler extends Thread {

    DateFormat fordate = new SimpleDateFormat("yyyy/MM/dd");
    DateFormat fortime = new SimpleDateFormat("hh:mm:ss");

    String path = System.getProperty("user.dir");
    String dataDir = "/files/";
    String fullPath = path + dataDir;
    String workingDirectory = fullPath;

    File directory;

    final DataInputStream in;
    final DataOutputStream out;
    final Socket s;

    public ClientHandler(Socket s, DataInputStream in, DataOutputStream out){
        this.s = s;
        this.in = in;
        this.out = out;

        directory = new File(fullPath);
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
                            toreturn = "";
                            File workingDir = new File(workingDirectory);
                            File[] listOfFiles = workingDir.listFiles();
                            for(int i = 0; i < listOfFiles.length; i++){
                                toreturn += listOfFiles[i].getName() + "\n";
                            }
                            out.writeUTF(toreturn);
                            //System.out.println(toreturn);
                            //System.out.println("LSd");
                            break;
                        case "cd":
                            if (splited.length < 2) {
                                toreturn = "Usage: cd <directory name>";
                            } else {
                                String directoryDest = splited[1];
                                if(directoryDest.equals("..")){
                                    System.out.println("in .. if");
                                    if(workingDirectory.equals(fullPath)){
                                        out.writeUTF("At base allowable directory.");
                                        break;
                                    }
                                    workingDirectory = (new File(workingDirectory)).getParent();
                                    toreturn = workingDirectory;
                                    out.writeUTF(toreturn);
                                    break;
                                }
                                String ret = findDirectory(directoryDest, workingDirectory);
                                if(ret.isEmpty()){
                                    toreturn = "Directory does not exist.";
                                }
                                else{
                                    workingDirectory = ret;
                                    toreturn = workingDirectory;
                                }
                            }
                            out.writeUTF(toreturn);
                            break;
                        case "rget":
                            toreturn = "";
                            if (splited.length < 2) {
                                toreturn = "Usage: rget <filename>";
                            } else {
                                filename = splited[1];
                                String ret = findDirectory(filename, workingDirectory);

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
                        case "rput":
                            received = in.readUTF();
                            if(received.equals("FOUND")){
                                received = in.readUTF();
                                System.out.println(received);
                                File file = new File(workingDirectory + splited[1]);
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
                                received = in.readUTF();
                                System.out.println(received);
                            }
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
            byte[] data = Files.readAllBytes(path);

            out.writeInt(data.length);
            int count = 0;
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
