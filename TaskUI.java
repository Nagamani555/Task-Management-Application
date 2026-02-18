package com.taskmanager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;

// --- 1. TASK SERVICE ---
class TaskService {
    private ArrayList<Task> tasks = new ArrayList<>();
    private int autoCounter = 1;
    private final String FILE_NAME = "tasks.txt";

    public TaskService() { loadFromFile(); }

    public void addTask(int id, String title) {
        tasks.add(new Task(id, title, "PENDING"));
        if (id >= autoCounter) autoCounter = id + 1;
    }

    public void updateTask(int id, String status) {
        for (Task t : tasks) { if (t.id == id) { t.status = status; break; } }
    }

    public void deleteTask(int id) { tasks.removeIf(t -> t.id == id); }

    public void clearAll() {
        tasks.clear();
        autoCounter = 1;
    }

    // --- FILE I/O ---
    public void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Task t : tasks) writer.println(t.id + "|" + t.title + "|" + t.status);
            System.out.println("Saved successfully to " + FILE_NAME);
        } catch (IOException e) { System.out.println("Save Error: " + e.getMessage()); }
    }

    private void loadFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 3) {
                    int id = Integer.parseInt(parts[0]);
                    tasks.add(new Task(id, parts[1], parts[2]));
                    if (id >= autoCounter) autoCounter = id + 1;
                }
            }
        } catch (IOException e) { System.out.println("Load Error: " + e.getMessage()); }
    }

    public ArrayList<Task> getTasks() { return tasks; }
    public int getTotal() { return tasks.size(); }
    public long getActive() { return tasks.stream().filter(t -> !t.status.equals("COMPLETED")).count(); }
    public long getCompleted() { return tasks.stream().filter(t -> t.status.equals("COMPLETED")).count(); }
    public boolean taskExists(int id) { return tasks.stream().anyMatch(t -> t.id == id); }
    public int getNextAutoId() { return autoCounter; }
}

// --- 2. UPDATED UI WITH SAVE BUTTON ---
public class TaskUI extends JFrame {
    TaskService service = new TaskService();
    JTextField titleInput, idInput, searchField;
    JLabel totalLbl, activeLbl, completedLbl;
    DefaultTableModel model;
    JTable table;
    TableRowSorter<DefaultTableModel> sorter;
    JComboBox<String> statusBox;

