package com.myplanner.app;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * manage calendar - display month, provide changing showing month and showing plan for selected day
 */
public class MyCalendar extends JPanel implements ActionListener {
    private JTable table;
    private final SimpleDateFormat day = new SimpleDateFormat("d");
    private final SimpleDateFormat month = new SimpleDateFormat("M");
    private final SimpleDateFormat yearText = new SimpleDateFormat("yyyy");
    private final SimpleDateFormat monthText = new SimpleDateFormat("MMMMMMMMM");
    private final JButton previous = new JButton("Previous");
    private final JButton next = new JButton("Next");
    private final Calendar current = Calendar.getInstance(); // represent current month which is display
    private final JLabel m, y;
    private JPanel display;

    /**
     * set panel with buttons, labels and calendar
     * @param window class that connect calendar with MyPlanner class (for showing plans)
     */
    MyCalendar(CalendarWindow window) {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {
                Font font = new Font("Dialog", java.awt.Font.PLAIN, (Math.min(getWidth() / 45, 25)));
                for (Component item : getComponents()){
                    item.setFont(font);
                }
                table.setRowHeight(getHeight()/15);
                for (int i = 0; i < table.getColumnModel().getColumnCount(); i++)
                    table.getColumnModel().getColumn(i).setPreferredWidth(getWidth() /20);
                table.getParent().setPreferredSize(new Dimension(getWidth()/2, table.getRowHeight()*table.getRowCount()));
                revalidate();
                repaint();
            }
        });

        // set buttons and labels
        previous.addActionListener(this);
        next.addActionListener(this);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        add(previous, c);

        m = new JLabel(monthText.format(current.getTime())); // set name of selected month
        c.gridx = 2;
        c.gridy = 0;
        c.insets = new Insets(0, 10, 0, 0);
        add(m, c);

        y = new JLabel(yearText.format(current.getTime()));
        c.gridx = 4;
        c.gridy = 0;
        c.insets = new Insets(0, 10, 0, 0);
        add(y, c);

        c.gridx = 6;
        c.gridy = 0;
        c.insets = new Insets(0, 10, 0, 0);
        add(next, c);

        setCalendar(c, window);
    }

    /**
     * @return return panel in which plans will be displayed
     */
    public JPanel getPanelForPlans() {
        return display;
    }

    /**
     * set calendar - display days of selected month
     * set display to showing plans of selected day from calendar
     * @param c grid
     * @param window class that connect calendar with MyPlanner class for showing plans
     */
    private void setCalendar(@NotNull GridBagConstraints c, CalendarWindow window) {
        display = new JPanel(); // panel where data is displayed
        c.gridwidth = 10;
        c.gridheight = 2;
        c.insets = new Insets(10, 10, 0, 0);
        c.gridx = 10;
        c.gridy = 0;
        add(display, c);

        String[][] data = new String[6][7];
        current.set(Calendar.DATE, 1);
        Calendar cal = (Calendar) current.clone();
        cal.add(Calendar.DATE, -cal.get(Calendar.DAY_OF_WEEK) + 1);
        for (int week = 0; week < 6; week++) {
            for (int d = 0; d < 7; d++) {
                data[week][d] = (day.format(cal.getTime())
                        + (month.format(cal.getTime()).equals(month.format(current.getTime())) ? "1" : "0"));
                cal.add(Calendar.DATE, +1);
            }
        }

        String[] header = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

        DefaultTableModel dm = new DefaultTableModel(data, header);

        table = new JTable(dm);
        table.getTableHeader().setReorderingAllowed(false);
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        table.setFillsViewportHeight(true);
        for (String title : header) {
            table.getColumn(title).setCellRenderer(new ButtonRenderer());
            table.getColumn(title).setCellEditor(new ButtonEditor(new JCheckBox(), window, current));
        }

        table.setBounds(30, 40, 150, 400);
        c.gridwidth = 10;
        c.insets = new Insets(10, 0, 0, 0);
        c.gridx = 0;
        c.gridy = 1;
        add(new JScrollPane(table), c);
    }

    /**
     * update calendar and labels to current month after change
     */
    private void updateCalendar() {
        m.setText(monthText.format(current.getTime()));
        y.setText(yearText.format(current.getTime()));

        current.set(Calendar.DATE, 1);
        Calendar cal = (Calendar) current.clone();
        cal.add(Calendar.DATE, -cal.get(Calendar.DAY_OF_WEEK) + 1);
        for (int week = 0; week < 6; week++) {
            for (int d = 0; d < 7; d++) {
                table.getModel().setValueAt(day.format(cal.getTime())
                        + (month.format(cal.getTime()).equals(month.format(current.getTime())) ? "1" : "0"), week, d);
                cal.add(Calendar.DATE, +1);
            }
        }
    }

    /**
     * set month on button click and update calendar
     */
    @Override
    public void actionPerformed(@NotNull ActionEvent e) {
        if (e.getSource() == previous) {
            current.add(Calendar.MONTH, -1);
        } else if (e.getSource() == next) {
            current.add(Calendar.MONTH, +1);
        }
        updateCalendar();
    }
}

/**
 * render of button in table
 * setting of label and visibility of button
 */
class ButtonRenderer extends JButton implements TableCellRenderer {

    public ButtonRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        String str = (value == null) ? "" : value.toString();
        setEnabled(!str.endsWith("0")); // set enable when in different month
        assert value != null;
        setText((value.equals("")) ? "" : str.substring(0, str.length() - 1)); // change label of button (hide number which define visibility)
        return this;
    }
}

/**
 * editor of button in table
 * managing clicks on button
 */
class ButtonEditor extends DefaultCellEditor {
    protected JButton button;
    private final CalendarWindow window; // managing displaying plans to panel
    private String label;
    private boolean wasPushed;
    private final Calendar current; // contains current select month

    /**
     * setting atrributes of class
     *
     * @param checkBox default (class extends DefaultCellEditor)
     * @param w        class managing displaying plans
     * @param cur      calendar used for storing current month
     */
    public ButtonEditor(JCheckBox checkBox, CalendarWindow w, Calendar cur) {
        super(checkBox);
        window = w;
        current = cur;
        button = new JButton();
        button.setOpaque(true);
        button.addActionListener(e -> fireEditingStopped());
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        label = (value == null) ? "" : value.toString();
        button.setText(label.substring(0, label.length() - 1)); // hide last number which define visibility of button
        wasPushed = true;
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        if (wasPushed) {
            String l = label.substring(0, label.length() - 1);
            window.getAndShowPlans(current.get(Calendar.YEAR), current.get(Calendar.MONTH) + 1, Integer.parseInt(l));
        }
        wasPushed = false;
        return label;
    }

    @Override
    public boolean stopCellEditing() {
        wasPushed = false;
        return super.stopCellEditing();
    }
}
