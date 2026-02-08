# TODO: Remove email/paymentMethod and add billNo auto-increment

## Backend Changes
- [x] 1. Update Bill.java POJO (add billNo, remove email, paymentMethod)
- [x] 2. Update BillServiceImpl.java (update PDF generation, insertBill, validateRequestMap)
- [x] 3. Update BillDao.java (add findByBillNo method)
- [x] 4. Create SQL migration script for database changes

## Frontend Changes
- [x] 5. Update manage-order.component.html (remove email, paymentMethod form fields)
- [x] 6. Update manage-order.component.ts (remove email, paymentMethod from FormGroup and logic)
- [x] 7. Update view-bill.component.html (add billNo column, remove email/paymentMethod)
- [x] 8. Update view-bill.component.ts (update displayedColumns and download logic)
- [x] 9. Update view-bill-products.component.html (remove email/paymentMethod display)

## Database Changes
- [x] 10. Execute SQL migration script

