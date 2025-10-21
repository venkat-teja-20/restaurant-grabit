package com.grabit.bean.Restaurant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestaurantDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("restaurant_name")
    private String restaurantName;

    @JsonProperty("total_orders")
    private Long totalOrders;

    @JsonProperty("branch_details")
    private List<BranchDTO> branchDTOList=new ArrayList<>();
}
