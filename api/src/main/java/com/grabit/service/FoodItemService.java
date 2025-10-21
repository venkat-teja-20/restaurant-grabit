package com.grabit.service;

import com.grabit.Utilities.ModelMapperUtility;
import com.grabit.Utilities.Utility;
import com.grabit.bean.Restaurant.FoodItemDTO;
import com.grabit.entity.Branch;
import com.grabit.entity.FoodItem;
import com.grabit.enums.FoodCategory;
import com.grabit.enums.ItemStatus;
import com.grabit.enums.ItemType;
import com.grabit.exception.CustomException;
import com.grabit.repository.BranchRepository;
import com.grabit.repository.FoodItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Log4j2
public class FoodItemService {

    @Autowired
    FoodItemRepository foodItemRepository;

    @Autowired
    BranchRepository branchRepository;

    public List<FoodItemDTO> addFoodItemToTheBranch(List<FoodItemDTO> request,String restaurantId,String branchId){
        if(!Utility.isNumeric(restaurantId))
            throw new CustomException(Utility.buildErrorObject("INVALID_RESTAURANT_ID","Restaurant Id Provided is Not Valid",400,"getRestaurant"));
        if(!Utility.isNumeric(branchId))
            throw new CustomException(Utility.buildErrorObject("INVALID_BRANCH_ID","Branch Id Provided is Not Valid",400,"getRestaurant"));

        Branch branch=branchRepository.findByIdAndRestaurantId(Long.valueOf(branchId),Long.valueOf(restaurantId)).orElseThrow(()->new EntityNotFoundException("Restaurant or Branch Details not Found"));
        List<FoodItem> foodItemList=setFoodItemDetails(request,branch);
        List<FoodItem> savedFoodItemsList=foodItemRepository.saveAll(foodItemList);
        List<FoodItemDTO> foodItemDTOList=savedFoodItemsList.stream().map(foodItem -> {
            FoodItemDTO foodItemDTO= ModelMapperUtility.map(foodItem,FoodItemDTO.class);
            foodItemDTO.setBranchId(Long.valueOf(branchId));
            return foodItemDTO;
        }).toList();
        return foodItemDTOList;
    }

    public Map<String,Object> checkItemExists(String itemId){
        if(!Utility.isNumeric(itemId))
            throw new CustomException(Utility.buildErrorObject("INVALID_ITEM_ID","Item id provided in the request is not valid",400,"checkItemExists"));
        Map<String,Object> itemExistStatus =new HashMap<>();
        itemExistStatus.put("item_id",itemId);
        itemExistStatus.put("is_present",foodItemRepository.existsById(Long.valueOf(itemId)));
        return itemExistStatus;
    }

    public Map<String,Object> checkItemExistsInGivenBranch(String itemId,String branchId,String itemDetailsStatus){
        if(!Utility.isNumeric(itemId))
            throw new CustomException(Utility.buildErrorObject("INVALID_ITEM_ID","Item id provided in the request is not valid",400,"checkItemExistsInGivenBranch"));
        if(!Utility.isNumeric(branchId))
            throw new CustomException(Utility.buildErrorObject("INVALID_BRANCH_ID","Branch id provided in the request is not valid",400,"checkItemExistsInGivenBranch"));
        Map<String,Object> itemExistStatus =new HashMap<>();
        Long numericItemId=Long.valueOf(itemId);
        Long numericBranchId=Long.valueOf(branchId);
        itemExistStatus.put("branch_id",numericBranchId);
        itemExistStatus.put("item_id", numericItemId);
        if("TRUE".equalsIgnoreCase(itemDetailsStatus)) {
            Optional<FoodItem> foodItem=foodItemRepository.findByIdAndBranchId(numericItemId,numericBranchId);
            itemExistStatus.put("is_present", foodItem.isPresent());
            FoodItemDTO foodItemDTO=null;
            if(foodItem.isPresent()) {
                foodItemDTO = ModelMapperUtility.map(foodItem, FoodItemDTO.class);
                foodItemDTO.setBranchId(numericBranchId);
            }
            itemExistStatus.put("item_details", foodItemDTO);
        }
        else
            itemExistStatus.put("is_present", foodItemRepository.existsByIdAndBranchId(numericItemId, numericBranchId));
        return itemExistStatus;
    }

