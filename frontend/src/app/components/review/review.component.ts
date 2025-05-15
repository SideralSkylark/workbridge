import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReviewService, ReviewRequestDTO } from '../../services/review.service';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-review',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="review-container" *ngIf="canReview">
      <h3>Write a Review</h3>
      <div class="rating">
        <span *ngFor="let star of [1,2,3,4,5]" 
              (click)="setRating(star)"
              [class.active]="star <= rating"
              class="star">★</span>
      </div>
      <textarea [(ngModel)]="comment" 
                (ngModelChange)="onCommentChange()"
                placeholder="Write your review here..."
                rows="4"></textarea>
      <button (click)="submitReview()" 
              [disabled]="!canSubmit">Submit Review</button>
    </div>
  `,
  styles: [`
    .review-container {
      margin: 1rem 0;
      padding: 1rem;
      border: 1px solid #ddd;
      border-radius: 4px;
    }
    .rating {
      margin: 1rem 0;
    }
    .star {
      cursor: pointer;
      font-size: 1.5rem;
      color: #ddd;
    }
    .star.active {
      color: gold;
    }
    textarea {
      width: 100%;
      margin: 0.5rem 0;
      padding: 0.5rem;
      border: 1px solid #ddd;
      border-radius: 4px;
    }
    button {
      padding: 0.5rem 1rem;
      background-color: #007bff;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }
    button:disabled {
      background-color: #ccc;
      cursor: not-allowed;
    }
  `]
})
export class ReviewComponent implements OnInit {
  @Input() providerId!: number;
  @Input() bookingId!: number;
  @Output() reviewSubmitted = new EventEmitter<void>();

  rating: number = 0;
  comment: string = '';
  canReview: boolean = false;
  canSubmit: boolean = false;

  constructor(
    private reviewService: ReviewService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.checkCanReview();
  }

  private checkCanReview(): void {
    const userId = this.authService.getUserId();
    if (!userId) {
      this.canReview = false;
      return;
    }

    // Check if the user has already reviewed this booking
    this.reviewService.hasUserReviewedBooking(this.bookingId).subscribe({
      next: (hasReviewed) => {
        if (hasReviewed) {
          this.canReview = false;
        } else {
          this.canReview = true;
          this.updateCanSubmit();
        }
      },
      error: (error) => {
        console.error('Error checking if booking was reviewed:', error);
        this.canReview = false;
      }
    });
  }

  setRating(rating: number): void {
    this.rating = rating;
    this.updateCanSubmit();
  }

  onCommentChange(): void {
    this.updateCanSubmit();
  }

  updateCanSubmit(): void {
    this.canSubmit = this.rating > 0 && this.comment.trim().length > 0;
  }

  submitReview(): void {
    if (!this.canSubmit) return;

    const userId = this.authService.getUserId();
    if (!userId) {
      console.error('User not authenticated');
      return;
    }

    const review: ReviewRequestDTO = {
      rating: this.rating,
      comment: this.comment,
      reviewerId: userId,
      reviewedId: this.providerId,
      bookingId: this.bookingId
    };

    this.reviewService.createReview(review).subscribe({
      next: () => {
        this.rating = 0;
        this.comment = '';
        this.canReview = false;
        this.reviewSubmitted.emit();
      },
      error: (error) => {
        console.error('Error submitting review:', error);
      }
    });
  }
} 