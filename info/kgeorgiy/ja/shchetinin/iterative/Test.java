package info.kgeorgiy.ja.shchetinin.iterative;

import java.util.List;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        IterativeParallelism ip = new IterativeParallelism();
        System.out.println(
                ip.reduce(3, List.of(1, 2, 3, 4, 5), 0, Integer::sum, 1)
        );
    }
}
