package com.budgettracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "name"})
})
@Getter
@Setter
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    private String color;

    private String icon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    public Category(String name, TransactionType type, String color, String icon, User user) {
        this.name = name;
        this.type = type;
        this.color = color;
        this.icon = icon;
        this.user = user;
    }
}