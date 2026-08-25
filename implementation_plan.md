# Kế hoạch phát triển hệ thống E-commerce Microservices (Java Spring Boot)

Dự án này nhằm xây dựng một website thương mại điện tử bằng **Java Spring Boot** và kiến trúc **Microservices**, phục vụ cho đồ án tốt nghiệp. Business logic được lấy từ dự án `WebApp` (ASP.NET Core MVC) và pattern triển khai kỹ thuật được tham khảo từ `PlatformService` (.NET Microservices).

## User Review Required

> [!IMPORTANT]
> Đây là bản kế hoạch chi tiết tổng thể. Vì quy mô dự án rất lớn, tôi đề xuất chúng ta sẽ chia thành các giai đoạn nhỏ. Sau khi bạn duyệt bản kế hoạch này, tôi sẽ tiến hành khởi tạo cấu trúc source code ban đầu (Phase 1).

## Open Questions

> [!WARNING]
> 1. Đối với **Service Discovery** và **API Gateway**, bạn muốn sử dụng Spring Cloud Eureka + Spring Cloud Gateway hay muốn triển khai trực tiếp thông qua **Nginx Ingress Controller** của Kubernetes (như cách PlatformService đang làm)?
> 2. Quản lý thông tin đăng nhập: Chúng ta có nên tích hợp Spring Security và JWT (hoặc Keycloak) ngay từ đầu cho **Customer Service** không?

## Phân tích Kiến trúc & Nghiệp vụ

Dựa trên cấu trúc model của `WebApp` (`Product`, `Category`, `Cart`, `Invoice`, `Member`, v.v.), hệ thống sẽ được chia thành các Microservices sau:

1. **Catalog Service (Dịch vụ sản phẩm):**
   - **Models:** Product, Category, SubCategory, Food.
   - **Database:** MongoDB (phù hợp với tính linh hoạt của sản phẩm) hoặc SQL Server.
   - **Nhiệm vụ:** Quản lý danh mục và thông tin sản phẩm.

2. **Customer Service (Dịch vụ khách hàng):**
   - **Models:** Member, Address, Province, District, Ward.
   - **Database:** SQL Server.
   - **Nhiệm vụ:** Quản lý thông tin tài khoản người dùng, địa chỉ giao hàng.

3. **Basket Service (Dịch vụ giỏ hàng):**
   - **Models:** Cart.
   - **Database:** Redis (tối ưu cho giỏ hàng lưu tạm thời).
   - **Nhiệm vụ:** Quản lý giỏ hàng của user trước khi checkout.

4. **Order Service (Dịch vụ đơn hàng):**
   - **Models:** Invoice, InvoiceDetail.
   - **Database:** SQL Server.
   - **Nhiệm vụ:** Xử lý đặt hàng, lưu trữ lịch sử giao dịch.

### Giao tiếp giữa các Services (Communication Patterns)

Tham khảo từ `PlatformService`:
- **Sync Communication (Đồng bộ):** Sử dụng **gRPC** (hoặc OpenFeign) để giao tiếp trực tiếp giữa các service, ví dụ: Order Service gọi Catalog Service để kiểm tra giá và số lượng sản phẩm.
- **Async Communication (Bất đồng bộ):** Sử dụng **RabbitMQ** để publish các events. Ví dụ: Khi Order được tạo thành công, Order Service gửi message lên RabbitMQ, Basket Service nhận event để xóa giỏ hàng tương ứng.

## Đề xuất Workflow & Các bước thực hiện (Giai đoạn nhỏ - Phase 1)

Theo yêu cầu 5.1, chúng ta sẽ bắt đầu ở **quy mô nhỏ** trước khi mở rộng.

### Phase 1: Nền tảng và 2 Core Services đầu tiên

1. **Khởi tạo Workspace cho Java:**
   - Khởi tạo thư mục `f:\JAVA\final_project\source`.
   - Setup Maven multi-module project cho cấu trúc cha hoặc tạo các project Spring Boot độc lập.

2. **Xây dựng Catalog Service:**
   - Init Spring Boot (Web, Data MongoDB/JPA).
   - Cấu hình kết nối Database.
   - Áp dụng các Models cơ bản (`Product`, `Category`).
   - Xây dựng REST API (GET, POST, PUT, DELETE).

3. **Xây dựng Customer Service:**
   - Init Spring Boot (Web, Data JPA, SQL Server).
   - Áp dụng Models (`Member`, `Address`).
   - Xây dựng REST API.

4. **Thiết lập Docker & Kubernetes cơ bản:**
   - Viết `Dockerfile` cho Catalog và Customer services.
   - Tạo các file `*-depl.yaml` và `ingress-srv.yaml` (sử dụng Nginx Ingress) tương tự thư mục `PlatformService`.

### Các Phase mở rộng (Phase 2 & 3) - Sẽ thực hiện sau
- Tích hợp **RabbitMQ** và **gRPC**.
- Xây dựng **Basket Service** (Redis) và **Order Service** (SQL Server).
- Thiết lập CI/CD, Monitoring.

## Verification Plan

### Automated Tests
- Chạy `mvn clean test` cho từng service sau khi khởi tạo để đảm bảo cấu hình Spring Boot hợp lệ.

### Manual Verification
- Dùng Postman để gọi API thông qua Nginx Ingress hoặc cổng mặc định của service.
- Xác nhận dữ liệu được ghi vào Database.

### Phase 4: Frontend Development (Hướng dẫn thực hiện)
Theo yêu cầu, quy trình phát triển giao diện sẽ được thực hiện dưới dạng hướng dẫn (không sinh code tự động).
1. **Dashboard:**
   - Sử dụng template tại `F:\JAVA\final_project\dashboard_template\DashboardSimple\startbootstrap-sb-admin-gh-pages`.
   - Setup project frontend (có thể dùng HTML/JS tĩnh, hoặc framework tùy chọn) và tích hợp template này.
   - Gọi API từ các Backend Services.
2. **Client App (Trang khách hàng):**
   - Sử dụng template tại `F:\JAVA\final_project\client_web_app_template\web`.
   - Setup project và tích hợp template.
   - Tích hợp tính năng hiển thị sản phẩm, giỏ hàng, và đặt hàng.
3. **Quy trình làm việc (Strict Rule):**
   - Agent sẽ hướng dẫn các bước và cấu hình cần thiết.
   - User (Bạn) sẽ tự viết/copy code dựa trên hướng dẫn.
   - Sau khi hoàn thành, Agent sẽ review lại kết quả.

---

## BỔ SUNG: CHI TIẾT KHÔI PHỤC TỪ SOURCE CODE (PHASE 2 - PHASE 6)
Dựa trên phân tích mã nguồn thực tế tại `f:\JAVA\final_project\source`, dự án đã phát triển qua các phase sau (bổ sung chi tiết để không miss bất kỳ nội dung nào, đồng thời giữ nguyên lịch sử kế hoạch cũ ở trên):

### Phase 2: Basket & Order Services
- **Basket Service (`basket-service`):**
  - Tích hợp Redis.
  - Đã xây dựng `BasketController`, `BasketRepository`.
- **Order Service (`order-service`):**
  - Sử dụng SQL Server để lưu thông tin hóa đơn.
  - Tích hợp gRPC client.
  - Có cơ chế publish message lên RabbitMQ khi tạo đơn hàng thành công.
  - Các Entity: `Order`, `OrderDetail`.

### Phase 3: Auth & Payment Services (Mở rộng thêm)
- **Auth Service (`auth-service`):**
  - Thiết lập Spring Security.
  - Cấu hình JWT (`JwtConfig`).
  - Xây dựng `AuthController` để xác thực người dùng.
- **Payment Service (`payment-service`):**
  - Tích hợp cổng thanh toán PayPal (`PayPalConfig`, `PayPalService`).
  - Xây dựng `PaymentController`.
  - Có event publisher (`PaymentEventPublisher`) dùng RabbitMQ.

### Phase 4: Inter-Service Communication (gRPC & RabbitMQ)
- **Protobuf / gRPC:** Thư mục `order-service/src/main/proto` và `protoc-dependencies` cho thấy gRPC đã được compile và tích hợp để giao tiếp đồng bộ.
- **RabbitMQ:** Đã được cấu hình trong `RabbitMQConfig` ở cả Order và Payment Service để trao đổi các event bất đồng bộ một cách ổn định.

### Phase 5: Deployment & Kubernetes (`k8s`)
- Đã có thư mục `k8s` chứa các manifest files.
- Toàn bộ 6 services (Catalog, Customer, Basket, Order, Auth, Payment) đều đã có cấu hình Deployment và Service tương ứng.
- Đã cấu hình Nginx Ingress (`ingress-srv.yaml`) để định tuyến API Gateway.

### Phase 6: Frontend Development (Kế hoạch hiện tại - Sử dụng Thymeleaf)
Phần Frontend sẽ được xây dựng dưới dạng Spring Boot Web Application sử dụng **Thymeleaf**.
- Tạo module mới (ví dụ: `frontend-service`) đóng vai trò là Web UI (Backend for Frontend).
- Ráp template HTML tĩnh (Dashboard & Client App) vào thư mục `resources/templates` và `resources/static`.
- Sử dụng `RestTemplate` hoặc `FeignClient` để gọi API từ các backend microservices thông qua API Gateway/Ingress.
- Tuân thủ quy tắc: Agent chỉ hướng dẫn, User tự code.

### Chi tiết Kế hoạch Thiết kế Frontend (UI Dashboard & Client App)

Dựa trên yêu cầu của bạn, hệ thống Frontend sẽ đóng vai trò như một **BFF (Backend for Frontend)**, xử lý việc render HTML phía server bằng Thymeleaf và giao tiếp với các Backend Microservices thông qua REST APIs.

#### 1. Cấu trúc thư mục dự kiến
- `src/main/resources/static/`: Chứa các tài nguyên tĩnh (CSS, JS, Images).
  - `/static/admin/`: Chứa assets từ `F:\JAVA\final_project\dashboard_template\DashboardSimple\startbootstrap-sb-admin-gh-pages`.
  - `/static/client/`: Chứa assets từ `F:\JAVA\final_project\client_web_app_template\web`.
- `src/main/resources/templates/`: Chứa các file HTML đã được gắn thẻ Thymeleaf.
  - `/templates/admin/`: Các view của Dashboard. Dùng Thymeleaf Layout Dialect để chia sẻ sidebar/header.
  - `/templates/client/`: Các view của Client App. Dùng Layout Dialect để chia sẻ navbar/footer.

#### 2. Thiết kế giao diện Dashboard (Quản trị viên)
- **Phương pháp Template (Layout Fragment):** Sẽ hướng dẫn bạn cắt file template HTML tĩnh thành các component có thể tái sử dụng.
  - Tách `Header` (Topbar) và `Sidebar` (Menu trái) thành các fragments riêng biệt (`_header.html`, `_sidebar.html`).
  - Xây dựng file `_layout.html` làm khung master chứa các fragments trên.
- **Các trang chức năng (View):**
  - **Trang chủ Dashboard (`index.html`):** Thống kê tổng quan đơn hàng, doanh thu (Gọi API từ Order Service).
  - **Quản lý Sản phẩm (`products.html`):** Hiển thị bảng danh sách sản phẩm, tích hợp phân trang.
  - **Form Sản phẩm (`product-form.html`):** Giao diện Thêm/Sửa sản phẩm (Gọi API Catalog Service).
  - **Quản lý Đơn hàng (`orders.html`):** Xem danh sách đơn hàng và trạng thái (Gọi API Order Service).

#### 3. Thiết kế giao diện Client App (Khách hàng)
- **Phương pháp Template (Layout Fragment):** Cắt `Navbar` (Menu chính, icon giỏ hàng) và `Footer` thành các fragments dùng chung.
- **Các trang chức năng (View):**
  - **Trang chủ (`index.html`):** Hiển thị danh sách sản phẩm nổi bật, mới nhất (Gọi API Catalog Service).
  - **Chi tiết sản phẩm (`product-details.html`):** Hiển thị thông tin mô tả chi tiết, giá tiền và có nút "Thêm vào giỏ hàng".
  - **Giỏ hàng (`cart.html`):** Hiển thị danh sách sản phẩm đang có trong giỏ, cho phép tăng/giảm số lượng (Gọi API Basket Service qua Redis).
  - **Thanh toán (`checkout.html`):** Giao diện nhập địa chỉ giao hàng và xác nhận mua hàng (Tương tác với Customer Service và Order Service).
  - **Lịch sử mua hàng (`orders.html`):** Giao diện quản lý cá nhân, xem lại lịch sử đặt hàng.

---

## Danh sách Task (Task List)

Đây là hồ sơ lịch sử các task thực hiện cho đồ án. Các task mới sẽ được append thêm, giữ nguyên lịch sử cũ.

- `[x]` **Phase 1: Nền tảng và 2 Core Services đầu tiên**
  - `[x]` Khởi tạo Workspace cho Java (`f:\JAVA\final_project\source`).
  - `[x]` Xây dựng Catalog Service.
  - `[x]` Xây dựng Customer Service.

- `[x]` **Phase 2: Basket & Order Services**
  - `[x]` Khởi tạo Basket Service, cấu hình Redis.
  - `[x]` Xây dựng BasketController, BasketRepository.
  - `[x]` Khởi tạo Order Service, cấu hình SQL Server.
  - `[x]` Cấu hình Entity Order, OrderDetail.

- `[x]` **Phase 3: Auth & Payment Services**
  - `[x]` Khởi tạo Auth Service, cấu hình Spring Security.
  - `[x]` Xây dựng JwtConfig, AuthController.
  - `[x]` Khởi tạo Payment Service.
  - `[x]` Cấu hình tích hợp PayPal (PayPalConfig, PayPalService).

- `[x]` **Phase 4: Inter-Service Communication**
  - `[x]` Compile file `.proto` (protobuf), setup gRPC client trong Order Service.
  - `[x]` Cấu hình RabbitMQConfig trong Order và Payment Service.
  - `[x]` Tạo Event Publisher publish event tạo đơn hàng / thanh toán.

- `[x]` **Phase 5: Deployment & Kubernetes**
  - `[x]` Khởi tạo thư mục `k8s`.
  - `[x]` Viết Manifest Deployments và Services cho toàn bộ 6 Backend Services.
  - `[x]` Cấu hình Ingress Nginx (`ingress-srv.yaml`).
  - `[x]` Cập nhật `mssql-depl.yaml` thêm PersistentVolumeClaim (PVC) để lưu trữ data vĩnh viễn.

- `[/]` **Phase 6: Frontend Development (Sử dụng Thymeleaf)**
  - `[x]` Khởi tạo Spring Boot module mới (`frontend-service`) tích hợp Thymeleaf & Spring Web.
  - `[x]` Cập nhật `pom.xml` cha để thêm `frontend-service`.
  - `[x]` Import template Dashboard vào `templates/admin` và cấu hình file static.
  - `[x]` Import template Client App vào `templates/client` và cấu hình file static.
  - `[x]` Áp dụng Thymeleaf Layout Dialect cắt fragments (Admin Dashboard).
  - `[x]` Áp dụng Thymeleaf Layout Dialect cắt fragments (Client App).
  - `[x]` Xây dựng Controllers và sử dụng RestTemplate gọi API backend.
  - `[x]` Bổ sung: Khởi tạo ProductController trong catalog-service để mở API /api/products.
  - `[x]` Tích hợp dữ liệu sản phẩm (Client App) lên giao diện Thymeleaf.
  - `[x]` Tích hợp giỏ hàng (Basket Service) qua REST API (Frontend to Backend).
    - `[x]` Khởi tạo BasketApiService trong frontend-service.
    - `[x]` Tạo Controller xử lý nhận dữ liệu Giỏ hàng.
    - `[x]` Bắt sự kiện Checkout bằng JS để gửi dữ liệu lên BFF.
    - `[x]` Đưa Frontend Service lên Kubernetes và xử lý biến môi trường thành công.
  - `[/]` Tích hợp trang Admin (Dashboard).
    - `[x]` Tạo Controller điều hướng cho trang Admin.
    - `[x]` Render view cho các trang Quản lý (Dashboard, Products, Categories...).
    - `[x]` Xây dựng tính năng Cập nhật (Sửa) Danh mục (Category).
      - `[x]` Cập nhật API Service để lấy chi tiết và cập nhật Category (GET / PUT).
      - `[x]` Tạo Controller ánh xạ giao diện Form chỉnh sửa.
      - `[x]` Render view form sửa danh mục (Sử dụng Thymeleaf form binding).
    - `[x]` Xây dựng tính năng Cập nhật (Sửa) Sản phẩm (Product).
      - `[x]` Cập nhật ProductController (Backend) thêm API GET/{id} và PUT/{id}.
      - `[x]` Cập nhật CatalogApiService và AdminController (Frontend).
      - `[x]` Tạo giao diện product-form.html (Upsert Form).
    - `[x]` Xây dựng tính năng Xóa (Delete) Danh mục và Sản phẩm.
      - `[x]` Cập nhật Backend (CategoryController & ProductController) thêm API DELETE.
      - `[x]` Cập nhật Frontend (CatalogApiService) để gọi API xóa.
      - `[x]` Cập nhật AdminController xử lý logic Xóa và redirect.
      - `[x]` Gắn link Xóa vào nút bấm trên file categories.html và products.html.
    - `[x]` Xây dựng tính năng Hiển thị danh sách Đơn hàng (Orders) — hoàn thành 15/08/2026, sau khi Phase 7 (PayPal) chạy được, dùng để xem lại các đơn hàng đã tạo.
      - `[x]` Thêm API `GET /api/orders` ở `order-service` (`InvoiceRepository.findAllByOrderByOrderDateDesc()` + `OrderController`) trả về toàn bộ hóa đơn, mới nhất trước.
      - `[x]` Tạo `OrderDTO`/`OrderDetailDTO` và `OrderApiService.getAllOrders()` trong frontend-service.
      - `[x]` Thêm route `/admin/orders` vào `AdminController`, render `admin/orders.html` (bảng: mã đơn, khách hàng, ngày đặt, số sản phẩm, tổng tiền, trạng thái có badge màu theo PAID/PENDING/CANCELLED).
      - `[x]` Thêm link "Orders" vào sidebar admin (`fragments/_sidebar.html`), cạnh Category/Product.
      - Chỉ dừng ở mức **hiển thị danh sách** (chưa có xem chi tiết đơn hàng, chưa có cập nhật trạng thái thủ công từ Admin) — có thể mở rộng sau nếu cần.
  - `[/]` Tích hợp tính năng Đăng nhập, Đăng ký, Đăng xuất (Auth Feature).
    - `[x]` Cập nhật `AuthApiService` trong frontend-service để gọi API từ auth-service.
    - `[x]` Tạo `AuthController` trong frontend-service xử lý các request đăng nhập, đăng ký từ giao diện.
    - `[x]` Cập nhật giao diện `login.html`, `register.html` với form action tương ứng.
    - `[x]` Fix lỗi 500 tại `CatalogApiService.getAllProducts()` (thêm try-catch RestClientException).
    - `[x]` Fix lỗi DB connection `auth-service` local: chuyển datasource.url sang `localhost:1433`, cập nhật `auth-depl.yaml` thêm env override cho K8s.
    - `[x]` Bổ sung session/cookie management để lưu trữ JWT Token sau khi đăng nhập thành công. *(Cập nhật 16/08/2026: phát hiện lúc khảo sát Phase 9 — phần này thực ra đã được làm từ trước, `AuthController` đã lưu `jwtToken`/`username` vào `HttpSession`, chỉ là checkbox chưa cập nhật.)*
    - `[x]` Cập nhật giao diện header/navbar hiển thị trạng thái đăng nhập và nút Đăng xuất. *(Cập nhật 16/08/2026: tương tự — `_header.html` đã có sẵn `th:if="${session.username == null}"` / nút Logout.)*
  - `[x]` Agent sẽ cung cấp hướng dẫn (TUYỆT ĐỐI KHÔNG CODE thay).
  - `[x]` Agent sẽ review code sau khi user hoàn tất.

---

## Phase 7: Checkout Flow với PayPal Payment (Kế hoạch mới)

### Tổng quan luồng Checkout
Luồng thanh toán sẽ theo flow chuẩn sau:
1. User xem giỏ hàng (`cart.html`) → Nhấn **"Proceed to Checkout"**
2. Frontend hiển thị trang `checkout.html` (nhập địa chỉ giao hàng)
3. User nhấn **"Pay with PayPal"** → Redirect sang PayPal để thanh toán
4. PayPal redirect về `payment-service` với kết quả (success/cancel)
5. Nếu thành công → `payment-service` tạo Order qua `order-service` → Clear giỏ hàng
6. Frontend hiển thị trang `order-success.html`

### Kiến trúc kỹ thuật
- **Frontend Service**: Hiển thị trang checkout, thu thập thông tin địa chỉ giao hàng, gọi API thanh toán.
- **Payment Service** (port 8084): Nhận yêu cầu, tạo PayPal payment, xử lý callback từ PayPal.
- **Order Service** (port 8083): Nhận event từ Payment Service qua RabbitMQ để tạo hóa đơn.
- **Basket Service** (port 8082): Nhận event để xóa giỏ hàng sau khi đặt hàng thành công.

### Các task cụ thể

