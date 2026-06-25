* Phần 1: 
  - Prompt:
    Bạn là một lập trình viên chuyên nghiệp hãy xem file OrderService của tôi đang bị vi phạm nguyên tắc thiết kế phần mềm.
    Mỗi khi cần thêm mã giảm giá hoặc thêm cổng thông tin mới thì lại phải sửa trực tiếp trên code.Dưới đây là soure code
    OrderService đang bị lỗi
  public class OrderService {

    public Order checkout(Cart cart, User user, String paymentMethod, String voucherCode) {

          if (user.getStatus() != 1) throw new RuntimeException("User locked");
          double total = 0;
          for (Item i : cart.getItems()) {

              total += i.getPrice() * i.getQuantity();
          }
          if (voucherCode != null) {

              if (voucherCode.startsWith("VIP")) total = total * 0.8;

              else if (voucherCode.startsWith("FREESHIP")) total = total - 30000;

          }
          if (paymentMethod.equals("MOMO")) {

              System.out.println("Connecting to Momo API...");

          } else if (paymentMethod.equals("VNPAY")) {

              System.out.println("Connecting to VNPay API...");

          } else {

              throw new RuntimeException("Payment not supported");

          }
          System.out.println("Sending email to " + user.getEmail() + " about order details...");

          return new Order(user, total, "SUCCESS");

    }

}
* Phần 2:
  Code file JwtAuthenticationFilter và mã hiển thị lỗi
  Hãy tìm ra giải pháp tối ưu nhất để bắt các lỗi trên chỉ trả về một JSON đồng nhất.
  * Phần 3:
    Lần 1:
      Prompt:
      Bạn là một chuyên viên phân tích hệ thống giỏi của công ty lớn, hãy đề xuất một bộ Tech Stack Phù hợp với các yêu cầu sau: 
          1.QUản lý người dùng :Hệ thống có 2 role chính: Khách hàng(Thường và Vip), Tài xế, Quản trị viên. 
            2. Nghiệp vụ tính phí giao hàng:
            - Phí qunagx đường : 5km đầu tiên là 40.000 VND. Từ  km thức 6 trở đi cộng thêm 5000 VND/km
            - Phụ phí trọng lượng:
            + Dưới 10kg(Miễn phí).
            + Từ 10kg-30kg (Phụ phí 20% trên phí quãng đường gốc).
            + Trên 30kg(Phụ phí cố định 100.000VND)
            - Đặc quyền VIP: Khách vip được miễn phí 100% miễn phí quãng đường nhưng vẫn phải chịu phụ phí trọng lượng(nếu có).
            - Mã giảm giá: Mã khuyễn mãi giảm tối đa 50.000 VND trên tổng số tiền cuối cùng nhưng không được áp dụng cho hàng cồng kềnh
            3. Theo dõi đơn hàng: Trạng thái đơn hàng cập nhập theo thời gian trên app của khách hàng
      AI đề xuất:
        1. Thành phần công nghệ (Tech Stack)
          - Backend Core (Tính phí & Phân quyền): Java (Spring Boot). Đảm bảo tính toán chính xác dữ liệu tài chính, quản lý phân quyền (Khách/VIP/Tài xế/Admin) chặt chẽ bằng Spring Security + JWT.
    
          - Real-time Service (Theo dõi đơn hàng): Node.js + Socket.io. Xử lý kết nối liên tục, cập nhật trạng thái và vị trí tài xế theo thời gian thực sang app khách hàng mà không làm treo hệ thống.
          
          - Database: MySQL (Lưu thông tin User, đơn hàng, hóa đơn) + Redis (Lưu nhanh vị trí GPS tạm thời của tài xế để giảm tải cho MySQL).
          
          - Frontend/Mobile: Flutter hoặc React Native (Phát triển nhanh đồng thời cho cả app Khách hàng và Tài xế).
        2. Thuật toán tính phí (Core Logic)
          Hệ thống sử dụng Strategy Pattern trong Java để tính tiền theo công thức:
          Phí quãng đường ($P_{base}$):$d \le 5\text{ km} \rightarrow 40.000\text{đ}$$d > 5\text{ km} \rightarrow 40.000\text{đ} + (d - 5) \times 5.000\text{đ}$
          Đặc quyền VIP: Nếu là khách VIP $\rightarrow P_{base} = 0\text{đ}$.
          Phụ phí trọng lượng ($W_{sur}$):$< 10\text{ kg} \rightarrow 0\text{đ}$$10 - 30\text{ kg} \rightarrow P_{base} \times 20\%$$> 30\text{ kg} \rightarrow 100.000\text{đ}$
          Mã giảm giá: Giảm tối đa $50.000\text{đ}$ trên tổng tiền.
          Hệ thống sẽ check biến isBulky (hàng cồng kềnh), nếu true thì hủy áp dụng mã.
        3. Luồng xử lý Real-time (Tracking)
            App Tài xế gửi tọa độ lên Node.js Server qua cổng Websocket $\rightarrow$ Ghi tạm vào Redis $\rightarrow$ Bắn thẳng xuống App Khách hàng theo thời gian thực (hiển thị trên bản đồ).Khi đơn hàng đổi trạng thái (Đã lấy hàng, Đã giao), Node.js mới gọi sang Spring Boot để lưu vào MySQL.
      Lần 2:
          Prompt:
            Phân tích các thực thể cốt lõi vã các thuộc tính quan trọng
          AI đề xuất:
            1. Thực thể User (Người dùng)
            Quản lý thông tin tài khoản và phân quyền hệ thống.
      
            id (Khóa chính)
          
            phone_number / email
          
            password_hash (Mật khẩu mã hóa)
          
            full_name
          
            role (Gồm 3 giá trị: CUSTOMER, DRIVER, ADMIN)
          
            customer_type (Phân loại khách hàng: NORMAL, VIP - Dùng để miễn phí quãng đường)
          
            2. Thực thể Driver_Profile (Thông tin tài xế)
               Tách biệt hoặc mở rộng từ User để quản lý trạng thái hoạt động của tài xế.
          
            driver_id (Khóa ngoại liên kết với User.id)
          
            license_plate (Biển số xe)
          
            is_active (Tài xế có đang bật app nhận đơn hay không)
          
            current_status (AVAILABLE - Đang rảnh, BUSY - Đang chạy đơn)
          
            3. Thực thể Order (Đơn hàng)
               Thực thể trung tâm lưu nghiệp vụ tính phí và vận chuyển.
          
            id (Khóa chính)
          
            customer_id (Khóa ngoại liên kết người đặt)
          
            driver_id (Khóa ngoại liên kết tài xế nhận đơn, có thể null khi đang tìm)
          
            pickup_address & delivery_address
          
            distance (Số km quãng đường - Dùng tính phí gốc)
          
            weight (Trọng lượng hàng hóa - Dùng tính phụ phí)
          
            is_bulky (Boolean: true/false - Đánh dấu hàng cồng kềnh để chặn áp mã giảm giá)
          
            base_price (Phí quãng đường gốc)
          
            weight_fee (Phụ phí trọng lượng)
          
            discount_amount (Số tiền được giảm)
          
            total_price (Tổng tiền cuối cùng khách phải trả)
          
            status (Trạng thái đơn: PENDING - Chờ tài xế, PICKING - Đang đến lấy, DELIVERING - Đang giao, COMPLETED - Thành công, CANCELLED - Đã hủy)
          
            4. Thực thể Promotion (Mã giảm giá)
               Quản lý các chương trình khuyến mãi.
          
            id (Khóa chính)
          
            code (Mã định danh, ví dụ: GIAM50K)
          
            discount_value (Số tiền hoặc % giảm)
          
            max_discount (Giới hạn giảm tối đa = 50.000)
          
            expiry_date (Hạn sử dụng)
          
            5. Thực thể Tracking_History (Lưu vết vị trí)
               Phục vụ lưu lịch sử di chuyển (Dữ liệu thời gian thực lúc chạy sẽ nằm ở Redis, khi hoàn thành sẽ lưu bản ghi tổng hợp vào Database).
          
            id (Khóa chính)
          
            order_id (Khóa ngoại)
          
            latitude & longitude (Tọa độ GPS)
          
            timestamp (Thời gian ghi nhận vị trí)
