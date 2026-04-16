package com.experiment.jpademo.repository;

import com.experiment.jpademo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * PART (a): Simple JPA Repository
 * PART (c): Custom JPQL queries for filtering, sorting, and pagination
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // =============================================
    // PART (a): Basic derived query methods
    // =============================================
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByUsernameContainingIgnoreCase(String keyword);

    // =============================================
    // PART (c): Custom JPQL Queries
    // =============================================

    /**
     * Find users by role name using JPQL JOIN.
     * Generated SQL:
     *   SELECT u.* FROM users u
     *   JOIN user_roles ur ON u.id = ur.user_id
     *   JOIN roles r ON ur.role_id = r.id
     *   WHERE r.name = ?
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

    /**
     * Find users by role name with pagination & sorting.
     * Demonstrates paginated JPQL.
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Page<User> findByRoleNamePaginated(@Param("roleName") String roleName, Pageable pageable);

    /**
     * Find users whose username or email contains a keyword (case-insensitive).
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Count users per role using JPQL aggregation.
     */
    @Query("SELECT r.name, COUNT(u) FROM User u JOIN u.roles r GROUP BY r.name ORDER BY COUNT(u) DESC")
    List<Object[]> countUsersByRole();
}
