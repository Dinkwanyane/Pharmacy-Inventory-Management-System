/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pharmacy.inventory.management.system.dao;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import pharmacy.inventory.management.system.DBConnection;
import pharmacy.inventory.management.system.model.Sale;
import pharmacy.inventory.management.system.model.SaleItem;

/**
 *
 * @author Dinkwanyane
 */
public class SaleDAO {
    
    private final MedicineDAO medicineDAO = new MedicineDAO();

    /**
     * Persists a sale and its line items in a single transaction, decrementing stock
     * for each medicine. Returns the generated sale_id, or -1 on failure (e.g. insufficient stock).
     * @param sale
     * @return 
     */
    public int recordSale(Sale sale) {
        String saleSql = "INSERT INTO sales (total_amount, user_id) VALUES (?, ?)";
        String itemSql = "INSERT INTO sale_items (sale_id, medicine_id, quantity_sold, price_at_sale) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int generatedSaleId;
            try (PreparedStatement ps = conn.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setBigDecimal(1, sale.getTotalAmount());
                ps.setInt(2, sale.getUserId());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Failed to obtain sale_id");
                    generatedSaleId = keys.getInt(1);
                }
            }

            for (SaleItem item : sale.getItems()) {
                boolean stockOk = medicineDAO.decrementStock(item.getMedicineId(), item.getQuantitySold(), conn);
                if (!stockOk) {
                    conn.rollback();
                    return -1; // insufficient stock
                }
                try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
                    ps.setInt(1, generatedSaleId);
                    ps.setInt(2, item.getMedicineId());
                    ps.setInt(3, item.getQuantitySold());
                    ps.setBigDecimal(4, item.getPriceAtSale());
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return generatedSaleId;

        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            return -1;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) {}
        }
    }

    /** Total sales revenue and count between two dates (inclusive), for the sales report.
     * @param from
     * @param to
     * @return  */
    public List<Sale> getSalesBetween(Date from, Date to) {
        List<Sale> list = new ArrayList<>();
        String sql = "SELECT * FROM sales WHERE DATE(sale_date) BETWEEN ? AND ? ORDER BY sale_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, from);
            ps.setDate(2, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Sale s = new Sale();
                    s.setSaleId(rs.getInt("sale_id"));
                    s.setSaleDate(rs.getTimestamp("sale_date"));
                    s.setTotalAmount(rs.getBigDecimal("total_amount"));
                    s.setUserId(rs.getInt("user_id"));
                    list.add(s);
                }
            }
        } catch (SQLException e) {
        }
        return list;
    }

    /** Item-wise sales report: quantity sold and revenue per medicine, across all time.
     * @return  */
    public List<Object[]> getItemWiseReport() {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT m.name, SUM(si.quantity_sold) AS total_qty, SUM(si.quantity_sold * si.price_at_sale) AS total_revenue " +
                     "FROM sale_items si JOIN medicines m ON si.medicine_id = m.medicine_id " +
                     "GROUP BY m.medicine_id, m.name ORDER BY total_revenue DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new Object[]{
                        rs.getString("name"),
                        rs.getInt("total_qty"),
                        rs.getBigDecimal("total_revenue")
                });
            }
        } catch (SQLException e) {
        }
        return rows;
    }

    public BigDecimal getTotalRevenue(Date from, Date to) {
        String sql = "SELECT COALESCE(SUM(total_amount),0) AS total FROM sales WHERE DATE(sale_date) BETWEEN ? AND ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, from);
            ps.setDate(2, to);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal("total");
            }
        } catch (SQLException e) {
        }
        return BigDecimal.ZERO;
    }
}
