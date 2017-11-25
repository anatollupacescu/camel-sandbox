package camel;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.sql.*;

public class DBUtils {

    private String className = "org.h2.Driver";
    private String url = "jdbc:h2:~/tumblr;INIT=CREATE SCHEMA IF NOT EXISTS TUMBLR";
    private String user = "tumblr";
    private String password = "tumblr";

    public DataSource buildDataSource() throws ClassNotFoundException {
        Class.forName(className);
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL(url);
        ds.setUser(user);
        ds.setPassword(password);
        return ds;
    }

    public ResultSet execSql(String sql) throws ClassNotFoundException, SQLException {
        Connection conn = DriverManager.getConnection(url, user, password);
        PreparedStatement ps2 = conn.prepareStatement(sql);
        return ps2.executeQuery();
    }
}
