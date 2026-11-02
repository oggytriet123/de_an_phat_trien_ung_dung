-- ============================================================================
-- BỔ SUNG DỮ LIỆU DANH MỤC BỆNH (file riêng, KHÔNG sửa PetCare.sql gốc)
-- Cách dùng: chạy PetCare.sql trước, sau đó chạy file này.
-- File chạy lại nhiều lần vẫn an toàn (bệnh đã có thì bỏ qua).
-- Mã hóa: UTF-8 with BOM (mở bằng SSMS để không lỗi tiếng Việt)
-- ============================================================================

USE PetCare;
GO

IF NOT EXISTS (SELECT 1 FROM DANHMUCBENH WHERE maBenh = N'BNH001')
    INSERT INTO DANHMUCBENH (maBenh, tenBenh, trieuChung, maNV_QL)
    VALUES (N'BNH001', N'Viêm da dị ứng', N'Ngứa ngáy dữ dội, rụng lông thành từng mảng, da ửng đỏ, nổi mẩn li ti', N'NV001');
IF NOT EXISTS (SELECT 1 FROM DANHMUCBENH WHERE maBenh = N'BNH002')
    INSERT INTO DANHMUCBENH (maBenh, tenBenh, trieuChung, maNV_QL)
    VALUES (N'BNH002', N'Parvo virus', N'Nôn ói liên tục, tiêu chảy ra máu tanh nồng, sốt cao, ủ rũ bỏ ăn', N'NV001');
IF NOT EXISTS (SELECT 1 FROM DANHMUCBENH WHERE maBenh = N'BNH003')
    INSERT INTO DANHMUCBENH (maBenh, tenBenh, trieuChung, maNV_QL)
    VALUES (N'BNH003', N'Viêm tai ngoài', N'Lắc đầu liên tục, gãi tai chảy máu, ống tai tiết dịch màu nâu hôi', N'NV001');
IF NOT EXISTS (SELECT 1 FROM DANHMUCBENH WHERE maBenh = N'BNH004')
    INSERT INTO DANHMUCBENH (maBenh, tenBenh, trieuChung, maNV_QL)
    VALUES (N'BNH004', N'Sỏi bàng quang', N'Tiểu rắt, tiểu ngắt quãng, có tia máu trong nước tiểu, cong lưng đau', N'NV001');
IF NOT EXISTS (SELECT 1 FROM DANHMUCBENH WHERE maBenh = N'BNH005')
    INSERT INTO DANHMUCBENH (maBenh, tenBenh, trieuChung, maNV_QL)
    VALUES (N'BNH005', N'Cảm cúm hô hấp', N'Hắt hơi, chảy nước mũi đặc, ho khan, mắt đỏ đổ ghèn, sốt nhẹ', N'NV001');
GO

SELECT maBenh, tenBenh FROM DANHMUCBENH ORDER BY maBenh;
GO
