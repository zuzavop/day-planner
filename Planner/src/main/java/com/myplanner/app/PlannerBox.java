package com.myplanner.app;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Displaying given plan
 * allow editing and deleting of plan (from panel and send request to class connected to database)
 */
public class PlannerBox extends JPanel implements ActionListener {
    private final JButton buttonEdit = new JButton("Edit");
    private final JButton buttonSave = new JButton("Save");
    private final JButton buttonDelete = new JButton("Delete");
    private final JLabel labelName, labelDate, labelTime;
    private final JTextField fieldName, fieldDate, fieldTime;
    private final Connection connection; // connection to database
    private final JPanel display; //display where PlannerBox is in
    private final Plan plan; // info about displaying plan

    /**
     * set attribute of box which display plan
     *
     * @param p    display plan
     * @param conn connection to database
     * @param dis  panel where plannerbox is in
     */
    public PlannerBox(@NotNull Plan plan, @NotNull Connection connection, JPanel display) {
        this.connection = connection;
        this.plan = plan;
        this.display = display;

        // set data
        JLabel label1 = new JLabel("Name: ");
        JLabel label2 = new JLabel("Date: ");
        JLabel label3 = new JLabel("Time: ");
        labelName = new JLabel(plan.getName());
        labelDate = new JLabel(plan.getDate());
        labelTime = new JLabel(plan.getTimeText());
        fieldName = new JTextField(16);
        fieldDate = new JTextField(16);
        fieldTime = new JTextField(16);

        initializeComponents();
        setupLayout();
    }

    /**
     * Initializes the components and sets up the button actions.
     */
    private void initializeComponents() {
        buttonEdit.addActionListener(this);
        buttonSave.addActionListener(this);
        buttonDelete.addActionListener(this);

        buttonSave.setVisible(false);
        buttonDelete.setVisible(false);
        fieldName.setVisible(false);
        fieldDate.setVisible(false);
        fieldTime.setVisible(false);
    }

    /**
     * Sets up the layout of the PlannerBox.
     */
    private void setupLayout() {
        setBorder(BorderFactory.createTitledBorder(""));
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);        

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addComponent(label1)
                        .addComponent(label2)
                        .addComponent(label3))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(labelName)
                        .addComponent(labelDate)
                        .addComponent(labelTime))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(fieldName)
                        .addComponent(fieldDate)
                        .addComponent(fieldTime))
                .addComponent(buttonEdit)
                .addComponent(buttonSave)
                .addComponent(buttonDelete)
        );

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(label1)
                        .addComponent(labelName)
                        .addComponent(fieldName))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(label2)
                        .addComponent(labelDate)
                        .addComponent(fieldDate))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(label3)
                        .addComponent(labelTime)
                        .addComponent(fieldTime))
                .addComponent(buttonEdit)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(buttonSave)
                        .addComponent(buttonDelete))
        );

        setAlignmentX(LEFT_ALIGNMENT);
    }

    /**
     * button press processing - update jpanel and data in it
     */
    @Override
    public void actionPerformed(@NotNull ActionEvent e) {
        boolean show = false;
        
        if (e.getSource() == buttonEdit) {
            // set values to text field
            fieldName.setText(labelName.getText());
            fieldDate.setText(labelDate.getText());
            fieldTime.setText(plan.getTime());
        } else if (e.getSource() == buttonSave) {
            try {
                if (fieldName.getText().isEmpty()) {
                    // set warning - name is empty (must be given)
                    fieldName.setBackground(Color.RED);
                } else {
                    fieldName.setBackground(Color.WHITE);
                }

                LocalDate date = AddingNew.getDate(fieldDate.getText()); // parse to valid date
                fieldDate.setBackground(Color.WHITE);
                fieldTime.setBackground(Color.WHITE);

                if (plan.setValuesIfValid(fieldName.getText(), fieldTime.getText(), date.getYear(), date.getMonthValue(), date.getDayOfMonth())) {
                    // save changed values
                    labelName.setText(fieldName.getText());
                    labelDate.setText(date.format(DateTimeFormatter.ofPattern("d.M.yyyy")));
                    labelTime.setText(plan.getTimeText());
                    show = true;

                    // save changes in database
                    try {
                        connection.updatePlan(plan);
                    } catch (ConnectionException ex) {
                        Planner.showWarning(display, ex.getMessage());
                    }
                } else {
                    fieldTime.setBackground(Color.RED);
                    return;
                }
            } catch (DateTimeException ex) {
                // show warning
                fieldDate.setBackground(Color.RED);
            }

        } else if (e.getSource() == buttonDelete) {
            removeAll(); //remove box from window
            try {
                connection.deletePlan(plan.getId()); // delete plan from database
            } catch (ConnectionException ex) {
                Planner.showWarning(display, ex.getMessage());
            }
        }

        // update visibility of components
        labelName.setVisible(show);
        labelDate.setVisible(show);
        labelTime.setVisible(show);
        buttonEdit.setVisible(show);
        buttonDelete.setVisible(!show);
        buttonSave.setVisible(!show);
        fieldName.setVisible(!show);
        fieldDate.setVisible(!show);
        fieldTime.setVisible(!show);

        // update jpanel
        this.revalidate();
        this.repaint();
    }
}
