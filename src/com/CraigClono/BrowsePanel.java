package com.CraigClono;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.*;


public class BrowsePanel extends JPanel {
    private User currentUser;
    private JList<Listing> listingsList;
    private DefaultListModel<Listing> listModel;
    private JLabel detailsLabel;
    private JLabel imageLabel;
    private JButton deleteButton;
    private JButton prevImageButton, nextImageButton;
    private JLabel imageCountLabel;
    private java.util.List<String> currentImages = new java.util.ArrayList<>();
    private int currentImageIndex = 0;
    private JLabel timeLabel; // NEW

    public BrowsePanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Add demo listings if database is empty
        addDemoListings();

        // Top: Search Panel
        JPanel searchPanel = new JPanel();
        searchPanel.setBackground(new Color(240, 240, 240));
        JTextField searchField = new JTextField(20);
        JComboBox<String> categoryFilter = new JComboBox<>(
                new String[]{"All", "Electronics", "Vehicles", "Furniture", "Books", "Others"}
        );
        JTextField minPriceField = new JTextField(6);
        JTextField maxPriceField = new JTextField(6);

        JButton filterButton = new JButton("Filter");
        JButton searchButton = new JButton("Search");
        JButton showAllButton = new JButton("Show All");
        JButton favoritesButton = new JButton("⭐ My Favorites");
        searchPanel.add(favoritesButton);

