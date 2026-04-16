package com.experiment.jpademo.controller;

import com.experiment.jpademo.entity.User;
import com.experiment.jpademo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller demonstrating queries on User entity.
 *
 * PART (b): Fetch related data (User ↔ Role Many-to-Many)
 * PART (c): JPQL filtering, sorting, pagination
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ---- PART (a): Basic CRUD ----

    /** GET /api/users — Fetch all users */
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /** GET /api/users/{id} — Fetch user by ID */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ---- PART (b): Fetch related data ----

    /** GET /api/users/by-role?role=ADMIN — Fetch users by role name */
    @GetMapping("/by-role")
    public List<User> getUsersByRole(@RequestParam String role) {
        return userRepository.findByRoleName(role);
    }

    // ---- PART (c): Custom JPQL with pagination & sorting ----

    /**
     * GET /api/users/by-role/paginated?role=USER&page=0&size=2&sort=username
     * Paginated users filtered by role.
     */
    @GetMapping("/by-role/paginated")
    public Page<User> getUsersByRolePaginated(
            @RequestParam String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "username") String sort) {
        return userRepository.findByRoleNamePaginated(role,
                PageRequest.of(page, size, Sort.by(sort)));
    }

    /** GET /api/users/search?keyword=ali — Search users by keyword */
    @GetMapping("/search")
    public List<User> searchUsers(@RequestParam String keyword) {
        return userRepository.searchByKeyword(keyword);
    }

    /** GET /api/users/count-by-role — Aggregation: count users per role */
    @GetMapping("/count-by-role")
    public List<Map<String, Object>> countUsersByRole() {
        return userRepository.countUsersByRole().stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("role", row[0]);
                    map.put("userCount", row[1]);
                    return map;
                })
                .collect(Collectors.toList());
    }
}
