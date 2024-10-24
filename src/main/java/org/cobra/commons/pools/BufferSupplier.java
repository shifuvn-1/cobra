package org.cobra.commons.pools;

import java.nio.ByteBuffer;

public interface BufferSupplier extends AutoCloseable {

    BufferSupplier NON_CACHING = new BufferSupplier() {

        @Override
        public ByteBuffer get(int capacity) {
            return ByteBuffer.allocate(capacity);
        }

        @Override
        public void release(ByteBuffer buffer) {
            // nop
        }

        @Override
        public void close() {
            // nop
        }

    };

    static BufferSupplier create() {
        return new BufferSupplierImpl();
    }

    static BufferSupplier nonCaching() {
        return NON_CACHING;
    }

    /**
     * @param capacity buffer capacity
     * @return supplied buffer with given capacity
     */
    ByteBuffer get(int capacity);

    /**
     * release provided buffer back
     *
     * @param buffer provided used buffer
     */
    void release(ByteBuffer buffer);
}
