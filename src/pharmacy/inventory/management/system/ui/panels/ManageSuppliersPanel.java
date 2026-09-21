/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package pharmacy.inventory.management.system.ui.panels;

import pharmacy.inventory.management.system.dao.SupplierDAO;
import pharmacy.inventory.management.system.model.Supplier;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
/**
 *
 * @author Dinkwanyane
 */

/** CRUD panel for suppliers. */
public class ManageSuppliersPanel extends JPanel {

    private final SupplierDAO supplierDAO = new SupplierDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Contact Person", "Phone", "Email", "Address"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    private final JTextField txtName = new JTextField(16);
    private final JTextField txtContact = new JTextField(16);
    private final JTextField txtPhone = new JTextField(12);
    private final JTextField txtEmail = new JTextField(16);
    private final JTextField txtAddress = new JTextField(20);

    private Integer selectedId = null;

    public ManageSuppliersPanel() {
        setLayout(new BorderLayout(10, 10));
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.SOUTH);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelected();
        });
        refreshTable();
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Supplier Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addField(form, gbc, 0, 0, "Name:", txtName);
        addField(form, gbc, 2, 0, "Contact Person:", txtContact);
        addField(form, gbc, 0, 1, "Phone:", txtPhone);
        addField(form, gbc, 2, 1, "Email:", txtEmail);
        addField(form, gbc, 0, 2, "Address:", txtAddress);

        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");
        btnAdd.addActionListener(e -> addSupplier());
        btnUpdate.addActionListener(e -> updateSupplier());
        btnDelete.addActionListener(e -> deleteSupplier());
        btnClear.addActionListener(e -> clearForm());

        JPanel buttons = new JPanel();
        buttons.add(btnAdd); buttons.add(btnUpdate); buttons.add(btnDelete); buttons.add(btnClear);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        form.add(buttons, gbc);
        return form;
    }

    private void addField(JPanel form, GridBagConstraints gbc, int x, int y, String label, JComponent field) {
        gbc.gridx = x; gbc.gridy = y; gbc.gridwidth = 1;
        form.add(new JLabel(label), gbc);
        gbc.gridx = x + 1;
        form.add(field, gbc);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Supplier s : supplierDAO.getAllSuppliers()) {
            tableModel.addRow(new Object[]{s.getSupplierId(), s.getName(), s.getContactPerson(),
                    s.getPhone(), s.getEmail(), s.getAddress()});
        }
    }

    private void loadSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedId = (Integer) tableModel.getValueAt(row, 0);
        txtName.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        txtContact.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        txtPhone.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        txtEmail.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        txtAddress.setText(String.valueOf(tableModel.getValueAt(row, 5)));
    }

    private Supplier readForm() {
        Supplier s = new Supplier();
        if (selectedId != null) s.setSupplierId(selectedId);
        s.setName(txtName.getText().trim());
        s.setContactPerson(txtContact.getText().trim());
        s.setPhone(txtPhone.getText().trim());
        s.setEmail(txtEmail.getText().trim());
        s.setAddress(txtAddress.getText().trim());
        return s;
    }

    private void addSupplier() {
        if (supplierDAO.addSupplier(readForm())) { refreshTable(); clearForm(); }
        else showError("Could not add supplier.");
    }

    private void updateSupplier() {
        if (selectedId == null) { showError("Select a supplier first."); return; }
        if (supplierDAO.updateSupplier(readForm())) { refreshTable(); clearForm(); }
        else showError("Could not update supplier.");
    }

    private void deleteSupplier() {
        if (selectedId == null) { showError("Select a supplier first."); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected supplier?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            supplierDAO.deleteSupplier(selectedId);
            refreshTable(); clearForm();
        }
    }

    private void clearForm() {
        selectedId = null;
        txtName.setText(""); txtContact.setText(""); txtPhone.setText("");
        txtEmail.setText(""); txtAddress.setText("");
        table.clearSelection();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
