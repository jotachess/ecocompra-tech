import { FieldError } from '../../core/field-error';
import { notBlank } from '../../core/form-validation';
import { ClienteService } from '../../core/services/clientes.service';
import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { apiError } from '../../core/http-error';
import { AuthService } from '../../core/auth.service';
import { Cliente } from '../../core/models';
@Component({
  selector: 'app-clientes',
  imports: [FieldError, FormsModule, ReactiveFormsModule],
  templateUrl: './clientes.html',
})
export class Clientes implements OnInit {
  private clienteService = inject(ClienteService);
  private fb = inject(FormBuilder);
  auth = inject(AuthService);
  rows = signal<Cliente[]>([]);
  search = signal('');
  error = signal('');
  notice = signal('');
  busy = signal(false);
  editing = signal<number | null>(null);
  pendingDelete = signal<number | null>(null);
  filtered = computed(() =>
    this.rows().filter((c) =>
      (c.nombre + ' ' + (c.correo ?? '')).toLowerCase().includes(this.search().toLowerCase()),
    ),
  );
  form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, notBlank, Validators.maxLength(120)]],
    correo: ['', [Validators.email, Validators.maxLength(150)]],
    telefono: ['', Validators.maxLength(20)],
  });
  ngOnInit() {
    this.load();
  }
  load() {
    this.clienteService
      .list()
      .subscribe({ next: (r) => this.rows.set(r), error: (e) => this.error.set(apiError(e)) });
  }
  edit(c: Cliente) {
    this.editing.set(c.idCliente);
    this.form.reset({ nombre: c.nombre, correo: c.correo ?? '', telefono: c.telefono ?? '' });
    this.error.set('');
    this.notice.set('');
  }
  reset() {
    this.editing.set(null);
    this.form.reset();
  }
  save() {
    if (this.busy()) return;
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.busy.set(true);
    this.notice.set('');
    this.error.set('');
    const v = this.form.getRawValue();
    const body = {
      nombre: v.nombre.trim(),
      correo: v.correo.trim() || null,
      telefono: v.telefono.trim() || null,
    };
    const req = this.editing()
      ? this.clienteService.update(this.editing()!, body)
      : this.clienteService.create(body);
    req.pipe(finalize(() => this.busy.set(false))).subscribe({
      next: () => {
        this.notice.set('Cliente guardado correctamente.');
        this.reset();
        this.load();
      },
      error: (e) => this.error.set(apiError(e)),
    });
  }
  remove(id: number) {
    if (this.busy()) return;
    this.busy.set(true);
    this.notice.set('');
    this.error.set('');
    this.clienteService.remove(id).pipe(finalize(() => this.busy.set(false))).subscribe({
      next: () => {
        this.notice.set('Cliente eliminado.');
        this.pendingDelete.set(null);
        this.load();
      },
      error: (e) => {
        this.error.set(apiError(e));
        this.pendingDelete.set(null);
      },
    });
  }
}

