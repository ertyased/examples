package info.kgeorgiy.ja.shchetinin.googler;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class Downloader {
    final private HttpClient client;
    public Downloader() {
        client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();

    }
    public String downloadSite(String websiteUrl) throws IOException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(websiteUrl))
                    .version(HttpClient.Version.HTTP_2)
                    .header("User-Agent", "hello")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(
                            StandardCharsets.UTF_8));
            System.out.println("HERE:" + response.body() + response.statusCode());
            return response.body();
        } catch (Exception e) {
            throw new IOException("Unable to connect", e);
        }
    }
}
