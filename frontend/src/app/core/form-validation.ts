import { AbstractControl, ValidationErrors } from '@angular/forms';
export function notBlank(control: AbstractControl): ValidationErrors | null {
  return typeof control.value === 'string' && !control.value.trim() ? { blank: true } : null;
}
export function passwordBytes(control: AbstractControl): ValidationErrors | null {
  return new TextEncoder().encode(control.value ?? '').length > 72 ? { passwordBytes: true } : null;
}
