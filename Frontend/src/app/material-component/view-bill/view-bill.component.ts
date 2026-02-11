import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { saveAs } from 'file-saver';
import { BillService } from 'src/app/services/bill.service';
import { GlobalConstants } from 'src/app/shared/global-constants';
import { ViewBillProductsComponent } from '../dialog/view-bill-products/view-bill-products.component';
import { SnackbarService } from 'src/app/services/snackbar.service';
import { ConfirmationComponent } from '../dialog/confirmation/confirmation.component';

@Component({
  selector: 'app-view-bill',
  templateUrl: './view-bill.component.html',
  styleUrls: ['./view-bill.component.scss'],
})
export class ViewBillComponent implements OnInit {
  displayedColumns: string[] = [
    'bill',
    'name',
    'contactNumber',
    'total',
    'status',
    'view',
  ];
  inProgressDataSource: any;
  completedDataSource: any;
  deliveredDataSource: any;
  responseMessage: any;

  constructor(
    private billservice: BillService,
    private dialog: MatDialog,
    private SnackbarService: SnackbarService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.tableData();
  }

  tableData() {
    this.billservice.getBills().subscribe(
      (response: any) => {
        // Filter bills by status
        const inProgress = response.filter((bill: any) => bill.status === 'In Progress' || !bill.status);
        const completed = response.filter((bill: any) => bill.status === 'Completed');
        const delivered = response.filter((bill: any) => bill.status === 'Delivered');

        this.inProgressDataSource = new MatTableDataSource(inProgress);
        this.completedDataSource = new MatTableDataSource(completed);
        this.deliveredDataSource = new MatTableDataSource(delivered);
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

  applyFilter(event: Event, dataSource: MatTableDataSource<any>) {
    const filterValue = (event.target as HTMLInputElement).value;
    dataSource.filter = filterValue.trim().toLowerCase();
  }

  handleViewAction(values: any) {
    const dialogConfog = new MatDialogConfig();
    dialogConfog.data = {
      data: values,
    };
    dialogConfog.width = '100%';
    const dialogRef = this.dialog.open(ViewBillProductsComponent, dialogConfog);
    this.router.events.subscribe(() => {
      dialogRef.close();
    });
  }

  handleDeleteAction(values: any) {
    const dialogConfog = new MatDialogConfig();
    dialogConfog.data = {
      message: 'delete ' + values.name + ' bill',
      confirmation: true,
    };
    const dialogRef = this.dialog.open(ConfirmationComponent, dialogConfog);
    const sub = dialogRef.componentInstance.onEmistStatusChange.subscribe(
      (response) => {
        this.deleteBill(values.bill);
        dialogRef.close();
      }
    );
  }

  deleteBill(bill: any) {
    this.billservice.delete(bill).subscribe(
      (response: any) => {
        this.tableData();
        this.responseMessage = response?.message;
        this.SnackbarService.openSnackBar(this.responseMessage, 'success');
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

  changeStatus(values: any) {
    let newStatus: string;
    
    // 3-stage workflow: In Progress → Completed → Delivered → Completed
    if (values.status === 'In Progress' || !values.status) {
      newStatus = 'Completed';
    } else if (values.status === 'Completed') {
      newStatus = 'Delivered';
    } else if (values.status === 'Delivered') {
      newStatus = 'Completed';
    } else {
      newStatus = 'Completed';
    }
    
    this.billservice.updateStatus(values.bill, newStatus).subscribe(
      (response: any) => {
        this.tableData();
        this.responseMessage = response?.message;
        this.SnackbarService.openSnackBar(this.responseMessage, 'success');
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

  downloadReportAction(values: any) {
    var data = {
      bill: values.bill,
    };
    this.downloadFile(values.bill, data);
  }

  downloadFile(fileName: string, data: any) {
    this.billservice.getPdf(data).subscribe((response: any) => {
      saveAs(response, 'Bill_' + fileName + '.pdf');
    });
  }
}
