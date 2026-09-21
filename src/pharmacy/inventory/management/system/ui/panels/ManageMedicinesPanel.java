package pharmacy.inventory.management.system.ui.panels;

import pharmacy.inventory.management.system.dao.MedicineDAO;
import pharmacy.inventory.management.system.dao.SupplierDAO;
import pharmacy.inventory.management.system.model.Medicine;
import pharmacy.inventory.management.system.model.Supplier;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;

/**
 *
 * @author Dinkwanyane
 */

/** Full CRUD panel for the medicines table. */
public class ManageMedicinesPanel extends JPanel {

    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Company", "Type", "Price", "Stock", "Reorder Lvl", "Expiry", "Supplier"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    private final JTextField txtName = new JTextField(15);
    private final JTextField txtCompany = new JTextField(15);
    private final JTextField txtType = new JTextField(10);
    private final JTextField txtPrice = new JTextField(8);
    private final JTextField txtStock = new JTextField(6);
    private final JTextField txtReorder = new JTextField(6);
    private final JTextField txtExpiry = new JTextField(10); // yyyy-MM-dd
    private final JComboBox<Supplier> cmbSupplier = new JComboBox<>();

    private Integer selectedMedicineId = null;

    public ManageMedicinesPanel() {
        setLayout(new BorderLayout(10, 10));
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildFormPanel(), BorderLayout.SOUTH);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedRowIntoForm();
        });
        refreshSuppliers();
        refreshTable();
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Medicine Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addField(form, gbc, 0, 0, "Name:", txtName);
        addField(form, gbc, 2, 0, "Company:", txtCompany);
        addField(form, gbc, 0, 1, "Type:", txtType);
        addField(form, gbc, 2, 1, "Price:", txtPrice);
        addField(form, gbc, 0, 2, "Stock Qty:", txtStock);
        addField(form, gbc, 2, 2, "Reorder Lvl:", txtReorder);
        addField(form, gbc, 0, 3, "Expiry (yyyy-MM-dd):", txtExpiry);
        addField(form, gbc, 2, 3, "Supplier:", cmbSupplier);

        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");

        btnAdd.addActionListener(e -> addMedicine());
        btnUpdate.addActionListener(e -> updateMedicine());
        btnDelete.addActionListener(e -> deleteMedicine());
        btnClear.addActionListener(e -> clearForm());

        JPanel buttons = new JPanel();
        buttons.add(btnAdd); buttons.add(btnUpdate); buttons.add(btnDelete); buttons.add(btnClear);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 4;
        form.add(buttons, gbc);

        return form;
    }

    private void addField(JPanel form, GridBagConstraints gbc, int x, int y, String label, JComponent field) {
        gbc.gridx = x; gbc.gridy = y; gbc.gridwidth = 1;
        form.add(new JLabel(label), gbc);
        gbc.gridx = x + 1;
        form.add(field, gbc);
    }

    private void refreshSuppliers() {
        cmbSupplier.removeAllItems();
        for (Supplier s : supplierDAO.getAllSuppliers()) cmbSupplier.addItem(s);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Medicine m : medicineDAO.getAllMedicines()) {
            tableModel.addRow(new Object[]{
                    m.getMedicineId(), m.getName(), m.getCompany(), m.getMedicineType(),
                    m.getPrice(), m.getQuantityInStock(), m.getReorderLevel(),
                    m.getExpiryDate(), m.getSupplierName()
            });
        }
    }

    private void loadSelectedRowIntoForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedMedicineId = (Integer) tableModel.getValueAt(row, 0);
        txtName.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        txtCompany.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        txtType.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        txtPrice.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        txtStock.setText(String.valueOf(tableModel.getValueAt(row, 5)));
        txtReorder.setText(String.valueOf(tableModel.getValueAt(row, 6)));
        txtExpiry.setText(String.valueOf(tableModel.getValueAt(row, 7)));
        String supplierName = String.valueOf(tableModel.getValueAt(row, 8));
        for (int i = 0; i < cmbSupplier.getItemCount(); i++) {
            if (cmbSupplier.getItemAt(i).getName().equals(supplierName)) {
                cmbSupplier.setSelectedIndex(i);
                break;
            }
        }
    }

    private Medicine readForm() {
        Medicine m = new Medicine();
        if (selectedMedicineId != null) m.setMedicineId(selectedMedicineId);
        m.setName(txtName.getText().trim());
        m.setCompany(txtCompany.getText().trim());
        m.setMedicineType(txtType.getText().trim());
        m.setPrice(new BigDecimal(txtPrice.getText().trim()));
        m.setQuantityInStock(Integer.parseInt(txtStock.getText().trim()));
        m.setReorderLevel(Integer.parseInt(txtReorder.getText().trim()));
        m.setExpiryDate(Date.valueOf(txtExpiry.getText().trim()));
        Supplier s = (Supplier) cmbSupplier.getSelectedItem();
        if (s != null) m.setSupplierId(s.getSupplierId());
        return m;
    }

    private void addMedicine() {
        try {
            if (medicineDAO.addMedicine(readForm())) {
                refreshTable();
                clearForm();
            } else {
                showError("Could not add medicine.");
            }
        } catch (Exception ex) {
            showError("Please check the values entered: " + ex.getMessage());
        }
    }

    private void updateMedicine() {
        if (selectedMedicineId == null) {
            showError("Select a medicine from the table first.");
            return;
        }
        try {
            if (medicineDAO.updateMedicine(readForm())) {
                refreshTable();
                clearForm();
            } else {
                showError("Could not update medicine.");
            }
        } catch (Exception ex) {
            showError("Please check the values entered: " + ex.getMessage());
        }
    }

    private void deleteMedicine() {
        if (selectedMedicineId == null) {
            showError("Select a medicine from the table first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected medicine?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            medicineDAO.deleteMedicine(selectedMedicineId);
            refreshTable();
            clearForm();
        }
    }

    private void clearForm() {
        selectedMedicineId = null;
        txtName.setText(""); txtCompany.setText(""); txtType.setText("");
        txtPrice.setText(""); txtStock.setText(""); txtReorder.setText(""); txtExpiry.setText("");
        table.clearSelection();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}