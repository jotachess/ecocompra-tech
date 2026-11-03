import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { ApiService, apiError } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { Cliente } from '../../core/models';
@Component({
  selector: 'app-clientes',
  imports: [FormsModule, ReactiveFormsModule],
  templateUrl: './clientes.html',
})
export class Clientes implements OnInit {
  private api = inject(ApiService);
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
    nombre: ['', [Validators.required, Validators.maxLength(120)]],
    correo: ['', [Validators.email, Validators.maxLength(150)]],
    telefono: ['', Validators.maxLength(20)],
  });
  ngOnInit() {
    this.load();
  }
  load() {
    this.api
      .list<Cliente>('clientes')
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
    if (this.form.invalid || this.busy()) return;
    this.busy.set(true);
    this.error.set('');
    const v = this.form.getRawValue();
    const body = {
      nombre: v.nombre.trim(),
      correo: v.correo.trim() || null,
      telefono: v.telefono.trim() || null,
    };
    const req = this.editing()
      ? this.api.update<Cliente>('clientes', this.editing()!, body)
      : this.api.create<Cliente>('clientes', body);
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
    this.api.remove('clientes', id).subscribe({
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
