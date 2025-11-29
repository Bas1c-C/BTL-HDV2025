package com.mycompany.dashboard;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

// Class Student
class Student {
    private Long id;
    private String name;
    private String email;
    private String phone;

    public Student() {}
    public Student(Long id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}

// Panel quản lý học viên
class StudentManagementPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton, refreshButton;
    private HttpClient httpClient;
    private Gson gson;
    private final String USERNAME = "admin";
    private final String PASSWORD = "admin123";
    private JFrame parentFrame;

    public StudentManagementPanel(JFrame parent) {
        this.parentFrame = parent;
        httpClient = HttpClient.newHttpClient();
        gson = new Gson();
        
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(236, 240, 241));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(236, 240, 241));
        JLabel titleLabel = new JLabel("Quản Lý Học Viên");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel, BorderLayout.WEST);
        add(titlePanel, BorderLayout.NORTH);
        
        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        tableModel = new DefaultTableModel(new String[]{"ID", "Tên", "Email", "Số Điện Thoại"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(52, 152, 219));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(52, 152, 219));
        table.setSelectionForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setBackground(new Color(236, 240, 241));
        
        addButton = createStyledButton("➕ Thêm Học Viên", new Color(46, 204, 113));
        editButton = createStyledButton("✏️ Sửa Học Viên", new Color(52, 152, 219));
        deleteButton = createStyledButton("🗑️ Xóa Học Viên", new Color(231, 76, 60));
        refreshButton = createStyledButton("🔄 Làm Mới", new Color(149, 165, 166));
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Action listeners
        addButton.addActionListener(e -> addStudent());
        editButton.addActionListener(e -> editStudent());
        deleteButton.addActionListener(e -> deleteStudent());
        refreshButton.addActionListener(e -> loadStudents());
        
        loadStudents();
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 35));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    private String createBasicAuthHeader() {
        String auth = USERNAME + ":" + PASSWORD;
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
    }
    
    private void loadStudents() {
        try {
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/students"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", createBasicAuthHeader())
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                List<Student> students = gson.fromJson(response.body(), new TypeToken<List<Student>>(){}.getType());
                tableModel.setRowCount(0);
                for (Student s : students) {
                    tableModel.addRow(new Object[]{s.getId(), s.getName(), s.getEmail(), s.getPhone()});
                }
            } else {
                JOptionPane.showMessageDialog(parentFrame, "Lỗi tải dữ liệu: " + response.statusCode(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | InterruptedException e) {
            JOptionPane.showMessageDialog(parentFrame, "Lỗi kết nối: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addStudent() {
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.add(new JLabel("Tên:"));
        panel.add(nameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Số điện thoại:"));
        panel.add(phoneField);
        
        int option = JOptionPane.showConfirmDialog(parentFrame, panel, "Thêm Học Viên", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            
            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(parentFrame, "Vui lòng điền đầy đủ thông tin!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            Student newStudent = new Student(null, name, email, phone);
            try {
                String json = gson.toJson(newStudent);
                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/students"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", createBasicAuthHeader())
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200 || response.statusCode() == 201) {
                    loadStudents();
                    JOptionPane.showMessageDialog(parentFrame, "Thêm học viên thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "Lỗi thêm: " + response.statusCode(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException | InterruptedException ex) {
                JOptionPane.showMessageDialog(parentFrame, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void editStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentFrame, "Vui lòng chọn một học viên để sửa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);
        String email = (String) tableModel.getValueAt(selectedRow, 2);
        String phone = (String) tableModel.getValueAt(selectedRow, 3);

        JTextField nameField = new JTextField(name);
        JTextField emailField = new JTextField(email);
        JTextField phoneField = new JTextField(phone);
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.add(new JLabel("Tên:"));
        panel.add(nameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Số điện thoại:"));
        panel.add(phoneField);
        
        int option = JOptionPane.showConfirmDialog(parentFrame, panel, "Sửa Học Viên", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            Student updatedStudent = new Student(id, nameField.getText(), emailField.getText(), phoneField.getText());
            try {
                String json = gson.toJson(updatedStudent);
                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/students/" + id))
                        .header("Content-Type", "application/json")
                        .header("Authorization", createBasicAuthHeader())
                        .PUT(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    loadStudents();
                    JOptionPane.showMessageDialog(parentFrame, "Sửa thông tin thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "Lỗi sửa: " + response.statusCode(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException | InterruptedException ex) {
                JOptionPane.showMessageDialog(parentFrame, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentFrame, "Vui lòng chọn một học viên để xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(parentFrame, 
            "Bạn có chắc muốn xóa học viên \"" + name + "\"?", 
            "Xác nhận xóa", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/students/" + id))
                        .header("Authorization", createBasicAuthHeader())
                        .DELETE()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200 || response.statusCode() == 204) {
                    loadStudents();
                    JOptionPane.showMessageDialog(parentFrame, "Xóa học viên thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "Lỗi xóa: " + response.statusCode(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException | InterruptedException ex) {
                JOptionPane.showMessageDialog(parentFrame, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

// Dashboard Frame chính
public class DashboardFrame extends JFrame {
    private JPanel contentPanel;
    private Color bgColor = new Color(236, 240, 241);
    
    public DashboardFrame() {
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Hệ Thống Quản Lý Học Viên - Lớp Anh Ngữ");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Sidebar
        JPanel sidebar = createSidebar();
        mainPanel.add(sidebar, BorderLayout.WEST);
        
        // Top bar
        JPanel topBar = createTopBar();
        mainPanel.add(topBar, BorderLayout.NORTH);
        
        // Content area
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(bgColor);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        showDashboardContent();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(44, 62, 80));
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBorder(new EmptyBorder(20, 10, 20, 10));
        
        JLabel titleLabel = new JLabel("QUẢN LÝ HỌC VIÊN");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(new EmptyBorder(10, 0, 30, 0));
        sidebar.add(titleLabel);
        
        addMenuItem(sidebar, "📊 Dashboard", e -> showDashboardContent());
        addMenuItem(sidebar, "👥 Quản Lý Học Viên", e -> showStudentManagement());
        addMenuItem(sidebar, "📚 Quản Lý Khóa Học", e -> showCourseManagement());
        addMenuItem(sidebar, "💰 Học Phí", e -> showTuitionManagement());
        addMenuItem(sidebar, "📈 Báo Cáo", e -> showReports());
        addMenuItem(sidebar, "⚙️ Cài Đặt", e -> showSettings());
        
        sidebar.add(Box.createVerticalGlue());
        
        JButton logoutBtn = createMenuButton("🚪 Đăng Xuất");
        logoutBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc chắn muốn đăng xuất?", 
                "Xác nhận", 
                JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                dispose();
            }
        });
        sidebar.add(logoutBtn);
        
        return sidebar;
    }
    
    private void addMenuItem(JPanel sidebar, String text, ActionListener action) {
        JButton btn = createMenuButton(text);
        btn.addActionListener(action);
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(5));
    }
    
    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.PLAIN, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(44, 62, 80));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(230, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(52, 73, 94));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(44, 62, 80));
            }
        });
        
        return btn;
    }
    
    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)),
            new EmptyBorder(10, 20, 10, 20)
        ));
        
        JLabel welcomeLabel = new JLabel("Xin chào, Admin!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topBar.add(welcomeLabel, BorderLayout.WEST);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(Color.WHITE);
        
        JButton notificationBtn = new JButton("🔔");
        notificationBtn.setFont(new Font("Arial", Font.PLAIN, 18));
        notificationBtn.setBorderPainted(false);
        notificationBtn.setContentAreaFilled(false);
        notificationBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        notificationBtn.setToolTipText("Thông báo");
        
        JButton profileBtn = new JButton("👤");
        profileBtn.setFont(new Font("Arial", Font.PLAIN, 18));
        profileBtn.setBorderPainted(false);
        profileBtn.setContentAreaFilled(false);
        profileBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profileBtn.setToolTipText("Tài khoản");
        
        rightPanel.add(notificationBtn);
        rightPanel.add(profileBtn);
        topBar.add(rightPanel, BorderLayout.EAST);
        
        return topBar;
    }
    
    private void showDashboardContent() {
        contentPanel.removeAll();
        
        JPanel dashPanel = new JPanel(new BorderLayout());
        dashPanel.setBackground(bgColor);
        
        JLabel titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        dashPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setBackground(bgColor);
        
        statsPanel.add(createStatCard("Tổng Học Viên", "Đang cập nhật", "👥", new Color(52, 152, 219)));
        statsPanel.add(createStatCard("Khóa Học", "Đang cập nhật", "📚", new Color(46, 204, 113)));
        statsPanel.add(createStatCard("Doanh Thu Tháng", "Đang cập nhật", "💰", new Color(241, 196, 15)));
        statsPanel.add(createStatCard("Học Viên Mới", "Đang cập nhật", "📈", new Color(155, 89, 182)));
        
        dashPanel.add(statsPanel, BorderLayout.CENTER);
        
        contentPanel.add(dashPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private JPanel createStatCard(String title, String value, String icon, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 40));
        iconLabel.setForeground(color);
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Arial", Font.PLAIN, 14));
        titleLbl.setForeground(new Color(127, 140, 141));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 28));
        valueLabel.setForeground(new Color(44, 62, 80));
        
        textPanel.add(titleLbl);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(valueLabel);
        
        card.add(iconLabel, BorderLayout.WEST);
        card.add(Box.createHorizontalStrut(15), BorderLayout.CENTER);
        card.add(textPanel, BorderLayout.EAST);
        
        return card;
    }
    
    private void showStudentManagement() {
        contentPanel.removeAll();
        StudentManagementPanel studentPanel = new StudentManagementPanel(this);
        contentPanel.add(studentPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void showCourseManagement() {
        showPlaceholder("Quản Lý Khóa Học");
    }
    
    private void showTuitionManagement() {
        showPlaceholder("Quản Lý Học Phí");
    }
    
    private void showReports() {
        showPlaceholder("Báo Cáo Thống Kê");
    }
    
    private void showSettings() {
        showPlaceholder("Cài Đặt Hệ Thống");
    }
    
    private void showPlaceholder(String title) {
        contentPanel.removeAll();
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bgColor);
        
        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(label, BorderLayout.NORTH);
        
        JLabel placeholder = new JLabel("Chức năng đang được phát triển...");
        placeholder.setFont(new Font("Arial", Font.PLAIN, 16));
        placeholder.setForeground(new Color(127, 140, 141));
        placeholder.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(placeholder, BorderLayout.CENTER);
        
        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            DashboardFrame dashboard = new DashboardFrame();
            dashboard.setVisible(true);
        });
    }
}