package com.workbridge.workbridge_app.dto;

import lombok.Data;

@Data
public class InvoiceDTO {
    private Long id;
    private String jobName;
    private String providerName;
    private String clientName;
    private String clientEmail;
    private String country;
    private String region;
    private String issueDate;
    private String dueDate;
    private Double totalAmount;
    private String currency;
}
