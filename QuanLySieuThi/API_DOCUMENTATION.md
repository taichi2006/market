# TÀI LIỆU TÍCH HỢP API BACKEND - SIÊU THỊ MINI (QUẢN LÝ SIÊU THỊ)

> **Dành cho**: Frontend Developer, Mobile Developer & Tester  
> **Phiên bản API**: `1.0.0` (Cập nhật chuẩn hóa Records DTO, Tối ưu hiệu năng, Báo cáo Doanh thu & Xuất Excel)  
> **Giao thức**: HTTP RESTful JSON / Binary Stream (Excel)  
> **Base URL Mặc định**:  
> - Local (Tomcat với context path): `http://localhost:8080/QuanLySieuThi/api`  
> - Local (Root context): `http://localhost:8080/api`  

---

## 1. QUY CHUẨN CHUNG & KIẾN TRÚC GIAO TIẾP

### 1.1. Headers Bắt Buộc
- **JSON Requests**: `Content-Type: application/json`
- **Xác thực (Protected APIs)**: `Authorization: Bearer <access_token>`
- **CORS & Cookie**: Frontend khi gọi API bằng `axios` hoặc `fetch` **BẮT BUỘC** bật `withCredentials: true` (hoặc `credentials: 'include'`) để trình duyệt tự động gửi và nhận cookie `refreshToken` (`HttpOnly`).

### 1.2. Cấu Trúc Response JSON Chuẩn (Standard API Response)

#### a. Phản hồi thành công thông thường:
```json
{
  "status": 200,
  "message": "Thông điệp kết quả thực thi",
  "data": { ... }
}
```

#### b. Phản hồi thành công có phân trang (Pagination):
```json
{
  "status": 200,
  "message": "Lấy danh sách thành công",
  "data": [ ... ],
  "meta": {
    "currentPage": 1,
    "totalPages": 5,
    "totalItems": 48,
    "pageSize": 10
  }
}
```

#### c. Phản hồi lỗi (Error Response):
```json
{
  "status": 400,
  "message": "Chi tiết nguyên nhân lỗi trả về từ hệ thống"
}
```

### 1.3. Bảng Mã Lỗi HTTP Phổ Biến
| Mã HTTP | Tên chuẩn | Ý nghĩa |
| :--- | :--- | :--- |
| **`200`** | OK | Thao tác truy vấn / cập nhật / xóa thành công. |
| **`201`** | CREATED | Tạo mới dữ liệu thành công. |
| **`400`** | BAD REQUEST | Dữ liệu gửi lên sai định dạng, thiếu trường bắt buộc, hoặc vi phạm logic nghiệp vụ. |
| **`401`** | UNAUTHORIZED | Chưa đăng nhập, thiếu token, hoặc token đã hết hạn. |
| **`403`** | FORBIDDEN | Đã đăng nhập nhưng Role không đủ quyền truy cập endpoint này. |
| **`404`** | NOT FOUND | Không tìm thấy tài nguyên theo ID hoặc sai đường dẫn endpoint. |
| **`409`** | CONFLICT | Xung đột dữ liệu (ví dụ: trùng username, trùng số điện thoại, xóa loại đang chứa hàng...). |
| **`500`** | INTERNAL SERVER ERROR | Lỗi ngoại lệ chưa xử lý ở tầng backend. |

---

## 2. PHÂN QUYỀN HỆ THỐNG (RBAC - ROLE BASED ACCESS CONTROL)

Hệ thống có 3 chức vụ chính được định nghĩa trong JWT Claim (`position`):
1. **`STORE_OWNER`** (Chủ cửa hàng / Quản trị viên): Toàn quyền truy cập tất cả API trên hệ thống.
2. **`CASHIER`** (Thu ngân): Bán hàng, tạo hóa đơn (`/invoice`), thanh toán (`/payment`), quản lý khách hàng (`/customer`), xử lý đổi trả (`/return-receipt`), tra cứu sản phẩm (`/product`).
3. **`INVENTORY_MANAGER`** (Thủ kho): Nhập hàng (`/purchase-receipt`), kiểm kê / báo cáo lệch tồn kho (`/report/stock-discrepancy`), xem & cập nhật sản phẩm (`/product`), cảnh báo tồn kho thấp.
4. **`Public`**: Không cần đăng nhập (`/api/auth/*`, `POST /api/feedback`, các webhook VNPay).

---

## 3. DANH SÁCH CHI TIẾT API THEO MODULE

---

### MODULE 1: XÁC THỰC NGƯỜI DÙNG (`/api/auth`)

#### 1.1. Đăng nhập hệ thống
- **Endpoint**: `POST /api/auth/login`
- **Quyền**: Public
- **Request Body**:
```json
{
  "username": "admin",
  "password": "Password@123"
}
```
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "employee": {
      "employeeId": "NV000001",
      "username": "admin",
      "fullName": "Nguyễn Văn Quản Lý",
      "position": "STORE_OWNER"
    }
  }
}
```
> **Cơ chế lưu Token cho FE**:  
> - `accessToken`: FE lưu vào `localStorage` hoặc state của ứng dụng để truyền vào header `Authorization: Bearer <accessToken>`.  
> - `refreshToken`: Server tự động ghi vào `Cookie` với cờ `HttpOnly; Path=/`. FE không thể và không cần đọc cookie này bằng JS.

---

#### 1.2. Làm mới Access Token (Refresh Token)
- **Endpoint**: `POST /api/auth/refresh`
- **Quyền**: Public (Trình duyệt tự động đính kèm Cookie `refreshToken`)
- **Request Body**: Rỗng `{}`
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Refresh token thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

---

#### 1.3. Đăng xuất
- **Endpoint**: `POST /api/auth/logout`
- **Quyền**: Public
- **Request Body**: Rỗng `{}`
- **Mô tả**: Xóa phiên refreshToken trong DB và xóa Cookie `refreshToken` trên trình duyệt.
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Đăng xuất thành công",
  "data": null
}
```

