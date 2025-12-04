package org.example.workers;

import java.util.ArrayList;
import java.util.List;

public class ByteBuffer {
    private final List<Byte> buffer;

    public ByteBuffer() {
        this.buffer = new ArrayList<>();
    }

    public void copyBytes(List<Byte> bytes) {
        buffer.clear();
        buffer.addAll(bytes);
    }

    public List<Byte> getBuffer() {
        return new ArrayList<>(buffer); // Возвращаем копию буфера
    }

    public boolean isEmpty() {
        return buffer.isEmpty();
    }
}
