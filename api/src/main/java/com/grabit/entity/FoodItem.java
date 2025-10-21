package com.grabit.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "food_item")
@Table(name = "food_item",uniqueConstraints = {
        @UniqueConstraint(name = "ITEM_NAME_UNIQUE",columnNames = "item_name")
})
@Setter
@Getter
public class FoodItem extends AuditDetails<String>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_name",length = 50,
            nullable = false,unique = true)
    @Size(min = 3,max = 50)
    private String itemName;

    @Column(name = "serve_count",nullable = false)
    private Integer serveCount;

    @Column(name = "description",length = 500)
    @Size(max = 500)
    private String foodDescription;

    @Column(name = "category",length = 50)
    @Size(max = 50)
    private String category;

    @Column(name = "item_status",nullable = false)
    private String itemStatus;

    @Column(name = "item_type",length = 10,
            nullable = false)
    @Size(max = 10)
    private String itemType;

    @Column(name = "item_price",nullable = false)
    private Integer itemPrice;

    @Column(name = "item_quantity", nullable = false)
    private Integer itemQuantity;

    @Column(name = "item_original_price", nullable = false)
    private Double itemOriginalPrice;

    @Column(name = "image")
    private String image;

    @Column(name = "order_count",nullable = false)
    private Long orderCount;

    @Column(name = "quantity_sold")
    private Long quantitySold;

    @ManyToOne
    @JoinColumn(name = "branch_id",nullable = false)
    @JsonIgnore
    private Branch branch;
}
