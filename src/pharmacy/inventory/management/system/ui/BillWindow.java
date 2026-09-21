/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pharmacy.inventory.management.system.ui;

import pharmacy.inventory.management.system.model.Sale;
import pharmacy.inventory.management.system.model.SaleItem;
import pharmacy.inventory.management.system.util.Session;

import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
/**
 *
 * @author Dinkwanyane
 */

/** Displays a generated bill for a completed sale; supports save-to-text and print. */
public class BillWindow extends JFrame {

    private final Sale sale;
    private final JTextArea billArea = new JTextArea();

    public BillWindow(Sale sale) {
        this.sale = sale;
        setTitle("Bill - Sale #" + sale.getSaleId());
        setSize(420, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
    }

    private void buildUI() {
        billArea.setEditable(false);
        billArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        billArea.setText(generateBillText());

        JButton btnPrint = new JButton("Print");
        JButton btnSave = new JButton("Save to File");
        JButton btnClose = new JButton("Close");
        btnPrint.addActionListener(e -> printBill());
        btnSave.addActionListener(e -> saveBill());
        btnClose.addActionListener(e -> dispose());

        JPanel buttons = new JPanel();
        buttons.add(btnPrint); buttons.add(btnSave); buttons.add(btnClose);

        setLayout(new BorderLayout());
        add(new JScrollPane(billArea), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private String generateBillText() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("         HEALTHFIRST PHARMACY\n");
        sb.append("========================================\n");
        sb.append("Bill No : ").append(sale.getSaleId()).append("\n");
        sb.append("Cashier : ").append(Session.getCurrentUser().getFullName()).append("\n");
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-18s %5s %8s%n", "Item", "Qty", "Total"));
        sb.append("----------------------------------------\n");
        for (SaleItem item : sale.getItems()) {
            sb.append(String.format("%-18s %5d %8.2f%n",
                    truncate(item.getMedicineName(), 18), item.getQuantitySold(), item.getLineTotal()));
        }
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-18s %5s %8.2f%n", "TOTAL", "", sale.getTotalAmount()));
        sb.append("========================================\n");
        sb.append("     Thank you for shopping with us!\n");
        return sb.toString();
    }

    private String truncate(String s, int len) {
        if (s == null) return "";
        return s.length() <= len ? s : s.substring(0, len - 1) + ".";
    }

    private void printBill() {
        try {
            billArea.print();
        } catch (java.awt.print.PrinterException e) {
            JOptionPane.showMessageDialog(this, "Printing failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveBill() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("Bill_" + sale.getSaleId() + ".txt"));
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(chooser.getSelectedFile()))) {
                pw.print(billArea.getText());
                JOptionPane.showMessageDialog(this, "Bill saved successfully.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Save failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}