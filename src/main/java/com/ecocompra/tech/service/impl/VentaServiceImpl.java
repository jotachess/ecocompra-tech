package com.ecocompra.tech.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ecocompra.tech.dto.*;
import com.ecocompra.tech.entity.*;
import com.ecocompra.tech.exception.*;
import com.ecocompra.tech.repository.*;
import com.ecocompra.tech.service.VentaService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class VentaServiceImpl implements VentaService {
    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final DetalleVentaRepository detalleRepository;

    @Override @Transactional(readOnly=true)
    public List<Venta> listarTodas() { return ventaRepository.findAll(); }

    @Override @Transactional(readOnly=true)
    public Venta obtenerPorId(Long id) {
        return ventaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Venta", "id", id));
    }

    @Override
    public Venta registrarVenta(VentaRequestDto request) {
        return aplicar(Venta.builder().fecha(LocalDateTime.now()).detalles(new ArrayList<>()).build(), request);
    }

    @Override
    public Venta actualizar(Long id, VentaRequestDto request) {
        Venta venta=ventaRepository.bloquearPorId(id).orElseThrow(() -> new ResourceNotFoundException("Venta","id",id));
        return aplicar(venta, request);
    }

    private Map<Long,Producto> bloquearProductos(Venta venta, List<DetalleVentaRequestDto> nuevos) {
        Set<Long> ids=new TreeSet<>();
        venta.getDetalles().forEach(d -> ids.add(d.getProducto().getIdProducto()));
        nuevos.forEach(d -> ids.add(d.getIdProducto()));
        Map<Long,Producto> productos=new HashMap<>();
        for(Long id:ids) productos.put(id,productoRepository.bloquearPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto","id",id)));
        return productos;
    }

    private void devolverStock(Venta venta, Map<Long,Producto> productos) {
        for(DetalleVenta detalle:venta.getDetalles()) {
            Producto p=productos.get(detalle.getProducto().getIdProducto());
            try { p.setStock(Math.addExact(p.getStock(),detalle.getCantidad())); }
            catch(ArithmeticException ex) { throw new IllegalOperationException("El stock excede el limite permitido."); }
        }
    }

    private Venta aplicar(Venta venta, VentaRequestDto request) {
        Cliente cliente=clienteRepository.findById(request.getIdCliente())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente","id",request.getIdCliente()));
        if(request.getDetalles()==null || request.getDetalles().isEmpty())
            throw new IllegalOperationException("Una venta debe contener al menos un detalle de producto.");
        Map<Long,Producto> productos=bloquearProductos(venta,request.getDetalles());
        devolverStock(venta,productos);
        if(venta.getIdVenta()!=null) {
            var anteriores=new ArrayList<>(venta.getDetalles());
            venta.getDetalles().clear();
            detalleRepository.deleteAll(anteriores);
            detalleRepository.flush();
        }
        venta.getDetalles().clear();
        venta.setCliente(cliente);
        for(DetalleVentaRequestDto item:request.getDetalles()) {
            Producto p=productos.get(item.getIdProducto());
            if(item.getCantidad()==null || item.getCantidad()<1) throw new IllegalArgumentException("La cantidad debe ser positiva.");
            if(p.getStock()<item.getCantidad()) throw new IllegalOperationException("Stock insuficiente para "+p.getNombre());
            p.setStock(p.getStock()-item.getCantidad());
            BigDecimal precio=item.getPrecioUnitario()!=null?item.getPrecioUnitario():p.getPrecio();
            venta.agregarDetalle(DetalleVenta.builder().producto(p).cantidad(item.getCantidad()).precioUnitario(precio).build());
        }
        return ventaRepository.saveAndFlush(venta);
    }

    @Override
    public void eliminar(Long id) {
        Venta venta=ventaRepository.bloquearPorId(id).orElseThrow(() -> new ResourceNotFoundException("Venta","id",id));
        var productos=bloquearProductos(venta,List.of());
        devolverStock(venta,productos);
        var anteriores=new ArrayList<>(venta.getDetalles());
        venta.getDetalles().clear();
        detalleRepository.deleteAll(anteriores);
        detalleRepository.flush();
        venta.getDetalles().clear();
        ventaRepository.delete(venta);
        ventaRepository.flush();
    }

    @Override @Transactional(readOnly=true)
    public List<Venta> listarPorCliente(Long idCliente) {
        if(!clienteRepository.existsById(idCliente)) throw new ResourceNotFoundException("Cliente","id",idCliente);
        return ventaRepository.findByClienteIdCliente(idCliente);
    }

    @Override @Transactional(readOnly=true)
    public List<Venta> buscarPorRangoFechas(LocalDateTime desde, LocalDateTime hasta) {
        if(desde==null || hasta==null || desde.isAfter(hasta)) throw new IllegalArgumentException("Revise el rango de fechas.");
        return ventaRepository.buscarPorRangoFechasJPQL(desde,hasta);
    }
}
