# 💰 Bill Reminder Application

Aplikasi manajemen tagihan berbasis web untuk membantu pengguna mencatat, memantau, dan menerima pengingat tagihan agar tidak terlambat membayar.

---

## 🎥 Demo Video

[![Tonton demo di YouTube](https://img.youtube.com/vi/PB0j0kJ_H7A/hqdefault.jpg)](https://www.youtube.com/watch?v=PB0j0kJ_H7A)

---

## 🧱 Tech Stack

| Layer    | Teknologi                           |
| -------- | ----------------------------------- |
| Frontend | Vue.js 3, Vite, Pinia, Vue Router   |
| Backend  | Spring Boot 3, Spring Security, JWT |
| Database | PostgreSQL                          |
| Build    | Maven (backend), npm (frontend)     |

---

## ✨ Fitur Utama

- 🔐 **Autentikasi** — Register, Login, JWT-based session
- 📋 **Manajemen Tagihan** — Tambah, edit, hapus, filter tagihan
- 💳 **Catat Pembayaran** — Simpan riwayat pembayaran dengan metode & catatan
- 📊 **Dashboard** — Ringkasan total tagihan, sudah/belum dibayar, overdue
- 🔔 **Notifikasi** — In-app notification untuk tagihan mendekati jatuh tempo
- ⏰ **Reminder Otomatis** — Scheduler harian kirim email & notifikasi H-1, H-3, H-7
- 🔁 **Tagihan Berulang** — Auto-generate tagihan berikutnya untuk tagihan recurring

---

## 📁 Struktur Proyek

```
Bill-Reminder-Application/
├── backend/                         # Spring Boot
│   ├── pom.xml
│   └── src/main/java/com/example/billreminder/
│       ├── BillReminderApplication.java
│       ├── config/                  # Security, JWT, Exception Handler
│       ├── controller/              # Auth, Bill, Payment, Dashboard, Notification
│       ├── dto/                     # Request & Response DTOs
│       ├── entity/                  # User, Bill, Payment, Notification
│       ├── repository/              # JPA Repositories
│       └── service/                 # Business Logic & Scheduler
└── frontend/                        # Vue.js
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── assets/                  # Global CSS
        ├── components/              # Navbar, BillCard, StatusBadge
        ├── views/                   # Login, Register, Dashboard, Bills, Payments
        ├── router/                  # Vue Router
        ├── services/                # Axios API services
        ├── stores/                  # Pinia auth store
        └── main.js
```

---

## ⚙️ Prasyarat

Pastikan software berikut sudah terinstall:

| Software     | Versi Minimum | Cek Instalasi    |
| ------------ | ------------- | ---------------- |
| Java JDK     | 17+           | `java -version`  |
| Apache Maven | 3.8+          | `mvn -version`   |
| Node.js      | 18+           | `node -version`  |
| npm          | 9+            | `npm -version`   |
| PostgreSQL   | 14+           | `psql --version` |

---

## 🗄️ Setup Database

### 1. Buat Database PostgreSQL

Masuk ke PostgreSQL dan buat database:

```sql
CREATE DATABASE bill_reminder;
```

> Tabel akan dibuat **otomatis** oleh Hibernate saat aplikasi pertama kali dijalankan (`ddl-auto=update`).

---

## 🚀 Menjalankan Backend (Spring Boot)

### 1. Masuk ke direktori backend

```bash
cd backend
```

### 2. Konfigurasi `application.properties`

Copy file `src/main/resources/application.example.properties`

Tambahkan file `src/main/resources/application.properties`:

```properties
# Sesuaikan dengan konfigurasi PostgreSQL Anda
spring.datasource.url=jdbc:postgresql://localhost:5432/bill_reminder
spring.datasource.username=postgres
spring.datasource.password=your_password

# JWT Secret (ganti dengan string panjang yang aman)
app.jwt.secret=yourSuperSecretKeyThatIsAtLeast32CharactersLong
app.jwt.expiration-ms=86400000

# Email SMTP (untuk fitur reminder via email)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Fonnte WhatsApp API (untuk fitur reminder via Whatsapp)
fonnte.api.url=https://api.fonnte.com/send
fonnte.api.token=YOUR_FONNTE_TOKEN_HERE

```

> **Catatan Gmail:** Gunakan **App Password** bukan password utama akun Gmail.  
> Aktifkan di: Google Account → Security → 2-Step Verification → App Passwords

### 3. Build & jalankan

```bash
# Build project
mvn clean package -DskipTests

# Jalankan aplikasi
mvn spring-boot:run
```

Backend akan berjalan di: **http://localhost:8081**

---

## 🖥️ Menjalankan Frontend (Vue.js)

### 1. Masuk ke direktori frontend

```bash
cd frontend
```

### 2. Install dependencies

```bash
npm install
```

### 3. Jalankan development server

```bash
npm run dev
```

Frontend akan berjalan di: **http://localhost:5173**

> Vite sudah dikonfigurasi untuk **proxy** request `/api` ke `http://localhost:8081`, jadi tidak perlu konfigurasi CORS tambahan saat development.

---

## 📦 Build untuk Production

### Backend

```bash
cd backend
mvn clean package -DskipTests
java -jar target/bill-reminder-0.0.1-SNAPSHOT.jar
```

### Frontend

```bash
cd frontend
npm run build
# Output tersedia di folder: frontend/dist/
```

---

## 🌐 API Endpoints

### Auth

| Method | Endpoint             | Deskripsi            |
| ------ | -------------------- | -------------------- |
| POST   | `/api/auth/register` | Daftar akun baru     |
| POST   | `/api/auth/login`    | Login & dapatkan JWT |
| GET    | `/api/auth/me`       | Info user saat ini   |

### Bills

| Method | Endpoint                    | Deskripsi                    |
| ------ | --------------------------- | ---------------------------- |
| GET    | `/api/bills`                | Daftar semua tagihan         |
| GET    | `/api/bills?status=OVERDUE` | Filter berdasarkan status    |
| GET    | `/api/bills/{id}`           | Detail tagihan               |
| POST   | `/api/bills`                | Buat tagihan baru            |
| PUT    | `/api/bills/{id}`           | Update tagihan               |
| DELETE | `/api/bills/{id}`           | Hapus tagihan                |
| PATCH  | `/api/bills/{id}/mark-paid` | Tandai tagihan sebagai lunas |

### Payments

| Method | Endpoint             | Deskripsi                |
| ------ | -------------------- | ------------------------ |
| GET    | `/api/payments`      | Riwayat semua pembayaran |
| GET    | `/api/payments/{id}` | Detail pembayaran        |
| POST   | `/api/payments`      | Catat pembayaran baru    |

### Dashboard

| Method | Endpoint                        | Deskripsi                     |
| ------ | ------------------------------- | ----------------------------- |
| GET    | `/api/dashboard/summary`        | Ringkasan tagihan bulan ini   |
| GET    | `/api/dashboard/upcoming-bills` | Tagihan mendekati jatuh tempo |

### Notifications

| Method | Endpoint                           | Deskripsi                     |
| ------ | ---------------------------------- | ----------------------------- |
| GET    | `/api/notifications`               | Semua notifikasi              |
| GET    | `/api/notifications/unread`        | Notifikasi belum dibaca       |
| PATCH  | `/api/notifications/mark-all-read` | Tandai semua sudah dibaca     |
| PATCH  | `/api/notifications/{id}/read`     | Tandai satu notifikasi dibaca |

---

## 🔑 Status Tagihan

| Status     | Keterangan                         |
| ---------- | ---------------------------------- |
| `UPCOMING` | Belum mendekati jatuh tempo        |
| `DUE_SOON` | Dalam 7 hari ke depan              |
| `OVERDUE`  | Sudah melewati tanggal jatuh tempo |
| `PAID`     | Sudah dibayar                      |

---

## ⏰ Scheduler Reminder

Scheduler berjalan setiap hari **pukul 08:00** secara otomatis:

1. Mengubah status tagihan yang sudah lewat jatuh tempo menjadi `OVERDUE`
2. Mengirim **in-app notification** untuk tagihan yang mendekati jatuh tempo
3. Mengirim **email reminder** ke alamat email pengguna

---

## 🐛 Troubleshooting

**Backend gagal connect ke database**

```
Pastikan PostgreSQL berjalan dan kredensial di application.properties sudah benar.
```

**Error `Port 8080 already in use`**

```bash
# Linux/Mac
lsof -i :8080
kill -9 <PID>

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

**Error `Port 5173 already in use`**

```bash
npm run dev -- --port 5174
```

**Email reminder tidak terkirim**

```
Pastikan App Password Gmail sudah diaktifkan dan dikonfigurasi di application.properties.
Pastikan juga koneksi internet tersedia di server.
```

---

## 📝 Contoh Request API

### Register

```json
POST /api/auth/register
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "secret123"
}
```

### Buat Tagihan

```json
POST /api/bills
Authorization: Bearer <token>
{
  "title": "Internet Indihome",
  "category": "Internet",
  "amount": 350000,
  "dueDate": "2025-06-01",
  "reminderDaysBefore": 3,
  "isRecurring": true,
  "recurringType": "MONTHLY"
}
```

### Catat Pembayaran

```json
POST /api/payments
Authorization: Bearer <token>
{
  "billId": 1,
  "paidAmount": 350000,
  "paidDate": "2025-05-30",
  "paymentMethod": "Transfer Bank",
  "notes": "Bayar via BCA Mobile"
}
```

---

## 👤 Developer

**Bill Reminder App** — dibuat sebagai proyek manajemen keuangan pribadi.

> Stack: Vue.js 3 · Spring Boot 3 · PostgreSQL · JWT · Maven · Vite
