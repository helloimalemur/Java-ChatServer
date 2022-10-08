package net.koonts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client extends Thread{
    Client() {}

    @Override
    public void run() {
        Socket socket = null;
        try {
            socket = new Socket("127.1", 8888);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            if (socket.isConnected()) {
                String messageTo = null;
                String messageFrom = null;

                out.println("hello");
                System.out.println(in.readLine());


//            while ((messageFrom = in.readLine()).length() > 0) {
//                System.out.println(messageFrom);
//            }
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