- `[x]` **Phase 7: Checkout Flow với PayPal Payment** — Hoàn thành 15/08/2026, test end-to-end thành công qua PayPal Sandbox thật (redirect → đăng nhập → thanh toán → capture → cập nhật Invoice → clear giỏ hàng → trang thành công).
  - `[x]` **7.1 - Thiết kế trang Checkout (Frontend)**
    - `[/]` Tạo `checkout.html` — **KHÔNG có form nhập địa chỉ giao hàng** (quyết định phạm vi: `Invoice` entity chưa có field lưu địa chỉ, để tránh giao diện giả không lưu được dữ liệu; chỉ hiển thị tóm tắt đơn hàng + nút thanh toán).
    - `[x]` Hiển thị tóm tắt đơn hàng (danh sách sản phẩm, tổng tiền) từ giỏ hàng.
    - `[x]` Tạo nút "Pay with PayPal" — dùng `<form method="post">` submit thường (không cần AJAX vì `CheckoutController` trả `redirect:` trực tiếp).
    - `[x]` Tạo `CheckoutController` trong frontend-service (`/checkout`, `/checkout/pay`, `/checkout/success`, `/checkout/cancel`).
  - `[x]` **7.2 - Kết nối Frontend → Payment Service**
    - `[x]` Tạo `PaymentApiService` và `OrderApiService` trong frontend-service.
    - `[x]` Endpoint thực tế: `POST /api/payments/orders` (khác tên so với kế hoạch gốc `/api/payment/create`) — nhận `amount` (VNĐ) + `invoiceId`, trả về `paypalOrderId`.
    - `[x]` Frontend redirect user sang PayPal approval URL (`https://www.sandbox.paypal.com/checkoutnow?token=...`).
    - **Bổ sung quan trọng (phát sinh khi debug):** phải gọi `order-service` (`POST /api/orders/checkout`) để tạo `Invoice` PENDING **trước** khi tạo PayPal order, lấy UUID thật làm `invoiceId` — nếu không, event thanh toán thành công không tìm được hóa đơn để cập nhật.
  - `[x]` **7.3 - Xử lý PayPal Callback**
    - `[x]` `PaymentController` (`payment-service`) có sẵn đủ endpoint: `POST /api/payments/orders`, `/orders/{id}/capture`, `/orders/{id}/cancel`, `/{id}/refund`. Việc nhận redirect từ PayPal (`token`) được xử lý ở **`CheckoutController` bên frontend-service** (`/checkout/success`, `/checkout/cancel`), không phải trực tiếp ở payment-service như kế hoạch gốc.
    - `[x]` Verify cấu hình PayPal sandbox — đã đổi sang tài khoản Sandbox Business tự tạo mới (`sb-dkp0g52485743@business.example.com`) + app `BigStore-v3` do tài khoản mặc định ban đầu dính lỗi `TRANSACTION_LIMIT_EXCEEDED` khi test qua SDK.
    - `[x]` **Viết lại `PayPalService` bỏ SDK `checkout-sdk:2.0.0`** (build request lỗi không rõ nguyên nhân), chuyển sang gọi thẳng REST API bằng `RestTemplate` (giống `refundPayment()` cũ).
    - `[x]` Thêm `application_context` (`return_url`, `cancel_url`, `user_action=PAY_NOW`) khi tạo order — thiếu phần này khiến màn hình PayPal bị kẹt ở bước "Continue to Review Order".
    - `[x]` Publish event thực tế tên `payment.success` (không phải `OrderCreatedEvent` như kế hoạch gốc) qua RabbitMQ.
    - `[x]` Redirect về frontend với kết quả (success/cancel page).
  - `[x]` **7.4 - Order Service nhận Event cập nhật đơn hàng**
    - `[x]` Consumer `PaymentEventListener` trong `order-service` nhận event `payment.success`, **cập nhật** `Invoice.status` từ `PENDING` → `PAID` (không phải "tạo mới" Order như kế hoạch gốc — Order/Invoice đã được tạo trước đó ở bước 7.2 khi user bấm "Pay with PayPal", trạng thái PENDING).
    - `[x]` **Sửa lỗi bảo mật RabbitMQ:** đổi message converter từ Java serialization mặc định sang `Jackson2JsonMessageConverter` ở cả `payment-service` (producer) và `order-service` (consumer) — Java serialization mặc định bị Spring AMQP chặn (`SecurityException: unauthorized class java.util.HashMap`), từng gây vòng lặp retry vô hạn (log phình tới 281MB) phải xử lý khẩn cấp.
  - `[x]` **7.5 - Trang xác nhận đặt hàng (Frontend)**
    - `[x]` Tạo `order-success.html` hiển thị thông báo đặt hàng thành công.
    - `[x]` Tạo `order-cancel.html` hiển thị thông báo hủy thanh toán.
  - `[ ]` **7.6 - Cấu hình K8s cho Payment Service** (CHƯA LÀM — mới test ở local, chưa đụng tới `payment-depl.yaml`/`order-depl.yaml`)
    - `[ ]` Thêm env override cho `payment-depl.yaml` theo đúng pattern local/K8s song song (`auth-depl.yaml`): `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_RABBITMQ_HOST`.
    - `[ ]` Thêm biến môi trường mới phát sinh: `EXCHANGE_RATE_VND-USD` (nếu muốn khác mặc định 25000), `FRONTEND_BASE_URL` (return_url/cancel_url — hiện mặc định `http://localhost:8888`, cần trỏ đúng domain K8s khi deploy).
    - `[ ]` Cân nhắc đưa `paypal.client-id`/`paypal.client-secret` vào K8s Secret thay vì hard-code trong `application.properties`.
    - `[ ]` Đảm bảo Ingress route `/api/payments/**` đến payment-service (đã có sẵn trong `ingress-srv.yaml`, cần verify lại).

### Nợ kỹ thuật phát sinh trong Phase 7 (cần quay lại xử lý)
- `[ ]` `SecurityConfig` của `order-service` đang **mở public tạm thời** `/api/orders/**` (`.permitAll()` thay vì `.authenticated()`) vì `frontend-service` chưa có cơ chế lưu/gắn JWT sau khi đăng nhập — liên quan trực tiếp tới task còn dang dở ở Phase 6 ("Bổ sung session/cookie management để lưu trữ JWT Token"). Cần làm JWT session trước khi siết lại bảo mật này. *(Cập nhật 16/08/2026: Phase 9 xác nhận session đã lưu JWT sẵn từ trước, nhưng `OrderApiService`/`PaymentApiService` phía frontend vẫn CHƯA đính kèm header `Authorization: Bearer ...` khi gọi sang backend — vẫn còn thiếu bước cuối để siết lại `.authenticated()`.)*
- `[ ]` Tỷ giá VNĐ→USD đang **cố định cứng** (25.000đ = 1 USD) trong `payment-service`, phù hợp demo đồ án nhưng không phản ánh tỷ giá thực tế — nếu cần chính xác hơn phải tích hợp API tỷ giá.
- `[ ]` Đã xóa 24 modal demo (`_layout.html`) và 16 nút "Special Offers" demo (`index.html`) còn sót từ template W3layouts gốc — icon "Quick View" trên lưới sản phẩm hiện không còn mở được gì (trỏ tới modal đã xóa), có thể làm lại sau nếu cần tính năng xem nhanh sản phẩm.

---

## Phase 9: Quản lý tài khoản người dùng (Hoàn thành 16/08/2026)

### Bối cảnh
Trước Phase 9, `auth-service` (bảng `User`: đăng nhập/JWT) và `customer-service` (bảng `Member`/`Address`: hồ sơ, địa chỉ) là hai hệ thống hoàn toàn tách biệt — đăng ký chỉ tạo `User`, không hề tạo `Member` tương ứng. Link "Order History" trên header cũng trỏ tĩnh tới `about.html`. Phạm vi Phase 9 được chốt qua 2 hướng: **Trang "Tài khoản của tôi" (Client)** và **Trang Admin quản lý tài khoản** (không làm bảo mật BCrypt/JWT propagation trong phạm vi này — xem mục nợ kỹ thuật Phase 7 ở trên).

### Các task cụ thể

- `[x]` **Phase 9: Quản lý tài khoản người dùng**
  - `[x]` **9.1 - Liên kết User (auth-service) ↔ Member (customer-service)**
    - `[x]` Thêm `GET /api/members/username/{username}` vào `MemberController` (customer-service) — dùng `MemberRepository.findByUsername()` có sẵn.
    - `[x]` Tạo `MemberDTO`, `CustomerApiService` (frontend-service).
    - `[x]` Sửa `AuthController.processRegister()`: sau khi đăng ký thành công bên `auth-service`, gọi thêm `customerApiService.createMember()` tạo `Member` cùng `username`.
    - `[x]` Fix bug `IllegalArgumentException` do thiếu tên tường minh ở `@PathVariable String username` (Spring cần compile `-parameters` nếu không ghi rõ tên) — sửa thành `@PathVariable("username") String username`, đúng convention các controller khác trong project.
    - `[x]` Fix `customer-service` chưa chạy được local (cùng lỗi hostname K8s `mssql-clusterip-srv` như các service trước) — chuyển sang `localhost:1433` / mật khẩu `123456`.
  - `[x]` **9.2 - Trang "Tài khoản của tôi" (Client, `/account`)**
    - `[x]` **Sửa bug quan trọng phát hiện lúc làm bước này:** `CartController.checkout()` từng gán `memberId` bằng chuỗi cứng `"user123"` lấy từ JS cũ (`_layout.html`), khiến `Invoice.memberId` không phản ánh đúng người dùng thật. Sửa lại: nếu đã đăng nhập (`session.username` khác null) thì dùng username thật làm `memberId`, bỏ qua giá trị JS gửi lên.
    - `[x]` Thêm `InvoiceRepository.findAllByMemberIdOrderByOrderDateDesc()` + `GET /api/orders/member/{memberId}` (order-service).
    - `[x]` Thêm `OrderApiService.getOrdersByMember()` (frontend-service).
    - `[x]` Tạo `AccountController` (route bảo vệ, tự redirect `/login` nếu chưa đăng nhập) + giao diện `client/account.html` (hồ sơ + lịch sử đơn hàng riêng của user).
    - `[x]` Sửa link "Order History" trên `_header.html` từ `about.html` (chết) sang `/account`.
  - `[x]` **9.3 - Đổi mật khẩu**
    - `[x]` Thêm `ChangePasswordRequest` DTO + `POST /api/auth/change-password` (auth-service) — kiểm tra mật khẩu cũ trước khi cho đổi.
    - `[x]` Thêm `AuthApiService.changePassword()` (frontend-service).
    - `[x]` Thêm form đổi mật khẩu vào `account.html`, xử lý ở `AccountController` (dùng `RedirectAttributes` flash message báo thành công/thất bại).
  - `[x]` **9.4 - Sổ địa chỉ giao hàng**
    - `[x]` Tạo `AddressController` (customer-service) — `GET /api/addresses/member/{memberId}`, `POST /api/addresses`, `DELETE /api/addresses/{id}` (dùng `AddressRepository.findByMemberId()` có sẵn).
    - `[x]` Tạo `AddressDTO`, bổ sung `CustomerApiService` (get/add/delete address).
    - `[x]` Thêm khối "Sổ địa chỉ" vào `account.html` (danh sách + form thêm + nút xóa có confirm).
    - **Đơn giản hóa có chủ đích:** dự án chưa có bảng dữ liệu Tỉnh/Huyện/Xã thật, nên form địa chỉ chỉ còn 2 trường **địa chỉ chi tiết (text tự do)** + **số điện thoại**, bỏ qua phần chọn cascading Tỉnh/Huyện/Xã.
  - `[x]` **9.5 - Trang Admin quản lý tài khoản**
    - `[x]` Thêm `CustomerApiService.getAllMembers()` (dùng lại `GET /api/members` có sẵn, không cần sửa backend).
    - `[x]` Thêm route `/admin/members` vào `AdminController`, render `admin/members.html` (bảng Username/Email, có search/phân trang tự động qua DataTables giống các trang admin khác).
    - `[x]` Thêm link "Members" vào sidebar admin, cạnh Orders.
- `[x]` Agent hướng dẫn từng bước, User tự dán code, Agent review + test end-to-end (curl + trình duyệt thật) sau mỗi bước.

### Nợ kỹ thuật phát sinh trong Phase 9 (cần quay lại xử lý)
- `[ ]` `Member.password` (customer-service) chỉ được set 1 lần lúc tạo tài khoản — khi đổi mật khẩu qua `/account` (mục 9.3), chỉ `User.password` (auth-service) được cập nhật, `Member.password` không đồng bộ theo. Không ảnh hưởng chức năng (không có API nào dùng `Member.password` để xác thực), nhưng là dữ liệu trùng lặp không nhất quán — nên cân nhắc bỏ hẳn field `password` khỏi `Member` sau này, vì `auth-service.User` mới là nguồn xác thực thật.
- `[ ]` Trang Admin quản lý tài khoản (9.5) mới dừng ở mức **xem danh sách** — chưa có khóa/mở tài khoản, chưa có xem chi tiết đơn hàng/địa chỉ của từng member từ phía Admin.
- `[ ]` Sổ địa chỉ (9.4) chưa có tính năng "đặt làm mặc định" (`isDefault`) dù entity đã có sẵn field, và trang Checkout hiện chưa dùng địa chỉ đã lưu (vẫn không thu thập địa chỉ giao hàng, theo quyết định phạm vi ở Phase 7).

---

## Sửa lỗi phát sinh sau Phase 9: Trang chi tiết sản phẩm 404 (16/08/2026)

### Vấn đề
User báo `http://localhost:8888/single.html` bị lỗi 404 (Whitelabel Error Page). Nguyên nhân: `single.html` là trang chi tiết sản phẩm từ template W3layouts gốc, **chưa từng được chuyển đổi sang Thymeleaf** — không có `layout:decorate`, không có route nào trong `ClientController` phục vụ nó. Trên trang chủ, tên/ảnh sản phẩm trỏ cứng tới chuỗi `"single.html"` (không kèm ID) hoặc tới modal Quick View đã bị xóa (`#myModal17`).

Khảo sát thêm phát hiện: file `single.html` gốc (1137 dòng) cũng chứa **15+ nút "Add to Cart" giả** trong khu vực "Special Offers" (Wheat, Lays, Kurkure...) và 12 modal Quick View giả — cùng loại lỗi trùng ID sản phẩm thật đã sửa ở Phase 7 (`_layout.html`, `index.html`). Đã hỏi ý kiến user: chọn giữ nguyên bố cục gốc (thay vì làm trang đơn giản mới) → phải chuyển đổi toàn bộ, không chỉ xóa.

### Các task cụ thể
- `[x]` Thêm field `description` còn thiếu vào `ProductDTO` (frontend-service) — khớp với field `description` đã có sẵn ở `catalog-service`.
- `[x]` Thêm route `GET /product/{id}` vào `ClientController`, lấy sản phẩm qua `catalogApiService.getProductById()`, kèm danh sách "sản phẩm khác" (loại trừ chính nó).
- `[x]` Viết lại toàn bộ `single.html`: thêm `layout:decorate="~{client/_layout}"`, giữ nguyên bố cục 2 cột (ảnh + thông tin) và khối gợi ý sản phẩm bên dưới, nhưng nối dữ liệu thật qua `${product}`/`${relatedProducts}` thay vì demo tĩnh. Xóa hoàn toàn phần header/footer/script trùng lặp (đã có ở `_layout.html`) và toàn bộ 15+ nút Add to Cart giả + 12 modal Quick View giả.
- `[x]` Sửa 2 link trong `index.html` (ảnh sản phẩm trỏ modal đã xóa, tên sản phẩm trỏ `single.html` tĩnh) → cả 2 trỏ đúng `/product/{id}`.
- `[x]` Test end-to-end trên trình duyệt thật: trang hiển thị đúng tên/giá/mô tả/ảnh thật, nút Add to Cart mang đúng `data-id` thật, danh sách sản phẩm khác toàn dữ liệu thật, link từ trang chủ điều hướng đúng.

---

## Đăng nhập / Đăng ký / Đăng xuất cho Admin Dashboard (Hoàn thành 16/08/2026)

### Vấn đề trước khi làm
- `/admin/**` (Dashboard, Products, Categories, Orders, Members...) đang **mở hoàn toàn công khai** — không có bất kỳ lớp bảo vệ nào, ai cũng truy cập được mà không cần đăng nhập.
- `auth-service` chỉ trả về `token` khi đăng nhập, **không trả `role`** — phía frontend không có cách nào biết user vừa đăng nhập có phải Admin hay không.
- Không có cách nào để tạo tài khoản `ROLE_ADMIN` — toàn bộ luồng đăng ký hiện có (Client) luôn gán cứng `ROLE_USER`.
- `admin/login.html`, `admin/register.html` đã tồn tại sẵn (kèm form Thymeleaf dở dang) nhưng trỏ nhầm sang route của Client (`/login`, `/register`) thay vì route riêng cho Admin.

### Các task cụ thể
- `[x]` **Nền tảng — trả `role` kèm token khi đăng nhập**
  - `[x]` Thêm field `role` vào `AuthResponse` (cả `auth-service` và `frontend-service`).
  - `[x]` `auth-service/AuthController.login()` trả `new AuthResponse(token, user.getRole())`.
  - `[x]` `AuthApiService.login()` đổi kiểu trả về từ `String` (chỉ token) sang `AuthResponse` (token + role) — dùng chung cho cả luồng Client và Admin.
  - `[x]` `AuthController` (Client) lưu thêm `session.setAttribute("role", ...)` sau khi đăng nhập.
- `[x]` **`AdminAuthController`** (route riêng `/admin/login`, `/admin/register`, `/admin/logout`, tách biệt khỏi `AuthController` của Client).
  - `[x]` Đăng ký Admin **luôn gán cứng `ROLE_ADMIN` ở phía server** (`userDto.setRole("ROLE_ADMIN")`), không nhận role từ form gửi lên — chống việc user thường tự sửa form để tự phong quyền Admin.
  - `[x]` Đăng nhập kiểm tra thêm điều kiện `role == ROLE_ADMIN`, sai role thì từ chối dù đúng mật khẩu.
- `[x]` **`AdminAuthInterceptor`** (`HandlerInterceptor`, đăng ký qua `AppConfig implements WebMvcConfigurer`) — chặn toàn bộ `/admin/**`, redirect về `/admin/login` nếu session chưa có `role == ROLE_ADMIN`; loại trừ chính `/admin/login` và `/admin/register`. `frontend-service` không có Spring Security nên dùng Interceptor thủ công thay vì `SecurityFilterChain`, giữ đúng phong cách đơn giản của project (giống cách `/account` đã tự kiểm tra session).
  - `[x]` Sửa `admin/login.html`, `admin/register.html`: trỏ đúng `/admin/login` / `/admin/register` (trước đó trỏ nhầm sang route Client), sửa luôn đường dẫn CSS/JS tĩnh cho khớp `admin/_layout.html`.
  - `[x]` Sửa `admin/fragments/_header.html`: dropdown user hiển thị đúng `session.username`, link "Logout" trỏ `/admin/logout` (trước đó là link tĩnh `login.html`).
- `[x]` Test end-to-end trên trình duyệt thật: chặn truy cập khi chưa đăng nhập → đăng ký Admin mới → xác nhận `role: ROLE_ADMIN` qua API → đăng nhập thành công vào Dashboard → header hiện đúng username → đăng xuất → xác nhận các route con khác (`/admin/orders`) cũng bị chặn lại sau khi logout.

### Nợ kỹ thuật phát sinh
- `[ ]` `/admin/register` hiện **không giới hạn ai được tạo tài khoản Admin** (self-serve, giống bootstrap tài khoản đầu tiên) — phù hợp quy mô đồ án demo, nhưng trong thực tế nên giới hạn: chỉ Admin có sẵn mới được tạo thêm Admin khác, hoặc ẩn/xóa route này sau khi đã có tài khoản Admin đầu tiên.
- `[ ]` Interceptor mới bảo vệ được đường `/admin/**` ở tầng `frontend-service` — **các API backend gốc** (`order-service`, `catalog-service`... khi gọi trực tiếp qua Ingress, không qua `frontend-service`) vẫn chưa được bảo vệ bằng JWT thật (liên quan nợ kỹ thuật đã ghi ở Phase 7: JWT có trong session nhưng chưa được đính vào header `Authorization` khi gọi API).

---

## Cập nhật tính năng Category (2 cấp danh mục) — Kế hoạch mới (16/08/2026)

### Tài liệu tham khảo
- `F:\JAVA\final_project\WebAPI401\Category.sql` — script SQL Server gốc, bảng `[dbo].[Category]` của dự án tham khảo (ASP.NET, DB tên `Blog502`).
- `F:\JAVA\final_project\WebAPI401\WebAPI401\WebAPI401\BusinessLogic\Repository\CategoryRepository\GetCategoriesFlattenHandler.cs` — logic dựng cây category từ danh sách phẳng (dùng `ParentId` + `Position`), lọc `Active`, và **tính `Level` tại runtime bằng thuật toán Stack DFS** (không lưu cứng — cột `Level` trong DB thực tế luôn `NULL` trong dữ liệu mẫu).

### Cấu trúc bảng `Category` tham khảo (đầy đủ)
`CategoryId, CategoryName, Slug, Description, ShowDefault, Position, Active, ParentId, CreatedBy, CreatedTime, ModifiedTime, Photo, IsBlog, Level, PhotoPath`

### Quyết định phạm vi (rút gọn cho e-commerce, tránh mang thừa field đặc thù Blog)
- **Giữ lại:** `CategoryId, CategoryName, Slug, Description, Position, Active, ParentId, ImageUrl (đổi tên từ Photo, khớp field đã có sẵn), CreatedTime`.
- **Bỏ:** `CreatedBy` (uniqueidentifier — không có hệ thống user-tracking phù hợp ở đây), `ShowDefault`, `IsBlog` (đặc thù blog, không liên quan e-commerce), `Level` lưu cứng (sẽ tính runtime giống bản gốc, không lưu DB để tránh dữ liệu bị lệch khi cây thay đổi), `PhotoPath`, `ModifiedTime` (có thể bổ sung sau nếu cần).
- **Giữ nguyên tên bảng số nhiều `Categories`** (khớp `@Table(name = "Categories")` hiện tại trong `Category.java`, catalog-service) thay vì đổi thành `Category` số ít như bản gốc — tránh phải sửa lại entity mapping không cần thiết.

