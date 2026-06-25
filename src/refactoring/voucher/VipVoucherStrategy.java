package refactoring.voucher;

import org.springframework.stereotype.Component;

@Component
public class VipVoucherStrategy implements VoucherStrategy {
    @Override
    public boolean isApplicable(String voucherCode) {
        return voucherCode != null && voucherCode.startsWith("VIP");
    }

    @Override
    public double applyDiscount(double total, String voucherCode) {
        return total * 0.8;
    }
}
