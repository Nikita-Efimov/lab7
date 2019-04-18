import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

class DatabaseInteraction {
    private static final String DB_DRIVER = "org.postgresql.Driver";
    private static final String DB_CONNECTION = "jdbc:postgresql://localhost:5432/lab7";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";
    private Connection dbConnection;

    public DatabaseInteraction() {
        getDBConnection();
        try {
            activateQuery("CREATE TABLE IF NOT EXISTS users (id SERIAL PRIMARY KEY, login TEXT NOT NULL UNIQUE, password TEXT NOT NULL);");
            activateQuery("CREATE TABLE IF NOT EXISTS objects (id int4 NOT NULL, parent_id int4 REFERENCES users(id) ON DELETE CASCADE, PRIMARY KEY (id, parent_id));");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void getDBConnection() {
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        try {
            dbConnection = DriverManager.getConnection(DB_CONNECTION, DB_USER,DB_PASSWORD);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void activateQuery(String q) throws SQLException {
        Statement statement;
        statement = dbConnection.createStatement();
        statement.execute(q);
    }
}
