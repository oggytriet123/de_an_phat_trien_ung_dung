package GUI;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

public class LichHenPanel extends JPanel {
    public LichHenPanel() {
        // Cài đặt nền chuẩn theo UI Style Guide
        setBackground(Color.decode("#F4F7F9"));
        setLayout(new BorderLayout(24, 24));
        setBorder(new EmptyBorder(24, 24, 24, 24));

        // 1. TOP BAR (Tiêu đề và Nút thao tác)
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        
        JLabel title = new JLabel("Lịch hẹn & Phân bổ dịch vụ");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.decode("#263238"));
        
        JButton btnAdd = new JButton("+ Tạo lịch hẹn");
        btnAdd.setBackground(Color.decode("#607D8B"));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setFont(new Font("Arial", Font.BOLD, 14));
        
        topBar.add(title, BorderLayout.WEST);
        topBar.add(btnAdd, BorderLayout.EAST);

        // 2. MAIN CONTENT (Chứa Lưới lịch và Sơ đồ lồng)
        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setOpaque(false);

        // MODULE 1: TIME-GRID (Ma trận lịch hẹn)
        JPanel timeGrid = new JPanel(new BorderLayout());
        timeGrid.setBackground(Color.WHITE);
        timeGrid.setPreferredSize(new Dimension(1000, 400));
        timeGrid.setBorder(BorderFactory.createLineBorder(Color.decode("#CFD8DC")));
        
        JLabel gridLabel = new JLabel("Khu vực Lưới Lịch Hẹn (Time-Grid) - Sẽ tích hợp kéo thả (Drag & Drop) tại đây", SwingConstants.CENTER);
        gridLabel.setForeground(Color.decode("#78909C"));
        timeGrid.add(gridLabel, BorderLayout.CENTER);

        // MODULE 2: PET HOTEL (Sơ đồ lồng lưu trú)
        JPanel petHotel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));
        petHotel.setBackground(Color.WHITE);
        petHotel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.decode("#CFD8DC")), 
            "Sơ đồ lưu trú (Pet Hotel)"
        ));
        
        // Tạo các lồng mẫu theo trạng thái
        JPanel cage1 = createCage("VIP 02 - Trống", "#F4F7F9", "#78909C", false);
        JPanel cage2 = createCage("Mochi (Dị ứng)", "#FFFFFF", "#E53935", true);
        
        petHotel.add(cage1);
        petHotel.add(cage2);

        // Lắp ráp các khối
        centerContent.add(timeGrid);
        centerContent.add(Box.createRigidArea(new Dimension(0, 24)));
        centerContent.add(petHotel);

        add(topBar, BorderLayout.NORTH);
        add(centerContent, BorderLayout.CENTER);
    }

    // Hàm tiện ích tạo khối Lồng thú cưng
    private JPanel createCage(String text, String bgColor, String fgColor, boolean hasAlert) {
        JPanel cage = new JPanel(new BorderLayout());
        cage.setPreferredSize(new Dimension(150, 120));
        cage.setBackground(Color.decode(bgColor));
        
        if (hasAlert) {
            cage.setBorder(BorderFactory.createLineBorder(Color.decode(fgColor), 2));
        } else {
            cage.setBorder(BorderFactory.createLineBorder(Color.decode("#CFD8DC"), 1));
        }
        
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(Color.decode(fgColor));
        label.setFont(new Font("Arial", Font.BOLD, 14));
        cage.add(label, BorderLayout.CENTER);
        
        return cage;
    }
}
