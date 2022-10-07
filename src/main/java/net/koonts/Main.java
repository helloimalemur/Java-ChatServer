package net.koonts;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) {
        Runnable runnable = () -> {
            try {
                Server server = new Server();
                server.startServer();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        runnable.run();
    }
}