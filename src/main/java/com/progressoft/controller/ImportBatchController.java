package com.progressoft.controller;

import com.progressoft.entity.ImportBatch;
import com.progressoft.repository.BatchRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/import-batches")
public class ImportBatchController {

    private final BatchRepository batchRepository;

    public ImportBatchController(BatchRepository batchRepository) {
        this.batchRepository = batchRepository;
    }

    // 1. List all Import Batches
    // GET /api/v1/import-batches
    @GetMapping
    public List<ImportBatch> getAllBatches() {
        return batchRepository.findAll();
    }

    // 2. Get Batch Summary by ID
    // GET /api/v1/import-batches/{batchId}
    @GetMapping("/{batchId}")
    public ResponseEntity<ImportBatch> getBatchById(@PathVariable java.util.UUID batchId) {
        return batchRepository.findById(batchId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
