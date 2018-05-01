package com.kjd;

import java.net.*;
import java.io.*;

public class Server {
    private ServerSocket server = null;

    public Server(int port){
        // start server & wait for connection
        boolean run = true;
        try{
            server = new ServerSocket(port);
            System.out.println("Server started.");

            while(true) {
                AcceptConnection();
            }
        }
        catch(SocketException s){
            System.out.println(s);
        }
        catch (IOException i){
            System.out.println(i);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void AcceptConnection(){
        Socket socket = null;
        try {
            System.out.println("Waiting for a client...");

            socket = server.accept();

            System.out.println("Client accepted: " + socket);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            System.out.println("Assigning new thread for client.");

            Thread t = new ClientHandler(socket, in, out);

            t.start();
        }
        catch(Exception e){
            CloseConnection(socket);
            e.printStackTrace();
        }
    }

    private void CloseConnection(Socket socket) {
        try {
            System.out.println("Closing connection.");

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
