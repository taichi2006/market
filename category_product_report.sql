USE taichi_sql;
GO

-- 1.1. Ràng buộc CHECK: Kiểm tra tính hợp lệ về Giá bán lẻ và Số lượng tồn kho của Sản phẩm
-- Ý nghĩa nghiệp vụ: Đơn giá sản phẩm phải lớn hơn 0 và số lượng tồn kho không được âm.
ALTER TABLE dbo.product
ADD CONSTRAINT ck_product_stock_and_price 
CHECK (unit_price > 0 AND stock_quantity >= 0);
GO

-- 1.2. Ràng buộc CHECK: Kiểm tra số lượng tồn kho hệ thống và thực tế trong Biên bản kiểm kê
-- Ý nghĩa nghiệp vụ: Số lượng kiểm kê (cả hệ thống và thực tế đếm được) phải là số không âm.
ALTER TABLE dbo.inventory_report_detail
ADD CONSTRAINT ck_report_detail_quantities 
CHECK (system_quantity >= 0 AND actual_quantity >= 0);
GO

-- 1.3. Ràng buộc UNIQUE: Tên loại sản phẩm (Category Name) không được phép trùng lặp
-- Ý nghĩa nghiệp vụ: Tránh việc thủ kho hoặc quản trị viên tạo nhiều danh mục trùng tên gây nhầm lẫn khi phân loại hàng hóa.
ALTER TABLE dbo.product_category
ADD CONSTRAINT uq_category_name UNIQUE (category_name);
GO

-- 1.4. Ràng buộc DEFAULT: Trạng thái sản phẩm mặc định là 'ON_SALE'

ALTER TABLE dbo.product
ADD CONSTRAINT df_product_status_default DEFAULT 'ON_SALE' FOR product_status;
GO

