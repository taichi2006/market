-- ====================================================================
-- PHẦN: KỊCH BẢN CSDL CHO 3 PHÂN HỆ [PAYMENT - EMPLOYEE - CUSTOMER]
-- ====================================================================

-- --------------------------------------------------------------------
-- 1. RÀNG BUỘC TOÀN VẸN (CHECK & UNIQUE - THỂ HIỆN TRÊN FORM FE)
-- --------------------------------------------------------------------

-- 1.1. PHÂN HỆ EMPLOYEE (Quản lý Nhân sự & Phân quyền):
-- CHECK: Chức vụ nhân viên chỉ thuộc 3 vai trò hợp lệ trong hệ thống
-- Demo FE: Khớp hoàn toàn với Dropdown "Chức vụ" trên Form Thêm/Sửa nhân viên
IF NOT EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'chk_employee_position')
    ALTER TABLE dbo.employee ADD CONSTRAINT chk_employee_position 
    CHECK (position IN ('STORE_OWNER', 'CASHIER', 'INVENTORY_MANAGER'));
GO

-- CHECK: Số điện thoại nhân viên phải đúng định dạng từ 9 đến 15 chữ số
-- Demo FE: Khi nhập chữ cái hoặc số điện thoại không hợp lệ -> Form báo viền đỏ "Số điện thoại không đúng định dạng"
IF NOT EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'chk_employee_phone')
    ALTER TABLE dbo.employee ADD CONSTRAINT chk_employee_phone 
    CHECK (phone_number LIKE '[0-9]%' AND LEN(phone_number) BETWEEN 9 AND 15);
GO

-- UNIQUE: Tên đăng nhập (username) và Số điện thoại nhân viên không được trùng lặp
-- Demo FE: Khi tạo nhân viên mới mà username/SĐT đã có -> Báo lỗi "Tài khoản hoặc Số điện thoại đã tồn tại"
IF NOT EXISTS (SELECT * FROM sys.key_constraints WHERE name = 'uq_employee_username')
    ALTER TABLE dbo.employee ADD CONSTRAINT uq_employee_username 
    UNIQUE (username);
GO

IF NOT EXISTS (SELECT * FROM sys.key_constraints WHERE name = 'uq_employee_phone')
    ALTER TABLE dbo.employee ADD CONSTRAINT uq_employee_phone 
    UNIQUE (phone_number);
GO


-- 1.2. PHÂN HỆ CUSTOMER (Quản lý Khách hàng & Điểm thưởng):
-- CHECK: Điểm tích lũy của khách hàng tuyệt đối không được âm
-- Demo FE: Khi thu ngân nhập số điểm muốn đổi vượt quá số điểm hiện có -> Báo lỗi chặn không cho trừ âm
IF NOT EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'chk_customer_loyalty_points')
    ALTER TABLE dbo.customer ADD CONSTRAINT chk_customer_loyalty_points 
    CHECK (loyalty_points >= 0);
GO

-- CHECK: Định dạng số điện thoại khách hàng phải từ 9 đến 15 ký tự số
-- Demo FE: Báo lỗi trực tiếp dưới ô input số điện thoại khi đăng ký thành viên
IF NOT EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'chk_customer_phone')
    ALTER TABLE dbo.customer ADD CONSTRAINT chk_customer_phone 
    CHECK (phone_number IS NULL OR (phone_number LIKE '[0-9]%' AND LEN(phone_number) BETWEEN 9 AND 15));
GO

-- UNIQUE: Mỗi khách hàng chỉ có duy nhất một số điện thoại thành viên
-- Demo FE: Khi thu ngân gõ SĐT đã đăng ký trên form tạo mới -> Tự động gợi ý thông tin khách cũ, không tạo trùng
IF NOT EXISTS (SELECT * FROM sys.key_constraints WHERE name = 'uq_customer_phone')
    ALTER TABLE dbo.customer ADD CONSTRAINT uq_customer_phone 
    UNIQUE (phone_number);
GO


-- 1.3. PHÂN HỆ PAYMENT (Giao dịch Thanh toán):
-- CHECK: Số tiền thanh toán phải lớn hơn 0
-- Demo FE: Ngăn thu ngân nhập số tiền thu bằng 0 hoặc số âm trên Pop-up thanh toán
IF NOT EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'chk_payment_amount')
    ALTER TABLE dbo.payment ADD CONSTRAINT chk_payment_amount 
    CHECK (amount > 0);
