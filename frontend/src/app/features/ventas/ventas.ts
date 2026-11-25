import { FieldError } from '../../core/field-error';
import { notBlank } from '../../core/form-validation';
import { VentaService } from '../../core/services/ventas.service';
import { ProductoService } from '../../core/services/productos.service';
import { ClienteService } from '../../core/services/clientes.service';
import { Component, inject, signal, OnInit } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import { apiError } from '../../core/http-error';
import { AuthService } from '../../core/auth.service';
import { Producto, Cliente, Venta } from '../../core/models';
@Component({
  selector: 'app-ventas',
  imports: [FieldError, CurrencyPipe, ReactiveFormsModule],
  templateUrl: './ventas.html',
})
export class Ventas implements OnInit {
  private fb = inject(FormBuilder);
  private ventaService = inject(VentaService);
  private productoService = inject(ProductoService);
  private clienteService = inject(ClienteService);
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
      v: this.ventaService.list(),
      p: this.productoService.list(),
      c: this.clienteService.list(),
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
    if (this.busy()) return;
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.busy.set(true);
    this.notice.set('');
    this.error.set('');
    const values = this.form.getRawValue();
    const body = {
      idCliente: values.idCliente!,
      detalles: values.detalles.map((d) => ({
        idProducto: d.idProducto!, cantidad: d.cantidad!, precioUnitario: d.precioUnitario,
      })),
    };
    const req = this.editing()
      ? this.ventaService.update(this.editing()!, body)
      : this.ventaService.create(body);
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
    if (this.busy()) return;
    this.busy.set(true);
    this.notice.set('');
    this.error.set('');
    this.ventaService.remove(id).pipe(finalize(() => this.busy.set(false))).subscribe({
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

