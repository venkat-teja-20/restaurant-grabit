package com.grabit.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grabit.Feign.OrderInterface;
import com.grabit.Utilities.ModelMapperUtility;
import com.grabit.Utilities.Utility;
import com.grabit.bean.Order.OrderDTO;
import com.grabit.bean.Restaurant.BranchDTO;
import com.grabit.bean.Restaurant.RestaurantDTO;
import com.grabit.entity.Branch;
import com.grabit.entity.Restaurant;
import com.grabit.enums.BranchActivity;
import com.grabit.enums.BranchStatus;
import com.grabit.enums.FoodType;
import com.grabit.exception.CustomException;
import com.grabit.repository.BranchRepository;
import com.grabit.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Log4j2
public class RestaurantService {

    @Autowired
    RestaurantRepository restaurantRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    private OrderInterface orderInterface;

    @Transactional
    public RestaurantDTO addNewRestaurant(RestaurantDTO request) {
        validateRequest(request);
        Restaurant restaurant = setRestaurantDetails(request, new Restaurant());
        Restaurant savedRestaurant=restaurantRepository.save(restaurant);
        log.info("Saved Restaurant : "+Utility.toJson(savedRestaurant));
        RestaurantDTO restaurantDTO= ModelMapperUtility.map(savedRestaurant, RestaurantDTO.class);
        List<BranchDTO> branchDTOList=new ArrayList<>();
        if(!savedRestaurant.getBranches().isEmpty())
            branchDTOList=savedRestaurant.getBranches().stream().map(branch -> {
                BranchDTO branchDTO=ModelMapperUtility.map(branch,BranchDTO.class);
                branchDTO.setRestaurantId(savedRestaurant.getId());
                return branchDTO;
            }).toList();
        restaurantDTO.setBranchDTOList(branchDTOList);
        return restaurantDTO;
    }

    public RestaurantDTO getRestaurant(String restaurantId) {
        isValidRestaurantId(restaurantId,"getRestaurant");
        Restaurant restaurant=restaurantRepository.findById(Long.valueOf(restaurantId)).orElseThrow(()->new EntityNotFoundException("No Such Restaurant Exists"));
        RestaurantDTO restaurantDTO=ModelMapperUtility.map(restaurant,RestaurantDTO.class);
        List<BranchDTO> branchDTOList=new ArrayList<>();
        if(!restaurant.getBranches().isEmpty()) {
            branchDTOList = restaurant.getBranches().stream().map(branch -> {
                BranchDTO branchDTO = ModelMapperUtility.map(branch, BranchDTO.class);
                branchDTO.setRestaurantId(restaurant.getId());
                return branchDTO;
            }).toList();
        }
        restaurantDTO.setBranchDTOList(branchDTOList);
        return restaurantDTO;
    }

    public RestaurantDTO getRestaurantAndBranchDetails(String restaurantId,String branchId) {
        isValidRestaurantId(restaurantId,"getRestaurantAndBranchDetails");
        isValidBranchId(branchId,"getRestaurantAndBranchDetails");
        Restaurant restaurant=restaurantRepository.findById(Long.valueOf(restaurantId)).orElseThrow(()->new EntityNotFoundException("No Such Restaurant Exists"));
//        List<Branch> branches=restaurant.getBranches();
//        Integer branchIdx=isBranchExists(Long.valueOf(branchId),branches);
        Branch branch=branchRepository.findByIdAndRestaurantId(Long.valueOf(branchId),Long.valueOf(restaurantId)).orElseThrow(()->new EntityNotFoundException("No Branch Id found with Restaurant Id : "+restaurantId));
        RestaurantDTO restaurantDTO=ModelMapperUtility.map(restaurant, RestaurantDTO.class);
        restaurantDTO.getBranchDTOList().add(ModelMapperUtility.map(branch, BranchDTO.class));
        return restaurantDTO;
    }

//    private Integer isBranchExists(Long branchId,List<Branch> branches){
//        Integer totalBranches= branches.size();
//        Integer l=0,r=totalBranches-1;
//        while(l<r){
//            Integer mid=(l+r)/2;
//            Long bId= branches.get(mid).getId();
//            if(Objects.equals(branchId, bId))
//                return mid;
//            else if (branchId<bId) {
//                r=mid-1;
//            }
//            else
//                l=mid+1;
//        }
//        return -1;
//    }

