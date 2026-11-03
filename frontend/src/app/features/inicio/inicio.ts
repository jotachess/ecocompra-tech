import { Component, inject, signal, OnInit } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ApiService, apiError } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { Producto, Cliente, Venta } from '../../core/models';
@Component({
  selector: 'app-inicio',
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './inicio.html',
})
export class Inicio implements OnInit {
  auth = inject(AuthService);
  private api = inject(ApiService);
  productos = signal<Producto[]>([]);
  clientes = signal<Cliente[]>([]);
  ventas = signal<Venta[]>([]);
  error = signal('');
  loading = signal(true);
  ngOnInit() {
    forkJoin({
      p: this.api.list<Producto>('productos'),
      c: this.api.list<Cliente>('clientes'),
      v: this.api.list<Venta>('ventas'),
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
