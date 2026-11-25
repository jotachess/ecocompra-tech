import { HttpErrorResponse } from '@angular/common/http';
export function apiError(error: HttpErrorResponse): string {
  if (!error.status)
    return 'No se pudo conectar con el servidor. Verifica que el sistema esté iniciado.';
  const payload = error.error;
  const fields = payload?.validaciones;
  const messages = fields && typeof fields === 'object'
    ? Object.values(fields).filter((v): v is string => typeof v === 'string' && !!v.trim()) : [];
  if (messages.length) return messages.join(' · ');
  if (typeof payload?.mensaje === 'string' && payload.mensaje.trim()) return payload.mensaje;
  if (error.status === 401) return 'La sesión no es válida. Ingresa nuevamente.';
  if (error.status === 403) return 'Tu cuenta no tiene permiso para esta operación.';
  if (error.status >= 500) return 'El servidor no pudo completar la operación. Intenta nuevamente.';
  return `No se pudo completar la operación (HTTP ${error.status}).`;
}
