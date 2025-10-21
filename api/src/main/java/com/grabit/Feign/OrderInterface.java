package com.grabit.Feign;

import com.grabit.bean.Order.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("ORDERSERVICE")
public interface OrderInterface {
    @GetMapping(value = "/{id}",produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderDTO getOrderById(@PathVariable(value = "id") String orderId);
}
