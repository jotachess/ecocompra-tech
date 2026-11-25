import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { UsuarioService } from '../../core/services/usuarios.service';
import { AuthService } from '../../core/auth.service';
import { Usuario, RolUsuario } from '../../core/models';
import { apiError } from '../../core/http-error';
import { FieldError } from '../../core/field-error';
import { notBlank, passwordBytes } from '../../core/form-validation';
@Component({ selector: 'app-usuarios', imports: [ReactiveFormsModule, FieldError], templateUrl: './usuarios.html' })
export class Usuarios implements OnInit {
  private api = inject(UsuarioService);
  private fb = inject(FormBuilder);
  auth = inject(AuthService);
  rows = signal<Usuario[]>([]);
  error = signal(''); notice = signal(''); busy = signal(false);
  editing = signal<number | null>(null); pendingDelete = signal<number | null>(null);
  private usernameValidators = [Validators.required, notBlank, Validators.pattern(/^[a-z0-9][a-z0-9._-]{2,79}$/)];
  private passwordValidators = [Validators.required, notBlank, Validators.minLength(12), Validators.maxLength(72), passwordBytes];
  form = this.fb.nonNullable.group({
    username: ['', this.usernameValidators], password: ['', this.passwordValidators],
    rol: ['VENDEDOR' as RolUsuario, Validators.required], activo: [true],
  });
  ngOnInit() { this.load(); }
  load() { this.api.list().subscribe({ next: r => this.rows.set(r), error: e => this.error.set(apiError(e)) }); }
  edit(u: Usuario) {
    this.editing.set(u.id);this.error.set('');this.notice.set('');
    this.form.reset({ username: u.username, password: '', rol: u.rol, activo: u.activo });
    this.form.controls.username.disable();this.form.controls.password.disable();
  }
  reset() {
    this.editing.set(null);this.form.controls.username.enable();this.form.controls.password.enable();
    this.form.reset({ username: '', password: '', rol: 'VENDEDOR', activo: true });
  }
  save() {
    if(this.busy()) return;
    if(this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.busy.set(true);this.error.set('');this.notice.set('');
    const v = this.form.getRawValue();
    const request = this.editing() ? this.api.update(this.editing()!, { rol: v.rol, activo: v.activo })
      : this.api.create({ username: v.username, password: v.password, rol: v.rol });
    request.pipe(finalize(()=>this.busy.set(false))).subscribe({
      next: () => { this.notice.set('Usuario guardado correctamente.');this.reset();this.load(); },
      error: e => this.error.set(apiError(e)),
    });
  }
  remove(id: number) {
    if(this.busy()) return;
    this.busy.set(true);this.error.set('');this.notice.set('');
    this.api.remove(id).pipe(finalize(()=>this.busy.set(false))).subscribe({
      next: () => { this.notice.set('Usuario eliminado.');this.pendingDelete.set(null);this.load(); },
      error: e => { this.error.set(apiError(e));this.pendingDelete.set(null); },
    });
  }
}
