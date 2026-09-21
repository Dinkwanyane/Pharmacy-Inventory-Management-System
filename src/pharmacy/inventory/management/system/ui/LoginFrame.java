/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pharmacy.inventory.management.system.ui;

import javax.swing.*;
import java.awt.*;
import pharmacy.inventory.management.system.dao.UserDAO;
import pharmacy.inventory.management.system.model.User;
import pharmacy.inventory.management.system.util.Session;

/**
 *
 * @author Dinkwanyane
 */
/** Secure login screen; redirects to AdminDashboard or CashierDashboard based on role. */
public class LoginFrame extends JFrame{
    
    
    private final JTextField txtUsername = new JTextField(18);
    private final JPasswordField txtPassword = new JPasswordField(18);
    private final JLabel lblStatus = new JLabel(" ");
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        setTitle("HealthFirst PIMS - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(380, 260);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("HealthFirst Pharmacy", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(txtPassword, gbc);

        JButton btnLogin = new JButton("Login");
        btnLogin.addActionListener(e -> handleLogin());
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(btnLogin, gbc);

        lblStatus.setForeground(Color.RED);
        gbc.gridy = 4;
        panel.add(lblStatus, gbc);

        getRootPane().setDefaultButton(btnLogin);
        add(panel);
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblStatus.setText("Please enter username and password.");
            return;
        }

        User user = userDAO.authenticate(username, password);
        if (user == null) {
            lblStatus.setText("Invalid username or password.");
            txtPassword.setText("");
            return;
        }

        Session.setCurrentUser(user);
        dispose();

        if (user.isAdmin()) {
            new AdminDashboard().setVisible(true);
        } else {
            new CashierDashboard().setVisible(true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
    
    
}