### Dữ liệu category mẫu (2 cấp, lấy từ `_header.html` — nav thật đang hiển thị trên `index.html`)
- **Cấp 1 (3):** Kitchen, Personal Care, Household
- **Cấp 2 của Kitchen (13):** Water & Beverages, Fruits & Vegetables, Staples, Branded Food, Breakfast & Cereal, Snacks, Spices, Biscuit & Cookie, Sweets, Pickle & Condiment, Instant Food, Dry Fruit, Tea & Coffee
- **Cấp 2 của Personal Care (12):** Ayurvedic, Baby Care, Cosmetics, Deo & Purfumes, Hair Care, Oral Care, Personal Hygiene, Skin care, Fashion Accessories, Grooming Tools, Shaving Need, Sanitary Needs
- **Cấp 2 của Household (12):** Cleaning Accessories, CookWear, Detergents, Gardening Needs, Kitchen & Dining, KitchenWear, Pet Care, Plastic Wear, Pooja Needs, Serveware, Safety Accessories, Festive Decoratives

### Các task cụ thể

- `[x]` **Cập nhật tính năng Category (2 cấp)**
  - `[x]` **Bước 1 — Nghiên cứu tài liệu tham khảo** (đã hoàn thành lúc lập kế hoạch: đọc `Category.sql`, `GetCategoriesFlattenHandler.cs`, đối chiếu `Category` entity hiện tại ở `catalog-service` — quá đơn giản, chỉ có `categoryId/categoryName/imageUrl`, thiếu hoàn toàn cấu trúc cây).
  - `[x]` **Bước 2 — SQL: Drop & tạo lại bảng `Categories`**
    - `[x]` Script `DROP TABLE dbo.categories` (SQL Server, DB `EcommerceDB`) — đã chạy trực tiếp qua `sqlcmd`.
    - `[x]` Script `CREATE TABLE dbo.categories` theo cấu trúc đã rút gọn (snake_case: `category_id, category_name, slug, description, position, active, parent_id, image_url, created_time` — đã xác nhận đúng quy ước cột thực tế của Hibernate qua `INFORMATION_SCHEMA.COLUMNS` trước khi viết script), kèm `FOREIGN KEY (parent_id) REFERENCES categories(category_id)` tự tham chiếu.
  - `[x]` **Bước 3 — SQL: Insert dữ liệu category mẫu 2 cấp**
    - `[x]` Insert 3 category cấp 1 (`parent_id = NULL`), dùng `SCOPE_IDENTITY()` lấy ID vừa tạo thay vì giả định trước ID.
    - `[x]` Insert 37 category cấp 2, gắn đúng `parent_id` tương ứng. Xác nhận qua query JOIN: toàn bộ 40 dòng đều đúng phân cấp.
    - **Lưu ý hệ quả:** 10 sản phẩm demo hiện có (`categoryId` cũ) sẽ không còn khớp category mới (danh mục cũ là điện thoại/laptop, danh mục mới là siêu thị) — cần gán lại `categoryId` cho từng sản phẩm qua Admin sau khi hoàn thành Bước 4/5.
  - `[x]` **Bước 4 — Cập nhật Backend (`catalog-service`)**
    - `[x]` Cập nhật `Category` entity: thêm `parentId`, `slug`, `description`, `position`, `active`, `createdTime` (đọc-only, `insertable/updatable=false` để khớp `DEFAULT GETDATE()` ở DB), thêm field `@Transient children` để trả cây (không lưu DB).
    - `[x]` Cập nhật `CategoryRepository`: thêm `findByActiveTrueAndParentIdIsNullOrderByPositionAsc()` và `findByActiveTrueAndParentIdOrderByPositionAsc(parentId)`.
    - `[x]` Cập nhật `CategoryController`: thêm `GET /api/categories/tree` — trả cây 2 cấp (chỉ danh mục Active), đơn giản hơn bản C# gốc vì chỉ có 2 cấp, không cần thuật toán Stack tổng quát cho N cấp.
    - `[x]` Test API `/api/categories/tree` — xác nhận trả đúng 3 danh mục cấp 1, mỗi danh mục có đúng danh sách con theo thứ tự `position`.
  - `[x]` **Bước 5 — Cập nhật trang quản lý Category trên Dashboard (Admin)**
    - `[x]` Cập nhật `admin/categories.html`: hiển thị phân cấp (cấp 1 in đậm, cấp 2 thụt lề `↳` + có cột "Danh mục cha", badge Active/Ẩn).
    - `[x]` Cập nhật `admin/category-form.html`: thêm `slug`, `description` (textarea), dropdown "Danh mục cha" (`parentId`, để trống = cấp 1, chỉ liệt kê danh mục cấp 1), `position`, checkbox `active`.
    - `[x]` Cập nhật `CategoryDTO` (frontend-service): thêm `slug, description, position, active, parentId, parentName` (`parentName` chỉ hiển thị, không map từ backend).
    - `[x]` Cập nhật `AdminController`: `manageCategories()` build danh sách phẳng theo đúng thứ tự cây (cấp 1 → các con ngay sau, sort theo `position`) kèm `parentName`; `createCategoryForm()`/`editCategoryForm()` truyền thêm `parentCategories` (danh mục cấp 1) cho dropdown.
    - `[x]` **Bug phát hiện khi test (áp dụng toàn app, không riêng Category):** lưu mô tả tiếng Việt qua form Admin bị lỗi mojibake (`"NÆ°á»›c giáº£i kháº¯t..."`) do `frontend-service` thiếu cấu hình encoding — Tomcat mặc định decode POST form theo ISO-8859-1. **Fix:** thêm vào `application.yml`:
      ```yaml
      server:
        servlet:
          encoding:
            charset: UTF-8
            enabled: true
            force: true
      ```
      Đã test lại qua trang edit Category (`/admin/categories/edit/4`, mô tả tiếng Việt có dấu) sau khi restart `frontend-service` — xác nhận lưu đúng UTF-8 qua `GET /api/categories/4`. Fix này áp dụng cho **mọi form Admin** (Product, Category, Member...) chứ không riêng Category — trước đây các form Product/Category nhập tiếng Việt có dấu đều có nguy cơ bị lưu sai.
  - `[x]` **Bước 6 — Cập nhật Navbar Header (Client) load động từ DB**
    - `[x]` Thêm field `children` (`List<CategoryDTO>`) vào `CategoryDTO` (frontend-service) để khớp JSON trả về từ `GET /api/categories/tree`.
    - `[x]` Thêm `CatalogApiService.getCategoryTree()` — gọi `{categoryServiceUrl}/tree`, bọc try/catch trả về list rỗng nếu catalog-service lỗi (vì header dùng chung mọi trang, không được để lỗi kết nối làm sập toàn bộ trang Client).
    - `[x]` Tạo `ClientGlobalAdvice` (`@ControllerAdvice(assignableTypes = {ClientController, AuthController, AccountController, CartController, CheckoutController})` + `@ModelAttribute("categoryTree")`) — nạp sẵn cây category vào mọi trang Client, không cần sửa lặp lại từng Controller. Cố ý không dùng quét toàn bộ `@Controller` vì `AdminController`/`AdminAuthController` nằm chung package nhưng dùng layout riêng, không cần `categoryTree`.
    - `[x]` Viết lại `_header.html`: thay 3 dropdown menu tĩnh (Kitchen/Personal Care/Household + submenu cứng, đúng bằng "demo" — mọi link con đều trỏ về cùng 1 file tĩnh) bằng `th:each` lặp 2 cấp qua `categoryTree`. Danh mục con hiển thị dạng lưới 3 cột bằng CSS `column-count:3` (không cần tính toán chia cột thủ công trong Thymeleaf, tự co giãn theo số lượng con thực tế). Cột ảnh minh họa (`w3l`) chỉ hiện khi category cấp 1 có `imageUrl`.
    - **Giới hạn đã biết (ngoài phạm vi Bước 6):** link danh mục con hiện trỏ về trang chủ (`/`) vì chưa có trang lọc sản phẩm theo category — đây là tính năng riêng, chưa được yêu cầu.
  - `[x]` Test: mở `/`, `/login`, `/product/{id}` (đại diện `ClientController`, `AuthController`) qua browser — xác nhận navbar hiển thị đúng 3 danh mục cấp 1 (Kitchen, Personal Care, Household) và tổng đúng 37 danh mục cấp 2 (13+12+12), khớp 100% dữ liệu DB thật. Chưa test trực tiếp việc Admin sửa/xóa category rồi F5 lại Client navbar, nhưng cơ chế gọi API trực tiếp mỗi request (không cache) nên chắc chắn phản ánh đúng dữ liệu mới nhất.

Sẽ làm tuần tự từng bước, mỗi bước dán code → review → sang bước tiếp, giữ đúng quy trình đã áp dụng xuyên suốt dự án.

## Cập nhật dữ liệu Products theo template gốc (17/08/2026)

Sau khi hoàn thành tính năng Category 2 cấp, 10 sản phẩm demo cũ (điện thoại/laptop) không còn khớp với bộ danh mục siêu thị mới. Thay vì gán lại category thủ công, quyết định thay hẳn bằng dữ liệu sản phẩm thật được trích xuất từ `client_web_app_template/web/index.html` (template gốc W3layouts).

- `[x]` Đọc `index.html`, trích xuất chính xác **24 sản phẩm** (16 sản phẩm theo 4 tab "Special Offers" đầu trang: Staples/Snacks/Fruits & Vegetables/Breakfast & Cereal, + 8 sản phẩm ở khối "Special Offers" thứ 2 cuối trang — xác nhận đủ 24 qua đối chiếu ID modal 1-24, không sót/thừa sản phẩm nào).
- `[x]` Map thủ công từng sản phẩm vào đúng category cấp 2 thật trong DB theo tên/ý nghĩa sản phẩm (vd. Moong/Sunflower Oil/Kabuli Chana/Soya Chunks → Staples id=6; Lays/Kurkure/Popcorn/Nuts → Snacks id=9; Banana/Onion/Apples/Grapes/Lady Finger → Fruits & Vegetables id=5; Honey/Chocos/Oats/Bread → Breakfast & Cereal id=8; Moisturiser → Skin care id=24; Conditioner/Gel → Hair Care id=21; Clips → Fashion Accessories id=25; Cleaner → Cleaning Accessories id=29; Satin Ribbon Red → Festive Decoratives id=40).
- `[x]` Quy đổi giá gốc USD trong template sang VNĐ theo tỷ giá 25.000 VNĐ/USD — dùng đúng con số đã hardcode sẵn ở `PayPalService.java` (`exchange.rate.vnd-usd:25000`) để nhất quán trong toàn hệ thống, không bịa tỷ giá mới.
- `[x]` Quy đổi khối lượng ("1 kg"/"500 g"/"6 pcs"...) sang gram (kiểu `weight SMALLINT` của DB), chọn đơn vị hiển thị (`unit`) phù hợp tiếng Việt cho từng loại sản phẩm (Kg, Gói, Chai, Hộp, Cuộn, Túi, Tuýp, Ổ, Nải...).
- `[x]` Viết script SQL `DROP TABLE` + `CREATE TABLE dbo.Products` (giữ nguyên đúng cấu trúc cột hiện tại đã xác nhận qua `INFORMATION_SCHEMA.COLUMNS`: `product_id, category_id, product_name, image_url, unit_price, sale_of_price, weight, unit, description, content` — không có FK ràng buộc nào tới/từ bảng `Products` nên an toàn để drop) + `INSERT` 24 dòng — đã xác nhận trước khi chạy rằng ảnh (`of.png`...`of23.png`) đã tồn tại sẵn trong `frontend-service/static/client/images/`, không cần copy thêm asset. Đã chạy trực tiếp qua `sqlcmd` theo xác nhận của người dùng.
- `[x]` Verify qua `GET /api/products` (catalog-service) — đủ 24 sản phẩm, đúng category/giá/ảnh.
- `[x]` **Bug phát hiện khi verify trực quan trên trang chủ:** toàn bộ 24 sản phẩm đều hiển thị `$7.00/$6.00` bất kể giá thật — do [index.html:130](../source/frontend-service/src/main/resources/templates/client/index.html:130) (khối "Special Offers" trang chủ) hardcode giá tĩnh trong HTML thay vì bind `th:text` vào `product.unitPrice`/`saleOfPrice` (chỉ thuộc tính ẩn `data-price` dùng cho JS giỏ hàng là đúng). Đây là lỗi có sẵn từ trước, không phải do lần cập nhật SQL này gây ra (đã tồn tại với 10 sản phẩm demo cũ).
  - `[x]` Sửa `index.html`: bind `th:text="${product.unitPrice}"` / `th:text="${product.saleOfPrice}"` (giữ tiền tố `$` theo đúng convention đã dùng ở `single.html`).
  - `[x]` **Bug thứ 2 phát hiện khi test lại (server lỗi 500 sau khi sửa):** `ProductDTO` (frontend-service) chưa từng có field `saleOfPrice` — đây là lý do gốc khiến template phải hardcode giá thay vì bind, vì DTO thiếu field từ đầu. Khi bind `${product.saleOfPrice}` vào Thymeleaf, SpringEL báo lỗi `EL1008E: Property or field 'saleOfPrice' cannot be found`, làm trang chủ crash (HTTP 500, response bị cắt giữa chừng). Fix: thêm field `saleOfPrice` (`BigDecimal`) + getter/setter vào `ProductDTO.java`.
  - `[x]` Compile, restart `frontend-service`, verify lại qua `curl` (Browser pane bị lỗi tool tạm thời lúc test, không liên quan tới ứng dụng) — xác nhận cả 24 sản phẩm hiển thị đúng giá gốc/giá khuyến mãi thật, không còn lỗi trong log.

## Bổ sung khối "Special Offers theo Category" trên trang chủ (17/08/2026)

Tham khảo lại `client_web_app_template/web/index.html` (khối tab `Staples/Snacks/Fruits & Vegetables/Breakfast & Cereal` ở đầu trang, đứng trước phần "New Collections") — khối này chưa tồn tại trong `frontend-service` (trang chủ trước đó chỉ có 1 khối "Special Offers" dạng lưới phẳng, không tab, ở cuối trang).

- `[x]` Thêm `categoryId` (`Short`) và `unit` (`String`) vào `ProductDTO` (frontend-service) — trước đó DTO chỉ có `productId/productName/unitPrice/saleOfPrice/imageUrl/description`, thiếu 2 field cần để nhóm sản phẩm theo category và hiển thị đơn vị tính thật.
- `[x]` Cập nhật `ClientController.home()`: build `Map<String, List<ProductDTO>>` (`productsByCategory`, `LinkedHashMap` giữ thứ tự xuất hiện) bằng cách tra `categoryId` của từng sản phẩm qua `Map<Short, String>` lấy từ `catalogApiService.getAllCategories()`. Tab được sinh **hoàn toàn động** theo category nào thực sự có sản phẩm — không hardcode tên/số lượng tab như bản template gốc (vốn chỉ có sẵn đúng 4 tab vì demo data giới hạn).
- `[x]` Viết lại `index.html`: thêm section `content-top` (đặt giữa banner video và `content-mid`, đúng vị trí như template gốc) chứa `<ul class="nav tabs">` + `<div class="tab-content">` lặp qua `productsByCategory` bằng `th:each="entry, catStat : ${productsByCategory}"`, dùng `catStat.first`/`catStat.index` để đánh dấu tab active và sinh id neo (`#cat-tab-N`).
  - Cố ý **bỏ** phần modal quick-view (`data-toggle="modal" data-target="#myModalN"`) của template gốc vì đó là tính năng riêng ngoài yêu cầu ("hiển thị danh sách sản phẩm theo category") — thay vào đó click ảnh/tên dẫn thẳng tới `/product/{id}`, nhất quán với khối "Special Offers" phẳng đã có sẵn phía dưới.
  - Giữ ruy băng "Offer" như trang trí tĩnh (không có logic khuyến mãi thật đứng sau).
- `[x]` Test qua `curl` (Browser pane gặp lỗi tool tạm thời lúc này, không liên quan ứng dụng — đã chuyển sang verify qua `curl`/log): xác nhận đủ **9 tab** sinh đúng theo dữ liệu thật (Staples, Snacks, Fruits & Vegetables, Breakfast & Cereal, Skin care, Festive Decoratives, Fashion Accessories, Hair Care, Cleaning Accessories — khớp chính xác 9 category đang có sản phẩm trong DB), tab đầu tiên (Staples) chứa đúng 4 sản phẩm (Moong, Sunflower Oil, Kabuli Chana, Soya Chunks), không có lỗi trong `frontend.log` sau khi restart.
- `[x]` **Bug phát hiện qua ảnh chụp UI của người dùng:** khối "Special Offers" phẳng (dưới cùng, 4 cột `col-md-3`) hiển thị khuyết ngẫu nhiên 1-2 sản phẩm mỗi vài hàng, trông như dữ liệu bị thiếu.
  - **Nguyên nhân (đã tái hiện + đo đạc trực tiếp qua DOM `getBoundingClientRect()`):** tên sản phẩm dài (`Sunflower Oil`, `Kabuli Chana`, `Satin Ribbon Red`...) bị wrap 2 dòng trong `<h6>` (không có giới hạn chiều cao), khiến `.col-m` của card đó cao hơn (286-305px) so với card tên ngắn (268px). Do `.product .con-w3l` dùng layout CSS float thuần (Bootstrap 3 `col-md-3`, không phải flexbox/grid, không có `clearfix` chèn sau mỗi 4 item), một card cao bất thường ở hàng trước làm card ở đúng cột đó tại hàng sau bị đẩy xuống lố, để lại khoảng trống ở các cột còn lại trong hàng đó — đây là lỗi CSS có sẵn từ template gốc, chỉ lộ rõ khi tên sản phẩm thật có độ dài chênh lệch nhiều (dữ liệu demo cũ ít bị wrap hơn).
  - **Fix:** thêm rule `.mid-1 h6 { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }` vào cuối [style.css](../source/frontend-service/src/main/resources/static/client/css/style.css) — ép tên sản phẩm luôn 1 dòng (cắt `...` nếu quá dài), đảm bảo mọi card cùng chiều cao.
  - **Lưu ý vận hành phát hiện thêm:** khi chạy `mvn spring-boot:run`, sửa file tĩnh trong `src/main/resources/static/` **không tự động phản ánh ngay** ra server đang chạy — server phục vụ từ `target/classes/static/` (bản snapshot tại lần `mvn compile` gần nhất), cần chạy lại `mvn compile` (không cần restart JVM) để đồng bộ. Khác với `.yml`/template Thymeleaf (chỉ cần restart, không cần compile) — đây là quy tắc riêng cho static resource (css/js/images).
  - Verify: đo `getBoundingClientRect()` của cả 24 card sau fix — toàn bộ đồng nhất 268px, xếp đúng 6 hàng x 4 cột liền mạch, không còn khoảng trống. Browser pane bị cache CSS cũ nên phải verify bằng cách inject rule tương đương qua JS trực tiếp; đã xác nhận file `style.css` trên server (qua `curl`) chứa đúng fix — người dùng cần hard-refresh (Ctrl+Shift+R) trên trình duyệt thật để thấy ngay.
- `[x]` **Bug thứ 2 phát hiện qua ảnh chụp UI (khối tab `content-top`):** các hàng sản phẩm dính sát nhau, không có khoảng cách dọc.
  - **Nguyên nhân:** card trong khối tab mới dùng class `.m-wthree` (đúng theo template gốc), nhưng `.m-wthree` chỉ có `margin-bottom` khai báo bên trong `@media(max-width:991px)` — ở độ phân giải desktop (≥992px, layout 4 cột), card này hoàn toàn không có khoảng cách hàng. (Khối "Special Offers" phẳng phía dưới dùng class khác `.pro-1` vốn đã có `margin-bottom:2em` ở mức desktop nên không bị lỗi này.)
  - **Fix:** thêm `@media(min-width:992px){ .content-top .m-wthree { margin-bottom: 2em; } }` vào `style.css`, scope riêng cho desktop và riêng khối `.content-top` để không đụng tới các breakpoint tablet/mobile đã hoạt động đúng.
- `[x]` **Bug thứ 3 phát hiện khi verify — nghiêm trọng hơn, ảnh hưởng dữ liệu:** trong lúc kiểm tra khối tab, phát hiện đơn vị tính (`unit`) hiển thị lỗi ký tự (`"Ná»£i"` thay vì `"Nải"`, `"GÃ³i"` thay vì `"Gói"`...). Kiểm tra qua `GET /api/products` xác nhận **toàn bộ 24 sản phẩm** đều bị lỗi encoding ở cả `unit` lẫn `description` (mọi giá trị có dấu tiếng Việt).
  - **Nguyên nhân (khác với bug UTF-8 form đã fix trước đó ở `application.yml`):** đây là dữ liệu insert trực tiếp qua `sqlcmd` (không qua form web), nên bug Tomcat encoding không liên quan. Root cause: file script `update_products.sql` được lưu dạng UTF-8 **không có BOM**; `sqlcmd` mặc định đọc input file theo codepage hệ thống (không phải UTF-8) khi không truyền `-f`, khiến các literal `N'...'` chứa dấu tiếng Việt bị hiểu sai byte ngay tại lúc INSERT — dữ liệu bị hỏng vĩnh viễn trong DB (không phải lỗi hiển thị).
  - **Fix:** viết script `UPDATE dbo.Products SET unit = N'...', description = N'...' WHERE product_id = ...` cho cả 24 dòng, chạy lại qua `sqlcmd` với cờ **`-f 65001`** (ép codepage UTF-8 khi đọc input file) — đã xác nhận qua `sqlcmd -?` rằng cờ này được hỗ trợ (`-f <codepage> | i:<codepage>[,o:<codepage>]`). Đã chạy trực tiếp theo xác nhận của người dùng.
  - Verify: `GET /api/products` — toàn bộ 24 sản phẩm hiển thị đúng `unit`/`description` tiếng Việt có dấu, không còn mojibake.
  - **Rút kinh nghiệm cho các script SQL có N'...' tiếng Việt sau này:** luôn chạy `sqlcmd` với `-f 65001`, không dùng mặc định.
