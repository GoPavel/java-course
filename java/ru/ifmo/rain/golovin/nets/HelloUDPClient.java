package ru.ifmo.rain.golovin.nets;

import info.kgeorgiy.java.advanced.hello.HelloClient;
import info.kgeorgiy.java.advanced.hello.Util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class HelloUDPClient implements HelloClient {

    //    static final int maxRepeatSend = 5;
    private static int TIMEOUT = 100;

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {

        ExecutorService pull = Executors.newFixedThreadPool(threads);

        InetSocketAddress address = new InetSocketAddress(host, port);

        for (int i = 0; i < threads; i++) {
            pull.submit(new Worker(requests, i, prefix, port, address));
        }

        pull.shutdown();
        try {
            if (!pull.awaitTermination(threads * requests, TimeUnit.MINUTES)) {
                pull.shutdownNow();
            }
        } catch (InterruptedException ignored) {
        }

    }

    private static class Worker implements Runnable {
        private int requests;
        private int number;
        private final String prefix;
        private final InetSocketAddress address;
        private final int port;

        public Worker(int requests, int number, String prefix, int port, InetSocketAddress address) {
            this.requests = requests;
            this.number = number;
            this.prefix = prefix;
            this.port = port;
            this.address = address;
        }

        @Override
        public void run() {
            int indexRequest = 0;
            String request = null;
            DatagramPacket requestPacket = null;
            byte[] buf = null;

            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setSoTimeout(TIMEOUT);

                while (!Thread.currentThread().isInterrupted() && indexRequest < requests) {
                    if (request == null) {
                        request = prefix + number + "_" + indexRequest;
                        buf = request.getBytes(Util.CHARSET);
                        requestPacket = new DatagramPacket(buf, 0, buf.length, address);
                    }

                    try {
                        socket.send(requestPacket);
                    } catch (IOException e) {
                        error(e, "Can't send packet.");
                        if (!socket.isClosed())
                            continue;
                        else return;
                    }

                    try {
                        buf = new byte[socket.getReceiveBufferSize()];

                        DatagramPacket responsePacket = new DatagramPacket(buf, buf.length);
                        socket.receive(responsePacket);

                        String response = new String(responsePacket.getData(), responsePacket.getOffset(), responsePacket.getLength(), Util.CHARSET);
                        if (response.contains(request) && !response.equals(request)) {
                            System.out.println(response);
                            request = null;
                            ++indexRequest;
                        }
                    } catch (IOException e) {
                        error(e, "Can't receive packet.");
                        if (!socket.isClosed())
                            continue;
                        else return;
                    }
//                System.out.println("end cycle");
                }

            } catch (SocketException e) {
                error(e, "Can't open or bind any port.");
            }


        }
    }

    private static void error(Exception e, String message) {
        System.err.println(message);
        System.err.println("Exception message: " + e.getMessage());
    }

    /**
     * Run client.
     * @param args :
     * <ul>
     *     <li>имя или ip-адрес компьютера, на котором запущен сервер</li>
     *     <li>номер порта, на который отсылать запросы</li>
     *     <li>префикс запросов (строка)</li>
     *     <li>число параллельных потоков запросов</li>
     *     <li>число запросов в каждом потоке</li>
     * </ul>
     */
    static void main(String[] args) {
        if (args == null || args.length != 5) {
            System.out.println("Expected five arguments.");
            return;
        }
        if(Arrays.stream(args).anyMatch(Predicate.isEqual(null))) {
            System.out.println("Expected non null argument.");
            return;
        }

        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            String prefix = args[2];
            int threads = Integer.parseInt(args[3]);
            int requests = Integer.parseInt(args[4]);

            new HelloUDPClient().run(host, port, prefix, threads, requests);
        } catch (NumberFormatException e) {
            System.out.println("Incorrect integer arguments");
        }


    }
}