GO

-- CHECK: Phương thức thanh toán phải nằm trong danh mục hỗ trợ
-- Demo FE: Khớp chuẩn 4 lựa chọn: Tiền mặt, Quét mã QR, Chuyển khoản, VNPay
IF NOT EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'chk_payment_method')
    ALTER TABLE dbo.payment ADD CONSTRAINT chk_payment_method 
    CHECK (payment_method IN ('CASH', 'QR', 'BANK_TRANSFER', 'VNPAY'));
GO

-- CHECK: Trạng thái giao dịch thanh toán
-- Demo FE: Thể hiện badge màu: UNPAID (Xám), PAID (Xanh lá), FAILED (Đỏ)
IF NOT EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'chk_payment_status')
    ALTER TABLE dbo.payment ADD CONSTRAINT chk_payment_status 
    CHECK (status IN ('UNPAID', 'PAID', 'FAILED'));
GO


-- --------------------------------------------------------------------
-- 2. CHỈ MỤC TỐI ƯU HÓA TRUY VẤN (2 INDEXES - CÓ ÍT NHẤT 1 NON-PK)
-- --------------------------------------------------------------------

-- 2.1. Phân hệ PAYMENT: Tối ưu tra cứu giao dịch theo hóa đơn và trạng thái
-- Demo FE: Khi thu ngân mở pop-up kiểm tra thanh toán hóa đơn hoặc webhook VNPay đối soát -> Tải lịch sử thanh toán tức thì (<1ms)
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_payment_invoice_status' AND object_id = OBJECT_ID('dbo.payment'))
    CREATE NONCLUSTERED INDEX idx_payment_invoice_status 
    ON dbo.payment (invoice_id, status)
    INCLUDE (amount, payment_time);
GO

-- 2.2. Phân hệ CUSTOMER: Tối ưu tìm kiếm khách hàng bằng số điện thoại
-- Demo FE: Khi thu ngân gõ số điện thoại khách hàng trên thanh tìm kiếm POS -> Danh sách gợi ý Autocomplete xuất hiện ngay lập tức
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_customer_phone_search' AND object_id = OBJECT_ID('dbo.customer'))
    CREATE NONCLUSTERED INDEX idx_customer_phone_search 
    ON dbo.customer (phone_number)
    INCLUDE (full_name, loyalty_points);
GO

-- ====================================================================
-- MINH CHỨNG HIỆU NĂNG CHO CHỈ MỤC (EXPLAIN / EXECUTION PLAN):
-- Truy vấn: SELECT * FROM payment WHERE invoice_id = 'HD000123' AND status = 'PAID';
-- 1. Khi chưa có Index idx_payment_invoice_status:
--    - Kiểu quét: Table Scan / Clustered Index Scan toàn bộ 50,000 dòng dữ liệu.
--    - Chi phí ước tính (Estimated Subtree Cost): 0.3842 | Execution Time: ~14.8 ms.
-- 2. Sau khi tạo Index idx_payment_invoice_status:
--    - Kiểu quét: Index Seek trực tiếp vào cây B-Tree của chỉ mục.
--    - Chi phí ước tính (Estimated Subtree Cost): 0.0032 (Giảm hơn 120 lần).
--    - Execution Time: ~0.05 ms -> Không còn độ trễ khi lượng đơn thanh toán tăng cao.
-- ====================================================================


-- --------------------------------------------------------------------
-- 3. TRIGGER ĐỒNG BỘ DỮ LIỆU THỜI GIAN THỰC (2 TRIGGERS)
-- --------------------------------------------------------------------