- `[x]` **Bug thứ 4 — fix khoảng cách hàng (Bug thứ 2) ban đầu KHÔNG có tác dụng, người dùng xác nhận lại bằng ảnh chụp (kể cả sau hard-refresh/incognito):** điều tra sâu hơn phát hiện nguyên nhân thật sự không phải cache mà là **lỗi cú pháp có sẵn trong `style.css` gốc** (không phải do các lần sửa trước gây ra).
  - **Nguyên nhân gốc:** khối `@media(max-width:414px){` cuối cùng của file gốc (mở ở dòng ~2820) **thiếu dấu đóng `}`**. Với file gốc (không có gì phía sau), trình duyệt tự "đóng ngầm" ở EOF (hành vi khoan dung tiêu chuẩn của CSS parser) nên không ai nhận ra bug này bấy lâu. Nhưng sau khi tôi append 2 fix CSS (Bug thứ 1 và thứ 2) vào cuối file, cả 2 rule mới đó bị rơi vào **bên trong** khối `@media(max-width:414px)` chưa đóng — khiến chúng chỉ có hiệu lực (hoặc vô hiệu hoàn toàn do lồng `@media` mâu thuẫn điều kiện) chứ không áp dụng như khai báo, dù xem bằng `curl`/đọc text file vẫn thấy rule "có mặt" bình thường (chỉ parser CSS thực sự của trình duyệt mới "nuốt mất" — không thể phát hiện qua so sánh text).
  - **Cách phát hiện:** dùng `document.styleSheets` (CSSOM đã parse) để liệt kê toàn bộ rule thực sự được trình duyệt nạp — thấy rule cuối cùng dừng đúng ở `.grid_3 p, .page-header p` (rule cuối file gốc), xác nhận mọi thứ sau đó bị "nuốt". Đối chiếu với việc đếm dấu `{`/`}` theo từng dòng (không phải đếm tổng, vì tổng vẫn cân bằng 694=694 do bù trừ) mới lộ ra khối `@media(max-width:414px)` không bao giờ quay về balance 0 trước khi hết file gốc.
  - **Fix:** chuyển dấu `}` (vốn nằm lạc ở cuối file, sau 2 fix của tôi) về đúng vị trí ngay sau rule `.grid_3 p, .page-header p { font-size: 13px; }` để đóng đúng khối `@media(max-width:414px)` gốc, rồi mới đặt 2 fix CSS phía sau (giờ đã ở đúng cấp top-level/media riêng như khai báo).
  - Verify: sau khi sửa, ép trình duyệt nạp lại `style.css` qua query string cache-busting (`?v=timestamp`, bỏ qua mọi tầng cache) — xác nhận `document.styleSheets` giờ có đúng rule `.content-top .m-wthree`, và khoảng cách 2 hàng đo được đúng 28px, khớp khối "Special Offers" phẳng phía dưới.
  - **Lưu ý:** do phát hiện muộn, **cả 2 fix trước đó (Bug 1 - ellipsis tên sản phẩm, Bug 2 - khoảng cách hàng) coi như MỚI thực sự có hiệu lực từ lần sửa này**, dù đã báo với người dùng là "đã fix" trước đó — cần người dùng hard-refresh/mở incognito lại một lần nữa để kiểm chứng cả 2.

## Lọc "Sản phẩm khác" theo cùng Category trên trang chi tiết sản phẩm (17/08/2026)

Trang `client/single.html` (chi tiết sản phẩm) có sẵn section "Sản phẩm khác" gợi ý sản phẩm liên quan, nhưng trước đó `ClientController.productDetail()` chỉ lọc loại trừ chính sản phẩm đang xem, không lọc theo category — hiển thị lẫn lộn sản phẩm khác category (vd. xem "Banana" (Fruits & Vegetables) lại gợi ý "Moong" (Staples), "Lays" (Snacks)...).

- `[x]` Sửa `ClientController.productDetail()`: thêm điều kiện lọc `p.getCategoryId()` phải trùng với `product.getCategoryId()` của sản phẩm đang xem, giữ nguyên điều kiện loại trừ chính nó. Tận dụng field `categoryId` đã có sẵn trên `ProductDTO` (thêm trước đó cho tính năng khối tab category).
- `[x]` Test qua `curl http://localhost:8888/product/9` (Banana, category Fruits & Vegetables): xác nhận "Sản phẩm khác" chỉ còn đúng 5 sản phẩm cùng category (Onion, Bitter Gourd, Apples, Lady Finger, Grapes), không còn sản phẩm khác category, không còn chính Banana.
- **Lưu ý quy trình:** lần này tôi lỡ sửa trực tiếp vào `ClientController.java` bằng Edit tool thay vì đưa code cho người dùng dán theo đúng quy trình xuyên suốt dự án — đã xin lỗi và xác nhận lại với người dùng trước khi compile/restart. Cần quay lại đúng quy trình "đưa code → người dùng dán → mới compile" cho các lần sau, trừ khi được trao quyền tường minh.

## Quản lý Role & Permission trên Admin Dashboard — Kế hoạch mới (17/08/2026)

**Yêu cầu:** thêm 1 trang trên Admin Dashboard để quản lý role và permission.

### Hiện trạng (khảo sát trước khi lập kế hoạch)

- `auth-service` hiện lưu phân quyền cực kỳ đơn giản: bảng `users` (`id, username, password, role`) với `role` là **cột chuỗi tự do** (`varchar`, giá trị thực tế chỉ có `"ROLE_USER"` / `"ROLE_ADMIN"`). Không có bảng `roles`/`permissions`/join table nào tồn tại (đã grep toàn bộ `source/`, không có kết quả).
- JWT (`JwtUtil.generateToken`) chỉ nhúng 1 claim `role` dạng chuỗi đơn — không có chỗ chứa danh sách permission.
- `SecurityConfig.java` có khai báo `@EnableWebSecurity` nhưng **không thực sự dùng** (`permitAll()` cho `/api/auth/**`, `anyRequest().authenticated()`, không có `UserDetailsService`/JWT filter/`@PreAuthorize`) — toàn bộ việc chặn quyền hiện nay nằm ở `frontend-service`, không phải Spring Security.
- `AdminAuthInterceptor` (frontend-service) hiện chặn `/admin/**` bằng đúng 1 điều kiện nhị phân: `session.role.equals("ROLE_ADMIN")`. Không có khái niệm "trang nào cần quyền gì" — mọi Admin đăng nhập được đều thấy tất cả trang (Products/Categories/Orders/Members) như nhau.
- `members` (customer-service) là bảng **tách biệt** với `users` (auth-service) và không có cột `role` — tính năng này **chỉ áp dụng cho tài khoản Admin/Staff (`users`)**, không đụng tới tài khoản khách hàng (`members`).
- Pattern CRUD Admin hiện có (theo Category — dùng làm khuôn mẫu): `GET /admin/x` (list), `GET /admin/x/new`, `GET /admin/x/edit/{id}`, `POST /admin/x/save` (upsert chung 1 endpoint), `POST /admin/x/delete/{id}`.

### Quyết định phạm vi

- Xây RBAC (Role-Based Access Control) đúng nghĩa: bảng `roles`, `permissions`, `role_permissions` (many-to-many) trong DB của `auth-service`.
- **Migrate sạch, không dùng shim tương thích ngược:** thay cột `users.role` (chuỗi tự do) bằng `users.role_id` (FK tới `roles`). Seed sẵn 2 role mặc định `ROLE_ADMIN`/`ROLE_USER` khớp đúng tên chuỗi cũ, để `AdminAuthInterceptor`/`AdminAuthController` chỉ cần đổi từ so sánh chuỗi trực tiếp sang so sánh `user.getRole().getRoleName()` — không cần viết lại toàn bộ logic đăng nhập.
- Permission không nhúng vào JWT (JWT giữ nguyên chỉ có `role` claim như hiện tại) — việc kiểm tra permission tra thẳng DB theo `role_id` mỗi request, vì dự án chưa có hạ tầng Spring Security thật để gắn `hasAuthority()`.
- Danh sách permission khởi tạo theo đúng các trang Admin đang tồn tại: `PRODUCT_MANAGE`, `CATEGORY_MANAGE`, `ORDER_MANAGE`, `MEMBER_MANAGE`, `ROLE_MANAGE` (tự áp dụng cho chính trang Role/Permission).

### Task list

- `[x]` **Bước 1 — SQL: tạo bảng `roles`, `permissions`, `role_permissions`; migrate `users.role`**
  - `[x]` `CREATE TABLE roles (role_id SMALLINT IDENTITY PK, role_name NVARCHAR(50) UNIQUE NOT NULL, description NVARCHAR(255))` — đã xác nhận cấu trúc `users` thật qua `INFORMATION_SCHEMA.COLUMNS` trước khi viết script (giống pattern Category/Product).
  - `[x]` `CREATE TABLE permissions (permission_id SMALLINT IDENTITY PK, permission_name NVARCHAR(50) UNIQUE NOT NULL, description NVARCHAR(255))`.
  - `[x]` `CREATE TABLE role_permissions (role_id SMALLINT FK, permission_id SMALLINT FK, PRIMARY KEY(role_id, permission_id))`.
  - `[x]` Seed 2 role mặc định (`ROLE_ADMIN`, `ROLE_USER`) + 5 permission khởi tạo (`PRODUCT_MANAGE`, `CATEGORY_MANAGE`, `ORDER_MANAGE`, `MEMBER_MANAGE`, `ROLE_MANAGE`), gán toàn bộ 5 permission cho `ROLE_ADMIN` (`ROLE_USER` không có quyền Admin nào).
  - `[x]` Thêm cột `users.role_id` (FK), migrate dữ liệu từ cột `role` cũ theo đúng tên chuỗi, sau đó `DROP COLUMN role`. Đã chạy trực tiếp qua `sqlcmd -f 65001` (rút kinh nghiệm từ bug mojibake trước đó) theo xác nhận của người dùng.
  - Verify: `roles` (2 dòng), `permissions` (5 dòng), `role_permissions` (đúng 5 dòng cho `ROLE_ADMIN`), `users` migrate đúng (`admin01`→`ROLE_ADMIN`, 3 user còn lại→`ROLE_USER`), cột `role` cũ đã biến mất khỏi `INFORMATION_SCHEMA.COLUMNS`. Dữ liệu tiếng Việt (`description`) xác nhận đúng UTF-8 qua `sqlcmd -o file.txt` rồi đọc file (console/bash hiển thị `?` chỉ là lỗi codepage terminal, không phải lỗi dữ liệu thật).
- `[x]` **Bước 2 — Backend `auth-service`:** entity `Role`, `Permission` (quan hệ `@ManyToMany` qua `role_permissions`, unidirectional từ `Role` — `Permission` không có back-reference nên tránh được vòng lặp serialize JSON); cập nhật `User.role` (String) → `User.role` (`@ManyToOne @JoinColumn(name="role_id")`); `RoleRepository`, `PermissionRepository`; `RoleController` (`GET/POST/PUT/DELETE /api/roles`, nhận `RoleRequest{roleName, description, permissionIds}` để gán permission qua danh sách ID thay vì object đầy đủ); `PermissionController` (CRUD đơn giản, trả/nhận thẳng entity `Permission` giống pattern `CategoryController`).
  - `[x]` Cập nhật `AuthController`: `login` dùng `user.getRole().getRoleName()` cho JWT + response; `register` đổi từ nhận thẳng entity `User` sang DTO `RegisterRequest{username,password,role}` (tra `Role` theo tên, mặc định `ROLE_USER` nếu thiếu/không khớp) — bắt buộc phải đổi vì `role` không còn là `String` để Jackson deserialize trực tiếp từ JSON được nữa.
  - `[x]` **Bug phát hiện khi test:** `/api/roles`, `/api/permissions` trả về `403 Forbidden` dù code không lỗi — do `SecurityConfig.java` (đã tồn tại từ trước, cấu hình `anyRequest().authenticated()`) chặn mọi route mới ngoài `/api/auth/**`. Fix: thêm `/api/roles/**`, `/api/permissions/**` vào danh sách `permitAll()`, giữ đúng kiến trúc hiện tại (việc chặn quyền thật sự nằm ở `AdminAuthInterceptor` bên `frontend-service`, không phải Spring Security ở từng service).
  - Verify: `GET /api/roles` trả đúng `ROLE_ADMIN` (5 permission) + `ROLE_USER` (0 permission) kèm mô tả tiếng Việt đúng UTF-8; `GET /api/permissions` trả đủ 5 dòng; `POST /api/auth/login` (admin01/Admin@123) vẫn hoạt động đúng, trả `role: "ROLE_ADMIN"` — xác nhận luồng login không bị phá vỡ sau khi đổi cấu trúc `User.role`.
  - **Lưu ý quy trình:** có 2 lần trong bước này tôi lỡ sửa trực tiếp file (`SecurityConfig.java` và trước đó 1 lần ở `ClientController.java` bước trước) thay vì đưa code cho người dùng dán — đã xin lỗi và xác nhận lại trước khi build mỗi lần. Cần chú ý giữ đúng quy trình "đưa code → dán → mới build" cho các bước sau.
- `[x]` **Bước 3 — Backend `frontend-service`:** `PermissionDTO`, `RoleDTO` (có cả `permissions: List<PermissionDTO>` dùng để hiển thị và `permissionIds: List<Short>` dùng để bind checkbox form); `RoleApiService` gọi API `auth-service` (`saveRole` gửi `Map<String,Object>` đúng shape `RoleRequest{roleName,description,permissionIds}` chứ không gửi thẳng `RoleDTO`, vì field `permissions` đầy đủ không khớp kiểu bên `RoleController`); thêm vào `AdminController`: `GET /admin/roles`, `/admin/roles/new`, `/admin/roles/edit/{id}` (tự suy ra `permissionIds` từ `permissions` để checkbox tick đúng), `POST /admin/roles/save`, `POST /admin/roles/delete/{id}`.
  - Verify: đăng nhập `admin01`, gọi `GET /admin/roles` — lỗi trả về đúng là `TemplateInputException: Error resolving template [admin/roles]` (chưa có giao diện, đúng phạm vi Bước 4), xác nhận `AdminController` đã gọi `roleApiService.getAllRoles()` thành công và chỉ dừng ở bước render view.
- `[x]` **Bước 4 — Giao diện Admin:** `admin/roles.html` (danh sách, DataTables giống các trang khác, hiển thị permission dạng badge), `admin/role-form.html` (tên role + mô tả + checkbox danh sách permission, dùng `th:field="*{permissionIds}"` kết hợp `th:each` — Thymeleaf tự tick đúng checkbox theo dữ liệu hiện có), thêm link "Roles & Permissions" vào sidebar (`admin/fragments/_sidebar.html`).
  - Test end-to-end trên browser thật (đăng nhập `admin01`): danh sách hiển thị đúng `ROLE_ADMIN` (5 badge permission) và `ROLE_USER` (0 permission); tạo Role mới `ROLE_STAFF` chỉ tick `PRODUCT_MANAGE`+`CATEGORY_MANAGE` → xác nhận qua `GET /api/roles` lưu đúng 2 permission; mở lại trang Edit của `ROLE_STAFF` → xác nhận đúng 2 checkbox tự động tick sẵn, 3 checkbox còn lại không tick; xóa `ROLE_STAFF` → xác nhận biến mất khỏi `GET /api/roles`, chỉ còn `ROLE_ADMIN`/`ROLE_USER`.
- `[x]` **Bước 5 — Wiring thực thi quyền:**
  - `[x]` `AuthResponse` (cả `auth-service` và `frontend-service`) thêm field `permissions: List<String>` — nạp sẵn danh sách permission ngay tại lúc đăng nhập (từ `user.getRole().getPermissions()`), tránh phải gọi thêm API tra permission ở mỗi request.
  - `[x]` `AdminAuthController.processLogin`: đổi điều kiện đăng nhập từ bắt buộc đúng chuỗi `"ROLE_ADMIN"` sang "có ít nhất 1 permission" — cho phép mọi Role tùy chỉnh (không chỉ riêng tên `ROLE_ADMIN`) đăng nhập được vào Admin Dashboard; lưu `permissions` (dạng `HashSet<String>`) vào session.
  - `[x]` `AdminAuthInterceptor`: đổi từ kiểm tra nhị phân `role == ROLE_ADMIN` sang tra `session.permissions` theo tiền tố route (`/admin/products→PRODUCT_MANAGE`, `/admin/categories→CATEGORY_MANAGE`, `/admin/orders→ORDER_MANAGE`, `/admin/members→MEMBER_MANAGE`, `/admin/roles→ROLE_MANAGE`; route không có trong danh sách như `/admin` gốc chỉ cần đăng nhập với ≥1 permission, không cần quyền cụ thể). Chưa đăng nhập/không có permission nào → về `/admin/login`; có đăng nhập nhưng thiếu đúng permission route → về `/admin/access-denied` (phân biệt 2 trường hợp, không gộp chung).
  - `[x]` Tận dụng lại `admin/401.html` có sẵn nhưng chưa từng được dùng (đường dẫn CSS/link tĩnh, không chạy qua Thymeleaf) — sửa thành `th:href` đúng chuẩn, thêm route `GET /admin/access-denied` và loại trừ khỏi `AdminAuthInterceptor` (giống `/admin/login`).
  - Test end-to-end trên `curl` thật (không phải giả lập): tạo `ROLE_STAFF` (chỉ `PRODUCT_MANAGE`) qua API, đăng ký tài khoản `staff01` gán role này, đăng nhập qua `frontend-service` → xác nhận `/admin/products` trả `200`, `/admin/categories` và `/admin/roles` đều bị redirect `/admin/access-denied` (trang 401 render đúng nội dung), `/admin` (dashboard gốc) vẫn `200`. Đối chứng lại `admin01` (`ROLE_ADMIN`, đủ 5 permission) — cả 5 trang (`products/categories/orders/members/roles`) đều `200`, không bị ảnh hưởng bởi thay đổi logic. Đã dọn dữ liệu test (`staff01`, `ROLE_STAFF`) sau khi verify xong.

**Ghi chú:** Bước 5 là phần biến permission từ dữ liệu tĩnh thành có tác dụng thật (chặn/cho phép truy cập) — nếu muốn làm nhẹ/nhanh hơn trước, có thể dừng ở Bước 4 (trang quản lý Role/Permission hoạt động, dữ liệu lưu đúng) và làm Bước 5 sau như một giai đoạn riêng.

Sẽ làm tuần tự từng bước, mỗi bước dán code → review → sang bước tiếp, giữ đúng quy trình đã áp dụng xuyên suốt dự án.

**Trạng thái tổng kết (18/08/2026):** Cả 5/5 bước của tính năng Role & Permission đã hoàn thành và test end-to-end thành công. Toàn bộ hạ tầng RBAC (bảng `roles`/`permissions`/`role_permissions`, entity, API, giao diện quản lý Role, và wiring thực thi quyền qua `AdminAuthInterceptor`) đã hoạt động đúng thiết kế.

**Gap phát hiện qua trao đổi sau khi hoàn thành (18/08/2026):** người dùng hỏi "làm sao để assign role cho user?" — phát hiện ra tính năng vừa xây chỉ quản lý **bản thân các Role** (tạo role, gán permission cho role), chứ chưa có nơi nào để gán 1 Role cho **1 tài khoản `users` cụ thể** sau khi tài khoản đã tồn tại. Hiện trạng: role chỉ gán được duy nhất lúc `POST /api/auth/register` (tạo mới), và trang `/admin/register` trên UI lại hardcode cứng `ROLE_ADMIN`, không cho chọn. Không có trang "danh sách users + sửa role" nào cả — đây là phần còn thiếu để tính năng Role/Permission thực sự dùng được trọn vẹn trong thực tế, dẫn tới kế hoạch mới ngay bên dưới.

## Quản lý tài khoản (Users) — gán Role cho tài khoản Admin/Staff — Kế hoạch mới (18/08/2026)

**Yêu cầu:** thêm 1 trang "Quản lý tài khoản": danh sách `users`, cho sửa role qua dropdown (dùng lại đúng pattern CRUD đã thiết lập).

### Hiện trạng

- Bảng `users` (auth-service) đã có sẵn `id, username, password, role_id` (FK tới `roles`, xem phần Role & Permission ở trên) — không cần thêm cột/bảng mới, chỉ cần API đọc/sửa.
- Chưa có `UserController` nào ở `auth-service` — `UserRepository` chỉ đang được dùng nội bộ bởi `AuthController` (login/register/change-password).
- **Lưu ý bảo mật quan trọng:** `User` entity có field `password` (hiện lưu dạng plaintext, không BCrypt — tech debt đã ghi nhận từ Phase đầu dự án). Trang danh sách users **không được** trả thẳng entity `User` qua REST như cách `CategoryController`/`PermissionController` đang làm với `Category`/`Permission`, vì sẽ lộ mật khẩu — bắt buộc phải có DTO riêng loại bỏ field `password`.
- Cần phân biệt rõ (đã từng nhầm lẫn khi hỏi trước đây): đây là bảng `users` (tài khoản Admin/Staff, auth-service, gắn Role/Permission) — **khác hoàn toàn** với trang `/admin/members` hiện có (bảng `members`, khách hàng, customer-service, không có khái niệm role).

### Quyết định phạm vi

- Trang mới **chỉ sửa được Role** của 1 tài khoản đã tồn tại — không cho sửa `username`/`password` ở đây (đổi mật khẩu đã có sẵn luồng riêng `/api/auth/change-password`), không cho **tạo mới** user (đã có `/admin/register`), không cho **xóa** user (chưa có yêu cầu, xóa tài khoản đang đăng nhập có thể gây lỗi phiên — để lại nếu cần sau). Giữ đúng phạm vi "gán role cho user" người dùng yêu cầu, tránh lan man.
- Permission bảo vệ trang mới: dùng chung `ROLE_MANAGE` (đổi role cho user về bản chất cũng là quản lý phân quyền) — không tạo permission riêng mới.

### Task list