---

### MODULE 2: QUẢN LÝ NHÂN VIÊN (`/api/employee`)
> **Quyền hạn**:  
> - `STORE_OWNER`: Có quyền trên toàn bộ các phương thức.  
> - Nhân viên khác (`CASHIER`, `INVENTORY_MANAGER`): Chỉ được gọi `GET /api/employee/:id` với `:id` đúng bằng mã nhân viên của chính mình.

#### 2.1. Lấy danh sách nhân viên
- **Endpoint**: `GET /api/employee`
- **Query Params**:
  - `page` (int, default: `1`)
  - `limit` (int, default: `10`)
  - `keyword` (string, tìm theo họ tên hoặc username)
  - `position` (string: `STORE_OWNER`, `CASHIER`, `INVENTORY_MANAGER`)
  - `status` (boolean: `true` / `false`)
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Lấy danh sách nhân viên thành công",
  "data": [
    {
      "employeeId": "NV000001",
      "fullName": "Nguyễn Văn Quản Lý",
      "username": "admin",
      "phoneNumber": "0901234567",
      "position": "STORE_OWNER",
      "status": true
    }
  ],
  "meta": {
    "currentPage": 1,
    "totalPages": 1,
    "totalItems": 1,
    "pageSize": 10
  }
}
```

---

#### 2.2. Lấy chi tiết nhân viên
- **Endpoint**: `GET /api/employee/:id`
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Lấy chi tiết nhân viên thành công",
  "data": {
    "employeeId": "NV000002",
    "fullName": "Trần Thị Thu Ngân",
    "username": "cashier01",
    "phoneNumber": "0987654321",
    "position": "CASHIER",
    "status": true
  }
}
```

---

#### 2.3. Tạo tài khoản nhân viên mới
- **Endpoint**: `POST /api/employee`
- **Quyền**: `STORE_OWNER`
- **Request Body**:
```json
{
  "fullName": "Lê Văn Kho",
  "username": "inventory01",
  "password": "Password@123",
  "phoneNumber": "0912345678",
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
    "employeeId": "NV000003",
    "fullName": "Lê Văn Kho",
    "username": "inventory01",
    "phoneNumber": "0912345678",
    "position": "INVENTORY_MANAGER",
    "status": true
  }
}
```

---

#### 2.4. Cập nhật thông tin nhân viên
- **Endpoint**: `PUT /api/employee/:id`
- **Quyền**: `STORE_OWNER`
- **Request Body**:
```json
{
  "fullName": "Lê Văn Kho (Cập nhật)",
  "username": "inventory01",
  "password": "NewPassword@123",
  "phoneNumber": "0912345679",
  "position": "INVENTORY_MANAGER",
  "status": true
}
```
*(Nếu không muốn đổi mật khẩu, có thể để trống trường `password`)*.
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Cập nhật nhân viên thành công",
  "data": {
    "employeeId": "NV000003",
    "fullName": "Lê Văn Kho (Cập nhật)",
    "username": "inventory01",
    "phoneNumber": "0912345679",
    "position": "INVENTORY_MANAGER",
    "status": true
  }
}
```

---

#### 2.5. Xóa mềm nhân viên (Vô hiệu hóa tài khoản)
- **Endpoint**: `DELETE /api/employee/:id`
- **Quyền**: `STORE_OWNER` (Không thể tự xóa chính mình)
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Xóa nhân viên thành công",
  "data": {
    "employeeId": "NV000003",
    "status": false
  }
}
```

---

### MODULE 3: QUẢN LÝ LOẠI SẢN PHẨM (`/api/category`)
> **Quyền hạn**:  
> - `GET`: Tất cả nhân viên đã đăng nhập.  
> - `POST`, `PUT`, `DELETE`: Chỉ `STORE_OWNER`.

#### 3.1. Lấy danh sách loại sản phẩm
- **Endpoint**: `GET /api/category`
- **Query Params**:
  - `all`: `true` -> Trả về toàn bộ danh sách loại SP (phục vụ đổ Select/Dropdown).
  - Hoặc phân trang: `page` (default: 1), `limit` (default: 10), `keyword`.
- **Response `200 OK`** (Khi `?all=true`):
```json
{
  "status": 200,
  "message": "Lấy danh sách tất cả loại sản phẩm thành công",
  "data": [
    {
      "categoryId": "LSP001",
      "categoryName": "Nước giải khát",
      "productCount": 15
    },
    {
      "categoryId": "LSP002",
      "categoryName": "Bánh kẹo",
      "productCount": 8
    }
  ]
}
```

---

