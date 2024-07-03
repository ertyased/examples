package info.kgeorgiy.ja.shchetinin.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class HelloUDPClient implements HelloClient {
    private static final int TIMEOUT = 200;

    public static void main(String[] args) {
        if (args.length < 5) {
            System.err.println("Not enough arguments");
            return;
        }
        int port = Integer.parseInt(args[1]);
        int runs = Integer.parseInt(args[3]);
        int requests = Integer.parseInt(args[4]);
        HelloUDPClient client = new HelloUDPClient();
        client.run(args[0], port, args[2], runs, requests);
    }

    private static class SocketThread extends Thread {
        private final int number;
        private final int requests;
        private final InetAddress address;
        private final int port;
        private final String prefix;
        public SocketThread(int n, String prefix, int requests, InetAddress address, int port) {
            this.number = n;
            this.requests =  requests;
            this.address = address;
            this.port = port;
            this.prefix = prefix;
        }

        @Override
        public void run() {
            SocketExtender socketExtender;
            try {
                socketExtender = new SocketExtender();
                socketExtender.setTimeout(TIMEOUT);
            } catch (SocketException e) {
                System.err.println("Unable to create Socket in SocketThread: " + e.getMessage());
                return;
            }
            for (int j = 1; j <= requests; ++j) {
                while (!Thread.interrupted()) {
                    String message = prefix + number + "_" + j;
                    try {
                        socketExtender.send(message, address, port);
                    } catch (IOException e) {
                        System.err.println("Unable to send to address: " + address + ":" + port);
                        continue;
                    }
                    System.err.println("Sent from Client: " + message + "\n");
                    try {
                        String s = socketExtender.waitReceiveString();
                        System.err.println("received on Client: " + s);
                        if (!isEquals(number, j, s)) {
                            continue;
                        }
                        break;
                    } catch (InterruptedException e) {
                        break;
                    } catch (IOException e) {
                        if (e instanceof SocketTimeoutException) {
                            continue;
                        }
                        System.err.println("Error occured while receiving packet in Client: " + e.getMessage());
                        break;
                    }
                }
            }
            socketExtender.close();
        }

        private boolean isEquals(int number, int j, String s) {
            StringBuilder builder = new StringBuilder();
            List<Long> numbers = new ArrayList<>();
            for (int i = s.length() - 1; i >= 0; --i) {
                if (Character.isDigit(s.charAt(i))) {
                    builder.append(Long.parseLong(Character.toString(s.charAt(i))));
                } else if (!builder.isEmpty()) {
                    numbers.add(Long.parseLong(builder.reverse().toString()));
                    builder = new StringBuilder();
                }
            }
            if (!builder.isEmpty()) {
                numbers.add(Long.parseLong(builder.reverse().toString()));
            }
            if (numbers.size() != 2) {
                return false;
            }
            return numbers.get(0) == j && numbers.get(1) == number;
        }
    }

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        InetAddress address;
        try {
            address = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            System.err.println("Unknown host");
            return;
        }
        // :NOTE:
        List<Thread> socketThreads = new ArrayList<>();
        for (int i = 0; i < threads; ++i) {
            socketThreads.add(new SocketThread(i + 1, prefix, requests, address, port));
            socketThreads.get(i).start();
        }
        for (Thread socketThread: socketThreads) {
            try {
                socketThread.join();
            } catch (InterruptedException e) {
                System.err.println("Current Thread was stopped");
                socketThread.interrupt();
                // :NOTE: не дожидаемся завершения потока
            }
        }
    }
}
