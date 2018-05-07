package ru.ifmo.rain.golovin.nets;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPServer implements AutoCloseable {
    private ExecutorService threadPool;
    private boolean isStart = false;

    /**
     * Run server.
     * @param args = [number of input port, count of thread]
     * @return
     */
    public static void main(String[] args) {
        if (args.length != 0) {
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

    public HelloUDPServer(int numberOfPort, int countOfThread) {
        if (isStart) {
            System.out.println("Server's started already.");
        } else {
            isStart = true;
            try(DatagramSocket socket = new DatagramSocket(numberOfPort)) {

                threadPool = Executors.newFixedThreadPool(countOfThread);

                while (true) {
                    byte[] buf = new byte[256];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    try {
                        socket.receive(packet);
                        threadPool.submit(new Task(new String(packet.getData()).substring(0, packet.getLength())));
                    } catch (IOException e) {
                        error(e, "When receive packet: ");
                    }
                }
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
        isStart = false;
        threadPool.shutdownNow();
    }

    static void error(Exception e, String message) {
        System.err.println(message);
        System.err.println("Exception message: " + e.getMessage());
    }
}
