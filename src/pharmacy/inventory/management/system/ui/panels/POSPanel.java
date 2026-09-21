/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pharmacy.inventory.management.system.ui.panels;

import pharmacy.inventory.management.system.dao.MedicineDAO;
import pharmacy.inventory.management.system.dao.SaleDAO;
import pharmacy.inventory.management.system.model.Medicine;
import pharmacy.inventory.management.system.model.Sale;
import pharmacy.inventory.management.system.model.SaleItem;
import pharmacy.inventory.management.system.ui.BillWindow;
import pharmacy.inventory.management.system.util.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Point-of-Sale interface: search medicine, add to cart, checkout and bill.
 * 
 * @author Dinkwanyane
 */
public class POSPanel extends JPanel {

    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final SaleDAO saleDAO = new SaleDAO();

    private final JTextField txtSearch = new JTextField(20);
    private final DefaultTableModel searchModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Price", "In Stock"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable searchTable = new JTable(searchModel);

    private final DefaultTableModel cartModel = new DefaultTableModel(
            new String[]{"Medicine ID", "Name", "Unit Price", "Qty", "Line Total"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable cartTable = new JTable(cartModel);

    private final JLabel lblTotal = new JLabel("Total: R 0.00");
    private final List<Medicine> lastSearchResults = new ArrayList<>();
    private final List<SaleItem> cartItems = new ArrayList<>();

    public POSPanel() {
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new BorderLayout(5, 5));
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchBar.add(new JLabel("Search medicine:"));
        searchBar.add(txtSearch);
        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(e -> doSearch());
        searchBar.add(btnSearch);
        top.add(searchBar, BorderLayout.NORTH);
        top.add(new JScrollPane(searchTable), BorderLayout.CENTER);

        JButton btnAddToCart = new JButton("Add Selected to Cart »");
        btnAddToCart.addActionListener(e -> addSelectedToCart());
        top.add(btnAddToCart, BorderLayout.SOUTH);
        searchTable.setPreferredScrollableViewportSize(new Dimension(400, 150));

        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        bottom.setBorder(BorderFactory.createTitledBorder("Cart"));
        bottom.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel cartButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 14));
        JButton btnRemove = new JButton("Remove Item");
        JButton btnClear = new JButton("Clear Cart");
        JButton btnCheckout = new JButton("Checkout & Generate Bill");
        btnRemove.addActionListener(e -> removeSelectedFromCart());
        btnClear.addActionListener(e -> clearCart());
        btnCheckout.addActionListener(e -> checkout());
        cartButtons.add(lblTotal);
        cartButtons.add(btnRemove);
        cartButtons.add(btnClear);
        cartButtons.add(btnCheckout);
        bottom.add(cartButtons, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
        split.setResizeWeight(0.45);
        add(split, BorderLayout.CENTER);
    }

    private void doSearch() {
        String keyword = txtSearch.getText().trim();
        lastSearchResults.clear();
        searchModel.setRowCount(0);
        List<Medicine> results = keyword.isEmpty() ? medicineDAO.getAllMedicines() : medicineDAO.searchByName(keyword);
        for (Medicine m : results) {
            lastSearchResults.add(m);
            searchModel.addRow(new Object[]{m.getMedicineId(), m.getName(), m.getPrice(), m.getQuantityInStock()});
        }
    }

    private void addSelectedToCart() {
        int viewRow = searchTable.getSelectedRow();
        if (viewRow < 0) { showError("Select a medicine from search results."); return; }
        
        int modelRow = searchTable.convertRowIndexToModel(viewRow);
        Medicine m = lastSearchResults.get(modelRow);
        if (m.getQuantityInStock() <= 0) { showError(m.getName() + " is out of stock."); return; }

        // Determine existing cart quantity for this medicine
        int currentCartQty = 0;
        SaleItem existingItem = null;
        int existingCartRow = -1;
        for (int i = 0; i < cartItems.size(); i++) {
            SaleItem item = cartItems.get(i);
            if (item.getMedicineId() == m.getMedicineId()) {
                existingItem = item;
                currentCartQty = item.getQuantitySold();
                existingCartRow = i;
                break;
            }
        }

        int availableToCart = m.getQuantityInStock() - currentCartQty;
        if (availableToCart <= 0) {
            showError("Maximum available stock (" + m.getQuantityInStock() + ") already added to cart.");
            return;
        }

        String qtyStr = JOptionPane.showInputDialog(this, "Quantity for " + m.getName() + ":", "1");
        if (qtyStr == null) return;
        
        int qty;
        try {
            qty = Integer.parseInt(qtyStr.trim());
        } catch (NumberFormatException ex) { showError("Invalid quantity."); return; }

        if (qty <= 0 || qty > availableToCart) {
            showError("Quantity must be between 1 and available stock (" + availableToCart + ").");
            return;
        }

        if (existingItem != null) {
            int newQty = currentCartQty + qty;
            existingItem.setQuantitySold(newQty);
            cartModel.setValueAt(newQty, existingCartRow, 3);
            cartModel.setValueAt(existingItem.getLineTotal(), existingCartRow, 4);
        } else {
            SaleItem item = new SaleItem(m.getMedicineId(), m.getName(), qty, m.getPrice());
            cartItems.add(item);
            cartModel.addRow(new Object[]{item.getMedicineId(), item.getMedicineName(), item.getPriceAtSale(),
                    item.getQuantitySold(), item.getLineTotal()});
        }
        recalcTotal();
    }

    private void removeSelectedFromCart() {
        int row = cartTable.getSelectedRow();
        if (row < 0) return;
        cartItems.remove(row);
        cartModel.removeRow(row);
        recalcTotal();
    }

    private void clearCart() {
        cartItems.clear();
        cartModel.setRowCount(0);
        recalcTotal();
    }

    private void recalcTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (SaleItem i : cartItems) total = total.add(i.getLineTotal());
        lblTotal.setText("Total: R " + total.setScale(2, RoundingMode.HALF_UP));
    }

    private void checkout() {
        if (cartItems.isEmpty()) { showError("Cart is empty."); return; }

        Sale sale = new Sale();
        sale.setUserId(Session.getCurrentUser().getUserId());
        sale.setItems(cartItems);
        BigDecimal total = BigDecimal.ZERO;
        for (SaleItem i : cartItems) total = total.add(i.getLineTotal());
        sale.setTotalAmount(total);

        int saleId = saleDAO.recordSale(sale);
        if (saleId == -1) {
            showError("Checkout failed - insufficient stock for one or more items. Please refresh and retry.");
            return;
        }

        sale.setSaleId(saleId);
        new BillWindow(sale).setVisible(true);
        clearCart();
        doSearch(); // refresh stock numbers shown in search results
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}