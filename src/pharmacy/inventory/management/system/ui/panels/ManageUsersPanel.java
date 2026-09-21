/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pharmacy.inventory.management.system.ui.panels;

import pharmacy.inventory.management.system.dao.UserDAO;
import pharmacy.inventory.management.system.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 *
 * @author Dinkwanyane
 */
/** Admin panel to create, delete and manage Cashier accounts. */
public class ManageUsersPanel extends JPanel {

    private final UserDAO userDAO = new UserDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Username", "Full Name", "Role"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    private final JTextField txtUsername = new JTextField(14);
    private final JTextField txtFullName = new JTextField(16);
    private final JPasswordField txtPassword = new JPasswordField(14);

    public ManageUsersPanel() {
        setLayout(new BorderLayout(10, 10));
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.SOUTH);
        refreshTable();
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("New Cashier Account"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; form.add(txtUsername, gbc);
        gbc.gridx = 2; form.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 3; form.add(txtFullName, gbc);

        gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; form.add(txtPassword, gbc);

        JButton btnAdd = new JButton("Add Cashier");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnReset = new JButton("Reset Password");
        btnAdd.addActionListener(e -> addCashier());
        btnDelete.addActionListener(e -> deleteSelected());
        btnReset.addActionListener(e -> resetPassword());

        JPanel buttons = new JPanel();
        buttons.add(btnAdd); buttons.add(btnDelete); buttons.add(btnReset);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        form.add(buttons, gbc);

        return form;
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (User u : userDAO.getAllCashiers()) {
            tableModel.addRow(new Object[]{u.getUserId(), u.getUsername(), u.getFullName(), u.getRole()});
        }
    }

    private void addCashier() {
        String username = txtUsername.getText().trim();
        String fullName = txtFullName.getText().trim();
        String password = new String(txtPassword.getPassword());
        if (username.isEmpty() || fullName.isEmpty() || password.isEmpty()) {
            showError("All fields are required.");
            return;
        }
        User u = new User();
        u.setUsername(username);
        u.setFullName(fullName);
        u.setRole("Cashier");
        if (userDAO.addUser(u, password)) {
            refreshTable();
            txtUsername.setText(""); txtFullName.setText(""); txtPassword.setText("");
        } else {
            showError("Could not add cashier (username may already exist).");
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { showError("Select a cashier row first."); return; }
        int id = (Integer) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this user account?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            userDAO.deleteUser(id);
            refreshTable();
        }
    }

    private void resetPassword() {
        int row = table.getSelectedRow();
        if (row < 0) { showError("Select a cashier row first."); return; }
        int id = (Integer) tableModel.getValueAt(row, 0);
        String newPass = JOptionPane.showInputDialog(this, "Enter new password:");
        if (newPass != null && !newPass.trim().isEmpty()) {
            userDAO.resetPassword(id, newPass.trim());
            JOptionPane.showMessageDialog(this, "Password updated.");
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
