package com.inn.cafe.dao;

import com.inn.cafe.POJO.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BillDao extends JpaRepository<Bill, Integer> {

    // ✅ Get all bills
    @Query("select b from bill b order by b.bill desc")
    List<Bill> getAllBills();

    // ✅ Get bills by username
    @Query("select b from bill b where b.createdBy = :username order by b.bill desc")
    List<Bill> getBillByUserName(@Param("username") String username);

    // ✅ Get bills by status
    List<Bill> findByStatus(String status);

    // ✅ Find bill by bill
    Optional<Bill> findByBill(Integer bill);
}
