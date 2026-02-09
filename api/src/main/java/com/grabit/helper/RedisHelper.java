package com.grabit.helper;

import com.grabit.bean.Restaurant.FoodItemDTO;
import com.grabit.entity.Branch;
import com.grabit.entity.FoodItem;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Set;

@Log4j2
public class RedisHelper {

    private RedisTemplate<String,Object> redisTemplate;

    public RedisHelper(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addFoodItemToRedis(String cacheName, List<FoodItemDTO> foodItemList){
        try{
            for(FoodItemDTO foodItem:foodItemList){
                String key=cacheName+"_"+foodItem.getBranchId()+"_"+foodItem.getId();
                redisTemplate.opsForValue().set(key,foodItem);
            }
            printFoodItem(cacheName);
        } catch (Exception e) {
            log.error("Error while adding/updating food item in redis : "+e);
        }
    }

    public Object getFoodItem(String cacheName, String branchId, String id){
        try{
            String key=cacheName+"_"+branchId+"_"+id;
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Error while retrieving food item in redis : "+e);
        }
        return null;
    }

    private void printFoodItem(String cacheName){
        try{
            Set<String> keys=redisTemplate.keys(cacheName+"*");
            for(String key:keys){
                System.out.println(redisTemplate.opsForValue().get(key));
            }
        } catch (Exception e) {
            log.error("Error while adding/updating food item in redis : "+e);
        }
    }
}
