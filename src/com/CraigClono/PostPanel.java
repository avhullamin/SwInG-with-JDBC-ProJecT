package com.CraigClono;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;

public class PostPanel extends JPanel {
    private User currentUser;
    private BrowsePanel browsePanel;
    private JTextField titleField, priceField, phoneField; // added phoneField
    private JTextArea descriptionArea;
    private JLabel imageLabel;
    private File selectedImageFile;
    private JComboBox<String> categoryBox;
    private JTextField locationField;

    public PostPanel(User user, BrowsePanel browsePanel) {
        this.currentUser = user;
        this.browsePanel = browsePanel;

        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(titleLabel, gbc);

        titleField = new JTextField(30);
        gbc.gridx = 1;
        add(titleField, gbc);

        // Price
        JLabel priceLabel = new JLabel("Price (₹):");
        priceLabel.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(priceLabel, gbc);

        priceField = new JTextField(30);
        gbc.gridx = 1;
        add(priceField, gbc);

        // Phone (NEW)
        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(phoneLabel, gbc);

        phoneField = new JTextField(30);
        gbc.gridx = 1;
        add(phoneField, gbc);

        // Category
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridx = 0;
        gbc.gridy = 4;
        add(categoryLabel, gbc);

        String[] categories = {"Electronics", "Vehicles", "Furniture", "Books", "Others"};
        categoryBox = new JComboBox<>(categories);
        gbc.gridx = 1;
        gbc.gridy = 4;
        add(categoryBox, gbc);

        // Location
        JLabel locationLabel = new JLabel("Location:");
        locationLabel.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridx = 0;
        gbc.gridy = 5;
        add(locationLabel, gbc);

        locationField = new JTextField(30);
        gbc.gridx = 1;
        gbc.gridy = 5;
        add(locationField, gbc);

        // Description
        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTH;
        add(descLabel, gbc);

        descriptionArea = new JTextArea(5, 30);
        descriptionArea.setPreferredSize(new Dimension(300, 100));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        add(new JScrollPane(descriptionArea), gbc);

        // Image
        JLabel imageSelectLabel = new JLabel("Image:");
        imageSelectLabel.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        add(imageSelectLabel, gbc);

        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JButton chooseImageButton = new JButton("📁 Choose Image");
        chooseImageButton.setBackground(new Color(52, 152, 219));
        chooseImageButton.setForeground(Color.WHITE);
        chooseImageButton.addActionListener(e -> chooseImage());
        imagePanel.add(chooseImageButton);

        imageLabel = new JLabel("No image selected");
        imageLabel.setForeground(new Color(100, 100, 100));
        imagePanel.add(imageLabel);

        gbc.gridx = 1;
        gbc.gridy = 6;
        add(imagePanel, gbc);

        // Button
        JButton postButton = new JButton("📤 Post Listing");
        postButton.setBackground(new Color(46, 204, 113));
        postButton.setForeground(Color.WHITE);
        postButton.setFont(new Font("Arial", Font.BOLD, 12));
        postButton.addActionListener(e -> postListing());
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(postButton, gbc);
    }

    private void chooseImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose an image for your listing");

        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Image Files (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = fileChooser.getSelectedFile();
            imageLabel.setText("✅ " + selectedImageFile.getName());
            imageLabel.setForeground(new Color(46, 204, 113));
        }
    }

    private void postListing() {
        String title = titleField.getText().trim();
        String priceStr = priceField.getText().trim();
        String description = descriptionArea.getText().trim();
        String phone = phoneField.getText().trim(); // NEW
        String category = (String) categoryBox.getSelectedItem();
        String location = locationField.getText().trim();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "❌ Please enter a title!");
            return;
        }

        if (priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "❌ Please enter a price!");
            return;
        }

        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "❌ Please enter phone number!");
            return;
        }

        if (description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "❌ Please enter a description!");
            return;
        }

        if (location.isEmpty()) {
            JOptionPane.showMessageDialog(this, "❌ Enter location!");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) {
                JOptionPane.showMessageDialog(this, "❌ Price must be positive!");
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "❌ Invalid price!");
            return;
        }

        String imagePath = null;
        if (selectedImageFile != null) {
            imagePath = saveImage(selectedImageFile);
            if (imagePath == null) {
                JOptionPane.showMessageDialog(this, "❌ Failed to upload image!");
                return;
            }
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "INSERT INTO listings (user_id, title, description, price, seller_name, seller_phone, category, location, image_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, currentUser.userId);
            pstmt.setString(2, title);
            pstmt.setString(3, description);
            pstmt.setDouble(4, price);
            pstmt.setString(5, currentUser.username);
            pstmt.setString(6, phone);
            pstmt.setString(7, category);
            pstmt.setString(8, location);
            pstmt.setString(9, imagePath);

            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "✅ Listing posted!");

            titleField.setText("");
            priceField.setText("");
            phoneField.setText(""); // CLEAR
            descriptionArea.setText("");
            selectedImageFile = null;
            imageLabel.setText("No image selected");
            imageLabel.setForeground(new Color(100, 100, 100));

            browsePanel.loadAllListings();

            conn.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "❌ Error: " + e.getMessage());
        }
    }

    private String saveImage(File imageFile) {
        try {
            File imagesDir = new File("images/listings");
            if (!imagesDir.exists()) {
                imagesDir.mkdirs();
            }

            long timestamp = System.currentTimeMillis();
            String fileName = "listing_" + timestamp + "_" + imageFile.getName();
            File destination = new File(imagesDir, fileName);

            Files.copy(imageFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);

            return "images/listings/" + fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}