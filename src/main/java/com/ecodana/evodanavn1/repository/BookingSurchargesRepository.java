package com.ecodana.evodanavn1.repository;

import com.ecodana.evodanavn1.model.BookingSurcharges;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingSurchargesRepository extends JpaRepository<BookingSurcharges, String> {
}