    private List<FoodItem> setFoodItemDetails(List<FoodItemDTO> request,Branch branch){
        List<FoodItem> foodItemList=new ArrayList<>();
        for(FoodItemDTO foodItemDTO:request){
            if(Utility.isNullOrEmpty(foodItemDTO.getItemName()))
                throw new CustomException(Utility.buildErrorObject("ITEM_NAME_MISSING", "Food Item must have a Name", 400, "addFoodItemToTheBranch"));

            if (Utility.isNullOrEmpty(foodItemDTO.getItemType()))
                throw new CustomException(Utility.buildErrorObject("ITEM_TYPE_MISSING", "Food Item must have a Serve Type", 400, "addFoodItemToTheBranch"));

            FoodItem foodItem=new FoodItem();
            BeanUtils.copyProperties(foodItemDTO,foodItem);

            // Food Item Type
            Boolean isValidItemServeType = Boolean.FALSE;
            for (ItemType itemType : ItemType.values()) {
                if ((foodItemDTO.getItemType()).equalsIgnoreCase(String.valueOf(itemType))) {
                    isValidItemServeType = Boolean.TRUE;
                    foodItem.setItemType(String.valueOf(itemType));
                    break;
                }
            }
            if (!isValidItemServeType)
                throw new CustomException(Utility.buildErrorObject("INVALID_ITEM_SERVE_TYPE", "Item Type is not Valid for Food : "+foodItemDTO.getItemName(), 406, "addFoodItemToTheBranch"));

            // Food Item Category
            Boolean isValidCategory = Boolean.FALSE;
            for (FoodCategory foodCategory : FoodCategory.values()) {
                if ((foodItemDTO.getCategory()).equalsIgnoreCase(String.valueOf(foodCategory))) {
                    isValidCategory = Boolean.TRUE;
                    foodItem.setCategory(String.valueOf(foodCategory));
                    break;
                }
            }
            if (!isValidCategory)
                foodItem.setCategory(FoodCategory.OTHERS.toString());

            // Food Item Status
            Boolean isValidStatus = Boolean.FALSE;
            for (ItemStatus itemStatus : ItemStatus.values()) {
                if ((foodItemDTO.getItemStatus()).equalsIgnoreCase(String.valueOf(itemStatus))) {
                    isValidStatus = Boolean.TRUE;
                    foodItem.setItemStatus(String.valueOf(itemStatus));
                    break;
                }
            }
            if (!isValidStatus)
                throw new CustomException(Utility.buildErrorObject("INVALID_ITEM_STATUS", "Item status is not Valid for Food : "+foodItemDTO.getItemName(), 406, "addFoodItemToTheBranch"));

            foodItem.setBranch(branch);
            foodItem.setOrderCount(0L);

            foodItemList.add(foodItem);
        }
        return foodItemList;
    }

    public FoodItemDTO getFoodItemDetailsById(String itemId){
        if(!Utility.isNumeric(itemId))
            throw new CustomException(Utility.buildErrorObject("INVALID_ITEM_ID","Item id provided in the request is not valid",400,"getFoodItemDetailsById"));
        FoodItem foodItem=foodItemRepository.findById(Long.valueOf(itemId)).orElseThrow(()->new EntityNotFoundException("Food item not found with provided item id"));
        return ModelMapperUtility.map(foodItem, FoodItemDTO.class);
    }

    @Transactional
    public Map<String,Object> updateOrdersAndAvailabilityOfAFoodItem(List<FoodItemDTO> request){
        Map<Long,Integer> idsIndex=new HashMap<>();
        List<Long> itemsList=request.parallelStream().map(foodItemDTO -> {
            idsIndex.put(foodItemDTO.getId(),foodItemDTO.getItemQuantity());
            return foodItemDTO.getId();
        }).sorted().toList();
        List<FoodItem> foodItemList=foodItemRepository.findAllById(itemsList);
        if(foodItemList.isEmpty())
            throw new EntityNotFoundException("Items does not exist");
        if(foodItemList.size()!=itemsList.size()) {
            List<Long> receivedIds=foodItemList.parallelStream().map(FoodItem::getId).toList();
            List<Long> misingIds=itemsList.parallelStream().filter(id->!receivedIds.contains(id)).toList();
            throw new CustomException(Utility.buildErrorObject("FOOD_ITEMS_MISSING","Some Food Items are Missing : "+misingIds,404,"updateOrdersAndAvailabilityOfAFoodItem"));
        }
        List<FoodItem> modifiedFoodItemList=foodItemList.parallelStream().map(foodItem -> {
            Long itemId=foodItem.getId();
            foodItem.setOrderCount(foodItem.getOrderCount()+1);
            if("AVAILABLE".equals(foodItem.getItemStatus()) && foodItem.getItemQuantity()>=idsIndex.get(itemId)){
                foodItem.setItemQuantity(foodItem.getItemQuantity()-idsIndex.get(itemId));
                foodItem.setQuantitySold(foodItem.getQuantitySold()+idsIndex.get(itemId));
                if(Objects.equals(foodItem.getItemQuantity(), idsIndex.get(itemId)))
                    foodItem.setItemStatus(ItemStatus.OUT_OF_STOCK.toString());
            }
            else if("AVAILABLE".equals(foodItem.getItemStatus()) && foodItem.getItemQuantity()<idsIndex.get(itemId) && foodItem.getItemQuantity()!=0){
                throw new CustomException(Utility.buildErrorObject("QUANTITY_NOT_AVAILABLE","Only "+foodItem.getItemQuantity()+" items are in stock for food item : "+itemId,409,"updateOrdersAndAvailabilityOfAFoodItem"));
            }
            else if("OUT_OF_STOCK".equals(foodItem.getItemStatus())){
                throw new CustomException(Utility.buildErrorObject("OUT_OF_STOCK","The items are out of stock for item : "+itemId,409,"updateOrdersAndAvailabilityOfAFoodItem"));
            }
            else if("REMOVED".equals(foodItem.getItemStatus())){
                throw new CustomException(Utility.buildErrorObject("UNACCEPTABLE_FOOD_ITEM","The item is currently not selling by the restaurant : "+itemId,409,"updateOrdersAndAvailabilityOfAFoodItem"));
            }
            return foodItem;
        }).toList();
        foodItemRepository.saveAll(modifiedFoodItemList);
        return Map.of("status","SUCCESS");
    }

    private void isValidFoodItemId(String itemId,String service){
        if(!Utility.isNumeric(itemId))
            throw new CustomException(Utility.buildErrorObject("INVALID_ITEM_ID","Item id provided in the request is not valid",400,service));
    }
}
