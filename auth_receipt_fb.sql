-- ====================================================================
-- PHẦN 2: CHẠY TRÊN DATABASE DỰ ÁN [taichi_sql]
-- ====================================================================

-- --------------------------------------------------------------------
-- 1. RÀNG BUỘC TOÀN VẸN (CHECK & UNIQUE - THỂ HIỆN TRÊN FORM FE)
-- --------------------------------------------------------------------

-- 1.1. PHÂN HỆ RETURN RECEIPT (Đổi trả hàng):
-- CHECK: Tiền hoàn trả phải > 0 và bắt buộc nhập lý do đổi trả (ít nhất 5 ký tự)
-- Demo FE: Khi thu ngân nhập tiền <= 0 hoặc để trống lý do -> Form báo lỗi đỏ
IF NOT EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'chk_return_valid_data')
    ALTER TABLE dbo.return_receipt ADD CONSTRAINT chk_return_valid_data 
    CHECK (refund_amount > 0 AND LEN(TRIM(return_reason)) >= 5);
GO

-- UNIQUE: Mỗi hóa đơn chỉ được đổi trả 1 lần duy nhất
-- Demo FE: Khi thu ngân nhập lại hóa đơn đã đổi trả trước đó -> Báo lỗi trùng lặp
IF NOT EXISTS (SELECT * FROM sys.key_constraints WHERE name = 'uq_return_receipt_invoice')
    ALTER TABLE dbo.return_receipt ADD CONSTRAINT uq_return_receipt_invoice 
    UNIQUE (original_invoice_id);
GO

-- 1.2. PHÂN HỆ FEEDBACK (Đánh giá dịch vụ):
-- CHECK: Điểm đánh giá chỉ từ 1 đến 5 sao
-- Demo FE: Khớp hoàn toàn với widget chọn 1-5 ngôi sao trên giao diện
IF NOT EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'chk_feedback_rating')
    ALTER TABLE dbo.store_feedback ADD CONSTRAINT chk_feedback_rating 
    CHECK (rating BETWEEN 1 AND 5);
GO

-- 1.3. PHÂN HỆ AUTH (Tài khoản & Phân quyền):
-- CHECK: Chức vụ nhân viên chỉ thuộc 3 vai trò hợp lệ
-- Demo FE: Khớp với Dropdown chức vụ trên form quản lý tài khoản nhân sự
IF NOT EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'chk_employee_position')
    ALTER TABLE dbo.employee ADD CONSTRAINT chk_employee_position 
    CHECK (position IN ('STORE_OWNER', 'CASHIER', 'INVENTORY_MANAGER'));
GO

-- --------------------------------------------------------------------
-- 2. CHỈ MỤC TỐI ƯU HÓA TRUY VẤN (1 INDEX DUY NHẤT)
-- --------------------------------------------------------------------
-- Phân hệ RETURN RECEIPT: Tối ưu hóa tốc độ tìm kiếm phiếu đổi trả theo mã hóa đơn
-- Demo FE: Khi thu ngân gõ mã hóa đơn vào ô tìm kiếm -> Kết quả lọc ra tức thì không bị lag
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_return_receipt_invoice' AND object_id = OBJECT_ID('dbo.return_receipt'))
    CREATE NONCLUSTERED INDEX idx_return_receipt_invoice 
    ON dbo.return_receipt (original_invoice_id);
GO

-- --------------------------------------------------------------------
-- 3. TRIGGER ĐỒNG BỘ DỮ LIỆU THỜI GIAN THỰC (1 TRIGGER DUY NHẤT)
-- --------------------------------------------------------------------
-- Phân hệ RETURN RECEIPT: Tự động đổi trạng thái hóa đơn thành 'REFUNDED' khi tạo phiếu trả hàng
-- Demo FE: Nhãn trạng thái hóa đơn tự động đổi màu từ 'PAID' (Xanh) sang 'REFUNDED' (Cam)
CREATE OR ALTER TRIGGER dbo.trg_CapNhatTrangThaiHoaDon_DoiTra
ON dbo.return_receipt
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE inv
    SET inv.status = 'REFUNDED'
    FROM dbo.invoice inv
    JOIN inserted i ON inv.invoice_id = i.original_invoice_id;
END;
GO

-- --------------------------------------------------------------------
-- 4. KHUNG NHÌN (VIEWS - HIỂN THỊ DỮ LIỆU TRÊN BẢNG FE)
-- --------------------------------------------------------------------

