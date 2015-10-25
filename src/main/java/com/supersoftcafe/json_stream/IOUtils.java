package com.supersoftcafe.json_stream;


import java.io.IOException;
import java.io.InputStream;
import java.io.Closeable;
import java.io.Reader;
import java.nio.CharBuffer;


public class IOUtils {



    public static InputStream noClose(InputStream in) {
        return altClose(in, () -> { });
    }

    public static InputStream altClose(InputStream in, Closeable closer) {
        return new InputStream() {
            private boolean closed = false;

            @Override public void close() throws IOException {
                closed = true;
                closer.close();
            }

            private InputStream closed() throws IOException {
                if (closed) throw new IOException();
                return in;
            }

            @Override public int read(byte[] b) throws IOException {
                return closed().read(b);
            }

            @Override public int read(byte[] b, int off, int len) throws IOException {
                return closed().read(b, off, len);
            }

            @Override public long skip(long n) throws IOException {
                return closed().skip(n);
            }

            @Override public int read() throws IOException {
                return closed().read();
            }
        };
    }

    public static Reader noClose(Reader in) {
        return altClose(in, () -> { });
    }

    public static Reader altClose(Reader in, Closeable closer) {
        return new Reader() {
            private boolean closed = false;

            @Override public void close() throws IOException {
                closed = true;
                closer.close();
            }

            private Reader closed() throws IOException {
                if (closed) throw new IOException();
                return in;
            }

            @Override public int read(char[] cbuf, int off, int len) throws IOException {
                return closed().read(cbuf, off, len);
            }

            @Override
            public int read(CharBuffer target) throws IOException {
                return closed().read(target);
            }

            @Override
            public int read() throws IOException {
                return closed().read();
            }

            @Override
            public long skip(long n) throws IOException {
                return super.skip(n);
            }
        };
    }

}
