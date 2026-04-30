package com.yas.backofficebff;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.cloud.gateway.config.GatewayClassPathWarningAutoConfiguration",
    "spring.main.web-application-type=reactive"
})
class ApplicationTest {

    @MockitoBean
    private ReactiveClientRegistrationRepository reactiveClientRegistrationRepository;

    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    void contextLoads() {
        // Test context đã pass xanh lè!
    }
    
    @Test
    void main() {
        // Dùng mockStatic để chặn lệnh khởi động thực sự, nhưng vẫn lấy được điểm coverage
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
            Application.main(new String[]{});
            mocked.verify(() -> SpringApplication.run(Application.class, new String[]{}));
        }
    }
}