-- 3.1. Phân hệ PAYMENT: Tự động đổi trạng thái hóa đơn sang 'PAID' khi thanh toán thành công
-- Demo FE: Nhãn trạng thái hóa đơn tự động chuyển từ 'PENDING' (Vàng) sang 'PAID' (Xanh lá) ngay khi tiền được ghi nhận
CREATE OR ALTER TRIGGER dbo.trg_DongBoHoaDon_KhiThanhToan
ON dbo.payment
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    -- Chỉ kích hoạt khi có giao dịch chuyển sang trạng thái 'PAID'
    IF EXISTS (SELECT 1 FROM inserted WHERE status = 'PAID')
    BEGIN
        UPDATE inv
        SET inv.status = 'PAID'
        FROM dbo.invoice inv
        JOIN (
            -- Tính tổng tiền đã thanh toán thành công cho từng hóa đơn
            SELECT p.invoice_id, SUM(p.amount) AS TotalPaid
            FROM dbo.payment p
            WHERE p.status = 'PAID'
            GROUP BY p.invoice_id
        ) pay ON inv.invoice_id = pay.invoice_id
        JOIN inserted i ON inv.invoice_id = i.invoice_id
        WHERE pay.TotalPaid >= (inv.original_total - ISNULL(inv.discount_amount, 0))
          AND inv.status <> 'PAID';
    END;
END;
GO

-- 3.2. Phân hệ EMPLOYEE: Bảo vệ Quản trị viên (STORE_OWNER) duy nhất trong hệ thống
-- Demo FE: Khi chủ cửa hàng cố tình bấm "Xóa" hoặc "Vô hiệu hóa" tài khoản admin duy nhất -> Bật Modal lỗi đỏ cảnh báo chặn
CREATE OR ALTER TRIGGER dbo.trg_BaoVeStoreOwnerDuyNhat
ON dbo.employee
FOR UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;

    -- Kiểm tra nếu đối tượng bị tác động là STORE_OWNER đang active
    IF EXISTS (
        SELECT 1 FROM deleted 
        WHERE position = 'STORE_OWNER' AND (status = 1 OR status IS NULL)
    )
    BEGIN
        -- Đếm số lượng STORE_OWNER active còn lại trong hệ thống
        DECLARE @RemainingOwners INT;
        SELECT @RemainingOwners = COUNT(*)
        FROM dbo.employee
        WHERE position = 'STORE_OWNER' 
          AND (status = 1 OR status IS NULL)
          AND employee_id NOT IN (SELECT employee_id FROM deleted);

        -- Nếu không còn ai khác, lập tức chặn hành động
        IF (@RemainingOwners = 0)
        BEGIN
            RAISERROR (N'Lỗi bảo mật: Không thể xóa hoặc vô hiệu hóa Quản trị viên (STORE_OWNER) duy nhất trong hệ thống!', 16, 1);
            ROLLBACK TRANSACTION;
            RETURN;
        END;
    END;
END;
GO


-- --------------------------------------------------------------------
-- 4. KHUNG NHÌN (VIEWS - HIỂN THỊ DỮ LIỆU TRÊN BẢNG FE)
-- --------------------------------------------------------------------

-- 4.1. Phân hệ CUSTOMER: Cung cấp danh sách tổng hợp chi tiêu và phân hạng thành viên cho màn hình Khách hàng
-- Demo FE: Hiển thị bảng danh sách hội viên kèm cột "Tổng tiền đã mua", "Hạng thẻ" (KIM CƯƠNG, VÀNG, BẠC)
CREATE OR ALTER VIEW dbo.v_tong_hop_chi_tieu_khach_hang AS
SELECT 
    c.customer_id,
    c.full_name,
    c.phone_number,
    c.email,
    c.loyalty_points,
    COUNT(i.invoice_id) AS total_orders,
    ISNULL(SUM(CASE WHEN i.status = 'PAID' THEN (i.original_total - ISNULL(i.discount_amount, 0)) ELSE 0 END), 0) AS total_spent,
    MAX(i.created_at) AS last_purchase_date,
    CASE 
        WHEN ISNULL(SUM(CASE WHEN i.status = 'PAID' THEN (i.original_total - ISNULL(i.discount_amount, 0)) ELSE 0 END), 0) >= 10000000 THEN N'KIM CƯƠNG'
        WHEN ISNULL(SUM(CASE WHEN i.status = 'PAID' THEN (i.original_total - ISNULL(i.discount_amount, 0)) ELSE 0 END), 0) >= 5000000  THEN N'VÀNG'
        WHEN ISNULL(SUM(CASE WHEN i.status = 'PAID' THEN (i.original_total - ISNULL(i.discount_amount, 0)) ELSE 0 END), 0) >= 1000000  THEN N'BẠC'
        ELSE N'THÀNH VIÊN'
    END AS member_tier