        searchButton.addActionListener(e -> searchListings(searchField.getText()));
        showAllButton.addActionListener(e -> loadAllListings());
        filterButton.addActionListener(e -> applyFilters(
                searchField.getText(),
                (String) categoryFilter.getSelectedItem(),
                minPriceField.getText(),
                maxPriceField.getText()
        ));
        favoritesButton.addActionListener(e -> loadFavorites());

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);

        searchPanel.add(new JLabel("Category:"));
        searchPanel.add(categoryFilter);

        searchPanel.add(new JLabel("Min ₹:"));
        searchPanel.add(minPriceField);

        searchPanel.add(new JLabel("Max ₹:"));
        searchPanel.add(maxPriceField);

        searchPanel.add(filterButton);

        add(searchPanel, BorderLayout.NORTH);

        // Main: 2-Column Layout
        createTwoColumnLayout();

        // Load all listings on start
        loadAllListings();
    }

    private void loadFavorites() {
        listModel.clear();

        try {
            Connection conn = DatabaseConnection.getConnection();

            String query = "SELECT l.* FROM listings l " +
                    "JOIN favorites f ON l.listing_id = f.listing_id " +
                    "WHERE f.user_id = ?";

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

                listing.imagePath = rs.getString("image_path");
                listing.createdDate = rs.getTimestamp("created_date");

                listModel.addElement(listing);
            }

            conn.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void applyFilters(String keyword, String category, String minPrice, String maxPrice) {
        listModel.clear();

        try {
            Connection conn = DatabaseConnection.getConnection();

            StringBuilder query = new StringBuilder(
                    "SELECT * FROM listings WHERE 1=1"
            );

            if (!keyword.isEmpty()) {
                query.append(" AND (title LIKE ? OR description LIKE ?)");
            }

            if (!category.equals("All")) {
                query.append(" AND category = ?");
            }

            if (!minPrice.isEmpty()) {
                query.append(" AND price >= ?");
            }

            if (!maxPrice.isEmpty()) {
                query.append(" AND price <= ?");
            }

            query.append(" ORDER BY created_date DESC");

            PreparedStatement pstmt = conn.prepareStatement(query.toString());

            int index = 1;

            if (!keyword.isEmpty()) {
                pstmt.setString(index++, "%" + keyword + "%");
                pstmt.setString(index++, "%" + keyword + "%");
            }

            if (!category.equals("All")) {
                pstmt.setString(index++, category);
            }

            if (!minPrice.isEmpty()) {
                pstmt.setDouble(index++, Double.parseDouble(minPrice));
            }

            if (!maxPrice.isEmpty()) {
                pstmt.setDouble(index++, Double.parseDouble(maxPrice));
            }

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

                listing.imagePath = rs.getString("image_path");
                listing.createdDate = rs.getTimestamp("created_date");

                listModel.addElement(listing);
            }

            conn.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Filter Error: " + e.getMessage());
        }
    }

    // Add demo listings if database is empty
    private void addDemoListings() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String countQuery = "SELECT COUNT(*) FROM listings";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(countQuery);
            rs.next();

            int count = rs.getInt(1);

            // Only add demo listings if table is empty
            if (count == 0) {
                // First, create a demo user if it doesn't exist
                String userQuery = "INSERT IGNORE INTO users (user_id, username, password, phone, email) VALUES (1, 'demo_seller', 'demo123', '9876543210', 'demo@email.com')";
                PreparedStatement userStmt = conn.prepareStatement(userQuery);
                userStmt.executeUpdate();

                // Now add demo listings
                String insertQuery = "INSERT INTO listings (user_id, title, description, price, seller_name, seller_phone, image_path) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(insertQuery);

                // Demo Listing 1
                pstmt.setInt(1, 1);
                pstmt.setString(2, "iPhone 13 - Like New Condition");
                pstmt.setString(3, "iPhone 13 in excellent condition. Comes with original box and charger. No scratches or damage. Battery health at 95%. Perfect for daily use.");
                pstmt.setBigDecimal(4, new java.math.BigDecimal("35000.00"));
                pstmt.setString(5, "demo_seller");
                pstmt.setString(6, "9876543210");
                pstmt.setString(7, null);
                pstmt.executeUpdate();

                // Demo Listing 2
                pstmt.setInt(1, 1);
                pstmt.setString(2, "Used Bicycle - Mountain Bike");
                pstmt.setString(3, "Trek mountain bike, 21-speed, good condition. Has been serviced recently by professional. Great for trails and casual weekend rides.");
                pstmt.setBigDecimal(4, new java.math.BigDecimal("8000.00"));
                pstmt.setString(5, "demo_seller");
                pstmt.setString(6, "9876543210");
                pstmt.setString(7, null);
                pstmt.executeUpdate();

                // Demo Listing 3
                pstmt.setInt(1, 1);
                pstmt.setString(2, "Office Desk - Wooden Premium");
                pstmt.setString(3, "Beautiful wooden office desk made from quality teak wood. Spacious surface with 3 drawer storage. Perfect for home office. Must go this week!");
                pstmt.setBigDecimal(4, new java.math.BigDecimal("9000.00"));
                pstmt.setString(5, "demo_seller");
                pstmt.setString(6, "9876543210");
                pstmt.setString(7, null);
                pstmt.executeUpdate();

                // Demo Listing 4
                pstmt.setInt(1, 1);
                pstmt.setString(2, "Laptop - Dell XPS 13 2022");
                pstmt.setString(3, "Dell XPS 13 laptop, Intel i7 12th Gen, 16GB RAM, 512GB SSD. Only 2 years old. Runs beautifully, perfect for work and light gaming. No issues.");
                pstmt.setBigDecimal(4, new java.math.BigDecimal("60000.00"));
                pstmt.setString(5, "demo_seller");
                pstmt.setString(6, "9876543210");
                pstmt.setString(7, null);
                pstmt.executeUpdate();

                // Demo Listing 5
                pstmt.setInt(1, 1);
                pstmt.setString(2, "Coffee Table - Modern Glass Design");
                pstmt.setString(3, "Modern glass and steel coffee table with minimalist design. Adds elegance to any living room. Barely used, looks like new. Very stylish!");
                pstmt.setBigDecimal(4, new java.math.BigDecimal("5000.00"));
                pstmt.setString(5, "demo_seller");
                pstmt.setString(6, "9876543210");
                pstmt.setString(7, null);
                pstmt.executeUpdate();

                // Demo Listing 6
                pstmt.setInt(1, 1);
                pstmt.setString(2, "Gaming Console - PlayStation 5");
                pstmt.setString(3, "PS5 with 2 DualSense controllers and 3 games (FIFA 24, Spider-Man 2, Elden Ring). All in perfect working condition. Original box included.");
                pstmt.setBigDecimal(4, new java.math.BigDecimal("35000.00"));
                pstmt.setString(5, "demo_seller");
                pstmt.setString(6, "9876543210");
                pstmt.setString(7, null);
                pstmt.executeUpdate();

                // Demo Listing 7
                pstmt.setInt(1, 1);
                pstmt.setString(2, "Camera - Canon EOS Rebel T7 DSLR");
                pstmt.setString(3, "Great beginner DSLR camera with 18-55mm lens. Excellent for photography and videography. Includes memory card, bag, and tripod. Very user-friendly.");
                pstmt.setBigDecimal(4, new java.math.BigDecimal("25000.00"));
                pstmt.setString(5, "demo_seller");
                pstmt.setString(6, "9876543210");
                pstmt.setString(7, null);
                pstmt.executeUpdate();

                // Demo Listing 8
                pstmt.setInt(1, 1);
                pstmt.setString(2, "Running Shoes - Nike Air Max");
                pstmt.setString(3, "Nike Air Max running shoes, size 10. Worn only 3 times. Perfect condition like new. Great for running and casual wear. Very comfortable!");
                pstmt.setBigDecimal(4, new java.math.BigDecimal("4000.00"));
                pstmt.setString(5, "demo_seller");
                pstmt.setString(6, "9876543210");
                pstmt.setString(7, null);
                pstmt.executeUpdate();

                System.out.println("✅ Demo listings added successfully!");
            }

            conn.close();
        } catch (SQLException e) {
            System.err.println("Error adding demo listings: " + e.getMessage());
        }
    }

    private void createTwoColumnLayout() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));

        // LEFT: Listings
        listModel = new DefaultListModel<>();
        listingsList = new JList<>(listModel);
        listingsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listingsList.addListSelectionListener(e -> showListingDetails());
        JScrollPane listScrollPane = new JScrollPane(listingsList);
        listScrollPane.setBorder(BorderFactory.createTitledBorder("Available Listings"));
        mainPanel.add(listScrollPane);

        // RIGHT: Details + Images (stacked)
        JPanel rightPanel = new JPanel(new GridLayout(2, 1));

        // Details panel
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(new Color(245, 245, 245));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Details"));

        detailsLabel = new JLabel("<html><center>Select a listing to see details</center></html>");
        detailsLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        detailsPanel.add(detailsLabel);
        detailsPanel.add(Box.createVerticalStrut(10));

        deleteButton = new JButton("🗑️ Delete (If Owner)");
        deleteButton.setBackground(new Color(231, 76, 60));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.addActionListener(e -> deleteSelectedListing());
        detailsPanel.add(deleteButton);

        JButton favoriteButton = new JButton("⭐ Add to Favorites");
        favoriteButton.setBackground(new Color(241, 196, 15));
        favoriteButton.setForeground(Color.BLACK);

        favoriteButton.addActionListener(e -> toggleFavorite());

        detailsPanel.add(Box.createVerticalStrut(10));
        detailsPanel.add(favoriteButton);

