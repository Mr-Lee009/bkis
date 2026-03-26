package vn.edu.bkis.model;

/**
 * Enum representing the status of a payment.
 */
public enum PaymentStatus {
    PENDING, // Payment is pending and has not been processed yet.
    COMPLETED, // Payment has been completed successfully.
    FAILED // Payment has failed due to an error or insufficient funds.
}
