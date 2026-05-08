package com.progressoft.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "account_check_requests")
public class AccountCheckRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id")
    private UUID batchId;

    @Column(unique = true)
    private String requestId;

    private String customerReference;

    private String identifier1;
    private String identifier2;
    private String identifier3;

    private LocalDateTime submittedAt;

    @Column(length = 1000)
    private String validationSummary;

    private String ownershipResult;

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getBatchId() {
        return batchId;
    }

    public void setBatchId(UUID batchId) {
        this.batchId = batchId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getCustomerReference() {
        return customerReference;
    }

    public void setCustomerReference(String customerReference) {
        this.customerReference = customerReference;
    }

    public String getIdentifier1() {
        return identifier1;
    }

    public void setIdentifier1(String identifier1) {
        this.identifier1 = identifier1;
    }

    public String getIdentifier2() {
        return identifier2;
    }

    public void setIdentifier2(String identifier2) {
        this.identifier2 = identifier2;
    }

    public String getIdentifier3() {
        return identifier3;
    }

    public void setIdentifier3(String identifier3) {
        this.identifier3 = identifier3;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getValidationSummary() {
        return validationSummary;
    }

    public void setValidationSummary(String validationSummary) {
        this.validationSummary = validationSummary;
    }

    public String getOwnershipResult() {
        return ownershipResult;
    }

    public void setOwnershipResult(String ownershipResult) {
        this.ownershipResult = ownershipResult;
    }
}
