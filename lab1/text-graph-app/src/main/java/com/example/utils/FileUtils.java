package com.example.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileUtils {
    public static String readFileToString(File file) {
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line).append(" ");
            }
            return sb.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void writeStringToFile(String content, String path) {
        try (BufferedWriter w = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))) {
            w.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
