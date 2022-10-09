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
        if (args.length>0) {
            switch (args[0]) {
                case "client":
                    client();
                    break;
                default:
                    server();
                    break;
            }
        } else {server();}
    }

    static void server() {
        Server server = new Server(8888);
        server.start();
    }
    static void client() {
        Client client = new Client();
        client.start();
    }
}