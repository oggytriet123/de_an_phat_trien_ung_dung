package GUI;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.imageio.ImageIO;

/**
 * Tiện ích tạo mã QR động chuẩn EMVCo VietQR / Napas247 (MoMo & Ngân hàng).
 * Khi quét bằng ứng dụng MoMo hoặc ứng dụng Ngân hàng trên điện thoại,
 * số tiền cần thanh toán và nội dung hóa đơn sẽ tự động hiển thị ngay.
 */
public final class VietQRUtil {

    // Thông tin định danh tài khoản MoMo VietQR trích xuất từ mã gốc của cửa hàng
    private static final String MERCHANT_INFO =
            "38630010A000000727013300069710250119PSP26010110000002500208QRIBFTTA";
    private static final String MOMO_REF_TAGS =
            "0515MOMOW2W936764860622fu5szuESM1JTxDHKyq-m9w";

    private VietQRUtil() {}

    /**
     * Tạo chuỗi payload chuẩn EMVCo VietQR có nhúng số tiền (Tag 54) và mã hóa đơn (Tag 62.08).
     */
    public static String taoPayloadVietQR(long soTien, String maHD) {
        StringBuilder sb = new StringBuilder();
        // 00: Payload Format Indicator (01)
        sb.append("000201");
        // 01: Point of Initiation Method (12 = Dynamic QR có số tiền)
        sb.append(soTien > 0 ? "010212" : "010211");
        // 38: Merchant Account Information (Napas247 / MoMo VietQR)
        sb.append(MERCHANT_INFO);
        // 53: Transaction Currency (704 = VND)
        sb.append("5303704");
        // 54: Transaction Amount
        if (soTien > 0) {
            String amt = String.valueOf(soTien);
            sb.append("54").append(String.format("%02d", amt.length())).append(amt);
        }
        // 58: Country Code (VN)
        sb.append("5802VN");

        // 62: Additional Data Field Template (Reference + Nội dung chuyển khoản)
        StringBuilder addData = new StringBuilder(MOMO_REF_TAGS);
        if (maHD != null && !maHD.trim().isEmpty()) {
            String content = "TT " + maHD.trim().replaceAll("[^A-Za-z0-9 ]", "");
            if (content.length() > 25) content = content.substring(0, 25);
            addData.append("08").append(String.format("%02d", content.length())).append(content);
        }
        String addStr = addData.toString();
        sb.append("62").append(String.format("%02d", addStr.length())).append(addStr);

        // 63: CRC (Tag 63, Length 04)
        sb.append("6304");
        String crc = tinhCRC16(sb.toString().getBytes(StandardCharsets.US_ASCII));
        sb.append(crc);
        return sb.toString();
    }

