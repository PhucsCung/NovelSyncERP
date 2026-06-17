package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.domain.SalesOrder;
import com.mycompany.myapp.repository.PaymentRepository;
import com.mycompany.myapp.repository.SalesOrderRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.service.PaymentService;
import com.mycompany.myapp.service.VNPayService;
import com.mycompany.myapp.service.dto.PaymentDTO;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.myapp.domain.Payment}.
 */
@RestController
@RequestMapping("/api")
public class PaymentResource {

    private final Logger log = LoggerFactory.getLogger(PaymentResource.class);
    private static final String ENTITY_NAME = "payment";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    @Value("${jhipster.mail.base-url:http://localhost:3000/}")
    private String frontendBaseUrl;

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final VNPayService vnPayService;
    private final SalesOrderRepository salesOrderRepository;

    public PaymentResource(
        PaymentService paymentService,
        PaymentRepository paymentRepository,
        VNPayService vnPayService,
        SalesOrderRepository salesOrderRepository
    ) {
        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
        this.vnPayService = vnPayService;
        this.salesOrderRepository = salesOrderRepository;
    }

    @PreAuthorize(
        "hasAnyAuthority(\"" +
        AuthoritiesConstants.ADMIN +
        "\", \"" +
        AuthoritiesConstants.SHIPPER +
        "\", \"" +
        AuthoritiesConstants.ACCOUNTANT +
        "\")"
    )
    @PostMapping("/payments/create-vnpay-url")
    public ResponseEntity<Map<String, String>> createVNPayUrl(@RequestParam Long orderId, javax.servlet.http.HttpServletRequest request) {
        log.debug("REST request để tạo Link thanh toán VNPay cho đơn hàng: {}", orderId);

        // 1. Lấy đơn hàng từ DB để đảm bảo tính chính xác của số tiền
        SalesOrder order = salesOrderRepository
            .findById(orderId)
            .orElseThrow(() -> new BadRequestAlertException("Không tìm thấy đơn hàng", "salesOrder", "id_not_found"));

        // 2. Lấy địa chỉ IP của người dùng thao tác
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || "".equals(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // 3. Sinh URL thanh toán
        String paymentUrl = vnPayService.createPaymentUrl(orderId, order.getTotalAmount(), ipAddress);

        // Trả về Frontend dưới dạng JSON { "url": "https://sandbox.vnpay..." }
        Map<String, String> response = new HashMap<>();
        response.put("url", paymentUrl);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize(
        "hasAnyAuthority(\"" +
        AuthoritiesConstants.ADMIN +
        "\", \"" +
        AuthoritiesConstants.SHIPPER +
        "\", \"" +
        AuthoritiesConstants.ACCOUNTANT +
        "\")"
    )
    @GetMapping("/payments")
    public ResponseEntity<List<PaymentDTO>> getAllPayments(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        @RequestParam(required = false, defaultValue = "false") boolean eagerload
    ) {
        log.debug("REST request to get a page of Payments");
        Page<PaymentDTO> page;
        if (eagerload) {
            page = paymentService.findAllWithEagerRelationships(pageable);
        } else {
            page = paymentService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @PreAuthorize(
        "hasAnyAuthority(\"" +
        AuthoritiesConstants.ADMIN +
        "\", \"" +
        AuthoritiesConstants.SHIPPER +
        "\", \"" +
        AuthoritiesConstants.ACCOUNTANT +
        "\")"
    )
    @GetMapping("/payments/{id}")
    public ResponseEntity<PaymentDTO> getPayment(@PathVariable Long id) {
        log.debug("REST request to get Payment : {}", id);
        Optional<PaymentDTO> paymentDTO = paymentService.findOne(id);
        return ResponseUtil.wrapOrNotFound(paymentDTO);
    }

    @GetMapping("/payments/vnpay-webhook")
    public ResponseEntity<Map<String, String>> receiveVNPayIpn(@RequestParam Map<String, String> params) {
        log.debug("REST request to handle VNPay IPN: {}", params);
        return handleVNPayCallback(params);
    }

    @GetMapping("/payments/vnpay-return")
    public ResponseEntity<Void> receiveVNPayReturn(@RequestParam Map<String, String> params) {
        log.debug("REST request to handle VNPay ReturnUrl: {}", params);
        Map<String, String> callbackResult = handleVNPayCallback(params).getBody();
        String rspCode = callbackResult != null ? callbackResult.getOrDefault("RspCode", "99") : "99";
        String message = callbackResult != null
            ? callbackResult.getOrDefault("Message", "Unknown payment result")
            : "Unknown payment result";
        String status = "00".equals(rspCode) && "Confirm Success".equals(message) ? "success" : "failed";
        Long orderId = parseOrderId(params.get("vnp_TxnRef"));

        URI redirectUri = UriComponentsBuilder
            .fromUriString(frontendBaseUrl)
            .path(frontendBaseUrl.endsWith("/") ? "payments/vnpay-result" : "/payments/vnpay-result")
            .queryParam("status", status)
            .queryParam("code", rspCode)
            .queryParam("message", message)
            .queryParam("orderId", orderId)
            .build()
            .toUri();

        return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
    }

    /**
     * API giả lập nhận Webhook từ VNPay (Mở public hoặc yêu cầu token tùy ý bạn)
     */
    @PostMapping("/payments/vnpay-webhook")
    public ResponseEntity<Void> receiveVNPayWebhook(@RequestBody Map<String, Object> payload) {
        log.debug("REST request to handle VNPay Webhook: {}", payload);

        //        // 1. Kiểm tra chữ ký (Signature Validation) - CỰC KỲ QUAN TRỌNG
        //        boolean isValid = vnPayService.verifySignature(payload);
        //        if (!isValid) {
        //            log.error("CẢNH BÁO: Phát hiện request giả mạo webhook VNPay!");
        //            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        //        }

        // Tự động bóc tách data giả lập từ VNPay
        String bankTxnId = payload.get("vnp_TransactionNo").toString();
        BigDecimal amount = new BigDecimal(payload.get("vnp_Amount").toString()); // Thường VNPay x100, bạn tự chia lại nếu cần
        String content = payload.get("vnp_OrderInfo").toString();

        Long customerId = payload.containsKey("customerId") ? Long.valueOf(payload.get("customerId").toString()) : null;
        Long orderId = payload.containsKey("orderId") ? Long.valueOf(payload.get("orderId").toString()) : null;

        paymentService.handleBankWebhook(bankTxnId, amount, content, customerId, orderId);

        return ResponseEntity.ok().build();
    }

    private ResponseEntity<Map<String, String>> handleVNPayCallback(Map<String, String> params) {
        if (!vnPayService.verifySignature(params)) {
            log.error("CẢNH BÁO: VNPay callback sai chữ ký: {}", params);
            return vnpayResponse("97", "Invalid signature");
        }

        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");
        if (!"00".equals(responseCode) || !"00".equals(transactionStatus)) {
            log.warn(
                "VNPay giao dịch không thành công, bỏ qua tạo payment. ResponseCode={}, TransactionStatus={}",
                responseCode,
                transactionStatus
            );
            return vnpayResponse("00", "Payment failed; ignored");
        }

        Long orderId = parseOrderId(params.get("vnp_TxnRef"));
        if (orderId == null) {
            return vnpayResponse("01", "Order not found");
        }

        SalesOrder order = salesOrderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return vnpayResponse("01", "Order not found");
        }

        BigDecimal vnpAmount = new BigDecimal(params.getOrDefault("vnp_Amount", "0"));
        BigDecimal expectedAmount = order.getTotalAmount().multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP);
        if (vnpAmount.compareTo(expectedAmount) != 0) {
            log.error("VNPay amount không khớp. orderId={}, expected={}, actual={}", orderId, expectedAmount, vnpAmount);
            return vnpayResponse("04", "Invalid amount");
        }

        String bankTxnId = params.get("vnp_TransactionNo");
        if (bankTxnId == null || bankTxnId.isBlank()) {
            bankTxnId = params.get("vnp_TxnRef");
        }

        BigDecimal amount = vnpAmount.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        Long customerId = order.getCustomer() != null ? order.getCustomer().getId() : null;
        String content = params.getOrDefault("vnp_OrderInfo", "VNPay payment for SalesOrder " + orderId);

        paymentService.handleBankWebhook(bankTxnId, amount, content, customerId, orderId);

        return vnpayResponse("00", "Confirm Success");
    }

    private Long parseOrderId(String txnRef) {
        if (txnRef == null || txnRef.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(txnRef.split("_")[0]);
        } catch (NumberFormatException e) {
            log.error("Không parse được orderId từ vnp_TxnRef={}", txnRef);
            return null;
        }
    }

    private ResponseEntity<Map<String, String>> vnpayResponse(String code, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("RspCode", code);
        response.put("Message", message);
        return ResponseEntity.ok(response);
    }

    /**
     * API dành cho Kế Toán hoặc Admin duyệt phiếu thanh toán (Chuyển sang COMPLETED)
     */
    @PutMapping("/payments/{id}/approve")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.ACCOUNTANT + "\")")
    public ResponseEntity<PaymentDTO> approvePayment(@PathVariable Long id) {
        log.debug("REST request to approve and reconcile Payment : {}", id);

        PaymentDTO result = paymentService.approveAndReconcile(id);

        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * API dành riêng cho Kế toán tạo Phiếu Chi sau khi đã chuyển khoản thật cho Nhà cung cấp.
     * Phương thức POST nhưng không dùng hàm save() mặc định để đảm bảo an toàn luồng tiền.
     */
    @PostMapping("/payments/disbursement")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.ACCOUNTANT + "\")")
    public ResponseEntity<PaymentDTO> createDisbursement(
        @RequestParam Long supplierId,
        @RequestParam BigDecimal amount,
        @RequestParam(required = false, defaultValue = "Thanh toán công nợ Nhà cung cấp") String note
    ) throws URISyntaxException {
        log.debug("REST request để Kế toán tạo Phiếu chi trả Nhà cung cấp: ID {}, Số tiền {}", supplierId, amount);

        // Validation cơ bản ở tầng API
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestAlertException("Số tiền chi phải lớn hơn 0", ENTITY_NAME, "invalid_amount");
        }

        PaymentDTO result = paymentService.createSupplierDisbursement(supplierId, amount, note);

        return ResponseEntity
            .created(new URI("/api/payments/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }
}
