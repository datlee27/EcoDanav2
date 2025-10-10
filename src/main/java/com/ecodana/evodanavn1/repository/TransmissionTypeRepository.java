package com.ecodana.evodanavn1.repository;

import com.ecodana.evodanavn1.model.TransmissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransmissionTypeRepository extends JpaRepository<TransmissionType, Integer> {
    Optional<TransmissionType> findByTransmissionTypeName(String transmissionTypeName);
    boolean existsByTransmissionTypeName(String transmissionTypeName);
}
