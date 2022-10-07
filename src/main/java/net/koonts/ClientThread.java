package net.koonts;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientThread extends Thread {
    Socket socket;
    SSLSocket sslSocket;
    InputStream inputStream;
    OutputStream outputStream;
    ClientThread(Socket socket) throws IOException {
        this.socket = socket;
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();

    }
    @Override
    public void run() {
        System.out.println("Client Thread started..");
        while (true) {
            try {
            InputStream inputStream = sslSocket.getInputStream();
            OutputStream outputStream = sslSocket.getOutputStream();
            outputStream.write(1);
            while (inputStream.available()>0) {
                System.out.println(inputStream.read());
            }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
