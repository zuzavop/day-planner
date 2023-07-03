/**
 * My Day Planner
 *
 * @author Zuzana Vopálková
 *
 * The program allows the user to create and edit plans saved in SQL database
 */


package com.myplanner.app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Objects;


public class Main {
    /**
     * program entry point
     */
    public static void main(String[] args) {
        Planner myPlanner = new Planner(Arrays.asList(args).contains("-l") || Arrays.asList(args).contains("--local"));
        myPlanner.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                }
        );
    }
}

/**
 * class Planner manage displaying components in window
 */
class Planner extends JFrame implements ActionListener {

    //parameters for finding plan on given day or with given name
    private JComboBox<String> year;
    private final JComboBox<String> month, day;
    private final JTextField name;

    private final JButton query, newPlan;
    private final MyPlanner handler;
    private JMenuItem showCalendar, findPlans; // menu bar
    private final JPanel display;

    private final boolean localServer;

    public Planner(boolean localServer) {
        super("My Day Planner"); // set name of window

        this.localServer = localServer;

        display = new JPanel();
        // set the output font
        Font font = new Font("Serif", java.awt.Font.PLAIN, 17);
        display.setFont(font);

        display.setLayout(new BoxLayout(display, BoxLayout.Y_AXIS));
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {
                Font font = new Font("Serif", java.awt.Font.PLAIN, getWidth()/20);
                setFont(font);
                revalidate();
                repaint();
            }
        });

        // create handler for displaying plans
        handler = new MyPlanner(display, localServer);
        display.add(Box.createVerticalGlue());

        // initialize the user interface components
        name = new JTextField();
        year = new JComboBox<>(handler.getYears());

        String[] months = new String[13];
        months[0] = "ANY";

        for (int i = 1; i < 13; i++)
            months[i] = Integer.toString(i);

        month = new JComboBox<>(months);

        String[] days = new String[32];
        days[0] = "ANY";

        for (int i = 1; i < 32; i++)
            days[i] = Integer.toString(i);

        day = new JComboBox<>(days);

        // create button - show plan
        query = new JButton("Show My Plans");
        query.addActionListener(this);

        // create button - add plan
        newPlan = new JButton("Add New Plan");
        newPlan.addActionListener(this);

        setQueryWindow();

        setJMenuBar(createMenuBar());
        setSize(700, 450);
        // centered window
        setLocationRelativeTo(null);
        // show window
        setVisible(true);
    }

    /**
     * set available years (years in database) to picker
     */
    private void setYears() {
        year = new JComboBox<>(handler.getYears());
    }

    /**
     * set all components of Query window - panel with buttons and form (to set parameters of query when looking for plans)
     */
    private void setQueryWindow() {
        setYears();

        // set panel containing components for querying
        JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayout(4, 2));
        panel1.add(new JLabel("Name"));
        panel1.add(name);
        panel1.add(new JLabel("Year"));
        panel1.add(year);
        panel1.add(new JLabel("Month"));
        panel1.add(month);
        panel1.add(new JLabel("Day"));
        panel1.add(day);

        // set panel2
        JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayout(1, 3));
        panel2.add(panel1);
        panel2.add(query);
        panel2.add(newPlan);

        // arrange panels on content pane using border layout
        JPanel window = new JPanel();
        window.setLayout(new BorderLayout());
        window.add(new JScrollPane(display), BorderLayout.CENTER);
        window.add(panel2, BorderLayout.SOUTH);

        setContentPane(window);
    }

    /**
     * set Calendar window when changing panels
     */
    private void setCalendarWindow() {
        CalendarWindow calendar_window = new CalendarWindow(localServer);
        setContentPane(calendar_window.setAndGetPanel());
    }

    /**
     * create menu items and add them to menu bar
     */
    private JMenuBar createMenuBar() {
        // create bar menu with options
        JMenuBar menuBar = new JMenuBar();

        // options of showing calendar
        JMenu mainMenu = new JMenu("Option");
        showCalendar = new JMenuItem("Show calendar");
        showCalendar.addActionListener(this);
        mainMenu.add(showCalendar);
        findPlans = new JMenuItem("Find plans");
        findPlans.addActionListener(this);
        mainMenu.add(findPlans);
        menuBar.add(mainMenu);

        return menuBar;
    }

    /**
     * open new window with adding new plan
     */
    private void openAddingWindow() {
        JFrame frame = new JFrame("Add new plan");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE); //close only current window
        frame.add(new AddingNew(frame, handler, year));
        frame.pack();
        frame.setLocationRelativeTo(null); // centered frame
        frame.setVisible(true);
    }

    /**
     * even processing - buttons click
     * @param e even on which action happened
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == query) {
            int numberYear, numberMonth, numberDay;

            // get the values of parameters
            numberYear = getValueOfInt((String) Objects.requireNonNull(year.getSelectedItem()));
            numberMonth = getValueOfInt((String) Objects.requireNonNull(month.getSelectedItem()));
            numberDay = getValueOfInt((String) Objects.requireNonNull(day.getSelectedItem()));
            String namePlan = name.getText();

            // get the result of query
            handler.getQueryResult(numberYear, numberMonth, numberDay, namePlan);
        } else if (e.getSource() == newPlan) {
            openAddingWindow();
        } else if (e.getSource() == showCalendar) {
            setCalendarWindow();
        } else if (e.getSource() == findPlans) {
            setQueryWindow();
        } else if (e.getSource() == year) {
            setYears();
        }
        // update window
        invalidate();
        validate();
    }

    /**
     * @param str string containing integer of "ANY"
     * @return integer value of given str
     */
    private int getValueOfInt(String str) {
        if (str.equals("ANY"))
            return -1;
        else
            return Integer.parseInt(str);

    }

    /**
     * displaying warning to given panel
     * @param display panel to which will be displayed warnings
     * @param message text of warning
     */
    public static void showWarning(JPanel display, String message){
        display.removeAll();
        display.add(new JLabel("Please check your connection - failed to connect to database."));
        display.add(new JLabel("Try repeat this action later."));
        display.add(new JLabel("Error: " + message));
    }
}

/**
 * class representing calendar window
 * mediates connection between mycalendar and myplanner
 */
class CalendarWindow {
    private final MyCalendar calendar;
    private final MyPlanner planner;

    CalendarWindow(boolean localServer) {
        calendar = new MyCalendar(this);
        planner = new MyPlanner(calendar.getPanelForPlans(), localServer);
    }

    /**
     * create new panel which contains calendar
     * @return jpanel containing instance of calendar
     */
    JPanel setAndGetPanel() {
        JPanel window = new JPanel();
        window.setLayout(new BorderLayout());
        window.add(new JScrollPane(calendar), BorderLayout.CENTER);
        return window;
    }

    /**
     * show plans which are set to given day in window set in constructor
     * @param y year of given date
     * @param m month of given date
     * @param d day of given date
     */
    public void getAndShowPlans(int y, int m, int d) {
        planner.getAndShowPlans(y, m, d);
    }
}