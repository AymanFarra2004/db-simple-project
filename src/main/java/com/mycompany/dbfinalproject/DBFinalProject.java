package com.mycompany.dbfinalproject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Vector;

public class DBFinalProject extends JFrame {

    private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521:XE?sessionTimezone=UTC";
    private static final String DB_USER = "system";
    private static final String DB_PASSWORD = "system";

    private JTextField studentIdField, studentNameField, studentEmailField;
    private JButton insertButton, updateButton, deleteButton, searchButton, loadAllButton;
    private JTextArea resultArea;
    private JTable studentTable;
    private DefaultTableModel tableModel;

    public DBFinalProject() {
        setTitle("Oracle Student Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Student Details"));

        studentIdField = new JTextField(10);
        studentNameField = new JTextField(20);
        studentEmailField = new JTextField(20);

        inputPanel.add(new JLabel("Student ID (for Update/Delete/Search):"));
        inputPanel.add(studentIdField);
        inputPanel.add(new JLabel("Student Name (for Insert/Update):"));
        inputPanel.add(studentNameField);
        inputPanel.add(new JLabel("Student Email (for Insert/Update):"));
        inputPanel.add(studentEmailField);

        topPanel.add(inputPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        insertButton = new JButton("Insert Student");
        updateButton = new JButton("Update Email");
        deleteButton = new JButton("Delete Student");
        searchButton = new JButton("Search Student (by ID)");
        loadAllButton = new JButton("Load All Students");

        buttonPanel.add(insertButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(loadAllButton);

        topPanel.add(buttonPanel, BorderLayout.CENTER);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        resultArea = new JTextArea(5, 40);
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        resultScrollPane.setBorder(BorderFactory.createTitledBorder("Messages / Search Result"));

        mainPanel.add(resultScrollPane, BorderLayout.CENTER);

        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Email"}, 0);
        studentTable = new JTable(tableModel);
        studentTable.setFillsViewportHeight(true);
        JScrollPane tableScrollPane = new JScrollPane(studentTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("All Students Data"));

        mainPanel.add(tableScrollPane, BorderLayout.SOUTH);

        add(mainPanel);

        insertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                insertStudent();
            }
        });

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateStudentEmail();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteStudent();
            }
        });

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchStudent();
            }
        });

        loadAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadAllStudents();
            }
        });
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private void insertStudent() {
        String name = studentNameField.getText();
        String email = studentEmailField.getText();

        if (name.isEmpty() || email.isEmpty()) {
            resultArea.setText("Error: Student Name and Email cannot be empty for insertion.");
            return;
        }

        try (Connection conn = getConnection();
             CallableStatement cstmt = conn.prepareCall("{call Insert_To_Student(?, ?)}")) {

            cstmt.setString(1, name);
            cstmt.setString(2, email);
            cstmt.execute();

            resultArea.setText("Student '" + name + "' inserted successfully!");
            clearInputFields();
            loadAllStudents();
        } catch (SQLException ex) {
            resultArea.setText("Error inserting student: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void updateStudentEmail() {
        String name = studentNameField.getText();
        String newEmail = studentEmailField.getText();

        if (name.isEmpty() || newEmail.isEmpty()) {
            resultArea.setText("Error: Student Name and New Email cannot be empty for update.");
            return;
        }

        try (Connection conn = getConnection();
             CallableStatement cstmt = conn.prepareCall("{call Update_Email_By_Name(?, ?)}")) {

            cstmt.setString(1, name);
            cstmt.setString(2, newEmail);
            cstmt.execute();

            resultArea.setText("Email for student '" + name + "' updated successfully!");
            clearInputFields();
            loadAllStudents();
        } catch (SQLException ex) {
            resultArea.setText("Error updating student email: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void deleteStudent() {
        String idText = studentIdField.getText();
        if (idText.isEmpty()) {
            resultArea.setText("Error: Student ID cannot be empty for deletion.");
            return;
        }

        try {
            int studentId = Integer.parseInt(idText);
            try (Connection conn = getConnection();
                 CallableStatement cstmt = conn.prepareCall("{call Delete_Student_By_ID(?)}")) {

                cstmt.setInt(1, studentId);
                cstmt.execute();

                resultArea.setText("Student with ID " + studentId + " deleted successfully!");
                clearInputFields();
                loadAllStudents();
            }
        } catch (NumberFormatException ex) {
            resultArea.setText("Error: Invalid Student ID. Please enter a number.");
        } catch (SQLException ex) {
            resultArea.setText("Error deleting student: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void searchStudent() {
        String idText = studentIdField.getText();
        if (idText.isEmpty()) {
            resultArea.setText("Error: Student ID cannot be empty for search.");
            return;
        }

        try {
            int studentId = Integer.parseInt(idText);
            try (Connection conn = getConnection();
                 CallableStatement cstmt = conn.prepareCall("{? = call getAllData(?)}")) {

                cstmt.registerOutParameter(1, Types.VARCHAR);
                cstmt.setInt(2, studentId);
                cstmt.execute();

                String result = cstmt.getString(1);
                resultArea.setText("Search Result for ID " + studentId + ":\n" + result);
            }
        } catch (NumberFormatException ex) {
            resultArea.setText("Error: Invalid Student ID. Please enter a number.");
        } catch (SQLException ex) {
            resultArea.setText("Error searching student: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void loadAllStudents() {
        tableModel.setRowCount(0);

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT STUDENT_ID, NAME, EMAIL FROM STUDENT ORDER BY STUDENT_ID")) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            Vector<String> columnNames = new Vector<>();
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnName(i));
            }
            tableModel.setColumnIdentifiers(columnNames);

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getObject(i));
                }
                tableModel.addRow(row);
            }
            resultArea.setText("All students loaded successfully.");
        } catch (SQLException ex) {
            resultArea.setText("Error loading all students: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void clearInputFields() {
        studentIdField.setText("");
        studentNameField.setText("");
        studentEmailField.setText("");
    }

    public static void main(String[] args) {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("Oracle JDBC Driver not found. Make sure ojdbcX.jar is in your classpath.");
            e.printStackTrace();
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new DBFinalProject().setVisible(true);
            }
        });
    }
}