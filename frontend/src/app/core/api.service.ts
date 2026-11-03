import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { API_URL } from './models';
@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  list<T>(resource: string) {
    return this.http.get<T[]>(`${API_URL}/${resource}`);
  }
  create<T>(resource: string, body: unknown) {
    return this.http.post<T>(`${API_URL}/${resource}`, body);
  }
  update<T>(resource: string, id: number, body: unknown) {
    return this.http.put<T>(`${API_URL}/${resource}/${id}`, body);
  }
  remove(resource: string, id: number) {
    return this.http.delete<void>(`${API_URL}/${resource}/${id}`);
  }
}
export function apiError(error: HttpErrorResponse): string {
  if (!error.status)
    return 'No se pudo conectar con el servidor. Verifica que el sistema esté iniciado.';
  const fields = error.error?.validaciones as Record<string, string> | undefined;
  return fields
    ? Object.values(fields).join(' · ')
    : (error.error?.mensaje ?? `No se pudo completar la operación (HTTP ${error.status}).`);
}
