/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pharmacy.inventory.management.system.ui;

import pharmacy.inventory.management.system.ui.panels.POSPanel;
import pharmacy.inventory.management.system.ui.panels.StockCheckPanel;
import pharmacy.inventory.management.system.util.Session;

import javax.swing.*;
import java.awt.*;

/**
 * Cashier dashboard: Point-of-Sale tab and Stock Check tab.
 * 
 * @author Dinkwanyane
 */
public class CashierDashboard extends JFrame {

    public CashierDashboard() {
        setTitle("HealthFirst PIMS - Cashier (" + Session.getCurrentUser().getFullName() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Point of Sale", new POSPanel());
        tabs.addTab("Stock Check", new StockCheckPanel());

        JPanel topBar = new JPanel(new BorderLayout());
        JLabel welcome = new JLabel("  Welcome, " + Session.getCurrentUser().getFullName() + " (Cashier)");
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