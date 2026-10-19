
-- 2.1. CUSTOMER
CREATE TABLE customer (
    customer_id VARCHAR(36) NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
    phone_number VARCHAR(15) NULL,
    email VARCHAR(150) NULL,
    loyalty_points INT DEFAULT 0,
    created_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT pk_customer PRIMARY KEY (customer_id),
    CONSTRAINT uq_customer_phone UNIQUE (phone_number)
);

-- 2.2. EMPLOYEE
CREATE TABLE employee (
    employee_id VARCHAR(36) NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    position VARCHAR(50) NOT NULL DEFAULT 'CASHIER',
    status BIT DEFAULT 1,
    refresh_token NVARCHAR(MAX) NULL,
    CONSTRAINT pk_employee PRIMARY KEY (employee_id),
    CONSTRAINT uq_employee_username UNIQUE (username),
    CONSTRAINT uq_employee_phone UNIQUE (phone_number)
);

-- 2.3. STORE_FEEDBACK
CREATE TABLE store_feedback (
    feedback_id VARCHAR(36) NOT NULL,
    customer_name NVARCHAR(100) NULL,
    phone_number VARCHAR(15) NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment NVARCHAR(MAX) NULL,
    created_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT pk_store_feedback PRIMARY KEY (feedback_id)
);

-- 2.4. REVIEW (Optional/Legacy)
CREATE TABLE review (
    review_id VARCHAR(36) NOT NULL,
    customer_id VARCHAR(36) NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    content NVARCHAR(MAX) NULL,
    review_date DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT pk_review PRIMARY KEY (review_id),
    CONSTRAINT fk_review_customer FOREIGN KEY (customer_id) REFERENCES customer(customer_id)
);

-- 2.5. PRODUCT_CATEGORY
CREATE TABLE product_category (
    category_id VARCHAR(36) NOT NULL,
    category_name NVARCHAR(100) NOT NULL,
    CONSTRAINT pk_product_category PRIMARY KEY (category_id)
);

-- 2.6. PRODUCT
CREATE TABLE product (
    product_id VARCHAR(36) NOT NULL,
    product_name NVARCHAR(100) NOT NULL,
    category_id VARCHAR(36) NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    expiration_date DATETIME2 NOT NULL,
    product_status VARCHAR(30) DEFAULT 'ON_SALE',
    CONSTRAINT pk_product PRIMARY KEY (product_id),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES product_category(category_id)
);

-- 2.7. VOUCHER
CREATE TABLE voucher (
    voucher_id VARCHAR(36) NOT NULL,
    employee_id VARCHAR(36) NULL,
    total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    created_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT pk_voucher PRIMARY KEY (voucher_id),
    CONSTRAINT fk_voucher_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
);

-- 2.8. PURCHASE_RECEIPT
CREATE TABLE purchase_receipt (
    voucher_id VARCHAR(36) NOT NULL,
    supplier_name NVARCHAR(100) NOT NULL,
    CONSTRAINT pk_purchase_receipt PRIMARY KEY (voucher_id),
    CONSTRAINT fk_purchase_receipt_voucher FOREIGN KEY (voucher_id) REFERENCES voucher(voucher_id)
);

-- 2.9. PURCHASE_RECEIPT_DETAIL
CREATE TABLE purchase_receipt_detail (
    purchase_detail_id VARCHAR(36) NOT NULL,
    voucher_id VARCHAR(36) NULL,
    product_id VARCHAR(36) NULL,
    purchased_quantity INT NOT NULL,
    purchase_price DECIMAL(12, 2) NULL,
    expiration_date DATETIME2 NOT NULL,
    CONSTRAINT pk_purchase_receipt_detail PRIMARY KEY (purchase_detail_id),
    CONSTRAINT fk_purchase_detail_receipt FOREIGN KEY (voucher_id) REFERENCES purchase_receipt(voucher_id),
    CONSTRAINT fk_purchase_detail_product FOREIGN KEY (product_id) REFERENCES product(product_id)
);

-- 2.10. DISPOSAL_RECEIPT
CREATE TABLE disposal_receipt (
    voucher_id VARCHAR(36) NOT NULL,
    CONSTRAINT pk_disposal_receipt PRIMARY KEY (voucher_id),
    CONSTRAINT fk_disposal_receipt_voucher FOREIGN KEY (voucher_id) REFERENCES voucher(voucher_id)
);

