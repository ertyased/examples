package info.kgeorgiy.ja.shchetinin.googler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GooglePage {
    private static final Downloader downloader = new Downloader();
    private static final String DuckDuckGoAPI = "http://api.duckduckgo.com/?q=";

    public record Result(String header, String link, String snippet) {
        @Override
        public String toString() {
            return new StringBuilder().append(header)
                    .append("\n").append(link).append("\n").append(snippet).append("\n").toString();
        }
    }

    private String result = null;
    private List<Result> results;
    public GooglePage(String request, int pageNumber) throws IOException {
        results = new ArrayList<>();
        String page = downloader.downloadSite(DuckDuckGoAPI + request
                + "&format=json&s=" + 10 * pageNumber);
        parsePage(page);
    }

    public String toString() {
        return result;
    }


    //No time for this
    private void parsePage(String s) {
        result = s;
    }
}
