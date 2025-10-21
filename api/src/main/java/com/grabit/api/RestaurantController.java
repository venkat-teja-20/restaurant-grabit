package com.grabit.api;

import com.grabit.bean.Restaurant.RestaurantDTO;
import com.grabit.service.RestaurantService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Log4j2
@RequestMapping(value = "/restaurant")
public class RestaurantController {

    @Autowired
    RestaurantService restaurantService;

    @PostMapping(value = "/add",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public RestaurantDTO addRestaurant(@RequestBody RestaurantDTO request, HttpServletResponse response){
        response.setStatus(201);
        return restaurantService.addNewRestaurant(request);
    }

    @PatchMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public RestaurantDTO updateRestaurant(@PathVariable(value = "id") String restaurantId,@RequestBody RestaurantDTO request, HttpServletResponse response){
        return restaurantService.updateRestaurant(restaurantId,request);
    }

    @GetMapping(value = "/{id}",produces = MediaType.APPLICATION_JSON_VALUE)
    public RestaurantDTO getRestaurant(@PathVariable(value = "id") String restaurantId){
        return restaurantService.getRestaurant(restaurantId);
    }

    @GetMapping(value = "/{id}/branch/{branchId}",produces = MediaType.APPLICATION_JSON_VALUE)
    public RestaurantDTO getRestaurantAndBranch(@PathVariable(value = "id") String restaurantId, @PathVariable(value = "branchId") String branchId){
        return restaurantService.getRestaurantAndBranchDetails(restaurantId,branchId);
    }

    @GetMapping(value = "/{id}/branch/{branchId}/name",produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String,String> getRestaurantAndBranchName(@PathVariable(value = "id") String restaurantId, @PathVariable(value = "branchId") String branchId){
        return restaurantService.getRestaurantAndBranchName(restaurantId,branchId);
    }

    @GetMapping(value = "/{id}/branch/{branchId}/exists",produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String,Object> checkRestaurantAndBranchExists(@PathVariable(value = "id") String restaurantId, @PathVariable(value = "branchId") String branchId){
        return restaurantService.checkRestaurantAndBranchExistence(restaurantId,branchId);
    }

    @GetMapping(value = "/{id}/exists",produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String,Object> checkRestaurantExists(@PathVariable(value = "id") String restaurantId){
        log.info("Restaurant Id : "+restaurantId);
        return restaurantService.checkRestaurantExists(restaurantId);
    }

    @PatchMapping(value = "/{id}/branch/{branchId}/orders/update",produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String,Object> updateRestaurantAndBranchOrders(@PathVariable(value = "id") String restaurantId, @PathVariable(value = "branchId") String branchId){
//        this request must be verified to come only from order service, implement this after learning OAuth or Spring Security
        return restaurantService.updateOrdersInRestaurantAndBranch(restaurantId,branchId);
    }

    @GetMapping(value = "/{id}/orders/branch/{branchId}",produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String,Object> getOrdersOfABranch(@PathVariable(value = "id") String restaurantId,@PathVariable(value = "branchId") String branchId,
                                                 @RequestParam(value = "page_number",required = false) int pageNumber,
                                                 @RequestParam(value = "page_size",required = false) int pageSize,
                                                 @RequestParam(value = "order_by", required = false) String orderBy,
                                                 @RequestParam(value = "field", required = false) String orderField){
        return restaurantService.getAllOrdersOfARestaurantBranch(restaurantId,branchId,pageNumber,pageSize,orderBy,orderField);
    }
}
