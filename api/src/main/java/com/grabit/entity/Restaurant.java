package com.grabit.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "restaurant")
@Table(name = "restaurant",uniqueConstraints = {
        @UniqueConstraint(name = "RESTAURANT_NAME_UNIQUE",columnNames = "restaurant_name")
})
@Setter
@Getter
public class Restaurant extends AuditDetails<String>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurant_name",nullable = false,length = 75)
    @Size(min = 3,max = 75)
    private String restaurantName;

    @Column(name = "total_orders")
    private Long totalOrders;

    @OneToMany(mappedBy = "restaurant",cascade = CascadeType.ALL)
    private List<Branch> branches=new ArrayList<>();
}
