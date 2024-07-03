package info.kgeorgiy.ja.shchetinin.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * Provides functionality for implementing interfaces or classes and generating their implementations
 * in Java source files or as JAR files.
 */
public class Implementor implements Impler, JarImpler {
    /**
     * Generates an implementation of the specified class or interface and saves it to a JAR file.
     *
     * @param token   type token to create implementation for.
     * @param jarFile target .jar file where the implementation will be saved.
     * @throws ImplerException if an error occurs during implementation.
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();

        Path tmp = jarFile.toAbsolutePath().getParent().resolve("tmp");
        try {
            Files.createDirectories(tmp);
        } catch (IOException e) {
            System.out.println("Unable to create dir to jarFile, still continue working");
        }

        implement(token, tmp);

        Path tmpPackage = tmp.resolve(token.getPackageName().replace(".", File.separator));
        Path tmpCode = tmpPackage.resolve(token.getSimpleName() + "Impl.java");
        StringBuilder paths = new StringBuilder().append(System.getProperty("java.class.path"));
        if (token.getProtectionDomain().getCodeSource() != null) {
           paths.append(File.pathSeparator).append( // :NOTE: почему бы здесь тоже не использовать Path? Зачем File?
                   new File(token.getProtectionDomain().getCodeSource().getLocation().getPath()).getAbsoluteFile());
        }
        int compiled = javaCompiler.run(null, null, null,
                "-cp", paths.toString(),
                "-encoding",
                "utf8",
                tmpCode.toAbsolutePath().toString());
        if (compiled != 0) {
            throw new ImplerException("unable to compile file :(. Error code: " + compiled);
        }
        Path tmpClass = tmpPackage.resolve(token.getSimpleName() + "Impl.class");
        try (OutputStream outputStream = Files.newOutputStream(jarFile);
             JarOutputStream jarOutputStream = new JarOutputStream(outputStream)) {
            String filePath = token.getPackageName().replace(".", "/") + "/" +
                    token.getSimpleName() + "Impl.class";
            jarOutputStream.putNextEntry(new ZipEntry(filePath));
            Files.copy(tmpClass, jarOutputStream);
            jarOutputStream.closeEntry();
        } catch (IOException e) {
            throw new ImplerException("Can't create output stream", e);
        }
        removeDir(tmp);
    }

    /**
     * Recursively removes a directory and all its contents.
     *
     * @param dir the Path of the directory to be removed
     * @throws ImplerException if there's an issue removing files or directories
     */
    private void removeDir(Path dir) throws ImplerException {
        // :NOTE: реузрсивный обход диеректорий, написанный руками
        // :NOTE: почему бы не использовать стандартное решение из java.nio.file.Files
        if (Files.isDirectory(dir)) {
            for (File file: Objects.requireNonNull(new File(dir.toUri()).listFiles())) {
                removeDir(dir.resolve(file.getName()));
            }
         }
        try {
            Files.delete(dir);
        } catch (IOException e) {
            throw new ImplerException("Unable to remove files");
        }
    }
    /**
     * Generates valid code implementation of the specified class or interface.
     *
     * @param token the class or interface token for which the implementation will be generated.
     * @return the generated class implementation as a string.
     * @throws ImplerException if an error occurs during code generation.
     */
    private String getCode(Class<?> token) throws ImplerException {
        AbstractCreator creator;
        if (token.isInterface()) {
            creator = new InterfaceCreator(token);
        } else {
            creator = new ClassCreator(token);
        }

        if (!creator.isCorrectClass()) {
            throw new ImplerException("Incorrect class");
        }

        return creator.create();
    }


    /**
     * Generates and writes an implementation of the specified class or interface.
     *
     * @param token   type token to create implementation for.
     * @param root    root directory where the implementation will be saved.
     * @throws ImplerException if an error occurs during implementation.
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        String result = getCode(token);
        Path path = root.resolve(token.getPackageName().replace(".", File.separator));
        try {
            Files.createDirectories(path);
        } catch (IOException ignored) {

        }

        Path filePath = path.resolve(token.getSimpleName() + "Impl.java");
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath.toString()))) {
            for (char b: result.toCharArray()) {
                if (b >= 128) {
                    bufferedWriter.write(String.format("\\u%04X", (int) b));
                } else {
                    bufferedWriter.write(b);
                }
            }
        } catch (IOException e) {
            throw new ImplerException("Failed while trying to write", e);
        }
    }

    /**
     * Main function of the package. Used to run the Implementor with command-line arguments.
     * Two usage options are supported:
     * <ul>
     *     <li>{@code java Implementor <className> <outputPath>} - generates implementation of the class
     *     or interface with the specified name and saves it to the specified directory.</li>
     *     <li>{@code java Implementor -jar <className> <outputJar>} - generates implementation of the class
     *     or interface with the specified name and saves it to the specified JAR file.</li>
     * </ul>
     * @param args command-line arguments:
     *             <ul>
     *                 <li>[-jar] - optional flag indicating JAR output.</li>
     *                 <li>className - name of the class or interface to generate implementation for.</li>
     *                 <li>outputPath - path where the generated implementation will be saved.</li>
     *                 <li>outputJar - path of the JAR file where the generated implementation will be saved.</li>
     *             </ul>
     */
    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println("Not enough arguments");
            return;
        }
        Implementor implementor = new Implementor();
        try {
            if (args[0].equals("-jar")) {
                implementor.implementJar(Class.forName(args[1]), Path.of(args[2]));
            } else {
                implementor.implement(Class.forName(args[0]), Path.of(args[1]));
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Unable to find class");
        } catch (ImplerException e) {
            System.out.println("Error while implementing");
        }
    }
}
