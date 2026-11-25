export interface Categoria {
  idCategoria: number;
  nombre: string;
  descripcion?: string;
}
export interface Producto {
  idProducto: number;
  nombre: string;
  descripcion?: string;
  precio: number;
  stock: number;
  categoria: Categoria;
  reparabilidad?: string;
  consumoKwh?: number | null;
  condicionesConsumo?: string;
  vidaUtilAnios?: number | null;
  fuenteTecnica?: string;
}
export interface Cliente {
  idCliente: number;
  nombre: string;
  correo?: string | null;
  telefono?: string | null;
}
export interface Detalle {
  idDetalle: number;
  producto: Producto;
  cantidad: number;
  precioUnitario: number;
}
export interface Venta {
  idVenta: number;
  cliente: Cliente;
  fecha: string;
  detalles: Detalle[];
}
export interface Sesion {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  username: string;
  roles: string[];
}
export const API_URL = 'http://localhost:8080/api';

export interface ProductoRequest {
  nombre: string;
  idCategoria: number;
  precio: number;
  stock: number;
  descripcion?: string | null;
  reparabilidad?: string | null;
  consumoKwh?: number | null;
  condicionesConsumo?: string | null;
  vidaUtilAnios?: number | null;
  fuenteTecnica?: string | null;
}
export interface ClienteRequest {
  nombre: string;
  correo?: string | null;
  telefono?: string | null;
}
export interface VentaRequest {
  idCliente: number;
  detalles: { idProducto: number; cantidad: number; precioUnitario?: number | null }[];
}

export type RolUsuario = 'ADMIN' | 'VENDEDOR';
export interface Usuario { id: number; username: string; rol: RolUsuario; activo: boolean; }
export interface UsuarioCreate { username: string; password: string; rol: RolUsuario; }
export interface UsuarioUpdate { rol: RolUsuario; activo: boolean; }
