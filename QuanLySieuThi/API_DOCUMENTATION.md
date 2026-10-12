# TÀI LIỆU TÍCH HỢP API BACKEND - SIÊU THỊ MINI (QUẢN LÝ SIÊU THỊ)

> **Dành cho**: Frontend Developer, Mobile Developer & Tester  
> **Phiên bản API**: `1.0.0`  
> **Giao thức**: HTTP RESTful JSON  
> **Base URL Mặc định (Local)**: `http://localhost:8080/QuanLySieuThi` (hoặc `http://localhost:8080` tùy cấu hình server)  

---

## 1. QUY CHUẨN CHUNG

### 1.1. Headers
- `Content-Type: application/json` (cho toàn bộ request POST/PUT).
- `Authorization: Bearer <access_token>` (bắt buộc cho tất cả các API được bảo vệ).

### 1.2. Định dạng phản hồi chuẩn (Standard JSON Response)

#### Phản hồi thành công:
```json
{
  "status": 200,
  "message": "Mô tả kết quả thành công",
  "data": { ... }
}
```
*(Đối với API phân trang có thêm trường `meta`)*:
```json
{
  "status": 200,
  "message": "Lấy danh sách thành công",
  "data": [ ... ],
  "meta": {
    "page": 1,
    "limit": 10,
    "total": 45,
    "totalPages": 5
  }
}
```

#### Phản hồi lỗi:
```json
{
  "status": 400,
  "message": "Chi tiết lỗi cụ thể từ hệ thống"
}
```

### 1.3. Mã HTTP Status
- `200 OK`: Yêu cầu xử lý thành công.
- `201 CREATED`: Tạo mới tài nguyên thành công.
- `400 BAD REQUEST`: Thiếu dữ liệu, sai định dạng dữ liệu (validation failed).
- `401 UNAUTHORIZED`: Chưa đăng nhập, token không hợp lệ hoặc đã hết hạn.
- `403 FORBIDDEN`: Không có quyền hạn truy cập API này (Role không khớp).
- `404 NOT FOUND`: Không tìm thấy tài nguyên theo ID.
- `409 CONFLICT`: Xung đột dữ liệu (ví dụ: trùng username, số điện thoại, trùng tài nguyên).
- `429 TOO MANY REQUESTS`: Bị giới hạn tần suất gửi yêu cầu (Rate Limit anti-spam).
- `500 INTERNAL SERVER ERROR`: Lỗi hệ thống server.

---

## 2. PHÂN QUYỀN HỆ THỐNG (ROLE-BASED ACCESS CONTROL)

Hệ thống hỗ trợ 3 chức vụ (`position`):
1. **`STORE_OWNER`** (Chủ cửa hàng): Toàn quyền truy cập tất cả API hệ thống.
2. **`CASHIER`** (Thu ngân): Bán hàng, tạo hóa đơn, xử lý thanh toán (tiền mặt/VNPay), quản lý khách hàng, xử lý đổi trả hàng, xem danh sách sản phẩm.
3. **`INVENTORY_MANAGER`** (Thủ kho): Nhập hàng (phiếu nhập), kiểm kê hàng hóa (báo cáo kiểm kê kho), xem sản phẩm & cảnh báo tồn kho thấp.

---

## 3. DANH SÁCH CHI TIẾT API THEO MODULE

---

### MODULE 1: AUTHENTICATION (`/api/auth`)

#### 1. Đăng nhập
- **Endpoint**: `POST /api/auth/login`
- **Quyền**: Public
- **Request Body**:
```json
{
  "username": "admin",
  "password": "password123"
}
```
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsIn...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "employeeId": "emp-uuid-123",
    "username": "admin",
    "fullName": "Nguyễn Văn Quản Lý",
    "position": "STORE_OWNER"
  }
}
```
> **Lưu ý quan trọng cho FE**: Server tự động thiết lập Cookie `refreshToken` (`HttpOnly`, `Path=/`). FE chỉ cần lưu trữ `accessToken` vào `localStorage` hoặc bộ nhớ state để gửi kèm header `Authorization: Bearer <token>`.

#### 2. Đăng xuất
- **Endpoint**: `POST /api/auth/logout`
- **Quyền**: Public
- **Mô tả**: Tự động xóa Cookie `refreshToken`.
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Đăng xuất thành công"
}
```

