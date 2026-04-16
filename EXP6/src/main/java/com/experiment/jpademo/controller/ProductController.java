package com.experiment.jpademo.controller;

import com.experiment.jpademo.entity.Product;
import com.experiment.jpademo.repository.ProductRepository;
import com.experiment.jpademo.specification.ProductSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller demonstrating queries on Product entity.
 *
 * PART (b): Fetch related data (Product → Category One-to-Many)
 * PART (c): JPQL & Criteria API queries with filtering, sorting, pagination
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ---- PART (a): Basic CRUD ----

    /** GET /api/products — Fetch all products */
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /** GET /api/products/{id} — Fetch product by ID */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ---- PART (c): JPQL Queries ----

    /**
     * GET /api/products/by-price?min=50&max=200
     * Filter products by price range using JPQL.
     */
    @GetMapping("/by-price")
    public List<Product> getProductsByPriceRange(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        return productRepository.findByPriceRange(min, max);
    }

    /**
     * GET /api/products/by-price/paginated?min=10&max=500&page=0&size=3&sort=price
     * Filter products by price range with pagination using JPQL.
     */
    @GetMapping("/by-price/paginated")
    public Page<Product> getProductsByPriceRangePaginated(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "price") String sort) {
        return productRepository.findByPriceRangePaginated(min, max,
                PageRequest.of(page, size, Sort.by(sort)));
    }

    /**
     * GET /api/products/by-category?name=Electronics
     * Filter products by category name using JPQL JOIN.
     */
    @GetMapping("/by-category")
    public List<Product> getProductsByCategory(@RequestParam String name) {
        return productRepository.findByCategoryName(name);
    }

    /**
     * GET /api/products/search?keyword=laptop&page=0&size=5
     * Search products by name keyword with pagination.
     */
    @GetMapping("/search")
    public Page<Product> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return productRepository.searchByName(keyword, PageRequest.of(page, size, Sort.by("name")));
    }

    /**
     * GET /api/products/avg-price-by-category
     * Aggregation: average price per category.
     */
    @GetMapping("/avg-price-by-category")
    public List<Map<String, Object>> getAveragePriceByCategory() {
        return productRepository.getAveragePriceByCategory().stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("category", row[0]);
                    map.put("averagePrice", row[1]);
                    map.put("productCount", row[2]);
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * GET /api/products/top-expensive?limit=5
     * Fetch top N most expensive products.
     */
    @GetMapping("/top-expensive")
    public List<Product> getTopExpensiveProducts(@RequestParam(defaultValue = "5") int limit) {
        return productRepository.findTopExpensiveProducts(PageRequest.of(0, limit));
    }

    /**
     * GET /api/products/low-stock?threshold=10
     * Fetch products with stock below threshold.
     */
    @GetMapping("/low-stock")
    public List<Product> getLowStockProducts(@RequestParam(defaultValue = "10") int threshold) {
        return productRepository.findLowStockProducts(threshold);
    }

    // ---- PART (c): Criteria API Queries ----

    /**
     * GET /api/products/filter?minPrice=50&maxPrice=500&category=Electronics&keyword=laptop&minStock=10&page=0&size=5
     * Dynamic filtering using Criteria API (JPA Specifications).
     *
     * All parameters are optional — specifications are composed dynamically.
     */
    @GetMapping("/filter")
    public Page<Product> filterProducts(
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer minStock,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "price") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        // Build dynamic specification by composing individual predicates
        Specification<Product> spec = Specification.where(null);

        if (minPrice != null && maxPrice != null) {
            spec = spec.and(ProductSpecification.priceBetween(minPrice, maxPrice));
        } else if (maxPrice != null) {
            spec = spec.and(ProductSpecification.priceLessThanOrEqual(maxPrice));
        }

        if (category != null && !category.isBlank()) {
            spec = spec.and(ProductSpecification.categoryNameEquals(category));
        }

        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and(ProductSpecification.nameContains(keyword));
        }

        if (minStock != null) {
            spec = spec.and(ProductSpecification.stockGreaterThanOrEqual(minStock));
        }

        Sort sortOrder = direction.equalsIgnoreCase("desc")
                ? Sort.by(sort).descending()
                : Sort.by(sort).ascending();

        return productRepository.findAll(spec, PageRequest.of(page, size, sortOrder));
    }
}
