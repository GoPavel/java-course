package ru.ifmo.rain.golovin.nets;

import info.kgeorgiy.java.advanced.hello.HelloClient;
import info.kgeorgiy.java.advanced.hello.Util;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

public class HelloUDPClient implements HelloClient {

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {


        ExecutorService pull  = Executors.newFixedThreadPool(threads);


        try {
            InetSocketAddress address = new InetSocketAddress(host, port);

            DatagramSocket socket = new DatagramSocket();
            for(int i = 0; i < threads; i++) {
                pull.submit(new Worker(requests, i, prefix, socket, address));
            }
        } catch (SocketException e) {
            error(e, "Can't open or bind any port.");
        }

        try {
            pull.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            return;
        }
    }

    private static class Worker implements Runnable {
        private int requests;
        private int number;
        private final String prefix;
        private final DatagramSocket socket;
        private final InetSocketAddress address;
//        private final int port;

        public Worker(int requests, int number, String prefix, DatagramSocket socket, InetSocketAddress address) {
            this.requests = requests;
            this.number = number;
            this.prefix = prefix;
            this.socket = socket;
            this.address = address;
//            this.port = port;
        }

        @Override
        public void run() {
            int indexRequest = 0;
            while(!Thread.currentThread().isInterrupted() && indexRequest < requests) {
                String request = prefix + number + "_" + indexRequest;
                byte [] buf = request.getBytes(StandardCharsets.UTF_8);
                DatagramPacket requestPacket = new DatagramPacket(buf, 0, buf.length, address);
                try {
                    socket.send(requestPacket);
                } catch (IOException e) {
                    error(e, "Can' send packet.");
                }

                buf = new byte[255];
                DatagramPacket responsePacket = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(responsePacket);
                } catch (IOException e) {
                    error(e, "Can't receive packet.");
                    return;
                }

                String response = new String (Arrays.copyOfRange(responsePacket.getData(), 0, responsePacket.getLength()), StandardCharsets.UTF_8);
                System.out.println(response);
                if (response.contains(request) && !response.equals(request)) {
                    indexRequest++;
                }
            }
        }
    }

    private static void error(Exception e, String message) {
        System.err.println(message);
        System.err.println("Exception message: " + e.getMessage());
    }

//    /**
//     * Run client.
//     * @param args :
//     * <ul>
//     *     <li>имя или ip-адрес компьютера, на котором запущен сервер</li>
//     *     <li>номер порта, на который отсылать запросы</li>
//     *     <li>префикс запросов (строка)</li>
//     *     <li>число параллельных потоков запросов</li>
//     *     <li>число запросов в каждом потоке</li>
//     * </ul>
//     */
//    static void main(String[] args) {
//
//    }
}

// TODO: переиспользование пакетов, в сервер это не очень нужно, потому что там реально разные пакеты.
