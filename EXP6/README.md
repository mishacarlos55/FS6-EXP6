# Experiment 6 — Configure JPA & Hibernate with MySQL/PostgreSQL

Configure JPA & Hibernate to model entity relationships and execute queries.

## Tech Stack

- **Java 17** + **Spring Boot 3.2.5**
- **Spring Data JPA** + **Hibernate ORM**
- **H2 Database** (default) / **MySQL** (switchable)
- **Maven** build tool

## Project Structure

```
src/main/java/com/experiment/jpademo/
├── JpaDemoApplication.java              # Main class
├── config/
│   └── DataInitializer.java             # Seeds sample data on startup
├── entity/
│   ├── User.java                        # Many-to-Many with Role
│   ├── Role.java                        # Many-to-Many with User
│   ├── Category.java                    # One-to-Many with Product
│   └── Product.java                     # Many-to-One with Category
├── repository/
│   ├── UserRepository.java              # JPQL queries for User
│   ├── RoleRepository.java              # Basic repository
│   ├── CategoryRepository.java          # Basic repository
│   └── ProductRepository.java           # JPQL + Criteria API
├── specification/
│   └── ProductSpecification.java        # Criteria API specifications
└── controller/
    ├── UserController.java              # User REST endpoints
    ├── ProductController.java           # Product REST endpoints
    └── CategoryController.java          # Category REST endpoints
```

## How to Run

```bash
# Clone and run (H2 in-memory, no setup needed)
./mvnw spring-boot:run

# Application: http://localhost:8080
# H2 Console:  http://localhost:8080/h2-console
```

To use **MySQL** instead of H2, edit `src/main/resources/application.properties`:
- Uncomment the MySQL configuration lines
- Comment out the H2 configuration lines
- Set your MySQL username and password

---

## Part (a): Database Connectivity & Simple JPA Entity

### Database Configuration (`application.properties`)

```properties
# H2 In-Memory (default)
spring.datasource.url=jdbc:h2:mem:jpa_experiment_db
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# MySQL (uncomment to switch)
# spring.datasource.url=jdbc:mysql://localhost:3306/jpa_experiment_db?createDatabaseIfNotExist=true
# spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### Simple JPA Entity — `User.java`

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Column(nullable = false, unique = true)
    private String username;

    @Email @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }
}
```

### JPA Repository — `UserRepository.java`

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
```

---

## Part (b): Entity Relationships

### Many-to-Many: User ↔ Role

```
┌─────────┐       ┌─────────────┐       ┌─────────┐
│  users  │──M:N──│ user_roles  │──M:N──│  roles  │
└─────────┘       └─────────────┘       └─────────┘
```

**User (owning side):**
```java
@ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
@JoinTable(name = "user_roles",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id"))
private Set<Role> roles = new HashSet<>();
```

**Role (inverse side):**
```java
@ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
@JsonIgnore
private Set<User> users = new HashSet<>();
```

### One-to-Many: Category → Product

```
┌────────────┐         ┌────────────┐
│ categories │──1:N──▶│  products  │
└────────────┘         └────────────┘
```

**Category (parent):**
```java
@OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Product> products = new ArrayList<>();
```

**Product (child):**
```java
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "category_id", nullable = false)
private Category category;
```

### Sample Data (auto-seeded)

| Users | Roles | Categories | Products |
|-------|-------|------------|----------|
| alice (ADMIN, USER) | ADMIN | Electronics (4 products) | Laptop Pro 15, Wireless Earbuds, Smartphone X, Tablet Ultra |
| bob (USER) | USER | Clothing (3 products) | Denim Jacket, Running Shoes, Cotton T-Shirt |
| charlie (MODERATOR, USER) | MODERATOR | Books (3 products) | Java in Action, Spring Boot Reference, Design Patterns |
| diana (ADMIN) | | Sports (3 products) | Yoga Mat, Mountain Bike, Tennis Racket |
| eve (USER) | | | |

---

## Part (c): Custom JPQL & Criteria API Queries

### JPQL Queries

**Products by price range:**
```java
@Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice ORDER BY p.price ASC")
List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
```
Generated SQL: `SELECT * FROM products WHERE price BETWEEN ? AND ? ORDER BY price ASC`

**Users by role (Many-to-Many JOIN):**
```java
@Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
List<User> findByRoleName(@Param("roleName") String roleName);
```
Generated SQL: `SELECT u.* FROM users u JOIN user_roles ur ON u.id = ur.user_id JOIN roles r ON ur.role_id = r.id WHERE r.name = ?`

**Paginated price range:**
```java
@Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
Page<Product> findByPriceRangePaginated(..., Pageable pageable);
```

**Aggregation — average price per category:**
```java
@Query("SELECT c.name, AVG(p.price), COUNT(p) FROM Product p JOIN p.category c GROUP BY c.name")
List<Object[]> getAveragePriceByCategory();
```

### Criteria API (JPA Specifications)

```java
// Dynamic filter composition
Specification<Product> spec = Specification.where(null);

if (minPrice != null && maxPrice != null)
    spec = spec.and(ProductSpecification.priceBetween(minPrice, maxPrice));
if (category != null)
    spec = spec.and(ProductSpecification.categoryNameEquals(category));
if (keyword != null)
    spec = spec.and(ProductSpecification.nameContains(keyword));

productRepository.findAll(spec, PageRequest.of(page, size, sortOrder));
```

---

## API Endpoints

### Users
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | All users |
| GET | `/api/users/{id}` | User by ID |
| GET | `/api/users/by-role?role=ADMIN` | Users by role (JPQL) |
| GET | `/api/users/by-role/paginated?role=USER&page=0&size=2` | Paginated by role |
| GET | `/api/users/search?keyword=ali` | Keyword search |
| GET | `/api/users/count-by-role` | Count per role (aggregation) |

### Products
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | All products |
| GET | `/api/products/{id}` | Product by ID |
| GET | `/api/products/by-price?min=50&max=200` | JPQL price filter |
| GET | `/api/products/by-price/paginated?min=10&max=500&page=0&size=3&sort=price` | Paginated price filter |
| GET | `/api/products/by-category?name=Electronics` | JPQL category filter |
| GET | `/api/products/search?keyword=laptop&page=0&size=5` | Paginated name search |
| GET | `/api/products/avg-price-by-category` | Average price aggregation |
| GET | `/api/products/top-expensive?limit=5` | Top N expensive |
| GET | `/api/products/low-stock?threshold=10` | Low stock products |
| GET | `/api/products/filter?minPrice=50&maxPrice=500&category=Electronics` | **Criteria API** dynamic filter |

### Categories
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/categories` | All categories |
| GET | `/api/categories/{id}` | Category by ID |
"# exp6_fsd" 