    public RestaurantDTO updateRestaurant(String restaurantId,RestaurantDTO restaurantDTO) {
        isValidRestaurantId(restaurantId,"updateRestaurant");
        Restaurant restaurant=restaurantRepository.findById(Long.valueOf(restaurantId)).orElseThrow(()->new EntityNotFoundException("No Restaurant with Provided Restaurant Id"));
        List<Branch> branchList=restaurant.getBranches();
        setRestaurantDetailsForUpdate(restaurant,restaurantDTO);
        restaurant.setBranches(branchList);
        Restaurant savedRestaurant=restaurantRepository.save(restaurant);
        RestaurantDTO restaurantDTOResponse=ModelMapperUtility.map(savedRestaurant, RestaurantDTO.class);
        List<BranchDTO> branchDTOList=savedRestaurant.getBranches().stream().map(branch -> {
            BranchDTO branchDTO=ModelMapperUtility.map(branch,BranchDTO.class);
            branchDTO.setRestaurantId(savedRestaurant.getId());
            return branchDTO;
        }).toList();
        restaurantDTOResponse.setBranchDTOList(branchDTOList);
        return restaurantDTOResponse;
    }

    public Map<String,Object> checkRestaurantExists(String restaurantId) {
        isValidRestaurantId(restaurantId,"checkRestaurantExists");
        Map<String,Object> restaurantCheck=new HashMap<>();
        restaurantCheck.put("restaurant_id",Long.valueOf(restaurantId));
        restaurantCheck.put("is_present",restaurantRepository.existsById(Long.valueOf(restaurantId)));
        return restaurantCheck;
    }

    public Map<String,Object> checkRestaurantAndBranchExistence(String restaurantId,String branchId) {
        isValidRestaurantId(restaurantId,"checkRestaurantAndBranchExistence");
        isValidBranchId(branchId,"checkRestaurantAndBranchExistence");
        Map<String,Object> restaurantCheck=new HashMap<>();
        restaurantCheck.put("restaurant_id",Long.valueOf(restaurantId));
        restaurantCheck.put("branch_id",Long.valueOf(branchId));
        restaurantCheck.put("is_present",branchRepository.existsByIdAndRestaurantId(Long.valueOf(branchId),Long.valueOf(restaurantId)));
        return restaurantCheck;
    }

    @Transactional
    public Map<String,Object> updateOrdersInRestaurantAndBranch(String restaurantId,String branchId) {
        isValidRestaurantId(restaurantId,"updateOrdersInRestaurantAndBranchAndFoodItem");
        isValidBranchId(branchId,"updateOrdersInRestaurantAndBranchAndFoodItem");
        Restaurant restaurant=restaurantRepository.findById(Long.valueOf(restaurantId)).orElseThrow(()->new EntityNotFoundException("Restaurant does not exists with restaurant id : "+restaurantId));
        Branch branch=branchRepository.findById(Long.valueOf(branchId)).orElseThrow(()->new EntityNotFoundException("Branch does not exists with branch id : "+branchId));
        branch.setOrdersReceived(branch.getOrdersReceived()+1);
        branchRepository.save(branch);
        log.info("Branch Orders Updated Successfully");
        restaurant.setTotalOrders(restaurant.getTotalOrders()+1);
        restaurantRepository.save(restaurant);
        return Map.of("status","SUCCESS");
    }

    public Map<String,String> getRestaurantAndBranchName(String restaurantId,String branchId){
        isValidRestaurantId(restaurantId,"getRestaurantAndBranchName");
        isValidBranchId(branchId,"getRestaurantAndBranchName");
        String restaurantName=restaurantRepository.findRestaurantNameById(Long.valueOf(restaurantId)).orElseThrow(()->new EntityNotFoundException("Restaurant Not Found"));
        String branchName=branchRepository.findBranchNameById(Long.valueOf(branchId)).orElseThrow(()->new EntityNotFoundException("Restaurant Not Found"));
//        if(!restaurantId.equals(String.valueOf(branch.getRestaurantId())))
//            throw new CustomException(Utility.buildErrorObject("INVALID_BRANCH","Restaurant with id "+restaurantId+" did not have the branch with id : "+branchId,409,"getRestaurantAndBranchName"));
        Map<String,String> names=new HashMap<>();
        names.put("restaurant_name",restaurantName);
        names.put("branch_name",branchName);
        return names;
    }

