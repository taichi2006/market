-- ====================================================================================
-- DỰ ÁN QUẢN LÝ SIÊU THỊ - TÍNH NĂNG CƠ SỞ DỮ LIỆU NÂNG CAO (DBMS FEATURES)
-- File: quanlysieuthidb_features.sql
-- Áp dụng cho 3 modules: Invoice (Hóa đơn), PurchaseReceipt (Phiếu nhập hàng), DisposalReceipt (Phiếu hủy kho)
-- Hệ Quản Trị Cơ Sở Dữ Liệu: Microsoft SQL Server (T-SQL)
-- ====================================================================================

-- ------------------------------------------------------------------------------------
-- 1. RÀNG BUỘC TOÀN VẸN (CONSTRAINTS: CHECK / UNIQUE / DEFAULT)
-- Đảm bảo tính hợp lệ của dữ liệu ngay ở tầng CSDL theo đúng nghiệp vụ thực tế
-- ------------------------------------------------------------------------------------

-- 1.1. Ràng buộc số lượng tồn kho sản phẩm không được âm
-- Nghiệp vụ: Tồn kho chỉ có thể giảm về 0, không bao giờ được phép mang giá trị âm khi bán hàng hoặc xuất hủy.
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'chk_product_stock_non_negative')
BEGIN
    ALTER TABLE product
    ADD CONSTRAINT chk_product_stock_non_negative 
    CHECK (stock_quantity >= 0);
END;
GO

-- 1.2. Ràng buộc trạng thái hóa đơn hợp lệ
-- Nghiệp vụ: Hóa đơn chỉ được nhận 1 trong 3 trạng thái: PENDING (chờ xử lý), PAID (đã thanh toán), CANCELLED (đã hủy)
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'chk_invoice_status')
BEGIN
    ALTER TABLE invoice
    ADD CONSTRAINT chk_invoice_status 
    CHECK (status IN ('PENDING', 'PAID', 'CANCELLED'));
END;
GO

-- 1.3. Ràng buộc số tiền giảm giá từ điểm tích lũy hợp lệ
-- Nghiệp vụ: Tiền giảm giá phải >= 0 và không được vượt quá tổng tiền gốc của hóa đơn
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'chk_invoice_discount_range')
BEGIN
    ALTER TABLE invoice
    ADD CONSTRAINT chk_invoice_discount_range 
    CHECK (discount_amount >= 0 AND discount_amount <= original_total);
END;
GO

-- 1.4. Ràng buộc số lượng xuất hủy phải lớn hơn 0
-- Nghiệp vụ: Mỗi dòng xuất hủy kho bắt buộc phải có số lượng tiêu hủy > 0
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'chk_disposal_quantity_positive')
BEGIN
    ALTER TABLE disposal_receipt_detail
    ADD CONSTRAINT chk_disposal_quantity_positive 
    CHECK (disposed_quantity > 0);
END;
GO


-- ------------------------------------------------------------------------------------
-- 2. CHỈ MỤC (INDEXES)
-- Tối ưu hóa hiệu năng truy vấn, lọc, phân trang và sắp xếp dữ liệu
-- ------------------------------------------------------------------------------------

-- 2.1. Index tối ưu hóa tìm kiếm và phân trang hóa đơn theo thời gian tạo và trạng thái
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_invoice_created_status' AND object_id = OBJECT_ID('invoice'))
BEGIN
    CREATE INDEX idx_invoice_created_status 
    ON invoice(created_at DESC, status);
END;
GO

-- 2.2. Index tối ưu hóa lọc chứng từ (Voucher: Nhập kho, Hủy kho) theo nhân viên và thời gian
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_voucher_created_employee' AND object_id = OBJECT_ID('voucher'))
BEGIN
    CREATE INDEX idx_voucher_created_employee 
    ON voucher(created_at DESC, employee_id);
END;
GO

-- 2.3. Index tối ưu hóa liên kết chi tiết hóa đơn
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_invoice_detail_invoice' AND object_id = OBJECT_ID('invoice_detail'))
BEGIN
    CREATE INDEX idx_invoice_detail_invoice 
    ON invoice_detail(invoice_id);
END;
GO


