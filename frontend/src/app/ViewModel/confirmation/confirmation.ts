import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-confirmation',
  standalone: true,
  templateUrl: './confirmation.html',
  styleUrl: './confirmation.css'
})
export class Confirmation {

  title = input<string>('');
  message = input<string>('');
  action = input<string>('');

  // Return to parent
  confirmed = output<boolean>();
  // cancelled = output<void>();

  onConfirm() {
    console.log("Confirm");
    this.confirmed.emit(true);
  }

  onCancel() {
    console.log("Cancel");
    this.confirmed.emit(false);
  }
}
