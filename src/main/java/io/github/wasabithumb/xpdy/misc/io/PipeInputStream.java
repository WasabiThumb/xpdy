package io.github.wasabithumb.xpdy.misc.io;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A stream which executes a given write operation on a worker thread, buffering the written data
 * and allowing it to be read.
 */
@ApiStatus.Internal
public final class PipeInputStream extends InputStream {

    private final Collector collector;
    private final Worker worker;

    public PipeInputStream(@NotNull Operation operation) {
        this.collector = new Collector();
        this.worker = new Worker(this.collector, operation);
        this.worker.start();
    }

    //

    @Override
    public int read() throws IOException {
        byte[] b = new byte[1];
        if (this.collector.read(b, 0, 1) == -1) return -1;
        return b[0] & 0xFF;
    }

    @Override
    public int read(byte @NotNull [] b, int off, int len) throws IOException {
        return this.collector.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        this.worker.close();
    }

    //

    @FunctionalInterface
    public interface Operation {
        void write(@NotNull OutputStream out) throws IOException;
    }

    //

    private static final class Worker extends Thread {

        private final Collector collector;
        private final Operation operation;
        private IOException error = null;

        private Worker(@NotNull Collector collector, @NotNull Operation operation) {
            this.collector = collector;
            this.operation = operation;
            this.setDaemon(true);
        }

        @Override
        public void run() {
            try (this.collector) {
                this.operation.write(this.collector);
            } catch (IOException e) {
                this.error = e;
            }
        }

        private void close() throws IOException {
            // Drain the stream
            byte[] drain = new byte[512];
            int r;
            do {
                r = this.collector.read(drain, 0, 512);
            } while (r != -1);

            // Join the thread
            try {
                this.join();
            } catch (InterruptedException e) {
                this.interrupt();
                Thread.currentThread().interrupt();
            }

            // Forward errors
            if (this.error != null)
                throw this.error;
        }

    }

    //

    private static final class Collector extends OutputStream {

        private static final int BUFFER_SIZE = 8192;
        private static final long BUFFER_SIZE_L = BUFFER_SIZE;

        //

        private final Object mutex = new Object();
        private final byte[] buffer = new byte[BUFFER_SIZE];
        private boolean open = true;
        private long ir = 0;
        private long iw = 0;

        //

        private int bi(long count) {
            return (int) (count % BUFFER_SIZE_L);
        }

        //

        private int read(byte @NotNull [] b, int off, int max) throws IOException {
            if (max <= 0) return 0;
            synchronized (this.mutex) {
                int count;

                while (true) {
                    long n = this.iw - this.ir;
                    if (n > 0L) {
                        count = (int) n;
                        break;
                    }
                    if (!this.open) return -1;
                    try {
                        this.mutex.wait();
                    } catch (InterruptedException e) {
                        throw new IOException("Interrupted while waiting for pipe", e);
                    }
                }

                count = Math.min(count, max);
                for (int i=0; i < count; i++) {
                    b[off + i] = this.buffer[this.bi(this.ir++)];
                }
                this.mutex.notify();
                return count;
            }
        }

        private int writeSome(byte @NotNull [] b, int off, int max) throws IOException {
            if (max <= 0) return 0;
            synchronized (this.mutex) {
                if (!this.open) throw new IOException("Stream closed");

                int count;
                while (true) {
                    long n = BUFFER_SIZE_L - this.iw + this.ir;
                    if (n > 0L) {
                        count = (int) n;
                        break;
                    }
                    try {
                        this.mutex.wait();
                    } catch (InterruptedException e) {
                        throw new IOException("Broken pipe", e);
                    }
                }

                count = Math.min(count, max);
                for (int i=0; i < count; i++) {
                    this.buffer[this.bi(this.iw++)] = b[off + i];
                }
                this.mutex.notify();
                return count;
            }
        }

        @Override
        public void write(int i) throws IOException {
            this.writeSome(new byte[] { (byte) i }, 0, 1);
        }

        @Override
        public void write(byte @NotNull [] b, int off, int len) throws IOException {
            int n;
            while (len > 0) {
                n = this.writeSome(b, off, len);
                off += n;
                len -= n;
            }
        }

        @Override
        public void close() {
            synchronized (this.mutex) {
                this.open = false;
                this.mutex.notify();
            }
        }

    }

}