#### 3.2. Lấy chi tiết loại sản phẩm
- **Endpoint**: `GET /api/category/:id`
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Lấy thông tin loại sản phẩm thành công",
  "data": {
    "categoryId": "LSP001",
    "categoryName": "Nước giải khát",
    "productCount": 15
  }
}
```

---

#### 3.3. Tạo loại sản phẩm mới
- **Endpoint**: `POST /api/category`
- **Quyền**: `STORE_OWNER`
- **Request Body**:
```json
{
  "categoryName": "Thực phẩm đóng hộp"
}
```
*(Mã `categoryId` sẽ được Backend tự động sinh dạng `LSP001`, `LSP002`...)*
- **Response `201 CREATED`**:
```json
{
  "status": 201,
  "message": "Tạo loại sản phẩm thành công",
  "data": {
    "categoryId": "LSP003",
    "categoryName": "Thực phẩm đóng hộp",
    "productCount": 0
  }
}
```

---

#### 3.4. Cập nhật loại sản phẩm
- **Endpoint**: `PUT /api/category/:id`
- **Quyền**: `STORE_OWNER`
- **Request Body**:
```json
{
  "categoryName": "Thực phẩm đóng hộp & Đồ khô"
}
```
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Cập nhật loại sản phẩm thành công",
  "data": {
    "categoryId": "LSP003",
    "categoryName": "Thực phẩm đóng hộp & Đồ khô",
    "productCount": 0
  }
}
```

---

#### 3.5. Xóa loại sản phẩm
- **Endpoint**: `DELETE /api/category/:id`
- **Quyền**: `STORE_OWNER`
- **Lưu ý**: Nếu loại sản phẩm đang có sản phẩm thuộc về nó (`productCount > 0`), hệ thống sẽ trả lỗi `409 CONFLICT`.
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Xóa loại sản phẩm thành công",
  "data": null
}
```

---

### MODULE 4: QUẢN LÝ SẢN PHẨM (`/api/product`)

#### 4.1. Lấy danh sách sản phẩm
- **Endpoint**: `GET /api/product`
- **Quyền**: Nhân viên đã đăng nhập
- **Query Params**:
  - `page` (int, default: 1)
  - `limit` (int, default: 10)
  - `keyword` (string, tên sản phẩm hoặc mã sản phẩm)
  - `categoryId` (string, mã loại sản phẩm)
  - `productStatus` (`ON_SALE` hoặc `STOP_SELLING`)
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Lấy danh sách sản phẩm thành công.",
  "data": [
    {
      "productId": "SP000001",
      "productName": "Nước ngọt Coca-Cola 320ml",
      "unitPrice": 10000.00,
      "stockQuantity": 150,
      "expirationDate": "2027-01-01 00:00:00",
      "productStatus": "ON_SALE",
      "category": {
        "categoryId": "LSP001",
        "categoryName": "Nước giải khát"
      }
    }
  ],
  "meta": {
    "currentPage": 1,
    "totalPages": 1,
    "totalItems": 1,
    "pageSize": 10
  }
}
```

---

#### 4.2. Lấy chi tiết sản phẩm
- **Endpoint**: `GET /api/product/:id`
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Lấy thông tin sản phẩm thành công.",
  "data": {
    "productId": "SP000001",
    "productName": "Nước ngọt Coca-Cola 320ml",
    "unitPrice": 10000.00,
    "stockQuantity": 150,
    "expirationDate": "2027-01-01 00:00:00",
    "productStatus": "ON_SALE",
    "category": {
      "categoryId": "LSP001",
      "categoryName": "Nước giải khát"
    }
  }
}
```

---

#### 4.3. Cập nhật sản phẩm
- **Endpoint**: `PUT /api/product/:id`
- **Quyền**: `STORE_OWNER`, `INVENTORY_MANAGER`
- **Request Body**:
```json
{
  "productName": "Nước ngọt Coca-Cola 330ml Lon Sleek",
  "categoryId": "LSP001",
  "unitPrice": 11000.00,
  "productStatus": "ON_SALE"
}
```
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Cập nhật sản phẩm thành công.",
  "data": {
    "productId": "SP000001",
    "productName": "Nước ngọt Coca-Cola 330ml Lon Sleek",
    "unitPrice": 11000.00,
    "stockQuantity": 150,
    "expirationDate": "2027-01-01 00:00:00",
    "productStatus": "ON_SALE",
    "category": {
      "categoryId": "LSP001",
      "categoryName": "Nước giải khát"
    }
  }
}
```

---

#### 4.4. Cảnh báo tồn kho thấp (Low Stock Alerts)
- **Endpoint**: `GET /api/product/low-stock/alerts`
- **Quyền**: Nhân viên đã đăng nhập
- **Query Params**:
  - `threshold` (int, ngưỡng số lượng cảnh báo, default: `10`)
  - `categoryId` (tùy chọn lọc theo loại SP)
  - `page`, `limit`
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Lấy danh sách cảnh báo tồn kho thành công.",
  "data": [
    {
      "productId": "SP000002",
      "productName": "Bánh mì sandwich bơ sữa 120g",
      "stockQuantity": 3,
      "category": {
        "categoryId": "LSP002",
        "categoryName": "Bánh kẹo"
      }
    }
  ],
  "meta": {
    "currentPage": 1,
    "totalPages": 1,
    "totalItems": 1,
    "pageSize": 10
  }
}
```

---

### MODULE 5: QUẢN LÝ KHÁCH HÀNG (`/api/customer`)
> **Quyền hạn**: `STORE_OWNER` hoặc `CASHIER`.

#### 5.1. Lấy danh sách khách hàng
- **Endpoint**: `GET /api/customer`
- **Query Params**:
  - `page` (default: 1)
  - `limit` (default: 10)
  - `keyword` (tìm theo họ tên, số điện thoại)
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Lấy danh sách khách hàng thành công",
  "data": [
    {
      "customerId": "KH000001",
      "fullName": "Nguyễn Hoàng Nam",
      "phoneNumber": "0909123456",
      "email": "nam@gmail.com",
      "loyaltyPoints": 350,
      "createdAt": "2026-10-01 10:00:00"
    }
  ],
  "meta": {
    "currentPage": 1,
    "totalPages": 1,
    "totalItems": 1,
    "pageSize": 10
  }
}
```

