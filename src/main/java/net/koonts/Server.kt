package net.koonts

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import kotlin.system.exitProcess

class Server(private var port: Int) : Thread() {
    private var running = false
    private var connections = ArrayList<ConnectionHandler>()

    constructor() : this(port = 8888)


    private fun startServer() {
        running = true
        println("Starting server")
        val serverSocket = ServerSocket(port)
        while (running) {
            try {
                //handle client connection
                //add ConnectionHandler ch to connections ArrayList<>
                val clientSocket = serverSocket.accept()
                println("client connected: " + clientSocket.inetAddress.hostAddress)
                val ch: ConnectionHandler = ConnectionHandler(clientSocket)
                connections.add(ch)
                ch.start()
            } catch (e: Exception) {
                //TODO: handle this
            }
        }
    }

    override fun run() {
        try {
            startServer()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    inner class ConnectionHandler(private var client: Socket) : Thread() {
        private var nickname: String? = null
        private var hostAddress: String? = client.inetAddress.hostAddress
        private var out: PrintWriter? = null
        private var input: BufferedReader? = null

        override fun run() {
            println("Client Thread started..")
            if (client.isConnected) {
                try {
                    out = PrintWriter(client.getOutputStream(), true)
                    input = BufferedReader(InputStreamReader(client.getInputStream()))
                    out!!.println("Welcome!")
                    out!!.println("Please enter nickname:") // first word sent is 'nickname'
                    val tmp = input!!.readLine().split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()[0] // otherwise address is used
                    nickname = tmp
                    if (tmp == "") {
                        out!!.println("Nothing Entered")
                        nickname = hostAddress
                    }
                    out!!.println("Hello $nickname")
                    var messageFromClient: String //begin reading input into chat
                    while (input!!.readLine()
                            .also { messageFromClient = it } != null
                    ) { //redundant comparators in place to catch excessive
                        if (nickname != null && hostAddress != null && messageFromClient.isNotEmpty()) { //null when client
                            println("$hostAddress:$nickname:: $messageFromClient") // abruptly disconnects.
                        } else {
                            if (hostAddress != null && messageFromClient.isNotEmpty()) { //
                                println("$hostAddress:: $messageFromClient")
                            }
                        }

                        //if client message begins with "/" process as command
                        if (messageFromClient.startsWith("/")) {
//                            System.out.println("received modifier..");
                            setOption(messageFromClient)
                        } else if (messageFromClient.isNotEmpty()) {
                            if (nickname != null) {
                                broadcast("$nickname: $messageFromClient")
                            } else {
                                broadcast("$hostAddress: $messageFromClient")
                            }
                        } else {
                            out!!.println("Please enter a message..")
                        }
                    }

                    //report disconnection to server log
                    if (nickname != null) {
                        println("$hostAddress : $nickname <<Disconnected>>")
                    } else {
                        println("$hostAddress :  <<Disconnected>>")
                    }
                } catch (e: Exception) {
                    println(e.message)
                }
            }
        }

        @Throws(InterruptedException::class)
        private fun setOption(options: String) {
            //user entered string beginning with '/'
            val messageSplit = options.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (messageSplit.isNotEmpty()) {
                ///set nickname
                if (messageSplit[0].startsWith("/nickname")) {
                    nickname = messageSplit[1]
                    broadcast("$hostAddress has changed nickname to: $nickname")
                }
                //display help message
                if (options.startsWith("/help")) {
                    val helpMessage = "/help for this, /nickname to change nickname, /quit to exit"
                    //                    out.println(helpMessage);
                    broadcast(helpMessage)
                }
                if (options.startsWith("/killserver")) {
                    sleep(3000)
                    exitProcess(0)
                }
            }
        }

        private fun broadcast(message: String) {
            //loop over connections ArrayList<ConnectionHandler>
            //to broadcast socket message
            for (connection in connections) {
                connection.out!!.println(message)
            }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val runnable = Runnable {
                val server = Server()
                server.start()
            }
            runnable.run()
        }
    }
}