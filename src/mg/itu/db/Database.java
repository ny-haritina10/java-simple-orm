package mg.itu.db;

import java.sql.Connection;
import java.sql.DriverManager;

public class Database {

    public static Connection getConnection()
        throws Exception
    {
        Class.forName("org.postgresql.Driver");
        Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ibex-crud", "postgres", "nh20041010");

        return con;
    }
}