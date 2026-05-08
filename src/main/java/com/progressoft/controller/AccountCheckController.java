package com.progressoft.controller;

import com.progressoft.entity.AccountCheckRequest;
import com.progressoft.entity.ImportBatch;
import com.progressoft.repository.BatchRepository;
import com.progressoft.repository.RequestRepository;
import com.progressoft.service.ReconciliationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/account-checks")
public class AccountCheckController {

    private final ReconciliationService reconciliationService;
    private final BatchRepository batchRepository;
    private final RequestRepository requestRepository;

    public AccountCheckController(ReconciliationService reconciliationService, BatchRepository batchRepository, RequestRepository requestRepository) {
        this.reconciliationService = reconciliationService;
        this.batchRepository = batchRepository;
        this.requestRepository = requestRepository;
    }

    // 1. CSV Import Endpoint
    // POST /api/v1/account-checks/import
    // Expects a multipart file with the parameter name "file"
    @PostMapping("/import")
    public ResponseEntity<ImportBatch> importFile(@RequestParam("file") MultipartFile file) {
        try {
            ImportBatch result = reconciliationService.importCsv(file.getInputStream(), file.getOriginalFilename());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 2. Get Request by ID
    // GET /api/v1/account-checks/{requestId}
    @GetMapping("/{requestId}")
    public ResponseEntity<AccountCheckRequest> getByRequestId(@PathVariable String requestId) {
        return requestRepository.findByRequestId(requestId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Get Requests by Customer Reference
    // GET /api/v1/account-checks/customer/{customerReference}
    @GetMapping("/customer/{customerReference}")
    public List<AccountCheckRequest> getByCustomer(@PathVariable String customerReference) {
        return requestRepository.findByCustomerReference(customerReference);
    }

     // 4. List all Import Batches
     // GET /api/v1/account-checks/batches
    @GetMapping("/batches")
    public List<ImportBatch> getAllBatches() {
        return batchRepository.findAll();
    }

     // 5. List all rows that failed validation
     // GET /api/v1/account-checks/failed
    @GetMapping("/failed")
    public List<AccountCheckRequest> getFailedRows() {
        return requestRepository.findAll().stream()
                .filter(request -> "INVALID".equals(request.getStatus()))
                .collect(Collectors.toList());
    }

     // 6. List all rows that imported
     // GET /api/v1/account-checks/failed
    @GetMapping("/imported")
    public List<AccountCheckRequest> getImportedRows() {
        return requestRepository.findAll().stream()
                .filter(request -> "IMPORTED".equals(request.getStatus()))
                .collect(Collectors.toList());
    }
}
