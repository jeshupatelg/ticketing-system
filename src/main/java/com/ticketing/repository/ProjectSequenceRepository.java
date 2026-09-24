package com.ticketing.repository;

import com.ticketing.model.ProjectSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectSequenceRepository extends JpaRepository<ProjectSequence, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ProjectSequence s WHERE s.projectCode = :projectCode")
    Optional<ProjectSequence> findByProjectCodeForUpdate(@Param("projectCode") String projectCode);
}
