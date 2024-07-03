package info.kgeorgiy.ja.shchetinin.googler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GoogleRequest {
    private Map<Integer, GooglePage> pages;
    private int pageNumber;
    private final String request;

    public GoogleRequest(String request) {
        pageNumber = 0;
        pages = new HashMap<>();
        this.request = request;
    }

    public GooglePage goToPage(int numPage) throws IOException {
        pageNumber = numPage;
        pages.putIfAbsent(numPage, new GooglePage(request, numPage));
        return pages.get(numPage);
    }

    public int getPageNumber() {
        return pageNumber;
    }

}
