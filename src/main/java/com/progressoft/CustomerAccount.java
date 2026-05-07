package com.progressoft;

import java.util.ArrayList;
import java.util.List;

public class CustomerAccount {

    public enum IdentifierType {
        IBAN, ACCOUNT_NUMBER, CIF_NUMBER, UNKNOWN
    }

    private final List<String> identifiers;

    private CustomerAccount(List<String> identifiers) {
        this.identifiers = identifiers != null ? new ArrayList<>(identifiers) : new ArrayList<>();
    }

    public String getSummary() {
        // Starter implementation. Candidates should replace this with the
        // validation and ownership-summary behavior described in Task.md.
        return "";

    }

    public static CustomerAccount of(List<String> identifiers) {
        return new CustomerAccount(identifiers);
    }
}
