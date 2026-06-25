package refactoring.order;

import refactoring.voucher.VoucherStrategy;
import refactoring.payment.PaymentStrategy;
import refactoring.notification.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final List<VoucherStrategy> voucherStrategies;
    private final List<PaymentStrategy> paymentStrategies;
    private final NotificationService notificationService;

    public OrderService(List<VoucherStrategy> voucherStrategies,
                        List<PaymentStrategy> paymentStrategies,
                        NotificationService notificationService) {
        this.voucherStrategies = voucherStrategies;
        this.paymentStrategies = paymentStrategies;
        this.notificationService = notificationService;
    }

    public Order checkout(Cart cart, User user, String paymentMethod, String voucherCode) {
        if (user.getStatus() != 1) {
            throw new RuntimeException("User locked");
        }

        double total = cart.getItems().stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        if (voucherCode != null) {
            total = voucherStrategies.stream()
                    .filter(strategy -> strategy.isApplicable(voucherCode))
                    .findFirst()
                    .map(strategy -> strategy.applyDiscount(total, voucherCode))
                    .orElse(total);
        }

        double finalTotal = total;
        PaymentStrategy paymentStrategy = paymentStrategies.stream()
                .filter(strategy -> strategy.isSupported(paymentMethod))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Payment method " + paymentMethod + " not supported"));

        paymentStrategy.processPayment(finalTotal);

        Order order = new Order(user, finalTotal, "SUCCESS");
        notificationService.sendOrderConfirmation(user, order);

        return order;
    }
}