---

#### 5.2. Tạo mới khách hàng
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
    "customerId": "KH000002",
    "fullName": "Phạm Minh Thư",
    "phoneNumber": "0938112233",
    "email": "thu@gmail.com",
    "loyaltyPoints": 0,
    "createdAt": "2026-10-14 15:30:00"
  }
}
```

---

#### 5.3. Xem lịch sử hóa đơn mua hàng của khách hàng
- **Endpoint**: `GET /api/customer/:id/invoices`
- **Query Params**: `page`, `limit`, `status` (`PENDING`, `PAID`, `CANCELLED`), `fromDate`, `toDate`
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Lấy lịch sử mua hàng thành công",
  "data": [
    {
      "invoiceId": "HD000001",
      "createdAt": "2026-10-14 10:15:00",
      "status": "PAID",
      "originalTotal": 200000.00,
      "discountAmount": 10000.00,
      "finalTotal": 190000.00,
      "employee": {
        "employeeId": "NV000002",
        "fullName": "Trần Thị Thu Ngân"
      }
    }
  ],
  "meta": {
    "currentPage": 1,
    "totalPages": 1,
    "totalItems": 1,
    "pageSize": 10
  }
}
```

---

### MODULE 6: NHẬP HÀNG (`/api/purchase-receipt`)
> **Quyền hạn**: `STORE_OWNER` hoặc `INVENTORY_MANAGER`.

#### 6.1. Tạo phiếu nhập hàng (Nhập kho)
- **Endpoint**: `POST /api/purchase-receipt`
- **Mô tả**: Tự động tăng tồn kho cho các sản phẩm đã có. Nếu sản phẩm mới (chưa có `productId`), hệ thống sẽ tạo mới sản phẩm trong kho.
- **Request Body**:
```json
{
  "supplierName": "Công ty TNHH Nước Giải Khát Coca-Cola",
  "items": [
    {
      "productId": "SP000001",
      "productName": "Nước ngọt Coca-Cola 320ml",
      "categoryId": "LSP001",
      "unitPrice": 10000.00,
      "purchasedQuantity": 100,
      "purchasePrice": 7500.00,
      "expirationDate": "2027-01-01"
    }
  ]
}
```
- **Response `201 CREATED`**:
```json
{
  "status": 201,
  "message": "Nhập hàng thành công",
  "data": {
    "voucherId": "PN000001",
    "supplierName": "Công ty TNHH Nước Giải Khát Coca-Cola",
    "totalAmount": 750000.00,
    "createdAt": "2026-10-14 16:00:00",
    "createdBy": {
      "employeeId": "NV000003",
      "fullName": "Lê Văn Kho",
      "username": "inventory01"
    },
    "createdProducts": [
      {
        "productId": "SP000001",
        "productName": "Nước ngọt Coca-Cola 320ml",
        "categoryId": "LSP001",
        "unitPrice": 10000.00,
        "stockQuantity": 250,
        "expirationDate": "2027-01-01",
        "productStatus": "ON_SALE"
      }
    ]
  }
}
```

---

#### 6.2. Lấy danh sách phiếu nhập hàng
- **Endpoint**: `GET /api/purchase-receipt`
- **Query Params**: `page`, `limit`, `supplierName`, `employeeId`, `fromDate`, `toDate`
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Lấy danh sách phiếu nhập hàng thành công",
  "data": [
    {
      "voucherId": "PN000001",
      "supplierName": "Công ty TNHH Nước Giải Khát Coca-Cola",
      "totalAmount": 750000.00,
      "createdAt": "2026-10-14 16:00:00",
      "createdBy": {
        "employeeId": "NV000003",
        "fullName": "Lê Văn Kho"
      }
    }
  ],
  "meta": {
    "currentPage": 1,
    "totalPages": 1,
    "totalItems": 1,
    "pageSize": 10
  }
}
```

---

#### 6.3. Chi tiết phiếu nhập hàng
- **Endpoint**: `GET /api/purchase-receipt/:id`
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Lấy chi tiết phiếu nhập hàng thành công",
  "data": {
    "voucherId": "PN000001",
    "supplierName": "Công ty TNHH Nước Giải Khát Coca-Cola",
    "totalAmount": 750000.00,
    "createdAt": "2026-10-14 16:00:00",
    "createdBy": {
      "employeeId": "NV000003",
      "fullName": "Lê Văn Kho",
      "username": "inventory01"
    },
    "items": [
      {
        "purchaseDetailId": "CTPN000001",
        "productId": "SP000001",
        "productName": "Nước ngọt Coca-Cola 320ml",
        "categoryName": "Nước giải khát",
        "purchasedQuantity": 100,
        "purchasePrice": 7500.00,
        "lineTotal": 750000.00,
        "expirationDate": "2027-01-01"
      }
    ]
  }
}
```

---

### MODULE 7: HÓA ĐƠN & BÁN HÀNG (`/api/invoice`)
> **Quyền hạn**: Nhân viên đã đăng nhập (`CASHIER`, `STORE_OWNER`).

