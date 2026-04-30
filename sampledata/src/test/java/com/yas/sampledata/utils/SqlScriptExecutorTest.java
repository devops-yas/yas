package com.yas.sampledata.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

class SqlScriptExecutorTest {

    @Test
    void shouldSwallowSqlExceptions() {
        SqlScriptExecutor executor = new SqlScriptExecutor();
        DataSource dataSource = new ThrowingDataSource();

        assertDoesNotThrow(() ->
                executor.executeScriptsForSchema(dataSource, "public", "classpath*:db/product/*.sql")
        );
    }

    private static final class ThrowingDataSource implements DataSource {
        @Override
        public Connection getConnection() throws SQLException {
            throw new SQLException("Connection refused");
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            throw new SQLException("Connection refused");
        }

        @Override
        public PrintWriter getLogWriter() throws SQLException {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void setLogWriter(PrintWriter out) throws SQLException {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void setLoginTimeout(int seconds) throws SQLException {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public int getLoginTimeout() throws SQLException {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException("Not supported");
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }
    }
}
