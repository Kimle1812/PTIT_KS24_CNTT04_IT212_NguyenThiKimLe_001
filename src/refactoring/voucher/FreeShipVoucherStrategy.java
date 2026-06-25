package refactoring.voucher;

import org.springframework.stereotype.Component;

@Component
public class FreeShipVoucherStrategy implements VoucherStrategy {
    @Override
    public boolean isApplicable(String voucherCode) {
        return voucherCode != null && voucherCode.startsWith("FREESHIP");
    }

    @Override
    public double applyDiscount(double total, String voucherCode) {
        return total - 30000;
    }
}