#### 7.1. Tạo hóa đơn bán hàng (POS Checkout)
- **Endpoint**: `POST /api/invoice`
- **Mô tả**: Tự động trừ số lượng tồn kho sản phẩm. Trạng thái ban đầu là `PENDING`.
- **Request Body**:
```json
{
  "customerId": "KH000001",
  "useLoyaltyPoints": true,
  "items": [
    {
      "productId": "SP000001",
      "quantity": 5
    }
  ]
}
```
*(Nếu là khách vãng lai không có tài khoản, `customerId` có thể truyền `null` và `useLoyaltyPoints: false`)*.
- **Response `201 CREATED`**:
```json
{
  "status": 201,
  "message": "Tạo hóa đơn thành công",
  "data": {
    "invoiceId": "HD000001",
    "createdAt": "2026-10-14 16:15:00",
    "status": "PENDING",
    "originalTotal": 50000.00,
    "discountAmount": 5000.00,
    "finalTotal": 45000.00,
    "employee": {
      "employeeId": "NV000002",
      "fullName": "Trần Thị Thu Ngân"
    },
    "customer": {
      "customerId": "KH000001",
      "fullName": "Nguyễn Hoàng Nam",
      "phoneNumber": "0909123456",
      "loyaltyPoints": 300
    },
    "items": [
      {
        "invoiceDetailId": "CTHD000001",
        "productId": "SP000001",
        "productName": "Nước ngọt Coca-Cola 320ml",
        "quantity": 5,
        "unitPrice": 10000.00,
        "subtotal": 50000.00
      }
    ]
  }
}
```

---

#### 7.2. Lấy danh sách hóa đơn
- **Endpoint**: `GET /api/invoice`
- **Query Params**: `page`, `limit`, `status` (`PENDING`, `PAID`, `CANCELLED`), `fromDate`, `toDate`, `customerId`, `employeeId`
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Lấy danh sách hóa đơn thành công",
  "data": [
    {
      "invoiceId": "HD000001",
      "createdAt": "2026-10-14 16:15:00",
      "status": "PENDING",
      "originalTotal": 50000.00,
      "discountAmount": 5000.00,
      "finalAmount": 45000.00,
      "employee": {
        "employeeId": "NV000002",
        "fullName": "Trần Thị Thu Ngân"
      },
      "customer": {
        "customerId": "KH000001",
        "fullName": "Nguyễn Hoàng Nam",
        "phoneNumber": "0909123456"
      }
    }
  ],
  "meta": {
    "currentPage": 1,
    "totalPages": 1,
    "totalItems": 1,
    "pageSize": 10
  }
}
```

---

#### 7.3. Chi tiết hóa đơn
- **Endpoint**: `GET /api/invoice/:id`
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Lấy chi tiết hóa đơn thành công",
  "data": {
    "invoiceId": "HD000001",
    "createdAt": "2026-10-14 16:15:00",
    "status": "PENDING",
    "originalTotal": 50000.00,
    "discountAmount": 5000.00,
    "finalAmount": 45000.00,
    "employee": {
      "employeeId": "NV000002",
      "fullName": "Trần Thị Thu Ngân"
    },
    "customer": {
      "customerId": "KH000001",
      "fullName": "Nguyễn Hoàng Nam",
      "phoneNumber": "0909123456"
    },
    "items": [
      {
        "invoiceDetailId": "CTHD000001",
        "productId": "SP000001",
        "productName": "Nước ngọt Coca-Cola 320ml",
        "quantity": 5,
        "unitPrice": 10000.00,
        "subtotal": 50000.00
      }
    ]
  }
}
```

---

#### 7.4. Hủy hóa đơn
- **Endpoint**: `DELETE /api/invoice/:id`
- **Mô tả**: Hủy hóa đơn (chỉ áp dụng cho hóa đơn chưa thanh toán - `PENDING`). Số lượng sản phẩm sẽ được tự động hoàn trả lại tồn kho.
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Hủy hóa đơn thành công",
  "data": {
    "invoiceId": "HD000001",
    "status": "CANCELLED"
  }
}
```

---

### MODULE 8: THANH TOÁN TIỀN MẶT & VNPAY (`/api/payment`)
> **Quyền hạn**: `STORE_OWNER` hoặc `CASHIER`. (Các callback VNPay là Public).

#### 8.1. Khởi tạo thanh toán hóa đơn
- **Endpoint**: `POST /api/payment/process`
- **Request Body 1 (Tiền mặt - CASH)**:
```json
{
  "invoiceId": "HD000001",
  "paymentMethod": "CASH"
}
```
- **Response `200 OK` (CASH)**:
```json
{
  "status": 200,
  "message": "Khởi tạo thanh toán thành công",
  "data": {
    "transactionId": "GD000001",
    "invoiceId": "HD000001",
    "amount": 45000.00,
    "paymentMethod": "CASH",
    "status": "PAID",
    "paymentTime": "2026-10-14 16:20:00"
  }
}
```

- **Request Body 2 (Cổng thanh toán online - VNPAY)**:
```json
{
  "invoiceId": "HD000001",
  "paymentMethod": "VNPAY"
}
```
- **Response `200 OK` (VNPAY)**:
```json
{
  "status": 200,
  "message": "Khởi tạo thanh toán thành công",
  "data": {
    "transactionId": "GD000002",
    "invoiceId": "HD000001",
    "amount": 45000.00,
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=4500000&vnp_Command=pay&..."
  }
}
```
> **Luồng tích hợp VNPay cho Frontend**:
> 1. Nhận `paymentUrl` từ phản hồi trên.
> 2. Trình duyệt redirect người dùng đến URL đó: `window.location.href = res.data.data.paymentUrl`.
> 3. Khách hàng quét mã VNPAY-QR hoặc nhập thẻ test.
> 4. Cổng VNPay tự động gọi Webhook Server Backend (`/api/payment/vnpay-ipn`) và redirect người dùng về trang return (`/api/payment/vnpay-return`).

---

#### 8.2. Danh sách các giao dịch thanh toán
- **Endpoint**: `GET /api/payment/transactions`
- **Query Params**: `page`, `limit`, `paymentMethod` (`CASH`, `VNPAY`), `status` (`PAID`, `UNPAID`), `fromDate`, `toDate`
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Lấy danh sách giao dịch thành công",
  "data": [
    {
      "transactionId": "GD000001",
      "invoiceId": "HD000001",
      "paymentMethod": "CASH",
      "amount": 45000.00,
      "status": "PAID",
      "paymentTime": "2026-10-14 16:20:00",
      "cashierName": "Trần Thị Thu Ngân"
    }
  ],
  "meta": {
    "currentPage": 1,
    "totalPages": 1,
    "totalItems": 1,
    "pageSize": 10
  }
}
```

