package refactoring.payment;

import org.springframework.stereotype.Component;

@Component
public class VnPayPaymentStrategy implements PaymentStrategy {
    @Override
    public boolean isSupported(String paymentMethod) {
        return "VNPAY".equalsIgnoreCase(paymentMethod);
    }

    @Override
    public void processPayment(double amount) {
        System.out.println("Connecting to VNPay API to pay " + amount + "...");
    }
}
