import { ProductoService } from '../../core/services/productos.service';
import { ClienteService } from '../../core/services/clientes.service';
import { VentaService } from '../../core/services/ventas.service';
import { Component, inject, signal, OnInit } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { apiError } from '../../core/http-error';
import { AuthService } from '../../core/auth.service';
import { Producto, Cliente, Venta } from '../../core/models';
@Component({
  selector: 'app-inicio',
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './inicio.html',
})
export class Inicio implements OnInit {
  auth = inject(AuthService);
  private productoService = inject(ProductoService);
  private clienteService = inject(ClienteService);
  private ventaService = inject(VentaService);
  productos = signal<Producto[]>([]);
  clientes = signal<Cliente[]>([]);
  ventas = signal<Venta[]>([]);
  error = signal('');
  loading = signal(true);
  ngOnInit() {
    forkJoin({
      p: this.productoService.list(),
      c: this.clienteService.list(),
      v: this.ventaService.list(),
    }).subscribe({
      next: (r) => {
        this.productos.set(r.p);
        this.clientes.set(r.c);
        this.ventas.set(r.v);
        this.loading.set(false);
      },
      error: (e) => {
        this.error.set(apiError(e));
        this.loading.set(false);
      },
    });
  }
  documentados() {
    return this.productos().filter((p) => !!p.fuenteTecnica).length;
  }
}

