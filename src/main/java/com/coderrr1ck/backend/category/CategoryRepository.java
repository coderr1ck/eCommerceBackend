package com.coderrr1ck.backend.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    boolean existsByCategoryIdAndActiveTrue(UUID categoryId);

    boolean existsByNameAndActiveTrue(String name);
    Optional<Category> findByNameAndActiveFalse(String name);
    Page<Category> findByActiveTrue(Pageable pageable);
    Page<Category> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

}