-- 2.11. DISPOSAL_RECEIPT_DETAIL
CREATE TABLE disposal_receipt_detail (
    disposal_detail_id VARCHAR(36) NOT NULL,
    voucher_id VARCHAR(36) NULL,
    product_id VARCHAR(36) NULL,
    disposed_quantity INT NOT NULL,
    disposal_reason NVARCHAR(MAX) NULL,
    CONSTRAINT pk_disposal_receipt_detail PRIMARY KEY (disposal_detail_id),
    CONSTRAINT fk_disposal_detail_receipt FOREIGN KEY (voucher_id) REFERENCES disposal_receipt(voucher_id),
    CONSTRAINT fk_disposal_detail_product FOREIGN KEY (product_id) REFERENCES product(product_id)
);

-- 2.12. INVENTORY_REPORT
CREATE TABLE inventory_report (
    report_id VARCHAR(36) NOT NULL,
    employee_id VARCHAR(36) NULL,
    report_date DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT pk_inventory_report PRIMARY KEY (report_id),
    CONSTRAINT fk_inventory_report_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
);

-- 2.13. INVENTORY_REPORT_DETAIL
CREATE TABLE inventory_report_detail (
    report_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    system_quantity INT NULL,
    actual_quantity INT NULL,
    CONSTRAINT pk_inventory_report_detail PRIMARY KEY (report_id, product_id),
    CONSTRAINT fk_inventory_detail_report FOREIGN KEY (report_id) REFERENCES inventory_report(report_id),
    CONSTRAINT fk_inventory_detail_product FOREIGN KEY (product_id) REFERENCES product(product_id)
);

-- 2.14. INVOICE
CREATE TABLE invoice (
    invoice_id VARCHAR(36) NOT NULL,
    customer_id VARCHAR(36) NULL,
    employee_id VARCHAR(36) NULL,
    status VARCHAR(30) NOT NULL,
    original_total DECIMAL(12, 2) NOT NULL,
    discount_amount DECIMAL(12, 2) DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT pk_invoice PRIMARY KEY (invoice_id),
    CONSTRAINT fk_invoice_customer FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    CONSTRAINT fk_invoice_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
);

-- 2.15. INVOICE_DETAIL
CREATE TABLE invoice_detail (
    invoice_detail_id VARCHAR(36) NOT NULL,
    invoice_id VARCHAR(36) NULL,
    product_id VARCHAR(36) NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    subtotal DECIMAL(12, 2) NOT NULL,
    CONSTRAINT pk_invoice_detail PRIMARY KEY (invoice_detail_id),
    CONSTRAINT fk_invoice_detail_invoice FOREIGN KEY (invoice_id) REFERENCES invoice(invoice_id),
    CONSTRAINT fk_invoice_detail_product FOREIGN KEY (product_id) REFERENCES product(product_id)
);

-- 2.16. PAYMENT
CREATE TABLE payment (
    transaction_id VARCHAR(36) NOT NULL,
    invoice_id VARCHAR(36) NULL,
    payment_method VARCHAR(30) DEFAULT 'CASH',
    amount DECIMAL(12, 2) NULL,
    status VARCHAR(30) DEFAULT 'UNPAID',
    payment_time DATETIME2 NULL,
    CONSTRAINT pk_payment PRIMARY KEY (transaction_id),
    CONSTRAINT fk_payment_invoice FOREIGN KEY (invoice_id) REFERENCES invoice(invoice_id)
);

-- 2.17. RETURN_RECEIPT
CREATE TABLE return_receipt (
    return_receipt_id VARCHAR(36) NOT NULL,
    original_invoice_id VARCHAR(36) NOT NULL,
    employee_id VARCHAR(36) NOT NULL,
    refund_amount DECIMAL(12, 2) NOT NULL,
    return_reason NVARCHAR(MAX) NULL,
    requested_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT pk_return_receipt PRIMARY KEY (return_receipt_id),
    CONSTRAINT fk_return_receipt_invoice FOREIGN KEY (original_invoice_id) REFERENCES invoice(invoice_id),
    CONSTRAINT fk_return_receipt_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
);
