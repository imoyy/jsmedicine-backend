package com.gugugaga.jsmedicine.infrastructure.storage;

import java.io.IOException;
import java.io.InputStream;

public record StorageObjectStream(
        InputStream inputStream,
        String contentType,
        long contentLength
) implements AutoCloseable {

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
