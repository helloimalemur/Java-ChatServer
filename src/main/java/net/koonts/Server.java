package net.koonts;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server extends Thread {
    boolean running = false;

    ArrayList<ConnectionHandler> connections = new ArrayList<>();

    public Server() throws IOException {


    }

    public void startServer() throws IOException {
        running = true;
        System.out.println("Starting server");
        ServerSocket serverSocket = new ServerSocket(8888);
        while (running) {

            System.out.println("Ready for client");
            try {

                Socket clientSocket = serverSocket.accept();
                System.out.println("Connecting to client: " + clientSocket.getInetAddress().getHostAddress());
                ConnectionHandler ch = new ConnectionHandler(clientSocket);
                connections.add(ch);
                ch.start();


            } catch (Exception e) {}
            ///

        }
    }

    @Override
    public void run() {
        try {
            startServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    class ConnectionHandler extends Thread {

        Socket client;
        ConnectionHandler(Socket client) throws IOException {
            this.client = client;
        }
        @Override
        public void run() {
            System.out.println("Client Thread started..");

            if (client.isConnected()) {
                try {
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                    String messageToClient = "Welcome..";
                    String messageFromClient;

                    out.println(messageToClient);
//                    out.println("");//client disconnects on zero length string

                    while ((messageFromClient = in.readLine()) != null) {
                        System.out.println(messageFromClient);
                    }
                    System.out.println("Client Disconnected");

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
