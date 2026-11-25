import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL, Usuario, UsuarioCreate, UsuarioUpdate } from '../models';
@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private http = inject(HttpClient);
  private url = `${API_URL}/usuarios`;
  list() { return this.http.get<Usuario[]>(this.url); }
  create(body: UsuarioCreate) { return this.http.post<Usuario>(this.url, body); }
  update(id: number, body: UsuarioUpdate) { return this.http.put<Usuario>(`${this.url}/${id}`, body); }
  remove(id: number) { return this.http.delete<void>(`${this.url}/${id}`); }
}
