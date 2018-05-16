package ru.ifmo.rain.golovin.nets;

import info.kgeorgiy.java.advanced.hello.HelloServer;
import info.kgeorgiy.java.advanced.hello.Util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPServer implements HelloServer {
    private ExecutorService threadPool;
    private DatagramSocket socket;


//    /**
//     * Run server.
//     * @param args = [number of input port, count of thread]
//     */
//    public static void main(String[] args) {
//        if (args.length != 2) {
//            System.out.println("incorrect count of arguments.");
//        } else if (args[0] == null || args[1] == null) {
//            System.out.println("expected non-null arguments.");
//        } else {
//            int numberOfPort = Integer.parseInt(args[0]);
//            int countOfThread = Integer.parseInt(args[1]);
////            try(HelloUDPServer server = new HelloUDPServer(numberOfPort, countOfThread)) {
////                while(true) { }
////            }
//        }
//    }

    @Override
    public void start(int port, int countOfThread) {
        threadPool = Executors.newFixedThreadPool(countOfThread);
        try {
            socket = new DatagramSocket(port);
            socket.setSoTimeout(1000); // TODO
        } catch (SocketException e) {
            error(e, "Can't open or bind Datagram socket");
        }

        for(int i = 0; i < countOfThread; ++i) {
            threadPool.submit(new Worker(socket));
        }
    }

    private static class Worker implements Runnable {
        private DatagramSocket socket;

        public Worker(DatagramSocket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    byte[] receiveBuffer = new byte[255]; // TODO: use your mind for find constant.
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    socket.receive(receivePacket);
                    String msg = "Hello, " + new String(Arrays.copyOfRange(receivePacket.getData(), 0, receivePacket.getLength()), Util.CHARSET);

                    byte[] sendBuffer = msg.getBytes(Util.CHARSET);
                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, receivePacket.getSocketAddress());
                    socket.send(sendPacket);
              } catch (IOException e) {
                    if(!socket.isClosed())
                        error(e, "when receive packet from port:" + socket.getPort());
                    return;
                }
            }

        }

    }

    @Override
    public void close() {
        socket.close();
        threadPool.shutdownNow();
    }

    private static void error(Exception e, String message) {
        System.err.println(message);
        System.err.println("Exception message: " + e.getMessage());
    }
}

//TODO: переиспользовать покеты
//TODO: сделать потоки легче. Например, обрадатывать запросы в отдельных потоках.
