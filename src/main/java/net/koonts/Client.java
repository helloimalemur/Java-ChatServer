package net.koonts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client extends Thread{
    boolean running = false;
    Socket socket;
    PrintWriter out;
    BufferedReader in;
    String host;
    int port;
    Client() {
        this.host = "127.1";
        this.port = 8888;
    }
    Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static void main(String[] args) {Runnable runnable = () -> {Client client = new Client();client.start();};runnable.run();}
    @Override
    public void run() {
        running = true;
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));


            InputHandler inputHandler = new InputHandler();
            inputHandler.start();

            String messageTo = null;
            String messageFrom = null;

            while ((messageFrom = in.readLine()).length() > 0) {
                System.out.println(messageFrom);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void shutdown() {
        try {
            out.flush();
            out.close();
            in.close();
            socket.close();
            System.exit(0);
        } catch(IOException e) {
            //TODO: handle exception
        }
    }

    class InputHandler extends Thread {
        InputHandler() {


        }

        @Override
        public void run() {
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (running) {
                    String message = inReader.readLine();
                    if (message.startsWith("/quit")) {
                        inReader.close();
                        shutdown();
                    }
                }
            } catch(IOException e) {
            }
        }
    }
}
