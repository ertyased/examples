package info.kgeorgiy.ja.shchetinin.hello;


import java.util.Map;

public class Main {
    private static void testServer() {
        HelloUDPServer server = new HelloUDPServer();
        HelloUDPClient client = new HelloUDPClient();
        server.start(3, Map.of(7777, "Привет, $", 7778, "您好, $"));
        client.run("127.0.0.1", 7777, "prefix", 3, 5);
        client.run("127.0.0.1", 7778, "prefix", 3, 5);
        server.close();
    }
    public static void main(String[] args) {
        testServer();
    }
}
