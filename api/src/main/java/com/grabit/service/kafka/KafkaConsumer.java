package com.grabit.service.kafka;

import com.grabit.Utilities.Utility;
import com.grabit.entity.Branch;
import com.grabit.entity.Restaurant;
import com.grabit.exception.CustomException;
import com.grabit.repository.BranchRepository;
import com.grabit.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Log4j2
public class KafkaConsumer {

    private RestaurantRepository restaurantRepository;

    private BranchRepository branchRepository;

    public KafkaConsumer(RestaurantRepository restaurantRepository, BranchRepository branchRepository) {
        this.restaurantRepository = restaurantRepository;
        this.branchRepository = branchRepository;
    }

    @KafkaListener(topics = "update_restaurant_and_branch_orders",groupId = "restaurant")
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

    @KafkaListener(topics = "update_restaurant_and_branch_orders.DLT")
    public void dltListener(ConsumerRecord<String,Map<String,String>> record){
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
