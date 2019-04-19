import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;

interface DBUserInteractionable {
    public void addUser(final String login, final String password);
    public boolean isUserRegistred(final String login);
    public boolean auth(final String login, final String password);
}

interface DBCityCollection {
    public void add(City city);
    public void removeFirst();
    public String show();
    public void remove(City city);
    public boolean addIfMax(City city);
    public boolean removeLower(City city);
}

class DatabaseInteraction implements DBUserInteractionable, DBCityCollection {
    private static final String DB_DRIVER = "org.postgresql.Driver";
    private static final String DB_CONNECTION = "jdbc:postgresql://localhost:5432/lab7";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    //private static final String DB_PASSWORD = "admin";
    //private static final String DB_USER = "postgres";
    //private static final String DB_CONNECTION = "jdbc:postgresql://127.0.0.1:5432/laba7DB";

    private Connection dbConnection;

    public DatabaseInteraction() {
        getDBConnection();
        try {
            activateQuery("CREATE TABLE IF NOT EXISTS users (id SERIAL PRIMARY KEY, login TEXT NOT NULL UNIQUE, password TEXT NOT NULL);");
            activateQuery("CREATE TABLE IF NOT EXISTS objects (id int4 NOT NULL, parent_id int4 REFERENCES users(id) ON DELETE CASCADE, json TEXT , PRIMARY KEY (id, parent_id));");
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
    public void add(City city) {

        PreparedStatement statement = null;
        try {
            statement = dbConnection.prepareStatement("INSERT INTO objects (json, parent_id) VALUES (?, ?)");

            JSONObject json= getJsonObject(city);


            statement.setString(1, json.toString());
        statement.setInt(2, 0);
        statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeFirst() {


        PreparedStatement statement = null;
        try {
            statement = dbConnection.prepareStatement("DELETE FROM objects [WHERE parent_id=?] LIMIT 1;");


            statement.setInt(1, 0);
            statement.execute();



        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void remove(City city) {

        PreparedStatement statement = null;
        try {
            statement = dbConnection.prepareStatement("DELETE FROM objects [WHERE json=? AND parent_id=?];");

            JSONObject json = getJsonObject(city);

            statement.setString(1, json.toString());
            statement.setInt(2, 0);
            statement.execute();



        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String show() {
        PreparedStatement statement = null;
        String returnString="";
        try {
        statement = dbConnection.prepareStatement("SELECT * FROM objects;");

        statement.execute();


            ResultSet result = statement.executeQuery();


            while (result.next()){
                returnString+= processInput(result.getString("json")).toString()+"\n";

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return returnString;
    }

    @Override
    public boolean addIfMax(City city) {


        PreparedStatement statement = null;

        try {
            statement = dbConnection.prepareStatement("SELECT * FROM objects WHERE parent_id=?;");
            statement.setInt(1, 0);
            statement.execute();


            ResultSet result = statement.executeQuery();

            City maxcity=null;
            City curcity=null;
            while (result.next()){
            curcity=processInput(result.getString("json"));
            if(curcity.compareTo(maxcity)>0) maxcity=curcity ;

            }

            if(city.compareTo(maxcity)>0) {
                JSONObject json = getJsonObject(city);


                statement = dbConnection.prepareStatement("INSERT INTO objects (json, parent_id) VALUES (?, ?)");
                statement.setString(1, json.toString());
                statement.setInt(2, 0);
                statement.execute();
                return true;
            }




        } catch (SQLException e) {
            e.printStackTrace();
        }


        return false;
    }

    private JSONObject getJsonObject(City city) {
        JSONObject json = new JSONObject();
        json.put("name",city.name);
        json.put("areaSize",city.areaSize);
        json.put("initDate",city.initDate);
        json.put("x",city.x);
        json.put("y",city.y);
        return json;
    }

    @Override
    public boolean removeLower(City city) {

        PreparedStatement statement = null;

        try {
            statement = dbConnection.prepareStatement("SELECT * FROM objects WHERE parent_id=?;");
            statement.setInt(1, 0);
            statement.execute();

            ResultSet result = statement.executeQuery();
            statement = dbConnection.prepareStatement("DELETE FROM objects [WHERE id=IN (?)]");


            String ids="";
            City curcity=null;
            while (result.next()){
                curcity=processInput(result.getString("json"));

                if(city.compareTo(curcity)>0) {
                    JSONObject json = getJsonObject(city);
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


    protected City processInput(String jsonInput) {
        City city = null;
        String name;
        Integer size, x, y;

        if (jsonInput.indexOf('}') < jsonInput.length() - 1)
            throw new RuntimeException("uncorrect command");

        try {
            JSONObject obj = new JSONObject(jsonInput);

            try {
                name = obj.getString("name");
            } catch (JSONException e) {
                name = "Unnamed";
            }

            try {
                size = obj.getInt("size");
            } catch (JSONException e) {
                size = 0;
            }

            try {
                x = obj.getInt("x");
            } catch (JSONException e) {
                x = 0;
            }

            try {
                y = obj.getInt("y");
            } catch (JSONException e) {
                y = 0;
            }

            city = new City(name, size, x, y);

        } catch (JSONException e) {
            throw new RuntimeException("uncorrect command");
        }

        return city;
    }


}

