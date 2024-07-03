package info.kgeorgiy.ja.shchetinin.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WebCrawler implements AdvancedCrawler {

    private final Downloader downloader;
    private final ExecutorService downloaders;
    private final ExecutorService extractors;
    private final int perHost;
    private final Map<String, HostManager> amHost;

    private class HostManager {
        private final Queue<Runnable> tasks;
        private final AtomicInteger runningTasks = new AtomicInteger(0);
        private final String host;

        public HostManager(String host) {
            this.host = host;
            tasks = new ConcurrentLinkedDeque<>();
        }

        public void addTask(Runnable task) {
            if (runningTasks.get() < perHost) {
                downloaders.submit(task);
                runningTasks.incrementAndGet();
                return;
            }
            tasks.add(task);
        }

        public void callNext() {
            Runnable nextJob = tasks.poll();
            if (nextJob != null) {
                downloaders.submit(nextJob);
            } else {
                runningTasks.decrementAndGet();
                if (runningTasks.get() == 0) {
                    amHost.remove(host);
                }
            }
        }
    }

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
        amHost = new ConcurrentHashMap<>();
    }

    @Override
    public Result download(String url, int depth, Set<String> excludes) {
        return superDownload(url, depth, excludes, null);
    }

    @Override
    public Result download(String url, int depth) {
        return download(url, depth, new HashSet<>());
    }

    @Override
    public Result advancedDownload(String url, int depth, List<String> hosts) {
        return superDownload(url, depth, new HashSet<>(), hosts);
    }

    private Result superDownload(String url, int depth, Set<String> excludes, List<String> hosts) {
        Set<String> results = ConcurrentHashMap.newKeySet();
        ConcurrentHashMap<String, IOException> errors = new ConcurrentHashMap<>();
        Set<String> visited = ConcurrentHashMap.newKeySet();
        Set<String> urls = ConcurrentHashMap.newKeySet();
        urls.add(url);
        Set<String> newHosts = null;
        if (hosts != null) {
            newHosts = new HashSet<>();
            newHosts.addAll(hosts);
        }
        bfs(urls, 0, depth, results, errors, visited, excludes, newHosts);
        return new Result(results.stream().toList(), errors);
    }

    private void bfs(Set<String> urls, int depth, int maxDepth,
                     Set<String> results, ConcurrentHashMap<String, IOException> errors,
                     Set<String> visited, Set<String> excludes, Set<String> hosts) {
        if (depth == maxDepth) {
            return;
        }
        CountDownLatch count = new CountDownLatch(urls.size());
        Set<String> newUrls = ConcurrentHashMap.newKeySet();
        for (String url : urls) {
            String host;
            try {
                host = URLUtils.getHost(url);
            } catch (MalformedURLException e) {
                errors.put(url, e);
                count.countDown();
                continue;
            }

            if ((hosts != null && !hosts.contains(host)) || !visited.add(url)) {
                count.countDown();
                continue;
            }

            HostManager manager = amHost.computeIfAbsent(host, k -> new HostManager(host));
            // :NOTE: right here host can be removed, what leads to leak
            Runnable downloadJob = () -> {
                if (excludes.stream().anyMatch(url::contains)) {
                    count.countDown();
                    manager.callNext();
                    return;
                }
                Document doc = null;
                try {
                    doc = downloader.download(url);
                    results.add(url);
                } catch (IOException e) {
                    errors.put(url, e);
                    count.countDown();
                } finally {
                    manager.callNext();
                }
                if (doc == null) {
                    return;
                }
                Document finalDoc = doc;
                Runnable extractJob = () -> {
                    try {
                        newUrls.addAll(finalDoc.extractLinks());
                    } catch (IOException e) {
                        errors.put(url, e);
                    } finally {
                        count.countDown();
                    }
                };
                extractors.submit(extractJob);
            };
            manager.addTask(downloadJob);
        }

        try {
            count.await();
        } catch (InterruptedException ignored) {
            System.out.println("Current thread was interrupted, aborting bfs");
            return;
        }
        bfs(newUrls, depth + 1, maxDepth, results, errors, visited, excludes, hosts);
    }

    @Override
    public void close() {
        downloaders.shutdownNow();
        extractors.shutdownNow();
    }

    public static int getArgsOrDefault(String[] args, int id, int defaultValue) {
        int data;
        if (id >= args.length) {
            return defaultValue;
        }
        try {
            data = Integer.parseInt(args[id]);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
        return data;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Not enough arguments");
            return;
        }
        String url = args[0];
        int depth = getArgsOrDefault(args, 1, 2);
        int downloads = getArgsOrDefault(args, 2, 1);
        int extractors = getArgsOrDefault(args, 3, 1);
        int perHost = getArgsOrDefault(args, 4, 100);
        WebCrawler webCrawler;
        try {
            webCrawler = new WebCrawler(new CachingDownloader(0), downloads, extractors, perHost);
        } catch (IOException e) {
            System.out.println("Unable to create WebCrawler");
            return;
        }
        Result result = webCrawler.download(url, depth);
        for (String a : result.getDownloaded()) {
            System.out.println(a);
        }
        webCrawler.close();
    }
}
