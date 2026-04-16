package com.experiment.jpademo.config;

import com.experiment.jpademo.entity.*;
import com.experiment.jpademo.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds the database with sample data on application startup.
 * Demonstrates entity creation and relationship wiring.
 */
@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    @Transactional
    CommandLineRunner initDatabase(
            UserRepository userRepo,
            RoleRepository roleRepo,
            CategoryRepository categoryRepo,
            ProductRepository productRepo
    ) {
        return args -> {
            log.info("======== SEEDING DATABASE ========");

            // ---- Create Roles ----
            Role adminRole  = roleRepo.save(new Role("ADMIN", "Full access administrator"));
            Role userRole   = roleRepo.save(new Role("USER", "Standard user"));
            Role modRole    = roleRepo.save(new Role("MODERATOR", "Content moderator"));

            log.info("Created roles: {}", List.of(adminRole, userRole, modRole));

            // ---- Create Users and assign Roles (Many-to-Many) ----
            User alice = new User("alice", "alice@example.com", "password123");
            alice.addRole(adminRole);
            alice.addRole(userRole);
            userRepo.save(alice);

            User bob = new User("bob", "bob@example.com", "password456");
            bob.addRole(userRole);
            userRepo.save(bob);

            User charlie = new User("charlie", "charlie@example.com", "password789");
            charlie.addRole(modRole);
            charlie.addRole(userRole);
            userRepo.save(charlie);

            User diana = new User("diana", "diana@example.com", "password000");
            diana.addRole(adminRole);
            userRepo.save(diana);

            User eve = new User("eve", "eve@example.com", "password111");
            eve.addRole(userRole);
            userRepo.save(eve);

            log.info("Created users: alice (ADMIN, USER), bob (USER), charlie (MODERATOR, USER), diana (ADMIN), eve (USER)");

            // ---- Create Categories ----
            Category electronics = categoryRepo.save(new Category("Electronics", "Electronic gadgets and devices"));
            Category clothing    = categoryRepo.save(new Category("Clothing", "Apparel and fashion"));
            Category books       = categoryRepo.save(new Category("Books", "Physical and digital books"));
            Category sports      = categoryRepo.save(new Category("Sports", "Sports equipment and gear"));

            log.info("Created categories: {}", List.of(electronics, clothing, books, sports));

            // ---- Create Products under Categories (One-to-Many) ----
            // Electronics
            Product p1 = new Product("Laptop Pro 15", "High-performance laptop", new BigDecimal("1299.99"), 25);
            electronics.addProduct(p1);

            Product p2 = new Product("Wireless Earbuds", "Noise-cancelling earbuds", new BigDecimal("149.99"), 100);
            electronics.addProduct(p2);

            Product p3 = new Product("Smartphone X", "Latest flagship smartphone", new BigDecimal("999.99"), 50);
            electronics.addProduct(p3);

            Product p4 = new Product("Tablet Ultra", "10-inch tablet with stylus", new BigDecimal("599.99"), 30);
            electronics.addProduct(p4);

            // Clothing
            Product p5 = new Product("Denim Jacket", "Classic blue denim", new BigDecimal("89.99"), 200);
            clothing.addProduct(p5);

            Product p6 = new Product("Running Shoes", "Lightweight running shoes", new BigDecimal("129.99"), 150);
            clothing.addProduct(p6);

            Product p7 = new Product("Cotton T-Shirt", "Plain white cotton tee", new BigDecimal("24.99"), 500);
            clothing.addProduct(p7);

            // Books
            Product p8 = new Product("Java in Action", "Modern Java programming guide", new BigDecimal("49.99"), 80);
            books.addProduct(p8);

            Product p9 = new Product("Spring Boot Reference", "Comprehensive Spring Boot guide", new BigDecimal("59.99"), 60);
            books.addProduct(p9);

            Product p10 = new Product("Design Patterns", "GoF design patterns", new BigDecimal("44.99"), 5);
            books.addProduct(p10);

            // Sports
            Product p11 = new Product("Yoga Mat", "Non-slip yoga mat", new BigDecimal("29.99"), 300);
            sports.addProduct(p11);

            Product p12 = new Product("Mountain Bike", "21-speed mountain bike", new BigDecimal("799.99"), 15);
            sports.addProduct(p12);

            Product p13 = new Product("Tennis Racket", "Professional tennis racket", new BigDecimal("199.99"), 3);
            sports.addProduct(p13);

            // Save all products via cascade
            categoryRepo.saveAll(List.of(electronics, clothing, books, sports));

            log.info("Created {} products across {} categories",
                     productRepo.count(), categoryRepo.count());

            log.info("======== DATABASE SEEDING COMPLETE ========");
        };
    }
}