---

#### 8.3. Thống kê nhanh tổng quan thanh toán
- **Endpoint**: `GET /api/payment/summary`
- **Query Params**: `fromDate`, `toDate` (`YYYY-MM-DD`)
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Lấy thông tin tổng kết doanh thu thành công",
  "data": {
    "originalTotal": 5000000.00,
    "discountAmount": 300000.00,
    "finalAmount": 4700000.00,
    "totalPaid": 4700000.00,
    "remainingAmount": 0.00
  }
}
```

---

#### 8.4. Lịch sử thanh toán của 1 hóa đơn
- **Endpoint**: `GET /api/payment/history/:invoiceId`
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Lấy lịch sử thanh toán hóa đơn thành công",
  "data": {
    "invoiceId": "HD000001",
    "invoiceStatus": "PAID",
    "summary": {
      "originalTotal": 50000.00,
      "discountAmount": 5000.00,
      "finalAmount": 45000.00,
      "totalPaid": 45000.00,
      "remainingAmount": 0.00
    },
    "transactions": [
      {
        "transactionId": "GD000001",
        "invoiceId": "HD000001",
        "paymentMethod": "CASH",
        "amount": 45000.00,
        "status": "PAID",
        "paymentTime": "2026-10-14 16:20:00",
        "cashierName": "Trần Thị Thu Ngân"
      }
    ]
  }
}
```

---

### MODULE 9: TRẢ HÀNG & HOÀN TIỀN (`/api/return-receipt`)
> **Quyền hạn**: Nhân viên đã đăng nhập (`CASHIER`, `STORE_OWNER`).

#### 9.1. Tạo phiếu trả hàng
- **Endpoint**: `POST /api/return-receipt`
- **Mô tả**: Ghi nhận trả hàng từ hóa đơn đã thanh toán. Tồn kho sản phẩm được cộng lại tự động và tính toán số tiền hoàn trả (`refundAmount`).
- **Request Body**:
```json
{
  "invoiceId": "HD000001",
  "reason": "Khách mua nhầm loại sản phẩm, hàng còn nguyên tem",
  "items": [
    {
      "productId": "SP000001",
      "returnedQuantity": 1
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
    "returnReceiptId": "TH000001",
    "invoiceId": "HD000001",
    "reason": "Khách mua nhầm loại sản phẩm, hàng còn nguyên tem",
    "refundAmount": 9000.00,
    "createdAt": "2026-10-14 16:30:00",
    "createdBy": {
      "employeeId": "NV000002",
      "fullName": "Trần Thị Thu Ngân",
      "username": "cashier01"
    }
  }
}
```

---

#### 9.2. Lấy danh sách phiếu trả hàng
- **Endpoint**: `GET /api/return-receipt`
- **Query Params**: `page`, `limit`, `invoiceId`, `employeeId`, `fromDate`, `toDate`
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Lấy danh sách phiếu trả hàng thành công",
  "data": [
    {
      "returnReceiptId": "TH000001",
      "invoiceId": "HD000001",
      "refundAmount": 9000.00,
      "reason": "Khách mua nhầm loại sản phẩm, hàng còn nguyên tem",
      "createdAt": "2026-10-14 16:30:00",
      "createdBy": {
        "employeeId": "NV000002",
        "fullName": "Trần Thị Thu Ngân"
      }
    }
  ],
  "meta": {
    "currentPage": 1,
    "totalPages": 1,
    "totalItems": 1,
    "pageSize": 10
  }
}
```

---

#### 9.3. Chi tiết phiếu trả hàng
- **Endpoint**: `GET /api/return-receipt/:id`
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Lấy chi tiết phiếu trả hàng thành công",
  "data": {
    "returnReceiptId": "TH000001",
    "invoiceId": "HD000001",
    "refundAmount": 9000.00,
    "reason": "Khách mua nhầm loại sản phẩm, hàng còn nguyên tem",
    "createdAt": "2026-10-14 16:30:00",
    "createdBy": {
      "employeeId": "NV000002",
      "fullName": "Trần Thị Thu Ngân",
      "username": "cashier01"
    },
    "customer": {
      "customerId": "KH000001",
      "fullName": "Nguyễn Hoàng Nam",
      "phoneNumber": "0909123456"
    }
  }
}
```

---

