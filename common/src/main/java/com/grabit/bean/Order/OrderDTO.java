package com.grabit.bean.Order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class OrderDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("restaurant_id")
    private Long restaurantId;

    @JsonProperty("restaurant_name")
    private String restaurantName;

    @JsonProperty("branch_name")
    private String branchName;

    @JsonProperty("member_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long memberId;

    @JsonProperty("gst")
    private Double gst;

    @JsonProperty("delivery_charges")
    private Double deliveryCharges;

    @JsonProperty("tip_amount")
    private Double tipAmount= (double) 0;

    @JsonProperty("total_price")
    private Double totalPrice;

    @JsonProperty("order_status")
    private String orderStatus;

    @JsonProperty("delivery_partner_id")
    private Long deliveryPartnerId;

    @JsonProperty("order_items")
    private List<OrderItemDTO> orderItemDTOList=new ArrayList<>();
}
