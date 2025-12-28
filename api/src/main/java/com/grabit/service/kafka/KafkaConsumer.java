package com.grabit.service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grabit.Utilities.Utility;
import com.grabit.bean.Restaurant.FoodItemDTO;
import com.grabit.entity.Branch;
import com.grabit.entity.FoodItem;
import com.grabit.entity.Restaurant;
import com.grabit.enums.ItemStatus;
import com.grabit.exception.CustomException;
import com.grabit.repository.BranchRepository;
import com.grabit.repository.FoodItemRepository;
import com.grabit.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Log4j2
public class KafkaConsumer {

    private RestaurantRepository restaurantRepository;

    private BranchRepository branchRepository;

    private FoodItemRepository foodItemRepository;

    public KafkaConsumer(FoodItemRepository foodItemRepository, RestaurantRepository restaurantRepository, BranchRepository branchRepository) {
        this.foodItemRepository = foodItemRepository;
        this.restaurantRepository = restaurantRepository;
        this.branchRepository = branchRepository;
    }

    @KafkaListener(topics = "update_restaurant_and_branch_orders",
            groupId = "restaurant"
    )
    @Transactional
    public void updateOrdersInRestaurantAndBranch(Map<String,String> ids) {
        log.info("Restaurant and Branch Order update event received");
        String restaurantId=ids.get("restaurant_id");
        String branchId=ids.get("branch_id");
        isValidRestaurantId(restaurantId,"updateOrdersInRestaurantAndBranch");
        isValidBranchId(branchId,"updateOrdersInRestaurantAndBranch");
        Branch branch=branchRepository.findById(Long.valueOf(branchId)).orElseThrow(()->new EntityNotFoundException("Branch does not exists with branch id : "+branchId));
        branch.setOrdersReceived(branch.getOrdersReceived()+1);
        branchRepository.save(branch);
        Restaurant restaurant=restaurantRepository.findById(Long.valueOf(restaurantId)).orElseThrow(()->new EntityNotFoundException("Restaurant does not exists with restaurant id : "+restaurantId));
        restaurant.setTotalOrders(restaurant.getTotalOrders()+1);
        restaurantRepository.save(restaurant);
        log.info("Restaurant and Branch Order update event completed");
    }

    @KafkaListener(topics = "update_item_quantity_and_sold_quantity",
            groupId = "food_item"
    )
    @Transactional
    public void updateOrdersAndAvailabilityOfAFoodItem(List<Object> rawData){
        try {
            log.info("Item Quantity and Item Sold Quantity update event received");
            ObjectMapper mapper = new ObjectMapper();
            List<FoodItemDTO> request = rawData.stream()
                    .map(obj -> mapper.convertValue(obj, FoodItemDTO.class))
                    .toList();
            Map<Long, Integer> idsIndex = new HashMap<>();
            List<Long> itemsList = request.stream().map(foodItemDTO -> {
                idsIndex.put(foodItemDTO.getId(), foodItemDTO.getItemQuantity());
                return foodItemDTO.getId();
            }).sorted().toList();
            List<FoodItem> foodItemList = foodItemRepository.findAllById(itemsList);
            if (foodItemList.isEmpty())
                throw new EntityNotFoundException("Items does not exist");
            if (foodItemList.size() != itemsList.size()) {
                List<Long> receivedIds = foodItemList.stream().map(FoodItem::getId).toList();
                List<Long> misingIds = itemsList.stream().filter(id -> !receivedIds.contains(id)).toList();
                throw new CustomException(Utility.buildErrorObject("FOOD_ITEMS_MISSING", "Some Food Items are Missing : " + misingIds, 404, "updateOrdersAndAvailabilityOfAFoodItem"));
            }
            List<FoodItem> modifiedFoodItemList = foodItemList.stream().map(foodItem -> {
                Long itemId = foodItem.getId();
                foodItem.setOrderCount(foodItem.getOrderCount() + 1);
                if ("AVAILABLE".equals(foodItem.getItemStatus()) && foodItem.getItemQuantity() >= idsIndex.get(itemId)) {
                    foodItem.setItemQuantity(foodItem.getItemQuantity() - idsIndex.get(itemId));
                    foodItem.setQuantitySold(foodItem.getQuantitySold() + idsIndex.get(itemId));
                    if (Objects.equals(foodItem.getItemQuantity(), idsIndex.get(itemId)))
                        foodItem.setItemStatus(ItemStatus.OUT_OF_STOCK.toString());
                } else if ("AVAILABLE".equals(foodItem.getItemStatus()) && foodItem.getItemQuantity() < idsIndex.get(itemId) && foodItem.getItemQuantity() != 0) {
                    throw new CustomException(Utility.buildErrorObject("QUANTITY_NOT_AVAILABLE", "Only " + foodItem.getItemQuantity() + " items are in stock for food item : " + itemId, 409, "updateOrdersAndAvailabilityOfAFoodItem"));
                } else if ("OUT_OF_STOCK".equals(foodItem.getItemStatus())) {
                    throw new CustomException(Utility.buildErrorObject("OUT_OF_STOCK", "The items are out of stock for item : " + itemId, 409, "updateOrdersAndAvailabilityOfAFoodItem"));
                } else if ("REMOVED".equals(foodItem.getItemStatus())) {
                    throw new CustomException(Utility.buildErrorObject("UNACCEPTABLE_FOOD_ITEM", "The item is currently not selling by the restaurant : " + itemId, 409, "updateOrdersAndAvailabilityOfAFoodItem"));
                }
                return foodItem;
            }).toList();
            foodItemRepository.saveAll(modifiedFoodItemList);
            log.info("Item Quantity and Item Sold Quantity update event completed");
        } catch (RuntimeException e) {
            log.info(e);
            throw e;
        }
    }


    @KafkaListener(topics = {
            "update_restaurant_and_branch_orders.DLT",
            "update_item_quantity_and_sold_quantity.DLT"
    },
            groupId = "dlt"
    )
    public void dltListenerForRestaurantOrders(ConsumerRecord<Object,Object> record){
        log.info("DLT message received : "+record.value());
    }

    private void isValidRestaurantId(String restaurantId,String service){
        if(!Utility.isNumeric(restaurantId))
            throw new CustomException(Utility.buildErrorObject("INVALID_RESTAURANT_ID","Restaurant Id Provided is Not Valid",400,service));
    }

    private void isValidBranchId(String branchId,String service){
        if(!Utility.isNumeric(branchId))
            throw new CustomException(Utility.buildErrorObject("INVALID_BRANCH_ID","Branch Id Provided is Not Valid",400,service));
    }
}
