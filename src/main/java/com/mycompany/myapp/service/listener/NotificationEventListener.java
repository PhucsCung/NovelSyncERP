package com.mycompany.myapp.service.listener;

import com.mycompany.myapp.domain.Employee;
import com.mycompany.myapp.domain.enumeration.NotificationType;
import com.mycompany.myapp.repository.EmployeeRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.service.MailService;
import com.mycompany.myapp.service.NotificationService;
import com.mycompany.myapp.service.dto.NotificationDTO;
import com.mycompany.myapp.service.event.OrderNotificationEvent;
import com.mycompany.myapp.service.mapper.EmployeeMapper;
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
        List<Employee> recipients = resolveRecipients(event.getOriginalCreatorLogin());
        if (recipients.isEmpty()) return;

        String typeName = getOrderTypeName(event.getOrderType());

        for (Employee recipient : recipients) {
            switch (event.getAction()) {
                case "CREATED":
                    sendNotification(
                        recipient,
                        "Đã lưu nháp " + typeName,
                        "Đơn " + event.getOrderCode() + " của bạn đã được tạo nháp thành công và đang chờ duyệt.",
                        event.getOrderId()
                    );
                    break;
                case "APPROVED":
                    sendNotification(
                        recipient,
                        capitalize(typeName) + " đã được duyệt",
                        "Đơn " + event.getOrderCode() + " đã được duyệt. Hệ thống đã tự động xử lý kho theo luồng.",
                        event.getOrderId()
                    );
                    break;
                case "PROCESSING":
                    sendNotification(
                        recipient,
                        capitalize(typeName) + " đang xử lý",
                        "Đơn " + event.getOrderCode() + " đã chuyển sang trạng thái đang xử lý.",
                        event.getOrderId()
                    );
                    break;
                case "ARRIVED":
                    sendNotification(
                        recipient,
                        capitalize(typeName) + " đã tới nơi",
                        "Đơn " + event.getOrderCode() + " đã tới nơi. Vui lòng kiểm tra và tiếp tục bước tiếp theo.",
                        event.getOrderId()
                    );
                    break;
                case "DELIVERED_WAITING_PAYMENT":
                    sendNotification(
                        recipient,
                        "Đơn bán hàng chờ ghi nhận thanh toán",
                        "Đơn " + event.getOrderCode() + " đã giao xong. Kế toán vui lòng kiểm tra dòng tiền để chốt sổ.",
                        event.getOrderId()
                    );
                    break;
                case "COMPLETED":
                    sendNotification(
                        recipient,
                        "Hoàn thành " + typeName,
                        "Đơn " + event.getOrderCode() + " đã hoàn tất. Công nợ/tồn kho đã được ghi nhận thành công.",
                        event.getOrderId()
                    );
                    break;
                case "CANCELLED":
                    sendNotification(
                        recipient,
                        capitalize(typeName) + " đã bị hủy",
                        "Đơn " +
                        event.getOrderCode() +
                        " đã bị hủy bởi " +
                        event.getActionByUserLogin() +
                        ". Dữ liệu tồn kho/công nợ liên quan đã được hoàn trả.",
                        event.getOrderId()
                    );
                    break;
                default:
                    break;
            }
        }
    }

    private List<Employee> resolveRecipients(String target) {
        Optional<String> authority = resolveGroupAuthority(target);
        if (authority.isPresent()) {
            return employeeRepository.findByUserAuthority(authority.get());
        }

        return employeeRepository.findByUserLogin(target).map(List::of).orElse(List.of());
    }

    private Optional<String> resolveGroupAuthority(String target) {
        if ("WAREHOUSE_GROUP".equals(target)) return Optional.of(AuthoritiesConstants.WAREHOUSE);
        if ("ACCOUNTANT_GROUP".equals(target)) return Optional.of(AuthoritiesConstants.ACCOUNTANT);
        if ("MANAGER_GROUP".equals(target)) return Optional.of(AuthoritiesConstants.MANAGER);
        if ("SHIPPER_GROUP".equals(target)) return Optional.of(AuthoritiesConstants.SHIPPER);
        return Optional.empty();
    }

    private void sendNotification(Employee recipient, String title, String message, Long refId) {
        NotificationDTO notification = new NotificationDTO();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(NotificationType.SYSTEM);
        notification.setReferenceId(refId);
        notification.setIsRead(false);
        notification.setCreatedAt(Instant.now());
        notification.setRecipient(employeeMapper.toDto(recipient));

        notificationService.save(notification);
        if (recipient.getUser() != null && recipient.getUser().getEmail() != null) {
            mailService.sendEmail(recipient.getUser().getEmail(), title, message, false, false);
        }
    }

    private String getOrderTypeName(String type) {
        if ("SALES".equals(type)) return "đơn bán hàng";
        if ("PURCHASE".equals(type)) return "đơn mua hàng";
        if ("TRANSFER".equals(type)) return "phiếu điều chuyển";
        return "chứng từ";
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) return value;
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }
}