-- ------------------------------------------------------------------------------------
-- 3. HÀM DO NGƯỜI DÙNG ĐỊNH NGHĨA (USER-DEFINED FUNCTIONS - UDF)
-- Chuyển đổi các logic tính toán nghiệp vụ từ tầng Java Service sang CSDL
-- ------------------------------------------------------------------------------------

-- 3.1. SCALAR FUNCTION: Tính điểm tích lũy thành viên nhận được từ hóa đơn
-- Nghiệp vụ (từ InvoiceService.java): Mỗi 10,000 VNĐ tiền hàng được tích 100 điểm thưởng
CREATE OR ALTER FUNCTION dbo.fn_CalculateLoyaltyPointsEarned(@OriginalTotal DECIMAL(12, 2))
RETURNS INT
AS
BEGIN
    IF @OriginalTotal IS NULL OR @OriginalTotal < 10000
        RETURN 0;

    -- Lấy phần nguyên của (tổng tiền / 10000) * 100
    RETURN CAST(FLOOR(@OriginalTotal / 10000.0) * 100 AS INT);
END;
GO

-- 3.2. SCALAR FUNCTION: Tính số tiền giảm giá từ điểm tích lũy của khách hàng
-- Nghiệp vụ (từ InvoiceService.java):
-- 1 điểm = 1 VNĐ, tối đa sử dụng 100,000 điểm cho mỗi đơn hàng, và không vượt quá tổng tiền đơn
CREATE OR ALTER FUNCTION dbo.fn_CalculateLoyaltyDiscount(
    @CustomerId VARCHAR(36),
    @OriginalTotal DECIMAL(12, 2)
)
RETURNS DECIMAL(12, 2)
AS
BEGIN
    IF @CustomerId IS NULL OR @OriginalTotal IS NULL OR @OriginalTotal <= 0
        RETURN 0;

    DECLARE @CurrentPoints INT = 0;
    SELECT @CurrentPoints = COALESCE(loyalty_points, 0)
    FROM customer
    WHERE customer_id = @CustomerId;

    IF @CurrentPoints <= 0
        RETURN 0;

    DECLARE @PointsToUse INT = @CurrentPoints;
    -- Quy tắc trần tối đa 100,000 điểm
    IF @PointsToUse > 100000
        SET @PointsToUse = 100000;

    -- Không được giảm vượt quá tổng tiền hóa đơn
    IF @PointsToUse > CAST(@OriginalTotal AS INT)
        SET @PointsToUse = CAST(@OriginalTotal AS INT);

    RETURN CAST(@PointsToUse AS DECIMAL(12, 2));
END;
GO

-- 3.3. TABLE-VALUED FUNCTION: Lấy chi tiết các mặt hàng của một hóa đơn
-- Nghiệp vụ: Trả về bảng gồm thông tin sản phẩm, số lượng, đơn giá, thành tiền
CREATE OR ALTER FUNCTION dbo.fn_GetInvoiceItemsDetail(@InvoiceId VARCHAR(36))
RETURNS TABLE
AS
RETURN (
    SELECT 
        d.invoice_detail_id,
        d.invoice_id,
        d.product_id,
        p.product_name,
        d.quantity,
        d.unit_price,
        d.subtotal
    FROM invoice_detail d
    JOIN product p ON d.product_id = p.product_id
    WHERE d.invoice_id = @InvoiceId
);
GO


-- ------------------------------------------------------------------------------------
-- 4. KHUNG NHÌN (VIEWS) PHỤC VỤ TRUY VẤN VÀ BÁO CÁO TỔNG HỢP
-- Đơn giản hóa các câu truy vấn phức tạp, tránh nhiều lệnh JOIN rườm rà ở tầng Repository
-- ------------------------------------------------------------------------------------

-- 4.1. VIEW: Tổng hợp thông tin danh sách hóa đơn bán hàng
-- Nghiệp vụ: Kết hợp hóa đơn với nhân viên thu ngân, khách hàng và tự tính số tiền thực trả final_total
CREATE OR ALTER VIEW dbo.v_invoice_summary AS
SELECT 
    inv.invoice_id,
    inv.created_at,
    inv.status,
    inv.original_total,
    inv.discount_amount,
    (inv.original_total - inv.discount_amount) AS final_total,
    inv.employee_id,
    e.full_name AS employee_name,
    e.username AS employee_username,
    inv.customer_id,
    c.full_name AS customer_name,
    c.phone_number AS customer_phone,
    c.loyalty_points AS customer_loyalty_points
