package com.coderrr1ck.backend.product;

import com.coderrr1ck.backend.category.Category;
import com.coderrr1ck.backend.productImage.ProductImage;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.validator.constraints.URL;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID productId;

    @Column(length = 200, nullable = false,unique = true)
    private String name;

    private String description;

    @Min(value = 0, message = "Price cannot be negative")
    @Column(precision = 12, scale = 2 ,nullable = false)
    private BigDecimal price;

    @Min(value = 0, message = "Available Stock cannot be negative")
    @Column(nullable = false)
    private Integer availableStock;

    @Min(value = 0, message = "Reserved Stock cannot be negative")
    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer reservedStock = 0;

    @ManyToOne(fetch = FetchType.LAZY) // use inverse mapping if you want to fetch products from category
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "product",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_image_id")
    private ProductImage primaryImage;

    private boolean active = true;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String lastModifiedBy;

    public void addImage(ProductImage image) {
        this.images.add(image);
        image.setProduct(this);
    }
}
