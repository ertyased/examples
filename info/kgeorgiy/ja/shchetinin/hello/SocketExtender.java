package info.kgeorgiy.ja.shchetinin.hello;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * The SocketExtender class provides an extended functionality over DatagramSocket
 * for sending and receiving data via UDP.
 * It allows setting a timeout for waiting for data, sending data to a specific
 * address and port, and receiving data.
 */
public class SocketExtender {
    /**
     * Default Timeout that will be set in DatagramSocket
     */
    private final static int DEFAULT_TIMEOUT = 1000;
    private final DatagramSocket socket;
    private final DatagramPacket packet = new DatagramPacket(new byte[2048], 2048);

    /**
     * Constructs a new SocketExtender with the specified port.
     *
     * @param port the port number on which to listen for incoming data
     * @throws SocketException if an error occurs while creating the socket
     */
    public SocketExtender(int port) throws SocketException {
        socket = new DatagramSocket(port);
        socket.setSoTimeout(DEFAULT_TIMEOUT);
    }


    /**
     * Constructs a new SocketExtender with an automatically assigned port.
     *
     * @throws SocketException if an error occurs while creating the socket
     */
    public SocketExtender() throws SocketException {
        socket = new DatagramSocket();
        socket.setSoTimeout(DEFAULT_TIMEOUT);
    }

    /**
     * Sets the timeout for receiving data.
     *
     * @param milliseconds the timeout value in milliseconds
     * @throws SocketException if an error occurs while setting the timeout
     */
    public void setTimeout(int milliseconds) throws SocketException {
        socket.setSoTimeout(milliseconds);
    }

    /**
     * Sends the specified string data to the specified address and port.
     *
     * @param s       the string data to send
     * @param address the destination address
     * @param port    the destination port
     * @throws IOException          if an I/O error occurs while sending data
     */
    public void send(String s, InetAddress address, int port) throws IOException {
        byte[] data = s.getBytes(StandardCharsets.UTF_8);
        socket.connect(address, port);
        socket.send(new DatagramPacket(data, data.length));
    }

    /**
     * Waits to receive a string message.
     *
     * @return the received string message
     * @throws InterruptedException if the current thread is interrupted
     * @throws IOException          if an I/O error occurs while receiving data
     */
    public String waitReceiveString() throws InterruptedException, IOException {
        waitReceivePacket();
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    }

    /**
     * Waits to receive a DatagramPacket and disconnects from current connection if it exists.
     * Returned DatagramPacket can be changed. To get copy of received packet use waitReceivePacketCopy.
     *
     * @return the received DatagramPacket
     * @throws InterruptedException if the current thread is interrupted
     * @throws IOException          if an I/O error occurs while receiving data
     */
    public DatagramPacket waitReceivePacket() throws  InterruptedException, IOException {
        socket.disconnect();
        if (Thread.currentThread().isInterrupted()) { // :NOTE: ??
            throw new InterruptedException("");
        }
        socket.receive(packet);
        return packet;
    }

    /**
     * Waits to receive DatagramPacket and return a copy of it.
     *
     * @return a copy of the received DatagramPacket
     * @throws InterruptedException if the current thread is interrupted while waiting
     * @throws IOException          if an I/O error occurs while receiving data
     */
    public DatagramPacket waitReceivePacketCopy() throws InterruptedException, IOException {
        waitReceivePacket();
        InetAddress copyAddress;
        try {
            copyAddress = InetAddress.getByAddress(packet.getAddress().getAddress());
        } catch (UnknownHostException e) {
            throw new IOException("Unable to copy new host. Something weird happend");
        }
        return new DatagramPacket(packet.getData().clone(), packet.getOffset(), packet.getLength(), copyAddress, packet.getPort());
    }

    public void close() {
        socket.close();
    }
}
