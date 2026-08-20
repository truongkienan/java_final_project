package com.ecommerce.payment.config;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayPalConfig {

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    @Value("${paypal.mode}")
    private String mode;

    @Bean
    public PayPalHttpClient payPalHttpClient() {
        // Khởi tạo môi trường dựa trên biến paypal.mode
        PayPalEnvironment environment;
        if ("sandbox".equalsIgnoreCase(mode)) {
            environment = new PayPalEnvironment.Sandbox(clientId, clientSecret);
        } else {
            environment = new PayPalEnvironment.Live(clientId, clientSecret);
        }

        // Trả về một PayPalHttpClient dùng chung cho toàn bộ ứng dụng
        return new PayPalHttpClient(environment);
    }
}
