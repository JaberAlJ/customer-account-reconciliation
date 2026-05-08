package com.progressoft;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class CustomerAccount {

    public enum IdentifierType {
        IBAN,
        ACCOUNT_NUMBER,
        CIF_NUMBER,
        UNKNOWN
    }

    // keep track of each identifier we process
    private static class CheckedIdentifier {
        int position;
        IdentifierType type;
        boolean isValid;
        String extractedCif;

        CheckedIdentifier(int position) {
            this.position = position;
            this.type = IdentifierType.UNKNOWN;
            this.isValid = false;
        }
    }

    private final List<String> identifiers;

    private CustomerAccount(List<String> identifiers) {
        this.identifiers = identifiers != null ? new ArrayList<>(identifiers) : new ArrayList<>();
    }

    public String getSummary() {
        // if there are no identifiers, return
        if (identifiers.isEmpty()) {
            return "Total Identifiers: 0, no identifiers provided";
        }

        List<CheckedIdentifier> results = new ArrayList<>();
        int total = identifiers.size();

        // 1. process and validate every identifier in the list
        for (int i = 0; i < total; i++) {
            String rawValue = identifiers.get(i);
            CheckedIdentifier result = validate(i + 1, rawValue);
            results.add(result);
        }

        // 2. start building the final string
        StringBuilder summary = new StringBuilder();
        summary.append("Total Identifiers: ").append(total).append(",");

        // 3. list the status of each identifier
        for (CheckedIdentifier result: results) {
            summary.append(" [").append(result.position).append("] ");

            // format the identifier type
            String typeName = formatTypeName(result.type);
            summary.append(typeName);

            summary.append(result.isValid ? " is valid," : " is invalid,");
        }

        // find the ownership match or not
        summary.append(buildOwnershipMessage(results));

        return summary.toString();
    }

    private CheckedIdentifier validate(int position, String value) {
        CheckedIdentifier result = new CheckedIdentifier(position);

        // null or blank is always invalid UNKNOWN
        if (value == null || value.trim().isEmpty()) {
            return result;
        }

        String cleanValue = value.trim().replace(" ", "");

        // check for IBAN identifier
        if (Character.isLetter(cleanValue.charAt(0)) && Character.isLetter(cleanValue.charAt(1)) && cleanValue.length() >= 15) {
            result.type = IdentifierType.IBAN;
            if (cleanValue.startsWith("OM") && isValidOmanIban(cleanValue)) {
                result.isValid = true;
                result.extractedCif = cleanValue.substring(13, 20);
            }
        }
        // check for Account Number identifier
        else if (cleanValue.length() == 14 && isAllDigits(cleanValue) && !isAllZeros(cleanValue)) {
            result.type = IdentifierType.ACCOUNT_NUMBER;
            result.isValid = true;
            result.extractedCif = cleanValue.substring(4, 11);
        }
        // check for CIF identifier
        else if (cleanValue.length() == 7 && isAllDigits(cleanValue) && !isAllZeros(cleanValue)) {
            result.type = IdentifierType.CIF_NUMBER;
            result.isValid = true;
            result.extractedCif = cleanValue;
        }

        // if none of the above match, it stays UNKNOWN and invalid
        return result;
    }

    // check Oman IBAN rules
    private boolean isValidOmanIban(String iban) {
        if (iban.length() != 23) return false;
        if (!isAlphanumeric(iban)) return false;

        // Mod-97 Checksum:
        // 1. Move first 4 characters (OMxx) to the end
        String rearranged = iban.substring(4) + iban.substring(0, 4);

        // 2. Replace letters with numbers (O=25, M=22)
        StringBuilder numericIban = new StringBuilder();
        for (char ch : rearranged.toCharArray()) {
            if (Character.isLetter(ch)) {
                numericIban.append(Character.getNumericValue(ch));
            } else {
                numericIban.append(ch);
            }
        }

        // 3. Convert to a massive number and check if modulo 97 is 1
        try {
            BigInteger bigNum = new BigInteger(numericIban.toString());
            return bigNum.mod(BigInteger.valueOf(97)).intValue() == 1;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isAlphanumeric(String s) {
        return s.matches("[A-Z0-9]+");
    }

    private boolean isAllDigits(String s) {
        return s.matches("\\d+");
    }

    private boolean isAllZeros(String s) {
        return s.matches("0+");
    }

    private String formatTypeName(IdentifierType type) {
        return switch (type) {
            case IBAN -> "IBAN";
            case ACCOUNT_NUMBER -> "Account number";
            case CIF_NUMBER -> "CIF";
            default -> "UNKNOWN";
        };
    }

    private String buildOwnershipMessage(List<CheckedIdentifier> results) {
        List<CheckedIdentifier> validResults = new ArrayList<>();
        for (CheckedIdentifier result : results) {
            if (result.isValid) validResults.add(result);
        }

        if (validResults.isEmpty()) {
            return " no valid identifiers";
        }

        // check if everyone matches the first person
        boolean allMatch = true;
        String firstCif = validResults.get(0).extractedCif;
        for (CheckedIdentifier v : validResults) {
            if (!v.extractedCif.equals(firstCif)) {
                allMatch = false;
                break;
            }
        }

        if (allMatch) {
            // if all match or only 1 identifier valid
            if (results.size() == validResults.size()) {
                return " all identifiers for same person";
            } else {
                // list only the valid identifiers that match
                StringBuilder message = new StringBuilder(" identifiers ");
                for (CheckedIdentifier validResult : validResults) {
                    message.append("[").append(validResult.position).append("]");
                }
                message.append(" for same person");
                return message.toString();
            }
        }

        // if they don't match, list the conflicts
        StringBuilder conflicts = new StringBuilder(" identifiers");
        boolean firstPair = true;
        for (int i = 0; i < validResults.size() - 1; i++) {
            for (int j = i + 1; j < validResults.size(); j++) {
                CheckedIdentifier a = validResults.get(i);
                CheckedIdentifier b = validResults.get(j);
                if (!a.extractedCif.equals(b.extractedCif)) {
                    if (!firstPair) conflicts.append(",");
                    conflicts.append(" [").append(a.position).append("][").append(b.position).append("] do not match");
                    firstPair = false;
                }
            }
        }
        return conflicts.toString();
    }

    public boolean hasValidIdentifiers() {
        for (String id : identifiers) {
            if (validate(0, id).isValid) return true;
        }
        return false;
    }

    public int getValidCount() {
        int count = 0;
        for (String id : identifiers) {
            if (validate(0, id).isValid) count++;
        }
        return count;
    }

    public static CustomerAccount of(List<String> identifiers) {
        return new CustomerAccount(identifiers);
    }
}
