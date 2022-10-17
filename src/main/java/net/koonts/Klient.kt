package net.koonts

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PrintWriter
import java.net.Socket

class Klient(private val host:String, private val port:Int) : Thread() {
    private var running: Boolean = false
    private var socket: Socket = Socket();
    val socinput = BufferedReader(InputStreamReader(socket.getInputStream()))
    val socoutput = PrintWriter(socket.getOutputStream())


    override fun run() {
        running = true
        socket = Socket(host, port)

        val handle = Handler(socket, socinput, socoutput)
        handle.start()

//        while (running) {
//            val message = input.readLine()
//            if (message.isNotEmpty()) {
//                println(message)
//            }
//        }
    }


    inner class Handler(socket: Socket, input: BufferedReader, output: PrintWriter) : Thread() {
        private val socket = socket;
        private val input = input;
        private val output = output;
        override fun run() {
            val userinput = BufferedReader(InputStreamReader(System.`in`))
            while (running) {

                val message = userinput.readLine()
                if (message.isNotEmpty()) {
                    //options
                    //if quit
                    //if killserver
                    output.println(message)
                    output.flush()
                }
            }
        }
    }
}