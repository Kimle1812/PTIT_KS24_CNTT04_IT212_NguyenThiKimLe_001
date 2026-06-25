package refactoring.voucher;

public interface VoucherStrategy {
    boolean isApplicable(String voucherCode);
    double applyDiscount(double total, String voucherCode);
}
