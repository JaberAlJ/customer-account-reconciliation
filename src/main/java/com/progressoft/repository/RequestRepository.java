package com.progressoft.repository;

import com.progressoft.entity.AccountCheckRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<AccountCheckRequest, Long> {

    Optional<AccountCheckRequest> findByRequestId(String requestId);

    List<AccountCheckRequest> findByCustomerReference(String customerReference);
}