- `[x]` **Bước 1 — Backend `auth-service`:** tạo `UserSummaryDTO` (`id, username, roleId, roleName`, không có `password`); tạo `UserController` mới (theo đúng pattern tách riêng `RoleController`/`PermissionController`) — `GET /api/users` (danh sách, map từ `User` sang `UserSummaryDTO`), `PUT /api/users/{id}/role` (nhận `{roleId}`, tìm `Role` theo id, gán vào `user.setRole(...)`, lưu). Thêm `/api/users/**` vào `permitAll()` trong `SecurityConfig.java` (giống Role/Permission, tránh lặp lại bug 403 đã gặp).
  - Verify qua `curl`: `GET /api/users` trả đúng 4 user hiện có, **không có field `password`** (đúng mục tiêu bảo mật); `PUT /api/users/2/role {"roleId":1}` đổi `test1` từ `ROLE_USER`→`ROLE_ADMIN` thành công, revert lại `roleId:2` cũng thành công — dữ liệu về đúng trạng thái ban đầu sau khi test.
- `[x]` **Bước 2 — Backend `frontend-service`:** `UserApiService` gọi API mới; thêm vào `AdminController`: `GET /admin/users` (danh sách), `GET /admin/users/edit/{id}` (form đổi role, có dropdown `allRoles`), `POST /admin/users/save` (gọi `PUT /api/users/{id}/role`).
  - **Bug phát hiện khi review (trước khi dán code, do người dùng chủ động soi lại):** dự định đặt tên DTO là `UserDTO.java`, nhưng package `com.ecommerce.frontend.dto` đã có sẵn `UserDto.java` (dùng cho luồng đăng ký tài khoản — `username/password/role` dạng chuỗi, hoàn toàn khác mục đích). Trên Windows (filesystem không phân biệt hoa/thường), 2 tên chỉ khác hoa/thường (`UserDTO` vs `UserDto`) sẽ bị coi là **cùng 1 file**, gây đè lẫn nhau. Đã đổi tên thành `UserSummaryDTO` (khớp đúng tên đã dùng bên `auth-service` ở Bước 1) để tránh xung đột — không đụng tới `UserDto.java` cũ.
  - Verify: đăng nhập `admin01`, gọi `GET /admin/users` — lỗi trả về đúng là `TemplateInputException: Error resolving template [admin/users]` (chưa có giao diện, đúng phạm vi Bước 3), xác nhận `AdminController` đã gọi `userApiService.getAllUsers()` thành công.
- `[x]` **Bước 3 — Giao diện Admin:** `admin/users.html` (danh sách, DataTables, cột Username + Role hiện tại, không có nút Thêm/Xóa theo đúng quyết định phạm vi), `admin/user-role-form.html` (username hiển thị `readonly`, dropdown chọn Role mới dùng `th:field="*{roleId}"` tự chọn đúng role hiện tại), thêm link "Quản lý tài khoản" vào sidebar (`admin/fragments/_sidebar.html`).
- `[x]` Test end-to-end trên browser thật (đăng nhập `admin01`): danh sách hiển thị đúng 4 tài khoản kèm role; mở form đổi role của `test1` → xác nhận dropdown tự chọn đúng `ROLE_USER` hiện tại; đổi sang `ROLE_ADMIN` và lưu → xác nhận qua `GET /api/users` cập nhật đúng; revert lại `ROLE_USER` → xác nhận dữ liệu về đúng trạng thái ban đầu.

**Rủi ro đã biết (chưa xử lý ở kế hoạch này):** không có cơ chế chặn user tự đổi role của chính mình xuống mức thấp hơn (có thể tự khóa mình khỏi Admin Dashboard) — chấp nhận rủi ro này ở bản đầu vì là đồ án, không phải hệ thống production.

**Mở rộng phạm vi (18/08/2026):** người dùng yêu cầu thêm tính năng **tạo tài khoản mới** ngay trên trang `/admin/users` — đảo lại quyết định phạm vi ban đầu ("không cho tạo mới user, đã có `/admin/register`"). Lý do đổi: `/admin/register` là luồng tự-đăng-ký (self-registration), hardcode cứng `ROLE_ADMIN`, không cho chọn Role — không phù hợp để Admin tạo tài khoản Staff với Role tùy chỉnh. Quyết định: **tái sử dụng `POST /api/auth/register`** (đã có sẵn từ Phase Auth, nhận `{username,password,role}`) qua `AuthApiService.register()` (đã có sẵn) thay vì viết thêm API tạo user mới ở `auth-service` — tránh trùng lặp logic đăng ký.

- `[x]` **Bug phát hiện khi rà lại trước khi code:** `/admin/users` **chưa từng được gán permission nào** trong `AdminAuthInterceptor.resolveRequiredPermission()` — dù kế hoạch ở trên đã ghi rõ "dùng chung `ROLE_MANAGE`", nhưng lúc build Bước 5 của tính năng Role & Permission (làm trước khi tính năng Users tồn tại) không có route này để gán, và lúc build Bước 1-3 của tính năng Users lại quên bổ sung lại. Hậu quả: bất kỳ tài khoản nào có ≥1 permission (bất kỳ Role nào) đều vào được `/admin/users` tự do, không đúng thiết kế. Fix: thêm `if (path.startsWith("/admin/users")) return "ROLE_MANAGE";` vào `AdminAuthInterceptor`.
- `[x]` **Bước 4 — Thêm tính năng tạo tài khoản mới:** `AdminController` thêm `GET /admin/users/new` (form, dùng lại DTO `UserDto` có sẵn — username/password/role dạng chuỗi, đúng kiểu `AuthApiService.register()` cần) và `POST /admin/users/create` (gọi `authApiService.register()`, hiển thị lỗi nếu username trùng — theo đúng pattern lỗi của `/admin/register`); template mới `admin/user-form.html` (username, password, dropdown Role theo tên); thêm nút "Thêm tài khoản mới" vào `admin/users.html`.
- `[x]` Test end-to-end trên browser thật (đăng nhập `admin01`): tạo tài khoản `staff02` chọn `ROLE_USER` (không phải `ROLE_ADMIN` mặc định của dropdown) → xác nhận qua `GET /api/users` tạo đúng, không bị hardcode; tạo trùng username `staff02` lần 2 → hiển thị đúng lỗi "Tạo tài khoản thất bại, username có thể đã tồn tại!", không tạo trùng dữ liệu; tạo role test `ROLE_NOMANAGE` (chỉ `PRODUCT_MANAGE`, không có `ROLE_MANAGE`) + tài khoản `nomanage01` gán role này → xác nhận `/admin/products` vẫn `200` nhưng `/admin/users` bị chặn đúng, redirect `/admin/access-denied`. Đã dọn toàn bộ dữ liệu test (`staff02`, `nomanage01`, `ROLE_NOMANAGE`) sau khi verify xong.

## Điều tra: `users` (auth-service) vs `members` (customer-service) có thừa không? (18/08/2026)

Người dùng đặt câu hỏi sau khi hoàn thành tính năng Quản lý tài khoản. Đã điều tra qua code + dữ liệu DB thật:

- **Mục đích ban đầu:** `users` = tài khoản đăng nhập (dùng chung cho cả Admin/Staff và Customer, phân biệt qua `role`); `members` = hồ sơ khách hàng mở rộng (`email`, `gender`) + gốc UUID cho `Addresses`.
- **Phát hiện qua code:** đăng ký khách hàng (`AuthController.processRegister` phía client) tạo **2 bản ghi trùng lặp** (`User` + `Member`, cùng username/password). Toàn bộ luồng Basket (`Cart.memberId`)/Order (`Invoice.memberId`, kiểu `String` chứ không phải UUID) đều dùng thẳng **username**, không hề đụng tới `Member.memberId` (UUID) thật. `Member.memberId` (UUID) chỉ thực sự được dùng cho đúng 1 việc: FK của `Addresses` (xác nhận 10 dòng Address đang liên kết thật qua `sqlcmd`).
- **Phát hiện qua dữ liệu:** `email`/`gender` trên `Member` là **dữ liệu chết** — không UI nào trong app set/sửa được, 2 tài khoản tạo qua đăng ký thật (`user2`, `testuser01`) đều `NULL` 2 field này (chỉ 10 dòng seed sẵn ngoài app mới có). Xác nhận **bug desync có thật**: `testuser01` có password khác nhau giữa `users` (`123`) và `Members` (`Test123`). 13 dòng `Members` nhưng chỉ 6 dòng `users` — đã lệch từ trước.
- **Kết luận:** không hoàn toàn thừa (Address cần `Member.memberId` UUID thật) nhưng đang cõng trách nhiệm không cần thiết (tự lưu username/password riêng, trùng với `users`).

**Quyết định của người dùng sau khi cân nhắc trade-off (mixed-table vs tách bảng):** chọn **tách hẳn 2 loại tài khoản** — lý do: ở quy mô hàng triệu khách hàng, gộp chung với vài chục tài khoản nhân viên trong 1 bảng gây khó quản lý (index, backup, bảo mật, audit). Dẫn tới kế hoạch mới ngay bên dưới.

## Tách 2 loại tài khoản: Staff (auth-service) vs Customer (customer-service) — Kế hoạch mới (18/08/2026)

**Yêu cầu:** tách hẳn tài khoản khách hàng (tự đăng ký trên client để mua hàng) và tài khoản nhân viên nội bộ (chỉ Admin tạo trên dashboard, thu hồi/cấp lại được khi nhân viên nghỉ việc) — không dùng chung 1 bảng `users` như hiện tại.

### Thiết kế cốt lõi

- **Không tách nơi ký JWT** — `auth-service` vẫn là nơi duy nhất ký token (giữ nguyên `jwt.secret` tập trung, tránh nhân bản secret ra nhiều service). Chỉ tách **nơi lưu trữ và xác thực danh tính**:
  - `users` (auth-service) → thu hẹp lại đúng nghĩa **Staff/Admin only** — giữ nguyên 100% Role/Permission đã xây (không đổi tên bảng/entity, không động vào `RoleController`/`PermissionController`/`role_id` — tránh phá lại 5 bước đã build và test xong).
  - `Members` (customer-service) → trở thành **nguồn xác thực khách hàng thật sự** (đã có sẵn `username`/`password`, chỉ cần dùng đúng vai trò thay vì để làm bản sao chết). Khách hàng đăng ký/đăng nhập không còn tạo/đụng tới `users` nữa.
  - `auth-service` xử lý đăng nhập khách hàng bằng cách **gọi sang customer-service** lấy `Member` theo username, so sánh password ngay tại `auth-service`, rồi tự ký JWT (`role="ROLE_USER"` cứng — khách hàng không cần tra Role/Permission vì không có khái niệm permission cho khách).
  - **Hệ quả tốt ăn theo:** xóa luôn nguồn gốc bug desync password đã phát hiện ở mục điều tra trên — vì không còn 2 bản sao credential nữa.
- **Thêm cờ `active` (thu hồi được) vào `users`** — đúng yêu cầu ban đầu của người dùng ("thu hồi nếu nhân viên nghỉ, cấp lại cho nhân viên mới"). Khuyến nghị đi kèm: **không tái dùng chung 1 tài khoản cho người khác** (mất dấu vết ai làm gì) — nên disable tài khoản cũ + tạo tài khoản mới, gán lại đúng Role (đã làm được sẵn qua trang Users).
- **Rà lại `/admin/register`:** hiện là self-registration công khai, mâu thuẫn với mục tiêu "chỉ Admin mới tạo được tài khoản Staff" (đã ghi nhận là tech debt từ trước — "không có giới hạn ai được tự đăng ký Admin"). Đề xuất khóa/xóa route này trong kế hoạch này luôn, vì đây đúng là thời điểm hợp lý để đóng lỗ hổng đó — chỉ tạo Staff qua `/admin/users/new` (đã yêu cầu đăng nhập + `ROLE_MANAGE`).
- **Rủi ro dễ bỏ sót đã rà trước:** `AccountController.changePassword()` (trang "Tài khoản của tôi" - client) hiện đổi mật khẩu ở `users` (auth-service) — sau khi tách, khách hàng không còn dòng trong `users` nữa nên **tính năng này sẽ hỏng ngay** nếu không sửa. `MemberController` (customer-service) hiện **chưa có** endpoint đổi mật khẩu — phải thêm mới.

### Task list

- `[x]` **Bước 1 — SQL migration:**
  - `[x]` `auth-service`: thêm cột `users.active BIT NOT NULL DEFAULT 1`.
  - `[x]` Dọn dữ liệu: đối chiếu `users` × `Members` trước khi xóa (không xóa mù) — chỉ đúng 2 dòng khớp tiêu chí "`ROLE_USER` + có `Member` trùng username": `testuser01` (id=3), `user2` (id=6). Phát hiện thêm 2 dòng mơ hồ không khớp tiêu chí (`test1` id=2, `user1` id=9 — `ROLE_USER` nhưng không có `Member` tương ứng, có thể là tài khoản test cũ từ giai đoạn trước) — đã hỏi người dùng, quyết định **giữ nguyên, không xóa** (chỉ xóa đúng những gì khớp tiêu chí đã xác nhận). Người dùng xác nhận thêm nguyên tắc thiết kế: tài khoản khách hàng không cần phân quyền, chỉ tài khoản nội bộ (Staff) mới cần — khớp đúng thiết kế JWT khách hàng gán cứng `role=ROLE_USER` không tra `roles` table ở Bước 2.
  - Verify: `users` còn đúng 4 dòng (`admin`, `test1`, `admin01`, `user1`), cột `active` mặc định `1` cho tất cả; `Members` không đổi (vẫn 13 dòng, không mất dữ liệu khách hàng — chỉ dọn bản sao trùng bên `users`).
- `[x]` **Bước 2 — Backend `auth-service`:**
  - `[x]` `User` entity thêm field `active`; `AuthController.login()` (đăng nhập Staff) kiểm tra thêm `active == true`, nếu không thì báo "Tài khoản đã bị thu hồi".
  - `[x]` Thêm `POST /api/auth/customer-login`: nhận `{username,password}`, gọi `GET /api/members/username/{username}` (customer-service, đã có sẵn) lấy `Member`, so sánh password, nếu đúng thì ký JWT (`role="ROLE_USER"` cứng, không tra `roles` table). Thêm `MemberAuthDTO` (tối giản, chỉ username/password) và `RestTemplateConfig` (auth-service trước đây chưa từng gọi service khác, cần bean `RestTemplate` mới).
  - `[x]` `POST /api/auth/register` giữ nguyên nhưng chỉ còn ý nghĩa "tạo tài khoản Staff" (đúng thực tế đang dùng qua `/admin/users/new`).
  - Verify qua `curl`: đăng nhập Staff (`admin01`) trả đúng JWT + đủ 5 permission; `customer-login` (`user2`) trả đúng JWT `role=ROLE_USER`, `permissions=[]`; sai mật khẩu và username không tồn tại đều trả lỗi gọn (404 từ customer-service được bắt bằng `RestClientException`, không rò rỉ lỗi); set `active=0` cho `admin01` → đăng nhập bị chặn đúng thông báo "Tài khoản đã bị thu hồi!", revert `active=1` → đăng nhập lại bình thường.
- `[x]` **Bước 3 — Backend `customer-service`:** thêm `PUT /api/members/{id}/password` (nhận `{oldPassword,newPassword}`, so sánh rồi cập nhật) — theo đúng pattern `PUT /api/users/{id}/role` đã làm ở tính năng Users.
  - Verify qua `curl`: đổi mật khẩu `user2` thành công với `oldPassword` đúng; từ chối đúng thông báo "Mật khẩu cũ không đúng!" khi sai; đã revert lại mật khẩu gốc sau khi test.
- `[x]` **Bước 4 — Backend `frontend-service`:**
  - `[x]` `AuthApiService` thêm `customerLogin()` gọi endpoint mới; `CustomerApiService` thêm `changeMemberPassword()`.
  - `[x]` Client `AuthController.processRegister()`: bỏ gọi `authApiService.register()`, chỉ gọi `customerApiService.createMember()`.
  - `[x]` Client `AuthController.processLogin()`: đổi sang gọi `authApiService.customerLogin()`.
  - `[x]` `AccountController.changePassword()`: đổi sang gọi `customerApiService.changeMemberPassword()` thay vì `authApiService.changePassword()`.
  - `[x]` `AdminAuthController` **giữ nguyên hoàn toàn** — không đổi gì (đã đúng thiết kế Staff-only từ trước).
  - `[x]` **Bổ sung ngoài kế hoạch ban đầu (`customer-service`):** thêm `MemberRepository.existsByUsername()` + check trùng username trong `MemberController.createMember()` — khi bỏ `authApiService.register()` khỏi luồng đăng ký khách hàng, cũng mất luôn bước kiểm tra trùng username mà trước đây `auth-service` đảm nhiệm; xác nhận `Members.username` không có ràng buộc unique nào ở DB, nên nếu bỏ qua sẽ tạo được nhiều tài khoản trùng username.
  - **Lỗi phát hiện trong lúc dán code (2 lần):**
    1. Lần dán đầu tiên, 3/6 file (`MemberController.java`, `AuthApiService.java`, `CustomerApiService.java`) **không thực sự được cập nhật** dù người dùng báo "đã dán xong" — bản thân tôi cũng verify sai ở vòng đầu (dùng `grep -c "customerApiService"` trên `AuthController.java` và thấy 2 kết quả rồi kết luận nhầm là đã đúng, trong khi chuỗi đó vốn có sẵn cả ở bản cũ lẫn bản mới, không phải dấu hiệu phân biệt được). Phải chuyển sang verify bằng marker **riêng biệt thật sự** của bản mới (vd. tìm đoạn comment mới, hoặc xác nhận đoạn code cũ đã biến mất) mới phát hiện ra. Test thực tế (đăng ký qua `/register`) lộ ra ngay: `users` tăng từ 4 lên 5 dòng dù lẽ ra phải giữ nguyên — xác nhận `AuthController.java` (client) vẫn chạy code cũ.
    2. Sau khi dán đúng `AuthController.java`, phát hiện tiếp bug tiềm ẩn có sẵn từ trước (không phải do tôi gây ra): `AccountController.changePassword()`/`addAddress()` dùng `@RequestParam String x` không khai tên tường minh → lỗi `IllegalArgumentException: Name for argument ... not specified` khi chạy qua `mvn` dòng lệnh (thiếu cờ compiler `-parameters`) — dù code này chưa từng bị tôi sửa method signature. Fix: thêm tên tường minh `@RequestParam("oldPassword")` cho cả 4 tham số trong file (rà toàn bộ `frontend-service` bằng `Grep`, xác nhận chỉ có đúng 2 chỗ bị pattern này).
  - Verify cuối cùng qua `curl` end-to-end: đăng ký `newcustomer02` qua `/register` → `users` giữ nguyên 4 dòng (không tạo thêm), chỉ tạo `Member` mới; đăng nhập qua `/login` → JWT đúng, sai mật khẩu bị từ chối; đổi mật khẩu qua `/account/change-password` → cập nhật đúng bên `Members`, đăng nhập lại bằng mật khẩu mới thành công; thêm địa chỉ qua `/account/addresses/add` → lưu đúng, xác nhận qua `GET /api/addresses/member/{id}`. Đã dọn toàn bộ dữ liệu test, `users`/`Members` về đúng baseline (4/13).
- `[x]` **Bước 5 — Giao diện:**
  - `[x]` **Phần A (toggle Active):** `UserSummaryDTO` (cả 2 service) thêm field `active`; `auth-service UserController` thêm `PUT /api/users/{id}/active`; `frontend-service UserApiService` thêm `updateActive()`; `admin/users.html` thêm cột "Trạng thái" (badge Active/Đã thu hồi, chỉ hiển thị); `admin/user-role-form.html` thêm checkbox `active` — `AdminController.saveUserRole()` lưu cả role lẫn active trong cùng 1 lần submit (không tách 2 action riêng, tránh 2 nguồn ghi cho cùng 1 dữ liệu).
  - `[x]` **Phần B (khóa `/admin/register`):** xóa hẳn `showRegisterForm()`/`processRegister()` khỏi `AdminAuthController`; bỏ link "Sign up" khỏi `admin/login.html`; bỏ `/admin/register` khỏi danh sách loại trừ trong `AppConfig` — hệ quả tự nhiên: route này giờ bị `AdminAuthInterceptor` chặn (redirect `/admin/login`) vì không còn nằm trong exclude list, cộng thêm việc mapping cũng đã bị xóa hẳn (404 nếu có ai vượt qua được interceptor).
  - Verify qua `curl`: `GET`/`POST /admin/register` đều redirect `/admin/login` (khóa thành công, không còn public); `GET /api/users` trả đúng field `active` cho cả 4 tài khoản; toggle active của `test1` qua đúng form thật (`POST /admin/users/save`) — tắt thành `false`, bật lại thành `true`, đều đúng; kịch bản đầy đủ "thu hồi/cấp lại" với `admin01` qua đúng giao diện thật (không SQL trực tiếp): thu hồi → `POST /api/auth/login` trả "Tài khoản đã bị thu hồi!" → cấp lại → đăng nhập thành công trở lại.

Sẽ làm tuần tự từng bước, mỗi bước dán code → review → sang bước tiếp, giữ đúng quy trình đã áp dụng xuyên suốt dự án.

**Tổng kết:** cả 5/5 bước của kế hoạch "Tách 2 loại tài khoản: Staff (auth-service) vs Customer (customer-service)" đã hoàn thành và test end-to-end đầy đủ. Khách hàng và nhân viên nội bộ giờ có 2 nguồn dữ liệu/xác thực hoàn toàn tách biệt (`users` vs `Members`), trong khi vẫn dùng chung 1 nơi ký JWT (`auth-service`) để tránh nhân bản secret. Tài khoản Staff có thể thu hồi/cấp lại qua cờ `active`; đăng ký Staff công khai đã bị khóa, chỉ Admin (có `ROLE_MANAGE`) mới tạo được tài khoản Staff mới.

## Fix bug giỏ hàng bị xóa sớm + điều tra luồng trạng thái đơn hàng (19/08/2026)

