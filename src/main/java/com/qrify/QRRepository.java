package com.qrify;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QRRepository extends JpaRepository<QRCodeRecord, Long> {
    QRCodeRecord findByCustomUniqueSlug(String customUniqueSlug);
}