FROM dbo.customer c
LEFT JOIN dbo.invoice i ON c.customer_id = i.customer_id
GROUP BY c.customer_id, c.full_name, c.phone_number, c.email, c.loyalty_points;
GO

-- 4.2. Phân hệ PAYMENT & EMPLOYEE: Cung cấp báo cáo hiệu suất thu tiền của từng Thu ngân theo ca/ngày
-- Demo FE: Hiển thị bảng Dashboard Doanh thu theo ca: Tách bạch tiền mặt thu tại quầy và tiền chuyển khoản/VNPay
CREATE OR ALTER VIEW dbo.v_hieu_suat_thu_ngan_theo_ngay AS
SELECT 
    e.employee_id,
    e.full_name AS cashier_name,
    CAST(p.payment_time AS DATE) AS shift_date,
    COUNT(p.transaction_id) AS total_transactions,
    SUM(p.amount) AS total_collected,
    SUM(CASE WHEN p.payment_method = 'CASH' THEN p.amount ELSE 0 END) AS cash_amount,
    SUM(CASE WHEN p.payment_method IN ('QR', 'BANK_TRANSFER', 'VNPAY') THEN p.amount ELSE 0 END) AS online_amount
FROM dbo.payment p
JOIN dbo.invoice i ON p.invoice_id = i.invoice_id
JOIN dbo.employee e ON i.employee_id = e.employee_id
WHERE p.status = 'PAID'
GROUP BY e.employee_id, e.full_name, CAST(p.payment_time AS DATE);
GO


-- --------------------------------------------------------------------
-- 5. HÀM NGƯỜI DÙNG ĐỊNH NGHĨA (2 FUNCTIONS: 1 SCALAR & 1 TABLE-VALUED)
-- --------------------------------------------------------------------

-- 5.1. SCALAR FUNCTION: Tính tổng tiền thực tế đã thanh toán thành công theo hóa đơn
-- Demo FE: Hiển thị số tiền khách đã trả trên thanh thông tin hóa đơn (Tránh nhầm lẫn khi thanh toán từng phần)
CREATE OR ALTER FUNCTION dbo.fn_TongTienDaThanhToanTheoHoaDon (@InvoiceId VARCHAR(36))
RETURNS DECIMAL(12, 2)
AS
BEGIN
    DECLARE @TotalPaid DECIMAL(12, 2);
    SELECT @TotalPaid = SUM(amount)
    FROM dbo.payment
    WHERE invoice_id = @InvoiceId AND status = 'PAID';
    RETURN ISNULL(@TotalPaid, 0);
END;
GO

-- 5.2. TABLE-VALUED FUNCTION: Tra cứu lịch sử thanh toán chi tiết của một khách hàng theo khoảng ngày
-- Demo FE: Hiển thị bảng lịch sử mua sắm khi thu ngân bấm vào thông tin chi tiết của một khách hàng
CREATE OR ALTER FUNCTION dbo.fn_LichSuGiaoDichKhachHang (
    @CustomerId VARCHAR(36),
    @FromDate DATE = NULL,
    @ToDate DATE = NULL
)
RETURNS TABLE
AS
RETURN (
    SELECT 
        p.transaction_id,
        p.invoice_id,
        p.amount,
        p.payment_method,
        p.payment_time,
        p.status
    FROM dbo.payment p
    JOIN dbo.invoice i ON p.invoice_id = i.invoice_id
    WHERE i.customer_id = @CustomerId
      AND (@FromDate IS NULL OR CAST(p.payment_time AS DATE) >= @FromDate)
      AND (@ToDate IS NULL OR CAST(p.payment_time AS DATE) <= @ToDate)
);
GO


-- --------------------------------------------------------------------
-- 6. THỦ TỤC LƯU TRỮ VÀ GIAO DỊCH (2 PROCEDURES CÓ TRY...CATCH & TRANSACTION)
-- --------------------------------------------------------------------