#### 3. Làm mới Access Token (Refresh Token)
- **Endpoint**: `POST /api/auth/refresh`
- **Quyền**: Public (yêu cầu Cookie `refreshToken`)
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsIn..."
  }
}
```

---

### MODULE 2: EMPLOYEE MANAGEMENT (`/api/employee`)
> **Quyền**: `STORE_OWNER` (nhân viên chỉ được xem thông tin chính mình qua `GET /api/employee/:id`).

#### 1. Lấy danh sách nhân viên
- **Endpoint**: `GET /api/employee`
- **Query Params**:
  - `page` (int, default: 1)
  - `limit` (int, default: 10)
  - `keyword` (string, tìm theo tên hoặc username)
  - `position` (string: `STORE_OWNER`, `CASHIER`, `INVENTORY_MANAGER`)
  - `status` (boolean: `true`/`false`)
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Lấy danh sách nhân viên thành công",
  "data": [
    {
      "employeeId": "e1a2b3c4-...",
      "username": "cashier01",
      "fullName": "Trần Thị Thu Ngân",
      "phoneNumber": "0987654321",
      "email": "thungan@gmail.com",
      "position": "CASHIER",
      "status": true,
      "createdAt": "2026-10-10T08:30:00"
    }
  ],
  "meta": {
    "page": 1,
    "limit": 10,
    "total": 1,
    "totalPages": 1
  }
}
```

#### 2. Lấy chi tiết nhân viên
- **Endpoint**: `GET /api/employee/:id`
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Lấy chi tiết nhân viên thành công",
  "data": {
    "employeeId": "e1a2b3c4-...",
    "username": "cashier01",
    "fullName": "Trần Thị Thu Ngân",
    "phoneNumber": "0987654321",
    "email": "thungan@gmail.com",
    "position": "CASHIER",
    "status": true,
    "createdAt": "2026-10-10T08:30:00"
  }
}
```

#### 3. Tạo tài khoản nhân viên
- **Endpoint**: `POST /api/employee`
- **Request Body**:
```json
{
  "username": "inventory01",
  "password": "Password@123",
  "fullName": "Lê Văn Kho",
  "phoneNumber": "0912345678",
  "email": "kho@gmail.com",
  "position": "INVENTORY_MANAGER",
  "status": true
}
```
- **Response `201 CREATED`**:
```json
{
  "status": 201,
  "message": "Tạo nhân viên thành công",
  "data": {
    "employeeId": "new-uuid-...",
    "username": "inventory01",
    "fullName": "Lê Văn Kho",
    "phoneNumber": "0912345678",
    "email": "kho@gmail.com",
    "position": "INVENTORY_MANAGER",
    "status": true
  }
}
```

#### 4. Cập nhật nhân viên
- **Endpoint**: `PUT /api/employee/:id`
- **Request Body**: (các trường cần update, password có thể bỏ trống nếu không đổi mật khẩu)
```json
{
  "fullName": "Lê Văn Kho (Cập nhật)",
  "phoneNumber": "0912345679",
  "email": "kho.new@gmail.com",
  "position": "INVENTORY_MANAGER",
  "status": true
}
```

#### 5. Xóa mềm nhân viên
- **Endpoint**: `DELETE /api/employee/:id`
- **Mô tả**: Chuyển `status` thành `false` (không xóa vật lý trong database).

---

### MODULE 3: CUSTOMER MANAGEMENT (`/api/customer`)
> **Quyền**: `STORE_OWNER` hoặc `CASHIER`.

#### 1. Lấy danh sách khách hàng
- **Endpoint**: `GET /api/customer`
- **Query Params**:
  - `page` (default: 1)
  - `limit` (default: 10)
  - `keyword` (tìm theo tên hoặc số điện thoại)
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Lấy danh sách khách hàng thành công",
  "data": [
    {
      "customerId": "cust-01-uuid",
      "fullName": "Nguyễn Hoàng Nam",
      "phoneNumber": "0909123456",
      "email": "nam@gmail.com",
      "loyaltyPoints": 350,
      "createdAt": "2026-10-01T10:00:00"
    }
  ]
}
```

