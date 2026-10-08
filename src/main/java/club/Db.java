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
        // 서버 MySQL 8 기본값. 개발 PC의 MariaDB도 같은 규칙(GROUP BY 등)으로 동작하게 한다
        ds.setInitSQL("SET SESSION sql_mode='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,"
                + "NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION'");
    }

    private Db() {}

    public static Connection get() throws SQLException {
        return ds.getConnection();
    }
}
