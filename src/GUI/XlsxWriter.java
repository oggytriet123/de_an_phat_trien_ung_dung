package GUI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Ghi một bảng ra tệp Excel (.xlsx) mà không cần thư viện ngoài.
 * Ô là chuỗi (String) hoặc số (Number); dòng tiêu đề được in đậm.
 */
final class XlsxWriter {
    private XlsxWriter() {}

    static void ghi(File tep, String tenSheet, String[] tieuDe, List<Object[]> dong) throws IOException {
        try (ZipOutputStream z = new ZipOutputStream(new FileOutputStream(tep))) {
            them(z, "[Content_Types].xml",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                    + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                    + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                    + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                    + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                    + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
                    + "</Types>");
            them(z, "_rels/.rels",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                    + "</Relationships>");
            them(z, "xl/workbook.xml",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
                    + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                    + "<sheets><sheet name=\"" + xml(tenSheetHopLe(tenSheet)) + "\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>");
            them(z, "xl/_rels/workbook.xml.rels",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                    + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>"
                    + "</Relationships>");
            them(z, "xl/styles.xml",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                    + "<fonts count=\"2\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
                    + "<font><b/><sz val=\"11\"/><name val=\"Calibri\"/></font></fonts>"
                    + "<fills count=\"2\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill></fills>"
                    + "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>"
                    + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                    + "<cellXfs count=\"3\">"
                    + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
                    + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/>"
                    + "<xf numFmtId=\"3\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\"/>"
                    + "</cellXfs>"
                    + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
                    + "</styleSheet>");

            StringBuilder sb = new StringBuilder();
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
              .append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
            sb.append("<cols>");
            for (int c = 0; c < tieuDe.length; c++) {
                sb.append("<col min=\"").append(c + 1).append("\" max=\"").append(c + 1).append("\" width=\"")
                  .append(doRong(c, tieuDe, dong)).append("\" customWidth=\"1\"/>");
            }
            sb.append("</cols><sheetData>");
            sb.append("<row r=\"1\">");
            for (int c = 0; c < tieuDe.length; c++) {
                sb.append("<c r=\"").append(cot(c)).append("1\" t=\"inlineStr\" s=\"1\"><is><t>")
                  .append(xml(tieuDe[c])).append("</t></is></c>");
            }
            sb.append("</row>");
            for (int r = 0; r < dong.size(); r++) {
                int so = r + 2;
                sb.append("<row r=\"").append(so).append("\">");
                Object[] row = dong.get(r);
                for (int c = 0; c < row.length; c++) {
                    String ref = cot(c) + so;
                    Object v = row[c];
                    if (v instanceof Number n) {
                        sb.append("<c r=\"").append(ref).append("\" s=\"2\"><v>").append(n.longValue()).append("</v></c>");
                    } else {
                        sb.append("<c r=\"").append(ref).append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                          .append(xml(v == null ? "" : v.toString())).append("</t></is></c>");
                    }
                }
                sb.append("</row>");
            }
            sb.append("</sheetData></worksheet>");
            them(z, "xl/worksheets/sheet1.xml", sb.toString());
        }
    }

    private static void them(ZipOutputStream z, String ten, String noiDung) throws IOException {
        z.putNextEntry(new ZipEntry(ten));
        z.write(noiDung.getBytes(StandardCharsets.UTF_8));
        z.closeEntry();
    }

    /** Tên sheet Excel: tối đa 31 ký tự, không chứa : \ / ? * [ ] */
    private static String tenSheetHopLe(String s) {
        String t = (s == null || s.isBlank() ? "Dữ liệu" : s).replaceAll("[:\\\\/?*\\[\\]]", " ").trim();
        return t.length() > 31 ? t.substring(0, 31) : t;
    }

    private static int doRong(int c, String[] tieuDe, List<Object[]> dong) {
        int max = tieuDe[c].length();
        for (Object[] r : dong) if (c < r.length && r[c] != null) max = Math.max(max, r[c].toString().length());
        return Math.min(60, Math.max(10, max + 3));
    }

    /** 0 thành A, 25 thành Z, 26 thành AA ... */
    static String cot(int i) {
        StringBuilder sb = new StringBuilder();
        for (int n = i; n >= 0; n = n / 26 - 1) sb.insert(0, (char) ('A' + n % 26));
        return sb.toString();
    }

    private static String xml(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                default -> { if (c >= 0x20 || c == '\t' || c == '\n') sb.append(c); }   // bỏ ký tự điều khiển không hợp lệ trong XML
            }
        }
        return sb.toString();
    }
}
