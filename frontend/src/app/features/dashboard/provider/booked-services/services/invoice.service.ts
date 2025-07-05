import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface Invoice {
  id?: number;
  jobName: string;
  providerName: string;
  clientName: string;
  country: string;
  region: string;
  issueDate?: string;
  dueDate: string;
  totalAmount: number;
  taxAmount?: number;
  taxRate?: number;
  currency: string;
  status?: string;
}

@Injectable({
  providedIn: 'root'
})
export class InvoiceService {
  private apiUrl = 'http://localhost:8180/invoices';

  constructor(private http: HttpClient) { }

  createInvoice(invoice: Invoice): Observable<Invoice> {
    return this.http.post<Invoice>(`${this.apiUrl}`, invoice);
  }

  getInvoicePdf(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/pdf`, {
      responseType: 'blob'
    });
  }

  sendInvoiceWithPdfEmail(invoiceData: any, pdfBlob: Blob): Observable<any> {
    const formData = new FormData();
    formData.append('invoiceData', new Blob([JSON.stringify(invoiceData)], { type: 'application/json' }));
    formData.append('pdf', pdfBlob, 'invoice.pdf');
  
    return this.http.post('http://localhost:8080/api/v1/invoices/send-email-with-pdf', formData);
  }   
}
