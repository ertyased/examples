package info.kgeorgiy.ja.shchetinin.walk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class AbstractHashCalculator {
    private static final int BUFFER_SIZE = 64;
    protected byte[] buffer = new byte[BUFFER_SIZE];
    protected FileInputStream fileReader;
    protected int hash;
    protected String result;
    public AbstractHashCalculator(File file) throws FileNotFoundException {
        fileReader = new FileInputStream(file);
    }

    public String calcHash() throws IOException {
        preCalc();
        int amount = 0;
        while ((amount = fileReader.read(buffer, 0, BUFFER_SIZE)) >= 0) {
            calcHashBuffer(amount);
        }
        postCalc();
        return result;
    }

    protected abstract void calcHashBuffer(int amount);

    protected abstract void preCalc();

    protected abstract void postCalc();
}
