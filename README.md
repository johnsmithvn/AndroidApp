# AndroidApp
# MyLocalManga Android Client

Ứng dụng Android WebView kết nối đến web reader riêng (chạy trên Tailscale).  
🔒 Bảo mật cao, không public internet, không chứa token, không gửi dữ liệu ra ngoài.

👉 Cần có server riêng (không bao gồm trong repo này).

## Cấu hình hostname
- Hostname server: `mypc.tailxxx.ts.net` (bên Tailscale)
- Sửa trong `MainActivity.java` nếu server đổi tên

## Không chứa:
- IP nội bộ
- Token truy cập
- API secret