FROM invoice inv
LEFT JOIN employee e ON inv.employee_id = e.employee_id
LEFT JOIN customer c ON inv.customer_id = c.customer_id;
GO

-- 4.2. VIEW: Báo cáo tổng hợp phiếu xuất hủy kho
-- Nghiệp vụ: Tự động tổng hợp số lượng chủng loại hàng hủy và tổng số lượng sản phẩm xuất hủy
CREATE OR ALTER VIEW dbo.v_disposal_receipt_summary AS
SELECT 
    dr.voucher_id,
    v.total_amount,
    v.created_at,
    v.employee_id,
    e.full_name AS employee_name,
    e.username AS employee_username,
    COUNT(d.disposal_detail_id) AS total_items,
    COALESCE(SUM(d.disposed_quantity), 0) AS total_disposed_quantity
FROM disposal_receipt dr
JOIN voucher v ON dr.voucher_id = v.voucher_id
LEFT JOIN employee e ON v.employee_id = e.employee_id
LEFT JOIN disposal_receipt_detail d ON dr.voucher_id = d.voucher_id
GROUP BY 
    dr.voucher_id,
    v.total_amount,
    v.created_at,
    v.employee_id,
    e.full_name,
    e.username;
GO

-- 4.3. VIEW: Báo cáo tổng hợp phiếu nhập hàng
-- Nghiệp vụ: Tổng hợp thông tin nhà cung cấp, tổng tiền và tổng số lượng mặt hàng nhập
CREATE OR ALTER VIEW dbo.v_purchase_receipt_summary AS
SELECT 
    pr.voucher_id,
    pr.supplier_name,
    v.total_amount,
    v.created_at,
    v.employee_id,
    e.full_name AS employee_name,
    e.username AS employee_username,
    COUNT(d.purchase_detail_id) AS total_items,
    COALESCE(SUM(d.purchased_quantity), 0) AS total_purchased_quantity
FROM purchase_receipt pr
JOIN voucher v ON pr.voucher_id = v.voucher_id
LEFT JOIN employee e ON v.employee_id = e.employee_id
LEFT JOIN purchase_receipt_detail d ON pr.voucher_id = d.voucher_id
GROUP BY 
    pr.voucher_id,
    pr.supplier_name,
    v.total_amount,
    v.created_at,
    v.employee_id,
    e.full_name,
    e.username;
GO


-- ------------------------------------------------------------------------------------
-- 5. TRIGGER ĐẢM BẢO TỰ ĐỘNG HÓA VÀ TOÀN VẸN DỮ LIỆU
-- Thay thế các vòng lặp kiểm tra và cập nhật tồn kho thủ công từ tầng Java Service
-- ------------------------------------------------------------------------------------

