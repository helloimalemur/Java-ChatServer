// https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html
package net.koonts;

import javax.net.SocketFactory;
import javax.net.ssl.*;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;

public class Client extends Thread{

    boolean running = false;
    SSLSocket socket;
    SSLSocket connection;
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
        this.host = "127.1";
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
            ///
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream tstore = Client.class.getResourceAsStream("/" + TRUST_STORE_NAME);
            trustStore.load(tstore, TRUST_STORE_PWD);
            if (tstore != null) {tstore.close();}
//            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(trustStore);
            System.out.println(trustManagerFactory.getTrustManagers().length);
            System.out.println(trustManagerFactory.getAlgorithm());
            //
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream kstore = Client.class.getResourceAsStream("/" + KEY_STORE_NAME);
            keyStore.load(kstore, KEY_STORE_PWD);
//            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, KEY_STORE_PWD);
//            //
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), SecureRandom.getInstanceStrong());
            //
            SocketFactory factory = sslContext.getSocketFactory();


            try {
                connection = (SSLSocket) factory.createSocket(host, port);
                connection.startHandshake();
                connection.setEnabledProtocols(new String[] {TLS_VERSION});
                System.out.println(connection.getApplicationProtocol());
                System.out.println(connection.getHandshakeApplicationProtocol());
                SSLParameters sslParameters = new SSLParameters();
                sslParameters.setEndpointIdentificationAlgorithm("HTTPS");
                connection.setSSLParameters(sslParameters);
                connection.startHandshake();
            } catch (Exception ignored) {

            }

            ///
            out = new PrintWriter(connection.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            inputHandler = new InputHandler();
            inputHandler.start();

            String messageTo;
            String messageFrom = in.readLine();
            System.out.println(messageFrom);//Welcome message does not get preceding '>'

            while ((messageFrom = in.readLine()).length() > 0) {
                System.out.println(messageFrom); //nickname prompt does not get preceding '>'
                System.out.print(">");
            }
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException |
                 KeyManagementException | UnrecoverableKeyException e) {
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
