package com.coderrr1ck.backend.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends MongoRepository<Category,String> {
    boolean existsByCategoryIdAndActiveTrue(String categoryId);

    boolean existsByNameAndActiveTrue(String name);
    Optional<Category> findByNameAndActiveFalse(String name);
    Page<Category> findByActiveTrue(Pageable pageable);
    Page<Category> findByNameRegexAndActiveTrue(String name, Pageable pageable);

}