#### 2. Tạo mới khách hàng
- **Endpoint**: `POST /api/customer`
- **Request Body**:
```json
{
  "fullName": "Phạm Minh Thư",
  "phoneNumber": "0938112233",
  "email": "thu@gmail.com"
}
```
- **Response `201 CREATED`**:
```json
{
  "status": 201,
  "message": "Tạo khách hàng thành công",
  "data": {
    "customerId": "cust-new-uuid",
    "fullName": "Phạm Minh Thư",
    "phoneNumber": "0938112233",
    "email": "thu@gmail.com",
    "loyaltyPoints": 0,
    "createdAt": "2026-10-13T00:30:00"
  }
}
```

#### 3. Lịch sử mua hàng của khách hàng
- **Endpoint**: `GET /api/customer/:id/invoices`
- **Query Params**: `page`, `limit`, `status` (`PENDING`, `PAID`, `CANCELLED`), `fromDate`, `toDate`
- **Response `200 OK`**: Danh sách hóa đơn của khách hàng kèm tổng tiền, giảm giá.

---

### MODULE 4: PRODUCT MANAGEMENT (`/api/product`)
> **Quyền**: Mọi nhân viên đã xác thực đều có thể xem danh sách sản phẩm. Cập nhật sản phẩm yêu cầu `STORE_OWNER` hoặc `INVENTORY_MANAGER`.