    public TaskUI() {
        setTitle("Professional Task Dashboard v4.5 (Manual Save)");
        setSize(1150, 850);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(new Color(245, 247, 250));

        // --- DASHBOARD CARDS ---
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        totalLbl = createStatCard("📊 Total Tasks: 0", new Color(51, 102, 204));
        activeLbl = createStatCard("⏳ Active: 0", new Color(255, 153, 0));
        completedLbl = createStatCard("✅ Completed: 0", new Color(0, 153, 51));
        statsPanel.add(totalLbl); statsPanel.add(activeLbl); statsPanel.add(completedLbl);
        add(statsPanel, BorderLayout.NORTH);

        // --- CENTER TABLE ---
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 10));
        searchField = new JTextField();
        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.add(new JLabel("🔍 Search:"), BorderLayout.WEST);
        searchBar.add(searchField, BorderLayout.CENTER);
        centerPanel.add(searchBar, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"ID", "Title", "Status"}, 0);
        table = new JTable(model);
        table.setRowHeight(40);
        
        // Row Coloring
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                String status = t.getValueAt(r, 2).toString();
                if (!isS) {
                    if (status.equals("PENDING")) comp.setBackground(new Color(255, 245, 200));
                    else if (status.equals("IN PROGRESS")) comp.setBackground(new Color(210, 230, 255));
                    else comp.setBackground(new Color(200, 255, 200));
                }
                return comp;
            }
        });

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // --- SIDEBAR ---
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setPreferredSize(new Dimension(340, 0));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 20));

        idInput = new JTextField(); 
        titleInput = new JTextField();
        statusBox = new JComboBox<>(new String[]{"PENDING", "IN PROGRESS", "COMPLETED"});

        addControl(sidePanel, "Task ID (Manual Entry):", idInput);
        addControl(sidePanel, "Task Title:", titleInput);
        addControl(sidePanel, "Change Status:", statusBox);

        JButton addBtn = createBtn("➕ Add Task", new Color(46, 204, 113));
        JButton updateBtn = createBtn("🔄 Update Status", new Color(52, 152, 219));
        JButton deleteBtn = createBtn("🗑 Delete Task", new Color(231, 76, 60));
        JButton saveBtn = createBtn("💾 SAVE DATA", new Color(142, 68, 173)); // Purple Save Button
        JButton clearBtn = createBtn("🧨 Clear All Data", Color.DARK_GRAY);

        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(addBtn); sidePanel.add(Box.createVerticalStrut(10));
        sidePanel.add(updateBtn); sidePanel.add(Box.createVerticalStrut(10));
        sidePanel.add(deleteBtn); sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(saveBtn); sidePanel.add(Box.createVerticalStrut(10));
        sidePanel.add(clearBtn);
        add(sidePanel, BorderLayout.EAST);

        // --- BUTTON ACTIONS ---
        addBtn.addActionListener(e -> {
            try {
                int id = idInput.getText().trim().isEmpty() ? service.getNextAutoId() : Integer.parseInt(idInput.getText().trim());
                if (service.taskExists(id)) { JOptionPane.showMessageDialog(this, "ID Already Exists!"); return; }
                service.addTask(id, titleInput.getText().trim());
                refreshUI(); clearInputs();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Enter Valid ID/Title!"); }
        });

        updateBtn.addActionListener(e -> {
            try {
                service.updateTask(Integer.parseInt(idInput.getText()), statusBox.getSelectedItem().toString());
                refreshUI();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Select a task first!"); }
        });

        deleteBtn.addActionListener(e -> {
            try {
                service.deleteTask(Integer.parseInt(idInput.getText()));
                refreshUI(); clearInputs();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Select a task first!"); }
        });

        saveBtn.addActionListener(e -> {
            service.saveToFile();
            JOptionPane.showMessageDialog(this, "Data Saved Successfully to tasks.txt!");
        });

        clearBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "This will delete EVERYTHING. Proceed?");
            if (confirm == JOptionPane.YES_OPTION) { service.clearAll(); refreshUI(); clearInputs(); }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int r = table.convertRowIndexToModel(table.getSelectedRow());
                idInput.setText(model.getValueAt(r, 0).toString());
                titleInput.setText(model.getValueAt(r, 1).toString());
                statusBox.setSelectedItem(model.getValueAt(r, 2).toString());
            }
        });

        refreshUI();
        setVisible(true);
    }

    private void refreshUI() {
        model.setRowCount(0);
        for (Task t : service.getTasks()) model.addRow(new Object[]{t.id, t.title, t.status});
        totalLbl.setText("📊 Total Tasks: " + service.getTotal());
        activeLbl.setText("⏳ Active: " + service.getActive());
        completedLbl.setText("✅ Completed: " + service.getCompleted());
    }

    private void clearInputs() { idInput.setText(""); titleInput.setText(""); }
    private JLabel createStatCard(String t, Color b) {
        JLabel l = new JLabel(t, 0); l.setFont(new Font("SansSerif", 1, 22));
        l.setOpaque(true); l.setBackground(b); l.setForeground(Color.WHITE);
        l.setBorder(BorderFactory.createEmptyBorder(25, 10, 25, 10));
        return l;
    }
    private void addControl(JPanel p, String l, JComponent c) {
        JLabel label = new JLabel(l); label.setFont(new Font("SansSerif", 1, 14));
        p.add(label); p.add(c); p.add(Box.createVerticalStrut(10));
    }
    private JButton createBtn(String t, Color b) {
        JButton btn = new JButton(t); btn.setBackground(b); btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", 1, 14));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45)); return btn;
    }
    public static void main(String[] args) { SwingUtilities.invokeLater(TaskUI::new); }
}