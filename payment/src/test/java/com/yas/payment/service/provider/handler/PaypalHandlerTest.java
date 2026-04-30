package com.yas.payment.service.provider.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.payment.model.CapturedPayment;
import com.yas.payment.model.InitiatedPayment;
import com.yas.payment.model.enumeration.PaymentMethod;
import com.yas.payment.model.enumeration.PaymentStatus;
import com.yas.payment.paypal.service.PaypalService;
import com.yas.payment.paypal.viewmodel.PaypalCapturePaymentRequest;
import com.yas.payment.paypal.viewmodel.PaypalCapturePaymentResponse;
import com.yas.payment.paypal.viewmodel.PaypalCreatePaymentRequest;
import com.yas.payment.paypal.viewmodel.PaypalCreatePaymentResponse;
import com.yas.payment.service.PaymentProviderService;
import com.yas.payment.viewmodel.CapturePaymentRequestVm;
import com.yas.payment.viewmodel.InitPaymentRequestVm;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaypalHandlerTest {

    private PaymentProviderService paymentProviderService;
    private PaypalService paypalService;
    private PaypalHandler paypalHandler;

    @BeforeEach
    void setUp() {
        paymentProviderService = mock(PaymentProviderService.class);
        paypalService = mock(PaypalService.class);
        paypalHandler = new PaypalHandler(paymentProviderService, paypalService);
    }

    @Test
    void getProviderId_ShouldReturnPaypal() {
        assertThat(paypalHandler.getProviderId()).isEqualTo(PaymentMethod.PAYPAL.name());
    }

    @Test
    void initPayment_ShouldReturnInitiatedPayment() {
        // Arrange
        InitPaymentRequestVm requestVm = InitPaymentRequestVm.builder()
            .totalPrice(new BigDecimal("100.00"))
            .checkoutId("checkout-123")
            .paymentMethod(PaymentMethod.PAYPAL.name())
            .build();

        when(paymentProviderService.getAdditionalSettingsByPaymentProviderId(anyString()))
            .thenReturn("dummy-settings");

        PaypalCreatePaymentResponse paypalResponse = PaypalCreatePaymentResponse.builder()
            .status("CREATED")
            .paymentId("PAY-123")
            .redirectUrl("http://paypal.com/checkout")
            .build();

        when(paypalService.createPayment(any(PaypalCreatePaymentRequest.class))).thenReturn(paypalResponse);

        // Act
        InitiatedPayment result = paypalHandler.initPayment(requestVm);

        // Assert
        assertThat(result.getStatus()).isEqualTo("CREATED");
        assertThat(result.getPaymentId()).isEqualTo("PAY-123");
        assertThat(result.getRedirectUrl()).isEqualTo("http://paypal.com/checkout");
        verify(paymentProviderService).getAdditionalSettingsByPaymentProviderId(PaymentMethod.PAYPAL.name());
    }

    @Test
    void capturePayment_ShouldReturnCapturedPayment() {
        // Arrange
        CapturePaymentRequestVm requestVm = CapturePaymentRequestVm.builder()
            .token("token-abc")
            .paymentMethod(PaymentMethod.PAYPAL.name())
            .build();

        when(paymentProviderService.getAdditionalSettingsByPaymentProviderId(anyString()))
            .thenReturn("dummy-settings");

        PaypalCapturePaymentResponse paypalResponse = PaypalCapturePaymentResponse.builder()
            .checkoutId("checkout-123")
            .amount(new BigDecimal("100.00"))
            .paymentFee(new BigDecimal("2.50"))
            .gatewayTransactionId("txn-xyz")
            .paymentMethod(PaymentMethod.PAYPAL.name())
            .paymentStatus(PaymentStatus.COMPLETED.name())
            .failureMessage(null)
            .build();

        when(paypalService.capturePayment(any(PaypalCapturePaymentRequest.class))).thenReturn(paypalResponse);

        // Act
        CapturedPayment result = paypalHandler.capturePayment(requestVm);

        // Assert
        assertThat(result.getCheckoutId()).isEqualTo("checkout-123");
        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.PAYPAL);
        assertThat(result.getAmount()).isEqualTo(new BigDecimal("100.00"));
        verify(paymentProviderService).getAdditionalSettingsByPaymentProviderId(PaymentMethod.PAYPAL.name());
    }
}