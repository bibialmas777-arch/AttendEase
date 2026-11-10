package com.attendease.repository;

import com.attendease.model.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByClassEntityId(Long classId);
    
    @Query("SELECT s FROM Section s JOIN FETCH s.classEntity")
    List<Section> findAllWithClass();

    @Query("SELECT s FROM Section s JOIN FETCH s.classEntity WHERE s.classEntity.id = :classId")
    List<Section> findByClassIdWithClass(@Param("classId") Long classId);
}