**Bug phát hiện qua ảnh chụp người dùng (trang Order History thấy PAID/PAID/PENDING, và modal "My Cart"):** hệ thống có **2 giỏ hàng riêng biệt** — giỏ hàng phía client (`jquery.mycart.js`, `localStorage`, điều khiển modal "My Cart") và giỏ hàng phía server (Redis, qua `BasketApiService`). Backend (Redis) vốn đã đúng — chỉ xóa ở `CheckoutController.paymentSuccess()` sau khi `capturePaypalOrder()` xác nhận thành công thật. Nhưng `jquery.mycart.js` (thư viện bên thứ 3 có sẵn từ template) tự ý gọi `ProductManager.clearProduct()` **ngay khi bấm nút Checkout trong modal** — xóa giỏ hàng phía client dù thanh toán chưa xong hay bị hủy giữa chừng.

- `[x]` Bỏ dòng `ProductManager.clearProduct()` khỏi handler bấm nút Checkout trong `jquery.mycart.js` (comment lại, không xóa hẳn dòng để dễ đối chiếu).
- `[x]` Thêm script vào `client/order-success.html` (trang chỉ render sau khi `paymentSuccess()` xác nhận PayPal capture `COMPLETED` thật) — xóa `localStorage.products` và reset badge tại đây thay vào.
- Verify trên browser thật: (a) thêm hàng → bấm Checkout → chưa thanh toán → quay về trang chủ → xác nhận badge/`localStorage` **còn nguyên** (trước fix sẽ về 0 ngay khi bấm Checkout); (b) mô phỏng đúng script của trang success → xác nhận `localStorage` về `[]`, badge về `0`, và **giữ nguyên sau khi reload** (xóa thật, không phải chỉ đổi tạm trên UI).

**Điều tra luồng PAID/PENDING (theo yêu cầu người dùng, trả lời trực tiếp không qua code):** lần theo đúng code thật — `PENDING` là giá trị mặc định của `Invoice.status` lúc tạo hóa đơn (`POST /api/orders/checkout`, trước khi sang PayPal); `PAID` chỉ được set khi `PaymentEventListener` (order-service) nhận sự kiện RabbitMQ `payment.success` do `PaymentEventPublisher` (payment-service) bắn ra, và **chỉ bắn khi PayPal capture trả về `"COMPLETED"` thật** — không có chỗ nào set PAID sớm hơn. Đơn PENDING trong ảnh là 1 lượt checkout bỏ dở, đúng thiết kế hiện tại — không phải bug.

## Thêm trạng thái CANCELLED/FAILED cho đơn hàng — Kế hoạch mới (19/08/2026)

**Bối cảnh:** từ câu hỏi "khi nào đơn hàng CANCELLED/FAILED trong e-commerce thực tế", đối chiếu với code hiện tại phát hiện: hệ thống **chưa từng** set `Invoice.status` thành CANCELLED/FAILED — đơn bỏ dở/thất bại sẽ treo ở PENDING vĩnh viễn.

### Hiện trạng (khảo sát trước khi lập kế hoạch — tin vui: hạ tầng đã có sẵn phần lớn)

- `payment-service` **đã có sẵn** gần như đầy đủ: `Payment.status` (bảng riêng của payment-service, khác `Invoice.status` bên order-service) đã hỗ trợ `CANCELLED`/`FAILED`/`REFUNDED`; `PayPalService.capturePayment()` đã set `FAILED` khi PayPal capture không trả `COMPLETED`; `PayPalService.cancelPayment()` đã set `CANCELLED`; endpoint `POST /api/payments/orders/{orderId}/cancel` **đã tồn tại và hoạt động** (chỉ chưa ai gọi tới).
- **Gap thật sự:** các trạng thái trên chỉ cập nhật trong bảng `Payments` (payment-service, phục vụ riêng payment-service) — **không bao giờ đồng bộ sang `Invoices`** (order-service, bảng thật sự hiển thị ở Order History/Admin Orders) vì `PaymentController` hiện chỉ publish RabbitMQ event ở đúng 1 nhánh (COMPLETED→PAID trong `captureOrder()`), nhánh FAILED và toàn bộ `cancelOrder()` đều không publish gì.
- `CheckoutController.paymentCancel()` (frontend, endpoint PayPal redirect về khi user bấm hủy) **hiện không hề gọi** endpoint cancel đã có sẵn — chỉ hiển thị message tĩnh, không nhận cả `token` (PayPal order id) từ query param.
- `admin/orders.html` **đã có sẵn badge cho CANCELLED** (chờ sẵn từ một giai đoạn trước, chưa từng dùng tới) nhưng **thiếu badge cho FAILED** — nếu không thêm, status FAILED sẽ hiện ô trống trong bảng.
- `PaymentEventListener` (order-service) **đã tổng quát hóa sẵn** — đọc `status` từ event map rồi set thẳng vào Invoice, không hardcode "PAID" — không cần sửa gì ở order-service.

### Quyết định thiết kế

- **Tái sử dụng đúng cơ chế RabbitMQ hiện có** (cùng exchange `ecommerce.exchange`, cùng routing key `payment.success`) — không đổi RabbitMQ topology, giảm rủi ro. Chỉ tổng quát hóa `PaymentEventPublisher.publishPaymentSuccess(invoiceId)` (hardcode "PAID") thành `publishPaymentStatus(invoiceId, status)` (nhận status tùy ý).
- Không đổi schema DB (`Invoice.status`/`Payment.status` đã là String tự do từ đầu, không ràng buộc enum).
- `PayPalService.cancelPayment()` đổi kiểu trả về từ `void` sang `Payment` — để `PaymentController.cancelOrder()` lấy được `invoiceId` publish event (hiện tại không trả gì nên không có invoiceId để publish).

### Task list

- `[x]` **Bước 1 — `payment-service`:** tổng quát hóa `PaymentEventPublisher` (`publishPaymentSuccess` → `publishPaymentStatus(invoiceId, status)`); `PaymentController.captureOrder()` publish `"FAILED"` ở nhánh else (hiện chỉ trả lỗi, không publish); `PayPalService.cancelPayment()` đổi trả về `Payment`; `PaymentController.cancelOrder()` publish `"CANCELLED"` sau khi gọi `cancelPayment()`.
- `[x]` **Bước 2 — `frontend-service`:** `PaymentApiService` thêm `cancelPaypalOrder(paypalOrderId)` (theo đúng pattern `capturePaypalOrder()` đã có); `CheckoutController.paymentCancel()` nhận thêm `@RequestParam(value="token", required=false)` (PayPal luôn gửi kèm khi redirect về cancel_url), gọi `paymentApiService.cancelPaypalOrder()` trước khi hiển thị trang hủy.
- `[x]` **Bước 3 — Giao diện:** thêm badge `FAILED` (`badge-dark`) vào `admin/orders.html`, cạnh 3 badge PAID/PENDING/CANCELLED đã có sẵn.
- `[x]` **Bug phát sinh khi test (đã vá):** nhánh FAILED ban đầu không hoạt động với lỗi capture phổ biến nhất trong thực tế — PayPal trả HTTP non-2xx (VD: 422 `ORDER_NOT_APPROVED`) khiến `RestTemplate.postForEntity()` ném exception **trước khi** code chạy tới đoạn set `"FAILED"`, exception bị `@ExceptionHandler` chung nuốt mất, `Payment.status` kẹt ở `CREATED`, event không được bắn. Fix: bọc try/catch quanh lời gọi PayPal capture trong `PayPalService.capturePayment()`, bắt exception → set `FAILED` → save → trả về `Payment` bình thường (không rethrow), để `captureOrder()` chạy đúng nhánh publish `FAILED` sẵn có.
- `[x]` Test end-to-end bằng `curl` (không dùng UI thật vì khó giả lập PayPal sandbox tương tác):
  - Tạo Payment test → `POST /api/payments/orders/{id}/cancel` → xác nhận `Payments.status` (payment-service DB) = `CANCELLED`, log RabbitMQ publish đúng, `PaymentEventListener` (order-service) nhận event và xử lý an toàn (bắt exception khi `invoiceId` test không phải UUID hợp lệ, không crash consumer).
  - Tạo Payment test → `POST /api/payments/orders/{id}/capture` khi order chưa được PayPal approve (mô phỏng thanh toán thất bại thật) → lần đầu phát hiện bug trên (status vẫn `CREATED`) → sau khi vá, test lại xác nhận `Payments.status` = `FAILED` và event `FAILED` được publish đúng.
  - Set tạm 1 dòng `Invoices` thật sang `FAILED` qua SQL → xác nhận badge `FAILED` render đúng trên `/admin/orders` (qua accessibility tree, đăng nhập bằng tài khoản `admin`) → khôi phục lại `PAID` ngay sau đó.
  - Dọn sạch toàn bộ dữ liệu test (`DELETE` các dòng `Payments` test) sau khi verify xong.
  - Restart cả `payment-service` và `frontend-service` (kill tiến trình `mvn spring-boot:run` cũ qua PID, chạy lại nền) để áp dụng code mới; cả 2 lên thành công, không lỗi.

**Tổng kết:** cả 3/3 bước + 1 bug phát sinh khi test đều đã hoàn thành, compile sạch, restart thành công, và verify end-to-end đầy đủ qua `curl` + DB + UI thật. Đơn hàng giờ có đủ 4 trạng thái PAID/PENDING/CANCELLED/FAILED, đồng bộ đúng từ payment-service sang order-service qua RabbitMQ.

## Fix bug đụng session giữa tài khoản Staff (dashboard) và Customer (client) (19/08/2026)

**Bug người dùng báo cáo:**
1. Login dashboard bằng tài khoản nhân viên nội bộ (VD: `admin01`) → trang client tự động "đồng bộ" theo tài khoản đó.
2. Login trang client bằng tài khoản khách hàng để mua hàng → dashboard tự động logout tài khoản nhân viên nội bộ.

Yêu cầu: trang client và trang dashboard phải hoàn toàn tách biệt, tài khoản khách hàng và nhân viên nội bộ không có mối liên hệ gì với nhau — đúng tinh thần kế hoạch "Tách 2 loại tài khoản" đã làm trước đó trong session.

**Điều tra + tái hiện (bằng `curl` với cookie jar dùng chung, mô phỏng đúng 1 trình duyệt):**
- Login `/admin/login` (staff) → gọi `/account` (client) bằng cùng cookie → trả về **200** thay vì redirect `/login` → xác nhận bug 1.
- Sau đó login `/login` (customer) bằng cùng cookie → gọi lại `/admin/orders` → trang vẫn load (do "permissions" không đổi) nhưng **topbar admin đổi tên hiển thị** từ `admin` sang tên khách hàng vừa login → đây chính là hiện tượng người dùng thấy như "dashboard tự động logout".

**Nguyên nhân gốc:** `AdminAuthController` (dashboard) và client `AuthController` (khách hàng) là 2 luồng xác thực khác nhau (JWT auth-service vs Members customer-service) nhưng cùng chạy trong **1 ứng dụng** `frontend-service` → dùng chung **1 `HttpSession`** (chung `JSESSIONID`, cookie `Path=/`). Cả 2 controller cùng ghi đè lên **cùng tên session key** (`"username"`, `"role"`, `"jwtToken"`) — ai login sau đè thông tin người login trước.

**Fix:** đổi namespace session phía Admin/Staff sang `adminUsername`/`adminRole`/`adminJwtToken` (giữ nguyên `username`/`role`/`jwtToken` phía client — do "permissions" vốn đã là key riêng của Admin, không cần đổi). Đồng thời đổi cả 2 nút "Đăng xuất" từ `session.invalidate()` (xóa toàn bộ session) sang `session.removeAttribute()` từng key đúng phạm vi của mình, để đăng xuất bên này không xóa luôn phiên đăng nhập bên kia nếu cùng mở 1 trình duyệt.

- `[x]` `AdminAuthController.java`: đổi 3 key session login; logout dùng `removeAttribute`.
- `[x]` `admin/fragments/_header.html`: `${session.username}` → `${session.adminUsername}`.
- `[x]` `AuthController.java` (client): giữ nguyên key login; logout dùng `removeAttribute` (thêm xóa `memberId`).
- `[x]` Compile sạch, restart `frontend-service` (cần restart toàn bộ vì đổi cả fragment `_header.html`).
- `[x]` Test end-to-end bằng `curl` + cookie jar dùng chung, đủ 6 kịch bản: login admin không lộ qua client; topbar admin giữ đúng tên sau khi khách login; khách login thấy đúng tên mình; đăng xuất khách không ảnh hưởng phiên admin; đăng xuất admin không ảnh hưởng phiên khách. Tất cả đều đúng như thiết kế.
- `[x]` Dọn tài khoản test (`repro_test_cust`) tạo ra trong lúc test.

**Giới hạn đã biết:** đây là tách theo session-key (logic), không phải tách theo session-storage vật lý — 2 loại tài khoản vẫn chung 1 `HttpSession`/`JSESSIONID` do cùng 1 app `frontend-service`. Muốn tách hoàn toàn ở tầng hạ tầng (2 cookie/session độc lập) cần tách `admin` thành 1 service/app riêng — không cần thiết ở quy mô đồ án hiện tại.

## Bổ sung Hủy đơn hàng + Hoàn tiền (có duyệt Admin) — Kế hoạch mới (19/08/2026)

**Bối cảnh:** người dùng phát hiện không có nút "Hủy đơn hàng" ở cả client lẫn dashboard. Điều tra xác nhận: `order-service` (`OrderController`) hiện chỉ có `checkout` (tạo) + 2 API xem danh sách — **không có bất kỳ endpoint nào đổi trạng thái Invoice sau khi tạo**. `PayPalService.refundPayment()` bên payment-service đã có sẵn logic gọi PayPal hoàn tiền nhưng chưa từng được nối dây (không có nút bấm, không đồng bộ ngược `Invoice.status` qua RabbitMQ).

**Quyết định nghiệp vụ** (đã hỏi và chốt với người dùng): hoàn tiền theo luồng **có Admin duyệt** (đúng chuẩn các sàn thực tế Shopee/Lazada/Amazon), không tự động hoàn ngay khi khách bấm yêu cầu.

### State machine hoàn chỉnh (thêm 2 trạng thái mới: `REFUND_REQUESTED`, `REFUNDED`)

- `PENDING` → **[Hủy đơn]** (khách hoặc Admin) → `CANCELLED`. Chỉ đổi trạng thái trực tiếp ở order-service, **không gọi payment-service** (chưa có tiền bị trừ nên không cần hoàn; PayPal order chưa capture sẽ tự hết hạn bên phía PayPal, không ảnh hưởng khách).
- `PAID` → **[Khách: Yêu cầu hoàn tiền]** → `REFUND_REQUESTED` (chỉ đổi trạng thái, chưa gọi PayPal).
- `REFUND_REQUESTED` → **[Admin: Duyệt hoàn tiền]** → gọi PayPal refund thật (payment-service) → `REFUNDED`.
- `REFUND_REQUESTED` → **[Admin: Từ chối]** → về lại `PAID` (chỉ đổi trạng thái).
- `PAID` → **[Admin: Hoàn tiền trực tiếp]** (không cần khách yêu cầu trước, VD xử lý khiếu nại qua điện thoại) → gọi PayPal refund thật → `REFUNDED`.
- `CANCELLED`/`FAILED`/`REFUNDED` → trạng thái cuối, không còn hành động nào.

### Task list

- `[x]` **Bước 1 — `order-service`:** thêm 3 endpoint vào `OrderController`: `PUT /api/orders/{id}/cancel` (validate status hiện tại phải `PENDING`, set `CANCELLED`), `PUT /api/orders/{id}/request-refund` (validate `PAID`, set `REFUND_REQUESTED`), `PUT /api/orders/{id}/reject-refund` (validate `REFUND_REQUESTED`, set về `PAID`). Mỗi endpoint trả 400 nếu trạng thái hiện tại không hợp lệ để chuyển.
- `[x]` **Bước 2 — `payment-service`:** `PaymentRepository` thêm `findByInvoiceId(String)`; `PayPalService.refundPayment()` đổi tham số từ `paymentId` (UUID nội bộ) sang `invoiceId` (tra cứu Payment qua `findByInvoiceId`, gọi PayPal refund bằng `captureId` sẵn có, set `Payment.status=REFUNDED`, trả về `Payment`); `PaymentController` đổi route `/{paymentId}/refund` → `/invoice/{invoiceId}/refund`, publish RabbitMQ `"REFUNDED"` qua `PaymentEventPublisher` (đã tổng quát hóa sẵn từ đợt CANCELLED/FAILED) sau khi refund thành công — Invoice.status tự đồng bộ qua `PaymentEventListener` có sẵn, không cần sửa order-service thêm.
- `[x]` **Bước 3 — `frontend-service` (service layer):** `OrderApiService` thêm `cancelOrder(id)`, `requestRefund(id)`, `rejectRefund(id)`; `PaymentApiService` thêm `refundPaypalOrder(invoiceId)` (gọi endpoint mới ở Bước 2).
- `[x]` **Bước 4 — `AccountController` (client):** thêm `POST /account/orders/{id}/cancel` và `POST /account/orders/{id}/request-refund` — verify đơn thuộc đúng khách hàng đang đăng nhập (check qua `orderApiService.getOrdersByMember(username)`) trước khi gọi API, chặn khách sửa URL để hủy/hoàn tiền đơn của người khác.
- `[x]` **Bước 5 — `AdminController` (dashboard):** thêm `POST /admin/orders/{id}/cancel`, `POST /admin/orders/{id}/refund` (gọi payment-service thật), `POST /admin/orders/{id}/reject-refund`. Không cần thêm permission mapping mới vì `/admin/orders/**` đã được `AdminAuthInterceptor` gán `ORDER_MANAGE` từ trước.
- `[x]` **Bước 6 — Giao diện:**
  - `client/account.html`: thay ô trạng thái tĩnh bằng nút hành động theo status — `PENDING` → nút "Hủy đơn hàng"; `PAID` → nút "Yêu cầu hoàn tiền"; `REFUND_REQUESTED` → text "Đang chờ duyệt hoàn tiền"; còn lại chỉ hiện text status. Thêm hiển thị `orderMessage`/`orderError`.
  - `admin/orders.html`: thêm badge `REFUND_REQUESTED` (`badge-info`) và `REFUNDED` (`badge-secondary`); thêm cột hành động — `PENDING` → nút "Hủy đơn"; `PAID` → nút "Hoàn tiền"; `REFUND_REQUESTED` → 2 nút "Duyệt hoàn tiền" + "Từ chối".
- `[x]` Test end-to-end bằng `curl` + verify UI thật:
  - Tạo Invoice test PENDING → cancel → verify `CANCELLED`; thử cancel lần 2 → đúng 400 (chặn chuyển trạng thái sai).
  - Dùng đơn PAID thật đã capture từ trước (`972b4bf4`, antk3, 88.000đ — do người dùng chọn để test thay vì tạo mới, vì cần 1 giao dịch PayPal đã capture thật): request-refund → verify `REFUND_REQUESTED` → reject-refund → verify về lại `PAID` (test nhánh từ chối) → request-refund lại → Admin duyệt (gọi PayPal refund thật) → verify cả `Payment.status` (payment-service) và `Invoice.status` (order-service, qua RabbitMQ) đều thành `REFUNDED`.
  - Test 2 edge-case chặn sai trạng thái: request-refund trên đơn đã REFUNDED → 400; reject-refund trên đơn không phải REFUND_REQUESTED → 400.
  - Test ownership: tạo 2 member test A/B, B cố hủy đơn của A qua `/account/orders/{id}/cancel` → bị chặn (redirect về `/account`, đơn A vẫn nguyên trạng thái); A tự hủy đơn của mình → thành công.
  - Verify UI thật trên `/admin/orders` (đăng nhập `admin`): badge `REFUNDED` hiển thị đúng và không còn nút hành động (terminal state); đơn `PAID` hiển thị đúng nút "Hoàn tiền".
  - Restart cả 3 service (`order-service`, `payment-service`, `frontend-service`) để áp dụng code mới — cả 3 lên thành công.
  - Dọn toàn bộ dữ liệu test (invoice, invoice_details, members) tạo ra trong lúc test. Riêng đơn `972b4bf4` giữ nguyên `REFUNDED` vì phản ánh đúng thực tế (tiền sandbox đã hoàn thật qua PayPal, set giả lại PAID sẽ sai lệch dữ liệu).

**Tổng kết:** cả 6/6 bước đã hoàn thành, compile sạch, restart thành công, và verify end-to-end đầy đủ (curl + DB + UI thật) bao gồm cả state machine, edge-case chặn sai trạng thái, và ownership check chống khách hủy/hoàn tiền đơn của người khác. Đơn hàng giờ có đủ cơ chế Hủy đơn (PENDING, tự động) và Hoàn tiền (PAID, có Admin duyệt) đúng chuẩn nghiệp vụ e-commerce thực tế.

### Fix UI bị bể trên trang `/account` sau khi thêm cột "Hành động" (19/08/2026)

Người dùng báo bảng "Lịch sử đơn hàng" bị bóp méo (header chữ xuống dòng lộn xộn, nút bấm bị chèn ép) kèm ảnh chụp thật. Nguyên nhân: `.main-agileits` (class bọc toàn bộ nội dung trang Tài khoản) cố định `width: 30%` trong `style.css` — vốn thiết kế cho form đăng nhập hẹp; trang Tài khoản dùng lại đúng class này cho cả bảng đơn hàng. Bảng gốc 5 cột hẹp còn vừa, nhưng thêm cột "Hành động" (nút "Yêu cầu hoàn tiền" khá dài) thì tràn/bóp méo.

- Thử fix đầu tiên (thêm `<style>` override trong `<head>` của `account.html`) **không có tác dụng** — phát hiện Thymeleaf Layout Dialect chỉ merge riêng `<title>` từ head trang con vào layout, các thẻ khác trong `<head>` (kể cả `<style>`) bị âm thầm bỏ qua khi dùng `layout:decorate`. Đây là hành vi cần nhớ cho các trang dùng layout dialect này sau này.
- Fix đúng: bỏ khối `<style>` ở head, chuyển sang inline `style="width:90%; max-width:1100px;"` ngay trên `<div class="main-agileits">` (nằm trong `layout:fragment="content"`, luôn được render đúng).
- Cần chạy `mvn compile` để đồng bộ `src/main/resources/templates` → `target/classes` (nơi Thymeleaf thực đọc) — xác nhận qua `grep` trực tiếp trên `target/classes` trước/sau khi compile. Không cần restart service (chỉ sửa nội dung trang thường, không phải fragment `_header`/`_layout`).
- Verify: tạo 1 member test với 4 invoice (PENDING/PAID/REFUND_REQUESTED/CANCELLED), đăng nhập qua JS submit form (browser tool bị flaky với nút submit của form này, đã gặp trước đó trong session), xác nhận qua accessibility tree cả 4 dòng render đúng nút/text theo đúng trạng thái và không còn bóp méo. Dọn dữ liệu test sau khi xong.

