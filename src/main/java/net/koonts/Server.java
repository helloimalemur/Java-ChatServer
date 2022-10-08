package net.koonts;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server extends Thread {
    int port;

    boolean running = false;
    ArrayList<ConnectionHandler> connections = new ArrayList<>();

    ///
    public Server() {
        this.port = 8888;
    }
    public Server(int port) {
        this.port = port;
    }
    public void startServer() throws IOException {
        running = true;
        System.out.println("Starting server");
        ServerSocket serverSocket = new ServerSocket(port);
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

        String nickname;
        String hostAddress;
        PrintWriter out;
        BufferedReader in;
        Socket client;

        ConnectionHandler(Socket client) throws IOException {
            this.client = client;
            this.hostAddress = client.getInetAddress().getHostAddress();
//            this.nickname = hostAddress;
        }
        @Override
        public void run() {
            System.out.println("Client Thread started..");

            if (client.isConnected()) {
                try {
                    out = new PrintWriter(client.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(client.getInputStream()));


                    out.println("Welcome!");
                    out.println("Please enter nickname: ");
                    String tmp = in.readLine().split(" ")[0];
                    if (tmp != null) {
                        nickname = tmp;
                        out.println("Hello " + nickname);
                    }

                    String messageFromClient;


                    while ((messageFromClient = in.readLine()) != null) {
                        if (nickname != null && hostAddress != null && messageFromClient != null) {
                            System.out.println(hostAddress + ":" + nickname + ":: " + messageFromClient);
                        } else {
                            if (hostAddress != null && messageFromClient != null) {
                                System.out.println(hostAddress + ":: " + messageFromClient);
                            }
                        }

                        //if client message begins with "/" process as command
                        if (messageFromClient.startsWith("/")) {
//                            System.out.println("received modifier..");
                            setOption(messageFromClient);

                        } else {
                            if (nickname != null) {
                                broadcast(nickname + ": " + messageFromClient);
                            } else {
                                broadcast(hostAddress + ": " + messageFromClient);
                            }
                        }
                    }

                    //report disconnection to server log
                    if (nickname != null) {
                        System.out.println(hostAddress + " : " + nickname + " <<Disconnected>>");
                    } else {
                        System.out.println(hostAddress + " : " + " <<Disconnected>>");
                    }

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        private void setOption(String options) {
            String[] messageSplit = options.split(" ");

            if (messageSplit.length == 2) {
                if (messageSplit[0].startsWith("/nickname")) {
                    this.nickname = messageSplit[1];
                    broadcast(hostAddress + " has changed nickname to: " + nickname);
                }
            }
        }

        private void broadcast(String message) {
            for (ConnectionHandler connection : connections) {
//                System.out.println(nickname + ": " + message);
//                connection.out.println("<<BROADCAST>>");
                connection.out.println(message);
//                if (nickname != null) {
////                    connection.out.println(hostAddress + " : " + nickname + ": " + message);
//                    connection.out.println(nickname + ": " + message);
//                } else {
//                    connection.out.println(hostAddress + ": " + message);
//                }
                connection.out.flush();
            }
        }
    }
    public static void main(String[] args) {Runnable runnable = () -> {Server server = new Server(8888);server.start();};runnable.run();}
}
