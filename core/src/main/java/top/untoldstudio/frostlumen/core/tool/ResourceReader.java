package top.untoldstudio.frostlumen.core.tool;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ResourceReader {
    public static String readString(String path) throws IOException {
        try (InputStream stream = ResourceReader.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new FileNotFoundException("Resource not file:" + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    public static byte[] readBytes(String path) throws IOException {
        try (InputStream stream = ResourceReader.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new FileNotFoundException("Resource not found: " + path);
            }
            return stream.readAllBytes();
        }
    }
}
