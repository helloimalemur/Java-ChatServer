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

        public String nickname;
        String hostAddress;
        PrintWriter out;
        BufferedReader in;
        Socket client;

        ConnectionHandler(Socket client) throws IOException {
            this.client = client;
            this.hostAddress = client.getInetAddress().getHostAddress();
        }
        @Override
        public void run() {
            System.out.println("Client Thread started..");

            if (client.isConnected()) {
                try {
                    out = new PrintWriter(client.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(client.getInputStream()));


                    out.println("Welcome!");
                    out.println("Please enter nickname: ");// first word sent is 'nickname'
                    String tmp = in.readLine().split(" ")[0];// otherwise address is used
                    nickname = tmp;
                    if (tmp.equals("")) {
                        out.println("Nothing Entered");
                        nickname = hostAddress;
                    }
                    out.println("Hello " + nickname);

                    String messageFromClient; //begin reading input into chat
                    while ((messageFromClient = in.readLine()) != null) { //redundant comparators in place to catch excessive
                        if (nickname != null && hostAddress != null && messageFromClient.length() != 0) {//null when client
                            System.out.println(hostAddress + ":" + nickname + ":: " + messageFromClient);// abruptly disconnects.
                        } else {
                            if (hostAddress != null && messageFromClient.length() != 0) {//
                                System.out.println(hostAddress + ":: " + messageFromClient);
                            }
                        }

                        //if client message begins with "/" process as command
                        if (messageFromClient.startsWith("/")) {
//                            System.out.println("received modifier..");
                            setOption(messageFromClient);

                        } else if(messageFromClient.length() != 0) {
                            if (nickname != null) {
                                broadcast(nickname + ": " + messageFromClient);
                            } else {
                                broadcast(hostAddress + ": " + messageFromClient);
                            }

                        } else {
                            out.println("Please enter a message..");
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
                ///set nickname
                if (messageSplit[0].startsWith("/nickname")) {
                    this.nickname = messageSplit[1];
                    broadcast(hostAddress + " has changed nickname to: " + nickname);
                }
                //display help
                if (messageSplit[0].startsWith("/help")) {
                    this.nickname = messageSplit[1];
                    String helpMessage = "/help for this, /nickname to change nickname, /quit to exit";
                    broadcast(helpMessage);
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
