package GUI;

import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/** Giới hạn độ dài và (tùy chọn) loại ký tự được gõ hoặc dán vào một ô nhập. */
final class GioiHanNhap extends DocumentFilter {
    private final int toiDa;
    private final String kyTuHopLe;   // regex cho MỘT ký tự; null = cho phép mọi ký tự

    private GioiHanNhap(int toiDa, String kyTuHopLe) {
        this.toiDa = toiDa;
        this.kyTuHopLe = kyTuHopLe;
    }

    static void ap(JTextField f, int toiDa, String kyTuHopLe) {
        ((AbstractDocument) f.getDocument()).setDocumentFilter(new GioiHanNhap(toiDa, kyTuHopLe));
    }

    private String loc(String text, int daCo) {
        String t = text == null ? "" : text;
        if (kyTuHopLe != null) {
            StringBuilder sb = new StringBuilder();
            for (char c : t.toCharArray()) if (String.valueOf(c).matches(kyTuHopLe)) sb.append(c);
            t = sb.toString();
        }
        int con = Math.max(0, toiDa - daCo);
        return t.length() > con ? t.substring(0, con) : t;
    }

    @Override public void insertString(FilterBypass fb, int offset, String s, AttributeSet a) throws BadLocationException {
        super.insertString(fb, offset, loc(s, fb.getDocument().getLength()), a);
    }

    @Override public void replace(FilterBypass fb, int offset, int length, String s, AttributeSet a) throws BadLocationException {
        super.replace(fb, offset, length, loc(s, fb.getDocument().getLength() - length), a);
    }
}
