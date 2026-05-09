package com.progressoft.service;

import com.progressoft.CustomerAccount;
import com.progressoft.entity.AccountCheckRequest;
import com.progressoft.entity.ImportBatch;
import com.progressoft.repository.BatchRepository;
import com.progressoft.repository.RequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class ReconciliationService {

    private final BatchRepository batchRepository;
    private final RequestRepository requestRepository;

    public ReconciliationService(BatchRepository batchRepository, RequestRepository requestRepository) {
        this.batchRepository = batchRepository;
        this.requestRepository = requestRepository;
    }

    @Transactional
    public ImportBatch importCsv(InputStream inputStream, String fileName) {
        // create new "Batch" to track this import session
        ImportBatch batch = new ImportBatch();
        batch.setFileName(fileName);
        batch.setImportDate(LocalDateTime.now());
        batch = batchRepository.save(batch);

        Set<String> seenInFile = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                batch.setTotalRows(batch.getTotalRows() + 1);

                String[] columns = line.split(",");
                if (columns.length < 6) {
                    saveInvalidRow(batch, null, null, null, "Malformed CSV row (too few columns)");
                    continue;
                }

                String requestId = columns[0].trim();
                String customerRef = columns[1].trim();
                String identifier1 = columns[2].trim();
                String identifier2 = columns[3].trim();
                String identifier3 = columns[4].trim();
                String submittedAtStr = columns[5].trim();

                // check for missing requestId field
                if (requestId.isEmpty()) {
                    String finalRequestId = "INVALID-REQ-" + java.util.UUID.randomUUID();
                    saveInvalidRow(batch, finalRequestId, customerRef, null, "Missing request_id");
                    continue;
                }

                // check for duplicate requestId, either from this batch session or from the requestRepository
                if (seenInFile.contains(requestId) || requestRepository.findByRequestId(requestId).isPresent()) {
                    batch.setDuplicateRows(batch.getDuplicateRows() + 1);
                    continue;
                }
                seenInFile.add(requestId);

                // check for other missing fields
                if (customerRef.isEmpty() || identifier1.isEmpty() || submittedAtStr.isEmpty()) {
                    saveInvalidRow(batch, requestId, customerRef, null, "Missing required fields");
                    continue;
                }

                // validate submission at
                LocalDateTime submittedAt;
                try {
                    submittedAt = LocalDateTime.parse(submittedAtStr, DateTimeFormatter.ISO_DATE_TIME);
                } catch (Exception e) {
                    saveInvalidRow(batch, requestId, customerRef, null, "Invalid timestamp format: " + submittedAtStr);
                    continue;
                }

                // validate the identifiers
                CustomerAccount account = CustomerAccount.of(Arrays.asList(identifier1, identifier2, identifier3));
                String summaryString = account.getSummary();

                AccountCheckRequest request = new AccountCheckRequest();
                request.setBatch(batch);
                request.setRequestId(requestId);
                request.setCustomerReference(customerRef);
                request.setIdentifier1(identifier1);
                request.setIdentifier2(identifier2);
                request.setIdentifier3(identifier3);
                request.setSubmittedAt(submittedAt);
                request.setValidationSummary(summaryString);
                request.setOwnershipResult(determineOwnershipResult(summaryString));

                batch.getRows().add(request);

                if (!account.hasValidIdentifiers()) {
                    request.setStatus("INVALID");
                    request.setFailureReason("No valid identifiers provided");
                    batch.setInvalidRows(batch.getInvalidRows() + 1);
                } else {
                    request.setStatus("IMPORTED");
                    batch.setImportedRows(batch.getImportedRows() + 1);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading CSV file: " + e.getMessage());
        }

        return batchRepository.save(batch);
    }

    private void saveInvalidRow(ImportBatch batch, String requestId, String customerRef, LocalDateTime submittedAt, String reason) {
        AccountCheckRequest request = new AccountCheckRequest();

        request.setBatch(batch);
        if (requestId == null || requestId.trim().isEmpty()) {
            requestId = "INVALID-REQ-" + java.util.UUID.randomUUID();
        }

        request.setRequestId(requestId);
        request.setCustomerReference(customerRef);
        request.setSubmittedAt(submittedAt != null ? submittedAt : LocalDateTime.now());
        request.setStatus("INVALID");
        request.setFailureReason(reason);
        request.setOwnershipResult("NO_VALID_IDENTIFIERS");
        request.setValidationSummary(reason);

        batch.getRows().add(request);
        batch.setInvalidRows(batch.getInvalidRows() + 1);
    }

    private String determineOwnershipResult(String summary) {
        if (summary.contains("no valid identifiers")) {
            return "NO_VALID_IDENTIFIERS";
        } else if (summary.contains("do not match")) {
            return "MISMATCH";
        } else {
            return "SAME_PERSON";
        }
    }
}
