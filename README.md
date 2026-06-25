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
    ## 1. Tổng Quan Bộ Tech Stack Đề Xuất

      Để hệ thống vận hành mượt mà, cấu trúc **Decoupled Architecture** (Kiến trúc tách rời giữa Backend, Frontend và Real-time Service) là lựa chọn tối ưu nhất.
      
      | Tầng (Layer) | Công Nghệ Đề Xuất | Lý Do Lựa Chọn |
      | :--- | :--- | :--- |
      | **Backend Core** | Java (Spring Boot / Spring MVC) | Độ ổn định cực cao, quản lý transaction (giao dịch thanh toán) chặt chẽ, bảo mật tốt cho phân quyền Role-based. |
      | **Real-time Server** | Node.js (NestJS) + Socket.io | Xử lý bất đồng bộ cực tốt, tối ưu chi phí tài nguyên khi duy trì hàng ngàn kết nối đồng thời (để track vị trí tài xế). |
      | **Database chính** | MySQL hoặc PostgreSQL | Cơ sở dữ liệu quan hệ (RDBMS) bắt buộc phải có cho các nghiệp vụ lưu trữ User, Hóa đơn, Transaction nhằm đảm bảo tính toàn vẹn dữ liệu (ACID). |
      | **Caching & Message** | Redis | Lưu trữ vị trí (GPS) tạm thời của tài xế và làm bộ đệm để giảm tải cho database chính. |
      | **Mobile App (Frontend)** | Flutter hoặc React Native | Viết một lần ra cả 2 app (Khách hàng và Tài xế) trên Android/iOS, hiệu năng mượt mà, hỗ trợ tốt map/GPS. |
      
      ---
      
      ## 2. Phân Tích Sâu Kỹ Thuật Cho Từng Nghiệp Vụ
      
      ### Nghiệp vụ 1: Quản lý người dùng & Phân quyền (RBAC)
      * **Đối tượng:** Hệ thống có 4 đối tượng: Khách thường, Khách VIP, Tài xế, và Admin.
        * **Giải pháp:** Sử dụng **Spring Security + JWT (JSON Web Token)**.
        * **Cách vận hành:** Khi người dùng đăng nhập, hệ thống cấp một chuỗi mã JWT chứa thông tin Role. Mỗi khi API gọi lên (ví dụ: Tài xế cập nhật trạng thái, Admin xem doanh thu), Spring Security sẽ intercept (chặn lại) để kiểm tra quyền. Riêng thuộc tính VIP của khách hàng sẽ là một flag (hoặc trường dữ liệu) trong bảng `users` để phục vụ cho Core tính phí.
      
      ### Nghiệp vụ 2: Engine Tính Phí Giao Hàng (Pricing Engine)
      Yêu cầu tính toán của bạn có nhiều điều kiện lồng nhau (Quãng đường $\rightarrow$ Trọng lượng $\rightarrow$ Hạng VIP $\rightarrow$ Mã giảm giá $\rightarrow$ Check hàng cồng kềnh). Nếu viết bằng `if-else` thuần túy thì code sẽ rất rối và khó bảo trì khi sau này thay đổi giá.
      
      * **Giải pháp Backend:** Triển khai **Strategy Pattern** kết hợp với **Factory Pattern** trong Java Spring Boot để tách biệt các loại phí (`DistancePricing`, `WeightPricing`, `VipDiscount`, `PromoDiscount`).
        * **Mô hình toán học hóa thuật toán tính phí:**
          * Gọi $P_{base}$ là phí quãng đường gốc. Với khoảng cách $d$ (km):
            $$P_{base} = \begin{cases} 40.000 & \text{nếu } d \le 5 \\ 40.000 + (d - 5) \times 5.000 & \text{nếu } d > 5 \end{cases}$$
          * Gọi $W_{sur}$ là phụ phí trọng lượng dựa trên khối lượng $w$ (kg):
            $$W_{sur} = \begin{cases} 0 & \text{nếu } w < 10 \\ P_{base} \times 20\% & \text{nếu } 10 \le w \le 30 \\ 100.000 & \text{nếu } w > 30 \end{cases}$$
          * Áp dụng đặc quyền VIP (Khách VIP thì $P_{base} = 0$):
            $$P_{subtotal} = \begin{cases} W_{sur} & \text{nếu là Khách VIP} \\ P_{base} + W_{sur} & \text{nếu là Khách Thường} \end{cases}$$
          * Áp dụng Mã giảm giá (Giảm tối đa 50.000 VND, không áp dụng nếu hàng cồng kềnh `is_bulky == true`):
            $$P_{final} = \max(0, P_{subtotal} - \text{DiscountAmount})$$
            *(Trong đó $\text{DiscountAmount}$ bằng số tiền giảm của mã nhưng giới hạn tối đa 50.000, và bằng 0 nếu hàng cồng kềnh).*
      
      ### Nghiệp vụ 3: Theo dõi đơn hàng theo thời gian thực (Real-time Tracking)
      Đây là tính năng "ngốn" tài nguyên nhất nếu làm không khéo. Nếu app khách hàng cứ 2 giây lại gửi API HTTP GET để hỏi "Tài xế đang ở đâu?" thì database sẽ sập nguồn ngay lập tức khi có nhiều đơn hàng.
      
      * **Giải pháp:** Tách luồng Real-time ra một Service riêng bằng **Node.js (NestJS) + Socket.io** kết hợp **Redis Geolocation**.
        * **Luồng hoạt động (Workflow):**
          1. App Tài xế bật GPS, cứ mỗi 3-5 giây sẽ đẩy tọa độ $(X, Y)$ lên qua cổng WebSocket của Node.js.
          2. Node.js nhận tọa độ này, **KHÔNG ghi vào MySQL** (để tránh thắt nút cổ chai), mà ghi đè vào Redis bằng tập lệnh `GEOADD` (Redis lưu trên RAM nên tốc độ cực kỳ nhanh).
          3. Node.js đồng thời dùng cơ chế Pub/Sub bắn tọa độ đó qua WebSocket xuống thẳng App của Khách hàng đang có mã đơn hàng tương ứng. Khách hàng thấy tài xế di chuyển mượt mà trên bản đồ (sử dụng Google Maps API hoặc Mapbox).
          4. Chỉ khi nào trạng thái đơn hàng thay đổi lớn (ví dụ: *Đã lấy hàng, Đã hoàn thành*), hệ thống mới gọi về Java Spring Boot để cập nhật trạng thái vào MySQL và chốt hóa đơn.
      
      ---
      
      ## 3. Thiết Kế Cơ Sở Dữ Liệu Gợi Ý (MySQL)
      
      Dưới đây là cấu trúc các bảng cốt lõi (Bỏ qua các trường phụ như `created_at`, `updated_at`...):
      
      ### Bảng `users`
      * `id` (INT, PK, Auto Increment)
        * `phone` (VARCHAR)
        * `password` (VARCHAR)
        * `full_name` (VARCHAR)
        * `role` (ENUM: 'CUSTOMER', 'DRIVER', 'ADMIN')
        * `customer_type` (ENUM: 'NORMAL', 'VIP')
      
      ### Bảng `orders`
      * `id` (INT, PK, Auto Increment)
        * `customer_id` (INT, FK -> users.id)
        * `driver_id` (INT, FK -> users.id, Nullable)
        * `status` (ENUM: 'PENDING', 'PICKING', 'DELIVERING', 'COMPLETED', 'CANCELLED')
        * `distance` (DECIMAL)
        * `weight` (DECIMAL)
        * `is_bulky` (BOOLEAN)
        * `base_price` (DECIMAL)
        * `weight_fee` (DECIMAL)
        * `discount_price` (DECIMAL)
        * `total_price` (DECIMAL)
      
      ### Bảng `promotions`
      * `id` (INT, PK, Auto Increment)
        * `code` (VARCHAR)
        * `discount_value` (DECIMAL)
        * `max_discount` (DECIMAL, Default: 50000)
      
      ---
      
      ## 4. Đánh Giá Ưu Điểm Của Phương Án
      
      * **Tính an toàn tài chính:** Toàn bộ phần tính tiền, áp mã, ví tiền được quản lý bởi Java Spring Boot chặt chẽ, có log đầy đủ, không lo bị can thiệp hay sai lệch số liệu.
        * **Hiệu năng cao:** Tách phần "định vị tài xế" (Real-time) ra Node.js và Redis giúp hệ thống chịu tải tốt. Dù có 10.000 tài xế chạy ngoài đường liên tục gửi tọa độ, hệ thống Backend chính vẫn "bình chân như vại".
          * **Dễ bảo trì:** Logic tính phí được đóng gói riêng. Sau này bạn muốn đổi "5km đầu thành 45.000 VND" hay "Khách VIP chỉ được giảm 50% phí đường", bạn chỉ cần sửa đúng 1 Class Strategy trong code Java mà không ảnh hưởng tới luồng Real-time hay App.




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
