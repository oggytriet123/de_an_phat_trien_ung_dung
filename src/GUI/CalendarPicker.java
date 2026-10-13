package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Component Popup Cuốn lịch chọn ngày (Date Picker) bằng Java Swing thuần & Java2D.
 * Hỗ trợ 2 chế độ:
 * 1. Chọn ngày đơn (Single Date) cho Form nhập liệu.
 * 2. Chọn khoảng ngày (Date Range: Từ ngày — Đến ngày) cho Bộ lọc thời gian.
 */
public class CalendarPicker extends JPanel {
    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] WEEKDAYS = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

    private final boolean rangeMode;
    private final Consumer<LocalDate> onSingleSelect;
    private final BiConsumer<LocalDate, LocalDate> onRangeSelect;
    private final Runnable onClose;

    private YearMonth currentMonth;
    private LocalDate singleSelected;
    private LocalDate rangeStart;
    private LocalDate rangeEnd;
    private boolean pickingStart = true;

    private JLabel lblMonthYear;
    private JPanel gridDays;
    private JLabel lblRangeInfo;

    // ---------------- Factory Methods ----------------

    /** Hiển thị Popup chọn ngày đơn dưới invoker component. */
    public static void showDatePicker(Component invoker, LocalDate initialDate, Consumer<LocalDate> onSelect) {
        JPopupMenu popup = createPopup();
        CalendarPicker picker = new CalendarPicker(initialDate, onSelect, popup::setVisible);
        popup.add(picker);
        popup.show(invoker, 0, invoker.getHeight() + 4);
    }

    /** Hiển thị Popup chọn khoảng ngày (Từ ngày - Đến ngày) dưới invoker component. */
    public static void showDateRangePicker(Component invoker, LocalDate start, LocalDate end, BiConsumer<LocalDate, LocalDate> onSelect) {
        JPopupMenu popup = createPopup();
        CalendarPicker picker = new CalendarPicker(start, end, onSelect, popup::setVisible);
        popup.add(picker);
        popup.show(invoker, 0, invoker.getHeight() + 4);
    }

    /** Tạo CalendarPicker component chế độ ngày đơn (dùng nhúng hoặc kiểm thử). */
    public static CalendarPicker createDatePickerPanel(LocalDate initialDate, Consumer<LocalDate> onSelect) {
        return new CalendarPicker(initialDate, onSelect, b -> {});
    }

    /** Tạo CalendarPicker component chế độ khoảng ngày (dùng nhúng hoặc kiểm thử). */
    public static CalendarPicker createDateRangePickerPanel(LocalDate start, LocalDate end, BiConsumer<LocalDate, LocalDate> onSelect) {
        return new CalendarPicker(start, end, onSelect, b -> {});
    }

    private static JPopupMenu createPopup() {
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(new LineBorder(Theme.BORDER, 1, true));
        popup.setBackground(Color.WHITE);
        popup.setOpaque(true);
        return popup;
    }

    // ---------------- Constructors ----------------

    /** Constructor chế độ chọn ngày đơn. */
    private CalendarPicker(LocalDate initialDate, Consumer<LocalDate> onSelect, Consumer<Boolean> setVisible) {
        this.rangeMode = false;
        this.onSingleSelect = onSelect;
        this.onRangeSelect = null;
        this.onClose = () -> setVisible.accept(false);

        this.singleSelected = (initialDate != null) ? initialDate : LocalDate.now();
        this.currentMonth = YearMonth.from(this.singleSelected);

        initUI();
    }

    /** Constructor chế độ chọn khoảng ngày. */
    private CalendarPicker(LocalDate start, LocalDate end, BiConsumer<LocalDate, LocalDate> onSelect, Consumer<Boolean> setVisible) {
        this.rangeMode = true;
        this.onSingleSelect = null;
        this.onRangeSelect = onSelect;
        this.onClose = () -> setVisible.accept(false);

        this.rangeStart = (start != null) ? start : LocalDate.now().minusDays(3);
        this.rangeEnd = (end != null) ? end : LocalDate.now();
        if (this.rangeStart.isAfter(this.rangeEnd)) {
            LocalDate tmp = this.rangeStart;
            this.rangeStart = this.rangeEnd;
            this.rangeEnd = tmp;
        }
        this.currentMonth = YearMonth.from(this.rangeEnd);

        initUI();
    }

    // ---------------- UI Initialization ----------------

    private void initUI() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(14, 16, 14, 16));
        setPreferredSize(new Dimension(rangeMode ? 310 : 290, rangeMode ? 360 : 320));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCalendarBody(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        refreshCalendar();
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JButton btnPrev = navBtn("‹", () -> {
            currentMonth = currentMonth.minusMonths(1);
            refreshCalendar();
        });

        JButton btnNext = navBtn("›", () -> {
            currentMonth = currentMonth.plusMonths(1);
            refreshCalendar();
        });

        lblMonthYear = new JLabel("", SwingConstants.CENTER);
        lblMonthYear.setFont(Theme.font(Font.BOLD, 14));
        lblMonthYear.setForeground(Theme.TEXT);

        header.add(btnPrev, BorderLayout.WEST);
        header.add(lblMonthYear, BorderLayout.CENTER);
        header.add(btnNext, BorderLayout.EAST);
        return header;
    }

    private JComponent buildCalendarBody() {
        JPanel body = new JPanel(new BorderLayout(0, 6));
        body.setOpaque(false);

        // Hàng thứ trong tuần: T2 -> CN
        JPanel rowWeekdays = new JPanel(new GridLayout(1, 7, 2, 0));
        rowWeekdays.setOpaque(false);
        for (String wd : WEEKDAYS) {
            JLabel lbl = new JLabel(wd, SwingConstants.CENTER);
            lbl.setFont(Theme.font(Font.BOLD, 11));
            lbl.setForeground(Theme.MUTED);
            lbl.setPreferredSize(new Dimension(34, 22));
            rowWeekdays.add(lbl);
        }

        // Lưới 42 ngày (6 tuần x 7 ngày)
        gridDays = new JPanel(new GridLayout(6, 7, 2, 2));
        gridDays.setOpaque(false);

        body.add(rowWeekdays, BorderLayout.NORTH);
        body.add(gridDays, BorderLayout.CENTER);
        return body;
    }

    private JComponent buildFooter() {
        if (!rangeMode) {
            JPanel foot = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            foot.setOpaque(false);

            RoundedButton btnToday = new RoundedButton("Hôm nay", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 6).pad(14, 6);
            btnToday.setFont(Theme.font(Font.PLAIN, 12));
            btnToday.addActionListener(e -> {
                LocalDate today = LocalDate.now();
                if (onSingleSelect != null) onSingleSelect.accept(today);
                onClose.run();
            });

            RoundedButton btnCancel = new RoundedButton("Đóng", Color.WHITE, new Color(0xF3F6F8), Theme.MUTED, Theme.BORDER, 6).pad(14, 6);
            btnCancel.setFont(Theme.font(Font.PLAIN, 12));
            btnCancel.addActionListener(e -> onClose.run());

            foot.add(btnToday);
            foot.add(btnCancel);
            return foot;
        } else {
            JPanel foot = new JPanel(new BorderLayout(0, 8));
            foot.setOpaque(false);

            lblRangeInfo = new JLabel("", SwingConstants.CENTER);
            lblRangeInfo.setFont(Theme.font(Font.PLAIN, 11));
            lblRangeInfo.setForeground(Theme.MUTED);

            JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            pnlBtns.setOpaque(false);

            RoundedButton btnCancel = new RoundedButton("Hủy", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 6).pad(12, 6);
            btnCancel.setFont(Theme.font(Font.PLAIN, 12));
            btnCancel.addActionListener(e -> onClose.run());

            RoundedButton btnApply = new RoundedButton("Áp dụng", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 6).pad(14, 6);
            btnApply.setFont(Theme.font(Font.BOLD, 12));
            btnApply.addActionListener(e -> {
                if (rangeStart != null && rangeEnd != null && onRangeSelect != null) {
                    LocalDate start = rangeStart.isBefore(rangeEnd) ? rangeStart : rangeEnd;
                    LocalDate end = rangeStart.isBefore(rangeEnd) ? rangeEnd : rangeStart;
                    onRangeSelect.accept(start, end);
                }
                onClose.run();
            });

            pnlBtns.add(btnCancel);
            pnlBtns.add(btnApply);

            foot.add(lblRangeInfo, BorderLayout.NORTH);
            foot.add(pnlBtns, BorderLayout.SOUTH);
            return foot;
        }
    }

    // ---------------- Refresh Logic ----------------

    private void refreshCalendar() {
        lblMonthYear.setText("Tháng " + currentMonth.getMonthValue() + ", " + currentMonth.getYear());

        gridDays.removeAll();

        LocalDate firstDay = currentMonth.atDay(1);
        int dayOfWeekVal = firstDay.getDayOfWeek().getValue(); // 1 = T2, 7 = CN
        LocalDate startGrid = firstDay.minusDays(dayOfWeekVal - 1);

        LocalDate today = LocalDate.now();

        for (int i = 0; i < 42; i++) {
            LocalDate date = startGrid.plusDays(i);
            boolean inCurrentMonth = (date.getMonthValue() == currentMonth.getMonthValue());

            DayCell cell = new DayCell(date, inCurrentMonth, today);
            gridDays.add(cell);
        }

        if (rangeMode && lblRangeInfo != null) {
            if (rangeStart != null && rangeEnd != null) {
                LocalDate s = rangeStart.isBefore(rangeEnd) ? rangeStart : rangeEnd;
                LocalDate e = rangeStart.isBefore(rangeEnd) ? rangeEnd : rangeStart;
                lblRangeInfo.setText(s.format(DATE_FMT) + " — " + e.format(DATE_FMT));
            } else if (rangeStart != null) {
                lblRangeInfo.setText("Từ: " + rangeStart.format(DATE_FMT) + " (Chọn ngày kết thúc)");
            } else {
                lblRangeInfo.setText("Chọn khoảng ngày cần xem");
            }
        }

        gridDays.revalidate();
        gridDays.repaint();
    }

    private JButton navBtn(String dir, Runnable action) {
        JButton b = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(0xF1F5F8));
                    g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 6, 6);
                }
                g2.setColor(Theme.TEXT);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                if ("left".equals(dir) || "‹".equals(dir)) {
                    g2.drawLine(cx + 2, cy - 5, cx - 3, cy);
                    g2.drawLine(cx - 3, cy, cx + 2, cy + 5);
                } else {
                    g2.drawLine(cx - 2, cy - 5, cx + 3, cy);
                    g2.drawLine(cx + 3, cy, cx - 2, cy + 5);
                }
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(28, 28));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> action.run());
        return b;
    }

    // ---------------- Day Cell Component ----------------

    private class DayCell extends JComponent {
        private final LocalDate date;
        private final boolean inCurrentMonth;
        private final boolean isToday;
        private boolean hovering = false;

        DayCell(LocalDate date, boolean inCurrentMonth, LocalDate today) {
            this.date = date;
            this.inCurrentMonth = inCurrentMonth;
            this.isToday = date.equals(today);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovering = false; repaint(); }
                @Override public void mouseClicked(MouseEvent e) { handleDateClick(date); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int size = Math.min(w, h) - 4;
            int x = (w - size) / 2;
            int y = (h - size) / 2;

            boolean isSelected = false;
            boolean inRange = false;

            if (!rangeMode) {
                isSelected = date.equals(singleSelected);
            } else {
                if (rangeStart != null && rangeEnd != null) {
                    LocalDate s = rangeStart.isBefore(rangeEnd) ? rangeStart : rangeEnd;
                    LocalDate e = rangeStart.isBefore(rangeEnd) ? rangeEnd : rangeStart;
                    isSelected = date.equals(s) || date.equals(e);
                    inRange = date.isAfter(s) && date.isBefore(e);
                } else if (rangeStart != null) {
                    isSelected = date.equals(rangeStart);
                }
            }

            // Vẽ dải màu ở giữa (in range)
            if (inRange) {
                g2.setColor(Theme.BLUE_BG);
                g2.fillRect(0, y, w, size);
            }

            // Vẽ ô ngày được chọn
            if (isSelected) {
                g2.setColor(Theme.ACCENT);
                g2.fillRoundRect(x, y, size, size, 8, 8);
            } else if (hovering) {
                g2.setColor(new Color(0xF1F5F8));
                g2.fillRoundRect(x, y, size, size, 8, 8);
            }

            // Viền ngày hôm nay
            if (isToday && !isSelected) {
                g2.setColor(Theme.ACCENT);
                g2.drawRoundRect(x, y, size - 1, size - 1, 8, 8);
            }

            // Chữ số ngày
            String text = String.valueOf(date.getDayOfMonth());
            g2.setFont(isSelected ? Theme.font(Font.BOLD, 12) : Theme.font(Font.PLAIN, 12));

            if (isSelected) {
                g2.setColor(Color.WHITE);
            } else if (inRange) {
                g2.setColor(Theme.BLUE_TEXT);
            } else if (inCurrentMonth) {
                g2.setColor(Theme.TEXT);
            } else {
                g2.setColor(new Color(0xBAC7D0)); // Ngày ngoài tháng mờ hơn
            }

            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(text)) / 2;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(text, tx, ty);

            g2.dispose();
        }
    }

    private void handleDateClick(LocalDate clickedDate) {
        if (!rangeMode) {
            singleSelected = clickedDate;
            if (onSingleSelect != null) onSingleSelect.accept(clickedDate);
            onClose.run();
        } else {
            if (pickingStart || rangeStart == null) {
                rangeStart = clickedDate;
                rangeEnd = null;
                pickingStart = false;
            } else {
                if (clickedDate.isBefore(rangeStart)) {
                    rangeEnd = rangeStart;
                    rangeStart = clickedDate;
                } else {
                    rangeEnd = clickedDate;
                }
                pickingStart = true;
            }
            refreshCalendar();
        }
    }
}
