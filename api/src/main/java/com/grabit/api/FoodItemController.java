package com.grabit.api;

import com.grabit.bean.Restaurant.FoodItemDTO;
import com.grabit.service.FoodItemService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/items")
public class FoodItemController {

    @Autowired
    FoodItemService foodItemService;

    @PostMapping(value = "/add/{restaurantId}/{branchId}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FoodItemDTO> addFoodItems(@RequestBody List<FoodItemDTO> request,@PathVariable(value = "restaurantId") String restaurantId,@PathVariable(value = "branchId") String branchId, HttpServletResponse response){
        response.setStatus(201);
        return foodItemService.addFoodItemToTheBranch(request,restaurantId,branchId);
    }

    @GetMapping(value = "/{id}/exists",produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String,Object> checkFoodItemExists(@PathVariable(value = "id") String idemId){
        return foodItemService.checkItemExists(idemId);
    }

    @GetMapping(value = "/{id}/branch/{branchId}/exists",produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String,Object> checkFoodItemExistsInABranch(@PathVariable(value = "id") String itemId, @PathVariable(value = "branchId") String branchId,@RequestParam(value = "item_details",required = false) String itemDetailsStatus){
        return foodItemService.checkItemExistsInGivenBranch(itemId,branchId,itemDetailsStatus);
    }

    @GetMapping(value = "/{id}",produces = MediaType.APPLICATION_JSON_VALUE)
    public FoodItemDTO getFoodItemDetails(@PathVariable(value = "id") String itemId){
        return foodItemService.getFoodItemDetailsById(itemId);
    }

    @GetMapping(value = "/{id}/branch/{branchId}",produces = MediaType.APPLICATION_JSON_VALUE)
    public FoodItemDTO getAFoodItemOfBranch(@PathVariable(value = "id") String itemId, @PathVariable(value = "branchId") String branchId){
        return foodItemService.getFoodItemDetailsOfABranch(itemId,branchId);
    }

    @PatchMapping(value = "/ordered/update",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String,Object> updateFoodItemOrdersAndAvailability(@RequestBody List<FoodItemDTO> request, @RequestHeader Map<String,String> headers){
        return foodItemService.updateOrdersAndAvailabilityOfAFoodItem(request);
    }
}
