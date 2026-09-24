package com.ticketing.repository;

import com.ticketing.model.PredefinedPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PredefinedPhotoRepository extends JpaRepository<PredefinedPhoto, Long> {
    List<PredefinedPhoto> findByCategoryOrCategory(String category1, String category2);
}
