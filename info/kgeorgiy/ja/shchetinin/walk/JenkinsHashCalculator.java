package info.kgeorgiy.ja.shchetinin.walk;

import java.io.File;
import java.io.FileNotFoundException;

public class JenkinsHashCalculator extends AbstractHashCalculator {
    public JenkinsHashCalculator(File file) throws FileNotFoundException {
        super(file);
    }
    @Override
    protected void calcHashBuffer(int amount) {
        for (int i = 0; i < amount; ++i) {
            hash += buffer[i] & 0xff;
            hash += hash << 10;
            hash ^= hash >>> 6;
        }
    }

    @Override
    protected void preCalc() {}

    @Override
    protected void postCalc() {
        hash += hash << 3;
        hash ^= hash >>> 11;
        hash += hash << 15;
        result = String.format("%08x", hash);
    }
}
