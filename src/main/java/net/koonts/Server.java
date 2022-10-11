//SSL: https://stackoverflow.com/questions/25637039/detecting-ssl-connection-and-converting-socket-to-sslsocket
//https://stackoverflow.com/questions/53323855/sslserversocket-and-certificate-setup
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
    SSLServerSocketFactory sslServerSocketFactory;
    SSLServerSocket sslServerSocket;
    SSLSocket sslClient = null;
    ArrayList<ConnectionHandler> connections = new ArrayList<>();

    //SSL config
    String trustStoreName = "";
    private static final String TLS_VERSION = "TLSv1.2";

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

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream tstore = Server.class.getResourceAsStream("/" + trustStoreName);
            trustStore.load(tstore, trustStorePassword);
            if (tstore != null) {tstore.close();}
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream kstore = Server.class.getResourceAsStream("/" + keyStoreName);
            keyStore.load(kstore, keyStorePassword);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePassword);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), SecureRandom.getInstanceStrong());

            sslServerSocketFactory = sslContext.getServerSocketFactory();
            ServerSocket serverSocket = sslServerSocketFactory.createServerSocket(port);
            sslServerSocket = (SSLServerSocket) serverSocket;
            sslServerSocket.getNeedClientAuth();
            sslServerSocket.setEnabledProtocols(new String[] {tlsVersion});



        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException |
                 KeyManagementException ignored) {

        }
        //
        //





        while (running) {
            try {
                //handle client connection
                //add ConnectionHandler ch to connections ArrayList<>
//                Socket clientSocket = serverSocket.accept();
                Socket clientSSLSocket = (Socket) sslServerSocket.accept();
                System.out.println("client connected: " + clientSSLSocket.getInetAddress().getHostAddress());
                ConnectionHandler ch = new ConnectionHandler(clientSSLSocket);
                connections.add(ch);
                ch.start();
            } catch (Exception e) {
                //TODO: handle this
            }
        }
    }

    @Override
    public void run() {
//        try {
//            startServer();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
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
                System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
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
