import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { BookingService } from './services/bookings.service';
import { AuthService } from '../../../auth/auth.service';
import { BookingResponseDTO } from './models/booking-responseDTO.model';
import { Router } from '@angular/router';
import { ChatService } from '../../chat/services/chat.service';
import { InvoiceService } from './services/invoice.service';
import { SafeUrlPipe } from '../../../../shared/pipes/safe-url.pipe';

@Component({
  selector: 'app-booked-services',
  standalone: true,
  imports: [CommonModule, SafeUrlPipe],
  templateUrl: './booked-services.component.html',
  styleUrls: ['./booked-services.component.scss']
})
export class BookedServicesComponent implements OnInit {
  isLoading = true;
  bookings: BookingResponseDTO[] = [];

  pdfPreviewUrl: string | null = null;
  showPdfPreview = false;
  currentInvoiceId: number | null = null;
  invoice: any;

  constructor(
    private bookingService: BookingService,
    private authService: AuthService,
    private router: Router,
    private chatService: ChatService,
    private invoiceService: InvoiceService
  ) {}

  ngOnInit() {
    const providerId = this.authService.getUserId(); // Get provider ID from AuthService

    if (providerId) {
      this.bookingService.getMyBookedServices(providerId).subscribe(
        (data: BookingResponseDTO[]) => {
          this.bookings = data;
          this.isLoading = false; // Data is fetched, set loading to false
          console.log(this.bookings);
        },
        (error) => {
          console.error('Error fetching bookings', error);
          this.isLoading = false; // Handle error and stop loading
        }
      );
    } else {
      console.error('No provider ID found');
      this.isLoading = false;
    }
  }

  openChat(customerId: string): void {
    this.chatService.ensureConnection(); // A gente vai criar isso também (pra resolver o problema 2)
    this.router.navigate(['/dashboard/chat'], {
      queryParams: { recipient: customerId }
    });
  }

  emitirFatura(booking: BookingResponseDTO): void {
    const invoice = {
      jobName: booking.serviceTitle,
      providerName: booking.providerName,
      clientName: booking.seekerName,
      country: 'MZ', // ou extraído de booking, se disponível
      region: 'National', // idem
      dueDate: new Date().toISOString(), // data de hoje
      totalAmount: booking.price,
      currency: 'MZN'
    };

    this.invoiceService.createInvoice(invoice).subscribe({
      next: (createdInvoice) => {
        const invoiceMapped = {
          id: (createdInvoice as any).ID,
          jobName: (createdInvoice as any).JobName,
          providerName: (createdInvoice as any).ProviderName,
          clientName: (createdInvoice as any).ClientName,
          country: (createdInvoice as any).Country,
          region: (createdInvoice as any).Region,
          issueDate: (createdInvoice as any).IssueDate,
          dueDate: (createdInvoice as any).DueDate,
          totalAmount: (createdInvoice as any).TotalAmount,
          taxAmount: (createdInvoice as any).TaxAmount,
          taxRate: (createdInvoice as any).TaxRate,
          currency: (createdInvoice as any).Currency,
          status: (createdInvoice as any).Status,
        };
        this.invoice = invoiceMapped;
        const id = invoiceMapped.id!;
        this.currentInvoiceId = id
        this.invoiceService.getInvoicePdf(id).subscribe(blob => {
          if (this.pdfPreviewUrl) {
            window.URL.revokeObjectURL(this.pdfPreviewUrl);
          }
          this.pdfPreviewUrl = window.URL.createObjectURL(blob);
          this.showPdfPreview = true;
        });
      },
      error: err => console.error('Erro ao emitir fatura:', err)
    });
  }

  abrirPdfEmNovaAba() {
    if (this.pdfPreviewUrl) {
      window.open(this.pdfPreviewUrl);
    }
  }

  fecharPreview() {
    this.showPdfPreview = false;
    if (this.pdfPreviewUrl) {
      window.URL.revokeObjectURL(this.pdfPreviewUrl);
      this.pdfPreviewUrl = null;
    }
  }

  enviarFaturaParaCliente() {
    if (!this.currentInvoiceId || !this.pdfPreviewUrl || !this.invoice) return;

    fetch(this.pdfPreviewUrl)
      .then(res => res.blob())
      .then(pdfBlob => {
        this.invoiceService.sendInvoiceWithPdfEmail(this.invoice, pdfBlob).subscribe({
          next: () => alert('Fatura enviada com sucesso!'),
          error: () => alert('Erro ao enviar a fatura.')
        });
      });
  }
}
