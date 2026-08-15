import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common'; // <-- IMPORT OBLIGATOIRE POUR *ngIf

@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [CommonModule], // <-- AJOUT ICI
  templateUrl: './modal.html',
  styleUrls: ['./modal.css']
})
export class ModalComponent {

  // Le test dépend de cette propriété
  @Input() visible = false;

  @Input() title = '';
  @Input() message = '';

  @Output() close = new EventEmitter<void>();
  @Output() confirm = new EventEmitter<void>();

  onClose(): void {
    this.close.emit();
  }

  onConfirm(event?: Event): void {
    // Empêche tout comportement par défaut / double émission
    event?.preventDefault();
    event?.stopPropagation();
    this.confirm.emit();
  }
}
