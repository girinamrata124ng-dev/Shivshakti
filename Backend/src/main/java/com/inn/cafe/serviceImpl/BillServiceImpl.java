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

    // ===================== GENERATE BILL PDF =====================
    @Override
    public ResponseEntity<String> generateReport(Map<String, Object> requestMap) {
        log.info("Inside generateReport");

        try {
            String fileName;

            if (validateResquestMap(requestMap)) {
                if (requestMap.containsKey("isGenerate") && !(Boolean) requestMap.get("isGenerate")) {
                    fileName = (String) requestMap.get("uuid");
                } else {
                    fileName = CafeUtils.getUUID();
                    requestMap.put("uuid", fileName);
                    insertBill(requestMap);
                }

                String data = "Name: " + requestMap.get("name") + "\n"
                        + "Contact Number: " + requestMap.get("contactNumber");

                Document document = new Document();
                PdfWriter.getInstance(document,
                        new FileOutputStream(CafeConstants.STORE_LOCATION + "\\" + fileName + ".pdf"));
                document.open();

                setRectaangleInPdf(document);

                Paragraph header = new Paragraph("Cafe Management System", getFont("Header"));
                header.setAlignment(Element.ALIGN_CENTER);
                document.add(header);

                // Add Bill No
                document.add(new Paragraph("Bill No: " + requestMap.get("billNo") + "\n\n", getFont("Data")));

                document.add(new Paragraph(data + "\n\n", getFont("Data")));

                PdfPTable table = new PdfPTable(5);
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
                return new ResponseEntity<>("{\"uuid\":\"" + fileName + "\", \"billNo\":\"" + requestMap.get("billNo") + "\"}", HttpStatus.OK);
            }

            return CafeUtils.getResponeEntity("Required data not found", HttpStatus.BAD_REQUEST);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return CafeUtils.getResponeEntity(CafeConstants.SOMETHING_WENT_WRONG,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ===================== GET BILLS =====================
    @Override
    public ResponseEntity<List<Bill>> getBills() {
        List<Bill> list;

        if (jwtFilter.isAdmin()) {
            list = billDao.getAllBills();
        } else {
            list = billDao.getBillByUserName(jwtFilter.getCurrentUsername());
        }

        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    // ===================== GET PDF =====================
    @Override
    public ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap) {
        log.info("Inside getPdf {}", requestMap);

        try {
            byte[] byteArray = new byte[0];

            if (!requestMap.containsKey("uuid")) {
                return new ResponseEntity<>(byteArray, HttpStatus.BAD_REQUEST);
            }

            String filePath = CafeConstants.STORE_LOCATION + "\\" +
                    requestMap.get("uuid") + ".pdf";

            if (!CafeUtils.isFileExist(filePath)) {
                requestMap.put("isGenerate", false);
                generateReport(requestMap);
            }

            byteArray = getByteArray(filePath);
            return new ResponseEntity<>(byteArray, HttpStatus.OK);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // ===================== DELETE BILL =====================
    @Override
    public ResponseEntity<String> delete(Integer id) {
        try {
            if (jwtFilter.isAdmin()) {
                Optional<Bill> optional = billDao.findById(id);

                if (optional.isPresent()) {
                    billDao.deleteById(id);
                    return CafeUtils.getResponeEntity("Bill deleted successfully", HttpStatus.OK);
                }

                return CafeUtils.getResponeEntity("Bill id not found", HttpStatus.OK);
            }

            return CafeUtils.getResponeEntity(CafeConstants.UNAUTHORIZED_ACCESS,
                    HttpStatus.UNAUTHORIZED);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return CafeUtils.getResponeEntity(CafeConstants.SOMETHING_WENT_WRONG,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ===================== UPDATE BILL STATUS =====================
    @Override
    public ResponseEntity<String> updateStatus(Map<String, String> requestMap, Integer id) {
        try {
            if (jwtFilter.isAdmin()) {
                Optional<Bill> optional =
                        billDao.findById(id);

                if (!optional.isPresent()) {
                    return CafeUtils.getResponeEntity("Bill id not found", HttpStatus.OK);
                }

                Bill bill = optional.get();
                bill.setStatus(requestMap.get("status")); // Completed
                billDao.save(bill);

                return CafeUtils.getResponeEntity("Bill status updated successfully",
                        HttpStatus.OK);
            }

            return CafeUtils.getResponeEntity(CafeConstants.UNAUTHORIZED_ACCESS,
                    HttpStatus.UNAUTHORIZED);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return CafeUtils.getResponeEntity(CafeConstants.SOMETHING_WENT_WRONG,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ===================== PRIVATE METHODS =====================
    private void insertBill(Map<String, Object> requestMap) {
        try {
            Bill bill = new Bill();
            bill.setUuid((String) requestMap.get("uuid"));
            bill.setName((String) requestMap.get("name"));
            bill.setContactNumber((String) requestMap.get("contactNumber"));
            bill.setTotal(Integer.parseInt((String) requestMap.get("totalAmount")));
            bill.setProductDetails((String) requestMap.get("productDetails"));
            bill.setCreatedBy(jwtFilter.getCurrentUsername());
            bill.setStatus("In Progress");
            billDao.save(bill);

        } catch (Exception ex) {
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
        Stream.of("Name", "Category", "Quantity", "Price", "Sub Total")
                .forEach(title -> {
                    PdfPCell header = new PdfPCell(new Phrase(title));
                    header.setBackgroundColor(BaseColor.YELLOW);
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(header);
                });
    }

    private void addRows(PdfPTable table, Map<String, Object> data) {
        table.addCell((String) data.get("name"));
        table.addCell((String) data.get("category"));
        table.addCell(String.valueOf(data.get("quantity")));
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

