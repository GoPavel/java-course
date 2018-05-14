package ru.ifmo.rain.golovin.nets;

import info.kgeorgiy.java.advanced.hello.HelloServer;
import sun.nio.ch.ThreadPool;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPServer implements HelloServer {
//    private ExecutorService threadPool;
//    private boolean isStart = false;
    private Thread thread;

    public HelloUDPServer() { }

    public HelloUDPServer(int numberOfPort, int countOfThread) {
        this.start(numberOfPort, countOfThread);
    }

    /**
     * Run server.
     * @param args = [number of input port, count of thread]
     * @return
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("incorrect count of arguments.");
        } else if (args[0] == null || args[1] == null) {
            System.out.println("expected non-null arguments.");
        } else {
            int numberOfPort = Integer.parseInt(args[0]);
            int countOfThread = Integer.parseInt(args[1]);
            try(HelloUDPServer server = new HelloUDPServer(numberOfPort, countOfThread)) {
                while(true) { }
            }
        }
    }

    @Override
    public void start(int numberOfPort, int countOfThread) {
        if (thread.isInterrupted()) {
            System.out.println("Server's started already.");
        } else {
//            isStart = true;
            try(DatagramSocket socket = new DatagramSocket(numberOfPort)) {

                thread = new Thread( () -> {
                    ExecutorService threadPool = Executors.newFixedThreadPool(countOfThread);

                    while (!Thread.currentThread().isInterrupted()) {
                        byte[] buf = new byte[256];
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        try {
                            System.out.print(0);
                            socket.receive(packet);
                            System.out.print(0);
                            threadPool.submit(new Task(new String(packet.getData()).substring(0, packet.getLength())));
                        } catch (IOException e) {
                            System.out.print(0);
                            error(e, "When receive packet: ");
                        }
                    }
                    threadPool.shutdownNow();
                });
                thread.run();
            } catch (SocketException e) {
                error(e, "UDP error:");
            }
        }
    }

    private class Task implements Runnable {
        private String str;

        public Task(String str) {
            this.str = str;
        }

        @Override
        public void run() {
            System.out.println("Hello, " + str);
        }
    }

    @Override
    public void close() {
//        if(isStart) {
//            threadPool.shutdownNow();
//            isStart = false;
//        }
        if(!thread.isInterrupted()) {
            thread.interrupt();
        }
    }

    static void error(Exception e, String message) {
        System.err.println(message);
        System.err.println("Exception message: " + e.getMessage());
    }
}
