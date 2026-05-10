package com.internship.tool.repository;

import com.internship.tool.entity.Risk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RiskRepository extends JpaRepository<Risk, Long> {
    
    Page<Risk> findByIsDeletedFalse(Pageable pageable);
    
    @Query("SELECT r FROM Risk r WHERE r.isDeleted = false AND " +
           "(LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.category) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Risk> searchRisks(@Param("query") String query, Pageable pageable);
    
    @Query("SELECT COUNT(r) FROM Risk r WHERE r.isDeleted = false")
    long countActiveRisks();
}
