package com.example.proyectoFinal.services;

import com.example.proyectoFinal.dto.RegistrarVentaRequest;
import com.example.proyectoFinal.entities.*;
import com.example.proyectoFinal.repositories.BaseRepository;
import com.example.proyectoFinal.repositories.ClienteRepository;
import com.example.proyectoFinal.repositories.PagoRepository;
import com.example.proyectoFinal.repositories.VentaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

@Service
public class VentaServiceImpl extends BaseServiceImpl<Venta, Long> implements VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private FuncionService funcionService;

    public VentaServiceImpl(BaseRepository<Venta, Long> baseRepository) {
        super(baseRepository);
    }

    /**
     * Registra una venta de forma atómica:
     * 1. Valida que el cliente exista y no haya comprado ya para esa función.
     * 2. Delega a FuncionService la validación de asientos y creación de entradas.
     * 3. Persiste el Pago de forma independiente (sin cascade desde Venta).
     * 4. Crea y persiste la Venta con referencias directas a cliente, funcion y pago.
     * 5. Asigna fk_cine vía query nativa para evitar cascades sobre ventas previas.
     */
    @Override
    @Transactional
    public Venta registrarVenta(Long cineId, RegistrarVentaRequest request) throws Exception {
        // 1. Cargar y validar el cliente
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new Exception("No existe el cliente con id: " + request.getClienteId()));

        // 2. Verificar que el cliente no haya comprado ya para esta función
        Long comprasExistentes = ventaRepository.countComprasByClienteAndFuncion(
                request.getClienteId(),
                request.getFuncionId());
        if (comprasExistentes > 0) {
            throw new Exception("El cliente ya ha comprado entradas para esta función. " +
                    "Un cliente no puede comprar 2 veces para la misma función.");
        }

        // 3. Delegar validación de asientos y creación de entradas a FuncionService
        //    FuncionService persiste las entradas asociadas a la función
        Funcion funcion = funcionService.agregarEntradas(
                request.getFuncionId(),
                request.getAsientos(),
                request.getPrecioUnitario());

        // 4. Crear y persistir el Pago de forma independiente
        Pago pago = new Pago();
        pago.setMonto(request.getAsientos().size() * request.getPrecioUnitario());
        pago.setTipo(TipoPago.valueOf(request.getTipoPago().toUpperCase()));
        pagoRepository.save(pago);

        // 5. Crear y persistir la Venta
        //    Venta referencia directamente la Funcion (sin lista de entradas propia)
        Venta venta = new Venta();
        venta.setFecha(parseFecha(request.getFecha()));
        venta.setCliente(cliente);
        venta.setPago(pago);
        venta.setFuncion(funcion);
        Venta ventaGuardada = ventaRepository.save(venta);

        // 6. Asignar fk_cine vía query nativa para no disparar cascade sobre otras ventas
        ventaRepository.assignToCine(ventaGuardada.getId(), cineId);

        return ventaGuardada;
    }

    private Date parseFecha(String fechaStr) {
        try {
            LocalDate ld = LocalDate.parse(fechaStr);
            return Date.from(ld.atTime(12, 0).toInstant(ZoneOffset.UTC));
        } catch (Exception e) {
            return new Date();
        }
    }
}
