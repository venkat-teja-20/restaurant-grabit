package com.grabit.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "branch")
public class Branch extends AuditDetails<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "building",nullable = false,length = 50)
    @Size(min = 3,max = 50)
    private String building;

    @Column(name = "address_line",nullable = false,length = 100)
    @Size(max = 100)
    @NotBlank
    private String addressLine;

    @Column(name = "land_mark",length = 25)
    private String landMark;

    @Column(name = "pin_code",nullable = false,length = 6)
    @Size(min = 6,max = 6)
    private String pinCode;

    @Column(name = "latitude",nullable = false)
    private String latitude;

    @Column(name = "longitude",nullable = false)
    private String longitude;

    @Column(name = "branch_name",nullable = false,length = 20)
    @Size(min = 3, max = 20)
    private String branchName;

    @Column(name = "open_time",nullable = false)
    private LocalDateTime openTime;

    @Column(name = "close_time",nullable = false)
    private LocalDateTime closeTime;

    @Column(name = "orders_received")
    private Long ordersReceived;

    @Column(name = "food_serve_type",nullable = false)
    private String foodServeType;

    @Column(name = "branch_status")
    private String branchStatus;

    @Column(name = "branch_activity")
    private String branchActivity;

    @OneToMany(mappedBy = "branch",cascade = CascadeType.ALL)
    private List<FoodItem> foodItems=new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "restaurant_id",nullable = false)
    @JsonIgnore
    private Restaurant restaurant;
}
