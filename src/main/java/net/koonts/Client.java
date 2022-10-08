package net.koonts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client extends Thread{
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

    @Override
    public void run() {
        Socket socket = null;
        try {
//            socket = new Socket("127.1", 8888);
//            socket = new Socket("10.150", 8888);
            socket = new Socket(host, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            if (socket.isConnected()) {
                String messageTo = null;
                String messageFrom = null;

                out.println("hello");
                out.println("/nickname john");
                System.out.println(in.readLine());


            while ((messageFrom = in.readLine()).length() > 0) {
                System.out.println(messageFrom);
            }
                ///out.println("");



                out.flush();
                out.close();
                in.close();
                socket.close();}
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
