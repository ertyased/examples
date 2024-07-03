package info.kgeorgiy.ja.shchetinin.hello;

import info.kgeorgiy.java.advanced.hello.NewHelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPServer implements NewHelloServer {
    private ExecutorService executors;
    private List<ReceiverThread> receivers;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Not enough arguments");
            return;
        }
        int port = Integer.parseInt(args[0]);
        int runs = Integer.parseInt(args[1]);
        HelloUDPServer server = new HelloUDPServer();
        server.start(port, runs);
        server.close();
    }

    private class ReceiverThread extends Thread {
        private final SocketExtender socket;
        private final String format;

        public ReceiverThread(int port, String format) throws SocketException {
            socket = new SocketExtender(port);
            this.format = format;
        }

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                DatagramPacket packet;
                try {
                    packet = socket.waitReceivePacketCopy();
                } catch (IOException e) {
                    if (e instanceof SocketTimeoutException) {
                        continue;
                    }
                    System.out.println("Error occurred while receiving packet on Server: " + e.getMessage());
                    break;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                String data = new String(packet.getData(), packet.getOffset(), packet.getLength()); // :NOTE: вынести в SocketExtander
                executors.submit(() -> {
                    SocketExtender privateSocket;
                    try {
                        privateSocket = new SocketExtender();
                    } catch (SocketException e) {
                        System.out.println("Unable to create socket in Executor thread" + e.getMessage());
                        return;
                    }
                    String message = format.replace("$", data);
                    try {
                        privateSocket.send(message, packet.getAddress(), packet.getPort());
                    } catch (IOException e) {
                        System.out.println("Unable to send to address: " + packet.getAddress() + ":" + packet.getPort());
                    }
                    privateSocket.close();
                });
            }
            socket.close();
        }
    }

    @Override
    public void start(int threads, Map<Integer, String> ports) {
        executors = Executors.newFixedThreadPool(threads);
        receivers = new ArrayList<>();
        for (Map.Entry<Integer, String> entry: ports.entrySet()) {
            try {
                receivers.add(new ReceiverThread(entry.getKey(), entry.getValue())); // :NOTE: Executor
                receivers.get(receivers.size() - 1).start();
            } catch (SocketException e) {
                close();
                System.out.println("Unbale to create receiver threads: " + e.getMessage());
                return;
            }
        }

    }


    @Override
    public void close() {
        executors.close();
//        try {
//            boolean ignored = executors.awaitTermination(10, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            System.out.println("Server Thread was interrupted before full closure.");
//        }
        for (ReceiverThread receiver : receivers) {
            receiver.interrupt();
        }

        for (ReceiverThread receiver : receivers) {
            try {
                receiver.join();
            } catch (InterruptedException e) {
                System.out.println("Server Thread was interrupted before full closure.");
                return;
            }
        }
    }
}
