# 🚀 HƯỚNG DẪN CÀI ĐẶT & CHẠY DỰ ÁN MINLISH BACKEND

### 📋 Yêu cầu hệ thống cần chuẩn bị trước:

1. **Node.js** (Phiên bản v18 hoặc mới hơn - khuyến nghị bản LTS).
2. **MySQL** hoặc **MariaDB** đang chạy cục bộ (mặc định cổng `3306`).
3. Công cụ quản lý cơ sở dữ liệu (như TablePlus, DBeaver hoặc Navicat) để dễ kiểm tra (không bắt buộc).

---

### 🛠️ CÁC BƯỚC THỰC HIỆN CHI TIẾT

#### **Bước 1: Clone Repository & Di chuyển vào thư mục backend**

Mở terminal trên máy cá nhân và chạy các lệnh sau:

```bash
git clone [<URL_KHO_LƯU_TRỮ_CỦA_BẠN>](https://github.com/1440isme/MinLish.git)
cd MinLish/backend
git fetch origin
git checkout <ten nhanh>
git pull origin main

```

#### **Bước 2: Cài đặt các thư viện phụ thuộc (Dependencies)**

Cài đặt toàn bộ thư viện cần thiết của NestJS và Prisma ORM:

```bash
npm install
```

#### **Bước 3: Cấu hình Biến môi trường (Environment Variables)**

1. Nhân bản file `.env.example` thành file `.env` riêng tư:
   ```bash
   cp .env.example .env
   ```
2. Mở file `.env` mới tạo lên và cập nhật cấu hình tài khoản/mật khẩu MySQL trên máy cục bộ của lập trình viên đó tại dòng `DATABASE_URL`:
   ```env
   DATABASE_URL="mysql://TÊN_USER:MẬT_KHẨU@localhost:3306/minlish"
   ```
   _Ví dụ:_ `mysql://root:12345678@localhost:3306/minlish`

#### **Bước 4: Tạo Cơ sở dữ liệu trống trên MySQL**

Đăng nhập vào MySQL Server của lập trình viên (qua command line hoặc ứng dụng quản lý GUI) và chạy câu lệnh SQL sau để tạo database trống tên là `minlish`:

```sql
CREATE DATABASE minlish CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### **Bước 5: Chạy Migrations và Nạp dữ liệu mẫu (Seeding)**

Tôi đã viết sẵn một script rút gọn cực kỳ mạnh mẽ trong `package.json` để tự động hóa toàn bộ việc tạo bảng và nạp dữ liệu mẫu chỉ với 1 câu lệnh duy nhất:

```bash
npm run db:reset
```

_Lưu ý: Lệnh này sẽ tạo cấu trúc bảng thông qua Prisma Migrations và nạp sẵn lộ trình TOEIC, IELTS, các level, các bộ từ vựng hệ thống mẫu và các từ vựng mặc định._

#### **Bước 6: Khởi chạy Backend Server**

Khởi động NestJS server ở môi trường phát triển (Development):

```bash
npm run start:dev
```

Sau khi khởi chạy thành công:

- Server sẽ lắng nghe tại cổng **3000** (hoặc cổng được định nghĩa trong file `.env`).
- Trên Terminal sẽ hiển thị dòng log xác nhận kết nối thành công:
  `[PrismaService] ✅ Database connected (minlish @ MySQL)`

---

### 💡 Các lệnh hữu ích khác trong quá trình phát triển:

- 📊 **`npm run db:studio`**: Khởi chạy **Prisma Studio**. Nó sẽ mở ra một giao diện Web cực đẹp tại địa chỉ `http://localhost:5555` giúp các lập trình viên xem, tìm kiếm, chỉnh sửa trực tiếp dữ liệu trong cơ sở dữ liệu MySQL mà không cần dùng đến phần mềm ngoài.
- 🔄 **`npm run db:generate`**: Đồng bộ và tự động tạo lại Prisma Client (cần chạy lệnh này mỗi khi có ai đó thay đổi cấu trúc bảng trong `prisma/schema.prisma`).
- 🧪 **`npx tsc --noEmit`**: Chạy trình kiểm tra cú pháp và kiểu dữ liệu TypeScript (luôn chạy trước khi commit code để đảm bảo dự án không bị lỗi cú pháp).
