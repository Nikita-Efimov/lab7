import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String DB_DRIVER = "org.postgresql.Driver";
    private static final String DB_CONNECTION = "jdbc:postgresql://localhost:5432/laba7DB";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "admin";
    private Connection dbConnection;

    public Database() {
        getDBConnection();
        try {
            activateQuery("CREATE TABLE IF NOT EXISTS users (ID SERIAL PRIMARY KEY, login VARCHAR(256), password VARCHAR(256));");
            activateQuery("CREATE TABLE IF NOT EXISTS objects (ID SERIAL PRIMARY KEY, owner_id int);");
            System.out.println('1');
        } catch (SQLException e) {
            e.printStackTrace();
        }




    }


    private Connection getDBConnection() {

        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        try {
            dbConnection = DriverManager.getConnection(DB_CONNECTION, DB_USER,DB_PASSWORD);
            return dbConnection;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return dbConnection;
    }

    public void activateQuery(String q) throws SQLException {
        Statement statement;
        statement = dbConnection.createStatement();
        statement.execute(q);

    }



}
