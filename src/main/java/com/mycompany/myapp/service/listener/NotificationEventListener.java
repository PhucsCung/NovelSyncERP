package com.mycompany.myapp.service.listener;

import com.mycompany.myapp.domain.Employee;
import com.mycompany.myapp.domain.enumeration.NotificationType;
import com.mycompany.myapp.repository.EmployeeRepository;
import com.mycompany.myapp.repository.TransferOrderRepository;
import com.mycompany.myapp.service.MailService;
import com.mycompany.myapp.service.NotificationService;
import com.mycompany.myapp.service.dto.NotificationDTO;
import com.mycompany.myapp.service.event.OrderNotificationEvent;
import com.mycompany.myapp.service.mapper.EmployeeMapper;
import com.mycompany.myapp.security.AuthoritiesConstants;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final MailService mailService;
    private final TransferOrderRepository transferOrderRepository;

    public NotificationEventListener(
        NotificationService notificationService,
        EmployeeRepository employeeRepository,
        EmployeeMapper employeeMapper,
        MailService mailService,
        TransferOrderRepository transferOrderRepository
    ) {
        this.notificationService = notificationService;
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
        this.mailService = mailService;
        this.transferOrderRepository = transferOrderRepository;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderNotificationEvent(OrderNotificationEvent event) {
        String typeName = getOrderTypeName(event.getOrderType());
        String action = event.getAction();

        switch (action) {
            case "CREATED": {
                Optional<Employee> creatorOpt = employeeRepository.findByUserLogin(event.getOriginalCreatorLogin());
                if (creatorOpt.isEmpty()) return;
                Employee originalCreator = creatorOpt.get();

                if (originalCreator.getScopedWarehouse() != null && originalCreator.getDepartment() != null) {
                    List<Employee> localManagers = employeeRepository.findManagersByBranchAndDepartment(
                        originalCreator.getScopedWarehouse().getId(),
                        originalCreator.getDepartment().getId(),
                        AuthoritiesConstants.MANAGER
                    );
                    for (Employee manager : localManagers) {
                        sendNotification(
                            manager,
                            "🔔 [" + typeName.toUpperCase() + " MỚI] Chờ phê duyệt",
                            "Nhân viên " + originalCreator.getFullName() + " vừa khởi tạo " + typeName + " mã số: " + event.getOrderCode() + ". Vui lòng kiểm tra và xét duyệt.",
                            event.getOrderId()
                        );
                    }
                }
                break;
            }

            case "APPROVED": {
                Optional<Employee> creatorOpt = employeeRepository.findByUserLogin(event.getOriginalCreatorLogin());
                if (creatorOpt.isEmpty()) return;
                Employee originalCreator = creatorOpt.get();

                // 1. Báo cho nhân viên tạo đơn
                sendNotification(
                    originalCreator,
                    "🎉 " + typeName + " ĐÃ ĐƯỢC DUYỆT!",
                    "Quản lý đã phê duyệt đơn " + event.getOrderCode() + " của bạn. Hệ thống đã tự động xử lý cập nhật kho bãi.",
                    event.getOrderId()
                );

                // 2. Báo cho đội Shipper cùng chi nhánh
                if (originalCreator.getScopedWarehouse() != null) {
                    List<Employee> localShippers = employeeRepository.findShippersByBranch(
                        originalCreator.getScopedWarehouse().getId(),
                        "ROLE_SHIPPER"
                    );

                    String shipperMessage = "";
                    if ("SALES".equals(event.getOrderType())) {
                        shipperMessage = "Đơn bán lẻ " + event.getOrderCode() + " đã được duyệt xuất kho. Vui lòng chuẩn bị bốc xếp hàng và đi giao cho khách!";
                    } else if ("PURCHASE".equals(event.getOrderType())) {
                        shipperMessage = "Đơn mua hàng " + event.getOrderCode() + " đã được phê duyệt chi. Vui lòng chuẩn bị phương tiện sang kho Nhà cung cấp để lấy hàng!";
                    } else if ("TRANSFER".equals(event.getOrderType())) {
                        shipperMessage = "Phiếu điều chuyển " + event.getOrderCode() + " đã được duyệt xuất. Vui lòng chuẩn bị bốc hàng từ Kho xuất lên xe để vận chuyển!";
                    }

                    for (Employee shipper : localShippers) {
                        sendNotification(shipper, "🚚 [NHIỆM VỤ GIAO VẬN] Đơn hàng chờ xử lý", shipperMessage, event.getOrderId());
                    }
                }
                // 3. 👇 THÊM MỚI: Báo cho nhân viên Kho để lấy hàng/đóng gói (LOẠI TRỪ ĐƠN MUA)
                if (originalCreator.getScopedWarehouse() != null &&
                    ("SALES".equals(event.getOrderType()) || "TRANSFER".equals(event.getOrderType()))) {

                    // Quét toàn bộ nhân viên Kho thuộc chi nhánh này
                    List<Employee> localWarehouseStaff = employeeRepository.findWarehouseStaffByBranch(
                        originalCreator.getScopedWarehouse().getId(),
                        "ROLE_WAREHOUSE" // Hoặc AuthoritiesConstants.WAREHOUSE tùy bác định nghĩa
                    );

                    String warehouseMessage = "SALES".equals(event.getOrderType())
                        ? "Đơn bán hàng " + event.getOrderCode() + " đã được duyệt. Vui lòng lấy hàng khỏi kệ và đóng gói để Shipper qua bốc đi!"
                        : "Phiếu điều chuyển " + event.getOrderCode() + " đã được duyệt xuất. Vui lòng chuẩn bị hàng hóa tại cửa bãi để Shipper chuyển sang Kho đích!";

                    for (Employee staff : localWarehouseStaff) {
                        sendNotification(
                            staff,
                            "📦 [KHO VẬN] Yêu cầu chuẩn bị hàng",
                            warehouseMessage,
                            event.getOrderId()
                        );
                    }
                }
                break;
            }

            case "PROCESSING": { // 🚚 👇 THÊM MỚI: Xử lý khi Shipper bấm Bắt đầu giao hàng
                Optional<Employee> creatorOpt = employeeRepository.findByUserLogin(event.getOriginalCreatorLogin());
                if (creatorOpt.isEmpty()) return;
                Employee originalCreator = creatorOpt.get();

                // 1. Thông báo cho Nhân viên tạo đơn ban đầu
                sendNotification(
                    originalCreator,
                    "🚚 " + typeName + " ĐANG ĐƯỢC GIAO",
                    "Shipper đã xác nhận bốc xếp hàng và đang trên đường vận chuyển " + typeName + " mã số: " + event.getOrderCode() + ".",
                    event.getOrderId()
                );

                // 2. Thông báo cho Quản lý chi nhánh (Người đã duyệt đơn)
                if (originalCreator.getScopedWarehouse() != null && originalCreator.getDepartment() != null) {
                    List<Employee> localManagers = employeeRepository.findManagersByBranchAndDepartment(
                        originalCreator.getScopedWarehouse().getId(),
                        originalCreator.getDepartment().getId(),
                        AuthoritiesConstants.MANAGER
                    );
                    for (Employee manager : localManagers) {
                        sendNotification(
                            manager,
                            "📈 [TIẾN ĐỘ] Xe hàng đã lăn bánh",
                            "Quản lý lưu ý: " + typeName + " " + event.getOrderCode() + " do bạn phê duyệt đã được Shipper xếp lên xe đi giao.",
                            event.getOrderId()
                        );
                    }
                }
                break;
            }

            case "DELIVERED_WAITING_PAYMENT": // Giao xong đơn Sales
            case "ARRIVED": {                 // Xe về tới kho đích đơn Purchase/Transfer

                if ("TRANSFER".equals(event.getOrderType())) {
                    // 📦 1. LUỒNG TRANSFER: Kéo Quản lý Kho Đích (Kho B) ra nhận hàng
                    Optional<Employee> creatorOpt = employeeRepository.findByUserLogin(event.getOriginalCreatorLogin());
                    if (creatorOpt.isEmpty()) return;
                    Employee originalCreator = creatorOpt.get(); // Lấy để biết ID phòng Kho vận

                    // Query ngược lại TransferOrder để lấy thông tin Kho đích
                    transferOrderRepository.findById(event.getOrderId()).ifPresent(transferOrder -> {
                        if (transferOrder.getToWarehouse() != null && originalCreator.getDepartment() != null) {

                            // Tìm Quản lý của ĐÚNG Kho Đích (Kho B)
                            List<Employee> receivingManagers = employeeRepository.findManagersByBranchAndDepartment(
                                transferOrder.getToWarehouse().getId(),
                                originalCreator.getDepartment().getId(),
                                AuthoritiesConstants.MANAGER
                            );

                            for (Employee manager : receivingManagers) {
                                sendNotification(
                                    manager,
                                    "📦 [KHO ĐÍCH] Xe hàng điều chuyển đã tới",
                                    "Xe chở hàng từ phiếu " + event.getOrderCode() + " đã về tới cổng kho của bạn. Vui lòng ra bãi kiểm đếm và bấm Nhận Hàng!",
                                    event.getOrderId()
                                );
                            }
                        }
                    });

                } else {
                    // 💰 2. LUỒNG SALES & PURCHASE: Báo cho Kế toán tổng như cũ
                    List<Employee> allAccountants = employeeRepository.findAllAccountants("ROLE_ACCOUNTANT");

                    String accountantMessage = "Shipper báo cáo hành trình vận chuyển " + typeName + " mã số: " + event.getOrderCode() + " đã hoàn tất an toàn. Vui lòng kiểm tra dòng tiền/công nợ để tiến hành chốt sổ hoàn thành!";

                    for (Employee accountant : allAccountants) {
                        sendNotification(
                            accountant,
                            "💰 [KẾ TOÁN TỔNG] Cần xác nhận dòng tiền",
                            accountantMessage,
                            event.getOrderId()
                        );
                    }
                }
                break;
            }

            case "COMPLETED": {
                Optional<Employee> creatorOpt = employeeRepository.findByUserLogin(event.getOriginalCreatorLogin());
                if (creatorOpt.isEmpty()) return;
                Employee originalCreator = creatorOpt.get();

                if ("TRANSFER".equals(event.getOrderType())) {
                    // 📦 1. LUỒNG ĐIỀU CHUYỂN: Bắn cho Quản lý Kho Xuất (Kho A)

                    // Quét tìm Quản lý của Kho xuất (Kho A) để báo cáo
                    if (originalCreator.getScopedWarehouse() != null && originalCreator.getDepartment() != null) {
                        List<Employee> localManagers = employeeRepository.findManagersByBranchAndDepartment(
                            originalCreator.getScopedWarehouse().getId(),
                            originalCreator.getDepartment().getId(),
                            AuthoritiesConstants.MANAGER
                        );
                        for (Employee manager : localManagers) {
                            sendNotification(
                                manager,
                                "📦 [KHO VẬN] Chốt sổ điều chuyển",
                                "Lô hàng từ phiếu " + event.getOrderCode() + " do bạn duyệt xuất đã được Kho đích nhận thành công và an toàn.",
                                event.getOrderId()
                            );
                        }
                    }
                } else {
                    // 💰 2. LUỒNG SALES & PURCHASE: Bỏ qua nhân viên, chỉ báo thẳng cho ADMIN

                    // Quét tìm tất cả ADMIN hệ thống
                    List<Employee> admins = employeeRepository.findAllAdmins("ROLE_ADMIN"); // Hoặc AuthoritiesConstants.ADMIN tùy bác định nghĩa

                    String adminMessage = "SALES".equals(event.getOrderType())
                        ? "Đơn bán hàng " + event.getOrderCode() + " đã chốt sổ thành công. Dòng tiền đã được cộng vào công nợ Phải thu của Khách hàng."
                        : "Đơn mua hàng " + event.getOrderCode() + " đã hoàn tất nhập kho và đối soát. Công nợ Phải trả cho Nhà cung cấp đã được ghi nhận.";

                    for (Employee admin : admins) {
                        sendNotification(
                            admin,
                            "👑 [TỔNG QUẢN TRỊ] Cập nhật Tài sản & Công nợ",
                            adminMessage,
                            event.getOrderId()
                        );
                    }
                }
                break;
            }

            case "CANCELLED": {
                Optional<Employee> creatorOpt = employeeRepository.findByUserLogin(event.getOriginalCreatorLogin());
                if (creatorOpt.isEmpty()) return;
                Employee originalCreator = creatorOpt.get();

                // 1. Báo cho người tạo đơn biết tin buồn
                sendNotification(
                    originalCreator,
                    "❌ " + typeName + " ĐÃ BỊ HỦY",
                    "Đơn " + event.getOrderCode() + " đã bị hủy bỏ bởi Quản lý/Admin (" + event.getActionByUserLogin() + "). Toàn bộ số dư liên quan đã được hoàn trả.",
                    event.getOrderId()
                );

                if (originalCreator.getScopedWarehouse() != null) {
                    Long branchId = originalCreator.getScopedWarehouse().getId();

                    // 2. Lệnh khẩn cấp cho đội Shipper chi nhánh (Bắt buộc)
                    List<Employee> localShippers = employeeRepository.findShippersByBranch(branchId, "ROLE_SHIPPER");
                    for (Employee shipper : localShippers) {
                        sendNotification(
                            shipper,
                            "🚨 [LỆNH HỦY KHẨN CẤP] Dừng giao đơn hàng",
                            "Đơn " + typeName + " " + event.getOrderCode() + " đã bị hủy. Nếu bạn đang vận chuyển, vui lòng DỪNG LẠI và hoàn trả hàng về kho!",
                            event.getOrderId()
                        );
                    }

                    // 3. Lệnh cho Thủ kho tiếp nhận hàng trả về (Chỉ áp dụng Sales & Transfer)
                    if ("SALES".equals(event.getOrderType()) || "TRANSFER".equals(event.getOrderType())) {
                        List<Employee> localWarehouseStaff = employeeRepository.findWarehouseStaffByBranch(branchId, "ROLE_WAREHOUSE");
                        for (Employee staff : localWarehouseStaff) {
                            sendNotification(
                                staff,
                                "🔄 [KHO VẬN] Tiếp nhận hàng hoàn trả",
                                "Đơn " + event.getOrderCode() + " đã bị hủy. Hệ thống đã cộng lại tồn kho, vui lòng tiếp nhận hàng vật lý trả về và cất lại lên kệ!",
                                event.getOrderId()
                            );
                        }
                    }
                }
                break;
            }
            case "DELETED": {
                Optional<Employee> creatorOpt = employeeRepository.findByUserLogin(event.getOriginalCreatorLogin());
                if (creatorOpt.isEmpty()) return;
                Employee originalCreator = creatorOpt.get();

                // Quét tìm đúng Quản lý cùng chi nhánh + bộ phận (Giống y hệt luồng CREATED)
                if (originalCreator.getScopedWarehouse() != null && originalCreator.getDepartment() != null) {
                    List<Employee> localManagers = employeeRepository.findManagersByBranchAndDepartment(
                        originalCreator.getScopedWarehouse().getId(),
                        originalCreator.getDepartment().getId(),
                        AuthoritiesConstants.MANAGER
                    );
                    for (Employee manager : localManagers) {
                        sendNotification(
                            manager,
                            "🗑️ [" + typeName.toUpperCase() + "] Đã hủy bỏ đơn nháp",
                            "Nhân viên " + originalCreator.getFullName() + " đã chủ động xóa/thu hồi " + typeName + " mã số: " + event.getOrderCode() + ". Bạn không cần xét duyệt chứng từ này nữa.",
                            event.getOrderId()
                        );
                    }
                }
                break;
            }
            case "LOW_STOCK": {
                if ("INVENTORY".equals(event.getOrderType())) {
                    try {
                        // Bóc tách ID Kho từ field đã "mượn"
                        Long warehouseId = Long.parseLong(event.getOriginalCreatorLogin());
                        String productInfo = event.getOrderCode(); // Tên sản phẩm + Số lượng còn lại

                        // Quét tìm Sếp phòng Mua hàng của đúng Kho/Chi nhánh đó
                        List<Employee> purchaseManagers = employeeRepository.findPurchaseManagersByBranch(
                            warehouseId,
                            AuthoritiesConstants.MANAGER
                        );

                        for (Employee manager : purchaseManagers) {
                            sendNotification(
                                manager,
                                "⚠️ [CẢNH BÁO TỒN KHO] Hàng sắp hết",
                                "Mã hàng " + productInfo + " tại chi nhánh của bạn đã chạm đáy an toàn. Vui lòng kiểm tra và lên đơn Mua hàng (Purchase Order) nhập bổ sung ngay!",
                                event.getOrderId() // Là Product ID để sếp bấm vào xem chi tiết mặt hàng
                            );
                        }
                    } catch (NumberFormatException e) {
                        // Bỏ qua nếu lỗi parse ID
                    }
                }
                break;
            }
        }
    }

    private void sendNotification(Employee recipient, String title, String message, Long refId) {
        NotificationDTO notif = new NotificationDTO();
        notif.setTitle(title);
        notif.setMessage(message);
        notif.setType(NotificationType.SYSTEM);
        notif.setReferenceId(refId);
        notif.setIsRead(false);
        notif.setCreatedAt(Instant.now());
        notif.setRecipient(employeeMapper.toDto(recipient));

        notificationService.save(notif);

        if (recipient.getUser() != null && recipient.getUser().getEmail() != null) {
            String toEmail = recipient.getUser().getEmail();
            mailService.sendEmail(toEmail, title, message, false, false);
        }
    }

    private String getOrderTypeName(String type) {
        if ("SALES".equals(type)) return "Đơn bán hàng";
        if ("PURCHASE".equals(type)) return "Đơn mua hàng";
        if ("TRANSFER".equals(type)) return "Phiếu điều chuyển";
        return "Chứng từ";
    }
}
