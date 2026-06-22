package com.xdata.db;

import java.sql.Connection;

/**
 * A short-lived, in-memory scratch database (Derby) pre-loaded with a schema and
 * test data. Auto-closeable: {@link #close} drops it. Used to execute and compare
 * queries off the live database.
 */
public interface ScratchDatabase extends AutoCloseable {

    Connection connection();

    @Override
    void close();
}