#### 1. Lấy danh sách sản phẩm
- **Endpoint**: `GET /api/product`
- **Query Params**:
  - `page` (default: 1)
  - `limit` (default: 10)
  - `keyword` (tên sản phẩm)
  - `categoryId` (ID danh mục)
  - `productStatus` (`ON_SALE`, `STOP_SELLING`)
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Lấy danh sách sản phẩm thành công.",
  "data": [
    {
      "productId": "prod-001",
      "productName": "Sữa chua Vinamilk 100g",
      "categoryId": "cat-01",
      "categoryName": "Sản phẩm từ sữa",
      "stockQuantity": 85,
      "expirationDate": "2026-11-20T00:00:00",
      "productStatus": "ON_SALE",
      "unitPrice": 8000.00
    }
  ]
}
```

#### 2. Lấy chi tiết sản phẩm
- **Endpoint**: `GET /api/product/:id`

#### 3. Cập nhật thông tin sản phẩm
- **Endpoint**: `PUT /api/product/:id`
- **Request Body**:
```json
{
  "productName": "Sữa chua Vinamilk nha đam 100g",
  "categoryId": "cat-01",
  "unitPrice": 8500.00,
  "productStatus": "ON_SALE"
}
```

#### 4. Cảnh báo tồn kho thấp (Low Stock Alert)
- **Endpoint**: `GET /api/product/low-stock/alerts`
- **Query Params**:
  - `threshold` (ngưỡng số lượng tồn, default: 10)
  - `categoryId` (tùy chọn lọc theo danh mục)
  - `page`, `limit`
- **Response `200 OK`**: Danh sách sản phẩm có `stockQuantity <= threshold`.

---

### MODULE 5: NHẬP HÀNG (`/api/purchase-receipt`)
> **Quyền**: `STORE_OWNER` hoặc `INVENTORY_MANAGER`.

#### 1. Tạo phiếu nhập hàng
- **Endpoint**: `POST /api/purchase-receipt`
- **Request Body**:
```json
{
  "supplierName": "Công ty TNHH Vinamilk",
  "note": "Nhập hàng đợt giữa tháng",
  "items": [
    {
      "productId": "prod-001",
      "productName": "Sữa chua Vinamilk 100g",
      "categoryId": "cat-01",
      "purchasedQuantity": 100,
      "purchasePrice": 6000.00,
      "unitPrice": 8000.00,
      "expirationDate": "2026-11-25T00:00:00"
    }
  ]
}
```
> *Ghi chú*: Nếu sản phẩm chưa có mã, FE có thể để trống `productId`, hệ thống sẽ tự sinh UUID và tạo mới sản phẩm với giá bán `unitPrice`. Tồn kho sẽ tự động được cộng dồn.

- **Response `201 CREATED`**:
```json
{
  "status": 201,
  "message": "Nhập hàng thành công",
  "data": {
    "voucherId": "vch-uuid-...",
    "supplierName": "Công ty TNHH Vinamilk",
    "createdAt": "2026-10-13T00:35:00",
    "totalAmount": 600000.00,
    "createdBy": {
      "employeeId": "emp-uuid",
      "fullName": "Lê Văn Kho"
    },
    "importedProducts": [
      {
        "productId": "prod-001",
        "productName": "Sữa chua Vinamilk 100g",
        "purchasedQuantity": 100,
        "purchasePrice": 6000.00,
        "unitPrice": 8000.00,
        "expirationDate": "2026-11-25T00:00:00"
      }
    ]
  }
}
```

#### 2. Danh sách phiếu nhập hàng
- **Endpoint**: `GET /api/purchase-receipt`
- **Query Params**: `page`, `limit`, `supplierName`, `employeeId`, `fromDate`, `toDate`

#### 3. Chi tiết phiếu nhập hàng
- **Endpoint**: `GET /api/purchase-receipt/:id`

---

### MODULE 6: BÁN HÀNG & HÓA ĐƠN (`/api/invoice`)
> **Quyền**: `STORE_OWNER` hoặc `CASHIER`.

#### 1. Tạo hóa đơn mới (POS Checkout)
- **Endpoint**: `POST /api/invoice`
- **Request Body**:
```json
{
  "phoneNumber": "0909123456",
  "usePoints": false,
  "items": [
    {
      "productId": "prod-001",
      "quantity": 3
    }
  ]
}
```
> *Lưu ý*:
> - `phoneNumber`: Tùy chọn. Nếu có, hệ thống tự động tìm và gắn khách hàng thân thiết.
> - `usePoints`: `true` nếu khách muốn quy đổi điểm tích lũy thành giảm giá (tối đa 50% hóa đơn).
> - Khi tạo hóa đơn, trạng thái hóa đơn mặc định là `PENDING` (chờ thanh toán). Tồn kho sản phẩm được trừ tự động.

- **Response `201 CREATED`**:
```json
{
  "status": 201,
  "message": "Tạo hóa đơn thành công",
  "data": {
    "invoiceId": "inv-uuid-001",
    "createdAt": "2026-10-13T00:38:00",
    "status": "PENDING",
    "originalTotal": 24000.00,
    "discountAmount": 0.00,
    "finalTotal": 24000.00,
    "pointsUsed": 0,
    "pointsEarned": 200,
    "employee": {
      "employeeId": "emp-uuid",
      "fullName": "Trần Thị Thu Ngân"
    },
    "customer": {
      "customerId": "cust-01-uuid",
      "fullName": "Nguyễn Hoàng Nam",
      "phoneNumber": "0909123456"
    },
    "items": [
      {
        "productId": "prod-001",
        "productName": "Sữa chua Vinamilk 100g",
        "quantity": 3,
        "unitPrice": 8000.00,
        "subtotal": 24000.00
      }
    ]
  }
}
```

#### 2. Danh sách hóa đơn
- **Endpoint**: `GET /api/invoice`
- **Query Params**: `page`, `limit`, `status` (`PENDING`, `PAID`, `CANCELLED`), `fromDate`, `toDate`, `customerId`, `employeeId`

#### 3. Chi tiết hóa đơn
- **Endpoint**: `GET /api/invoice/:id`

#### 4. Hủy hóa đơn
- **Endpoint**: `DELETE /api/invoice/:id`
- **Mô tả**: Chỉ hủy được hóa đơn ở trạng thái `PENDING`. Khi hủy, số lượng tồn kho của các sản phẩm sẽ được hoàn lại tự động.

---

### MODULE 7: THANH TOÁN (`/api/payment`)
> **Quyền**: `STORE_OWNER` hoặc `CASHIER`. (Riêng webhook VNPay return/ipn là Public).

#### 1. Xử lý thanh toán hóa đơn
- **Endpoint**: `POST /api/payment/process`
- **Request Body (Tiền mặt - CASH)**:
```json
{
  "invoiceId": "inv-uuid-001",
  "paymentMethod": "CASH"
}
```
- **Response `200 OK` (CASH)**:
```json
{
  "status": 200,
  "message": "Khởi tạo thanh toán thành công",
  "data": {
    "transactionId": "tx-uuid-...",
    "invoiceId": "inv-uuid-001",
    "amount": 24000.00,
    "paymentMethod": "CASH",
    "status": "PAID",
    "paymentTime": "2026-10-13T00:40:00Z"
  }
}
```

- **Request Body (Chuyển khoản VNPay - VNPAY)**:
```json
{
  "invoiceId": "inv-uuid-001",
  "paymentMethod": "VNPAY"
}
```
- **Response `200 OK` (VNPAY)**:
```json
{
  "status": 200,
  "message": "Khởi tạo thanh toán thành công",
  "data": {
    "transactionId": "tx-uuid-...",
    "invoiceId": "inv-uuid-001",
    "amount": 24000.00,
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=2400000&..."
  }
}
```
> **Luồng FE với VNPay**: Khi nhận được `paymentUrl`, FE chuyển hướng (redirect) người dùng sang link đó để quét mã QR / nhập thẻ. Sau khi thanh toán xong, cổng VNPay sẽ chuyển hướng trình duyệt về `GET /api/payment/vnpay-return`.

#### 2. Lấy danh sách giao dịch thanh toán
- **Endpoint**: `GET /api/payment/transactions`
- **Query Params**: `page`, `limit`, `paymentMethod` (`CASH`, `VNPAY`), `status` (`PAID`, `UNPAID`), `fromDate`, `toDate`

#### 3. Báo cáo doanh thu thanh toán
- **Endpoint**: `GET /api/payment/summary`
- **Query Params**: `fromDate`, `toDate`
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Lấy thông tin tổng kết doanh thu thành công",
  "data": {
    "totalCash": 1500000.00,
    "totalVnpay": 850000.00,
    "totalRevenue": 2350000.00,
    "totalTransactions": 18
  }
}
```

