import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/auth.service';
import { apiError } from '../../core/api.service';
@Component({ selector: 'app-login', imports: [ReactiveFormsModule], templateUrl: './login.html' })
export class Login {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  busy = signal(false);
  error = signal('');
  form = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.maxLength(80)]],
    password: ['', [Validators.required, Validators.maxLength(72)]],
  });
  constructor() {
    if (this.auth.valid()) void this.router.navigate(['/inicio']);
  }
  submit() {
    if (this.form.invalid || this.busy()) return;
    this.busy.set(true);
    this.error.set('');
    const { username, password } = this.form.getRawValue();
    this.auth
      .login(username, password)
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: () => void this.router.navigate(['/inicio']),
        error: (e) => this.error.set(apiError(e)),
      });
  }
}