-- 4.1. Phân hệ RETURN RECEIPT: Cung cấp danh sách lịch sử phiếu đổi trả cho Frontend hiển thị
CREATE VIEW dbo.v_danh_sach_phieu_tra_hang AS
SELECT return_receipt_id, original_invoice_id, employee_id, refund_amount, return_reason, requested_at
FROM dbo.return_receipt;
GO

-- 4.2. Phân hệ FEEDBACK: Cung cấp danh sách phản hồi của khách hàng cho màn hình Admin hiển thị
CREATE VIEW dbo.v_danh_gia_khach_hang AS
SELECT feedback_id, customer_name, phone_number, rating, comment, created_at
FROM dbo.store_feedback;
GO

-- --------------------------------------------------------------------
-- 5. HÀM NGƯỜI DÙNG ĐỊNH NGHĨA (1 FUNCTION DUY NHẤT)
-- --------------------------------------------------------------------
-- Phân hệ RETURN RECEIPT: Tính tổng tiền đã hoàn trả theo mã hóa đơn
CREATE FUNCTION dbo.fn_TongTienHoanTraTheoHoaDon (@InvoiceId VARCHAR(36))
RETURNS DECIMAL(12, 2)
AS
BEGIN
    DECLARE @TotalRefund DECIMAL(12, 2);
    SELECT @TotalRefund = SUM(refund_amount)
    FROM dbo.return_receipt
    WHERE original_invoice_id = @InvoiceId;
    RETURN ISNULL(@TotalRefund, 0);
END;
GO

-- --------------------------------------------------------------------
-- 6. THỦ TỤC LƯU TRỮ VÀ GIAO DỊCH (1 STORED PROCEDURE & 1 TRANSACTION)
-- --------------------------------------------------------------------
-- Phân hệ RETURN RECEIPT: Lập phiếu đổi trả an toàn với Giao dịch (TRANSACTION)
CREATE PROCEDURE dbo.sp_LapPhieuTraHang
    @ReturnReceiptId VARCHAR(36),
    @InvoiceId VARCHAR(36),
    @EmployeeId VARCHAR(36),
    @RefundAmount DECIMAL(12, 2),
    @Reason NVARCHAR(MAX)
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        -- Bắt đầu Giao dịch (Transaction) đảm bảo tính toàn vẹn ACID
        BEGIN TRANSACTION;

        -- 1. Kiểm tra hóa đơn gốc có tồn tại hay không
        IF NOT EXISTS (SELECT 1 FROM dbo.invoice WHERE invoice_id = @InvoiceId)
        BEGIN
            RAISERROR (N'Giao dịch thất bại: Hóa đơn gốc không tồn tại!', 16, 1);
        END;

        -- 2. Kiểm soát tài chính: Gọi Function kiểm tra tổng tiền hoàn trả
        -- Chặn hoàn tiền nếu (Số tiền đã hoàn + Số tiền yêu cầu lần này) > Tổng tiền hóa đơn gốc
        DECLARE @OriginalTotal DECIMAL(12, 2);
        DECLARE @AlreadyRefunded DECIMAL(12, 2);

        SELECT @OriginalTotal = original_total 
        FROM dbo.invoice 
        WHERE invoice_id = @InvoiceId;

        SET @AlreadyRefunded = dbo.fn_TongTienHoanTraTheoHoaDon(@InvoiceId);

        IF (@AlreadyRefunded + @RefundAmount > @OriginalTotal)
        BEGIN
            RAISERROR (N'Giao dịch thất bại: Tổng số tiền hoàn trả vượt quá giá trị hóa đơn gốc!', 16, 1);
        END;

        -- 3. Ghi nhận phiếu đổi trả hàng với thời gian chuẩn máy chủ
        INSERT INTO dbo.return_receipt (return_receipt_id, original_invoice_id, employee_id, refund_amount, return_reason, requested_at)
        VALUES (@ReturnReceiptId, @InvoiceId, @EmployeeId, @RefundAmount, @Reason, SYSUTCDATETIME());

        -- Cam kết lưu dữ liệu thành công
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        -- Tự động hoàn tác (Rollback) toàn bộ thao tác nếu xảy ra bất kỳ lỗi nào
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO

