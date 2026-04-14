package com.CraigClono;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private User currentUser;
    private BrowsePanel browsePanel;
    private PostPanel postPanel;

    public MainFrame(User user) {
        this.currentUser = user;
        setTitle("Open Market Listings - Welcome " + user.username);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create panels
        browsePanel = new BrowsePanel(currentUser);
        postPanel = new PostPanel(currentUser, browsePanel);

        // Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Browse Listings", browsePanel);
        tabbedPane.addTab("Post New Listing", postPanel);
        tabbedPane.addTab("My Listings", new MyListingsPanel(currentUser));

        add(tabbedPane);
        setVisible(true);
    }
}