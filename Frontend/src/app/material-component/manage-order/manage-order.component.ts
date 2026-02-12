import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { BillService } from 'src/app/services/bill.service';
import { ProductService } from 'src/app/services/product.service';
import { SnackbarService } from 'src/app/services/snackbar.service';
import { GlobalConstants } from 'src/app/shared/global-constants';
import { saveAs } from 'file-saver';

@Component({
  selector: 'app-manage-order',
  templateUrl: './manage-order.component.html',
  styleUrls: ['./manage-order.component.scss'],
})
export class ManageOrderComponent implements OnInit {
  displayedColumns: string[] = [
    'name',
    'price',
    'quantity',
    'plus',
    'waste',
    'wastage2',
    'total',
    'bill',
    'edit',
  ];
  dataSource: any = [];
  manageOrderForm: any = FormGroup;
  products: any = [];
  price: any;
  totalAmount: number = 0;
  bill: number = 0;
  responseMessage: any;

  constructor(
    private formBulider: FormBuilder,
    private productService: ProductService,
    private billService: BillService,
    private dialog: MatDialog,
    private SnackbarService: SnackbarService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.getProducts();
    this.manageOrderForm = this.formBulider.group({
      bill: [0, [Validators.required]],
      name: [
        null,
        [Validators.required, Validators.pattern(GlobalConstants.nameRegex)],
      ],
      contactNumber: [null, [Validators.required]],
      product: [null, [Validators.required]],
      quantity: [null, [Validators.required]],
      price: [null, [Validators.required]],
      total: [0, [Validators.required]],
      wastage2: [null],
    });
  }

  getProducts() {
    this.productService.getProducts().subscribe(
      (response: any) => {
        this.products = response;
      },
      (error: any) => {
        console.log(error.error?.message);
        if (error.error?.message) {
          this.responseMessage = error.error?.message;
        } else {
          this.responseMessage = GlobalConstants.genericError;
        }
        this.SnackbarService.openSnackBar(
          this.responseMessage,
          GlobalConstants.error
        );
      }
    );
  }

  getProductDetails(value: any) {
    this.productService.getById(value.id).subscribe(
      (response: any) => {
        this.price = response.price;
        this.manageOrderForm.controls['price'].setValue(response.price);
        this.manageOrderForm.controls['quantity'].setValue('1');
        this.manageOrderForm.controls['total'].setValue(this.price * 1);
      },
      (error: any) => {
        console.log(error.error?.message);
        if (error.error?.message) {
          this.responseMessage = error.error?.message;
        } else {
          this.responseMessage = GlobalConstants.genericError;
        }
        this.SnackbarService.openSnackBar(
          this.responseMessage,
          GlobalConstants.error
        );
      }
    );
  }

  setQuantity(value: any) {
    var temp = this.manageOrderForm.controls['quantity'].value;
    if (temp > 0) {
      this.manageOrderForm.controls['total'].setValue(
        this.manageOrderForm.controls['quantity'].value *
          this.manageOrderForm.controls['price'].value
      );
    } else if (temp != '') {
      this.manageOrderForm.controls['quantity'].setValue('1');
      this.manageOrderForm.controls['total'].setValue(
        this.manageOrderForm.controls['quantity'].value *
          this.manageOrderForm.controls['price'].value
      );
    }
  }

  validateProductAdd() {
    var fromData = this.manageOrderForm.value;
    var Value = this.manageOrderForm.controls['price'].value;
    if (
      Value === null ||
      fromData?.product?.total === 0 ||
      fromData?.product?.total === '' ||
      fromData?.product?.quantity <= 0
    ) {
      return true;
    } else {
      return false;
    }
  }

  validateSubmit() {
    var formData = this.manageOrderForm.value;
    if (
      this.totalAmount === 0 ||
      !formData.product ||
      !formData.product.name ||
      !formData.contactNumber
    ) {
      return true;
    } else {
      return false;
    }
  }

  add() {
    var fromData = this.manageOrderForm.value;
    var productName = this.dataSource.find(
      (e: { id: number }) => e.id === fromData.product.id
    );
    if (productName === undefined) {
      this.totalAmount = this.totalAmount + fromData.total;
      this.dataSource.push({
        id: fromData.product.id,
        name: fromData.product.name,
        category: 'General',
        quantity: fromData.quantity,
        plus: null,
        waste: null,
        wastage2: fromData.wastage2 || null,
        price: fromData.price,
        total: fromData.total,
      });
      this.dataSource = [...this.dataSource];
      this.SnackbarService.openSnackBar(
        GlobalConstants.productAdded,
        'Success'
      );
    } else {
      this.SnackbarService.openSnackBar(
        GlobalConstants.productExistError,
        GlobalConstants.error
      );
    }
  }

  handleDeleteAction(value: any, element: any) {
    this.totalAmount = this.totalAmount = element.total;
    this.dataSource.splice(value, 1);
    this.dataSource = [...this.dataSource];
  }

  submitAction() {
    var formData = this.manageOrderForm.value;
    var data = {
      name: formData.name,
      contactNumber: formData.contactNumber,
      totalAmount: this.totalAmount.toString(),
      productDetails: JSON.stringify(this.dataSource),
    };

    this.billService.generateReport(data).subscribe(
      (response: any) => {
        // Extract bill from response and store it
        this.bill = response.bill || 0;
        
        // Update dataSource with bill for all items
        this.dataSource = this.dataSource.map((item: any) => ({
          ...item,
          bill: this.bill
        }));
        
        this.downloadFile(response?.bill);
        
        // Properly reset form with initial values
        this.manageOrderForm = this.formBulider.group({
          bill: [0, [Validators.required]],
          name: [
            null,
            [Validators.required, Validators.pattern(GlobalConstants.nameRegex)],
          ],
          contactNumber: [null, [Validators.required]],
          product: [null, [Validators.required]],
          quantity: [null, [Validators.required]],
          price: [null, [Validators.required]],
          total: [0, [Validators.required]],
          wastage2: [null],
        });
        this.dataSource = [];
        this.totalAmount = 0;
        this.bill = 0;
      },
      (error: any) => {
        console.log(error.error?.message);
        if (error.error?.message) {
          this.responseMessage = error.error?.message;
        } else {
          this.responseMessage = GlobalConstants.genericError;
        }
        this.SnackbarService.openSnackBar(
          this.responseMessage,
          GlobalConstants.error
        );
      }
    );
  }
  
  downloadFile(fileName: string) {
    var data = {
      bill: fileName,
    };
    this.billService.getPdf(data).subscribe((response: any) => {
      saveAs(response, 'Bill_' + fileName + '.pdf');
    });
  }
}