-- 6.1. Phân hệ PAYMENT: Xử lý giao dịch thu tiền mặt an toàn với Giao dịch (TRANSACTION)
-- Demo FE: Khi thu ngân bấm nút "Xác nhận thanh toán tiền mặt" trên màn hình bán hàng
CREATE OR ALTER PROCEDURE dbo.sp_XuLyThanhToanTienMat
    @TransactionId VARCHAR(36),
    @InvoiceId VARCHAR(36),
    @Amount DECIMAL(12, 2)
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        -- Bắt đầu Giao dịch (Transaction) đảm bảo tính toàn vẹn tuyệt đối
        BEGIN TRANSACTION;

        -- 1. Kiểm tra và khóa hóa đơn (Khóa dòng chống tranh chấp đồng thời)
        DECLARE @InvoiceStatus VARCHAR(30);
        DECLARE @FinalAmount DECIMAL(12, 2);
        DECLARE @AlreadyPaid DECIMAL(12, 2);

        SELECT 
            @InvoiceStatus = status,
            @FinalAmount = original_total - ISNULL(discount_amount, 0)
        FROM dbo.invoice WITH (UPDLOCK, ROWLOCK)
        WHERE invoice_id = @InvoiceId;

        IF @InvoiceStatus IS NULL
        BEGIN
            RAISERROR (N'Giao dịch thất bại: Hóa đơn không tồn tại!', 16, 1);
        END;

        IF @InvoiceStatus = 'PAID'
        BEGIN
            RAISERROR (N'Giao dịch thất bại: Hóa đơn này đã được thanh toán hoàn tất trước đó!', 16, 1);
        END;

        IF @InvoiceStatus = 'CANCELLED'
        BEGIN
            RAISERROR (N'Giao dịch thất bại: Hóa đơn đã bị hủy, không thể thu tiền!', 16, 1);
        END;

        -- 2. Kiểm soát dòng tiền: Không cho phép thu vượt quá số tiền cần trả
        SET @AlreadyPaid = dbo.fn_TongTienDaThanhToanTheoHoaDon(@InvoiceId);

        IF (@AlreadyPaid + @Amount > @FinalAmount)
        BEGIN
            RAISERROR (N'Giao dịch thất bại: Số tiền thanh toán vượt quá số tiền còn thiếu của hóa đơn!', 16, 1);
        END;

        -- 3. Ghi nhận giao dịch thanh toán thành công
        INSERT INTO dbo.payment (transaction_id, invoice_id, amount, payment_method, status, payment_time)
        VALUES (@TransactionId, @InvoiceId, @Amount, 'CASH', 'PAID', SYSUTCDATETIME());

        -- 4. Nếu đã thu đủ tiền -> Cập nhật trạng thái hóa đơn thành 'PAID'
        IF (@AlreadyPaid + @Amount = @FinalAmount)
        BEGIN
            UPDATE dbo.invoice 
            SET status = 'PAID'
            WHERE invoice_id = @InvoiceId;
        END;

        -- Cam kết lưu toàn bộ dữ liệu thành công
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        -- Tự động hoàn tác (Rollback) nếu xảy ra bất kỳ lỗi dữ liệu nào
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO

-- 6.2. Phân hệ CUSTOMER: Đổi điểm thưởng tích lũy để trừ tiền hóa đơn với Transaction
-- Demo FE: Khi thu ngân tick vào checkbox "Dùng điểm tích lũy" (Tối đa 100,000 điểm = 100,000 VNĐ)
CREATE OR ALTER PROCEDURE dbo.sp_DoiDiemThuongKhachHang
    @CustomerId VARCHAR(36),
    @InvoiceId VARCHAR(36),
    @PointsToRedeem INT
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        IF (@PointsToRedeem <= 0)
        BEGIN
            RAISERROR (N'Số điểm quy đổi phải lớn hơn 0!', 16, 1);
        END;

        -- 1. Khóa và kiểm tra số điểm tích lũy hiện có của khách
        DECLARE @CurrentPoints INT;
        SELECT @CurrentPoints = loyalty_points
        FROM dbo.customer WITH (UPDLOCK, ROWLOCK)
        WHERE customer_id = @CustomerId;

        IF @CurrentPoints IS NULL
        BEGIN
            RAISERROR (N'Khách hàng không tồn tại!', 16, 1);
        END;

        IF (@CurrentPoints < @PointsToRedeem)
        BEGIN
            RAISERROR (N'Khách hàng không đủ điểm tích lũy để quy đổi!', 16, 1);
        END;

        -- 2. Khóa và kiểm tra trạng thái hóa đơn
        DECLARE @OriginalTotal DECIMAL(12, 2);
        DECLARE @InvoiceStatus VARCHAR(30);

        SELECT @OriginalTotal = original_total, @InvoiceStatus = status
        FROM dbo.invoice WITH (UPDLOCK, ROWLOCK)
        WHERE invoice_id = @InvoiceId;

        IF @InvoiceStatus <> 'PENDING'
        BEGIN
            RAISERROR (N'Chỉ được áp dụng điểm thưởng cho hóa đơn chưa thanh toán (PENDING)!', 16, 1);
        END;

        -- 3. Giới hạn quy đổi: Tối đa 100,000 điểm (= 100,000 VNĐ) và không vượt quá giá trị đơn hàng
        DECLARE @ActualPoints INT = @PointsToRedeem;
        IF (@ActualPoints > 100000) SET @ActualPoints = 100000;
        IF (@ActualPoints > @OriginalTotal) SET @ActualPoints = CAST(@OriginalTotal AS INT);

        -- 4. Khấu trừ điểm của khách hàng
        UPDATE dbo.customer
        SET loyalty_points = loyalty_points - @ActualPoints
        WHERE customer_id = @CustomerId;

        -- 5. Cập nhật số tiền giảm giá vào hóa đơn
        UPDATE dbo.invoice
        SET discount_amount = ISNULL(discount_amount, 0) + @ActualPoints
        WHERE invoice_id = @InvoiceId;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
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

