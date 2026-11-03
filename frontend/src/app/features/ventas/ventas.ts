import { Component, inject, signal, OnInit } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import { ApiService, apiError } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { Producto, Cliente, Venta } from '../../core/models';
@Component({
  selector: 'app-ventas',
  imports: [CurrencyPipe, ReactiveFormsModule],
  templateUrl: './ventas.html',
})
export class Ventas implements OnInit {
  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  auth = inject(AuthService);
  rows = signal<Venta[]>([]);
  products = signal<Producto[]>([]);
  clients = signal<Cliente[]>([]);
  error = signal('');
  notice = signal('');
  busy = signal(false);
  editing = signal<number | null>(null);
  selected = signal<Venta | null>(null);
  pendingDelete = signal<number | null>(null);
  line() {
    return this.fb.group({
      idProducto: [null as number | null, Validators.required],
      cantidad: [1, [Validators.required, Validators.min(1), Validators.pattern(/^\d+$/)]],
      precioUnitario: [null as number | null],
    });
  }
  form = this.fb.group({
    idCliente: [null as number | null, Validators.required],
    detalles: this.fb.array([this.line()]),
  });
  get details() {
    return this.form.controls.detalles;
  }
  ngOnInit() {
    this.load();
  }
  load() {
    forkJoin({
      v: this.api.list<Venta>('ventas'),
      p: this.api.list<Producto>('productos'),
      c: this.api.list<Cliente>('clientes'),
    }).subscribe({
      next: (r) => {
        this.rows.set(r.v);
        this.products.set(r.p);
        this.clients.set(r.c);
      },
      error: (e) => this.error.set(apiError(e)),
    });
  }
  total(v: Venta) {
    return v.detalles.reduce((s, d) => s + d.cantidad * d.precioUnitario, 0);
  }
  estimated() {
    return this.details.controls.reduce((sum, line) => {
      const v = line.getRawValue();
      const price =
        v.precioUnitario ??
        this.products().find((p) => p.idProducto === Number(v.idProducto))?.precio ??
        0;
      return sum + Number(v.cantidad) * price;
    }, 0);
  }
  edit(v: Venta) {
    this.editing.set(v.idVenta);
    this.details.clear();
    for (const d of v.detalles) {
      const line = this.line();
      line.setValue({
        idProducto: d.producto.idProducto,
        cantidad: d.cantidad,
        precioUnitario: d.precioUnitario,
      });
      this.details.push(line);
    }
    this.form.controls.idCliente.setValue(v.cliente.idCliente);
    this.notice.set('');
    this.error.set('');
  }
  reset() {
    this.editing.set(null);
    this.form.reset();
    this.details.clear();
    this.details.push(this.line());
  }
  save() {
    if (this.form.invalid || this.busy()) return;
    this.busy.set(true);
    this.error.set('');
    const body = this.form.getRawValue();
    const req = this.editing()
      ? this.api.update<Venta>('ventas', this.editing()!, body)
      : this.api.create<Venta>('ventas', body);
    req.pipe(finalize(() => this.busy.set(false))).subscribe({
      next: () => {
        this.notice.set('Venta guardada y stock actualizado.');
        this.reset();
        this.load();
      },
      error: (e) => this.error.set(apiError(e)),
    });
  }
  remove(id: number) {
    this.api.remove('ventas', id).subscribe({
      next: () => {
        this.notice.set('Venta eliminada y stock restituido.');
        this.pendingDelete.set(null);
        this.selected.set(null);
        this.load();
      },
      error: (e) => {
        this.error.set(apiError(e));
        this.pendingDelete.set(null);
      },
    });
  }
}
