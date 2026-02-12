import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root',
})
export class BillService {
  url = environment.apiUrl;
  constructor(private httpClient: HttpClient) {}

  generateReport(data: any) {
    return this.httpClient.post(this.url + '/bill/generateReport', data, {
      headers: new HttpHeaders().set('Content-Type', 'application/json'),
    });
  }

  getPdf(data: any): Observable<Blob> {
    return this.httpClient.post(this.url + '/bill/getPdf', data, {
      responseType: 'blob',
    });
  }

  getBills() {
    return this.httpClient.get(this.url + '/bill/getBills');
  }

  getBillsByStatus(status: string) {
    return this.httpClient.get(this.url + '/bill/getBillsByStatus/' + status);
  }

  updateStatus(bill: any, status: string) {
    return this.httpClient.post(this.url + '/bill/updateStatus/' + bill, { status: status }, {
      headers: new HttpHeaders().set('Content-Type', 'application/json'),
    });
  }

  delete(bill: any) {
    return this.httpClient.post(this.url + '/bill/delete/' + bill, {
      headers: new HttpHeaders().set('Content-Type', 'application/json'),
    });
  }

  updateProductDetails(data: any) {
    return this.httpClient.post(this.url + '/bill/updateProductDetails', data, {
      headers: new HttpHeaders().set('Content-Type', 'application/json'),
    });
  }
}