-- 2.1. Trigger 1: trg_Product_StockStatusSync
-- Ý nghĩa nghiệp vụ: Tự động đồng bộ trạng thái sản phẩm (product_status) theo số lượng tồn kho:
--   - Khi stock_quantity giảm về 0: Tự động đổi trạng thái sang 'OUT_OF_STOCK'.
--   - Khi stock_quantity được nhập thêm > 0 mà đang ở trạng thái 'OUT_OF_STOCK': Tự động mở bán lại 'ON_SALE'.
--   - Kiểm tra chặn lỗi: Ngăn chặn tuyệt đối mọi hành vi cập nhật làm số lượng tồn kho bị âm (Rollback giao dịch).
CREATE TRIGGER dbo.trg_Product_StockStatusSync
ON dbo.product
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    -- Kiểm tra nếu có bất kỳ sản phẩm nào có số lượng tồn kho âm
    IF EXISTS (SELECT 1 FROM inserted WHERE stock_quantity < 0)
    BEGIN
        RAISERROR (N'Lỗi nghiệp vụ: Số lượng tồn kho không được phép nhỏ hơn 0!', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END

    -- Tự động chuyển thành 'OUT_OF_STOCK' nếu tồn kho = 0 và đang ở trạng thái 'ON_SALE'
    UPDATE p
    SET p.product_status = 'OUT_OF_STOCK'
    FROM dbo.product p
    INNER JOIN inserted i ON p.product_id = i.product_id
    WHERE i.stock_quantity = 0 
      AND (p.product_status = 'ON_SALE' OR p.product_status IS NULL);

    -- Tự động mở bán lại 'ON_SALE' nếu tồn kho > 0 và đang bị đánh dấu 'OUT_OF_STOCK'
    UPDATE p
    SET p.product_status = 'ON_SALE'
    FROM dbo.product p
    INNER JOIN inserted i ON p.product_id = i.product_id
    WHERE i.stock_quantity > 0 
      AND p.product_status = 'OUT_OF_STOCK';
END;
GO

-- 2.2. Trigger 2: trg_PreventDeleteCategoryWithProducts
-- Ý nghĩa nghiệp vụ: Ngăn chặn xóa ngành hàng (category) nếu trong ngành hàng đó vẫn còn sản phẩm đang kinh doanh hoặc còn tồn kho.
-- Giúp bảo đảm toàn vẹn dữ liệu nghiệp vụ và thông báo lỗi rõ ràng thay vì lỗi FK thô của hệ thống.

CREATE TRIGGER dbo.trg_PreventDeleteCategoryWithProducts
ON dbo.product_category
INSTEAD OF DELETE
AS
BEGIN
    SET NOCOUNT ON;

    -- Kiểm tra xem danh mục bị xóa có sản phẩm nào không
    IF EXISTS (
        SELECT 1 
        FROM dbo.product p
        INNER JOIN deleted d ON p.category_id = d.category_id
        WHERE p.stock_quantity > 0 OR p.product_status = 'ON_SALE'
    )
    BEGIN
        RAISERROR (N'Không thể xóa danh mục này vì vẫn còn sản phẩm đang kinh doanh hoặc còn tồn kho!', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END

    -- Nếu các sản phẩm đã ngừng kinh doanh và hết tồn kho, cho phép gỡ liên kết category_id và xóa
    UPDATE dbo.product
    SET category_id = NULL
    WHERE category_id IN (SELECT category_id FROM deleted);

    DELETE FROM dbo.product_category
    WHERE category_id IN (SELECT category_id FROM deleted);
END;
GO

-- 3.1. View 1: vw_ProductCategoryInventorySummary
-- Ý nghĩa nghiệp vụ: Báo cáo tổng quan tình hình kho theo từng danh mục hàng hóa:
--   - Tổng số mặt hàng, tổng số lượng tồn kho.
--   - Tổng giá trị vốn hóa tồn kho của danh mục (stock_quantity * unit_price).
--   - Số mặt hàng sắp hết hàng (tồn <= 10) và số mặt hàng cận hạn sử dụng (hết hạn trong vòng 30 ngày tới).

CREATE VIEW dbo.vw_ProductCategoryInventorySummary
AS
SELECT 
    c.category_id,
    c.category_name,
    COUNT(p.product_id) AS total_products,
    ISNULL(SUM(p.stock_quantity), 0) AS total_stock_quantity,
    ISNULL(SUM(CAST(p.stock_quantity AS DECIMAL(18,2)) * p.unit_price), 0) AS total_inventory_value,
    SUM(CASE WHEN p.stock_quantity <= 10 AND p.product_status = 'ON_SALE' THEN 1 ELSE 0 END) AS low_stock_product_count,
    SUM(CASE WHEN p.expiration_date <= DATEADD(DAY, 30, SYSUTCDATETIME()) THEN 1 ELSE 0 END) AS expiring_soon_product_count
FROM dbo.product_category c
LEFT JOIN dbo.product p ON c.category_id = p.category_id
GROUP BY c.category_id, c.category_name;
GO

-- 3.2. View 2: vw_InventoryAuditDiscrepancy
-- Ý nghĩa nghiệp vụ: Báo cáo đối soát chi tiết sai lệch kiểm kê kho:
--   - So sánh giữa tồn kho thực tế đếm được và tồn kho trên hệ thống.
--   - Phân loại trạng thái: 'THỪA' (SURPLUS), 'THIẾU' (SHORTAGE), 'KHỚP' (MATCH).
--   - Tính giá trị tiền chênh lệch tương ứng để kế toán/thủ kho xử lý bồi hoàn hoặc xuất hủy.

CREATE VIEW dbo.vw_InventoryAuditDiscrepancy
AS
SELECT 
    ir.report_id,
    ir.report_date,
    e.employee_id,
    e.full_name AS auditor_name,
    p.product_id,
    p.product_name,
    c.category_name,
    p.unit_price,
    ird.system_quantity,
    ird.actual_quantity,
    (ird.actual_quantity - ird.system_quantity) AS discrepancy_quantity,
    CASE 
        WHEN ird.actual_quantity > ird.system_quantity THEN N'THỪA HÀNG'
        WHEN ird.actual_quantity < ird.system_quantity THEN N'THIẾU HÀNG'
        ELSE N'KHỚP TỒN KHO'
    END AS discrepancy_status,
    CAST((ird.actual_quantity - ird.system_quantity) * p.unit_price AS DECIMAL(18,2)) AS discrepancy_amount
FROM dbo.inventory_report ir
INNER JOIN dbo.inventory_report_detail ird ON ir.report_id = ird.report_id
INNER JOIN dbo.product p ON ird.product_id = p.product_id
LEFT JOIN dbo.product_category c ON p.category_id = c.category_id
LEFT JOIN dbo.employee e ON ir.employee_id = e.employee_id;
GO

-- 4.1. Index 1: Chỉ mục Non-Clustered bao phủ (Covering Index) trên bảng product
-- Mục đích: Tối ưu hóa truy vấn tìm kiếm sản phẩm theo danh mục và trạng thái bán hàng:
--   WHERE category_id = @catId AND product_status = 'ON_SALE'
-- Sử dụng INCLUDE để chứa các cột hiển thị (product_name, unit_price, stock_quantity)
-- giúp SQL Server thực hiện Index Seek trực tiếp mà KHÔNG cần tốn chi phí Key Lookup vào Clustered Index.

CREATE NONCLUSTERED INDEX idx_product_category_status
ON dbo.product (category_id, product_status)
INCLUDE (product_name, unit_price, stock_quantity, expiration_date);
GO

-- 4.2. Index 2: Chỉ mục Non-Clustered trên bảng inventory_report theo ngày kiểm kê
-- Mục đích: Tối ưu hóa truy vấn lọc báo cáo kiểm kê theo khoảng thời gian và sắp xếp giảm dần (ORDER BY report_date DESC).

CREATE NONCLUSTERED INDEX idx_inventory_report_date
ON dbo.inventory_report (report_date DESC)
INCLUDE (employee_id);
GO

-- 5.1. UDF 1 (Scalar Function): fn_EvaluateProductStockRisk
-- Ý nghĩa nghiệp vụ: Đánh giá mức độ rủi ro kho của một mặt hàng cụ thể dựa trên số lượng tồn kho và số ngày còn hạn sử dụng.
-- Trả về nhãn trạng thái cảnh báo: 'HẾT HÀNG', 'CẬN HẠN NGUY CƠ CAO', 'TỒN KHO THẤP', 'AN TOÀN'.

CREATE FUNCTION dbo.fn_EvaluateProductStockRisk (@ProductId VARCHAR(36))
RETURNS NVARCHAR(50)
AS
BEGIN
    DECLARE @Stock INT;
    DECLARE @ExpDate DATETIME2;
    DECLARE @DaysRemaining INT;
    DECLARE @RiskLevel NVARCHAR(50);

    SELECT 
        @Stock = stock_quantity,
        @ExpDate = expiration_date
    FROM dbo.product
    WHERE product_id = @ProductId;

    IF @Stock IS NULL
        RETURN N'KHÔNG TÌM THẤY SẢN PHẨM';

    SET @DaysRemaining = DATEDIFF(DAY, SYSUTCDATETIME(), @ExpDate);

    IF @Stock = 0
        SET @RiskLevel = N'HẾT HÀNG TRONG KHO';
    ELSE IF @DaysRemaining <= 0
        SET @RiskLevel = N'ĐÃ HẾT HẠN SỬ DỤNG - CẦN HỦY';
    ELSE IF @DaysRemaining <= 15
        SET @RiskLevel = N'CẬN HẠN KHẨN CẤP (<= 15 NGÀY)';
    ELSE IF @Stock <= 10
        SET @RiskLevel = N'TỒN KHO THẤP CẦN NHẬP BỔ SUNG';
    ELSE
        SET @RiskLevel = N'AN TOÀN BÌNH THƯỜNG';

    RETURN @RiskLevel;
END;
GO

-- 5.2. UDF 2 (Table-Valued Function): fn_GetExpiringProductsByDays
-- Ý nghĩa nghiệp vụ: Trả về bảng danh sách các sản phẩm sắp hết hạn trong vòng @DaysThreshold ngày tới,
-- kèm theo tính toán giá trị rủi ro tiềm ẩn (potential_loss_value) để siêu thị lên kế hoạch xả hàng giảm giá.

CREATE FUNCTION dbo.fn_GetExpiringProductsByDays (@DaysThreshold INT)
RETURNS TABLE
AS
RETURN
(
    SELECT 
        p.product_id,
        p.product_name,
        c.category_name,
        p.stock_quantity,
        p.unit_price,
        p.expiration_date,
        DATEDIFF(DAY, SYSUTCDATETIME(), p.expiration_date) AS days_until_expiration,
        CAST(p.stock_quantity * p.unit_price AS DECIMAL(18,2)) AS potential_loss_value,
        dbo.fn_EvaluateProductStockRisk(p.product_id) AS risk_status
    FROM dbo.product p
    LEFT JOIN dbo.product_category c ON p.category_id = c.category_id
    WHERE p.stock_quantity > 0
      AND DATEDIFF(DAY, SYSUTCDATETIME(), p.expiration_date) <= @DaysThreshold
);
GO

-- 6.1. Stored Procedure 1: sp_AdjustProductStockWithAudit
-- Ý nghĩa nghiệp vụ: Điều chỉnh số lượng tồn kho (tăng/giảm do bán lẻ, nhập bù, hoặc hao hụt) 
-- Có kiểm tra tính hợp lệ và bẫy lỗi TRY...CATCH toàn diện.

CREATE PROCEDURE dbo.sp_AdjustProductStockWithAudit
    @ProductId VARCHAR(36),
    @QuantityChange INT,           -- Số nguyên: Dương (cộng thêm), Âm (trừ đi)
    @Reason NVARCHAR(255),
    @UpdatedStock INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        -- 1. Kiểm tra tham số đầu vào
        IF @ProductId IS NULL OR TRIM(@ProductId) = ''
        BEGIN
            RAISERROR(N'Mã sản phẩm (ProductId) không được để trống!', 16, 1);
        END

        IF @QuantityChange = 0
        BEGIN
            RAISERROR(N'Số lượng điều chỉnh (QuantityChange) phải khác 0!', 16, 1);
        END

        -- 2. Kiểm tra sự tồn tại của sản phẩm
        DECLARE @CurrentStock INT;
        DECLARE @ProductName NVARCHAR(100);

        SELECT 
            @CurrentStock = stock_quantity,
            @ProductName = product_name
        FROM dbo.product WITH (UPDLOCK, ROWLOCK)
        WHERE product_id = @ProductId;

        IF @CurrentStock IS NULL
        BEGIN
            RAISERROR(N'Không tìm thấy sản phẩm có mã: %s trong hệ thống!', 16, 1, @ProductId);
        END

        -- 3. Kiểm tra nếu trừ kho dẫn đến âm
        IF (@CurrentStock + @QuantityChange) < 0
        BEGIN
            DECLARE @ErrMsg NVARCHAR(255) = N'Số lượng tồn kho không đủ để xuất! Hiện còn: ' 
                + CAST(@CurrentStock AS NVARCHAR(10)) 
                + N', Yêu cầu trừ: ' + CAST(ABS(@QuantityChange) AS NVARCHAR(10));
            RAISERROR(@ErrMsg, 16, 1);
        END

        -- 4. Bắt đầu cập nhật tồn kho
        BEGIN TRANSACTION;

        UPDATE dbo.product
        SET stock_quantity = stock_quantity + @QuantityChange
        WHERE product_id = @ProductId;

        -- Lấy giá trị sau khi cập nhật để trả về output
        SELECT @UpdatedStock = stock_quantity 
        FROM dbo.product 
        WHERE product_id = @ProductId;

        COMMIT TRANSACTION;

        PRINT N'Thành công: Đã cập nhật tồn kho sản phẩm [' + @ProductName + N']. Tồn kho mới: ' + CAST(@UpdatedStock AS NVARCHAR(10));
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        DECLARE @ErrorNumber INT = ERROR_NUMBER();
        DECLARE @ErrorMessage NVARCHAR(4000) = ERROR_MESSAGE();
        DECLARE @ErrorSeverity INT = ERROR_SEVERITY();
        DECLARE @ErrorState INT = ERROR_STATE();

        RAISERROR(N'Lỗi tại thủ tục sp_AdjustProductStockWithAudit (Mã lỗi: %d): %s', 
                  @ErrorSeverity, @ErrorState, @ErrorNumber, @ErrorMessage);
    END CATCH
END;
GO

-- 6.2. Stored Procedure 2: sp_CreateInventoryAuditSession
-- Ý nghĩa nghiệp vụ: Khởi tạo một phiên kiểm kê kho mới cho toàn bộ một danh mục hàng hóa:
--   - Tạo bản ghi trong `inventory_report`.
--   - Tự động nạp toàn bộ sản phẩm thuộc danh mục vào `inventory_report_detail` với số lượng hệ thống (system_quantity).
--   - Có bẫy lỗi TRY...CATCH đảm bảo nếu nạp chi tiết thất bại thì rollback toàn bộ phiên kiểm kê.

CREATE PROCEDURE dbo.sp_CreateInventoryAuditSession
    @ReportId VARCHAR(36),
    @EmployeeId VARCHAR(36),
    @CategoryId VARCHAR(36),
    @TotalItemsCreated INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        -- Kiểm tra nhân viên có tồn tại không
        IF NOT EXISTS (SELECT 1 FROM dbo.employee WHERE employee_id = @EmployeeId)
        BEGIN
            RAISERROR(N'Nhân viên kiểm kê (EmployeeId: %s) không tồn tại trong hệ thống!', 16, 1, @EmployeeId);
        END

        -- Kiểm tra danh mục hàng hóa
        IF NOT EXISTS (SELECT 1 FROM dbo.product_category WHERE category_id = @CategoryId)
        BEGIN
            RAISERROR(N'Danh mục hàng hóa (CategoryId: %s) không tồn tại!', 16, 1, @CategoryId);
        END

        -- Kiểm tra mã báo cáo đã tồn tại chưa
        IF EXISTS (SELECT 1 FROM dbo.inventory_report WHERE report_id = @ReportId)
        BEGIN
            RAISERROR(N'Mã báo cáo kiểm kê (ReportId: %s) đã tồn tại!', 16, 1, @ReportId);
        END

        BEGIN TRANSACTION;

        -- 1. Tạo Header báo cáo kiểm kê
        INSERT INTO dbo.inventory_report (report_id, employee_id, report_date)
        VALUES (@ReportId, @EmployeeId, SYSUTCDATETIME());

        -- 2. Tự động snapshot số lượng tồn kho hiện tại vào chi tiết kiểm kê
        INSERT INTO dbo.inventory_report_detail (report_id, product_id, system_quantity, actual_quantity)
        SELECT 
            @ReportId,
            p.product_id,
            p.stock_quantity,
            p.stock_quantity -- Mặc định khởi tạo bằng tồn hệ thống trước khi thủ kho đếm thực tế
        FROM dbo.product p
        WHERE p.category_id = @CategoryId;

        SET @TotalItemsCreated = @@ROWCOUNT;

        IF @TotalItemsCreated = 0
        BEGIN
            RAISERROR(N'Danh mục được chọn hiện không có bất kỳ sản phẩm nào để kiểm kê!', 16, 1);
        END

        COMMIT TRANSACTION;

        PRINT N'Khởi tạo đợt kiểm kê thành công! Tổng số mặt hàng đưa vào đối soát: ' + CAST(@TotalItemsCreated AS NVARCHAR(10));
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        DECLARE @ErrMsgProc NVARCHAR(4000) = ERROR_MESSAGE();
        RAISERROR(N'Lỗi tạo phiên kiểm kê: %s', 16, 1, @ErrMsgProc);
    END CATCH
END;
GO

-- 7.1. Nghiệp vụ Transaction 1: Chốt đợt kiểm kê kho & Tự động xuất hủy hàng hao hụt
-- Ý nghĩa: Sau khi thủ kho đếm hàng thực tế:
--   1. Cập nhật tồn kho sản phẩm theo số lượng thực tế (actual_quantity).
--   2. Với các mặt hàng bị THIẾU (actual < system): Tự động tạo Phiếu xuất hủy kho (voucher + disposal_receipt + disposal_receipt_detail).
--   3. Đảm bảo tính toàn vẹn tuyệt đối qua Transaction: Nếu xuất hủy lỗi -> Rollback, không để kho bị lệch số liệu.

CREATE PROCEDURE dbo.sp_FinalizeAuditAndAutoDispose
    @ReportId VARCHAR(36),
    @DisposalVoucherId VARCHAR(36),
    @EmployeeId VARCHAR(36)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        -- Kiểm tra báo cáo kiểm kê
        IF NOT EXISTS (SELECT 1 FROM dbo.inventory_report WHERE report_id = @ReportId)
        BEGIN
            RAISERROR(N'Không tìm thấy báo cáo kiểm kê mã: %s', 16, 1, @ReportId);
        END

        BEGIN TRANSACTION;

        -- 1. Cập nhật lại số lượng tồn kho của các sản phẩm theo số lượng thực tế kiểm kê
        UPDATE p
        SET p.stock_quantity = ird.actual_quantity
        FROM dbo.product p
        INNER JOIN dbo.inventory_report_detail ird ON p.product_id = ird.product_id
        WHERE ird.report_id = @ReportId;

        -- 2. Kiểm tra xem có sản phẩm nào bị thiếu hụt không (actual < system)
        DECLARE @TotalShortageItems INT;
        DECLARE @TotalShortageAmount DECIMAL(18,2);

        SELECT 
            @TotalShortageItems = COUNT(*),
            @TotalShortageAmount = ISNULL(SUM(CAST((ird.system_quantity - ird.actual_quantity) AS DECIMAL(18,2)) * p.unit_price), 0)
        FROM dbo.inventory_report_detail ird
        INNER JOIN dbo.product p ON ird.product_id = p.product_id
        WHERE ird.report_id = @ReportId 
          AND ird.actual_quantity < ird.system_quantity;

        -- Nếu có thất thoát, tự động ghi nhận phiếu xuất hủy kho
        IF @TotalShortageItems > 0
        BEGIN
            -- Tạo Voucher
            INSERT INTO dbo.voucher (voucher_id, employee_id, total_amount, created_at)
            VALUES (@DisposalVoucherId, @EmployeeId, @TotalShortageAmount, SYSUTCDATETIME());

            -- Tạo Phiếu xuất hủy
            INSERT INTO dbo.disposal_receipt (voucher_id)
            VALUES (@DisposalVoucherId);

            -- Tạo Chi tiết phiếu xuất hủy
            INSERT INTO dbo.disposal_receipt_detail (disposal_detail_id, voucher_id, product_id, disposed_quantity, disposal_reason)
            SELECT 
                CONVERT(VARCHAR(36), NEWID()),
                @DisposalVoucherId,
                ird.product_id,
                (ird.system_quantity - ird.actual_quantity),
                N'Hao hụt thất thoát theo biên bản kiểm kê [' + @ReportId + N']'
            FROM dbo.inventory_report_detail ird
            WHERE ird.report_id = @ReportId 
              AND ird.actual_quantity < ird.system_quantity;

            PRINT N'Đã tự động tạo phiếu xuất hủy [' + @DisposalVoucherId + N'] cho ' + CAST(@TotalShortageItems AS NVARCHAR(10)) + N' mặt hàng bị hao hụt.';
        END

        COMMIT TRANSACTION;
        PRINT N'Chốt đợt kiểm kê thành công và đã đồng bộ tồn kho an toàn!';
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        DECLARE @ErrorMsg NVARCHAR(4000) = ERROR_MESSAGE();
        RAISERROR(N'Lỗi trong quá trình chốt kiểm kê: %s', 16, 1, @ErrorMsg);
    END CATCH
END;
GO

-- 7.2. Nghiệp vụ Transaction 2: Gộp danh mục hàng hóa (Merge Categories) an toàn
-- Ý nghĩa: Chuyển toàn bộ sản phẩm từ danh mục nguồn sang danh mục đích và xóa danh mục nguồn
-- Nếu có bất kỳ lỗi nào trong quá trình chuyển hoặc xóa, Transaction sẽ ROLLBACK lại trạng thái ban đầu.

CREATE PROCEDURE dbo.sp_MergeProductCategories
    @SourceCategoryId VARCHAR(36),
    @TargetCategoryId VARCHAR(36)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        IF @SourceCategoryId = @TargetCategoryId
        BEGIN
            RAISERROR(N'Danh mục nguồn và danh mục đích không được trùng nhau!', 16, 1);
        END

        IF NOT EXISTS (SELECT 1 FROM dbo.product_category WHERE category_id = @SourceCategoryId)
        BEGIN
            RAISERROR(N'Danh mục nguồn không tồn tại!', 16, 1);
        END

        IF NOT EXISTS (SELECT 1 FROM dbo.product_category WHERE category_id = @TargetCategoryId)
        BEGIN
            RAISERROR(N'Danh mục đích không tồn tại!', 16, 1);
        END

        BEGIN TRANSACTION;

        -- 1. Chuyển toàn bộ sản phẩm từ category nguồn sang category đích
        UPDATE dbo.product
        SET category_id = @TargetCategoryId
        WHERE category_id = @SourceCategoryId;

        -- 2. Xóa category nguồn
        DELETE FROM dbo.product_category
        WHERE category_id = @SourceCategoryId;

        COMMIT TRANSACTION;
        PRINT N'Thao tác gộp danh mục hoàn tất thành công!';
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        DECLARE @Err NVARCHAR(4000) = ERROR_MESSAGE();
        RAISERROR(N'Lỗi khi gộp danh mục: %s', 16, 1, @Err);
    END CATCH
END;
GO


/*
TÌNH HUỐNG TRANH CHẤP NGHIỆP VỤ (LOST UPDATE / CONCURRENCY CONFLICT):
- Tình huống:
    + Luồng A (Thu ngân): Đang bán 2 sản phẩm SP001 (Đọc tồn = 5, chuẩn bị ghi tồn = 3).
    + Luồng B (Thủ kho kiểm kê hoặc nhập bù): Cùng lúc đọc tồn = 5 và cập nhật thành 10.
    + Nếu không có kiểm soát đồng thời, giao dịch nào commit sau sẽ ghi đè và làm mất dữ liệu của giao dịch trước (Lost Update).

GIẢI PHÁP 1: PESSIMISTIC LOCKING (KHÓA BI QUAN VỚI GỢI Ý KHÓA TRONG SQL SERVER)
Sử dụng khóa hàng độc quyền: WITH (UPDLOCK, ROWLOCK, HOLDLOCK) khi đọc dữ liệu trước khi cập nhật.
*/


CREATE PROCEDURE dbo.sp_SafeDeductProductStock
    @ProductId VARCHAR(36),
    @DeductQuantity INT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    -- Thiết lập mức cô lập giao dịch cao hoặc dùng khóa độc quyền
    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @CurrentStock INT;

        -- Sử dụng UPDLOCK + ROWLOCK + HOLDLOCK:
        -- Khóa ngay dòng sản phẩm cần trừ tồn kho cho tới khi kết thúc giao dịch
        -- Ngăn chặn bất kỳ transaction nào khác đọc-để-sửa hoặc ghi đè đồng thời trên dòng này!
        SELECT @CurrentStock = stock_quantity
        FROM dbo.product WITH (UPDLOCK, ROWLOCK, HOLDLOCK)
        WHERE product_id = @ProductId;

        IF @CurrentStock IS NULL
        BEGIN
            RAISERROR(N'Sản phẩm không tồn tại!', 16, 1);
        END

        IF @CurrentStock < @DeductQuantity
        BEGIN
            RAISERROR(N'Tồn kho không đủ để xuất bán!', 16, 1);
        END

        -- Giả lập độ trễ xử lý (để kiểm tra tranh chấp đồng thời nếu cần)
        -- WAITFOR DELAY '00:00:02';

        UPDATE dbo.product
        SET stock_quantity = stock_quantity - @DeductQuantity
        WHERE product_id = @ProductId;

        COMMIT TRANSACTION;
        PRINT N'Đã trừ tồn kho an toàn!';
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        DECLARE @ConErr NVARCHAR(4000) = ERROR_MESSAGE();
        RAISERROR(@ConErr, 16, 1);
    END CATCH
END;
GO

-- 8.4. PHÂN QUYỀN CHI TIẾT (GRANT / DENY) CHO PHÂN HỆ SẢN PHẨM - DANH MỤC - KIỂM KÊ KHO
-- 8.4.1. CHỦ CỬA HÀNG / QUẢN LÝ (role_owner): Toàn quyền quản trị trên phân hệ
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.product_category TO role_owner;
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.product TO role_owner;
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.inventory_report TO role_owner;
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.inventory_report_detail TO role_owner;
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.voucher TO role_owner;
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.disposal_receipt TO role_owner;
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.disposal_receipt_detail TO role_owner;

GRANT SELECT ON dbo.vw_ProductCategoryInventorySummary TO role_owner;
GRANT SELECT ON dbo.vw_InventoryAuditDiscrepancy TO role_owner;

GRANT EXECUTE ON dbo.fn_EvaluateProductStockRisk TO role_owner;
GRANT SELECT ON dbo.fn_GetExpiringProductsByDays TO role_owner;

GRANT EXECUTE ON dbo.sp_AdjustProductStockWithAudit TO role_owner;
GRANT EXECUTE ON dbo.sp_CreateInventoryAuditSession TO role_owner;
GRANT EXECUTE ON dbo.sp_FinalizeAuditAndAutoDispose TO role_owner;
GRANT EXECUTE ON dbo.sp_MergeProductCategories TO role_owner;
GRANT EXECUTE ON dbo.sp_SafeDeductProductStock TO role_owner;
GO

-- 8.4.2. THỦ KHO (role_inventory): Quản lý tồn kho, kiểm kê kho, đối soát lệch và xuất hủy
GRANT SELECT ON dbo.product_category TO role_inventory;
GRANT SELECT, INSERT, UPDATE ON dbo.product TO role_inventory;
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.inventory_report TO role_inventory;
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.inventory_report_detail TO role_inventory;
GRANT SELECT, INSERT, UPDATE ON dbo.voucher TO role_inventory;
GRANT SELECT, INSERT, UPDATE ON dbo.disposal_receipt TO role_inventory;
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.disposal_receipt_detail TO role_inventory;

GRANT SELECT ON dbo.vw_ProductCategoryInventorySummary TO role_inventory;
GRANT SELECT ON dbo.vw_InventoryAuditDiscrepancy TO role_inventory;

GRANT EXECUTE ON dbo.fn_EvaluateProductStockRisk TO role_inventory;
GRANT SELECT ON dbo.fn_GetExpiringProductsByDays TO role_inventory;

GRANT EXECUTE ON dbo.sp_AdjustProductStockWithAudit TO role_inventory;
GRANT EXECUTE ON dbo.sp_CreateInventoryAuditSession TO role_inventory;
GRANT EXECUTE ON dbo.sp_FinalizeAuditAndAutoDispose TO role_inventory;

-- Ràng buộc bảo mật (DENY): Chặn thủ kho tự ý xóa sản phẩm, can thiệp cây danh mục hoặc gộp danh mục
DENY DELETE ON dbo.product TO role_inventory;
DENY INSERT, UPDATE, DELETE ON dbo.product_category TO role_inventory;
DENY EXECUTE ON dbo.sp_MergeProductCategories TO role_inventory;
GO

-- 8.4.3. THU NGÂN (role_cashier): Bán lẻ POS, tra cứu thông tin sản phẩm và trừ kho an toàn
GRANT SELECT ON dbo.product_category TO role_cashier;
GRANT SELECT ON dbo.product TO role_cashier;
GRANT EXECUTE ON dbo.sp_SafeDeductProductStock TO role_cashier;
GRANT EXECUTE ON dbo.fn_EvaluateProductStockRisk TO role_cashier;

-- Ràng buộc bảo mật (DENY): Thu ngân không được sửa giá/tồn, không can thiệp kho/chứng từ, không xem giá vốn tồn kho
DENY INSERT, UPDATE, DELETE ON dbo.product TO role_cashier;
DENY INSERT, UPDATE, DELETE ON dbo.product_category TO role_cashier;
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.inventory_report TO role_cashier;
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.inventory_report_detail TO role_cashier;
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.voucher TO role_cashier;
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.disposal_receipt TO role_cashier;
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.disposal_receipt_detail TO role_cashier;

DENY SELECT ON dbo.vw_ProductCategoryInventorySummary TO role_cashier;
DENY SELECT ON dbo.vw_InventoryAuditDiscrepancy TO role_cashier;

DENY EXECUTE ON dbo.sp_AdjustProductStockWithAudit TO role_cashier;
DENY EXECUTE ON dbo.sp_CreateInventoryAuditSession TO role_cashier;
DENY EXECUTE ON dbo.sp_FinalizeAuditAndAutoDispose TO role_cashier;
DENY EXECUTE ON dbo.sp_MergeProductCategories TO role_cashier;
GO

-- 8.4.4. KHÁCH HÀNG (role_customer): Tra cứu danh mục và thông tin sản phẩm kinh doanh
GRANT SELECT ON dbo.product_category TO role_customer;
GRANT SELECT (product_id, category_id, product_name, unit_price, product_status) ON dbo.product TO role_customer;
GRANT SELECT ON dbo.fn_GetExpiringProductsByDays TO role_customer;

-- Ràng buộc bảo mật (DENY): Khách hàng bị chặn toàn bộ quyền ghi/sửa, không xem kho và các thủ tục xử lý nội bộ
DENY INSERT, UPDATE, DELETE ON dbo.product TO role_customer;
DENY INSERT, UPDATE, DELETE ON dbo.product_category TO role_customer;
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.inventory_report TO role_customer;
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.inventory_report_detail TO role_customer;
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.voucher TO role_customer;
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.disposal_receipt TO role_customer;
DENY SELECT, INSERT, UPDATE, DELETE ON dbo.disposal_receipt_detail TO role_customer;

DENY SELECT ON dbo.vw_ProductCategoryInventorySummary TO role_customer;
DENY SELECT ON dbo.vw_InventoryAuditDiscrepancy TO role_customer;

DENY EXECUTE ON dbo.sp_AdjustProductStockWithAudit TO role_customer;
DENY EXECUTE ON dbo.sp_CreateInventoryAuditSession TO role_customer;
DENY EXECUTE ON dbo.sp_FinalizeAuditAndAutoDispose TO role_customer;
DENY EXECUTE ON dbo.sp_MergeProductCategories TO role_customer;
DENY EXECUTE ON dbo.sp_SafeDeductProductStock TO role_customer;
GO