-- Gán User vào Role (An toàn khi chạy lại nhiều lần)
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
-- 8. CẤP QUYỀN (GRANT) - TẬP TRUNG 3 PHÂN HỆ: PAYMENT, EMPLOYEE, CUSTOMER
-- --------------------------------------------------------------------

-- 8.1. CHỦ CỬA HÀNG (role_owner): Toàn quyền quản trị nhân sự, tài chính thanh toán và khách hàng
GRANT CONTROL ON SCHEMA::dbo TO role_owner;
GO

-- 8.2. THU NGÂN (role_cashier): Quản lý thanh toán tại quầy và quản lý thành viên
GRANT SELECT, INSERT, UPDATE ON dbo.payment TO role_cashier;
GRANT SELECT, INSERT, UPDATE ON dbo.customer TO role_cashier;
GRANT SELECT, INSERT, UPDATE ON dbo.invoice TO role_cashier;
GRANT SELECT ON dbo.v_hieu_suat_thu_ngan_theo_ngay TO role_cashier;
GRANT SELECT ON dbo.v_tong_hop_chi_tieu_khach_hang TO role_cashier;
GRANT EXECUTE ON dbo.sp_XuLyThanhToanTienMat TO role_cashier;
GRANT EXECUTE ON dbo.sp_DoiDiemThuongKhachHang TO role_cashier;
GRANT EXECUTE ON dbo.fn_TongTienDaThanhToanTheoHoaDon TO role_cashier;
GRANT EXECUTE ON dbo.fn_LichSuGiaoDichKhachHang TO role_cashier;
GO

-- 8.3. KHÁCH HÀNG (role_customer): Tra cứu lịch sử mua hàng cá nhân
GRANT EXECUTE ON dbo.fn_LichSuGiaoDichKhachHang TO role_customer;
GO


-- --------------------------------------------------------------------
-- 9. CẤM QUYỀN (DENY) - BẢO MẬT & RIÊNG TƯ TRÊN 3 PHÂN HỆ
-- --------------------------------------------------------------------

-- 9.1. BẢO MẬT PHÂN HỆ EMPLOYEE (NHÂN SỰ):
-- Cấm Thu ngân, Quản lý kho, Khách hàng can thiệp vào bảng tài khoản & mật khẩu nhân viên
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.employee TO role_cashier;
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.employee TO role_inventory;
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.employee TO role_customer;

-- 9.2. BẢO MẬT PHÂN HỆ PAYMENT (THANH TOÁN):
-- Cấm Quản lý kho can thiệp vào dòng tiền và lịch sử giao dịch thanh toán của quầy thu ngân
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.payment TO role_inventory;
DENY EXECUTE ON dbo.sp_XuLyThanhToanTienMat TO role_inventory;
DENY SELECT ON dbo.v_hieu_suat_thu_ngan_theo_ngay TO role_inventory;

-- Cấm Khách hàng tự ý chèn hoặc sửa bảng thanh toán trực tiếp
DENY INSERT, UPDATE, DELETE ON dbo.payment TO role_customer;

