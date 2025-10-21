package com.grabit.repository;

import com.grabit.entity.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem,Long> {
    Boolean existsByIdAndBranchId(Long id, Long branchId);

    Optional<FoodItem> findByIdAndBranchId(Long id, Long branchId);
}
