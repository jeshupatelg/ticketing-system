package com.ticketing.repository;

import com.ticketing.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {
    Optional<Project> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
}
