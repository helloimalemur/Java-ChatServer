package net.koonts;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Arrays;

public class Client extends Thread{

    boolean running = false;
    Socket socket;
    PrintWriter out;
    BufferedReader in;
    String host;
    int port;
    InputHandler inputHandler;
    SSLSocketFactory sslSocketFactory;
    SSLSocket sslSocket;

    private static final int SERVER_PORT = 8888;
    private static final String TLS_VERSION = "TLSv1.2";
    private static final int SERVER_COUNT = 1;
    private static final String SERVER_HOST_NAME = "127.0.0.1";
    private static final String TRUST_STORE_NAME = "servercert.p12";
    private static final char[] TRUST_STORE_PWD = new char[] {'a', 'b', 'c', '1',
            '2', '3'};
    private static final String KEY_STORE_NAME = "servercert.p12";
    private static final char[] KEY_STORE_PWD = new char[] {'a', 'b', 'c', '1',
            '2', '3'};

    public Client() {
        this.host = "10.150";
        this.port = 8888;
    }
    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

//    public static void main(String[] args) {Runnable runnable = () -> {Client client = new Client("10.150",8888);client.start();};runnable.run();}
    @Override
    public void run() {
        running = true;
        try {
            //create initial non ssl socket
            socket = new Socket(host, port);

            //check for ssl
            if (true) {
                sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                sslSocket = (SSLSocket) sslSocketFactory.createSocket(socket, null, false);

            }

//            SSLSession session = sslSocket.getHandshakeSession();
            SSLSession session = sslSocket.getSession();
            //debug
            System.out.println(session.getCipherSuite());
            System.out.println(session.getProtocol());
            System.out.println(Arrays.toString(session.getLocalCertificates()));

            out = new PrintWriter(sslSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            inputHandler = new InputHandler();
            inputHandler.start();

            String messageTo;
            String messageFrom = in.readLine();
            System.out.println(messageFrom);//Welcome message does not get preceding '>'

            while ((messageFrom = in.readLine()).length() > 0) {
                System.out.println(messageFrom); //nickname prompt does not get preceding '>'
                System.out.print(">");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void shutdown() {
        try {
            running = false;
//            out.close();
//            in.close();
            sslSocket.close();
            inputHandler.interrupt();

            System.exit(0);
        } catch(IOException e) {
            //TODO: handle exception

        }
    }

    class InputHandler extends Thread {

        InputHandler() {


        }
        @Override
        public void interrupt() {

            running = false;
        }

        @Override
        public void run() {
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (running) {
                    String message = inReader.readLine();
                    if (message != null) {


                        if (message.startsWith("/quit")) {
                            inReader.close();
                            shutdown();
                        }
                        if (message.startsWith("/killserver")) {
                            out.println(message);
                            inReader.close();
                            shutdown();
                        }
                    }
                    out.flush();
                    out.println(message);
                }
            } catch(IOException e) {
                shutdown();
            }
        }

    }
    public static void main(String[] args) {Runnable runnable = () -> {Client client = new Client();client.start();};runnable.run();}
}
