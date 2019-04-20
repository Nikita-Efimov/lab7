import org.json.JSONException;
import org.json.JSONObject;
import java.sql.*;
import java.time.OffsetDateTime;
import java.time.Instant;
import java.time.ZoneId;

interface DBUserInteractionable {
    public void addUser(final String login, final String password);
    public boolean isUserRegistred(final String login);
    public boolean auth(final String login, final String password);
    public Integer getUserIdFromLogin(final String login);
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

    // private static final String DB_PASSWORD = "admin";
    // private static final String DB_USER = "postgres";
    // private static final String DB_CONNECTION = "jdbc:postgresql://127.0.0.1:5432/laba7DB";

    private static final String DB_CONNECTION = "jdbc:postgresql://localhost:5432/lab7";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";
    private Connection dbConnection;

    public DatabaseInteraction() {
        getDBConnection();
        try {
            activateQuery("CREATE TABLE IF NOT EXISTS users (id SERIAL PRIMARY KEY, login TEXT NOT NULL UNIQUE, password TEXT NOT NULL);");
            activateQuery("CREATE TABLE IF NOT EXISTS objects (id SERIAL, parent_id int4 REFERENCES users(id) ON DELETE CASCADE, name TEXT NOT NULL, area_size int4, x int4, y int4, init_date int4, PRIMARY KEY (id, parent_id));");
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
            dbConnection = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
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
    public Integer getUserIdFromLogin(final String login) {
        Integer id = null;

        try {
            PreparedStatement statement = dbConnection.prepareStatement("SELECT id FROM users WHERE login = ?");
            statement.setString(1, login);
            ResultSet result = statement.executeQuery();

            result.next();
            id = result.getInt("id");
        } catch (SQLException e) {
            throw new RuntimeException("this user not authorized");
        }

        return id;
    }

    @Override
    public void add(City city) {
        final int userId = getUserIdFromLogin(UserAuth.getCurrentThreadUserAuth().login);

        try {
            PreparedStatement statement = dbConnection.prepareStatement("INSERT INTO objects (parent_id, name, area_size, x, y, init_date) VALUES (?, ?, ?, ?, ?, ?)");
            statement.setInt(1, userId);
            statement.setString(2, city.getName());
            statement.setInt(3, city.getAreaSize());
            statement.setInt(4, city.getX());
            statement.setInt(5, city.getY());
            statement.setInt(6, (int)city.getInitDate());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeFirst() {
        final int userId = getUserIdFromLogin(UserAuth.getCurrentThreadUserAuth().login);

        PreparedStatement statement = null;
        try {
            statement = dbConnection.prepareStatement("DELETE FROM objects [WHERE parent_id = ?] LIMIT 1");

            statement.setInt(1, userId);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void remove(City city) {
        final int userId = getUserIdFromLogin(UserAuth.getCurrentThreadUserAuth().login);

        try {
            PreparedStatement statement = dbConnection.prepareStatement("DELETE FROM objects WHERE (parent_id, name, area_size, x, y, init_date)= (?, ?, ?, ?, ?, ?);");

            statement.setInt(1, userId);
            statement.setString(2, city.getName());
            statement.setInt(3, city.getAreaSize());
            statement.setInt(4, city.getX());
            statement.setInt(5, city.getY());
            statement.setInt(6, (int)city.getInitDate());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String show() {
        String returnString = "";

        try {
            PreparedStatement statement = dbConnection.prepareStatement("SELECT * FROM objects;");
            ResultSet result = statement.executeQuery();

            City city = new City();
            while (result.next()) {
                city.setName(result.getString("name"));
                city.x = result.getInt("x");
                city.y = result.getInt("y");
                city.areaSize = result.getInt("area_size");
                city.initDate = OffsetDateTime.ofInstant(Instant.ofEpochSecond(result.getInt("init_date")), ZoneId.systemDefault());

                returnString += city.toString() + "\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnString;
    }

    @Override
    public boolean addIfMax(City city) {
        final int userId = getUserIdFromLogin(UserAuth.getCurrentThreadUserAuth().login);

        try {
            PreparedStatement statement = dbConnection.prepareStatement("SELECT * FROM objects WHERE parent_id = ?;");
            statement.setInt(1, userId);
            ResultSet result = statement.executeQuery();

            City maxcity = null;
            City curcity = null;
            while (result.next()){
                curcity.setName(result.getString("name"));
                curcity.x=(result.getInt("x"));
                curcity.y=(result.getInt("y"));
                curcity.areaSize=(result.getInt("area_size"));
                curcity.initDate=OffsetDateTime.parse(result.getString("init_date"));


                if(curcity.compareTo(maxcity)>0) maxcity=curcity ;

            }

            if(city.compareTo(maxcity)>0) {



                statement = dbConnection.prepareStatement("INSERT INTO objects (parent_id, name, area_size, x, y, init_date) VALUES (?, ?, ?, ?, ?, ?)");
                statement.setInt(1, userId);
                statement.setString(2, city.getName());
                statement.setInt(3, city.getAreaSize());
                statement.setInt(4, city.getX());
                statement.setInt(5, city.getY());
                statement.setInt(6, (int)city.getInitDate());
                statement.execute();


                return true;
            }




        } catch (SQLException e) {
            e.printStackTrace();
        }


        return false;
    }



    @Override
    public boolean removeLower(City city) {
        final int userId = getUserIdFromLogin(UserAuth.getCurrentThreadUserAuth().login);

        PreparedStatement statement = null;

        try {
            statement = dbConnection.prepareStatement("SELECT * FROM objects WHERE parent_id=?;");
            statement.setInt(1, userId);
            statement.execute();

            ResultSet result = statement.executeQuery();
            statement = dbConnection.prepareStatement("DELETE FROM objects [WHERE id=IN (?)]");


            String ids="";
            City curcity=null;
            while (result.next()){
                curcity.setName(result.getString("name"));
                curcity.x=(result.getInt("x"));
                curcity.y=(result.getInt("y"));
                curcity.areaSize=(result.getInt("area_size"));
                curcity.initDate=OffsetDateTime.parse(result.getString("init_date"));

                if(city.compareTo(curcity)>0) {

                    ids+=","+result.getInt("id");

                }

            }
            ids=ids.substring(1);

            statement.setString(1, ids);
            statement.execute();


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


}
