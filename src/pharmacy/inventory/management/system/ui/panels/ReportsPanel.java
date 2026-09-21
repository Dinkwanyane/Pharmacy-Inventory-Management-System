/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pharmacy.inventory.management.system.ui.panels;

import pharmacy.inventory.management.system.dao.MedicineDAO;
import pharmacy.inventory.management.system.dao.SaleDAO;
import pharmacy.inventory.management.system.model.Medicine;
import pharmacy.inventory.management.system.model.Sale;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

/**
 * Admin reporting: Sales, Item-Wise, Low-Stock and Expiry reports.
 * 
 * @author Dinkwanyane
 */
public class ReportsPanel extends JPanel {

    private final SaleDAO saleDAO = new SaleDAO();
    private final MedicineDAO medicineDAO = new MedicineDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JLabel lblSummary = new JLabel(" ");

    public ReportsPanel() {
        setLayout(new BorderLayout(10, 10));

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnSales = new JButton("Sales Report (Last 30 Days)");
        JButton btnItemWise = new JButton("Item-Wise Sales Report");
        JButton btnLowStock = new JButton("Low Stock Report");
        JButton btnExpiry = new JButton("Expiry Report (Next 30 Days)");

        btnSales.addActionListener(e -> showSalesReport());
        btnItemWise.addActionListener(e -> showItemWiseReport());
        btnLowStock.addActionListener(e -> showLowStockReport());
        btnExpiry.addActionListener(e -> showExpiryReport());

        buttonBar.add(btnSales);
        buttonBar.add(btnItemWise);
        buttonBar.add(btnLowStock);
        buttonBar.add(btnExpiry);

        add(buttonBar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(lblSummary, BorderLayout.SOUTH);

        showSalesReport();
    }

    private void setColumns(String... cols) {
        tableModel.setDataVector(new Object[0][cols.length], cols);
    }

    private void showSalesReport() {
        setColumns("Sale ID", "Date", "Cashier User ID", "Total Amount");
        Date from = Date.valueOf(LocalDate.now().minusDays(30));
        Date to = Date.valueOf(LocalDate.now());
        List<Sale> sales = saleDAO.getSalesBetween(from, to);
        for (Sale s : sales) {
            tableModel.addRow(new Object[]{s.getSaleId(), s.getSaleDate(), s.getUserId(), s.getTotalAmount()});
        }
        BigDecimal total = saleDAO.getTotalRevenue(from, to);
        lblSummary.setText("  Total revenue (last 30 days): R " + total.setScale(2, RoundingMode.HALF_UP) +
                "  |  Transactions: " + sales.size());
    }

    private void showItemWiseReport() {
        setColumns("Medicine", "Total Qty Sold", "Total Revenue");
        List<Object[]> rows = saleDAO.getItemWiseReport();
        for (Object[] row : rows) tableModel.addRow(row);
        lblSummary.setText("  Item-wise sales, all-time, ordered by revenue.");
    }

    private void showLowStockReport() {
        setColumns("Medicine", "Company", "In Stock", "Reorder Level", "Supplier");
        List<Medicine> list = medicineDAO.getLowStock();
        for (Medicine m : list) {
            tableModel.addRow(new Object[]{m.getName(), m.getCompany(), m.getQuantityInStock(),
                    m.getReorderLevel(), m.getSupplierName()});
        }
        lblSummary.setText("  " + list.size() + " medicine(s) at or below reorder level.");
    }

    private void showExpiryReport() {
        setColumns("Medicine", "Company", "Expiry Date", "In Stock", "Supplier");
        List<Medicine> list = medicineDAO.getExpiringWithinDays(30);
        for (Medicine m : list) {
            tableModel.addRow(new Object[]{m.getName(), m.getCompany(), m.getExpiryDate(),
                    m.getQuantityInStock(), m.getSupplierName()});
        }
        lblSummary.setText("  " + list.size() + " medicine(s) expiring within 30 days.");
    }
}