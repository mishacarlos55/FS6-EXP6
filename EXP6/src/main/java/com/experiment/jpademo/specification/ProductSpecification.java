package com.experiment.jpademo.specification;

import com.experiment.jpademo.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * PART (c): Criteria API Queries using JPA Specifications.
 *
 * Each method returns a Specification<Product> that can be composed
 * with other specifications using .and(), .or(), .not() for complex queries.
 *
 * These specifications are translated by Hibernate into SQL WHERE clauses.
 */
public class ProductSpecification {

    /**
     * Filter by price range.
     *
     * Equivalent SQL: WHERE p.price BETWEEN :min AND :max
     */
    public static Specification<Product> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.between(root.get("price"), min, max);
    }

    /**
     * Filter by product name containing keyword (case-insensitive).
     *
     * Equivalent SQL: WHERE LOWER(p.name) LIKE '%keyword%'
     */
    public static Specification<Product> nameContains(String keyword) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get("name")),
                "%" + keyword.toLowerCase() + "%"
            );
    }

    /**
     * Filter by category name.
     *
     * Equivalent SQL: WHERE c.name = :categoryName (with JOIN on category)
     */
    public static Specification<Product> categoryNameEquals(String categoryName) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("category").get("name"), categoryName);
    }

    /**
     * Filter by minimum stock quantity.
     *
     * Equivalent SQL: WHERE p.stock_quantity >= :minStock
     */
    public static Specification<Product> stockGreaterThanOrEqual(int minStock) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.greaterThanOrEqualTo(root.get("stockQuantity"), minStock);
    }

    /**
     * Filter by maximum price.
     *
     * Equivalent SQL: WHERE p.price <= :maxPrice
     */
    public static Specification<Product> priceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    /**
     * Sort results by price ascending.
     *
     * Equivalent SQL: ORDER BY p.price ASC
     */
    public static Specification<Product> orderByPriceAsc() {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(criteriaBuilder.asc(root.get("price")));
            return criteriaBuilder.conjunction(); // always true (no additional filter)
        };
    }
}
