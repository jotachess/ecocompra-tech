import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL, Categoria, Producto, ProductoRequest } from '../models';

@Injectable({ providedIn: 'root' })
export class ProductoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_URL}/productos`;

  list() { return this.http.get<Producto[]>(this.url); }
  get(id: number) { return this.http.get<Producto>(`${this.url}/${id}`); }
  create(body: ProductoRequest) { return this.http.post<Producto>(this.url, body); }
  update(id: number, body: ProductoRequest) {
    return this.http.put<Producto>(`${this.url}/${id}`, body);
  }
  remove(id: number) { return this.http.delete<void>(`${this.url}/${id}`); }
  categories() { return this.http.get<Categoria[]>(`${API_URL}/categorias`); }
}
