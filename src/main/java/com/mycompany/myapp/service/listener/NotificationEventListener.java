package com.mycompany.myapp.service.listener;

import com.mycompany.myapp.domain.Employee;
import com.mycompany.myapp.domain.enumeration.NotificationType;
import com.mycompany.myapp.repository.EmployeeRepository;
import com.mycompany.myapp.service.MailService;
import com.mycompany.myapp.service.NotificationService;
import com.mycompany.myapp.service.dto.NotificationDTO;
import com.mycompany.myapp.service.event.OrderNotificationEvent;
import com.mycompany.myapp.service.mapper.EmployeeMapper;
import java.time.Instant;
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

    public NotificationEventListener(
        NotificationService notificationService,
        EmployeeRepository employeeRepository,
        EmployeeMapper employeeMapper,
        MailService mailService
    ) {
        this.notificationService = notificationService;
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
        this.mailService = mailService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderNotificationEvent(OrderNotificationEvent event) {
        // Lấy thông tin người chủ của đơn hàng
        Optional<Employee> creatorOpt = employeeRepository.findByUserLogin(event.getOriginalCreatorLogin());
        if (creatorOpt.isEmpty()) return;
        Employee originalCreator = creatorOpt.get();

        String typeName = getOrderTypeName(event.getOrderType()); // Chuyển tiếng Anh sang Tiếng Việt

        switch (event.getAction()) {
            case "CREATED":
                // 1. Thông báo cho chính nhân viên tạo đơn
                sendNotification(
                    originalCreator,
                    "✅ Đã lưu nháp " + typeName,
                    "Đơn " + event.getOrderCode() + " của bạn đã được tạo nháp thành công và đang chờ duyệt.",
                    event.getOrderId()
                );

                // 2. Thông báo cho Sếp (Quản lý trực tiếp)
                if (originalCreator.getManager() != null) {
                    sendNotification(
                        originalCreator.getManager(),
                        "🔔 Có " + typeName + " mới cần duyệt!",
                        "Nhân viên " + originalCreator.getFullName() + " vừa tạo đơn: " + event.getOrderCode(),
                        event.getOrderId()
                    );
                }
                break;
            case "APPROVED":
                // Báo cho nhân viên biết Sếp đã duyệt
                sendNotification(
                    originalCreator,
                    "🎉 " + typeName + " ĐÃ ĐƯỢC DUYỆT!",
                    "Sếp đã duyệt đơn " + event.getOrderCode() + " của bạn. Hệ thống đã tự động xử lý kho.",
                    event.getOrderId()
                );
                break;
            case "COMPLETED":
                sendNotification(
                    originalCreator,
                    "🏆 Hoàn thành " + typeName,
                    "Đơn " + event.getOrderCode() + " đã hoàn tất. Đã ghi nhận công nợ thành công.",
                    event.getOrderId()
                );
                break;
            case "CANCELLED":
                sendNotification(
                    originalCreator,
                    "❌ " + typeName + " ĐÃ BỊ HỦY",
                    "Đơn " +
                    event.getOrderCode() +
                    " đã bị hủy bởi " +
                    event.getActionByUserLogin() +
                    ". Tồn kho/công nợ đã được hoàn trả.",
                    event.getOrderId()
                );
                break;
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
            // Hàm sendEmail mặc định của JHipster (to, subject, content, isMultipart, isHtml)
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
