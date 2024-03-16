import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class FormStateService {
  private formOpenState = new BehaviorSubject<boolean>(false);
  formOpen$ = this.formOpenState.asObservable();

  setFormOpen(value: boolean) {
    this.formOpenState.next(value);
  }
}
