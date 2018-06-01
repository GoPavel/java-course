package ru.ifmo.rain.golovin.nets;

import info.kgeorgiy.java.advanced.hello.HelloServer;
import info.kgeorgiy.java.advanced.hello.Util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HelloUDPServer implements HelloServer {
    private ExecutorService threadPool;
    private ExecutorService listenThread;
    private DatagramSocket socket;


    /**
     * Run server.
     * @param args :
     * <ul>
     *     <li>number of port</li>
     *     <li>count of threads</li>
     * </ul>
     */
    public static void main(String[] args) {
        if (args == null || args.length != 2) {
            System.out.println("incorrect count of arguments.");
        } else if (args[0] == null || args[1] == null) {
            System.out.println("expected non-null arguments.");
        } else {
            int numberOfPort = Integer.parseInt(args[0]);
            int countOfThread = Integer.parseInt(args[1]);
            try(HelloUDPServer server = new HelloUDPServer()) {
                server.start(numberOfPort, countOfThread);
                Thread.sleep(10000);
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Override
    public void start(int port, int countOfThread) {
        System.out.println("Start");
        threadPool = new ThreadPoolExecutor(countOfThread + 1, countOfThread + 1, 0, TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(1000), new ThreadPoolExecutor.DiscardPolicy());
        listenThread = Executors.newSingleThreadExecutor();
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            error(e, "Can't open or bind Datagram socket");
        }

        listenThread.submit(new Listener(socket, threadPool));
    }

    private static class Listener implements Runnable {

        private DatagramSocket socket;
        private ExecutorService threadPool;

        public Listener(DatagramSocket socket, ExecutorService threadPool) {
            this.socket = socket;
            this.threadPool = threadPool;
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    byte[] receiveBuffer = new byte[socket.getReceiveBufferSize()];
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    try {
                        socket.receive(receivePacket);
                        threadPool.submit(new Sender(socket, receivePacket));
                    } catch (IOException e) {
                        if (socket.isClosed()) {
                            return;
                        }
                        error(e, "When receive packet from port:");
                    }
                }
            } catch (SocketException e) {

            }

        }
    }

    private static class Sender implements Runnable {
        private DatagramSocket socket;
        private DatagramPacket receivePacket;


        public Sender(DatagramSocket socket, DatagramPacket receivePacket) {
            this.socket = socket;
            this.receivePacket = receivePacket;
        }

        @Override
        public void run() {
            try {
                String msg = "Hello, " + new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);
                byte[] sendBuffer = msg.getBytes(StandardCharsets.UTF_8);
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, receivePacket.getSocketAddress());
                socket.send(sendPacket);
            } catch (IOException e) {
                if (socket.isClosed()) {
                    return;
                }
                error(e, "When send answer:");
            }
        }

    }

    @Override
    public void close() {
        socket.close();
        listenThread.shutdownNow();
        threadPool.shutdownNow();
        try {
            listenThread.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
        System.out.println("close");
    }

    private static void error(Exception e, String message) {
        System.err.println(message);
        System.err.println("Exception message: " + e.getMessage());
    }
}
