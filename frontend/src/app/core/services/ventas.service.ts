import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL, Venta, VentaRequest } from '../models';

@Injectable({ providedIn: 'root' })
export class VentaService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_URL}/ventas`;

  list() { return this.http.get<Venta[]>(this.url); }
  get(id: number) { return this.http.get<Venta>(`${this.url}/${id}`); }
  create(body: VentaRequest) { return this.http.post<Venta>(this.url, body); }
  update(id: number, body: VentaRequest) {
    return this.http.put<Venta>(`${this.url}/${id}`, body);
  }
  remove(id: number) { return this.http.delete<void>(`${this.url}/${id}`); }
}