#### 4. Xem lịch sử thanh toán của 1 hóa đơn
- **Endpoint**: `GET /api/payment/history/:invoiceId`

---

### MODULE 8: ĐỔI TRẢ HÀNG (`/api/return-receipt`)
> **Quyền**: `STORE_OWNER` hoặc `CASHIER`.

#### 1. Tạo phiếu trả hàng
- **Endpoint**: `POST /api/return-receipt`
- **Request Body**:
```json
{
  "invoiceId": "inv-uuid-001",
  "reason": "Hàng bị móp méo khi vận chuyển",
  "items": [
    {
      "productId": "prod-001",
      "returnQuantity": 1
    }
  ]
}
```
- **Response `201 CREATED`**:
```json
{
  "status": 201,
  "message": "Tạo phiếu trả hàng thành công",
  "data": {
    "returnReceiptId": "ret-uuid-...",
    "invoiceId": "inv-uuid-001",
    "createdAt": "2026-10-13T00:45:00",
    "totalRefundAmount": 8000.00,
    "reason": "Hàng bị móp méo khi vận chuyển",
    "employee": {
      "employeeId": "emp-uuid",
      "fullName": "Trần Thị Thu Ngân"
    },
    "items": [
      {
        "productId": "prod-001",
        "productName": "Sữa chua Vinamilk 100g",
        "returnQuantity": 1,
        "refundPrice": 8000.00
      }
    ]
  }
}
```

#### 2. Danh sách phiếu trả hàng
- **Endpoint**: `GET /api/return-receipt`
- **Query Params**: `page`, `limit`, `invoiceId`, `employeeId`, `fromDate`, `toDate`

#### 3. Chi tiết phiếu trả hàng
- **Endpoint**: `GET /api/return-receipt/:id`

---

### MODULE 9: BÁO CÁO KIỂM KÊ KHO (`/api/report`)
> **Quyền**: `STORE_OWNER` hoặc `INVENTORY_MANAGER`.

#### 1. Tạo biên bản kiểm kê kho (Lệch tồn kho)
- **Endpoint**: `POST /api/report/stock-discrepancy`
- **Request Body**:
```json
{
  "note": "Kiểm kê định kỳ tháng 10",
  "items": [
    {
      "productId": "prod-001",
      "actualQuantity": 82,
      "discrepancyReason": "Hư hỏng bể hộp trong kho"
    }
  ]
}
```
- **Response `201 CREATED`**:
```json
{
  "status": 201,
  "message": "Tạo báo cáo kiểm kê kho thành công, đã gửi yêu cầu xử lý đến Chủ cửa hàng",
  "data": {
    "reportId": "rep-uuid-...",
    "createdAt": "2026-10-13T00:50:00",
    "note": "Kiểm kê định kỳ tháng 10",
    "createdBy": {
      "employeeId": "emp-uuid",
      "fullName": "Lê Văn Kho"
    },
    "discrepancies": [
      {
        "productId": "prod-001",
        "productName": "Sữa chua Vinamilk 100g",
        "systemQuantity": 85,
        "actualQuantity": 82,
        "difference": -3,
        "discrepancyReason": "Hư hỏng bể hộp trong kho"
      }
    ]
  }
}
```