### MODULE 10: BÁO CÁO DOANH THU, KIỂM KÊ & XUẤT EXCEL (`/api/report`)
> **Quyền hạn**: `STORE_OWNER` hoặc `INVENTORY_MANAGER`.

#### 10.1. Báo cáo doanh thu (Revenue Report)
- **Endpoint**: `GET /api/report/revenue`
- **Query Params**:
  - `type` (Bắt buộc một trong các giá trị: `DAILY`, `MONTHLY`, `YEARLY`, `CUSTOM`)
  - `date`: Định dạng `YYYY-MM-DD` (bắt buộc khi `type=DAILY`, ví dụ `2026-10-14`)
  - `month`: Số tháng từ `1` đến `12` (bắt buộc khi `type=MONTHLY`)
  - `year`: Năm `YYYY` (bắt buộc khi `type=MONTHLY` hoặc `type=YEARLY`, ví dụ `2026`)
  - `fromDate`, `toDate`: Định dạng `YYYY-MM-DD` (khi `type=CUSTOM`)
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Lấy báo cáo doanh thu thành công",
  "data": {
    "filter": {
      "type": "DAILY",
      "date": "2026-10-14",
      "month": null,
      "year": null,
      "from": null,
      "to": null
    },
    "summary": {
      "totalRevenue": 15450000.00,
      "originalTotal": 16200000.00,
      "totalDiscount": 750000.00,
      "totalRefund": 120000.00,
      "netRevenue": 15330000.00,
      "totalInvoices": 52,
      "paymentMethodBreakdown": {
        "cash": 9250000.00,
        "online": 6200000.00
      }
    },
    "timeline": [
      {
        "timePoint": "08:00",
        "revenue": 1250000.00,
        "invoicesCount": 4,
        "cashAmount": 850000.00,
        "onlineAmount": 400000.00
      },
      {
        "timePoint": "09:00",
        "revenue": 2100000.00,
        "invoicesCount": 7,
        "cashAmount": 1300000.00,
        "onlineAmount": 800000.00
      }
    ]
  }
}
```

---

#### 10.2. Xuất file Excel phiếu & báo cáo (Excel Export)
- **Endpoint**: `GET /api/report/export-excel` (hoặc `GET /api/report/excel`)
- **Content-Type Trả về**: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- **Header Trả về**: `Content-Disposition: attachment; filename="..."`
- **Query Params**:
  - `target` (hoặc `type`):
    - `PURCHASE`: Xuất phiếu nhập hàng.
    - `DISCREPANCY`: Xuất biên bản kiểm kê / phiếu xử lý hủy hàng hóa lệch hỏng.
  - `receiptId` (hoặc `id`): Mã phiếu cụ thể cần in (ví dụ: `PN000001` hoặc `KK000001`). Nếu truyền trường này, hệ thống sẽ xuất chi tiết từng mặt hàng của phiếu.
  - Trường hợp xuất danh sách tổng hợp theo chu kỳ (không truyền `receiptId`):
    - `filterType` (hoặc `period`): `DAILY` | `MONTHLY` | `YEARLY` | `ALL`
    - `date`: `YYYY-MM-DD` (khi `filterType=DAILY`)
    - `month`: `1` - `12` (khi `filterType=MONTHLY`)
    - `year`: `YYYY`
    - `fromDate`, `toDate`: `YYYY-MM-DD`

> **Cách FE xử lý tải file Excel với Axios**:
> ```javascript
> const downloadExcel = async () => {
>   const response = await api.get('/report/export-excel', {
>     params: { target: 'PURCHASE', receiptId: 'PN000001' },
>     responseType: 'blob', // Bắt buộc để nhận binary file
>   });
>   const url = window.URL.createObjectURL(new Blob([response]));
>   const link = document.createElement('a');
>   link.href = url;
>   link.setAttribute('download', 'phieu_nhap_PN000001.xlsx');
>   document.body.appendChild(link);
>   link.click();
>   link.remove();
> };
> ```

---

#### 10.3. Tạo biên bản kiểm kê kho (Lệch tồn kho)
- **Endpoint**: `POST /api/report/stock-discrepancy`
- **Request Body**:
```json
{
  "auditTitle": "Kiểm kê kho định kỳ giữa tháng 10",
  "note": "Kiểm tra hư hỏng và hạn sử dụng",
  "items": [
    {
      "productId": "SP000001",
      "actualQuantity": 145,
      "reason": "Hư hỏng bể 5 hộp trong quá trình xếp kệ"
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
    "reportId": "KK000001",
    "auditTitle": "Kiểm kê kho định kỳ giữa tháng 10",
    "createdAt": "2026-10-14 16:40:00",
    "createdBy": {
      "employeeId": "NV000003",
      "fullName": "Lê Văn Kho"
    },
    "totalDiscrepancyItems": 1,
    "items": [
      {
        "productId": "SP000001",
        "productName": "Nước ngọt Coca-Cola 320ml",
        "systemQuantity": 150,
        "actualQuantity": 145,
        "difference": -5,
        "status": "SHORTAGE",
        "reason": "Hư hỏng bể 5 hộp trong quá trình xếp kệ"
      }
    ]
  }
}
```

---

#### 10.4. Lấy danh sách báo cáo kiểm kê kho
- **Endpoint**: `GET /api/report/stock-discrepancy`
- **Query Params**: `page`, `limit`, `employeeId`, `fromDate`, `toDate`
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Lấy danh sách báo cáo kiểm kê kho thành công",
  "data": [
    {
      "reportId": "KK000001",
      "auditTitle": "Kiểm kê kho định kỳ giữa tháng 10",
      "createdAt": "2026-10-14 16:40:00",
      "totalDiscrepancyItems": 1,
      "createdBy": {
        "employeeId": "NV000003",
        "fullName": "Lê Văn Kho",
        "username": "inventory01"
      }
    }
  ],
  "meta": {
    "currentPage": 1,
    "totalPages": 1,
    "totalItems": 1,
    "pageSize": 10
  }
}
```

---

#### 10.5. Lấy chi tiết báo cáo kiểm kê kho
- **Endpoint**: `GET /api/report/stock-discrepancy/:id`
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Lấy chi tiết báo cáo kiểm kê kho thành công",
  "data": {
    "reportId": "KK000001",
    "auditTitle": "Kiểm kê kho định kỳ giữa tháng 10",
    "note": "Kiểm tra hư hỏng và hạn sử dụng",
    "createdAt": "2026-10-14 16:40:00",
    "createdBy": {
      "employeeId": "NV000003",
      "fullName": "Lê Văn Kho",
      "username": "inventory01"
    },
    "totalDiscrepancyItems": 1,
    "items": [
      {
        "detailId": "CTKK000001",
        "productId": "SP000001",
        "productName": "Nước ngọt Coca-Cola 320ml",
        "categoryName": "Nước giải khát",
        "systemQuantity": 150,
        "actualQuantity": 145,
        "difference": -5,
        "status": "SHORTAGE",
        "reason": "Hư hỏng bể 5 hộp trong quá trình xếp kệ"
      }
    ]
  }
}
```

---

### MODULE 11: ĐÁNH GIÁ CỬA HÀNG (`/api/feedback`)

#### 11.1. Gửi đánh giá phản hồi (Public cho khách hàng)
- **Endpoint**: `POST /api/feedback`
- **Quyền**: Public
- **Request Body**:
```json
{
  "customerName": "Khách hàng thân thiết",
  "phoneNumber": "0909123456",
  "rating": 5,
  "comment": "Dịch vụ thanh toán rất nhanh, nhân viên thân thiện và nhiệt tình!"
}
```
- **Response `201 CREATED`**:
```json
{
  "status": 201,
  "message": "Cảm ơn bạn đã gửi đánh giá cho cửa hàng!",
  "data": {
    "feedbackId": "DG000001",
    "createdAt": "2026-10-14 16:50:00"
  }
}
```

---

#### 11.2. Xem danh sách đánh giá & Thống kê số sao
- **Endpoint**: `GET /api/feedback`
- **Quyền**: Chỉ dành cho `STORE_OWNER`
- **Query Params**:
  - `page` (default: 1)
  - `limit` (default: 10)
  - `rating` (lọc từ 1 đến 5 sao)
  - `fromDate`, `toDate` (`YYYY-MM-DD`)
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
        "feedbackId": "DG000001",
        "customerName": "Khách hàng thân thiết",
        "phoneNumber": "0909123456",
        "rating": 5,
        "comment": "Dịch vụ thanh toán rất nhanh, nhân viên thân thiện và nhiệt tình!",
        "createdAt": "2026-10-14 16:50:00"
      }
    ]
  }
}
```

