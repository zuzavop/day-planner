package com.myplanner.app;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * managing displaying plans to given panel
 * connected to MyConnection - mediates query parameters into MySQL database
 */
public class MyPlanner {
    private final JPanel display;    // for displaying output
    private final Connection connection;

    /**
     * constructor - set attributes
     * @param output panel where plans will be displayed
     */
    MyPlanner(@NotNull JPanel output, boolean localServer) {
        display = output;
        display.setLayout(new BoxLayout(display, BoxLayout.Y_AXIS));
        connection = localServer ? new ConnectionToLocalDatabase() : new ConnectionViaPHP();
    }

    /**
     * @return array of years (string) used in database
     */
    public String[] getYears() {
        String[] years = new String[]{"ANY"};
        try {
            years = connection.getYears();
        } catch (ConnectionException e) {
            display.add(new JLabel(e.getMessage()));
        }
        return years;
    }

    /**
     * method to initialize the query - looking for plan with given parameters and displaying result
     *
     * @param y year when plan is set
     * @param m month when plan is set
     * @param d day when plan is set
     * @param n name of plan
     */
    public void getQueryResult(int y, int m, int d, String n) {
        //reset display
        display.removeAll();
        display.revalidate();
        display.repaint();

        // show data that respond query
        try {
            Plan[] plans = connection.getPlans(n, y, m, d);
            showResult(plans);
        } catch (ConnectionException e) {
            showWarning(e.getMessage());
        }
    }

    /**
     * method to output the result of query
     *
     * @param plans array of plans which should be display to panel
     */
    private void showResult(Plan[] plans) {
        for (Plan item : plans) {
            // control if corresponds to query
            display.add(new PlannerBox(item, connection, display));
        }
        if (plans.length == 0) {
            display.add(new Label("No plans match")); // get respond when nothing fit
        }
    }

    /**
     * @return instance of MyConnection which is used by this class
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * display date from given data and show plans which correspond to given parameters
     *
     * @param y year when the plan is set
     * @param m month when the plan is set
     * @param d day when the plan is set
     */
    public void getAndShowPlans(int y, int m, int d) {
        //reset display
        display.removeAll();
        display.revalidate();
        display.repaint();

        // show data that respond query
        display.add(new JLabel(d + "." + m + ". " + y));
        Plan[] plans;
        try {
            plans = connection.getPlansOnDay(y, m, d);
            showResult(plans);
        } catch (ConnectionException e) {
            showWarning(e.getMessage());
        }
    }

    public void addNewPlan(Plan plan) {
        display.add(new PlannerBox(plan, connection, display));
        display.revalidate();
        display.repaint();
    }

    public void showWarning(String message) {
        Planner.showWarning(display, message);
    }
}

/**
 * class create and manage layout of window with form where user can set attributes of new plan
 * after user click to "save" button class send attributes to class MyConnection (manage adding to database)
 * window (frame) and connection is given in constructor
 */
class AddingNew extends JPanel implements ActionListener {
    private final JButton save = new JButton("Save");
    private final JTextField name = new JTextField(20);
    private final JTextField date = new JTextField(20);
    private final JTextField time = new JTextField(20);
    private final JFrame frame;
    private final MyPlanner handler; // connection to database
    private JComboBox<String> year;

    /**
     * create window attributes and set labels
     *
     * @param f    frame where form will be created
     * @param han  MyPlanner instance through which data will be sent to database
     *             and new plan will be added to frame
     * @param y    picker of years
     */
    public AddingNew(@NotNull JFrame f, @NotNull MyPlanner han, JComboBox<String> y) {
        frame = f;
        handler = han;
        year = y;
        save.addActionListener(this);

        // set instructions for user
        JLabel nameL = new JLabel("(mandatory) Please add name of your plan");
        JLabel dateL = new JLabel("(mandatory) Please fill date of your plan in the form 'd.M.yyyy' or 'd/M/yyyy'");
        JLabel timeL = new JLabel("(optional) Please fill time of your plan in form 'hh:mm'");

        // set labels
        JLabel labelName = new JLabel("Name: ");
        JLabel labelDate = new JLabel("Date: ");
        JLabel labelTime = new JLabel("Time: ");

        // set window attributes
        GroupLayout showing = new GroupLayout(this);
        this.setLayout(showing);
        showing.setAutoCreateGaps(true);
        showing.setAutoCreateContainerGaps(true);
        showing.setHorizontalGroup(showing.createSequentialGroup()
                .addGroup(showing.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addComponent(labelName)
                        .addComponent(labelDate)
                        .addComponent(labelTime))
                .addGroup(showing.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(name)
                        .addComponent(date)
                        .addComponent(time))
                .addGroup(showing.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(nameL)
                        .addComponent(dateL)
                        .addComponent(timeL))
                .addComponent(save)
        );

        showing.setVerticalGroup(showing.createSequentialGroup()
                .addGroup(showing.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(labelName)
                        .addComponent(name)
                        .addComponent(nameL))
                .addGroup(showing.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(labelDate)
                        .addComponent(date)
                        .addComponent(dateL))
                .addGroup(showing.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(labelTime)
                        .addComponent(time)
                        .addComponent(timeL))
                .addComponent(save)
        );
    }

    /**
     * even processing - save button click
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == save) {
            actionOnSave();
        }
    }

    /**
     * control input in fields and if everything valid sent request to add new plan to database
     * other way warn user to invalid input
     *
     * date (if it can be parse in format d.M.yyyy or dd/MM/yyyy) is convert automatically to valid date (for instance 30.2. is convert to 28.2. if not leap year)
     */
    private void actionOnSave() {
        Plan plan = new Plan();
        try {
            if (name.getText().length() == 0) {
                // set warning - name is empty (must be given)
                name.setBackground(Color.RED);
            } else {
                name.setBackground(Color.WHITE);
            }
            LocalDate dat = getDate(date.getText()); //parse to valid date
            date.setBackground(Color.WHITE);
            time.setBackground(Color.WHITE);
            if (plan.setValuesIfValid(name.getText(), time.getText(), dat.getYear(), dat.getMonthValue(), dat.getDayOfMonth())) {
                try {
                    handler.getConnection().addNew(plan);

                    // update values in picker of years
                    year = new JComboBox<>(handler.getYears());
                    // add new plan
                    handler.addNewPlan(plan);

                    // close window
                    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                } catch (ConnectionException e) {
                    handler.showWarning(e.getMessage());
                }
            } else {
                time.setBackground(Color.RED);
            }
        } catch (DateTimeException ex) {
            // date was in wrong format - set warning
            date.setBackground(Color.RED);
        }
    }

    /**
     * convert string date to localdate if in correct format
     * @param text text containing the date
     * @return date as LocalDate if parsable
     * @throws DateTimeException throw if date is in invalid format
     */
    public static LocalDate getDate(String text) throws DateTimeException{
        DateTimeFormatter[] formatter = new DateTimeFormatter[]{DateTimeFormatter.ofPattern("d.M.yyyy"), DateTimeFormatter.ofPattern("d/M/yyyy")};
        for (DateTimeFormatter pattern : formatter) {
            try {
                // Take a try
                return LocalDate.parse(text, pattern);

            } catch (DateTimeException pe) {
                // Try second option
            }
        }
        // date don't fit to any of formats
        throw new DateTimeException("");
    }

}