// NEW TIME LABEL (GOES IN THAT EMPTY SPACE)
        timeLabel = new JLabel(" ");
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        timeLabel.setForeground(new Color(120, 120, 120));
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.add(Box.createVerticalStrut(10));
        detailsPanel.add(timeLabel);

        detailsPanel.add(Box.createVerticalGlue());

        rightPanel.add(new JScrollPane(detailsPanel));

        // Image panel
        JPanel imageGalleryPanel = new JPanel();
        imageGalleryPanel.setLayout(new BorderLayout());
        imageGalleryPanel.setBackground(Color.WHITE);
        imageGalleryPanel.setBorder(BorderFactory.createTitledBorder("Images"));

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.CENTER);
        imageLabel.setBackground(new Color(240, 240, 240));
        imageLabel.setOpaque(true);
        imageLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        imageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (currentImages.size() > 0 && !currentImages.get(currentImageIndex).isEmpty()) {
                    zoomImage();
                }
            }
        });
        imageGalleryPanel.add(imageLabel, BorderLayout.CENTER);

        JPanel navPanel = new JPanel();
        navPanel.setBackground(new Color(240, 240, 240));

        prevImageButton = new JButton("⬅ Prev");
        prevImageButton.addActionListener(e -> showPreviousImage());
        navPanel.add(prevImageButton);

        imageCountLabel = new JLabel("0/0");
        imageCountLabel.setFont(new Font("Arial", Font.BOLD, 12));
        imageCountLabel.setPreferredSize(new Dimension(50, 25));
        navPanel.add(imageCountLabel);

        nextImageButton = new JButton("Next ➡");
        nextImageButton.addActionListener(e -> showNextImage());
        navPanel.add(nextImageButton);

        imageGalleryPanel.add(navPanel, BorderLayout.SOUTH);
        rightPanel.add(imageGalleryPanel);

        mainPanel.add(rightPanel);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void toggleFavorite() {
        Listing selected = listingsList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a listing first!");
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();

            // Check if already favorited
            String checkQuery = "SELECT * FROM favorites WHERE user_id=? AND listing_id=?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, currentUser.userId);
            checkStmt.setInt(2, selected.listingId);

            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Already exists → remove
                String deleteQuery = "DELETE FROM favorites WHERE user_id=? AND listing_id=?";
                PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
                deleteStmt.setInt(1, currentUser.userId);
                deleteStmt.setInt(2, selected.listingId);
                deleteStmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "❌ Removed from favorites");
            } else {
                // Add
                String insertQuery = "INSERT INTO favorites (user_id, listing_id) VALUES (?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setInt(1, currentUser.userId);
                insertStmt.setInt(2, selected.listingId);
                insertStmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "⭐ Added to favorites!");
            }

            conn.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    public void loadAllListings() {
        listModel.clear();
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT * FROM listings ORDER BY created_date DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

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
                listing.imagePath = rs.getString("image_path");
                listing.createdDate = rs.getTimestamp("created_date"); // NEW
                listModel.addElement(listing);
            }

            System.out.println("✅ Loaded listings");
            conn.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading listings: " + e.getMessage());
        }
    }

    private void searchListings(String keyword) {
        listModel.clear();
        if (keyword.isEmpty()) {
            loadAllListings();
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT * FROM listings WHERE title LIKE ? OR description LIKE ? ORDER BY created_date DESC";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");

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
                listing.imagePath = rs.getString("image_path");
                listing.createdDate = rs.getTimestamp("created_date"); // NEW
                listModel.addElement(listing);
            }

            conn.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void showListingDetails() {
        Listing selected = listingsList.getSelectedValue();
        if (selected == null) return;

        currentImageIndex = 0;
        currentImages.clear();

        if (selected.imagePath != null && !selected.imagePath.isEmpty()) {
            File imageFile = new File(selected.imagePath);
            if (imageFile.exists()) {
                currentImages.add(selected.imagePath);
            }
        }

        displayCurrentImage();

        String details = "<html>" +
                "<b style='font-size:13px'>📋 " + selected.title + "</b><br><br>" +
                "<b>💰 Price:</b> ₹" + String.format("%,.0f", selected.price) + "<br><br>" +
                "<b>📝 Description:</b><br><font size='2'>" + selected.description + "</font><br><br>" +
                "<b>👤 Seller:</b> " + selected.sellerName + "<br>" +
                "<b>📞 Phone:</b> " + selected.sellerPhone + "<br>" +
                "<b>📍 Location:</b> " + selected.location + "<br>" +
                "<b>📂 Category:</b> " + selected.category + "<br>" +
                "</html>";
        detailsLabel.setText(details);
        if (selected.createdDate != null) {
            String time = new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a")
                    .format(selected.createdDate);
            timeLabel.setText("🕒 Posted: " + time);
        } else {
            timeLabel.setText("🕒 Posted: N/A");
        }

        updateImageNavigation();
    }

    private void displayCurrentImage() {
        if (currentImages.size() == 0) {
            imageLabel.setIcon(null);
            imageLabel.setText("📷 No images available");
            return;
        }

        try {
            String imagePath = currentImages.get(currentImageIndex);
            ImageIcon imageIcon = new ImageIcon(imagePath);
            Image scaledImage = imageIcon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
            imageLabel.setText("");
        } catch (Exception e) {
            imageLabel.setIcon(null);
            imageLabel.setText("❌ Error loading image");
        }
    }

    private void updateImageNavigation() {
        if (currentImages.size() == 0) {
            imageCountLabel.setText("0/0");
            prevImageButton.setEnabled(false);
            nextImageButton.setEnabled(false);
        } else {
            imageCountLabel.setText((currentImageIndex + 1) + "/" + currentImages.size());
            prevImageButton.setEnabled(currentImageIndex > 0);
            nextImageButton.setEnabled(currentImageIndex < currentImages.size() - 1);
        }
    }

    private void showNextImage() {
        if (currentImageIndex < currentImages.size() - 1) {
            currentImageIndex++;
            displayCurrentImage();
            updateImageNavigation();
        }
    }

    private void showPreviousImage() {
        if (currentImageIndex > 0) {
            currentImageIndex--;
            displayCurrentImage();
            updateImageNavigation();
        }
    }

    private void zoomImage() {
        if (currentImages.size() == 0) return;
        String imagePath = currentImages.get(currentImageIndex);
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) return;

        ImageZoomDialog zoomDialog = new ImageZoomDialog((JFrame) SwingUtilities.getWindowAncestor(this), imagePath);
        zoomDialog.setVisible(true);
    }

    private void deleteSelectedListing() {
        Listing selected = listingsList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a listing!");
            return;
        }

        if (selected.userId != currentUser.userId) {
            JOptionPane.showMessageDialog(this, "You can only delete your own listings!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this listing?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "DELETE FROM listings WHERE listing_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, selected.listingId);
            pstmt.executeUpdate();

            if (selected.imagePath != null && !selected.imagePath.isEmpty()) {
                File imageFile = new File(selected.imagePath);
                if (imageFile.exists()) {
                    imageFile.delete();
                }
            }

            JOptionPane.showMessageDialog(this, "✅ Listing deleted successfully!");
            loadAllListings();
            conn.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "❌ Error: " + e.getMessage());
        }
    }
}

class ImageZoomDialog extends JDialog {
    public ImageZoomDialog(JFrame parent, String imagePath) {
        super(parent, "Image Zoom", true);
        setSize(600, 700);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.CENTER);
        imageLabel.setBackground(Color.BLACK);
        imageLabel.setOpaque(true);

        try {
            ImageIcon imageIcon = new ImageIcon(imagePath);
            Image scaledImage = imageIcon.getImage().getScaledInstance(500, 500, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            imageLabel.setText("Error loading image");
            imageLabel.setForeground(Color.WHITE);
        }

        panel.add(imageLabel, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        panel.add(closeButton, BorderLayout.SOUTH);

        add(panel);
    }
}