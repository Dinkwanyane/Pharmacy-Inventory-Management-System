/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pharmacy.inventory.management.system.ui;

import pharmacy.inventory.management.system.util.Session;

import javax.swing.*;
import java.awt.*;

/** Administrator dashboard: tabs for Medicines, Suppliers, Users and Reports. */
/**
 *
 * @author Dinkwanyane
 */
public class AdminDashboard extends JFrame {

    public AdminDashboard() {
        setTitle("HealthFirst PIMS - Admin Dashboard (" + Session.getCurrentUser().getFullName() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 620);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Manage Medicines", new JPanel()); // Replace with panel class when created
        tabs.addTab("Manage Suppliers", new JPanel()); // Replace with panel class when created
        tabs.addTab("Manage Users", new JPanel());     // Replace with panel class when created
        tabs.addTab("Reports", new JPanel());          // Replace with panel class when created

        JPanel topBar = new JPanel(new BorderLayout());
        JLabel welcome = new JLabel("  Welcome, " + Session.getCurrentUser().getFullName() + " (Administrator)");
        welcome.setFont(new Font("SansSerif", Font.BOLD, 13));
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> logout());
        topBar.add(welcome, BorderLayout.WEST);
        topBar.add(btnLogout, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(topBar, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    private void logout() {
        Session.clear();
        dispose();
        new LoginFrame().setVisible(true);
    }
}