## Tồn kho (Inventory) — Microservice mới + Saga Pattern (Choreography) — Kế hoạch mới (19/08/2026)

**Yêu cầu người dùng:**
1. Nghiệp vụ: khi đặt hàng thành công, trừ số lượng sản phẩm trong đơn khỏi bảng tồn kho.
2. Kỹ thuật: tạo 1 service riêng (`inventory-service`), áp dụng Saga pattern để đảm bảo toàn vẹn dữ liệu khi luồng xử lý thất bại.

**Quyết định kiến trúc (đã thảo luận và chốt với người dùng):** ban đầu đề xuất Saga kiểu Orchestration (order-service gọi đồng bộ gRPC sang inventory-service ngay lúc checkout) vì không tốn công sửa UX. Người dùng hỏi thẳng "Choreography có báo hết hàng ngay được không?" — trả lời trung thực: **được, nhưng phải trả giá** (thêm state trung gian, thêm endpoint status, thêm cơ chế chờ). Người dùng chọn **chấp nhận trả giá, đi theo Choreography-based Saga đầy đủ** — đúng bản chất Saga hơn (các service không gọi trực tiếp nhau, chỉ giao tiếp qua event, dễ mở rộng thêm participant sau này mà không đụng code các service cũ).

**Insight quan trọng giúp giảm "giá phải trả":** cơ chế "chờ" không cần lộ ra ngoài trình duyệt. Luồng `/checkout/pay` hiện tại vốn đã là 1 request đồng bộ duy nhất xử lý ở `CheckoutController` (frontend-service) — nên việc "chờ kết quả trừ kho" có thể làm **ngầm ở tầng server** (poll nội bộ giữa frontend-service ↔ order-service trong lúc xử lý request), khách vẫn chỉ thấy bấm nút → đợi chút → sang PayPal hoặc thấy lỗi hết hàng, y hệt UX hiện tại, không cần sửa JS phía client.

### Kiến trúc luồng

```
1. Checkout (forward path — Choreography):
   Frontend --POST /checkout/pay--> CheckoutController (frontend-service)
     --> orderApiService.createOrder() --> OrderController.checkout() (order-service)
           - verify giá qua gRPC (catalog-service) - GIỮ NGUYÊN như hiện tại
           - lưu Invoice status = PENDING_INVENTORY (state MỚI)
           - publish event "order.created" {orderId, items:[{productId,quantity}]}
           - trả Invoice về ngay (không chờ) <-- vẫn đồng bộ, không đổi contract

   inventory-service lắng nghe "order.created":
           - @Transactional: với từng item, UPDATE ... SET quantity = quantity - :qty
             WHERE productId = :id AND quantity >= :qty (atomic, tránh race condition
             khi 2 đơn cùng mua sản phẩm cuối cùng)
           - nếu 1 item thiếu hàng -> throw exception -> rollback TOÀN BỘ transaction
             (không trừ dở dang) -> publish "inventory.result" {orderId, status:"FAILED"}
           - nếu tất cả đủ hàng -> publish "inventory.result" {orderId, status:"RESERVED"}

   order-service lắng nghe "inventory.result":
           - RESERVED -> Invoice.status = PENDING (như cũ - nghĩa là "đã xác nhận, chờ thanh toán")
           - FAILED   -> Invoice.status = OUT_OF_STOCK (state MỚI, terminal)

   CheckoutController (frontend-service), SAU KHI orderApiService.createOrder() trả về:
           - poll GET /api/orders/{id} (endpoint MỚI) mỗi ~200ms, tối đa ~5s,
             cho tới khi status != PENDING_INVENTORY
           - PENDING -> tiếp tục y như cũ (tạo PayPal order, redirect)
           - OUT_OF_STOCK hoặc hết timeout -> redirect /checkout?error=out_of_stock,
             KHÔNG tạo PayPal order

2. Compensating transaction (rollback path — khi đơn đã trừ kho rồi mới hỏng):
   Khi Invoice chuyển sang CANCELLED / FAILED / REFUNDED (qua PaymentEventListener
   đã tổng quát hóa sẵn từ trước, HOẶC qua endpoint cancelOrder() PENDING->CANCELLED)
   -> order-service publish "inventory.restore" {orderId, items: đọc từ invoice.getDetails()}
   -> inventory-service lắng nghe, UPDATE quantity = quantity + :qty cho từng item
      (hoàn kho - đây chính là "compensating transaction" của Saga)
   Lưu ý: OUT_OF_STOCK KHÔNG cần hoàn kho vì chưa từng trừ được gì (transaction đã rollback
   ngay từ đầu). PAID cũng không hoàn kho (giao dịch thành công, giữ nguyên đã trừ).
```

### Naming cụ thể (theo đúng convention hiện có: exchange dùng chung `ecommerce.exchange`, routing key dạng `domain.action`, queue dạng `service.domain.action.queue`)

- Exchange: tái sử dụng `RabbitMQConfig.EXCHANGE_NAME = "ecommerce.exchange"` **đúng bản có dấu chấm** của order-service (KHÔNG copy theo basket-service).
- Routing key mới: `order.created`, `inventory.result`, `inventory.restore`.
- Queue mới: `inventory.order.created.queue`, `inventory.restore.queue` (cả 2 do inventory-service khai báo+lắng nghe), `order.inventory.result.queue` (order-service khai báo+lắng nghe).
- Invoice.status thêm 2 giá trị mới: `PENDING_INVENTORY` (khởi tạo, thay cho việc nhảy thẳng vào PENDING như hiện tại), `OUT_OF_STOCK` (terminal, hết hàng).

### ⚠️ Phát hiện phụ (ngoài phạm vi, đã ghi nhận, KHÔNG sửa trong kế hoạch này)

Lúc khảo sát RabbitMQ hiện có, phát hiện **bug tên exchange lệch nhau**: `order-service` dùng `"ecommerce.exchange"` (dấu chấm) nhưng `basket-service` lại khai báo `"ecommerce_exchange"` (gạch dưới) cho đúng cùng 1 mục đích (basket-service lắng nghe `order.placed` để tự xóa giỏ hàng Redis khi đặt hàng). Đây là 2 exchange **khác nhau thật sự** trong RabbitMQ nên event `order.placed` hiện KHÔNG BAO GIỜ tới được basket-service qua đường này — may là việc xóa giỏ hàng thực tế đã hoạt động qua đường khác (gọi trực tiếp `basketApiService.deleteBasket()` trong `CheckoutController.paymentSuccess()`), nên không phải bug ảnh hưởng người dùng, nhưng là dead code / cấu hình sai tiềm ẩn. Sẽ tách thành task riêng, không xử lý trong kế hoạch Tồn kho này.

### Task list

- `[x]` **Bước 1 — Khởi tạo `inventory-service`:** scaffold project mới (pom.xml, `application.properties`: port `8086`, datasource trỏ vào `EcommerceDB` dùng chung — đúng pattern hiện có, không tạo DB riêng; cấu hình RabbitMQ giống order-service), main Application class, entity `Stock` (`productId` String PK, `quantity` Integer, `updatedAt` Date), table `Stocks`, repository với 2 method atomic: `deductIfAvailable` (UPDATE có điều kiện, trả về số dòng bị ảnh hưởng) và `restore` (UPDATE cộng dồn).
- `[x]` **Bước 2 — `inventory-service`: xử lý Saga:** `RabbitMQConfig` (exchange `ecommerce.exchange`, 2 queue mới); listener `order.created` — `@Transactional`, trừ kho atomic từng item, rollback toàn bộ nếu bất kỳ item nào thiếu hàng, publish `inventory.result`; listener `inventory.restore` — cộng trả lại kho. Bug phát sinh: 2 bean `Queue` cùng kiểu trong `RabbitMQConfig` khiến Spring không tự inject qua tham số được (thiếu cờ compiler `-parameters`, cùng nguyên nhân với lỗi `@RequestParam` gặp trước đó) — fix bằng cách gọi trực tiếp method thay vì nhận qua tham số. Test độc lập bằng cách publish message giả trực tiếp qua RabbitMQ Management API (chưa cần đợi order-service ở Bước 3): (1) đủ hàng → trừ đúng + publish RESERVED; (2) thiếu hàng → không trừ + publish FAILED; (3) đơn nhiều sản phẩm, 1 sản phẩm thiếu hàng → rollback toàn bộ kể cả sản phẩm đủ hàng (all-or-nothing đúng thiết kế); (4) inventory.restore → cộng trả đúng số lượng. Cả 4 test đều đạt.
- `[x]` **Bước 3 — `order-service`:** thêm state `PENDING_INVENTORY`/`OUT_OF_STOCK`; sửa `checkout()` set status khởi tạo = `PENDING_INVENTORY` và publish `order.created` (kèm danh sách productId+quantity) thay vì chỉ publish `order.placed` như cũ (giữ nguyên `order.placed` cho basket-service, không đụng); thêm `GET /api/orders/{id}` (đơn lẻ, phục vụ polling); thêm `InventoryEventListener` (nhận `inventory.result`, cập nhật Invoice.status); thêm `InventoryEventPublisher` (bắn `inventory.restore`, đọc lại `invoice.getDetails()` để lấy danh sách item); gắn gọi publisher này vào `PaymentEventListener` (khi status mới là CANCELLED/FAILED/REFUNDED) và `cancelOrder()` (PENDING→CANCELLED). Bug phát sinh: chủ động vá trước bug "2 bean Queue cùng kiểu" (giống hệt bug gặp ở inventory-service Bước 2) trong `RabbitMQConfig.java` do file này giờ có thêm 1 Queue mới. Test end-to-end thật (không giả lập) qua `POST /api/orders/checkout` thật: (1) đủ hàng → `PENDING_INVENTORY`→`PENDING`, tồn kho trừ đúng; (2) hủy đơn `PENDING`→`CANCELLED` → tồn kho tự hoàn lại đúng qua compensating transaction; (3) đặt vượt tồn kho → `PENDING_INVENTORY`→`OUT_OF_STOCK`, tồn kho không đổi. Cả 3 đều đạt.
- `[x]` **Bước 4 — `frontend-service`:** `OrderApiService` thêm `getOrderById(id)`; sửa `CheckoutController.initiatePayment()` thêm vòng lặp poll (~200ms/lần, timeout ~5s) chờ status rời khỏi `PENDING_INVENTORY` trước khi quyết định tạo PayPal order hay báo lỗi hết hàng; `checkout.html` thêm hiển thị lỗi `error=out_of_stock`. Test end-to-end thật qua toàn bộ chuỗi HTTP (login → thêm giỏ hàng Redis → `POST /checkout/pay`): đủ hàng → redirect đúng sang PayPal, tồn kho trừ đúng; hết hàng → redirect `/checkout?error=out_of_stock` (phản hồi nhanh ~0.5s, không phải chờ hết timeout 5s vì Saga xử lý gần như tức thì), tồn kho không đổi, trang hiển thị đúng thông báo lỗi tiếng Việt. Khách hàng hoàn toàn không thấy độ trễ/polling này qua giao diện — đúng như thiết kế.
- `[x]` **Bước 5 — Admin UI `/admin/inventory`:** thêm `GET/PUT /api/stocks` (upsert) vào `inventory-service` (`StockController` mới - trước đó service này chỉ có RabbitMQ listener, chưa có REST nào); `InventoryApiService` (frontend), route trong `AdminController` (liệt kê toàn bộ sản phẩm catalog join số lượng tồn kho, mặc định 0 nếu chưa có dòng Stock), template `admin/inventory.html` kèm form sửa số lượng tay; thêm nav link + permission mapping (`PRODUCT_MANAGE`, tái dùng permission có sẵn). Test qua UI thật (đăng nhập `admin`, submit form qua JS do nút submit form này từng bị flaky với browser tool): sửa số lượng "Moong" → 50, xác nhận DB cập nhật đúng và trang reload hiển thị đúng số liệu mới.
- `[x]` **Bước 6 — Seed dữ liệu tồn kho ban đầu:** script SQL insert `Stocks` khớp toàn bộ 24 `productId` trong bảng `Products` - đa số 100 đơn vị, cố tình để `Lays` (id=5) = 3 (sắp hết) và `Grapes` (id=20) = 0 (hết hàng) để dễ demo cả 2 trường hợp trên `/admin/inventory` và luồng checkout. Verify qua JOIN với `Products` xác nhận đủ 24/24 dòng, tên khớp đúng. Lưu ý kỹ thuật: `sqlcmd -i <file>` phải chạy qua PowerShell với đường dẫn Windows backslash chuẩn - chạy qua Bash (kể cả path dạng `C:/...` forward-slash) bị lỗi "Access is denied" khi dùng flag `-i`; flag `-Q` (inline query) thì chạy bình thường qua cả 2.
- `[x]` **Bước 7 — Test end-to-end:** phần lớn đã verify rải rác ở Bước 2-4 (đủ hàng → `PENDING_INVENTORY`→`PENDING` mượt không lộ UI, tồn kho trừ đúng; vượt tồn kho → `OUT_OF_STOCK`, không tạo PayPal order, tồn kho không đổi; hủy đơn đã trừ kho → hoàn kho đúng qua compensating transaction). Riêng test race condition làm cuối cùng: seed 1 sản phẩm test còn đúng 1 đơn vị, bắn 2 message `order.created` **song song thật sự** (cùng lúc, không tuần tự) qua RabbitMQ Management API tranh nhau đúng 1 đơn vị đó → verify tồn kho cuối cùng = 0 (không bị âm), chỉ đúng 1 trong 2 đơn thắng (RESERVED), đơn kia thua (FAILED). Ghi chú kỹ thuật: `@RabbitListener` mặc định 1 consumer/queue nên 2 message thực chất được RabbitMQ tuần tự hóa hộ, nhưng tính đúng đắn thật sự đến từ việc `UPDATE ... WHERE quantity >= :qty` là 1 câu lệnh atomic ở tầng SQL Server (row-level lock trong transaction) - vẫn an toàn ngay cả khi có nhiều consumer thread thật sự cùng lúc, không phụ thuộc vào việc RabbitMQ có serialize message hay không.

**Tổng kết:** cả 7/7 bước của kế hoạch Tồn kho + Saga Pattern (Orchestration→Choreography-based) đã hoàn thành, compile sạch, cả 3 service (`inventory-service` mới, `order-service`, `frontend-service`) đã restart và chạy ổn định, verify end-to-end đầy đủ từ tầng DB tới tầng UI thật, bao gồm cả kịch bản race condition. Đơn hàng giờ tự động trừ tồn kho khi đặt thành công và tự động hoàn lại khi đơn hỏng ở bất kỳ giai đoạn nào sau đó, đúng chuẩn Saga pattern với compensating transaction.

**Ghi chú giới hạn đã biết (nói rõ trước, không giấu):** cơ chế poll trong `CheckoutController` chiếm 1 thread servlet trong tối đa ~5s/lượt checkout — chấp nhận được ở quy mô đồ án (bước trừ kho thực tế chỉ mất vài chục ms), nhưng ở hệ thống production thật sẽ cần thay bằng cơ chế non-blocking (WebSocket/SSE hoặc reactive) để không chiếm thread pool khi tải cao — ghi nhận as known trade-off, không làm trong phạm vi đồ án này.

Sẽ làm tuần tự từng bước, mỗi bước dán code → review → compile → sang bước tiếp, giữ đúng quy trình đã áp dụng xuyên suốt dự án.

---

## Fix bug: Exchange RabbitMQ lệch tên giữa order-service và basket-service (19/08/2026)

### Bối cảnh
Bug này đã được phát hiện và ghi nhận trước đó (xem mục "⚠️ Phát hiện phụ" ở kế hoạch Tồn kho phía trên, dòng ~830) lúc khảo sát RabbitMQ cho Saga pattern, nhưng để ngoài phạm vi lúc đó vì không ảnh hưởng người dùng. Nay quay lại xử lý dứt điểm theo yêu cầu của user.

### Vấn đề
- `order-service/RabbitMQConfig.EXCHANGE_NAME = "ecommerce.exchange"` (dấu chấm) — publish `order.placed` lên đây trong `OrderController.checkout()`.
- `basket-service/RabbitMQConfig.EXCHANGE_NAME = "ecommerce_exchange"` (gạch dưới) — bind `basket_queue` vào exchange NÀY với cùng routing key `order.placed`.
- Đây là 2 exchange khác nhau thật sự trong RabbitMQ (topic exchange khớp theo tên chính xác) → event `order.placed` không bao giờ tới được `basket-service.OrderEventListener.handleOrderPlacedEvent()`. Không lộ ra ngoài vì `CheckoutController.paymentSuccess()` (frontend-service) đã xóa giỏ hàng trực tiếp qua `basketApiService.deleteBasket(memberId)` (đường đồng bộ) — đường RabbitMQ là dead code.

### Phát hiện thêm khi khảo sát kỹ trước khi fix
Nếu chỉ sửa tên exchange, message SẼ tới được `basket_queue`, nhưng phát sinh vấn đề thứ 2: `order-service` publish qua `RabbitTemplate` dùng chung `Jackson2JsonMessageConverter` (bean toàn app, đã thêm ở Phase 7 cho `payment.success`) — nghĩa là `order.placed` cũng bị serialize thành JSON, không còn là chuỗi Java thuần. `basket-service` hiện chưa khai báo `MessageConverter` nào (mặc định `SimpleMessageConverter`), nên khi nhận message content-type `application/json`, converter mặc định trả thẳng `byte[]` thay vì `String` — `@RabbitListener` ép kiểu `byte[]` → `String` bằng converter mặc định của Spring, cho ra chuỗi kèm dấu ngoặc kép bao quanh (`"member-id"` thay vì `member-id`). Hậu quả: log vẫn in ra như đã chạy thành công, nhưng `cartRepository.deleteById(memberId)` tìm sai key Redis → không xóa được giỏ hàng thật, lỗi âm thầm khó phát hiện qua log. Cùng loại lỗi lệch message converter giữa producer/consumer đã từng gây sự cố nghiêm trọng ở Phase 7 (payment-service ↔ order-service, vòng lặp retry vô hạn, log phình 281MB) — lần này nhẹ hơn (không crash, không loop) nhưng vẫn sai dữ liệu nếu không sửa cùng lúc.

### Fix (2 thay đổi trong cùng 1 file `basket-service/RabbitMQConfig.java`)
1. Đổi `EXCHANGE_NAME` từ `"ecommerce_exchange"` sang `"ecommerce.exchange"`, khớp đúng bản order-service đang publish thật (và khớp exchange dùng chung đã chốt cho Saga tồn kho ở kế hoạch phía trên).
2. Thêm `@Bean MessageConverter jsonMessageConverter()` (`Jackson2JsonMessageConverter`) — đúng pattern đã áp dụng ở `order-service`/`payment-service` từ Phase 7, để khớp converter với producer.

### Task list
- `[ ]` Cập nhật `basket-service/src/main/java/com/ecommerce/basket/config/RabbitMQConfig.java` (đổi exchange name + thêm MessageConverter bean) — theo quy trình dán code thủ công.
- `[ ]` Restart `basket-service`.
- `[ ]` Test end-to-end: đặt 1 đơn hàng thật qua PayPal sandbox → xác nhận log basket-service in đúng `"Đã tự động xóa giỏ hàng của {memberId} khỏi Redis."` với `memberId` KHÔNG có dấu ngoặc kép, và giỏ hàng Redis thực sự trống.
- `[ ]` (Tùy chọn, không bắt buộc) Xóa exchange rác `ecommerce_exchange` còn sót lại trên RabbitMQ qua Management UI — không còn binding/consumer nào dùng tới sau fix này.

## Dashboard Admin: Doanh thu theo tháng + Đơn hàng gần đây (19/08/2026)

**Yêu cầu người dùng:** thay 2 phần dữ liệu giả (`Area Chart Example`, `DataTable Example`) trên trang chủ Dashboard admin bằng dữ liệu thật — biểu đồ doanh thu theo tháng, và bảng do người dùng chọn sau khi được gợi ý ("Đơn hàng gần đây").

**Quyết định kiến trúc:** người dùng hỏi có nên tách 1 service riêng cho thống kê không — trả lời: **không cần** ở quy mô này, vì cả 2 nhu cầu đều lấy 100% từ dữ liệu `Invoice` mà `order-service` đã có sẵn qua `getAllOrders()` (đã sort sẵn desc theo ngày), không có phép tổng hợp xuyên nhiều service nào. Tự làm trực tiếp trong `AdminController.dashboard()` hiện có.

### Task list

- `[x]` `AdminController.dashboard()`: tính doanh thu 6 tháng gần nhất (chỉ đơn `PAID`, gộp theo `yyyy-MM` từ `orderDate`) và lấy 10 đơn gần nhất (`orders.stream().limit(10)`) — không cần sửa/thêm gì bên `order-service`.
- `[x]` `admin/index.html`: bind dữ liệu thật vào `myAreaChart` qua biến global `window.__revenueChartData`; thay bảng dummy bằng `th:each` liệt kê `recentOrders` kèm badge trạng thái (tái dùng đúng pattern `admin/orders.html`).
- `[x]` `chart-area-demo.js`: đọc `window.__revenueChartData` nếu có (fallback về demo cũ cho các trang admin khác không set biến này).

### Bug phát sinh khi test (cả 3 đều đã vá, đáng ghi nhớ cho các lần sau)

