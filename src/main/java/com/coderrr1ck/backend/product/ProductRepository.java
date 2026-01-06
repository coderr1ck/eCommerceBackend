package com.coderrr1ck.backend.product;

import com.coderrr1ck.backend.category.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    boolean existsByNameAndActiveTrue(String name);
    Page<Product> findByActiveTrue(Pageable page);
    Optional<Product> findByNameAndActiveFalse(String name);
    Page<Product> findByNameRegexAndActiveTrue(String name, Pageable pageable);

}
