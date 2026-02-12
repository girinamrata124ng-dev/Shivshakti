# TODO: Add "Wastage 2" Field Implementation

## Frontend Changes

### 1. manage-order.component.html
- [x] Add optional input field for "Wastage 2" in Select Product form
- [x] Add "Wastage 2" column after "Waste" in the table

### 2. manage-order.component.ts
- [x] Add form control for wastage2
- [x] Add 'wastage2' to displayedColumns array
- [x] Update add() method to include wastage2 in dataSource

### 3. view-bill-products.component.html
- [x] Add "Wastage 2" column after "Waste" in the table

### 4. view-bill-products.component.ts
- [x] Add 'wastage2' to dataplayedColumns array

## Backend Changes

### 5. BillServiceImpl.java
- [x] Update addTableHeader() to include "Wastage 2" (7 columns total)
- [x] Update addRows() to include wastage2 data
- [x] Change PdfPTable from 6 to 7 columns

## Testing
- [ ] Test manage order page with Wastage 2 field
- [ ] Test view bill dialog shows Wastage 2
- [ ] Test PDF generation includes Wastage 2 column