-- 5.1. TRIGGER: Tự động kiểm tra và trừ tồn kho khi thêm chi tiết phiếu hủy
-- Bảng áp dụng: disposal_receipt_detail
-- Nghiệp vụ:
--   1. Kiểm tra tồn kho của sản phẩm: nếu số lượng hủy > tồn kho thì ROLLBACK và báo lỗi.
--   2. Tự động trừ tồn kho sản phẩm mà không cần Java Repository phải lặp qua Map để trừ.
CREATE OR ALTER TRIGGER dbo.trg_DisposalDetail_CheckAndDeductStock
ON disposal_receipt_detail
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    -- Kiểm tra nếu có bất kỳ sản phẩm nào không đủ tồn kho để xuất hủy
    IF EXISTS (
        SELECT 1 
        FROM inserted i
        JOIN product p ON i.product_id = p.product_id
        WHERE p.stock_quantity < i.disposed_quantity
    )
    BEGIN
        RAISERROR (N'Lỗi: Số lượng xuất hủy vượt quá số lượng tồn kho hiện có của sản phẩm.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END;

    -- Tự động trừ tồn kho sản phẩm tương ứng
    UPDATE p
    SET p.stock_quantity = p.stock_quantity - i.disposed_quantity
    FROM product p
    JOIN inserted i ON p.product_id = i.product_id;
END;
GO

-- 5.2. TRIGGER: Tự động cập nhật tồn kho và tổng tiền chứng từ khi thêm chi tiết phiếu nhập
-- Bảng áp dụng: purchase_receipt_detail
-- Nghiệp vụ:
--   1. Tự động cộng tồn kho sản phẩm khi hàng về kho.
--   2. Tự động cập nhật lại hạn sử dụng mới nhất cho sản phẩm.
--   3. Tự động cộng dồn total_amount trên bảng voucher tương ứng.
CREATE OR ALTER TRIGGER dbo.trg_PurchaseReceiptDetail_AfterInsert
ON purchase_receipt_detail
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    -- 1. Tự động tăng tồn kho và cập nhật hạn sử dụng cho sản phẩm
    UPDATE p
    SET p.stock_quantity = p.stock_quantity + i.purchased_quantity,
        p.expiration_date = i.expiration_date
    FROM product p
    JOIN inserted i ON p.product_id = i.product_id;

    -- 2. Tự động cập nhật lại tổng tiền trên Voucher chứng từ
    UPDATE v
    SET v.total_amount = (
        SELECT COALESCE(SUM(d.purchased_quantity * COALESCE(d.purchase_price, 0)), 0)
        FROM purchase_receipt_detail d
        WHERE d.voucher_id = v.voucher_id
    )
    FROM voucher v
    JOIN inserted i ON v.voucher_id = i.voucher_id;
END;
GO


-- ------------------------------------------------------------------------------------
-- 6. THỦ TỤC LƯU TRỮ (STORED PROCEDURES)
-- Áp dụng:
--   - Xử lý lỗi bằng TRY...CATCH
--   - Quản lý giao dịch TRANSACTION (BEGIN / COMMIT / ROLLBACK) đảm bảo tính toàn vẹn đa bảng
-- ------------------------------------------------------------------------------------

-- 6.1. STORED PROCEDURE: Hủy hóa đơn bán hàng (sp_CancelInvoice)
-- Nghiệp vụ:
--   1. Kiểm tra hóa đơn tồn tại và đang ở trạng thái 'PENDING'.
--   2. Hoàn lại tồn kho cho toàn bộ sản phẩm trong chi tiết hóa đơn (invoice_detail).
--   3. Hoàn tác điểm tích lũy của khách hàng:
--      - Nếu đơn dùng giảm giá: hoàn lại điểm tương ứng số tiền giảm giá.
--      - Nếu đơn không dùng giảm giá (được tích điểm): khấu trừ lại số điểm đã cộng.
--   4. Cập nhật trạng thái hóa đơn thành 'CANCELLED'.
--   Toàn bộ nằm trong 1 TRANSACTION, bảo đảm tính nguyên tố (ACID).
CREATE OR ALTER PROCEDURE dbo.sp_CancelInvoice
    @InvoiceId VARCHAR(36)
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- 1. Kiểm tra sự tồn tại của hóa đơn
        DECLARE @CurrentStatus VARCHAR(30);
        DECLARE @CustomerId VARCHAR(36);
        DECLARE @OriginalTotal DECIMAL(12, 2);
        DECLARE @DiscountAmount DECIMAL(12, 2);

        SELECT 
            @CurrentStatus = status,
            @CustomerId = customer_id,
            @OriginalTotal = original_total,
            @DiscountAmount = discount_amount
        FROM invoice
        WHERE invoice_id = @InvoiceId;

        IF @CurrentStatus IS NULL
        BEGIN
            THROW 50001, N'Không tìm thấy hóa đơn với ID tương ứng.', 1;
        END;

        -- 2. Chỉ cho phép hủy hóa đơn ở trạng thái PENDING
        IF UPPER(@CurrentStatus) <> 'PENDING'
        BEGIN
            DECLARE @ErrMsg NVARCHAR(200) = N'Hóa đơn đang ở trạng thái ' + @CurrentStatus + N' (chỉ cho phép hủy hóa đơn PENDING).';
            THROW 50002, @ErrMsg, 1;
        END;

        -- 3. Hoàn lại số lượng tồn kho cho các sản phẩm trong hóa đơn
        UPDATE p
        SET p.stock_quantity = p.stock_quantity + d.quantity
        FROM product p
        JOIN invoice_detail d ON p.product_id = d.product_id
        WHERE d.invoice_id = @InvoiceId;

        -- 4. Hoàn tác điểm tích lũy của khách hàng
        IF @CustomerId IS NOT NULL AND LTRIM(RTRIM(@CustomerId)) <> ''
        BEGIN
            IF @DiscountAmount > 0
            BEGIN
                -- Khách đã dùng điểm để giảm giá -> Hoàn lại số điểm đã trừ
                UPDATE customer
                SET loyalty_points = COALESCE(loyalty_points, 0) + CAST(@DiscountAmount AS INT)
                WHERE customer_id = @CustomerId;
            END
            ELSE
            BEGIN
                -- Khách được tích lũy điểm từ đơn -> Thu hồi số điểm đã cộng
                DECLARE @EarnedPoints INT = dbo.fn_CalculateLoyaltyPointsEarned(@OriginalTotal);
                UPDATE customer
                SET loyalty_points = CASE 
                    WHEN COALESCE(loyalty_points, 0) >= @EarnedPoints THEN loyalty_points - @EarnedPoints 
                    ELSE 0 
                END
                WHERE customer_id = @CustomerId;
            END;
        END;

        -- 5. Cập nhật trạng thái hóa đơn sang CANCELLED
        UPDATE invoice
        SET status = 'CANCELLED'
        WHERE invoice_id = @InvoiceId;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        DECLARE @ErrorText NVARCHAR(4000) = ERROR_MESSAGE();
        DECLARE @Severity INT = ERROR_SEVERITY();
        DECLARE @State INT = ERROR_STATE();
        RAISERROR(@ErrorText, @Severity, @State);
    END CATCH
END;
GO

-- 6.2. STORED PROCEDURE: Tạo phiếu xuất hủy kho (sp_CreateDisposalReceiptItem)
-- Nghiệp vụ:
--   1. Kiểm tra sản phẩm tồn tại và số lượng xuất hủy không vượt tồn kho hiện tại.
--   2. Tự động tạo bản ghi voucher và disposal_receipt nếu là dòng đầu tiên của phiếu hủy.
--   3. Thêm bản ghi chi tiết vào disposal_receipt_detail.
--   4. Trừ tồn kho của sản phẩm.
--   Toàn bộ nằm trong 1 TRANSACTION, bảo đảm tính toàn vẹn dữ liệu khi ghi phiếu hủy.
CREATE OR ALTER PROCEDURE dbo.sp_CreateDisposalReceiptItem
    @VoucherId VARCHAR(36),
    @EmployeeId VARCHAR(36),
    @DetailId VARCHAR(36),
    @ProductId VARCHAR(36),
    @DisposedQuantity INT,
    @DisposalReason NVARCHAR(MAX)
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- 1. Kiểm tra sản phẩm và số lượng tồn kho
        DECLARE @ProductName NVARCHAR(100);
        DECLARE @StockAvailable INT;

        SELECT @ProductName = product_name, @StockAvailable = stock_quantity
        FROM product
        WHERE product_id = @ProductId;

        IF @ProductName IS NULL
        BEGIN
            THROW 50003, N'Sản phẩm không tồn tại trong hệ thống.', 1;
        END;

        IF @StockAvailable < @DisposedQuantity
        BEGIN
            DECLARE @StockError NVARCHAR(300) = N'Số lượng xuất hủy (' + CAST(@DisposedQuantity AS NVARCHAR(10)) 
                + N') lớn hơn số lượng tồn kho hiện tại (' + CAST(@StockAvailable AS NVARCHAR(10)) 
                + N') của sản phẩm: ' + @ProductName;
            THROW 50004, @StockError, 1;
        END;

        -- 2. Đảm bảo Voucher và DisposalReceipt tồn tại
        IF NOT EXISTS (SELECT 1 FROM voucher WHERE voucher_id = @VoucherId)
        BEGIN
            INSERT INTO voucher (voucher_id, employee_id, total_amount, created_at)
            VALUES (@VoucherId, @EmployeeId, 0, SYSUTCDATETIME());

            INSERT INTO disposal_receipt (voucher_id)
            VALUES (@VoucherId);
        END;

        -- 3. Ghi chi tiết phiếu hủy
        INSERT INTO disposal_receipt_detail (
            disposal_detail_id, 
            voucher_id, 
            product_id, 
            disposed_quantity, 
            disposal_reason
        )
        VALUES (
            @DetailId, 
            @VoucherId, 
            @ProductId, 
            @DisposedQuantity, 
            @DisposalReason
        );

        -- (Lưu ý: Nếu có trigger trg_DisposalDetail_CheckAndDeductStock thì việc trừ tồn kho được trigger xử lý;
        --  nếu chưa cài trigger thì câu lệnh bên dưới đảm bảo trừ kho).
        IF NOT EXISTS (SELECT 1 FROM sys.triggers WHERE name = 'trg_DisposalDetail_CheckAndDeductStock')
        BEGIN
            UPDATE product
            SET stock_quantity = stock_quantity - @DisposedQuantity
            WHERE product_id = @ProductId;
        END;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        DECLARE @Msg NVARCHAR(4000) = ERROR_MESSAGE();
        DECLARE @Sev INT = ERROR_SEVERITY();
        DECLARE @St INT = ERROR_STATE();
        RAISERROR(@Msg, @Sev, @St);
    END CATCH
END;
GO


-- ------------------------------------------------------------------------------------
-- 7. PHÂN QUYỀN TRUY CẬP CSDL (DATABASE ROLE-BASED ACCESS CONTROL - RBAC)
-- 7.1. PHÂN QUYỀN CHO CHỦ CỬA HÀNG (role_owner - Tương ứng STORE_OWNER trong Middleware)
-- Nghiệp vụ: Chủ cửa hàng có toàn quyền vận hành, giám sát và kiểm tra toàn bộ hệ thống siêu thị.
-- A. Toàn quyền trên các bảng dữ liệu của 3 phân hệ
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.invoice TO role_owner;
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.invoice_detail TO role_owner;
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.purchase_receipt TO role_owner;
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.purchase_receipt_detail TO role_owner;
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.disposal_receipt TO role_owner;
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.disposal_receipt_detail TO role_owner;
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.voucher TO role_owner;

-- B. Toàn quyền truy vấn trên các Khung nhìn (Views)
GRANT SELECT ON dbo.v_invoice_summary TO role_owner;
GRANT SELECT ON dbo.v_disposal_receipt_summary TO role_owner;
GRANT SELECT ON dbo.v_purchase_receipt_summary TO role_owner;

-- C. Toàn quyền thực thi các Hàm nghiệp vụ (Functions)
GRANT EXECUTE ON dbo.fn_CalculateLoyaltyPointsEarned TO role_owner;
GRANT EXECUTE ON dbo.fn_CalculateLoyaltyDiscount TO role_owner;
GRANT SELECT ON dbo.fn_GetInvoiceItemsDetail TO role_owner;

-- D. Toàn quyền thực thi các Thủ tục lưu trữ (Stored Procedures)
GRANT EXECUTE ON dbo.sp_CancelInvoice TO role_owner;
GRANT EXECUTE ON dbo.sp_CreateDisposalReceiptItem TO role_owner;
GO


-- 7.2. PHÂN QUYỀN CHO THU NGÂN (role_cashier - Tương ứng CASHIER trong Middleware)
-- Nghiệp vụ: Thu ngân chỉ được quyền thao tác với nghiệp vụ Bán hàng / Hóa đơn (Invoice),
--           tuyệt đối không được xem hay chỉnh sửa phiếu Nhập hàng hoặc Xuất hủy kho.
-- A. Quyền hạn đối với phân hệ Hóa đơn (Invoice)
GRANT SELECT, INSERT, UPDATE ON dbo.invoice TO role_cashier;
GRANT SELECT, INSERT, UPDATE ON dbo.invoice_detail TO role_cashier;
GRANT SELECT ON dbo.voucher TO role_cashier;

GRANT SELECT ON dbo.v_invoice_summary TO role_cashier;
GRANT SELECT ON dbo.fn_GetInvoiceItemsDetail TO role_cashier;
GRANT EXECUTE ON dbo.fn_CalculateLoyaltyPointsEarned TO role_cashier;
GRANT EXECUTE ON dbo.fn_CalculateLoyaltyDiscount TO role_cashier;
GRANT EXECUTE ON dbo.sp_CancelInvoice TO role_cashier;

-- B. Cấm can thiệp các đối tượng thuộc Kho (Purchase Receipt & Disposal Receipt)
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.purchase_receipt TO role_cashier;
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.purchase_receipt_detail TO role_cashier;
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.disposal_receipt TO role_cashier;
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.disposal_receipt_detail TO role_cashier;

DENY SELECT ON dbo.v_purchase_receipt_summary TO role_cashier;
DENY SELECT ON dbo.v_disposal_receipt_summary TO role_cashier;
DENY EXECUTE ON dbo.sp_CreateDisposalReceiptItem TO role_cashier;
GO


-- 7.3. PHÂN QUYỀN CHO QUẢN LÝ KHO (role_inventory - Tương ứng INVENTORY_MANAGER trong Middleware)
-- Nghiệp vụ: Nhân viên kho phụ trách lập phiếu Nhập hàng, lập phiếu Hủy hàng tồn hỏng/hết hạn,
--           tuyệt đối không được can thiệp vào Hóa đơn bán lẻ, tiền thu ngân hoặc điểm thưởng khách hàng.
-- A. Quyền hạn đối với phân hệ Nhập hàng và Hủy hàng kho
GRANT SELECT, INSERT, UPDATE ON dbo.purchase_receipt TO role_inventory;
GRANT SELECT, INSERT, UPDATE ON dbo.purchase_receipt_detail TO role_inventory;
GRANT SELECT, INSERT, UPDATE ON dbo.disposal_receipt TO role_inventory;
GRANT SELECT, INSERT, UPDATE ON dbo.disposal_receipt_detail TO role_inventory;
GRANT SELECT, INSERT, UPDATE ON dbo.voucher TO role_inventory;

GRANT SELECT ON dbo.v_purchase_receipt_summary TO role_inventory;
GRANT SELECT ON dbo.v_disposal_receipt_summary TO role_inventory;
GRANT EXECUTE ON dbo.sp_CreateDisposalReceiptItem TO role_inventory;

-- B. Cấm can thiệp phân hệ Hóa đơn bán hàng (Invoice)
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.invoice TO role_inventory;
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.invoice_detail TO role_inventory;

DENY SELECT ON dbo.v_invoice_summary TO role_inventory;
DENY SELECT ON dbo.fn_GetInvoiceItemsDetail TO role_inventory;
DENY EXECUTE ON dbo.fn_CalculateLoyaltyPointsEarned TO role_inventory;
DENY EXECUTE ON dbo.fn_CalculateLoyaltyDiscount TO role_inventory;
DENY EXECUTE ON dbo.sp_CancelInvoice TO role_inventory;
GO


-- 7.4. PHÂN QUYỀN CHO KHÁCH HÀNG (role_customer - Tương ứng Khách hàng / Public Client)
-- Nghiệp vụ: Khách hàng chỉ có quyền tra cứu tỷ lệ quy đổi điểm thưởng công khai,
--           tuyệt đối cấm can thiệp hoặc truy cập dữ liệu chứng từ nội bộ của siêu thị.
-- A. Quyền hạn cho phép (Tra cứu tỷ lệ tích lũy điểm thưởng)
GRANT EXECUTE ON dbo.fn_CalculateLoyaltyPointsEarned TO role_customer;

-- B. Cấm toàn bộ truy cập vào các Khung nhìn, Hàm nội bộ, Thủ tục và Bảng dữ liệu 3 phân hệ
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.invoice TO role_customer;
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.invoice_detail TO role_customer;
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.purchase_receipt TO role_customer;
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.purchase_receipt_detail TO role_customer;
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.disposal_receipt TO role_customer;
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.disposal_receipt_detail TO role_customer;
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.voucher TO role_customer;

DENY SELECT ON dbo.v_invoice_summary TO role_customer;
DENY SELECT ON dbo.v_purchase_receipt_summary TO role_customer;
DENY SELECT ON dbo.v_disposal_receipt_summary TO role_customer;

DENY SELECT ON dbo.fn_GetInvoiceItemsDetail TO role_customer;
DENY EXECUTE ON dbo.fn_CalculateLoyaltyDiscount TO role_customer;

DENY EXECUTE ON dbo.sp_CancelInvoice TO role_customer;
DENY EXECUTE ON dbo.sp_CreateDisposalReceiptItem TO role_customer;
GO

