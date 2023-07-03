package com.myplanner.app;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * connection to database by using php scripts on server - sending request to url of php script with given data
 */
public class ConnectionViaPHP implements Connection {
    private int maxId; // using for adding new plans (as new id)
    private String server;
    private final int timeout = 5000; // timeout to try to connect

    /**
     * finding max(id) in database
     */
    public ConnectionViaPHP() {
        try (FileInputStream propsInput = new FileInputStream("src/config.properties")){
            Properties prop = new Properties();
            prop.load(propsInput);

            server = prop.getProperty("SERVER_ADDRESS");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            String req = server + "getId.php";

            URL url = new URL(req);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            new Thread(new InterruptThread(conn)).start();

            String[] response = getData(conn);
            if (response.length > 0) {
                final JSONObject obj = new JSONObject("{" + response[0]);
                maxId = obj.getInt("MAX(id)");
            } else {
                // database is empty
                maxId = 0;
            }
        } catch (ConnectionException e) {
            maxId = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * read data from url response and parse then by "{"
     *
     * @param conn connection to url where runs php script
     * @return parse data in json format
     */
    private String[] getData(@NotNull HttpURLConnection conn) throws ConnectionException {
        StringBuilder response = new StringBuilder();
        try {
            // controling response of url
            int responseCode = conn.getResponseCode();

            InputStream inputStream;
            if (200 <= responseCode && responseCode <= 299) {
                inputStream = conn.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

                String currentLine;
                while ((currentLine = in.readLine()) != null)
                    response.append(currentLine); // get response

                in.close();
            } else {
                throw new ConnectionException("Failed to connect to server");
            }
        } catch (IOException ex) {
            throw  new ConnectionException(ex.getMessage());
        }

        String[] res = response.toString().split("\\{");
        res = Arrays.copyOfRange(res, 1, res.length);

        return res;
    }

    /**
     * @param conn connection to url where runs php script
     * @return list of plan given from database
     */
    private List<Plan> getPlansList(@NotNull HttpURLConnection conn) throws ConnectionException {
        String[] response = getData(conn);
        List<Plan> plans = new ArrayList<>();

        for (String line : response) {
            final JSONObject obj = new JSONObject("{" + line.substring(0, line.length() - 1));
            plans.add(new Plan(obj.getInt("id"), obj.getString("name"),
                    (obj.isNull("time") ? "" : (String) obj.get("time")), obj.getInt("year"),
                    obj.getInt("month"), obj.getInt("day")));
        }

        return plans;
    }

    /**
     * sent request to php script with given parameters
     *
     * @param name  name of request plans
     * @param year  year of request plans
     * @param month month of request plans
     * @param day   day of request plans
     * @return plans which correspond to request (given parameters)
     */
    public Plan[] getPlans(@NotNull String name, int year, int month, int day) throws ConnectionException {
        List<Plan> plans;
        try {
            String n = URLEncoder.encode(name, StandardCharsets.UTF_8.name());
            String req = server + "getData.php?name=" + n + "&year=" + year + "&month=" + month + "&day=" + day;

            URL url = new URL(req);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            new Thread(new InterruptThread(conn)).start();
            conn.setRequestMethod("GET");

            plans = getPlansList(conn);
        } catch (IOException e) {
            throw new ConnectionException(e.getMessage());
        }
        return plans.toArray(new Plan[0]);
    }

    /**
     * sent request to php script with given date
     *
     * @param y year of request plans
     * @param m month of request plans
     * @param d day of request plans
     * @return plans that match given date
     */
    public Plan[] getPlansOnDay(int y, int m, int d) throws ConnectionException {
        List<Plan> plans = new ArrayList<>();
        try {
            String req = server + "getData.php?year=" + y + "&month=" + m + "&day=" + d;

            URL url = new URL(req);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            new Thread(new InterruptThread(conn)).start();

            plans = getPlansList(conn);
        } catch (UnknownHostException ex) {
            throw  new ConnectionException(ex.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return plans.toArray(new Plan[0]);
    }

    /**
     * sent request to php script which return json with years
     *
     * @return array of sort years from database
     */
    public String[] getYears() throws ConnectionException {
        List<String> years = new ArrayList<>();
        try {
            String req = server + "getYears.php";

            URL url = new URL(req);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            new Thread(new InterruptThread(conn)).start();

            // parse given json
            String[] response = getData(conn);
            for (String line : response) {
                final JSONObject obj = new JSONObject("{" + line.substring(0, line.length() - 1));
                String year = String.valueOf(obj.getInt("year"));
                if (!years.contains(year)) {
                    years.add(year);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // sorting data
        List<Integer> intList = new ArrayList<>();
        for (String s : years) {
            intList.add(Integer.valueOf(s));
        }
        Collections.sort(intList);
        years.clear();
        years.add("ANY");
        for (int i : intList) {
            years.add(String.valueOf(i));
        }

        return years.toArray(new String[0]);
    }

    /**
     * sent request to php script for adding new plan with given parameters
     *
     * @param item new set plan
     */
    public void addNew(@NotNull Plan item) throws ConnectionException {
        try {
            String name = URLEncoder.encode(item.getName(), StandardCharsets.UTF_8.name());
            String req = server + "insert.php?id=" + ++maxId
                    + "&name=" + name + "&year=" + item.getYear() + "&month=" + item.getMonth()
                    + "&day=" + item.getDay() + "&time=" + item.getTime();

            URL url = new URL(req);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            new Thread(new InterruptThread(conn)).start();

            conn.getResponseMessage();
        } catch (UnknownHostException ex) {
            throw  new ConnectionException(ex.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * sent request to php script to update plan with given parameters
     *
     * @param item plan that was changed (must be update in database)
     */
    public void updatePlan(@NotNull Plan item) throws ConnectionException {
        try {
            String name = URLEncoder.encode(item.getName(), StandardCharsets.UTF_8.name());
            String req = server + "update.php?id=" + item.getId()
                    + "&name=" + name + "&year=" + item.getYear() + "&month=" + item.getMonth()
                    + "&day=" + item.getDay() + "&time=" + item.getTime();

            URL url = new URL(req);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            new Thread(new InterruptThread(conn)).start();

            conn.getResponseMessage();
        } catch (UnknownHostException ex) {
            throw  new ConnectionException(ex.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * sent delete request to php script
     *
     * @param idPlan plan which should be delete
     */
    public void deletePlan(int idPlan) throws ConnectionException {
        try {
            URL url = new URL(server + "deldata.php?id=" + idPlan);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            new Thread(new InterruptThread(conn)).start();

            conn.getResponseMessage();
        } catch (UnknownHostException ex) {
            throw new ConnectionException(ex.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/**
 * thread that wait for 5000 milliseconds and then close connection
 */
class InterruptThread implements Runnable {

    HttpURLConnection con;
    public InterruptThread(HttpURLConnection con) {
        this.con = con;
    }

    public void run() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {

        }
        con.disconnect();
    }
}