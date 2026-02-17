# Task: Update Grand Total Coordinate in PDF

## Objective
Update the Y-coordinate of the Grand Total in the bill PDF generation to position it correctly below the table.

## Current State
- Grand Total at coordinates (500, 150) - ABOVE the table (incorrect)
- Table starts at Y=610 and goes upward
- Last table row is around Y=480-520 depending on number of items

## Plan
1. Update Grand Total Y-coordinate from 150 to ~480-490 (below the table)
2. Add "एकूण" label before the amount

## Steps Completed
- [x] Read and understand BillServiceImpl.java
- [x] Identify the Grand Total drawText line
- [x] Update the Y-coordinate from 150 to 485
- [x] Add "एकूण:" label

## Changes Made
- Updated Grand Total from coordinates (500, 150) to (440, 485)
- Added "एकूण:" (Total) label in Marathi before the amount
- Result: "एकूण: ₹ <totalAmount>/-"
- Fixed "Pdf indirect object belongs to other PDF document" error by loading image as bytes first

