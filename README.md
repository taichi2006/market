# Quản Lý Siêu Thị - Backend

Chào mừng các bạn đến với repository Backend của dự án **Quản Lý Siêu Thị**. 
Dự án này cung cấp các API và xử lý logic phía server cho hệ thống quản lý siêu thị.

## 🚀 Công nghệ sử dụng
- **Ngôn ngữ**: Java 17
- **Core**: Jakarta Servlet API 6.0 (chạy trên Tomcat 10+)
- **ORM**: Hibernate Core 6.5
- **Database**: PostgreSQL
- **Caching**: Redis (thông qua Jedis)
- **Connection Pool**: HikariCP
- **Quản lý build & thư viện**: Maven
- **Khác**: Gson (xử lý JSON), Dotenv (quản lý biến môi trường)

## 📋 Yêu cầu môi trường
Để chạy được dự án này trên máy cá nhân, các thành viên cần cài đặt:
- **JDK 17**
- **Apache Maven**
- **Apache Tomcat 10** trở lên
- **PostgreSQL** (Đang chạy service)
- **Redis Server** (Đang chạy service ở port mặc định 6379)
- IDE khuyến nghị: IntelliJ IDEA, Eclipse, hoặc NetBeans.

## ⚙️ Cài đặt và Cấu hình

**Bước 1: Clone dự án**
```bash
git clone <đường-dẫn-repo-của-nhóm>
cd QuanLySieuThi
```

**Bước 2: Chuẩn bị Database**
- Tạo một database mới trong PostgreSQL dành cho dự án này (ví dụ: `quan_ly_sieu_thi`).

**Bước 3: Cấu hình biến môi trường (`.env`)**
Dự án sử dụng thư viện `dotenv-java` để load cấu hình kết nối, giúp bảo mật thông tin.
- Tạo một file tên là `.env` ở **thư mục gốc của dự án** (ngang hàng với `pom.xml`).
- Copy nội dung sau vào file `.env` và thay đổi cho phù hợp với máy của bạn:

```env
DB_URL=jdbc:postgresql://localhost:5432/ten_database_cua_ban
DB_USER=postgres
DB_PASSWORD=mat_khau_database_cua_ban
REDIS_HOST=localhost
REDIS_PORT=6379
```
*(Lưu ý: Không commit file `.env` lên Github để tránh lộ mật khẩu)*

**Bước 4: Tải thư viện và Build**
Chạy lệnh sau tại thư mục chứa `pom.xml` để Maven tải thư viện:
```bash
mvn clean install
```
*(Hoặc các bạn có thể reload Maven project trực tiếp trong IDE)*

## ▶️ Hướng dẫn chạy dự án
1. Mở IDE và thêm cấu hình **Tomcat 10 Server**.
2. Deploy Artifact dạng `war` hoặc `war exploded` của dự án vào Tomcat.
3. Khởi chạy Server. Nếu cấu hình đúng, Hibernate sẽ tự động kết nối DB và Redis test thành công.

## 📂 Cấu trúc dự án cơ bản
- `src/main/java/com/mycompany/quanlysieuthi/`: Nơi chứa toàn bộ mã nguồn Java chia theo mô hình MVC/3-tier (Controllers, Models, Services, Utils...).
- `src/main/resources/`: Chứa các file cấu hình như `hibernate.cfg.xml`.
- `pom.xml`: Quản lý các dependencies của dự án.

---
*Chúc cả team code tốt và hoàn thành dự án thành công! Nếu có lỗi khi setup, hãy báo lên group để mọi người cùng fix nhé.*
