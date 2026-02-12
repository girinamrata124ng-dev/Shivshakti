import { Component, OnInit, Inject, ChangeDetectorRef } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { BillService } from 'src/app/services/bill.service';
import { SnackbarService } from 'src/app/services/snackbar.service';
import { GlobalConstants } from 'src/app/shared/global-constants';

@Component({
  selector: 'app-complete-order',
  templateUrl: './complete-order.component.html',
  styleUrls: ['./complete-order.component.scss']
})
export class CompleteOrderComponent implements OnInit {
  displayedColumns: string[] = [
    'name',
    'price',
    'quantity',
    'plus',
    'waste',
    'wastage2',
    'total',
    'action'
  ];
  dataSource: any;
  data: any;
  responseMessage: any;

  constructor(
    @Inject(MAT_DIALOG_DATA) public dialogData: any,
    public dialogRef: MatDialogRef<CompleteOrderComponent>,
    private billService: BillService,
    private snackbarService: SnackbarService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.data = this.dialogData.data;
    this.dataSource = JSON.parse(this.dialogData.data.productDetails);
  }

  calculateValues(element: any) {
    // Calculate Daal and Waste from Wastage 2
    const wastage2 = element.wastage2 || 0;
    const quantity = parseFloat(element.quantity) || 0;
    
    // Calculate updated quantity: quantity = quantity - wastage2
    const updatedQuantity = Math.max(0, quantity - wastage2);
    
    // Calculate waste: waste = updatedQuantity / 3.33
    const calculatedWaste = parseFloat((updatedQuantity / 3.33).toFixed(2));
    
    // Calculate Daal: Daal = updatedQuantity - waste
    const calculatedDaal = parseFloat((updatedQuantity - calculatedWaste).toFixed(2));
    
    // Update the element with calculated values
    element.plus = calculatedDaal;
    element.waste = calculatedWaste;
    
    return element;
  }

  previewCalculation(element: any, index: number) {
    // Calculate and preview Daal and Waste values in real-time as user types
    // Allow wastage2 to be 0 or greater
    if (element.wastage2 !== null && element.wastage2 !== undefined && element.wastage2 >= 0) {
      this.calculateValues(element);
      // Update the dataSource with calculated values for preview
      this.dataSource[index].plus = element.plus;
      this.dataSource[index].waste = element.waste;
      this.dataSource[index].wastage2 = element.wastage2;
      // Trigger change detection to update UI
      this.cdr.detectChanges();
    }
  }

  saveForProduct(element: any, index: number) {
    if (element.wastage2 === null || element.wastage2 === undefined) {
      this.snackbarService.openSnackBar('Please enter a Wastage 2 value', 'error');
      return;
    }

    // Calculate and update Daal and Waste values immediately on UI
    this.calculateValues(element);
    
    // Update the dataSource with calculated values
    this.dataSource[index].plus = element.plus;
    this.dataSource[index].waste = element.waste;
    this.dataSource[index].wastage2 = element.wastage2;
    
    // Trigger change detection to update UI
    this.cdr.detectChanges();

    const data = {
      bill: this.data.bill,
      productDetails: JSON.stringify(this.dataSource)
    };

    this.billService.updateProductDetails(data).subscribe(
      (response: any) => {
        this.responseMessage = response?.message;
        this.snackbarService.openSnackBar(this.responseMessage, 'success');
        // Refresh the data to ensure consistency
        this.refreshData();
      },
      (error: any) => {
        console.log(error.error?.message);
        if (error.error?.message) {
          this.responseMessage = error.error?.message;
        } else {
          this.responseMessage = GlobalConstants.genericError;
        }
        this.snackbarService.openSnackBar(
          this.responseMessage,
          GlobalConstants.error
        );
      }
    );
  }

  refreshData() {
    // Fetch the latest bill data from the database
    this.billService.getBills().subscribe(
      (response: any) => {
        const updatedBill = response.find((bill: any) => bill.bill === this.data.bill);
        if (updatedBill) {
          this.data = updatedBill;
          this.dataSource = JSON.parse(updatedBill.productDetails);
          // Update the dialog data reference
          this.dialogData.data = updatedBill;
        }
      },
      (error: any) => {
        console.log('Error refreshing data:', error);
      }
    );
  }
}

