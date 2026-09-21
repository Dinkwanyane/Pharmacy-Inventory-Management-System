/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pharmacy.inventory.management.system.ui.panels;

import pharmacy.inventory.management.system.dao.MedicineDAO;
import pharmacy.inventory.management.system.model.Medicine;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Read-only stock lookup for cashiers - no add/edit/delete permitted.
 * 
 * @author Dinkwanyane
 */
public class StockCheckPanel extends JPanel {

    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final JTextField txtSearch = new JTextField(20);
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Name", "Company", "Type", "Price", "In Stock", "Expiry"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    public StockCheckPanel() {
        setLayout(new BorderLayout(10, 10));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchBar.add(new JLabel("Search medicine:"));
        searchBar.add(txtSearch);
        JButton btnSearch = new JButton("Search");
        JButton btnShowAll = new JButton("Show All");
        btnSearch.addActionListener(e -> search());
        btnShowAll.addActionListener(e -> loadAll());
        searchBar.add(btnSearch);
        searchBar.add(btnShowAll);

        add(searchBar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadAll();
    }

    private void loadAll() {
        populate(medicineDAO.getAllMedicines());
    }

    private void search() {
        String keyword = txtSearch.getText().trim();
        populate(keyword.isEmpty() ? medicineDAO.getAllMedicines() : medicineDAO.searchByName(keyword));
    }

    private void populate(List<Medicine> medicines) {
        tableModel.setRowCount(0);
        for (Medicine m : medicines) {
            tableModel.addRow(new Object[]{m.getName(), m.getCompany(), m.getMedicineType(),
                    m.getPrice(), m.getQuantityInStock(), m.getExpiryDate()});
        }
    }
}