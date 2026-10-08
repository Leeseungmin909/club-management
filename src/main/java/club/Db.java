package club;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.tomcat.jdbc.pool.DataSource;

/** Tomcat 내장 커넥션 풀. 사용: try (Connection c = Db.get()) { ... } */
public final class Db {
    private static final DataSource ds = new DataSource();

    static {
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setUrl(Config.get("db.url"));
        ds.setUsername(Config.get("db.user"));
        ds.setPassword(Config.get("db.password"));
        ds.setTestOnBorrow(true);
        ds.setValidationQuery("SELECT 1");
    }

    private Db() {}

    public static Connection get() throws SQLException {
        return ds.getConnection();
    }
}
