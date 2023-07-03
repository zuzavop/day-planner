package com.myplanner.app;

/**
 * this class is not used cause of testing of application
 * contains original connection to the MySQL database via a Java library
 */

import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * connection to local database
 */
public class ConnectionToLocalDatabase implements Connection {
    private static java.sql.Connection con = null;
    private int maxId;

    /**
     * open connection to database on localhost server
     */
    private void connect() throws ConnectionException {
        try (FileInputStream propsInput = new FileInputStream("src/config.properties")){
            Properties prop = new Properties();
            prop.load(propsInput);

            String driver = prop.getProperty("DB_DRIVER");
            Class.forName(driver);
            String url = prop.getProperty("DB_URL");
            String user = prop.getProperty("DB_USER");
            String password = prop.getProperty("DB_PASSWORD");
            con = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new ConnectionException("SQLException: " + e.getMessage());
        } catch (ClassNotFoundException | IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * get plans from database
     * @param name  name of request plans
     * @param year  year of request plans
     * @param month month of request plans
     * @param day   day of request plans
     * @return all plans from database
     */
    @Override
    public Plan[] getPlans(@NotNull String name, int year, int month, int day) throws ConnectionException {
        connect();
        List<Plan> plans = new ArrayList<>();
        try {
            String query = "SELECT id, name, time FROM calendar  WHERE year = ? AND month = ? AND day = ? AND name = ?";

            // create the java statement
            PreparedStatement preparedStmt = con.prepareStatement(query);
            if(year != -1) preparedStmt.setInt(1, year);
            if(month != -1) preparedStmt.setInt(2, month);
            if(day != -1) preparedStmt.setInt(3, day);
            if(!name.equals("")) preparedStmt.setString(4, name);

            // execute the query, and get a java resultset
            ResultSet rs = preparedStmt.executeQuery(query);

            // iterate through the java resultset
            while (rs.next()) {
                int id = rs.getInt("id");
                String n = rs.getString("name");
                Time time = rs.getTime("time");

                plans.add(new Plan(id, n, time, year, month, day));
            }

            preparedStmt.close();
        } catch (SQLException | NullPointerException e) {
            throw new ConnectionException(e.getMessage());
        } finally {
            try {
                con.close();
            } catch (SQLException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return plans.toArray(new Plan[0]);
    }

    /**
     * connect to database and select plans which corresponds to given parameters
     * @param y year of request plans
     * @param m month of request plans
     * @param d day of request plans
     * @return array of plans that math request
     */
    @Override
    public Plan[] getPlansOnDay(int y, int m, int d) throws ConnectionException {
        connect();
        List<Plan> plans = new ArrayList<>();
        try {
            String query = "SELECT id, name, time FROM calendar  WHERE year = ? AND month = ? AND day = ?";

            // create the java statement
            PreparedStatement preparedStmt = con.prepareStatement(query);
            if(y != -1) preparedStmt.setInt(1, y);
            if(m != -1) preparedStmt.setInt(2, m);
            if(d != -1) preparedStmt.setInt(3, d);

            // execute the query, and get a java resultset
            ResultSet rs = preparedStmt.executeQuery(query);

            // iterate through the java resultset
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                Time time = rs.getTime("time");

                plans.add(new Plan(id, name, time, y, m, d));
            }

            preparedStmt.close();
        } catch (SQLException | NullPointerException e) {
            throw new ConnectionException(e.getMessage());
        } finally {
            try {
                con.close();
            } catch (SQLException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return plans.toArray(new Plan[0]);
    }

    /**
     * get years from database and set maxId to max id from database
     * @return array of distinct years from database
     */
    @Override
    public String[] getYears() throws ConnectionException {
        connect();
        List<String> years = new ArrayList<>();
        years.add("ANY");
        try {
            String query = "SELECT DISTINCT year, id FROM calendar";

            // create the java statement
            Statement st = con.createStatement();

            // execute the query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next()) {
                int date = rs.getInt("year");
                maxId = Math.max(maxId, rs.getInt("id"));
                if (!years.contains(Integer.toString(date))) {
                    years.add(Integer.toString(date));
                }
            }

            st.close();
        } catch (SQLException | NullPointerException e) {
            throw new ConnectionException(e.getMessage());
        } finally {
            try {
                con.close();
            } catch (SQLException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return years.toArray(new String[0]);
    }

    /**
     * insert new plan to database
     * @param item plan which will be add to database
     */
    @Override
    public void addNew(Plan item) throws ConnectionException {
        connect();
        // the mysql insert statement
        String query = " INSERT INTO calendar (id, name, year, month, day, time)"
                + " values (?, ?, ?, ?, ?, ?)";

        try {
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt = con.prepareStatement(query);
            preparedStmt.setInt(1, ++maxId);
            preparedStmt.setString(2, item.getName());
            preparedStmt.setInt(3, item.getYear());
            preparedStmt.setInt(4, item.getMonth());
            preparedStmt.setInt(5, item.getDay());
            if (item.getTime().equals("")) preparedStmt.setNull(4, Types.TIME);
            else preparedStmt.setTime(4, Time.valueOf(item.getTime() + ":00"));

            // execute the preparedstatement
            preparedStmt.execute();
        } catch (SQLException e) {
            throw new ConnectionException(e.getMessage());
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * update row in database by given parameters
     * @param item plan which will be update, id, name and date of plan must be set
     */
    @Override
    public void updatePlan(Plan item) throws ConnectionException {
        connect();
        try {
            // create the java mysql update preparedstatement
            String query = "UPDATE calendar SET name = ?, year = ?, month = ?, day = ?, time = ? WHERE id = ?";

            PreparedStatement preparedStmt = con.prepareStatement(query);
            preparedStmt.setString(1, item.getName());
            preparedStmt.setInt(2, item.getYear());
            preparedStmt.setInt(3, item.getMonth());
            preparedStmt.setInt(4, item.getDay());
            preparedStmt.setTime(5, (Objects.equals(item.getTime(), "") ? null : Time.valueOf(item.getTime() + ":00")));
            preparedStmt.setInt(6, item.getId());

            // execute the java preparedstatement
            preparedStmt.executeUpdate();
        } catch (SQLException e) {
            throw new ConnectionException(e.getMessage());
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * delete row with given id from database
     * @param idPlan id of plan which will be delete from database
     */
    @Override
    public void deletePlan(int idPlan) throws ConnectionException {
        connect();
        try {
            String query = "DELETE FROM calendar WHERE id = ?";
            PreparedStatement preparedStmt = con.prepareStatement(query);
            preparedStmt.setInt(1, idPlan);

            // execute the preparedstatement
            preparedStmt.execute();
        } catch (SQLException e) {
            throw new ConnectionException(e.getMessage());
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
}