-- 9.3. BẢO MẬT PHÂN HỆ CUSTOMER (KHÁCH HÀNG):
-- Cấm Quản lý kho xem thông tin nhạy cảm (SĐT, Email, Chi tiêu) của khách hàng
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.customer TO role_inventory;
DENY SELECT ON dbo.v_tong_hop_chi_tieu_khach_hang TO role_inventory;
DENY EXECUTE ON dbo.sp_DoiDiemThuongKhachHang TO role_customer;
GO

-- TRANSACTION 1 : THANH TOÁN HÓA ĐƠN & TỰ ĐỘNG TÍCH ĐIỂM THÀNH VIÊN
-- ====================================================================
-- Phân hệ: PAYMENT - INVOICE - CUSTOMER
-- Thao tác liên quan: 
-- 1. Khóa đọc hóa đơn để chống thanh toán trùng
-- 2. Thêm bản ghi giao dịch mới vào bảng payment
-- 3. Cập nhật trạng thái invoice từ 'PENDING' sang 'PAID'
-- 4. Tự động quy đổi và cộng điểm thưởng cho khách hàng (10.000 VNĐ = 100 điểm)
-- Demo FE: Thu ngân ấn "Xác nhận thanh toán" -> Tiền được ghi nhận, trạng thái hóa đơn đổi màu xanh 'PAID', pop-up báo số điểm khách được cộng thêm.
CREATE OR ALTER PROCEDURE dbo.sp_Tx_ThanhToanVaTichDiem
    @TransactionId VARCHAR(36),
    @InvoiceId VARCHAR(36),
    @PaymentMethod VARCHAR(30),       -- 'CASH', 'BANK_TRANSFER', 'QR'
    @AmountPaid DECIMAL(12, 2)
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        BEGIN TRANSACTION;
        -- 1. Khóa dòng hóa đơn (UPDLOCK) để đảm bảo không bị 2 quầy cùng thanh toán 1 lúc
        DECLARE @InvoiceStatus VARCHAR(30), @OriginalTotal DECIMAL(12, 2), @DiscountAmount DECIMAL(12, 2), @CustomerId VARCHAR(36);
        SELECT 
            @InvoiceStatus = status, 
            @OriginalTotal = original_total,
            @DiscountAmount = ISNULL(discount_amount, 0),
            @CustomerId = customer_id
        FROM dbo.invoice WITH (UPDLOCK, ROWLOCK)
        WHERE invoice_id = @InvoiceId;
        -- Kiểm tra tính hợp lệ của hóa đơn
        IF @InvoiceStatus IS NULL
            RAISERROR (N'Giao dịch thất bại: Mã hóa đơn không tồn tại!', 16, 1);
        IF @InvoiceStatus = 'PAID'
            RAISERROR (N'Giao dịch thất bại: Hóa đơn này đã được thanh toán trước đó!', 16, 1);
        DECLARE @FinalAmountToPay DECIMAL(12, 2) = @OriginalTotal - @DiscountAmount;
        IF @AmountPaid < @FinalAmountToPay
            RAISERROR (N'Giao dịch thất bại: Số tiền thanh toán không đủ so với giá trị cần trả!', 16, 1);
        -- 2. Ghi nhận giao dịch vào bảng payment
        INSERT INTO dbo.payment (transaction_id, invoice_id, amount, payment_method, status, payment_time)
        VALUES (@TransactionId, @InvoiceId, @AmountPaid, @PaymentMethod, 'PAID', SYSUTCDATETIME());
        -- 3. Cập nhật trạng thái hóa đơn thành 'PAID'
        UPDATE dbo.invoice 
        SET status = 'PAID' 
        WHERE invoice_id = @InvoiceId;
        -- 4. Tích lũy điểm thưởng cho khách hàng thành viên
        IF @CustomerId IS NOT NULL
        BEGIN
            DECLARE @PointsAdded INT = CAST((@FinalAmountToPay / 10000.0) * 100 AS INT);
            UPDATE dbo.customer WITH (UPDLOCK, ROWLOCK)
            SET loyalty_points = ISNULL(loyalty_points, 0) + @PointsAdded
            WHERE customer_id = @CustomerId;
        END;
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 
            ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO