package com.technolab.istime.ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.technolab.istime.dao.CoursDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginUI extends JFrame {

    private JTextField txtUser;
    private JPasswordField txtPass;
    private CoursDAO dao;

    public LoginUI() {
        dao = new CoursDAO();
        initComponents();
    }

    private void initComponents() {
        setTitle("Connexion - ISTime");
        setSize(400, 350);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Icône
        try {
            ImageIcon icon = new ImageIcon("app.png");
            setIconImage(icon.getImage());
        } catch (Exception e) {}

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JLabel lblLogo = new JLabel("ISTime Authentification", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblLogo.setForeground(new Color(210, 79, 46));
        lblLogo.setBorder(new EmptyBorder(20, 0, 20, 0));
        mainPanel.add(lblLogo, BorderLayout.NORTH);

        // Formulaire
        JPanel formPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        formPanel.setBorder(new EmptyBorder(20, 40, 20, 40));
        formPanel.setBackground(Color.WHITE);

        txtUser = new JTextField();
        txtUser.putClientProperty("JTextField.placeholderText", "Nom d'utilisateur");
        
        txtPass = new JPasswordField();
        txtPass.putClientProperty("JTextField.placeholderText", "Mot de passe");

        JButton btnLogin = new JButton("Se connecter");
        btnLogin.setBackground(new Color(210, 79, 46));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setFocusPainted(false);

        btnLogin.addActionListener(e -> checkLogin());
        
        // Action sur la touche Entrée
        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) checkLogin();
            }
        };
        txtUser.addKeyListener(enterKey);
        txtPass.addKeyListener(enterKey);

        formPanel.add(new JLabel("Identifiant :"));
        formPanel.add(txtUser);
        formPanel.add(new JLabel("Mot de passe :"));
        formPanel.add(txtPass);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(btnLogin);
        bottomPanel.setBorder(new EmptyBorder(0,0,20,0));
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void checkLogin() {
        String user = txtUser.getText();
        String pass = new String(txtPass.getPassword());

        try {
            String role = dao.verifierLogin(user, pass);
            if (role != null) {
                // Connexion réussie
                this.dispose(); // Ferme la fenêtre de login
                new MainUI(role).setVisible(true); // Lance l'appli avec le rôle
            } else {
                JOptionPane.showMessageDialog(this, "Identifiants incorrects !", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur connexion : " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        try { FlatLightLaf.setup(); } catch( Exception ex ) {}
        SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
    }
}
