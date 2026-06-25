package refactoring.payment;

public interface PaymentStrategy {
    boolean isSupported(String paymentMethod);
    void processPayment(double amount);
}
