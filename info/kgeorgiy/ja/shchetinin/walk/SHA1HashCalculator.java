package info.kgeorgiy.ja.shchetinin.walk;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1HashCalculator extends AbstractHashCalculator {
    MessageDigest md;
    public SHA1HashCalculator(File file) throws FileNotFoundException, NoSuchAlgorithmException {
        super(file);
        md = MessageDigest.getInstance("SHA-1");
    }

    @Override
    protected void calcHashBuffer(int amount) {
        md.update(buffer, 0, amount);
    }

    @Override
    protected void preCalc() {}

    @Override
    protected void postCalc() {
        byte[] sha1 = md.digest();
        result = String.format("%040x", new BigInteger(1, sha1));
    }
}