    /**
     * Tính mã kiểm tra CRC-16/CCITT-FALSE (Polynomial 0x1021, Init 0xFFFF) theo chuẩn EMVCo.
     */
    public static String tinhCRC16(byte[] bytes) {
        int crc = 0xFFFF;
        for (byte b : bytes) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = ((crc << 1) ^ 0x1021) & 0xFFFF;
                } else {
                    crc = (crc << 1) & 0xFFFF;
                }
            }
        }
        return String.format("%04X", crc);
    }

    /**
     * Tạo ảnh QR động có nhúng số tiền và nội dung thanh toán.
     * Ưu tiên tự sinh bằng thuật toán nội bộ (nhanh tức thì & hoạt động offline),
     * nếu lỗi mới fallback về ảnh tĩnh.
     */
    public static Image taoAnhQR(long soTien, String maHD, int size) {
        String payload = taoPayloadVietQR(soTien, maHD);
        try {
            boolean[][] matrix = QREncoder.encode(payload);
            return veAnhVietQR(matrix, size);
        } catch (Exception e) {
            // Fallback qua API tạo QR trực tuyến nếu có mạng
            try {
                String encoded = URLEncoder.encode(payload, StandardCharsets.UTF_8);
                String url = "https://api.qrserver.com/v1/create-qr-code/?size=" + size + "x" + size + "&ecc=M&margin=1&data=" + encoded;
                return ImageIO.read(URI.create(url).toURL());
            } catch (Exception ignored) {}
        }
        return layAnhQRTinh();
    }

    private static Image veAnhVietQR(boolean[][] matrix, int size) {
        int n = matrix.length;
        int quietZone = 2;
        int totalModules = n + quietZone * 2;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Nền trắng
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, size, size);

        // Vùng vẽ QR (chừa phần chân 22px cho logo VIETQR | napas 247)
        int qrArea = size - 22;
        double cellSize = (double) qrArea / totalModules;
        double offsetX = (size - qrArea) / 2.0;
        double offsetY = 2;

        g2.setColor(Color.BLACK);
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (matrix[r][c]) {
                    int x1 = (int) Math.round(offsetX + (c + quietZone) * cellSize);
                    int y1 = (int) Math.round(offsetY + (r + quietZone) * cellSize);
                    int x2 = (int) Math.round(offsetX + (c + quietZone + 1) * cellSize);
                    int y2 = (int) Math.round(offsetY + (r + quietZone + 1) * cellSize);
                    g2.fillRect(x1, y1, Math.max(1, x2 - x1), Math.max(1, y2 - y1));
                }
            }
        }

        // Vẽ huy hiệu "NT" nhỏ ở tâm (tương tự ảnh gốc MoMo VietQR)
        int badgeSize = (int) (qrArea * 0.16);
        int bx = (size - badgeSize) / 2;
        int by = (int) offsetY + (qrArea - badgeSize) / 2;
        g2.setColor(Color.WHITE);
        g2.fillOval(bx - 2, by - 2, badgeSize + 4, badgeSize + 4);
        g2.setColor(new Color(0xFDF2F8));
        g2.fillOval(bx, by, badgeSize, badgeSize);
        g2.setColor(new Color(0xF472B6));
        g2.drawOval(bx, by, badgeSize, badgeSize);
        g2.setColor(new Color(0xDB2777));
        g2.setFont(new Font("Segoe UI", Font.BOLD, Math.max(10, badgeSize / 2 - 1)));
        FontMetrics fm = g2.getFontMetrics();
        String label = "NT";
        g2.drawString(label, bx + (badgeSize - fm.stringWidth(label)) / 2,
                by + (badgeSize - fm.getHeight()) / 2 + fm.getAscent());

        // Vẽ dòng chữ VIETQR | napas247 ở chân
        int footerY = size - 6;
        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        FontMetrics fmf = g2.getFontMetrics();
        String vqr = "VIETQR";
        String sep = "  |  ";
        String napas = "napas 247";
        int totalW = fmf.stringWidth(vqr + sep + napas);
        int startX = (size - totalW) / 2;

        g2.setColor(new Color(0xDC2626));
        g2.drawString(vqr, startX, footerY);
        startX += fmf.stringWidth(vqr);

        g2.setColor(new Color(0xCBD5E1));
        g2.drawString(sep, startX, footerY);
        startX += fmf.stringWidth(sep);

        g2.setColor(new Color(0x1D4ED8));
        g2.drawString(napas, startX, footerY);

        g2.dispose();
        return img;
    }

    private static Image layAnhQRTinh() {
        try {
            File f = new File("data/qr_momo.png");
            if (f.exists()) return ImageIO.read(f);
            java.net.URL url = VietQRUtil.class.getResource("qr_momo.png");
            if (url != null) return ImageIO.read(url);
        } catch (Exception ignored) {}
        return null;
    }

    // =========================================================================
    // Bộ mã hóa QR Code chuẩn ISO/IEC 18004 (Byte Mode, ECC Level M, Version 1..10)
    // =========================================================================
    static final class QREncoder {
        // Tổng số từ mã (data + EC) của mỗi version 1..12
        private static final int[] TOTAL_CODEWORDS = {
            0, 26, 44, 70, 100, 134, 172, 196, 242, 292, 346, 404, 466
        };
        // Số từ mã sửa lỗi (EC codewords per block) ở mức ECC Level M cho version 1..12
        private static final int[] EC_CODEWORDS_PER_BLOCK_M = {
            0, 10, 16, 26, 18, 24, 16, 18, 22, 22, 26, 30, 22
        };
        // Số khối (blocks) ở mức ECC Level M cho version 1..12
        private static final int[] NUM_ERROR_CORRECTION_BLOCKS_M = {
            0, 1, 1, 1, 2, 2, 4, 4, 4, 5, 5, 5, 8
        };
        // Tọa độ tâm các Alignment Pattern cho version 1..12
        private static final int[][] ALIGNMENT_POS = {
            {}, {}, {6, 18}, {6, 22}, {6, 26}, {6, 30},
            {6, 34}, {6, 22, 38}, {6, 24, 42}, {6, 26, 46}, {6, 28, 50}, {6, 30, 54}, {6, 32, 58}
        };

        static boolean[][] encode(String text) {
            byte[] data = text.getBytes(StandardCharsets.ISO_8859_1);
            int ver = chonVersion(data.length);
            int totalCodewords = TOTAL_CODEWORDS[ver];
            int ecPerBlock = EC_CODEWORDS_PER_BLOCK_M[ver];
            int numBlocks = NUM_ERROR_CORRECTION_BLOCKS_M[ver];
            int dataCapacity = totalCodewords - ecPerBlock * numBlocks;

            // 1. Tạo luồng bit dữ liệu (Byte Mode = 0100)
            BitBuffer bits = new BitBuffer();
            bits.appendBits(0b0100, 4);
            bits.appendBits(data.length, ver <= 9 ? 8 : 16);
            for (byte b : data) bits.appendBits(b & 0xFF, 8);

            // Terminator (tối đa 4 bit 0)
            int maxBits = dataCapacity * 8;
            bits.appendBits(0, Math.min(4, maxBits - bits.length));
            if (bits.length % 8 != 0) {
                bits.appendBits(0, 8 - (bits.length % 8));
            }
            // Pad bytes (0xEC, 0x11 luân phiên)
            int padByte = 0xEC;
            while (bits.length < maxBits) {
                bits.appendBits(padByte, 8);
                padByte ^= (0xEC ^ 0x11);
            }

            byte[] dataCodewords = bits.toByteArray();

            // 2. Chia khối và tính Reed-Solomon Error Correction, sau đó đan xen (interleave)
            byte[] allCodewords = interleaveWithEC(dataCodewords, ver, totalCodewords, ecPerBlock, numBlocks);

            // 3. Khởi tạo ma trận và vẽ các mẫu chức năng (Function Patterns)
            int size = ver * 4 + 17;
            boolean[][] modules = new boolean[size][size];
            boolean[][] isFunction = new boolean[size][size];

            drawFunctionPatterns(ver, size, modules, isFunction);
            drawCodewords(allCodewords, size, modules, isFunction);

            // 4. Chọn mask tối ưu nhất (0..7)
            int bestMask = 0;
            int minPenalty = Integer.MAX_VALUE;
            for (int mask = 0; mask < 8; mask++) {
                boolean[][] copy = copyMatrix(modules);
                applyMask(mask, size, copy, isFunction);
                drawFormatBits(mask, size, copy, isFunction);
                int penalty = tinhPenalty(copy, size);
                if (penalty < minPenalty) {
                    minPenalty = penalty;
                    bestMask = mask;
                }
            }

            applyMask(bestMask, size, modules, isFunction);
            drawFormatBits(bestMask, size, modules, isFunction);
            return modules;
        }

        private static int chonVersion(int byteLen) {
            for (int v = 1; v <= 12; v++) {
                int dataCap = TOTAL_CODEWORDS[v] - EC_CODEWORDS_PER_BLOCK_M[v] * NUM_ERROR_CORRECTION_BLOCKS_M[v];
                int headerBytes = (v <= 9) ? 2 : 3;
                if (byteLen + headerBytes <= dataCap) return v;
            }
            throw new IllegalArgumentException("Dữ liệu quá dài cho QR Version 1..12: " + byteLen);
        }

        private static byte[] interleaveWithEC(byte[] data, int ver, int totalCw, int ecPerBlock, int numBlocks) {
            int numShortBlocks = numBlocks - (totalCw % numBlocks);
            int shortBlockTotalLen = totalCw / numBlocks;
            int shortDataLen = shortBlockTotalLen - ecPerBlock;

            byte[][] dataBlocks = new byte[numBlocks][];
            byte[][] ecBlocks = new byte[numBlocks][];
            byte[] rsDiv = rsGeneratorPoly(ecPerBlock);

            int offset = 0;
            for (int i = 0; i < numBlocks; i++) {
                int dLen = shortDataLen + (i < numShortBlocks ? 0 : 1);
                dataBlocks[i] = Arrays.copyOfRange(data, offset, offset + dLen);
                offset += dLen;
                ecBlocks[i] = rsComputeRemainder(dataBlocks[i], rsDiv);
            }

            byte[] result = new byte[totalCw];
            int idx = 0;
            int maxDataLen = shortDataLen + 1;
            for (int i = 0; i < maxDataLen; i++) {
                for (int j = 0; j < numBlocks; j++) {
                    if (i < dataBlocks[j].length) {
                        result[idx++] = dataBlocks[j][i];
                    }
                }
            }
            for (int i = 0; i < ecPerBlock; i++) {
                for (int j = 0; j < numBlocks; j++) {
                    result[idx++] = ecBlocks[j][i];
                }
            }
            return result;
        }

        private static void drawFunctionPatterns(int ver, int size, boolean[][] modules, boolean[][] isFunc) {
            // Timing patterns
            for (int i = 0; i < size; i++) {
                setFunc(6, i, i % 2 == 0, modules, isFunc);
                setFunc(i, 6, i % 2 == 0, modules, isFunc);
            }
            // 3 Finder patterns
            drawFinder(3, 3, size, modules, isFunc);
            drawFinder(size - 4, 3, size, modules, isFunc);
            drawFinder(3, size - 4, size, modules, isFunc);

            // Alignment patterns
            int[] align = ALIGNMENT_POS[ver];
            for (int r : align) {
                for (int c : align) {
                    if ((r == 6 && c == 6) || (r == 6 && c == size - 7) || (r == size - 7 && c == 6)) continue;
                    drawAlignment(r, c, modules, isFunc);
                }
            }

            // Reserve Format Information areas
            for (int i = 0; i <= 8; i++) {
                if (i != 6) {
                    setFunc(8, i, false, modules, isFunc);
                    setFunc(i, 8, false, modules, isFunc);
                }
                if (i < 8) {
                    setFunc(8, size - 1 - i, false, modules, isFunc);
                    setFunc(size - 1 - i, 8, false, modules, isFunc);
                }
            }
            // Dark module
            setFunc(8, size - 8, true, modules, isFunc);

            // Version info (nếu ver >= 7)
            if (ver >= 7) {
                int rem = ver;
                for (int i = 0; i < 12; i++) rem = (rem << 1) ^ ((rem >>> 11) * 0x1F25);
                int bits = (ver << 12) | rem;
                for (int i = 0; i < 18; i++) {
                    boolean bit = ((bits >>> i) & 1) != 0;
                    int a = size - 11 + i % 3;
                    int b = i / 3;
                    setFunc(a, b, bit, modules, isFunc);
                    setFunc(b, a, bit, modules, isFunc);
                }
            }
        }

        private static void drawFinder(int cx, int cy, int size, boolean[][] modules, boolean[][] isFunc) {
            for (int dy = -4; dy <= 4; dy++) {
                for (int dx = -4; dx <= 4; dx++) {
                    int x = cx + dx, y = cy + dy;
                    if (x >= 0 && x < size && y >= 0 && y < size) {
                        int dist = Math.max(Math.abs(dx), Math.abs(dy));
                        setFunc(x, y, dist != 2 && dist != 4, modules, isFunc);
                    }
                }
            }
        }

        private static void drawAlignment(int cx, int cy, boolean[][] modules, boolean[][] isFunc) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dx = -2; dx <= 2; dx++) {
                    setFunc(cx + dx, cy + dy, Math.max(Math.abs(dx), Math.abs(dy)) != 1, modules, isFunc);
                }
            }
        }

        private static void setFunc(int x, int y, boolean val, boolean[][] modules, boolean[][] isFunc) {
            modules[y][x] = val;
            isFunc[y][x] = true;
        }

        private static void drawCodewords(byte[] data, int size, boolean[][] modules, boolean[][] isFunc) {
            int i = 0;
            for (int right = size - 1; right >= 1; right -= 2) {
                if (right == 6) right = 5;
                for (int vert = 0; vert < size; vert++) {
                    for (int j = 0; j < 2; j++) {
                        int x = right - j;
                        boolean upward = ((right + 1) & 2) == 0;
                        int y = upward ? size - 1 - vert : vert;
                        if (!isFunc[y][x] && i < data.length * 8) {
                            modules[y][x] = ((data[i >>> 3] >>> (7 - (i & 7))) & 1) != 0;
                            i++;
                        }
                    }
                }
            }
        }

        private static void applyMask(int mask, int size, boolean[][] modules, boolean[][] isFunc) {
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (isFunc[y][x]) continue;
                    boolean invert = switch (mask) {
                        case 0 -> (x + y) % 2 == 0;
                        case 1 -> y % 2 == 0;
                        case 2 -> x % 3 == 0;
                        case 3 -> (x + y) % 3 == 0;
                        case 4 -> (x / 3 + y / 2) % 2 == 0;
                        case 5 -> (x * y) % 2 + (x * y) % 3 == 0;
                        case 6 -> ((x * y) % 2 + (x * y) % 3) % 2 == 0;
                        case 7 -> ((x + y) % 2 + (x * y) % 3) % 2 == 0;
                        default -> false;
                    };
                    modules[y][x] ^= invert;
                }
            }
        }

        private static void drawFormatBits(int mask, int size, boolean[][] modules, boolean[][] isFunc) {
            // ECC Level M format bits = 00
            int data = (0b00 << 3) | mask;
            int rem = data;
            for (int i = 0; i < 10; i++) rem = (rem << 1) ^ ((rem >>> 9) * 0x537);
            int bits = ((data << 10) | rem) ^ 0x5412;

            for (int i = 0; i <= 5; i++) setFunc(8, i, ((bits >>> i) & 1) != 0, modules, isFunc);
            setFunc(8, 7, ((bits >>> 6) & 1) != 0, modules, isFunc);
            setFunc(8, 8, ((bits >>> 7) & 1) != 0, modules, isFunc);
            setFunc(7, 8, ((bits >>> 8) & 1) != 0, modules, isFunc);
            for (int i = 9; i < 15; i++) setFunc(14 - i, 8, ((bits >>> i) & 1) != 0, modules, isFunc);

            for (int i = 0; i < 8; i++) setFunc(size - 1 - i, 8, ((bits >>> i) & 1) != 0, modules, isFunc);
            for (int i = 8; i < 15; i++) setFunc(8, size - 15 + i, ((bits >>> i) & 1) != 0, modules, isFunc);
            setFunc(8, size - 8, true, modules, isFunc);
        }

        private static int tinhPenalty(boolean[][] m, int size) {
            int p = 0;
            for (int y = 0; y < size; y++) {
                int run = 1;
                for (int x = 1; x < size; x++) {
                    if (m[y][x] == m[y][x - 1]) {
                        run++;
                        if (run == 5) p += 3;
                        else if (run > 5) p++;
                    } else run = 1;
                }
            }
            for (int x = 0; x < size; x++) {
                int run = 1;
                for (int y = 1; y < size; y++) {
                    if (m[y][x] == m[y - 1][x]) {
                        run++;
                        if (run == 5) p += 3;
                        else if (run > 5) p++;
                    } else run = 1;
                }
            }
            return p;
        }

        private static boolean[][] copyMatrix(boolean[][] src) {
            boolean[][] dst = new boolean[src.length][];
            for (int i = 0; i < src.length; i++) dst[i] = src[i].clone();
            return dst;
        }

        // Reed-Solomon GF(256) với đa thức nguyên thủy 0x11D
        private static byte[] rsGeneratorPoly(int degree) {
            byte[] poly = new byte[degree];
            poly[degree - 1] = 1;
            int root = 1;
            for (int i = 0; i < degree; i++) {
                for (int j = 0; j < degree; j++) {
                    poly[j] = (byte) gfMultiply(poly[j] & 0xFF, root);
                    if (j + 1 < degree) poly[j] ^= poly[j + 1];
                }
                root = gfMultiply(root, 0x02);
            }
            return poly;
        }

        private static byte[] rsComputeRemainder(byte[] data, byte[] gen) {
            byte[] rem = new byte[gen.length];
            for (byte b : data) {
                int factor = (b ^ rem[0]) & 0xFF;
                System.arraycopy(rem, 1, rem, 0, rem.length - 1);
                rem[rem.length - 1] = 0;
                for (int i = 0; i < gen.length; i++) {
                    rem[i] ^= (byte) gfMultiply(gen[i] & 0xFF, factor);
                }
            }
            return rem;
        }

        private static int gfMultiply(int x, int y) {
            int z = 0;
            for (int i = 7; i >= 0; i--) {
                z = (z << 1) ^ ((z >>> 7) * 0x11D);
                z ^= ((y >>> i) & 1) * x;
            }
            return z;
        }

        private static final class BitBuffer {
            private final byte[] buf = new byte[512];
            int length = 0;

            void appendBits(int val, int len) {
                for (int i = len - 1; i >= 0; i--) {
                    if (((val >>> i) & 1) != 0) {
                        buf[length >>> 3] |= (byte) (1 << (7 - (length & 7)));
                    }
                    length++;
                }
            }

            byte[] toByteArray() {
                return Arrays.copyOf(buf, (length + 7) >>> 3);
            }
        }
    }
}
