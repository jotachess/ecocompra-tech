import { FieldError } from '../../core/field-error';
import { notBlank } from '../../core/form-validation';
import { ProductoService } from '../../core/services/productos.service';
import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CurrencyPipe, NgClass } from '@angular/common';
import {
  FormBuilder,
  ReactiveFormsModule,
  FormsModule,
  Validators,
  AbstractControl,
} from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import { apiError } from '../../core/http-error';
import { AuthService } from '../../core/auth.service';
import { Producto, Categoria } from '../../core/models';
@Component({
  selector: 'app-productos',
  imports: [FieldError, CurrencyPipe, NgClass, ReactiveFormsModule, FormsModule],
  templateUrl: './productos.html',
})
export class Productos implements OnInit {
  private productoService = inject(ProductoService);
  auth = inject(AuthService);
  private fb = inject(FormBuilder);
  rows = signal<Producto[]>([]);
  categories = signal<Categoria[]>([]);
  search = signal('');
  error = signal('');
  notice = signal('');
  busy = signal(false);
  editing = signal<number | null>(null);
  pendingDelete = signal<number | null>(null);
  filtered = computed(() =>
    this.rows().filter((p) =>
      p.nombre.toLocaleLowerCase().includes(this.search().toLocaleLowerCase()),
    ),
  );
  form = this.fb.group(
    {
      nombre: ['', [Validators.required, notBlank, Validators.maxLength(120)]],
      idCategoria: [null as number | null, Validators.required],
      descripcion: ['', Validators.maxLength(500)],
      precio: [
        null as number | null,
        [Validators.required, Validators.min(0), Validators.pattern(/^\d{1,8}(\.\d{1,2})?$/)],
      ],
      stock: [0, [Validators.required, Validators.min(0), Validators.max(2147483647), Validators.pattern(/^\d+$/)]],
      reparabilidad: ['', Validators.maxLength(500)],
      consumoKwh: [null as number | null, [Validators.min(0), Validators.pattern(/^\d{1,8}(\.\d{1,4})?$/)]],
      condicionesConsumo: ['', Validators.maxLength(500)],
      vidaUtilAnios: [null as number | null, [Validators.min(0.01), Validators.pattern(/^\d{1,3}(\.\d{1,2})?$/)]],
      fuenteTecnica: ['', Validators.maxLength(1000)],
    },
    {
      validators: (c: AbstractControl) => {
        const v = c.value;
        if (v.consumoKwh != null && !v.condicionesConsumo?.trim())
          return { sostenibilidad: 'El consumo necesita condiciones de medición.' };
        return (v.consumoKwh != null || v.vidaUtilAnios != null || v.reparabilidad?.trim()) &&
          !v.fuenteTecnica?.trim()
          ? { sostenibilidad: 'Agrega una fuente técnica para respaldar los datos.' }
          : null;
      },
    },
  );
  ngOnInit() {
    this.load();
  }
  load() {
    forkJoin({
      p: this.productoService.list(),
      c: this.productoService.categories(),
    }).subscribe({
      next: (r) => {
        this.rows.set(r.p);
        this.categories.set(r.c);
      },
      error: (e) => this.error.set(apiError(e)),
    });
  }
  edit(p: Producto) {
    this.editing.set(p.idProducto);
    this.form.reset({
      nombre: p.nombre,
      descripcion: p.descripcion ?? '',
      idCategoria: p.categoria.idCategoria,
      precio: p.precio,
      stock: p.stock,
      reparabilidad: p.reparabilidad ?? '',
      consumoKwh: p.consumoKwh ?? null,
      condicionesConsumo: p.condicionesConsumo ?? '',
      vidaUtilAnios: p.vidaUtilAnios ?? null,
      fuenteTecnica: p.fuenteTecnica ?? '',
    });
    this.error.set('');
    this.notice.set('');
  }
  reset() {
    this.editing.set(null);
    this.form.reset({ stock: 0 });
  }
  save() {
    if (this.busy()) return;
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.busy.set(true);
    this.notice.set('');
    this.error.set('');
    const v = this.form.getRawValue();
    const body = {
      ...v,
      nombre: v.nombre?.trim() ?? '',
      idCategoria: v.idCategoria!,
      precio: v.precio!,
      stock: v.stock!,
      reparabilidad: v.reparabilidad?.trim() || null,
      fuenteTecnica: v.fuenteTecnica?.trim() || null,
    };
    const request = this.editing()
      ? this.productoService.update(this.editing()!, body)
      : this.productoService.create(body);
    request.pipe(finalize(() => this.busy.set(false))).subscribe({
      next: () => {
        this.notice.set('Producto guardado correctamente.');
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
    this.error.set('');
    this.productoService.remove(id).pipe(finalize(() => this.busy.set(false))).subscribe({
      next: () => {
        this.notice.set('Producto eliminado.');
        this.pendingDelete.set(null);
        this.load();
      },
      error: (e) => {
        this.error.set(apiError(e));
        this.pendingDelete.set(null);
      },
    });
  }
}

