import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { AbstractControl } from '@angular/forms';
@Component({
  selector: 'app-field-error',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `@if (control && control.invalid && (control.touched || control.dirty)) {
    <p class="field-error" role="alert">{{ message() }}</p>
  }`,
})
export class FieldError {
  @Input({ required: true }) control: AbstractControl | null = null;
  @Input() label = 'El campo';
  message() {
    const e = this.control?.errors ?? {};
    if (e['required'] || e['blank']) return `${this.label} es obligatorio y no puede contener solo espacios.`;
    if (e['email']) return 'Ingresa un correo válido.';
    if (e['minlength']) return `${this.label} requiere al menos ${e['minlength'].requiredLength} caracteres.`;
    if (e['maxlength']) return `${this.label} admite hasta ${e['maxlength'].requiredLength} caracteres.`;
    if (e['min']) return `${this.label} debe ser mayor o igual a ${e['min'].min}.`;
    if (e['max']) return `${this.label} no puede superar ${e['max'].max}.`;
    if (e['passwordBytes']) return 'La contraseña no puede superar 72 bytes UTF-8.';
    if (e['pattern']) return `${this.label} tiene un formato inválido. Revisa las indicaciones del campo.`;
    return `Revisa ${this.label.toLocaleLowerCase()}.`;
  }
}