#### 2. Danh sách báo cáo kiểm kê kho
- **Endpoint**: `GET /api/report/stock-discrepancy`
- **Query Params**: `page`, `limit`, `employeeId`, `fromDate`, `toDate`

#### 3. Chi tiết báo cáo kiểm kê kho
- **Endpoint**: `GET /api/report/stock-discrepancy/:id`

---

### MODULE 10: ĐÁNH GIÁ KHÁCH HÀNG (`/api/feedback`)

#### 1. Gửi đánh giá phản hồi (Public cho khách hàng)
- **Endpoint**: `POST /api/feedback`
- **Quyền**: Public (Áp dụng **RateLimitFilter**: Tối đa 5 lượt gửi/phút/IP để chống spam)
- **Request Body**:
```json
{
  "customerName": "Khách hàng thân thiết",
  "phoneNumber": "0909123456",
  "rating": 5,
  "comment": "Dịch vụ thanh toán rất nhanh, nhân viên thân thiện!"
}
```
- **Response `201 CREATED`**:
```json
{
  "status": 201,
  "message": "Cảm ơn bạn đã gửi đánh giá cho cửa hàng!",
  "data": {
    "feedbackId": "fb-uuid-...",
    "createdAt": "2026-10-13T00:52:00.000Z"
  }
}
```

#### 2. Xem danh sách đánh giá & Tổng hợp thống kê
- **Endpoint**: `GET /api/feedback`
- **Quyền**: Chỉ dành cho `STORE_OWNER`
- **Query Params**:
  - `page` (default: 1)
  - `limit` (default: 10)
  - `rating` (lọc từ 1 đến 5 sao)
  - `fromDate`, `toDate` (định dạng `YYYY-MM-DD`)
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Lấy danh sách đánh giá thành công",
  "data": {
    "summary": {
      "totalFeedbacks": 42,
      "averageRating": 4.8,
      "ratingBreakdown": {
        "fiveStar": 35,
        "fourStar": 5,
        "threeStar": 2,
        "twoStar": 0,
        "oneStar": 0
      }
    },
    "feedbacks": [
      {
        "feedbackId": "fb-uuid-...",
        "customerName": "Khách hàng thân thiết",
        "phoneNumber": "0909123456",
        "rating": 5,
        "comment": "Dịch vụ thanh toán rất nhanh, nhân viên thân thiện!",
        "createdAt": "2026-10-13T00:52:00.000Z"
      }
    ]
  }
}
```

#### 3. Chi tiết đánh giá
- **Endpoint**: `GET /api/feedback/:id`
- **Quyền**: Chỉ dành cho `STORE_OWNER`

---

## 4. HƯỚNG DẪN CẤU HÌNH FRONTEND (FE INTEGRATION GUIDE)

### 4.1. Cấu hình Axios / Fetch Client
Nên cấu hình một instance Axios dùng chung với Interceptor tự động gắn Access Token và xử lý Refresh Token khi gặp lỗi `401`:

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/QuanLySieuThi/api',
  withCredentials: true, // Bắt buộc true để trình duyệt tự gửi Cookie refreshToken
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Tự động đính kèm Bearer token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Tự động refresh token nếu 401
api.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        const res = await axios.post('http://localhost:8080/QuanLySieuThi/api/auth/refresh', {}, {
          withCredentials: true,
        });
        const newAccessToken = res.data.data.accessToken;
        localStorage.setItem('accessToken', newAccessToken);
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return api(originalRequest);
      } catch (refreshErr) {
        localStorage.removeItem('accessToken');
        window.location.href = '/login';
        return Promise.reject(refreshErr);
      }
    }
    return Promise.reject(error.response?.data || error.message);
  }
);

export default api;
```
