package org.example;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    public Server() {
        sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

    }

    public void startServer() {
        running = true;
        while (running) {
            System.out.println("Starting server");
            try (SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket();) {
                InputStream inputStream = sslSocket.getInputStream();
                OutputStream outputStream = sslSocket.getOutputStream();
                outputStream.write(1);
                while (inputStream.available()>0) {
                    System.out.println(inputStream.read());
                }
            } catch (Exception e) {
                running = false;
                System.out.println(e.getMessage());
            }
            ///clean up
        }
    }
}
