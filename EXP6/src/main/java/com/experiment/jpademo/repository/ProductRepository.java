package com.experiment.jpademo.repository;

import com.experiment.jpademo.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * PART (a): Simple JPA Repository for Product entity.
 * PART (c): Custom JPQL queries + Criteria API support (via JpaSpecificationExecutor).
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    // =============================================
    // PART (c): Custom JPQL Queries
    // =============================================

    /**
     * Find products within a price range using JPQL.
     * Generated SQL:
     *   SELECT p.* FROM products p
     *   WHERE p.price BETWEEN ? AND ?
     *   ORDER BY p.price ASC
     */
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice ORDER BY p.price ASC")
    List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Find products by price range with pagination.
     */
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByPriceRangePaginated(@Param("minPrice") BigDecimal minPrice,
                                            @Param("maxPrice") BigDecimal maxPrice,
                                            Pageable pageable);

    /**
     * Find products by category name using JPQL JOIN.
     * Generated SQL:
     *   SELECT p.* FROM products p
     *   JOIN categories c ON p.category_id = c.id
     *   WHERE c.name = ?
     */
    @Query("SELECT p FROM Product p JOIN p.category c WHERE c.name = :categoryName")
    List<Product> findByCategoryName(@Param("categoryName") String categoryName);

    /**
     * Search products by name keyword (case-insensitive) with pagination.
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchByName(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Get average price per category using JPQL aggregation.
     */
    @Query("SELECT c.name, AVG(p.price), COUNT(p) FROM Product p JOIN p.category c " +
           "GROUP BY c.name ORDER BY AVG(p.price) DESC")
    List<Object[]> getAveragePriceByCategory();

    /**
     * Find top N expensive products.
     */
    @Query("SELECT p FROM Product p ORDER BY p.price DESC")
    List<Product> findTopExpensiveProducts(Pageable pageable);

    /**
     * Find products with stock below a threshold.
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity < :threshold ORDER BY p.stockQuantity ASC")
    List<Product> findLowStockProducts(@Param("threshold") int threshold);
}
