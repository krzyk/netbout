/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code occasionally and without intent to use it, please report this
 * incident to the author by email.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.netbout.inf.triples;

import com.jolbox.bonecp.BoneCPDataSource;
import com.ymock.util.Logger;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import javax.sql.DataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.SerializationUtils;

/**
 * Triples with HSQL DB.
 *
 * <p>The class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class HsqlTriples implements Triples {

    /**
     * Names of tables already created in the DB.
     */
    private final transient Set<String> tables =
        new ConcurrentSkipListSet<String>();

    /**
     * Preserved result sets.
     */
    private final transient ConcurrentHashMap<Long, ResultSet> rsets =
        new ConcurrentHashMap<Long, ResultSet>();

    /**
     * The connection to DB.
     */
    private final transient DataSource source;

    /**
     * Public ctor.
     * @param dir Where to keep data
     */
    public HsqlTriples(final File dir) {
        this.source = HsqlTriples.datasource(dir);
        Logger.info(
            this,
            "#HsqlTriples(.../%s): instantiated",
            dir.getName()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.session().sql("SHUTDOWN COMPACT").execute();
        Logger.info(this, "#close(): closed");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> void put(final Long number, final String name, final T value) {
        final JdbcSession session = this.session(name).sql(
            // @checkstyle StringLiteralsConcatenation (4 lines)
            "MERGE INTO %table-1% AS t USING"
            + " (VALUES(CAST(? AS BIGINT), CAST(? AS BINARY(255))))"
            + " AS vals(k, v) ON t.key = vals.k AND t.value = vals.v"
            + " WHEN NOT MATCHED THEN INSERT VALUES vals.k, vals.v, ?"
        )
            .table(name)
            .set(number)
            .set(this.serialize(value));
        if (value instanceof Long) {
            session.set(value);
        } else {
            session.set(0L);
        }
        session.insert();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> boolean has(final Long number, final String name,
        final T value) {
        return this.session(name)
            .sql("SELECT * FROM %table-1% WHERE key=? AND value=? LIMIT 1")
            .table(name)
            .set(number)
            .set(this.serialize(value))
            .select(
                new JdbcSession.Handler<Boolean>() {
                    @Override
                    public Boolean handle(final ResultSet rset)
                        throws SQLException {
                        final boolean has = rset.next();
                        rset.close();
                        return has;
                    }
                }
            );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(final Long number, final String name)
        throws MissedTripleException {
        final T value = this.session(name)
            .sql("SELECT value FROM %table-1% WHERE key=? LIMIT 1")
            .table(name)
            .set(number)
            .select(
                new JdbcSession.Handler<T>() {
                    @Override
                    public T handle(final ResultSet rset) throws SQLException {
                        T val = null;
                        if (rset.next()) {
                            val = (T) HsqlTriples.deserialize(rset.getBytes(1));
                        }
                        rset.close();
                        return val;
                    }
                }
            );
        if (value == null) {
            throw new MissedTripleException(
                String.format(
                    "can't find %d in %s",
                    number,
                    name
                )
            );
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Iterator<T> all(final Long number, final String name) {
        return this.session(name)
            .sql("SELECT value FROM %table-1% WHERE key=?")
            .table(name)
            .set(number)
            .select(new ValuesHandler<T>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Iterator<Long> reverse(final String name, final T value) {
        return this.session(name)
            .sql("SELECT key FROM %table-1% WHERE value=? ORDER BY key DESC")
            .table(name)
            .set(HsqlTriples.serialize(value))
            .select(new LongHandler());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Iterator<Long> reverse(final String name, final String join,
        final T value) {
        return this.session(name, join)
            // @checkstyle LineLength (1 line)
            .sql("SELECT l.key FROM %table-1% AS l JOIN %table-2% AS r ON l.vnum = r.key WHERE r.value=? ORDER BY l.key DESC")
            .table(name)
            .table(join)
            .set(HsqlTriples.serialize(value))
            .select(new LongHandler());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear(final Long number, final String name) {
        this.session(name)
            .sql("DELETE FROM %table-1% WHERE key=?")
            .table(name)
            .set(number)
            .execute();
    }

    /**
     * Create new session.
     * @param names Names of the table to use
     * @return JDBC session
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private JdbcSession session(final String... names) {
        try {
            synchronized (this.tables) {
                for (String name : names) {
                    if (!this.tables.contains(name)) {
                        new JdbcSession(this.source.getConnection()).sql(
                            // @checkstyle StringLiteralsConcatenation (5 lines)
                            "CREATE CACHED TABLE IF NOT EXISTS %table-1% ("
                            + " key BIGINT NOT NULL,"
                            + " value BINARY(255) NOT NULL,"
                            + " vnum BIGINT,"
                            + " PRIMARY KEY(key, value))"
                        ).table(name).execute();
                        this.tables.add(name);
                    }
                }
            }
            return new JdbcSession(this.source.getConnection());
        } catch (java.sql.SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Serialize this value into string.
     * @param value The value
     * @return Serialized
     * @param <T> Type of value
     */
    private static <T> byte[] serialize(final T value) {
        return SerializationUtils.serialize((Serializable) value);
    }

    /**
     * Deserialize this string into value.
     * @param bytes The data
     * @return De-serialized value
     * @param <T> Type of value
     */
    private static <T> T deserialize(final byte[] bytes) {
        return (T) SerializationUtils.deserialize(bytes);
    }

    /**
     * Create datasource in this directory.
     * @param dir Where to keep data
     * @return The datasource
     */
    private static DataSource datasource(final File dir) {
        dir.mkdirs();
        final BoneCPDataSource src = new BoneCPDataSource();
        src.setDriverClass("org.hsqldb.jdbcDriver");
        src.setJdbcUrl(
            String.format(
                "jdbc:hsqldb:file:%s",
                new File(dir, "hsqldb.")
            )
        );
        src.setUsername("sa");
        src.setPassword("");
        // @checkstyle MagicNumber (1 line)
        src.setMaxConnectionsPerPartition(50);
        return src;
    }

    /**
     * Preserve this ResultSet for some time future.
     * @param rset The result set
     */
    private void preserve(final ResultSet rset) {
        synchronized (this.rsets) {
            Long now = System.currentTimeMillis();
            for (Long time : this.rsets.keySet()) {
                // @checkstyle MagicNumber (1 line)
                if (time < now - 60 * 1000) {
                    DbUtils.closeQuietly(this.rsets.get(time));
                    this.rsets.remove(time);
                }
            }
            while (this.rsets.containsKey(now)) {
                --now;
            }
            this.rsets.put(now, rset);
        }
    }

    /**
     * Long handler.
     */
    private final class LongHandler implements
        JdbcSession.Handler<Iterator<Long>> {
        @Override
        public Iterator<Long> handle(final ResultSet rset)
            throws SQLException {
            return new AbstractIterator<Long>() {
                @Override
                public Long fetch() {
                    Long number = null;
                    try {
                        if (rset.next()) {
                            number = rset.getLong(1);
                        }
                    } catch (SQLException ex) {
                        throw new IllegalStateException(ex);
                    }
                    HsqlTriples.this.preserve(rset);
                    return number;
                }
            };
        }
    }

    /**
     * Values handler.
     */
    private final class ValuesHandler<T> implements
        JdbcSession.Handler<Iterator<T>> {
        @Override
        public Iterator<T> handle(final ResultSet rset)
            throws SQLException {
            return new AbstractIterator<T>() {
                @Override
                public T fetch() {
                    T value = null;
                    try {
                        if (rset.next()) {
                            value = (T) HsqlTriples.deserialize(
                                rset.getBytes(1)
                            );
                        }
                    } catch (SQLException ex) {
                        throw new IllegalStateException(ex);
                    }
                    HsqlTriples.this.preserve(rset);
                    return value;
                }
            };
        }
    }

}
