package com.ecodana.evodanavn1.repository;

import com.ecodana.evodanavn1.model.WithdrawalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, String> {
    List<WithdrawalRequest> findByOwnerId(String ownerId);
    List<WithdrawalRequest> findByOwnerIdAndStatus(String ownerId, WithdrawalRequest.WithdrawalStatus status);
    List<WithdrawalRequest> findByStatus(WithdrawalRequest.WithdrawalStatus status);
}
