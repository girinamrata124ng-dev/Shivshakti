# Implementation Plan: Mark as Completed Dialog with Wastage 2 Editing

## User Requirements:
1. Add new popup dialog on "Mark as Completed" button in In Progress section
2. Dialog should show all customer records (like View Bill button)
3. Wastage 2 field should be editable
4. Logic when Wastage 2 value is entered:
   - quantity = quantity - wastage2
   - waste = updated quantity / 3.33
   - Daal = updated quantity - waste
5. Update database with new values
6. Completed page should have all values from dialog
7. Bill PDF should have new values

## Files to Create/Modify:

### Frontend:
1. Create: `dialog/complete-order/complete-order.component.html`
2. Create: `dialog/complete-order/complete-order.component.ts`
3. Modify: `view-bill/view-bill.component.ts` - changeStatus method to open dialog
4. Modify: `services/bill.service.ts` - add updateProductDetails method

### Backend:
5. Modify: `BillService.java` - add updateProductDetails method
6. Modify: `BillRest.java` - add endpoint for updating product details
7. Modify: `BillRestImpl.java` - implement updateProductDetails logic

## Steps:
1. Create complete-order dialog component with editable Wastage 2 field
2. Add method to calculate: updatedQuantity, waste, Daal
3. Update bill service to call backend API
4. Update backend to handle calculations and save to database
5. Update PDF generation to use new values

