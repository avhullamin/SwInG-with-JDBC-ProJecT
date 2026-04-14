package com.CraigClono;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MyListingsPanel extends JPanel {
    private User currentUser;
    private JList<Listing> myListingsList;
    private DefaultListModel<Listing> listModel;

    public MyListingsPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Your Listings:");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(titleLabel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        myListingsList = new JList<>(listModel);

        add(new JScrollPane(myListingsList), BorderLayout.CENTER);

        // Load user's listings when panel is shown
        loadUserListings();
    }

    public void loadUserListings() {
        listModel.clear();
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT * FROM listings WHERE user_id = ? ORDER BY created_date DESC";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, currentUser.userId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Listing listing = new Listing(
                        rs.getInt("listing_id"),
                        rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getBigDecimal("price"),
                        rs.getString("seller_name"),
                        rs.getString("seller_phone"),
                        rs.getString("category"),
                        rs.getString("location")
                );
                listModel.addElement(listing);
            }
            conn.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}