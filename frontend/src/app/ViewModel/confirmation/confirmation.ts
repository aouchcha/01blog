import { Component, input, output } from '@angular/core';
import { MatIcon } from '@angular/material/icon';

@Component({
  imports: [MatIcon],
  selector: 'app-confirmation',
  standalone: true,
  templateUrl: './confirmation.html',
  styleUrl: './confirmation.css'
})
export class Confirmation {

  title = input<string>('');
  message = input<string>('');
  action = input<string>('');

  confirmed = output<boolean>();

  onConfirm() {
    this.confirmed.emit(true);
  }

  onCancel() {
    this.confirmed.emit(false);
  }
}
