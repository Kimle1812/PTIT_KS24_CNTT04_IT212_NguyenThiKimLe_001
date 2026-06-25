package refactoring.payment;

import org.springframework.stereotype.Component;

@Component
public class MomoPaymentStrategy implements PaymentStrategy {
    @Override
    public boolean isSupported(String paymentMethod) {
        return "MOMO".equalsIgnoreCase(paymentMethod);
    }

    @Override
    public void processPayment(double amount) {
        System.out.println("Connecting to Momo API to pay " + amount + "...");
    }
}
