import java.sql.*;

interface DBUserInteractionable {
    public void addUser(final String login, final String password);
    public boolean isUserRegistred(final String login);
    public boolean auth(final String login, final String password);
    public int getUserIdFromLogin(final String login);
}

interface DBCityCollection {
    public void add(City city);
    public void removeFirst();
    public void remove(City city);
    public String show();
    public boolean addIfMax(City city);
    public boolean removeLower(City city);
}

class DatabaseInteraction implements DBUserInteractionable, DBCityCollection {
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

    private void activateQuery(final String q) throws SQLException {
        Statement statement;
        statement = dbConnection.createStatement();
        statement.execute(q);
    }

    @Override
    public void addUser(final String login, final String password) {
        try {
            PreparedStatement statement = dbConnection.prepareStatement("INSERT INTO users (login, password) VALUES (?, ?)");
            statement.setString(1, login);
            statement.setString(2, password);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isUserRegistred(final String login) {
        try {
            PreparedStatement statement = dbConnection.prepareStatement("SELECT id FROM users WHERE login = ?");
            statement.setString(1, login);
            ResultSet result = statement.executeQuery();
            // Если что-то есть то да если нет то нет
            return result.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // заглушечка
        return true;
    }

    @Override
    public boolean auth(final String login, final String password) {
        try {
            PreparedStatement statement = dbConnection.prepareStatement("SELECT id FROM users WHERE login = ? and password = ?");
            statement.setString(1, login);
            statement.setString(2, password);
            ResultSet result = statement.executeQuery();
            // Если что-то есть то да если нет то нет
            return result.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // заглушечка
        return false;
    }

    @Override
    public int getUserIdFromLogin(final String login) {

    }

    @Override
    public void add(City city) {
        final int userId = getUserIdFromLogin(UserAuth.getCurrentThreadUserAuth().login);
        System.out.println(UserAuth.getCurrentThreadUserAuth().login);
    }

    @Override
    public void removeFirst() {

    }

    @Override
    public void remove(City city) {

    }

    @Override
    public String show() {
        return "";
    }

    @Override
    public boolean addIfMax(City city) {
        return false;
    }

    @Override
    public boolean removeLower(City city) {
        return false;
    }
}
