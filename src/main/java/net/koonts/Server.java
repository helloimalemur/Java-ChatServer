package net.koonts;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    boolean running = false;

    ArrayList<String> hosts = new ArrayList<>();
    ArrayList<Integer> ports = new ArrayList<>();
    Vector<Integer> vector = new Vector<>();
    LinkedList<Integer> linkedList = new LinkedList<>();
//    Queue<String> queue = new Queue<String>() { };
    HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
    SSLSocketFactory sslSocketFactory;
    ServerSocket serverSocket;
    public Server() throws IOException {
        sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        serverSocket = new ServerSocket();

    }

    public void startServer() throws IOException {
        running = true;
        System.out.println("Starting server");
        while (running) {
            System.out.println("Catching client");
            try (Socket socket = serverSocket.accept()) {
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(1);
                while (inputStream.available()>0) {
                    inputStream.read();
                }
            }
            ///clean up
        }
    }
}
