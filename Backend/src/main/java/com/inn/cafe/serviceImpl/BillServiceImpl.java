package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.CustomerUserDetailsService;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Bill;
import com.inn.cafe.constents.CafeConstants;
import com.inn.cafe.dao.BillDao;
import com.inn.cafe.service.BillService;
import com.inn.cafe.utils.CafeUtils;
import com.inn.cafe.utils.EmailUtil;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class BillServiceImpl implements BillService {

    @Autowired
    BillDao billDao;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    com.inn.cafe.JWT.jwtUtil jwtUtil;

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    CustomerUserDetailsService customerUserDetailsService;

    @Autowired
    EmailUtil emailUtil;

    @Override
    public ResponseEntity<String> generateReport(Map<String, Object> requestMap) {
        log.info("Inside generateReport");

        try {
            if (validateResquestMap(requestMap)) {
                // Insert bill first to get bill number
                insertBill(requestMap);
                
                Integer bill = (Integer) requestMap.get("bill");
                String fileName = "Bill_" + bill;

                log.info("Bill generated with bill: {}", bill);

                String data = "Name: " + requestMap.get("name") + "\n"
                        + "Contact Number: " + requestMap.get("contactNumber");

                Document document = new Document();
                PdfWriter.getInstance(document,
                        new FileOutputStream(CafeConstants.STORE_LOCATION + "\\" + fileName + ".pdf"));
                document.open();

                setRectaangleInPdf(document);

                Paragraph header = new Paragraph("Shivshakti Dal Udyog", getFont("Header"));
                header.setAlignment(Element.ALIGN_CENTER);
                document.add(header);

                // Add Bill
                document.add(new Paragraph("Bill: " + bill + "\n\n", getFont("Data")));

                document.add(new Paragraph(data + "\n\n", getFont("Data")));

                PdfPTable table = new PdfPTable(7);
                table.setWidthPercentage(100);
                addTableHeader(table);

                JSONArray jsonArray = CafeUtils.getJsonArrayFromString(
                        (String) requestMap.get("productDetails"));

                for (int i = 0; i < jsonArray.length(); i++) {
                    addRows(table, CafeUtils.getMapFromJson(jsonArray.getString(i)));
                }

                document.add(table);

                document.add(new Paragraph(
                        "Total : " + requestMap.get("totalAmount") + "\nThank you for visiting our website.",
                        getFont("Data")));

                document.close();
                log.info("PDF generated successfully for bill: {}", bill);
                return new ResponseEntity<>("{\"bill\":\"" + bill + "\"}", HttpStatus.OK);
            }

            log.warn("Invalid request map for generateReport");
            return CafeUtils.getResponeEntity("Required data not found", HttpStatus.BAD_REQUEST);

        } catch (Exception ex) {
            log.error("Error in generateReport", ex);
            ex.printStackTrace();
        }

        return CafeUtils.getResponeEntity(CafeConstants.SOMETHING_WENT_WRONG,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Bill>> getBills() {
        try {
            List<Bill> list;

            if (jwtFilter.isAdmin()) {
                list = billDao.getAllBills();
            } else {
                String username = jwtFilter.getCurrentUsername();
                log.info("Getting bills for user: {}", username);
                list = billDao.getBillByUserName(username);
            }

            log.info("Found {} bills", list.size());
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Error in getBills", ex);
            ex.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap) {
        log.info("Inside getPdf {}", requestMap);

        try {
            byte[] byteArray = new byte[0];

            if (!requestMap.containsKey("bill")) {
                log.warn("bill not found in request");
                return new ResponseEntity<>(byteArray, HttpStatus.BAD_REQUEST);
            }

            // Handle both String and Integer types for bill
            Integer bill = null;
            Object billObj = requestMap.get("bill");
            if (billObj instanceof Integer) {
                bill = (Integer) billObj;
            } else if (billObj instanceof String) {
                bill = Integer.parseInt((String) billObj);
            } else if (billObj instanceof Number) {
                bill = ((Number) billObj).intValue();
            }

            if (bill == null) {
                log.warn("bill is null after conversion");
                return new ResponseEntity<>(byteArray, HttpStatus.BAD_REQUEST);
            }

            String fileName = "Bill_" + bill;
            String filePath = CafeConstants.STORE_LOCATION + "\\" + fileName + ".pdf";

            log.info("Looking for PDF at: {}", filePath);

            if (!CafeUtils.isFileExist(filePath)) {
                log.warn("PDF not found, attempting to regenerate");
                Optional<Bill> billOptional = billDao.findByBill(bill);
                if (billOptional.isPresent()) {
                    Bill billEntity = billOptional.get();
                    
                    Map<String, Object> regenerateMap = new HashMap<>();
                    regenerateMap.put("bill", bill);
                    regenerateMap.put("name", billEntity.getName());
                    regenerateMap.put("contactNumber", billEntity.getContactNumber());
                    regenerateMap.put("totalAmount", String.valueOf(billEntity.getTotal()));
                    regenerateMap.put("productDetails", billEntity.getProductDetails());
                    
                    generateReport(regenerateMap);
                } else {
                    log.warn("Bill not found for bill: {}", bill);
                    return new ResponseEntity<>(byteArray, HttpStatus.BAD_REQUEST);
                }
            }

            byteArray = getByteArray(filePath);
            log.info("PDF retrieved successfully");
            return new ResponseEntity<>(byteArray, HttpStatus.OK);

        } catch (Exception ex) {
            log.error("Error in getPdf", ex);
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public ResponseEntity<String> delete(Integer bill) {
        try {
            if (jwtFilter.isAdmin()) {
                Optional<Bill> optional = billDao.findByBill(bill);

                if (optional.isPresent()) {
                    Bill billEntity = optional.get();
                    String fileName = "Bill_" + billEntity.getBill();
                    String filePath = CafeConstants.STORE_LOCATION + "\\" + fileName + ".pdf";
                    File file = new File(filePath);
                    if (file.exists()) {
                        file.delete();
                    }
                    
                    billDao.delete(billEntity);
                    log.info("Bill {} deleted successfully", bill);
                    return CafeUtils.getResponeEntity("Bill deleted successfully", HttpStatus.OK);
                }

                log.warn("Bill not found for deletion: {}", bill);
                return CafeUtils.getResponeEntity("Bill not found", HttpStatus.OK);
            }

            return CafeUtils.getResponeEntity(CafeConstants.UNAUTHORIZED_ACCESS,
                    HttpStatus.UNAUTHORIZED);

        } catch (Exception ex) {
            log.error("Error in delete", ex);
            ex.printStackTrace();
        }

        return CafeUtils.getResponeEntity(CafeConstants.SOMETHING_WENT_WRONG,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateStatus(Map<String, String> requestMap, Integer bill) {
        try {
            if (jwtFilter.isAdmin()) {
                Optional<Bill> optional = billDao.findByBill(bill);

                if (!optional.isPresent()) {
                    log.warn("Bill not found for status update: {}", bill);
                    return CafeUtils.getResponeEntity("Bill not found", HttpStatus.OK);
                }

                Bill billEntity = optional.get();
                billEntity.setStatus(requestMap.get("status"));
                billDao.save(billEntity);
                log.info("Bill {} status updated to {}", bill, requestMap.get("status"));

                return CafeUtils.getResponeEntity("Bill status updated successfully",
                        HttpStatus.OK);
            }

            return CafeUtils.getResponeEntity(CafeConstants.UNAUTHORIZED_ACCESS,
                    HttpStatus.UNAUTHORIZED);

        } catch (Exception ex) {
            log.error("Error in updateStatus", ex);
            ex.printStackTrace();
        }

        return CafeUtils.getResponeEntity(CafeConstants.SOMETHING_WENT_WRONG,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateProductDetails(Map<String, Object> requestMap) {
        try {
            if (jwtFilter.isAdmin()) {
                Integer bill = (Integer) requestMap.get("bill");
                Optional<Bill> optional = billDao.findByBill(bill);

                if (!optional.isPresent()) {
                    log.warn("Bill not found for product details update: {}", bill);
                    return CafeUtils.getResponeEntity("Bill not found", HttpStatus.OK);
                }

                Bill billEntity = optional.get();
                
                // Get the current product details and calculate wastage2 values
                String productDetailsJson = (String) requestMap.get("productDetails");
                JSONArray jsonArray = CafeUtils.getJsonArrayFromString(productDetailsJson);
                
                // Calculate wastage2 values for all products and update the JSON
                JSONArray updatedJsonArray = new JSONArray();
                for (int i = 0; i < jsonArray.length(); i++) {
                    Map<String, Object> product = CafeUtils.getMapFromJson(jsonArray.getString(i));
                    calculateWastage2ForProduct(product);
                    updatedJsonArray.put(new org.json.JSONObject(product));
                }
                
                // Update product details with calculated values
                billEntity.setProductDetails(updatedJsonArray.toString());
                
                // Update status if provided
                if (requestMap.containsKey("status")) {
                    billEntity.setStatus((String) requestMap.get("status"));
                }
                
                billDao.save(billEntity);
                log.info("Bill {} product details updated successfully with calculated wastage2 values", bill);
                
                // Regenerate PDF with updated product details
                regeneratePdf(billEntity);
                
                return CafeUtils.getResponeEntity("Bill product details updated successfully",
                        HttpStatus.OK);
            }

            return CafeUtils.getResponeEntity(CafeConstants.UNAUTHORIZED_ACCESS,
                    HttpStatus.UNAUTHORIZED);

        } catch (Exception ex) {
            log.error("Error in updateProductDetails", ex);
            ex.printStackTrace();
        }

        return CafeUtils.getResponeEntity(CafeConstants.SOMETHING_WENT_WRONG,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    private void calculateWastage2ForProduct(Map<String, Object> product) {
        // Calculate Daal and Waste from Wastage 2
        Object wastage2Obj = product.get("wastage2");
        if (wastage2Obj != null) {
            double wastage2 = Double.parseDouble(wastage2Obj.toString());
            Object quantityObj = product.get("quantity");
            if (quantityObj != null) {
                double quantity = Double.parseDouble(quantityObj.toString());
                
                // Calculate updated quantity: quantity = quantity - wastage2
                double updatedQuantity = Math.max(0, quantity - wastage2);
                
                // Calculate waste: waste = updatedQuantity / 3.33
                double calculatedWaste = updatedQuantity / 3.33;
                
                // Calculate Daal: Daal = updatedQuantity - waste
                double calculatedDaal = updatedQuantity - calculatedWaste;
                
                // Update the product with calculated values
                product.put("plus", String.format("%.2f", calculatedDaal));
                product.put("waste", String.format("%.2f", calculatedWaste));
                
                log.info("Calculated for product - Wastage2: {}, UpdatedQty: {}, Waste: {}, Daal: {}", 
                        wastage2, updatedQuantity, calculatedWaste, calculatedDaal);
            }
        }
    }
    
    private void regeneratePdf(Bill billEntity) {
        try {
            Integer bill = billEntity.getBill();
            String fileName = "Bill_" + bill;
            
            Document document = new Document();
            PdfWriter.getInstance(document,
                    new FileOutputStream(CafeConstants.STORE_LOCATION + "\\" + fileName + ".pdf"));
            document.open();

            setRectaangleInPdf(document);

            Paragraph header = new Paragraph("Shivshakti Dal Udyog", getFont("Header"));
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            // Add Bill
            document.add(new Paragraph("Bill: " + bill + "\n\n", getFont("Data")));

            String data = "Name: " + billEntity.getName() + "\n"
                    + "Contact Number: " + billEntity.getContactNumber();
            document.add(new Paragraph(data + "\n\n", getFont("Data")));

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            addTableHeader(table);

            JSONArray jsonArray = CafeUtils.getJsonArrayFromString(billEntity.getProductDetails());

            for (int i = 0; i < jsonArray.length(); i++) {
                Map<String, Object> product = CafeUtils.getMapFromJson(jsonArray.getString(i));
                calculateWastage2ForProduct(product);
                addRows(table, product);
            }

            document.add(table);

            document.add(new Paragraph(
                    "Total : " + billEntity.getTotal() + "\nThank you for visiting our website.",
                    getFont("Data")));

            document.close();
            log.info("PDF regenerated successfully for bill: {}", bill);
            
        } catch (Exception ex) {
            log.error("Error regenerating PDF", ex);
            ex.printStackTrace();
        }
    }

    private void insertBill(Map<String, Object> requestMap) {
        try {
            Bill billEntity = new Bill();
            billEntity.setName((String) requestMap.get("name"));
            billEntity.setContactNumber((String) requestMap.get("contactNumber"));
            billEntity.setTotal(Integer.parseInt((String) requestMap.get("totalAmount")));
            billEntity.setProductDetails((String) requestMap.get("productDetails"));
            billEntity.setCreatedBy(jwtFilter.getCurrentUsername());
            billEntity.setStatus("In Progress");
            
            log.info("Inserting bill with createdBy: {}", jwtFilter.getCurrentUsername());
            
            Bill savedBill = billDao.save(billEntity);
            
            requestMap.put("bill", savedBill.getBill());
            log.info("Bill saved with bill: {}", savedBill.getBill());

        } catch (Exception ex) {
            log.error("Error in insertBill", ex);
            ex.printStackTrace();
        }
    }

    private boolean validateResquestMap(Map<String, Object> requestMap) {
        return requestMap.containsKey("name") &&
                requestMap.containsKey("contactNumber") &&
                requestMap.containsKey("productDetails") &&
                requestMap.containsKey("totalAmount");
    }

    private void setRectaangleInPdf(Document document) throws DocumentException {
        Rectangle rectangle = new Rectangle(577, 825, 18, 15);
        rectangle.enableBorderSide(1 | 2 | 4 | 8);
        rectangle.setBorderColor(BaseColor.BLACK);
        rectangle.setBorderWidth(1);
        document.add(rectangle);
    }

    private Font getFont(String type) {
        if ("Header".equals(type)) {
            return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
        }
        return FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, BaseColor.BLACK);
    }

    private void addTableHeader(PdfPTable table) {
        Stream.of("Name", "Quantity", "Daal", "Waste", "Wastage 2", "Price", "Sub Total")
                .forEach(title -> {
                    PdfPCell header = new PdfPCell(new Phrase(title));
                    header.setBackgroundColor(BaseColor.YELLOW);
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(header);
                });
    }

    private void addRows(PdfPTable table, Map<String, Object> data) {
        table.addCell((String) data.get("name"));
        table.addCell(String.valueOf(data.get("quantity")));
        table.addCell(String.valueOf(data.get("plus") != null ? data.get("plus") : "-"));
        table.addCell(String.valueOf(data.get("waste") != null ? data.get("waste") : "-"));
        table.addCell(String.valueOf(data.get("wastage2") != null ? data.get("wastage2") : "-"));
        table.addCell(String.valueOf(data.get("price")));
        table.addCell(String.valueOf(data.get("total")));
    }

    private byte[] getByteArray(String filePath) throws Exception {
        InputStream targetStream = new FileInputStream(new File(filePath));
        byte[] byteArray = IOUtils.toByteArray(targetStream);
        targetStream.close();
        return byteArray;
    }
}
