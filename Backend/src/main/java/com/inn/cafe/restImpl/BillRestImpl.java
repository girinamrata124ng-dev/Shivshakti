package com.inn.cafe.restImpl;

import com.inn.cafe.POJO.Bill;
import com.inn.cafe.constents.CafeConstants;
import com.inn.cafe.rest.BillRest;
import com.inn.cafe.service.BillService;
import com.inn.cafe.utils.CafeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class BillRestImpl implements BillRest {

    private final BillService billService;

    public BillRestImpl(BillService billService) {
        this.billService = billService;
    }

    @Override
    public ResponseEntity<String> generateReport(Map<String, Object> requestMap) {
        try {
            return billService.generateReport(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
            return CafeUtils.getResponeEntity(
                    CafeConstants.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public ResponseEntity<List<Bill>> getBills() {
        try {
            return billService.getBills();
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap) {
        try {
            return billService.getPdf(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(new byte[0], HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> deleteBill(Integer id) {
        try {
            return billService.delete(id);
        } catch (Exception ex) {
            ex.printStackTrace();
            return CafeUtils.getResponeEntity(
                    CafeConstants.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    // ✅ REQUIRED method from BillRest
    @Override
    public ResponseEntity<String> updateStatus(Map<String, String> requestMap, Integer id) {
        try {
            return billService.updateStatus(requestMap, id);
        } catch (Exception ex) {
            ex.printStackTrace();
            return CafeUtils.getResponeEntity(
                    CafeConstants.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    // ✅ REQUIRED method from BillRest
    @Override
    public ResponseEntity<String> updateProductDetails(Map<String, Object> requestMap) {
        try {
            return billService.updateProductDetails(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
            return CafeUtils.getResponeEntity(
                    CafeConstants.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