-- --------------------------------------------------------------------
-- 7. KHỞI TẠO NGƯỜI DÙNG (USERS) VÀ VAI TRÒ (ROLES) AN TOÀN
-- --------------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'user_owner')
    CREATE USER user_owner FOR LOGIN login_owner;
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'user_cashier')
    CREATE USER user_cashier FOR LOGIN login_cashier;
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'user_inventory')
    CREATE USER user_inventory FOR LOGIN login_inventory;
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'user_customer')
    CREATE USER user_customer FOR LOGIN login_customer;
GO

IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'role_owner' AND type = 'R')
    CREATE ROLE role_owner;
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'role_cashier' AND type = 'R')
    CREATE ROLE role_cashier;
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'role_inventory' AND type = 'R')
    CREATE ROLE role_inventory;
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'role_customer' AND type = 'R')
    CREATE ROLE role_customer;
GO

-- Gán User vào Role (An toàn tuyệt đối khi chạy lại nhiều lần)
IF ISNULL(IS_ROLEMEMBER('role_owner', 'user_owner'), 0) = 0
    ALTER ROLE role_owner ADD MEMBER user_owner;
IF ISNULL(IS_ROLEMEMBER('role_cashier', 'user_cashier'), 0) = 0
    ALTER ROLE role_cashier ADD MEMBER user_cashier;
IF ISNULL(IS_ROLEMEMBER('role_inventory', 'user_inventory'), 0) = 0
    ALTER ROLE role_inventory ADD MEMBER user_inventory;
IF ISNULL(IS_ROLEMEMBER('role_customer', 'user_customer'), 0) = 0
    ALTER ROLE role_customer ADD MEMBER user_customer;
GO

-- --------------------------------------------------------------------
-- 8. CẤP QUYỀN (GRANT) - TẬP TRUNG 3 PHÂN HỆ: AUTH, RETURN, FEEDBACK
-- --------------------------------------------------------------------

-- 8.1. CHỦ CỬA HÀNG (role_owner): Toàn quyền quản trị nhân sự, duyệt đổi trả và xem phản hồi
GRANT CONTROL ON SCHEMA::dbo TO role_owner;
GO

-- 8.2. THU NGÂN (role_cashier): Phụ trách phân hệ Đổi trả hàng (Return Receipt)
GRANT SELECT, INSERT ON dbo.return_receipt TO role_cashier;
GRANT SELECT ON dbo.v_danh_sach_phieu_tra_hang TO role_cashier;
GRANT EXECUTE ON dbo.sp_LapPhieuTraHang TO role_cashier;
GRANT EXECUTE ON dbo.fn_TongTienHoanTraTheoHoaDon TO role_cashier;
GO

-- 8.3. KHÁCH HÀNG (role_customer): Phụ trách phân hệ Phản hồi & Đánh giá (Feedback)
GRANT INSERT ON dbo.store_feedback TO role_customer;
GO


-- --------------------------------------------------------------------
-- 9. CẤM QUYỀN (DENY) - BẢO MẬT & RIÊNG TƯ TRÊN 3 PHÂN HỆ
-- --------------------------------------------------------------------

-- 9.1. BẢO MẬT PHÂN HỆ AUTH:
-- Cấm Thu ngân, Quản lý kho, Khách hàng can thiệp vào thông tin tài khoản nhân viên
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.employee TO role_cashier;
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.employee TO role_inventory;
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.employee TO role_customer;

-- 9.2. BẢO MẬT PHÂN HỆ RETURN RECEIPT (ĐỔI TRẢ HÀNG):
-- Cấm Quản lý kho và Khách hàng xem hoặc lập phiếu đổi trả tiền
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.return_receipt TO role_inventory;
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.return_receipt TO role_customer;
DENY SELECT ON dbo.v_danh_sach_phieu_tra_hang TO role_inventory;
DENY SELECT ON dbo.v_danh_sach_phieu_tra_hang TO role_customer;
DENY EXECUTE ON dbo.sp_LapPhieuTraHang TO role_inventory;
DENY EXECUTE ON dbo.sp_LapPhieuTraHang TO role_customer;

-- 9.3. BẢO MẬT PHÂN HỆ FEEDBACK (ĐÁNH GIÁ DỊCH VỤ):
-- Khách hàng chỉ được gửi đánh giá; CẤM XEM, SỬA, XÓA đánh giá của khách khác
DENY SELECT, UPDATE, DELETE ON dbo.store_feedback TO role_customer;
DENY SELECT ON dbo.v_danh_gia_khach_hang TO role_customer;
GO