    private void setRestaurantDetailsForUpdate(Restaurant restaurant,RestaurantDTO restaurantDTO) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            String requestJson = objectMapper.writeValueAsString(restaurantDTO);
            RestaurantDTO restaurantDTOUpdateRequest=new RestaurantDTO();
            objectMapper.readerForUpdating(restaurantDTOUpdateRequest).readValue(requestJson);
            BeanUtils.copyProperties(restaurantDTOUpdateRequest,restaurant);
        }
        catch (JsonProcessingException e){
            throw new CustomException(Utility.buildErrorObject("MAPPING_FIELDS_ERROR","Error while mapping request to entity",500,"updateRestaurant"));
        }
    }

    private Restaurant setRestaurantDetails(RestaurantDTO request, Restaurant restaurant) {
        BeanUtils.copyProperties(request, restaurant);
        // Add Addresses to the Restaurant
        List<BranchDTO> branchDTOList = request.getBranchDTOList();
        List<Branch> branchList = new ArrayList<>();
        for (BranchDTO branchDTO : branchDTOList) {
            if(Utility.isNullOrEmpty(branchDTO.getBranchName()))
                throw new CustomException(Utility.buildErrorObject("BRANCH_NAME_MISSING","Branch Name is Required",400,"addNewRestaurant"));
            if(Utility.isNullOrEmpty(branchDTO.getFoodServeType()))
                throw new CustomException(Utility.buildErrorObject("FOOD_SERVING_TYPE_MISSING","Restaurant's Food Serving Type is Missing",400,"addNewRestaurant"));
            Branch branch = new Branch();
            BeanUtils.copyProperties(branchDTO, branch);
            Boolean isValidFoodServeType = Boolean.FALSE;
            for (FoodType foodType : FoodType.values()) {
                if ((branchDTO.getFoodServeType()).equalsIgnoreCase(String.valueOf(foodType))) {
                    isValidFoodServeType = Boolean.TRUE;
                    branch.setFoodServeType(String.valueOf(foodType));
                    break;
                }
            }
            if (!isValidFoodServeType)
                throw new CustomException(Utility.buildErrorObject("INVALID_SERVING_TYPE", "Food Serve Type is not Valid for Branch : "+branchDTO.getBranchName(), 406, "addNewRestaurant"));

            if(!Utility.isNullOrEmpty(branchDTO.getBranchActivity())) {
                Boolean isValidActivityStatus = Boolean.FALSE;
                for (BranchActivity branchActivity : BranchActivity.values()) {
                    if ((branchDTO.getBranchActivity()).equalsIgnoreCase(String.valueOf(branchActivity))) {
                        isValidActivityStatus = Boolean.TRUE;
                        branch.setBranchActivity(String.valueOf(branchActivity));
                        break;
                    }
                }
                if (!isValidActivityStatus)
                    throw new CustomException(Utility.buildErrorObject("INVALID_BRANCH_ACTIVITY", "Branch Activity is not Valid for Branch : " + branchDTO.getBranchName(), 406, "addNewRestaurant"));
            }
            else
                branch.setBranchActivity(BranchActivity.ACTIVE.toString());

            if(isOpen(branchDTO.getOpenTime(),branchDTO.getCloseTime()))
                branch.setBranchStatus(BranchStatus.OPENED.toString());
            else
                branch.setBranchStatus(BranchStatus.CLOSED.toString());

            branch.setRestaurant(restaurant);
            branch.setOrdersReceived(0L);
            branchList.add(branch);

        }
        restaurant.setBranches(branchList);
        restaurant.setTotalOrders(0L);
        return restaurant;
    }

    public Map<String,Object> getAllOrdersOfARestaurantBranch(String restaurantId,String branchId,int pageNumber,int pageSize,String orderBy,String orderField){
        isValidRestaurantId(restaurantId,"getAllOfARestaurantBranch");
        isValidBranchId(branchId,"getAllOfARestaurantBranch");

        return null;
    }

    public OrderDTO getOrderDetailsById(String orderId){
        return orderInterface.getOrderById(orderId);
    }

    private void validateRequest(RestaurantDTO request){

        if (Utility.isNullOrEmpty(request.getBranchDTOList()))
            throw new CustomException(Utility.buildErrorObject("BRANCH_DETAILS_MISSING", "Branch Details are required for the Restaurant", 400, "addNewRestaurant"));
    }

    private void isValidRestaurantId(String restaurantId,String service){
        if(!Utility.isNumeric(restaurantId))
            throw new CustomException(Utility.buildErrorObject("INVALID_RESTAURANT_ID","Restaurant Id Provided is Not Valid",400,service));
    }

    private void isValidBranchId(String branchId,String service){
        if(!Utility.isNumeric(branchId))
            throw new CustomException(Utility.buildErrorObject("INVALID_BRANCH_ID","Branch Id Provided is Not Valid",400,service));
    }

    private boolean isOpen(LocalDateTime openTime, LocalDateTime closeTime) {
        LocalDateTime now = LocalDateTime.now();

        return (now.isEqual(openTime) || now.isAfter(openTime))
                && (now.isEqual(closeTime) || now.isBefore(closeTime));
    }
}
