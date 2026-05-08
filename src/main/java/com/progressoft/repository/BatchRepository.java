package com.progressoft.repository;

import com.progressoft.entity.ImportBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BatchRepository extends JpaRepository<ImportBatch, UUID> {
}
