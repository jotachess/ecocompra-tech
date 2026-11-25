import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL, Cliente, ClienteRequest } from '../models';

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_URL}/clientes`;

  list() { return this.http.get<Cliente[]>(this.url); }
  get(id: number) { return this.http.get<Cliente>(`${this.url}/${id}`); }
  create(body: ClienteRequest) { return this.http.post<Cliente>(this.url, body); }
  update(id: number, body: ClienteRequest) {
    return this.http.put<Cliente>(`${this.url}/${id}`, body);
  }
  remove(id: number) { return this.http.delete<void>(`${this.url}/${id}`); }
}
