package com.inn.cafe.dao;

import com.inn.cafe.POJO.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BillDao extends JpaRepository<Bill, Integer> {

    // ✅ Get all bills
    @Query("select b from bill b order by b.id desc")
    List<Bill> getAllBills();

    // ✅ Get bills by username
    @Query("select b from bill b where b.createdBy = :username order by b.id desc")
    List<Bill> getBillByUserName(@Param("username") String username);

    // ✅ NEW: Get bills by status (In Progress / Completed)
    List<Bill> findByStatus(String status);

    // ✅ NEW: Find bill by billNo
    Optional<Bill> findByBillNo(Integer billNo);
}
