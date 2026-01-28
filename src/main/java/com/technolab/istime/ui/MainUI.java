package com.technolab.istime.ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.technolab.istime.dao.CoursDAO;
import com.technolab.istime.model.Cours;
import com.technolab.istime.utils.SyncService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainUI extends JFrame {

    private CoursDAO dao;
    private JTabbedPane tabbedPane;
    private Map<String, DefaultTableModel> tableModels = new HashMap<>();
    private Integer idCoursEnEdition = null;
    private String userRole = "ADMIN"; 

    private final String[] SALLES = {"A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "B2", "B3", "Informatique", "Profs"};
    
    private final String[] CRENEAUX_LIGNES = {"17h – 19h", "19h – 21h"};
    private final String[] CHOIX_HEURES_SEMAINE = {"17h – 19h", "19h – 21h"};
    private final String[] CHOIX_HEURES_SAMEDI = {"09h – 11h", "11h – 13h"};
    private final String[] JOURS = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};

    private JComboBox<String> cbFiliere, cbSalle, cbJour, cbCreneau, cbProf, cbMatiere;
    private JButton btnAction;

    public MainUI() {
        this("ADMIN");
    }

    public MainUI(String role) {
        this.userRole = role;
        dao = new CoursDAO();
        initComponents();
        chargerDonneesReference();
        if ("GUEST".equals(userRole)) applyGuestRestrictions();
    }

    private void initComponents() {
        setTitle("ISTime - Gestion & Administration");
        setSize(1350, 850);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            ImageIcon icon = new ImageIcon("app.png");
            setIconImage(icon.getImage());
            if (Taskbar.isTaskbarSupported() && Taskbar.getTaskbar().isSupported(Taskbar.Feature.ICON_IMAGE)) {
                Taskbar.getTaskbar().setIconImage(icon.getImage());
            }
        } catch (Exception e) {}

        JPanel mainPanel = new JPanel(new BorderLayout());
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(210, 79, 46));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel title = new JLabel("ISTIME | " + ("ADMIN".equals(userRole) ? "Mode Administrateur" : "Mode Consultation"));
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        
        JButton btnLogout = new JButton("Déconnexion");
        btnLogout.setBackground(new Color(180, 50, 30));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.addActionListener(e -> { this.dispose(); new LoginUI().setVisible(true); });
        
        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(btnLogout, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        tabbedPane.addTab(" Planning ", createConsultationPanel());
        tabbedPane.addTab(" Dashboard ", createDashboardPanel());
        tabbedPane.addTab(" Administration ", createAdminPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }
    
    private void applyGuestRestrictions() {
        if (tabbedPane.getTabCount() > 2) tabbedPane.remove(2);
        if (btnAction != null) { btnAction.setEnabled(false); btnAction.setText("Lecture Seule"); }
    }

    private void chargerDonneesReference() {
        try {
            List<String> profs = dao.getListe("professeurs");
            cbProf.removeAllItems(); for(String p : profs) cbProf.addItem(p);

            List<String> matieres = dao.getListe("matieres");
            cbMatiere.removeAllItems(); for(String m : matieres) cbMatiere.addItem(m);
            
            List<String> filieres = dao.getListe("filieres");
            cbFiliere.removeAllItems(); for(String f : filieres) cbFiliere.addItem(f);
            
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- CONSULTATION
    private JPanel createConsultationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Barre d'outils HAUT
        JPanel topToolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topToolBar.setBackground(new Color(230, 230, 230));
        
        JComboBox<String> cbSearchType = new JComboBox<>(new String[]{"Professeur", "Salle"});
        JTextField txtSearch = new JTextField(15);
        JButton btnSearch = new JButton("🔍 Rechercher");
        btnSearch.addActionListener(e -> actionRecherche(cbSearchType.getSelectedItem().toString(), txtSearch.getText()));
        
        JButton btnFreeRooms = new JButton("🏢 Trouver Salle Libre");
        btnFreeRooms.setBackground(new Color(0, 150, 136));
        btnFreeRooms.setForeground(Color.WHITE);
        btnFreeRooms.addActionListener(e -> actionTrouverSallesLibres());

        topToolBar.add(new JLabel("Filtre : "));
        topToolBar.add(cbSearchType);
        topToolBar.add(txtSearch);
        topToolBar.add(btnSearch);
        topToolBar.add(Box.createHorizontalStrut(20));
        topToolBar.add(btnFreeRooms);
        
        panel.add(topToolBar, BorderLayout.NORTH);

        JTabbedPane joursTab = new JTabbedPane(JTabbedPane.LEFT);
        for (String jour : JOURS) joursTab.addTab(jour, createTablePanel(jour));
        panel.add(joursTab, BorderLayout.CENTER);

        // Barre d'outils BAS
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        toolbar.setBackground(new Color(240, 242, 245));
        
        JButton btnEdit = createStyledButton("Modifier", new Color(255, 152, 0));
        JButton btnDelete = createStyledButton("Supprimer", new Color(244, 67, 54));
        JButton btnExport = createStyledButton("Générer PDF", new Color(63, 81, 181));
        JButton btnStats = createStyledButton("Volume Horaire", new Color(100, 100, 100));

        btnEdit.addActionListener(e -> actionModifier(joursTab));
        btnDelete.addActionListener(e -> actionSupprimer(joursTab));
        btnExport.addActionListener(e -> exportPDFDialog());
        btnStats.addActionListener(e -> afficherStatistiques());

        if ("GUEST".equals(userRole)) {
            btnEdit.setEnabled(false); btnDelete.setEnabled(false);
        }

        toolbar.add(btnStats);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(btnExport);

        panel.add(toolbar, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createTablePanel(String jour) {
        JPanel p = new JPanel(new BorderLayout());
        String[] columns = {"ID", "Salle", "Créneau", "Filière", "Matière", "Professeur"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableModels.put(jour, model);
        
        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        chargerDonneesDansTableau(jour, model);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    // --- TROUVER SALLES LIBRES
    private void actionTrouverSallesLibres() {
        JDialog dialog = new JDialog(this, "Recherche de disponibilité", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        JComboBox<String> comboJour = new JComboBox<>(JOURS);
        JComboBox<String> comboCreneau = new JComboBox<>(CHOIX_HEURES_SEMAINE);
        JButton btnCheck = new JButton("Vérifier");
        
        btnCheck.setBackground(new Color(0, 150, 136));
        btnCheck.setForeground(Color.WHITE);
        btnCheck.setFont(new Font("Segoe UI", Font.BOLD, 14));

        comboJour.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED) {
                comboCreneau.removeAllItems();
                if("Samedi".equals(e.getItem())) for(String h : CHOIX_HEURES_SAMEDI) comboCreneau.addItem(h);
                else for(String h : CHOIX_HEURES_SEMAINE) comboCreneau.addItem(h);
            }
        });

        topPanel.add(new JLabel("Jour :"));
        topPanel.add(comboJour);
        topPanel.add(new JLabel("Créneau :"));
        topPanel.add(comboCreneau);
        topPanel.add(btnCheck);

        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        resultArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        btnCheck.addActionListener(e -> {
            try {
                String j = (String) comboJour.getSelectedItem();
                String c = (String) comboCreneau.getSelectedItem();
                
                // Récupération des données
                List<String> occupees = dao.getSallesOccupees(j, c);
                List<String> libres = new ArrayList<>(Arrays.asList(SALLES));
                libres.removeAll(occupees); // On enlève celles qui sont prises
                
                if(libres.isEmpty()) {
                    resultArea.setText("⛔. AUCUNE SALLE DISPONIBLE\n\nToutes les salles sont occupées\npour ce créneau.");
                    resultArea.setForeground(Color.RED);
                } else {
                    resultArea.setText("✅. SALLES LIBRES (" + j + " " + c + ") :\n\n" + String.join("\n- ", libres));
                    if(!libres.isEmpty()) resultArea.setText(resultArea.getText().replaceFirst(libres.get(0), "- " + libres.get(0)));
                    resultArea.setForeground(new Color(0, 100, 0));
                }
            } catch(SQLException ex) {
                resultArea.setText("Erreur Base de données : " + ex.getMessage());
            }
        });

        dialog.add(topPanel, BorderLayout.NORTH);
        dialog.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        
        dialog.setVisible(true);
    }

    // --- ADMINISTRATION
private JPanel createAdminPanel() {
    JPanel mainPanel = new JPanel(new BorderLayout());
    
    JPanel listsPanel = new JPanel(new GridLayout(1, 3, 20, 20));
    listsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
    
    listsPanel.add(createGestionPanel("Professeurs", "professeurs"));
    listsPanel.add(createGestionPanel("Matières", "matieres"));
    listsPanel.add(createGestionPanel("Filières", "filieres"));
    
    mainPanel.add(listsPanel, BorderLayout.CENTER);
    
    JPanel toolsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    toolsPanel.setBorder(new EmptyBorder(10, 20, 20, 20));
    toolsPanel.setBackground(new Color(240, 242, 245));
    
    JButton btnBackup = new JButton("💾 Sauvegarder Données");
    btnBackup.setBackground(new Color(46, 204, 113));
    btnBackup.setForeground(Color.WHITE);
    btnBackup.addActionListener(e -> sauvegarderBaseDeDonnees());
    
    JButton btnReset = new JButton("Reset/Nouveau Semestre");
    btnReset.setBackground(new Color(192, 57, 43));
    btnReset.setForeground(Color.WHITE);
    btnReset.addActionListener(e -> actionResetSemestre());
    
    JButton btnPublier = new JButton("Publier Planning Web");
    btnPublier.setBackground(new Color(52, 152, 219)); // Bleu
    btnPublier.setForeground(Color.WHITE);

    btnPublier.addActionListener(e -> {

        try {
            List<String> fils = dao.getListe("filieres");
            String filiere = (String) JOptionPane.showInputDialog(this, "Choisir la filière à mettre en ligne :", "Publication Web", JOptionPane.QUESTION_MESSAGE, null, fils.toArray(), fils.get(0));
            
            if (filiere != null) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                
                try {
                        new SyncService().publierPlanning(filiere);
                    JOptionPane.showMessageDialog(this, "L'emploi du temps de " + filiere + " est en ligne.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erreur de publication :\n" + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    });
    
    toolsPanel.add(btnBackup);
    toolsPanel.add(Box.createHorizontalStrut(20));
    toolsPanel.add(btnReset);
    toolsPanel.add(Box.createHorizontalStrut(20));
    toolsPanel.add(btnPublier);
    
    mainPanel.add(toolsPanel, BorderLayout.SOUTH);
    return mainPanel;
}
    
    private void actionResetSemestre() {
        int reponse = JOptionPane.showConfirmDialog(this, 
            "⚠️. DANGER : SUPPRESSION TOTALE\n\n" +
            "Vous allez effacer TOUS les cours du planning.\n" +
            "Les Professeurs et les Filières seront conservés.\n\n" +
            "Voulez-vous vraiment continuer ?", 
            "Confirmation de Reset", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);

        if (reponse == JOptionPane.YES_OPTION) {
            String code = JOptionPane.showInputDialog(this, "Pour confirmer, tapez le mot 'RESET' en majuscules :");
            
            if ("RESET".equals(code)) {
                try {
                    dao.viderPlanning();
                    
                    for (DefaultTableModel model : tableModels.values()) {
                        model.setRowCount(0);
                    }
                    
                    refreshUI();
                    
                    JOptionPane.showMessageDialog(this, "Le planning a été entièrement vidé.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                    
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Erreur lors du reset : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            } else if (code != null) {
                JOptionPane.showMessageDialog(this, "Code incorrect. Action annulée.", "Annulé", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createGestionPanel(String titre, String tableDb) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(null, titre, TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, new Font("Segoe UI", Font.BOLD, 14)));
        
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> list = new JList<>(listModel);
        refreshList(listModel, tableDb);
        
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        JButton btnAdd = new JButton("+");
        JButton btnDel = new JButton("-");
        
        btnAdd.addActionListener(e -> {
            String nom = JOptionPane.showInputDialog(this, "Nouveau " + titre + " :");
            if(nom != null && !nom.trim().isEmpty()) {
                try {
                    dao.ajouterRef(tableDb, nom);
                    refreshList(listModel, tableDb);
                    chargerDonneesReference();
                } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage()); }
            }
        });
        
        btnDel.addActionListener(e -> {
            String val = list.getSelectedValue();
            if(val != null && JOptionPane.showConfirmDialog(this, "Supprimer " + val + " ?", "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    dao.supprimerRef(tableDb, val);
                    refreshList(listModel, tableDb);
                    chargerDonneesReference();
                } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Impossible de supprimer (peut-être utilisé dans un cours)."); }
            }
        });
        
        btnPanel.add(btnAdd);
        btnPanel.add(btnDel);
        p.add(new JScrollPane(list), BorderLayout.CENTER);
        p.add(btnPanel, BorderLayout.SOUTH);
        return p;
    }

    // --- DASHBOARD
    private JPanel createDashboardPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(new Color(245, 245, 250));
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1), new EmptyBorder(30, 40, 30, 40)));
        JLabel lblTitre = new JLabel("Assignation des Cours");
        lblTitre.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitre.setForeground(new Color(40, 60, 100));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        cbFiliere = new JComboBox<>();
        cbSalle = new JComboBox<>(SALLES);
        cbJour = new JComboBox<>(JOURS);
        cbCreneau = new JComboBox<>(CHOIX_HEURES_SEMAINE);
        cbProf = new JComboBox<>();
        cbMatiere = new JComboBox<>();
        
        cbJour.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selectedDay = (String) e.getItem();
                cbCreneau.removeAllItems();
                if ("Samedi".equals(selectedDay)) for(String h : CHOIX_HEURES_SAMEDI) cbCreneau.addItem(h);
                else for(String h : CHOIX_HEURES_SEMAINE) cbCreneau.addItem(h);
            }
        });
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(lblTitre, gbc);
        gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;
        addLabelAndComp(formPanel, "Filière :", cbFiliere, 0, 1, gbc);
        addLabelAndComp(formPanel, "Matière :", cbMatiere, 0, 2, gbc);
        addLabelAndComp(formPanel, "Professeur :", cbProf, 0, 3, gbc);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;
        addLabelAndComp(formPanel, "Jour :", cbJour, 0, 5, gbc);
        addLabelAndComp(formPanel, "Créneau :", cbCreneau, 0, 6, gbc);
        addLabelAndComp(formPanel, "Salle :", cbSalle, 0, 7, gbc);
        btnAction = createStyledButton("Assigner Salle", new Color(33, 150, 243));
        JButton btnCancel = createStyledButton("Annuler / Nouveau", new Color(158, 158, 158));
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(btnCancel);
        btnPanel.add(btnAction);
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);
        btnAction.addActionListener(e -> traitementSauvegarde());
        btnCancel.addActionListener(e -> resetForm());
        wrapper.add(formPanel);
        return wrapper;
    }

    private void traitementSauvegarde() {
        String filiere = (String) cbFiliere.getSelectedItem();
        String prof = (String) cbProf.getSelectedItem();
        String mat = (String) cbMatiere.getSelectedItem();
        String salle = (String) cbSalle.getSelectedItem();
        String jour = (String) cbJour.getSelectedItem();
        String creneau = (String) cbCreneau.getSelectedItem();
        if(prof == null || mat == null || filiere == null) { JOptionPane.showMessageDialog(this, "Veuillez tout sélectionner !"); return; }
        
        try {
            boolean conflitSalle = dao.verifierConflitSalle(salle, jour, creneau);
            
            if (conflitSalle && idCoursEnEdition == null) {
                // 1. Vérifier QUI est dans la salle
                String profEnPlace = dao.getProfDansSalle(salle, jour, creneau);
                
                if (profEnPlace != null && profEnPlace.equals(prof)) {
                    // C'est le même prof -> C'est un cours commun !
                    int rep = JOptionPane.showConfirmDialog(this, 
                        "COURS COMMUN DÉTECTÉ\n\nLe professeur " + prof + " est déjà en salle " + salle + ".\nVoulez-vous ajouter la filière " + filiere + " à ce cours ?", 
                        "Cours en commun", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    
                    if (rep != JOptionPane.YES_OPTION) return;
                    
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "CONFLIT SALLE : La salle " + salle + " est déjà occupée par " + profEnPlace + ".", 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else if (conflitSalle && idCoursEnEdition == null) {
                 return;
            }

            if (dao.verifierConflitProf(prof, jour, creneau) && !conflitSalle) {
                 JOptionPane.showMessageDialog(this, "⛔ " + prof + " a déjà un cours ailleurs !", "Erreur", JOptionPane.ERROR_MESSAGE);
                 return;
            }

            if (idCoursEnEdition == null) {
                dao.ajouterCours(new Cours(0, filiere, prof, mat, salle, jour, creneau));
                JOptionPane.showMessageDialog(this, "Ajouté avec succès !");
            } else {
                dao.modifierCours(new Cours(idCoursEnEdition, filiere, prof, mat, salle, jour, creneau));
                JOptionPane.showMessageDialog(this, "Modifié avec succès !");
                resetForm();
            }
            refreshUI();
        } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Erreur BD: " + ex.getMessage()); }
    }
    
    private void actionModifier(JTabbedPane joursTab) {
        JPanel currentPanel = (JPanel) joursTab.getSelectedComponent();
        JScrollPane scroll = (JScrollPane) currentPanel.getComponent(0);
        JTable table = (JTable) scroll.getViewport().getView();
        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = (int) table.getModel().getValueAt(row, 0);
        String jour = joursTab.getTitleAt(joursTab.getSelectedIndex());
        String creneauReel = (String) table.getModel().getValueAt(row, 2);
        cbJour.setSelectedItem(jour);
        if("Samedi".equals(jour)) { cbCreneau.removeAllItems(); for(String h : CHOIX_HEURES_SAMEDI) cbCreneau.addItem(h); }
        else { cbCreneau.removeAllItems(); for(String h : CHOIX_HEURES_SEMAINE) cbCreneau.addItem(h); }
        cbCreneau.setSelectedItem(creneauReel);
        cbSalle.setSelectedItem(table.getModel().getValueAt(row, 1));
        cbFiliere.setSelectedItem(table.getModel().getValueAt(row, 3));
        cbMatiere.setSelectedItem((String) table.getModel().getValueAt(row, 4));
        cbProf.setSelectedItem((String) table.getModel().getValueAt(row, 5));
        idCoursEnEdition = id;
        btnAction.setText("Enregistrer Modification");
        btnAction.setBackground(new Color(255, 152, 0));
        tabbedPane.setSelectedIndex(1);
    }
    
    private void actionSupprimer(JTabbedPane joursTab) {
        JPanel currentPanel = (JPanel) joursTab.getSelectedComponent();
        JScrollPane scroll = (JScrollPane) currentPanel.getComponent(0);
        JTable table = (JTable) scroll.getViewport().getView();
        int row = table.getSelectedRow();
        if (row != -1) {
            int rep = JOptionPane.showConfirmDialog(this, "Voulez-vous vraiment supprimer ce cours ?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (rep == JOptionPane.YES_OPTION) {
                int id = (int) table.getModel().getValueAt(row, 0);
                try { dao.supprimerCours(id); refreshUI(); } catch(Exception e) { e.printStackTrace(); }
            }
        }
    }
    
    private void refreshUI() { for (String jour : JOURS) { DefaultTableModel model = tableModels.get(jour); if (model != null) chargerDonneesDansTableau(jour, model); } }
    private void chargerDonneesDansTableau(String jour, DefaultTableModel model) {
        model.setRowCount(0);
        try {
            List<Cours> coursList = dao.listerCoursParJour(jour);
            for (Cours c : coursList) model.addRow(new Object[]{c.getId(), c.getSalle(), c.getCreneau(), c.getFiliere(), c.getMatiere(), c.getProfesseur()});
        } catch (SQLException ex) { ex.printStackTrace(); }
    }
    private void resetForm() { idCoursEnEdition = null; btnAction.setText("Assigner Salle"); btnAction.setBackground(new Color(33, 150, 243)); if(cbProf.getItemCount()>0) cbProf.setSelectedIndex(0); if(cbMatiere.getItemCount()>0) cbMatiere.setSelectedIndex(0); }
    private void addLabelAndComp(JPanel p, String text, Component c, int x, int y, GridBagConstraints gbc) { gbc.gridx = x; gbc.gridy = y; JLabel l = new JLabel(text); l.setFont(new Font("Segoe UI", Font.BOLD, 12)); p.add(l, gbc); gbc.gridx = x + 1; p.add(c, gbc); }
    private JButton createStyledButton(String text, Color bg) { JButton btn = new JButton(text); btn.setBackground(bg); btn.setForeground(Color.WHITE); btn.setFocusPainted(false); btn.setFont(new Font("Segoe UI", Font.BOLD, 13)); return btn; }
    
    private void sauvegarderBaseDeDonnees() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("backup_istime.db"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.nio.file.Files.copy(new File("istime.db").toPath(), fileChooser.getSelectedFile().toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(this, "Sauvegarde réussie !");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage()); }
        }
    }
    
    private void refreshList(DefaultListModel<String> model, String tableDb) {
        model.clear();
        try {
            List<String> data = dao.getListe(tableDb);
            for(String s : data) model.addElement(s);
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    private void actionRecherche(String type, String valeur) {
        if(valeur.trim().isEmpty()) return;
        JDialog resultDialog = new JDialog(this, "Résultats : " + valeur, true);
        resultDialog.setSize(900, 500);
        resultDialog.setLocationRelativeTo(this);
        String[] cols = {"Jour", "Heure", "Salle", "Filière", "Matière", "Professeur"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        try {
            List<Cours> resultats = dao.rechercherGlobal(type.toLowerCase(), valeur);
            for(Cours c : resultats) model.addRow(new Object[]{c.getJour(), c.getCreneau(), c.getSalle(), c.getFiliere(), c.getMatiere(), c.getProfesseur()});
        } catch(SQLException ex) { ex.printStackTrace(); }
        resultDialog.add(new JScrollPane(table));
        resultDialog.setVisible(true);
    }

    private void exportPDFDialog() {
        try {
            List<String> fils = dao.getListe("filieres");
            String filiere = (String) JOptionPane.showInputDialog(this, "Choisir la filière :", "Export PDF", JOptionPane.QUESTION_MESSAGE, null, fils.toArray(), fils.get(0));
            if (filiere != null) genererPDFGrille(filiere);
        } catch(SQLException e) { e.printStackTrace(); }
    }

    private void genererPDFGrille(String filiere) {
        String dest = "Planning_" + filiere.replace(" ", "_").replace("/", "-") + ".pdf";
        try {
            PdfWriter writer = new PdfWriter(dest);
            PdfDocument pdf = new PdfDocument(writer);
            pdf.setDefaultPageSize(PageSize.A4.rotate());
            Document document = new Document(pdf);
            document.setMargins(20, 20, 20, 20);
            Table headerTable = new Table(new float[]{1, 4}).useAllAvailableWidth();
            String logoPath = "logo.png";
            if (new File(logoPath).exists()) {
                ImageData data = ImageDataFactory.create(logoPath);
                Image img = new Image(data).scaleToFit(80, 80);
                headerTable.addCell(new Cell().add(img).setBorder(null));
            } else { headerTable.addCell(new Cell().add(new Paragraph("TechnoLab")).setBorder(null)); }
            Paragraph headerText = new Paragraph().add("TechnoLAB-ISTA\n").setBold().setFontSize(14).add("2025 - 2026\n\n").setFontSize(12).add("EMPLOI DU TEMPS - SEMESTRE 3\n").setBold().setFontSize(16).add(filiere + " - SEVARE");
            headerTable.addCell(new Cell().add(headerText).setBorder(null).setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE));
            document.add(headerTable);
            document.add(new Paragraph("\n"));
            float[] colWidths = {2, 3, 3, 3, 3, 3, 3}; 
            Table table = new Table(UnitValue.createPercentArray(colWidths)).useAllAvailableWidth();
            com.itextpdf.kernel.colors.Color headerColor = new DeviceRgb(240, 240, 240);
            table.addHeaderCell(createHeaderCell("Horaires", headerColor));
            for(String j : JOURS) table.addHeaderCell(createHeaderCell(j, headerColor));
            List<Cours> coursFiliere = dao.listerCoursParFiliere(filiere);
            for (int i = 0; i < CRENEAUX_LIGNES.length; i++) {
                String creneauPrincipal = CRENEAUX_LIGNES[i];
                Cell cellHeure = new Cell().add(new Paragraph(creneauPrincipal).setBold()).setVerticalAlignment(VerticalAlignment.MIDDLE).setTextAlignment(TextAlignment.CENTER).setMinHeight(60); 
                table.addCell(cellHeure);
                for (String jour : JOURS) {
                    Cours coursTrouve = null;
                    for (Cours c : coursFiliere) {
                        if (c.getJour().equals(jour)) {
                            if (!jour.equals("Samedi") && c.getCreneau().equals(creneauPrincipal)) { coursTrouve = c; break; }
                            if (jour.equals("Samedi")) {
                                if (i == 0 && c.getCreneau().equals("09h – 11h")) { coursTrouve = c; break; }
                                if (i == 1 && c.getCreneau().equals("11h – 13h")) { coursTrouve = c; break; }
                            }
                        }
                    }
                    Cell c = new Cell().setVerticalAlignment(VerticalAlignment.MIDDLE).setTextAlignment(TextAlignment.CENTER);
                    if (coursTrouve != null) {
                        Paragraph p = new Paragraph();
                        p.add(new com.itextpdf.layout.element.Text(coursTrouve.getMatiere() + "\n").setBold());
                        if(jour.equals("Samedi")) p.add(new com.itextpdf.layout.element.Text(coursTrouve.getCreneau() + "\n").setFontSize(9));
                        p.add("SALLE " + coursTrouve.getSalle() + "\n");
                        p.add(new com.itextpdf.layout.element.Text(coursTrouve.getProfesseur()).setItalic());
                        
                        try {
                            String communs = dao.getFilieresEnCommun(coursTrouve.getSalle(), coursTrouve.getJour(), coursTrouve.getCreneau());
                            String filiereCouranteShort = filiere.replace("DUT ", "").replace("Licence ", "L.");
                            if (communs.length() > filiereCouranteShort.length() + 5) {
                                p.add(new com.itextpdf.layout.element.Text("\n(Classes: " + communs + ")").setFontSize(8).setFontColor(com.itextpdf.kernel.colors.ColorConstants.DARK_GRAY));
                            }
                        } catch (SQLException ex) {}

                        c.add(p);
                    }
                    table.addCell(c);
                }
            }
            document.add(table);
            document.close();
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(new File(dest));
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Erreur PDF: " + e.getMessage()); }
    }
    
    private Cell createHeaderCell(String text, com.itextpdf.kernel.colors.Color bg) { return new Cell().add(new Paragraph(text)).setBold().setBackgroundColor(bg).setTextAlignment(TextAlignment.CENTER).setBorder(new SolidBorder(ColorConstants.BLACK, 1)); }
    private void afficherStatistiques() { try { Map<String, Integer> volumes = dao.getVolumeHoraire(); StringBuilder message = new StringBuilder("Volume Horaire par Professeur :\n\n"); for (Map.Entry<String, Integer> entry : volumes.entrySet()) { message.append(String.format("- %-20s : %d Heures\n", entry.getKey(), entry.getValue())); } JTextArea textArea = new JTextArea(message.toString()); textArea.setEditable(false); textArea.setFont(new Font("Monospaced", Font.PLAIN, 14)); JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Statistiques Professeurs", JOptionPane.INFORMATION_MESSAGE); } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Erreur Stats: " + ex.getMessage()); } }

    public static void main(String[] args) {
        try { FlatLightLaf.setup(); } catch( Exception ex ) { }
        SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
    }
}
