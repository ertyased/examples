package info.kgeorgiy.ja.shchetinin.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class HashCalculator {
    public void recursiveWalk(File file, StringBuilder result, String filename, String hashType, boolean recursive) {
        if (recursive && file.isDirectory() && file.listFiles() != null) {
            for (File fileDirectory : Objects.requireNonNull(file.listFiles())) {
                recursiveWalk(fileDirectory, result, Path.of(filename).resolve(fileDirectory.toPath()).toString(), hashType, true);
            }
            return;
        }
        String hash;
        try {
            AbstractHashCalculator hashCalculator;
            if (hashType != null && hashType.equals("sha-1")) {
                hashCalculator = new SHA1HashCalculator(file);
            } else {
                hashCalculator = new JenkinsHashCalculator(file);
            }
            hash = hashCalculator.calcHash();
        } catch (IOException | NoSuchAlgorithmException | SecurityException e) {
            if (hashType != null && hashType.equals("sha-1")) {
                hash = "0000000000000000000000000000000000000000"; // :NOTE: constants
            } else {
                hash = "00000000";
            }
        }
        result.append(hash).append(" ").append(filename).append("\n");
    }

    public void solve(String[] args, boolean recursive) {
        if (args == null || args.length < 2) {
            System.out.println("Not enough arguments");
            return;
        }
        String hashType = "";
        if (args.length >= 3) {
            hashType = args[2];
        }
        if (args[0] == null || args[1] == null) {
            System.out.println("Arguments are null.");
            return;
        }
        // :NOTE: use new java io api
        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);

        try {
            Path outputFilePath = outputFile.toPath();
            if (outputFilePath.getParent() != null) {
                Files.createDirectories(outputFilePath.getParent());
            }
        } catch (IOException | InvalidPathException ignored) {
        }

        try (BufferedReader inputFileReader = new BufferedReader(new FileReader(inputFile, StandardCharsets.UTF_8));
             FileWriter fileWriter = new FileWriter(outputFile, StandardCharsets.UTF_8)) {
            String fileName;
            while ((fileName = inputFileReader.readLine()) != null) {
                StringBuilder result = new StringBuilder();
                recursiveWalk(new File(fileName), result, fileName, hashType, recursive);
                fileWriter.write(result.toString());
            }
        } catch (IOException | SecurityException e) {
            System.out.println("Failed to read or write in file");
        }
    }
}
