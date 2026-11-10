package com.attendease.repository;

import com.attendease.model.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    Optional<Faculty> findByUserId(Long userId);

    @Query("SELECT f FROM Faculty f JOIN FETCH f.user u WHERE u.active = true")
    List<Faculty> findAllActiveWithDetails();

    @Query("SELECT f FROM Faculty f JOIN FETCH f.user u WHERE f.id = :id")
    Optional<Faculty> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT f FROM Faculty f JOIN FETCH f.user u WHERE u.id = :userId")
    Optional<Faculty> findByUserIdWithDetails(@Param("userId") Long userId);
}

