package org.javatari.utils;

import org.javatari.general.board.RAM64k;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

public class ROMtoRAMLoader {

    private static final int MAX_ROM_SIZE = 64 * 1024;
    private static final int MAX_STREAM_SIZE = MAX_ROM_SIZE;

    public static RAM64k load(File file) throws IOException {
        return load(file.toURI().toURL(), false);
    }

    public static RAM64k load(URL url, boolean provided) throws IOException {
        InputStream stream;
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(5000);
        stream = conn.getInputStream();
        return createFromExternalURL(stream, url.toString(), provided);
    }

    private static RAM64k createFromExternalURL(InputStream stream, String romURL, boolean provided) throws IOException {
        System.out.println("Loading ROM from: " + romURL);
        BufferedInputStream buffer = bufferedStream(stream);
        try {
            return new RAM64k(loadContent(buffer, romURL));
        } finally {
            try {
                stream.close();
                buffer.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static byte[] loadContent(InputStream stream, String romURL) throws IOException {
        byte[] buffer = new byte[MAX_STREAM_SIZE];
        int totalRead = 0;
        do {
            int read = stream.read(buffer, totalRead, MAX_STREAM_SIZE - totalRead);
            if (read == -1) break;    // End of Stream
            totalRead += read;
        } while (totalRead < MAX_STREAM_SIZE);
        return (totalRead > 0) ? Arrays.copyOf(buffer, totalRead) : new byte[0];
    }

    private static BufferedInputStream bufferedStream(InputStream stream) {
        BufferedInputStream buf = new BufferedInputStream(stream, MAX_STREAM_SIZE);
        buf.mark(MAX_STREAM_SIZE);
        return buf;
    }

}