1. **HTTP caching của trình duyệt phục vụ bản JS cũ vô thời hạn:** Spring serve static resource kèm `Last-Modified` nhưng KHÔNG có `Cache-Control` → trình duyệt tự áp dụng "heuristic caching" (RFC 7234) và không bao giờ hỏi lại server dù file đã đổi, kể cả ở tab hoàn toàn mới (cache là per-browser-profile, không phải per-tab). Chỉ phát hiện được bằng cách so sánh nội dung request thực tế qua Network log (`fetch` với `cache:'no-store'` mới thấy đúng, `<script src>` bình thường vẫn nhận bản cache cũ). Fix: thêm query string version (`?v=2`) vào `<script th:src="...">` trong `_layout.html` để cache-bust — cần nhớ tăng version mỗi khi sửa các file JS tĩnh này sau này.
2. **Cú pháp mảng JS lồng `[[...]]` trùng cú pháp inline-expression của Thymeleaf:** viết `window.__dataTableOrder = [[2, 'desc']];` (input hợp lệ cho DataTables) bên trong 1 trang có `th:inline="javascript"` ở phần tử khác khiến Thymeleaf cố parse `2, 'desc'` như biểu thức SpEL, ném `TemplateProcessingException` **giữa lúc đang stream response** → response HTTP bị cắt đứt ngang, làm mất toàn bộ phần còn lại của trang (card/chart/bảng) mà KHÔNG có lỗi rõ ràng ở phía client (trình duyệt chỉ thấy trang thiếu nội dung, không phải lỗi 500 rõ ràng vì response đã commit 1 phần trước khi exception xảy ra). Chẩn đoán qua log server (`org.thymeleaf.exceptions.TemplateProcessingException: Could not parse as expression`), không thấy được từ phía trình duyệt. Fix đúng: thêm `th:inline="none"` ngay trên chính thẻ `<script>` chứa cú pháp `[[...]]` không liên quan tới Thymeleaf, để tắt hẳn cơ chế inline mặc định cho riêng phần tử đó.
3. **Comment HTML giải thích lỗi #2 lại vô tình chứa nguyên văn cú pháp gây lỗi** (`"[[2, 'desc']]"` trong lời giải thích) → Thymeleaf tự động áp dụng inline-processing cho MỌI comment `<!-- ... -->` trong toàn trang (không cần `th:inline`, không cần nằm trong vùng nào đặc biệt) nên bị lỗi y hệt lần 2. Bài học: khi viết comment giải thích về 1 xung đột cú pháp Thymeleaf, tuyệt đối không chép nguyên văn cú pháp gây lỗi vào trong comment.

Test cuối: verify qua browser thật (đăng nhập `admin`) — biểu đồ đúng `label:"Doanh thu"`, dữ liệu 6 tháng khớp DB (176.351.000đ ở tháng hiện tại, 5 tháng trước = 0); bảng "Đơn hàng gần đây" sort đúng theo Ngày đặt giảm dần, badge trạng thái hiển thị đúng cho mọi status (PAID/PENDING/CANCELLED/FAILED/PENDING_INVENTORY/OUT_OF_STOCK/REFUND_REQUESTED/REFUNDED).

## Triển khai lên Kubernetes — Kế hoạch mới (21/08/2026)

**Yêu cầu người dùng:** lập kế hoạch triển khai lên môi trường k8s, bám sát các service/manifest đã có sẵn (`source/k8s/*.yaml`) để đi đúng hướng, không tự đặt ra convention mới.

### Khảo sát hiện trạng (trước khi lập kế hoạch)

Đọc toàn bộ 10 file trong `source/k8s/` + 7 `Dockerfile` hiện có + toàn bộ `@Value(".. _URL:...")` trong `frontend-service`/`auth-service` để đối chiếu. Convention hiện tại:
- Mỗi service: 1 `Deployment` (image `<ten>-service:latest`, `imagePullPolicy: IfNotPresent`, build local bằng `docker build`, **không dùng registry, không có script build tự động** — thao tác thủ công) + 1 `Service` (đa số đặt tên `<ten>-clusterip-srv`, một số ít `<ten>-srv`).
- Ghi đè cấu hình qua biến môi trường trực tiếp trong `Deployment` (không dùng ConfigMap/Secret) — DB dùng chung `mssql-clusterip-srv`/database `EcommerceDB`/password `pa55w0rd!` (khác password `123456` dùng khi chạy local, đây là 2 giá trị tách biệt có chủ đích).
- `Dockerfile`: `FROM eclipse-temurin:21-jre-alpine`, copy thẳng `target/*.jar` đã build sẵn (không multi-stage build trong Docker) — nghĩa là phải `mvn package` trước, `docker build` sau.
- `Ingress` (`ingress-srv.yaml`): expose trực tiếp từng `/api/...` prefix ra ngoài (không qua API Gateway tập trung), cộng `/` cho `frontend-srv`.

**Phát hiện các lỗ hổng (không chỉ thiếu `inventory-service`):**
1. `inventory-service` (thêm ở Phase Saga Tồn kho, sau khi các file k8s này được tạo) **hoàn toàn chưa có** trong hạ tầng k8s — không `Dockerfile`, không `Deployment`/`Service`, không route `Ingress`.
2. `auth-depl.yaml` **thiếu biến `CUSTOMER_SERVICE_URL`** — `AuthController.java` (auth-service) gọi sang customer-service để xác thực khách hàng (từ Phase "Tách 2 loại tài khoản"), nếu thiếu biến này sẽ fallback về `http://localhost:8082/...` bên trong container → lỗi kết nối, đăng nhập khách hàng gãy hoàn toàn trên k8s dù chạy local vẫn bình thường.
3. `frontend-depl.yaml` **chỉ có 4/12 biến `_SERVICE_URL` cần thiết** (thiếu `PAYMENT_SERVICE_URL`, `CUSTOMER_SERVICE_URL`, `ADDRESS_SERVICE_URL`, `INVENTORY_SERVICE_URL`, `USER_SERVICE_URL`, `ROLE_SERVICE_URL`, `PERMISSION_SERVICE_URL`, `ORDER_SERVICE_URL`) — các file k8s này được tạo từ giai đoạn đầu dự án, trước khi các tính năng Đơn hàng/Thanh toán/Sổ địa chỉ/Tồn kho/Role-Permission-Users được xây dựng, nên chưa được cập nhật theo. Nếu deploy nguyên trạng, gần như toàn bộ tính năng nghiệp vụ chính sẽ lỗi kết nối trên k8s.
4. `ingress-srv.yaml` thiếu route `/api/stocks` (inventory-service) — không bắt buộc về mặt chức năng (frontend-service gọi nội bộ qua `ClusterIP`, không qua `Ingress`) nhưng nên thêm để nhất quán với các API khác đã expose, tiện test trực tiếp qua Postman/curl từ ngoài cluster.

### Task list

- `[x]` **Bước 1 — Tạo `source/inventory-service/Dockerfile`:** theo đúng mẫu tối giản của `catalog-service`/`payment-service` (`eclipse-temurin:21-jre-alpine`, copy `target/*.jar`).
- `[x]` **Bước 2 — Tạo `source/k8s/inventory-depl.yaml`:** theo đúng mẫu `basket-depl.yaml` (có `SPRING_DATASOURCE_URL`/`SPRING_DATASOURCE_PASSWORD` trỏ `mssql-clusterip-srv`, `SPRING_RABBITMQ_HOST` trỏ `rabbitmq-clusterip-srv`, không có gRPC vì inventory-service không dùng); `Service` tên `inventory-clusterip-srv`, port `8086` (khớp cùng nhóm backend nội bộ như order/payment/basket).
- `[x]` **Bước 3 — Vá `source/k8s/auth-depl.yaml`:** thêm `CUSTOMER_SERVICE_URL` trỏ `http://customer-srv:8082/api/members`.
- `[x]` **Bước 4 — Vá `source/k8s/frontend-depl.yaml`:** bổ sung đủ 8 biến còn thiếu, trỏ đúng tên `Service` nội bộ tương ứng (`order-clusterip-srv`, `payment-clusterip-srv`, `customer-srv`, `inventory-clusterip-srv`, `auth-srv`). Tiện tay dọn luôn `BASKET_SERVICE_URL` đang bị khai báo lặp 2 lần trong file gốc.
- `[x]` **Bước 5 — Vá `source/k8s/ingress-srv.yaml`:** thêm route `/api/stocks` → `inventory-clusterip-srv:8086`.
- `[x]` **Bước 6 — Build + deploy + test trên cluster thật:** môi trường xác định là **Docker Desktop Kubernetes** (context `docker-desktop`, 1 node, cluster đã có sẵn từ trước với 7 deployment cũ). Build lại toàn bộ 8 image (`docker build -t <ten>-service:latest .`) → `kubectl apply -f source/k8s/` → `kubectl rollout restart` toàn bộ 7 deployment cũ để buộc nạp image mới (`imagePullPolicy: IfNotPresent` + tag `:latest` không tự nạp lại image mới nếu không restart thủ công).
  - **Sự cố phát sinh 1 — DB trong k8s (`mssql-depl`, PVC riêng) là một instance hoàn toàn tách biệt, cũ ~14 ngày** so với SQL Server local dùng để phát triển suốt phiên làm việc: thiếu cột (`active`/`position`/`slug` trên `categories`) và chỉ có dữ liệu demo gốc (10 category/10 product/2 user, 0 dòng ở roles/permissions/members/stocks/invoices) — gây lỗi `500` (`Invalid column name 'active'`) khi test qua `Ingress`. Người dùng chọn hướng xử lý triệt để: xoá sạch + đồng bộ lại toàn bộ dữ liệu giống hệt local.
  - Thử `BACKUP`/`RESTORE` trước — bỏ do bế tắc quyền NTFS (user thường không đọc được thư mục Backup mặc định của SQL Server, và ngược lại service SQL Server không ghi được vào thư mục user, không có quyền admin để cấp `icacls`).
  - Chuyển hướng: viết script T-SQL sinh câu lệnh `INSERT INTO ...` trực tiếp từ dữ liệu local (12 bảng, xử lý `NULL`/escape string/`N'...'`/datetime/`uniqueidentifier` đúng kiểu cột) → chạy trên local, thu được 199 dòng `INSERT` chính xác → xoá sạch toàn bộ bảng trên DB k8s (drop hết FK rồi hết table qua script động, không cần biết thứ tự phụ thuộc) → `kubectl rollout restart` 6 service có JPA để Hibernate (`ddl-auto=update`) tự tạo lại schema mới từ entity hiện tại.
  - **Sự cố phát sinh 2 — `order-depl.yaml` thiếu hẳn `SPRING_DATASOURCE_USERNAME`/`PASSWORD`, `customer-depl.yaml` thiếu toàn bộ biến môi trường lẫn `containerPort`** (2 manifest gốc chưa từng được hoàn thiện, sót lại từ đợt vá Bước 3-4 vì lúc đó chỉ audit `auth-depl.yaml`/`frontend-depl.yaml`) — cả 2 service rơi về `spring.datasource.password=123456` (mặc định trong `application.properties`, dùng cho local) trong khi SA password trên k8s là `pa55w0rd!` → lỗi `Login failed for user 'sa'`, pod `CrashLoopBackOff`. Vá cả 2 file (thêm `SPRING_DATASOURCE_USERNAME=sa`/`PASSWORD=pa55w0rd!`, và với `customer-depl.yaml` thêm cả `containerPort: 8082` + `SPRING_DATASOURCE_URL`) theo đúng convention các service khác, `kubectl apply` + `rollout restart` lại — cả 2 khởi động thành công.
  - Chạy 199 dòng `INSERT` (bọc `SET IDENTITY_INSERT <table> ON/OFF` cho 6 bảng có cột IDENTITY: `categories`/`products`/`roles`/`permissions`/`users`/`addresses`, theo đúng thứ tự phụ thuộc FK) vào DB k8s. Phát hiện 1 dòng `category_id=1` bị thiếu do file export có ký tự BOM (UTF-8) ở đầu dòng đầu tiên khiến script tự động chèn `SET IDENTITY_INSERT` bị lệch vị trí cho riêng bảng đầu tiên — chèn bù thủ công 1 dòng đó, verify lại: đủ 12/12 bảng, tổng 199 dòng khớp chính xác dữ liệu local.
  - Verify cuối: `kubectl get pods` toàn bộ `Running` (0 restart mới), test qua `curl` tới `Ingress` (`http://ecommerce.local/`) — `/api/categories`, `/api/categories/tree` (xác nhận category id=1 "Kitchen" trả về đúng), `/api/products`, `/api/members`, `/api/stocks`, trang chủ frontend — toàn bộ `HTTP 200`.
  - **Giới hạn đã biết:** không có `readinessProbe` trong manifest nào — k8s route traffic tới pod ngay khi container start, chưa chờ app thực sự sẵn sàng (từng gây "Connection refused" thoáng qua khi 7 JVM khởi động đồng loạt, có JVM mất tới 125s do tranh chấp CPU) — chưa fix, ghi nhận là cải tiến tiềm năng sau này. Kiểm tra UI qua trình duyệt tự động bị chặn bởi lớp phê duyệt an toàn cho domain lạ (`ecommerce.local`) — chưa test được qua giao diện thật, chỉ verify qua API trực tiếp; người dùng nên tự mở trình duyệt kiểm tra thêm luồng đăng nhập/đặt hàng/Saga tồn kho.

**Lưu ý phạm vi:** giữ nguyên convention "biến môi trường thô trong Deployment YAML" đã dùng xuyên suốt (không đổi sang Secret/ConfigMap dù đó là thực hành tốt hơn cho secret thật) — vì mục tiêu là bám sát hướng đi đã có, không tái thiết kế. Có thể đề xuất tách secret ra `Secret` riêng như 1 cải tiến sau này nếu người dùng muốn.

Sẽ làm tuần tự từng bước, mỗi bước dán code → review → sang bước tiếp, giữ đúng quy trình đã áp dụng xuyên suốt dự án.

### Sự cố phát sinh sau Bước 6 — phát hiện qua test thật trên trình duyệt của người dùng

Người dùng tự test checkout trên trình duyệt thật (Chrome), gặp alert **"Có lỗi xảy ra khi đồng bộ giỏ hàng!"**. Log `frontend-depl` cho thấy `ResourceAccessException: Connection refused` khi `BasketApiService.updateBasket()` gọi tới `http://localhost:8083/api/baskets` — chính là giá trị fallback mặc định trong code (`@Value("${BASKET_SERVICE_URL:http://localhost:8083/api/baskets}")`), nghĩa là pod đang chạy không có biến `BASKET_SERVICE_URL`.

**Nguyên nhân gốc:** đối chiếu file `frontend-depl.yaml` trên đĩa (đã có đủ 12 biến, đúng từ Bước 4) với biến môi trường THỰC TẾ trong pod (`kubectl exec ... env`) thì pod đang chạy thiếu hẳn `BASKET_SERVICE_URL` — pod đó (tồn tại từ trước, thuộc nhóm 7 deployment cũ) chưa từng được `kubectl apply` lại với bản vá kể từ Bước 4/Bước 6, dù `rollout restart` đã chạy cho 6 service JPA lúc xử lý sự cố DB, **frontend-depl không nằm trong nhóm đó nên không được restart lại lần cuối**.

**Xử lý:** `kubectl apply -f frontend-depl.yaml` lại + `kubectl rollout restart deployment/frontend-depl` → verify biến môi trường trong pod mới đầy đủ → test lại `POST /api/cart/checkout` qua `curl` trả về `200`/`/checkout` đúng như mong đợi.

**Bài học:** sau khi vá nhiều manifest trong 1 phiên làm việc dài, cần rà soát lại xem MỌI deployment đã sửa đều thực sự được `apply` + `restart` lần cuối cùng — không chỉ dựa vào việc file trên đĩa đã đúng, vì có thể có khoảng trống giữa lúc patch file và lúc restart thực tế (nhất là khi patch nhiều file rải rác qua nhiều bước).

### Sự cố thứ 2 — lỗi khi PayPal redirect về sau khi thanh toán xong

Sau khi fix giỏ hàng, người dùng test tiếp tới bước thanh toán PayPal thật (sandbox) — thanh toán xong, PayPal redirect trình duyệt về `localhost:8888/checkout/success?token=...&PayerID=...` thay vì `ecommerce.local`, trang báo lỗi "Order Cancelled — Có lỗi trong quá trình thanh toán".

**Nguyên nhân:** `PayPalService.java` (payment-service) build `return_url`/`cancel_url` gửi cho PayPal từ biến `frontendBaseUrl` (`@Value("${frontend.base-url:http://localhost:8888}")`) — `payment-depl.yaml` **chưa từng khai báo** `FRONTEND_BASE_URL`, nên rơi về mặc định `localhost:8888` (địa chỉ dev local, không phải domain Ingress `ecommerce.local` mà người dùng đang truy cập). PayPal dùng URL này để redirect **trình duyệt của người dùng** (chạy ngoài cluster) sau khi thanh toán — sai domain khiến bước capture/xác nhận đơn hàng thất bại.

**Xử lý:** thêm `FRONTEND_BASE_URL=http://ecommerce.local` vào `payment-depl.yaml`, `kubectl apply` + `rollout restart` — verify biến môi trường trong pod mới đúng.

**Lưu ý:** đây là link PayPal cũ (token đã dùng/hết hạn) nên không thể test lại được — người dùng cần bắt đầu lại từ đầu (thêm sản phẩm vào giỏ, checkout, thanh toán PayPal mới) để kiểm tra bản vá.

### Sự cố thứ 3 — trang Admin Login mất hoàn toàn CSS/JS (lỗi code, không phải deploy)

Người dùng test trang `/admin/login` qua trình duyệt, giao diện hiển thị HTML thô không style (font mặc định trình duyệt).

**Nguyên nhân:** `AdminAuthInterceptor` (`AppConfig.java`, frontend-service) áp `addPathPatterns("/admin/**")` cho toàn bộ đường dẫn bắt đầu `/admin/`, chỉ `excludePathPatterns("/admin/login", "/admin/access-denied")`. Vì các tài nguyên tĩnh của khu Admin (`static/admin/css/`, `static/admin/js/`, `static/admin/assets/`) cũng được Spring Boot map vào URL cùng tiền tố `/admin/`, nên MỌI request tới CSS/JS (kể cả của chính trang login) bị Interceptor coi là route cần đăng nhập và redirect `302` về `/admin/login` — verify qua `curl -i http://ecommerce.local/admin/css/styles.css` thấy `302 Location: /admin/login`. Đây là lỗi logic có sẵn trong code, không liên quan tới k8s/deploy.

**Xử lý (theo quy trình Guide-Don't-Fix — dán code qua chat):** vá `AppConfig.java`, thêm `"/admin/css/**", "/admin/js/**", "/admin/assets/**"` vào `excludePathPatterns`. Build lại (`mvn package -DskipTests` → `docker build -t frontend-service:latest .`) → `kubectl rollout restart deployment/frontend-depl`. Verify: `/admin/css/styles.css`, `/admin/js/scripts.js`, `/admin/assets/demo/chart-area-demo.js` đều `200`; `/admin` (dashboard, chưa đăng nhập) vẫn `302` về login đúng như thiết kế — xác nhận không làm lộ route quản trị nào ngoài static assets.

## Tính năng: Đổi mật khẩu cho tài khoản Admin/Staff sau khi đăng nhập

**Yêu cầu người dùng:** thêm chức năng đổi mật khẩu khi đã đăng nhập vào Admin Dashboard.

**Khảo sát trước khi làm:** phát hiện `POST /api/auth/change-password` (auth-service, `AuthController.java`) đã tồn tại sẵn, xác thực bằng `username` (bảng `users` — đúng bảng chứa tài khoản Admin/Staff, khác `members` bên customer-service dùng cho khách hàng). `AuthApiService.changePassword(username, oldPassword, newPassword)` (frontend-service) cũng đã có sẵn, đang được dùng cho trang tài khoản khách hàng cũ (nay khách hàng đã chuyển sang dùng `CustomerApiService.changeMemberPassword` vì đổi bảng lưu trữ) — tái sử dụng lại được nguyên vẹn cho Admin, không cần thêm endpoint backend mới.

**Triển khai (theo quy trình Guide-Don't-Fix):**
- `AdminController.java`: thêm `GET /admin/change-password` (hiển thị form) và `POST /admin/change-password` (lấy `username` từ session `adminUsername`, gọi `authApiService.changePassword(...)`, phản hồi qua `RedirectAttributes` flash attribute `passwordMessage`/`passwordError`, theo đúng pattern các action khác trong file như `saveInventory`/`rejectRefund`).
- Template mới `admin/change-password.html`: decorate `admin/_layout`, breadcrumb + card + form (mật khẩu hiện tại/mới), theo đúng pattern `user-form.html`.
- `admin/fragments/_header.html`: thêm link "Đổi mật khẩu" vào dropdown user (trước Logout).
- Route `/admin/change-password` vẫn nằm trong `AdminAuthInterceptor` (`/admin/**`, không nằm trong danh sách loại trừ) nên tự động yêu cầu đã đăng nhập — không cần thêm code kiểm tra quyền riêng; không match bất kỳ `resolveRequiredPermission()` prefix nào nên chỉ cần đã đăng nhập (bất kỳ quyền gì) là đổi được mật khẩu của chính mình.
- Build (`mvn package -DskipTests` → `docker build -t frontend-service:latest .`) → `kubectl rollout restart deployment/frontend-depl`. Verify qua `curl`: `/admin/change-password` khi chưa đăng nhập trả về `302` (đúng thiết kế, không bị lộ), các endpoint khác (`/admin/login`, static assets, trang chủ) vẫn `200` bình thường.
- **Lưu ý:** password trong bảng `users` (auth-service) hiện lưu dạng plain text (không hash) — quy ước có sẵn từ trước trong dự án (comment xác nhận trong `AuthController.java`), không phải thứ được thay đổi bởi tính năng này.
- **Còn lại:** người dùng cần tự đăng nhập Admin Dashboard qua trình duyệt, vào dropdown góc phải trên cùng → "Đổi mật khẩu" để test luồng thật.

