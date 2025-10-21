package com.grabit.repository;

import com.grabit.entity.FoodItem;
import com.grabit.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant,Long> {
    @Query("SELECT r.restaurantName FROM restaurant r WHERE r.id=:id")
    Optional<String> findRestaurantNameById(@Param("id") Long id);
}
