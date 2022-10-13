//SSL: https://stackoverflow.com/questions/25637039/detecting-ssl-connection-and-converting-socket-to-sslsocket
//https://stackoverflow.com/questions/53323855/sslserversocket-and-certificate-setup

//https://stackoverflow.com/questions/15405581/no-cipher-suites-in-common-while-establishing-a-secure-connection/15406581#15406581
//https://docs.oracle.com/javase/10/security/sample-code-illustrating-secure-socket-connection-client-and-server.htm#JSSEC-GUID-B1060A74-9BAE-40F1-AB2B-C8D83812A4C7
//https://www.amongbytes.com/post/201804-creating-certificates-for-ssl-testing/


package net.koonts;

import javax.net.ssl.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.*;

public class Server extends Thread {
    int port;
    boolean running = false;
    SSLSocketFactory sslSocketFactory;
    ServerSocket serverSocket;
    SSLContext sslContext;
    SSLServerSocketFactory sslServerSocketFactory;
    SSLSocket sslClientSocket;
    SSLSocket sslClient = null;
    ArrayList<ConnectionHandler> connections = new ArrayList<>();

    private static final int SERVER_PORT = 8888;
    private static final String TLS_VERSION = "TLSv1.3";
    private static final String CIPHER_SUITE = "TLS_AES_128_GCM_SHA256";
    private static final int SERVER_COUNT = 1;
    private static final String SERVER_HOST_NAME = "0.0.0.0";
//    private static final String TRUST_STORE_NAME = "servercert.p12";
//    private static final String KEY_STORE_NAME = "servercert.p12";
    private static final String TRUST_STORE_NAME = "ed25519.cert";
    private static final String KEY_STORE_NAME = "ed25519.cert";
    private static final char[] TRUST_STORE_PWD = new char[] {'t','e', 's', 't', '1', '2', '3'};;
    private static final char[] KEY_STORE_PWD = new char[] {'t','e', 's', 't', '1', '2', '3'};

    //SSL config

    //
    public Server() {
        this.port = 8888;
    }
    public Server(int port) {
        this.port = port;
    }


    public void startServer(int port, String tlsVersion, String trustStoreName, char[] trustStorePassword, String keyStoreName, char[] keyStorePassword) throws IOException {
        running = true;
        if (port<=0) {throw new IllegalArgumentException("Invalid port");}

        System.out.println("Starting server");

        //setup keystore
        System.out.println("Setting up Keystore");
        try {
            //
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream tstore = Server.class.getResourceAsStream("/" + trustStoreName);
            trustStore.load(tstore, trustStorePassword);
            if (tstore != null) {tstore.close();}
//            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            System.out.println("trust manager factory");
            System.out.println(trustManagerFactory.getAlgorithm());
            trustManagerFactory.init(trustStore);
            //
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream kstore = Server.class.getResourceAsStream("/" + keyStoreName);
            keyStore.load(kstore, keyStorePassword);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            System.out.println(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePassword);
            System.out.println(keyManagerFactory.getKeyManagers().length);
            System.out.println(keyManagerFactory.getAlgorithm());
            //
            sslContext = SSLContext.getInstance(TLS_VERSION);
//            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), SecureRandom.getInstanceStrong());
            System.out.println("Using: " + sslContext.getProtocol());
            sslServerSocketFactory = sslContext.getServerSocketFactory();
            serverSocket = sslServerSocketFactory.createServerSocket(port);






        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException |
                 KeyManagementException exception) {
            System.out.println(exception.getMessage());

        }
        //
        //





        while (running) {
            try {
                //handle client connection
//                sslClientSocket.setEnabledProtocols(new String[]{"TLSv1.2"});
                //add ConnectionHandler ch to connections ArrayList<>
//                Socket clientSocket = serverSocket.accept();
                System.out.println("Waiting on connection..");
                sslClientSocket = (SSLSocket) serverSocket.accept();
                sslClientSocket.setEnabledProtocols(new String[] {TLS_VERSION});
                sslClientSocket.setEnabledCipherSuites(new String[] {CIPHER_SUITE});
                sslClientSocket.getNeedClientAuth();
                System.out.println("client connected: " + sslClientSocket.getInetAddress().getHostAddress());
                System.out.println("Adding to connection Handler");
                ConnectionHandler ch = new ConnectionHandler(sslClientSocket);
                connections.add(ch);
                ch.start();
            } catch (Exception e) {
                //TODO: handle this
            }
        }
    }