---

#### 11.3. Chi tiết đánh giá
- **Endpoint**: `GET /api/feedback/:id`
- **Quyền**: Chỉ dành cho `STORE_OWNER`
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Lấy chi tiết đánh giá thành công",
  "data": {
    "feedbackId": "DG000001",
    "customerName": "Khách hàng thân thiết",
    "phoneNumber": "0909123456",
    "rating": 5,
    "comment": "Dịch vụ thanh toán rất nhanh, nhân viên thân thiện và nhiệt tình!",
    "createdAt": "2026-10-14 16:50:00"
  }
}
```

---

## 4. HƯỚNG DẪN TÍCH HỢP CHO FRONTEND (FE INTEGRATION TEMPLATE)

File cấu hình mẫu Axios hoàn chỉnh kèm Interceptors tự động Refresh Token và xử lý mã lỗi:

```typescript
// src/api/axiosClient.ts
import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';

const BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/QuanLySieuThi/api';

export const axiosClient = axios.create({
  baseURL: BASE_URL,
  withCredentials: true, // RẤT QUAN TRỌNG: Để trình duyệt tự gửi & nhận Cookie refreshToken HttpOnly
  headers: {
    'Content-Type': 'application/json',
  },
});

// 1. Request Interceptor: Tự động đính kèm Access Token vào mọi request
axiosClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('accessToken');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 2. Response Interceptor: Tự động refresh token nếu gặp 401
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: unknown) => void;
  reject: (reason?: unknown) => void;
}> = [];

const processQueue = (error: unknown, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

axiosClient.interceptors.response.use(
  (response) => {
    // Trả về data trực tiếp
    return response.data;
  },
  async (error: AxiosError<{ status: number; message: string }>) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${token}`;
            }
            return axiosClient(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        // Gọi endpoint refresh token (sử dụng refreshToken từ cookie)
        const refreshResponse = await axios.post(
          `${BASE_URL}/auth/refresh`,
          {},
          { withCredentials: true }
        );

        const newAccessToken = refreshResponse.data.data.accessToken;
        localStorage.setItem('accessToken', newAccessToken);

        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        }
        processQueue(null, newAccessToken);
        return axiosClient(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        localStorage.removeItem('accessToken');
        localStorage.removeItem('currentUser');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error.response?.data || error);
  }
);

export default axiosClient;
```
