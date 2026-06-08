package vn.edu.bkis.dto;

public record CoursePaymentGatewayDto(
        String code,
        String displayName,
        String providerType,
        String description,
        String badgeText,
        String badgeClass
) {
}