    @Override
    public void run() {
        try {
            startServer(SERVER_PORT, TLS_VERSION, TRUST_STORE_NAME,
                    TRUST_STORE_PWD, KEY_STORE_NAME, KEY_STORE_PWD);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    class ConnectionHandler extends Thread {

        public String nickname;
        String hostAddress;
        PrintWriter out;
        BufferedReader in;
        Socket client;

        ConnectionHandler(Socket client) throws IOException {
            this.client = client;
            this.hostAddress = client.getInetAddress().getHostAddress();
        }
        public SSLSocket moveToSSL(Socket client) {

            try {
//                System.setProperty("https.protocols", "TLSv1.2");
                sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                sslClient = (SSLSocket) sslSocketFactory.createSocket(client, null, client.getPort(), false);
                
            } catch (IOException e) {
                //TODO: catch
            }
            return sslClient;
        }
        @Override
        public void run() {
            System.out.println("Client Thread started..");

            if (client.isConnected()) {
                try {
                    client = moveToSSL(client);/////////
                    out = new PrintWriter(client.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(client.getInputStream()));


                    out.println("Welcome!");
                    out.println("Please enter nickname:");// first word sent is 'nickname'
                    String tmp = in.readLine().split(" ")[0];// otherwise address is used
                    nickname = tmp;
                    if (tmp.equals("")) {
                        out.println("Nothing Entered");
                        nickname = hostAddress;
                    }
                    out.println("Hello " + nickname);

                    String messageFromClient; //begin reading input into chat
                    while ((messageFromClient = in.readLine()) != null) { //redundant comparators in place to catch excessive
                        if (nickname != null && hostAddress != null && messageFromClient.length() != 0) {//null when client
                            System.out.println(hostAddress + ":" + nickname + ":: " + messageFromClient);// abruptly disconnects.
                        } else {
                            if (hostAddress != null && messageFromClient.length() != 0) {//
                                System.out.println(hostAddress + ":: " + messageFromClient);
                            }
                        }

                        //if client message begins with "/" process as command
                        if (messageFromClient.startsWith("/")) {
//                            System.out.println("received modifier..");
                            setOption(messageFromClient);

                        } else if(messageFromClient.length() != 0) {
                            if (nickname != null) {
                                broadcast(nickname + ": " + messageFromClient);
                            } else {
                                broadcast(hostAddress + ": " + messageFromClient);
                            }

                        } else {
                            out.println("Please enter a message..");
                        }
                    }

                    //report disconnection to server log
                    if (nickname != null) {
                        System.out.println(hostAddress + " : " + nickname + " <<Disconnected>>");
                    } else {
                        System.out.println(hostAddress + " : " + " <<Disconnected>>");
                    }

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        private void setOption(String options) throws InterruptedException {
            //user entered string beginning with '/'
            String[] messageSplit = options.split(" ");
            if (messageSplit.length>0) {
                ///set nickname
                if (messageSplit[0].startsWith("/nickname")) {
                    this.nickname = messageSplit[1];
                    broadcast(hostAddress + " has changed nickname to: " + nickname);
                }
                //display help message
                if (options.startsWith("/help")) {
                    String helpMessage = "/help for this, /nickname to change nickname, /quit to exit";
//                    out.println(helpMessage);
                    broadcast(helpMessage);
                }
                if (options.startsWith("/killserver")) {
                    Thread.sleep(3000);
                    System.exit(0);
                }
            }
        }

        private void broadcast(String message) {
            //loop over connections ArrayList<ConnectionHandler>
            //to broadcast socket message
            for (ConnectionHandler connection : connections) {
                connection.out.println(message);
            }
        }

    }
    public static void main(String[] args) {Runnable runnable = () -> {Server server = new Server();server.start();};runnable.run();}
}
