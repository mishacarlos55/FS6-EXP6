package com.experiment.jpademo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

/**
 * PART (b): Many-to-Many with User
 *
 * A Role can be assigned to many Users.
 */
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Role name is required")
    @Column(nullable = false, unique = true, length = 30)
    private String name;

    @Column(length = 200)
    private String description;

    // =============================================
    // Many-to-Many (inverse side – mapped by User)
    // =============================================
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @JsonIgnore  // Prevent infinite recursion in JSON serialization
    private Set<User> users = new HashSet<>();

    // ---- Constructors ----
    public Role() {}

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // ---- Getters and Setters ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Set<User> getUsers() { return users; }
    public void setUsers(Set<User> users) { this.users = users; }

    @Override
    public String toString() {
        return "Role{id=" + id + ", name='" + name + "'}";
    }
}
