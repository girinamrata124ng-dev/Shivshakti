package com.inn.cafe.rest;

import com.inn.cafe.POJO.Bill;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/bill")
public interface BillRest {

    @PostMapping(path = "/generateReport")
    ResponseEntity<String> generateReport(@RequestBody Map<String, Object> requestMap);

    @GetMapping(path = "/getBills")
    ResponseEntity<List<Bill>> getBills();

    @PostMapping(path = "/getPdf")
    ResponseEntity<byte[]> getPdf(@RequestBody Map<String, Object> requestMap);

    @PostMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteBill(@PathVariable Integer id);

    // ✅ NEW API — Update Bill Status (ADMIN only)
    @PostMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@RequestBody Map<String, String> requestMap, @PathVariable Integer id);

    // ✅ NEW API — Update Bill Product Details (for Wastage 2 editing)
    @PostMapping(path = "/updateProductDetails")
    ResponseEntity<String> updateProductDetails(@RequestBody Map<String, Object> requestMap);
}
