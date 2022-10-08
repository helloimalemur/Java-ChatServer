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
            Server server = null;
            try {
                server = new Server();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            server.run();
        };
        runnable.run();
    }
}