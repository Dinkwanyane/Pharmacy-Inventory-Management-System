/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pharmacy.inventory.management.system.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import pharmacy.inventory.management.system.DBConnection;
import pharmacy.inventory.management.system.model.Medicine;

/**
 *
 * @author Dinkwanyane
 */
public class MedicineDAO {
    
    private static final String BASE_SELECT =
        "SELECT m.*, s.name AS supplier_name FROM medicines m " +
        "LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id ";

    public List<Medicine> getAllMedicines() {
        List<Medicine> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY m.name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
        }
        return list;
    }

    public List<Medicine> searchByName(String keyword) {
        List<Medicine> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE m.name LIKE ? ORDER BY m.name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
        }
        return list;
    }

    public List<Medicine> getLowStock() {
        List<Medicine> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE m.quantity_in_stock <= m.reorder_level ORDER BY m.quantity_in_stock";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
        }
        return list;
    }

    public List<Medicine> getExpiringWithinDays(int days) {
        List<Medicine> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE m.expiry_date <= DATE_ADD(CURDATE(), INTERVAL ? DAY) ORDER BY m.expiry_date";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
        }
        return list;
    }

    public boolean addMedicine(Medicine m) {
        String sql = "INSERT INTO medicines (name, company, medicine_type, price, quantity_in_stock, " +
                     "reorder_level, expiry_date, supplier_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindMedicine(ps, m);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean updateMedicine(Medicine m) {
        String sql = "UPDATE medicines SET name=?, company=?, medicine_type=?, price=?, quantity_in_stock=?, " +
                     "reorder_level=?, expiry_date=?, supplier_id=? WHERE medicine_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindMedicine(ps, m);
            ps.setInt(9, m.getMedicineId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean deleteMedicine(int medicineId) {
        String sql = "DELETE FROM medicines WHERE medicine_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /** Decrements stock after a sale; returns false if insufficient stock.
     * @param medicineId
     * @param quantity
     * @param conn
     * @return 
     * @throws java.sql.SQLException */
    public boolean decrementStock(int medicineId, int quantity, Connection conn) throws SQLException {
        String sql = "UPDATE medicines SET quantity_in_stock = quantity_in_stock - ? " +
                     "WHERE medicine_id = ? AND quantity_in_stock >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, medicineId);
            ps.setInt(3, quantity);
            return ps.executeUpdate() > 0;
        }
    }

    private void bindMedicine(PreparedStatement ps, Medicine m) throws SQLException {
        ps.setString(1, m.getName());
        ps.setString(2, m.getCompany());
        ps.setString(3, m.getMedicineType());
        ps.setBigDecimal(4, m.getPrice());
        ps.setInt(5, m.getQuantityInStock());
        ps.setInt(6, m.getReorderLevel());
        ps.setDate(7, m.getExpiryDate());
        if (m.getSupplierId() > 0) {
            ps.setInt(8, m.getSupplierId());
        } else {
            ps.setNull(8, Types.INTEGER);
        }
    }

    private Medicine mapRow(ResultSet rs) throws SQLException {
        Medicine m = new Medicine(
                rs.getInt("medicine_id"),
                rs.getString("name"),
                rs.getString("company"),
                rs.getString("medicine_type"),
                rs.getBigDecimal("price"),
                rs.getInt("quantity_in_stock"),
                rs.getInt("reorder_level"),
                rs.getDate("expiry_date"),
                rs.getInt("supplier_id")
        );
        m.setSupplierName(rs.getString("supplier_name"));
        return m;
    }
}
