package com.grabit.repository;

import com.grabit.bean.Restaurant.BranchDTO;
import com.grabit.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch,Long> {
    @Query("SELECT b.branchName FROM Branch b WHERE b.id=:id")
    Optional<String> findBranchNameById(@Param("id") Long id);

    Optional<Branch> findByIdAndRestaurantId(Long id, Long restaurantId);

    Boolean existsByIdAndRestaurantId(Long id, Long restaurantId);
}
