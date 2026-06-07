package com.mycompany.myapp.config;

import com.mycompany.myapp.security.jwt.TokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);
    private final TokenProvider tokenProvider; // Tiêm bộ xử lý mã hóa Token vào đây

    public WebSocketConfig(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Mở 2 kênh: /topic (loa phường cho tất cả) và /queue (tai nghe riêng cho từng người)
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Cấu hình cổng kết nối gốc cho hệ thống
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    /**
     * CẤU HÌNH BẢO MẬT: Đánh chặn đường truyền tin nhắn để ép xác thực Token JWT
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(
            new ChannelInterceptor() {
                @Override
                public Message<?> preSend(Message<?> message, MessageChannel channel) {
                    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                    // Khi client gửi lệnh CONNECT (yêu cầu thiết lập đường ống lắng nghe thông báo)
                    if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                        // Trích xuất mã Token từ Header "X-Authorization" hoặc "Authorization" do client gửi lên
                        String bearerToken = accessor.getFirstNativeHeader("X-Authorization");

                        if (!StringUtils.hasText(bearerToken)) {
                            bearerToken = accessor.getFirstNativeHeader("Authorization");
                        }

                        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                            String jwt = bearerToken.substring(7);
                            // Kiểm tra tính hợp lệ của chữ ký Token dưới Database
                            if (tokenProvider.validateToken(jwt)) {
                                Authentication authentication = tokenProvider.getAuthentication(jwt);
                                // Gắn danh tính người dùng hợp lệ vào luồng xử lý của WebSocket
                                accessor.setUser(authentication);
                                log.debug("Xác thực WebSocket thành công cho tài khoản: {}", authentication.getName());
                                return message;
                            }
                        }

                        // Nếu không có Token hoặc Token hết hạn -> Bẻ gãy kết nối, từ chối lệnh kết nối
                        log.error("Kết nối WebSocket bị từ chối do thiếu hoặc sai Token bảo mật!");
                        throw new IllegalArgumentException("Từ chối quyền truy cập đường ống kết nối!");
                    }
                    return message;
                }
            }
        );
    }
}
